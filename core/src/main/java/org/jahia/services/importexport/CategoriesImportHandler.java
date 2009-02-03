/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.importexport;

import org.apache.log4j.Logger;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLEntry;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.pages.ContentPage;
import org.jahia.utils.LanguageCodeConverters;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:47
 * To change this template use File | Settings | File Templates.
 */
public class CategoriesImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(CategoriesImportHandler.class);
    private Stack cats = new Stack();
    private CategoryService cs ;
    private JahiaSite site;
    private List uuidProps = new ArrayList();

    public CategoriesImportHandler(ProcessingContext jParams) {
        site = jParams.getSite();
        cs = ServicesRegistry.getInstance().getCategoryService();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (localName.equals("category")) {
                Category c;
                String key = attributes.getValue(ImportExportBaseService.JAHIA_URI, "key");
                if (cats.isEmpty()) {
                    c = cs.getRootCategory();
                } else {
                    c =  cs.getCategory(key);
                    Category parent = ((Category) cats.peek());
                    if (c == null) {
                        c = Category.createCategory(key,parent);
                    } else {
                        List parents = c.getParentObjectKeys();
                        if (!parents.contains(parent.getObjectKey())) {
                            parent.addChildObjectKey(c.getObjectKey());
                            for (Iterator iterator = parents.iterator(); iterator.hasNext();) {
                                ObjectKey k = (ObjectKey) iterator.next();
                                ((Category)Category.getChildInstance(k,null)).removeChildObjectKey(c.getObjectKey());
                            }
                        }
                    }
                }
                for (int i = 0; i < attributes.getLength(); i++) {
                    String k = (String) attributes.getLocalName(i);
                    if (k.startsWith("title_")) {
                        c.setTitle(LanguageCodeConverters.languageCodeToLocale(k.substring(6)), attributes.getValue(i));
                    }
                }
                String acl = attributes.getValue(ImportExportBaseService.JAHIA_URI, "acl");
                if (acl != null) {
                    fillAcl(c.getACL(), acl);
                }
                cats.push(c);
            }
            if (localName.equals("property")) {
                String key = attributes.getValue(ImportExportBaseService.JAHIA_URI, "key");
                String value = attributes.getValue(ImportExportBaseService.JAHIA_URI, "value");
                if (!cats.empty()) {
                    Category c = (Category) cats.peek();
                    if (key != null && value != null) {
                        if (key.startsWith("homepage")) {
                            List l = findPage(value);
                            if (!l.isEmpty()) {
                                value = "" + ((ContentPage) l.iterator().next()).getID();
                            } else {
                                uuidProps.add(new String[] {c.getKey(),key,value});
                            }
                        }
                        c.setProperty(key, value);
                    }
                }
            }
        } catch (JahiaException e) {
            throw new SAXException(e);
        }
    }

    private void fillAcl(JahiaBaseACL jAcl, String acl) {
        StringTokenizer st = new StringTokenizer(acl, "|");
        try {
            while (st.hasMoreTokens())  {
                String ace = st.nextToken();
                if (ace.equals("break")) {
                    jAcl.setInheritance(1);
                } else {
                    int colonIndex = ace.lastIndexOf(":");
                    String perm = ace.substring(colonIndex+1);

                    JahiaAclEntry permissions = new JahiaAclEntry ();
                    permissions.setPermission (JahiaBaseACL.READ_RIGHTS, perm.charAt (0) == 'r' ?
                            JahiaACLEntry.ACL_YES :
                            JahiaACLEntry.ACL_NO);
                    permissions.setPermission (JahiaBaseACL.WRITE_RIGHTS, perm.charAt (1) == 'w' ?
                            JahiaACLEntry.ACL_YES :
                            JahiaACLEntry.ACL_NO);
                    permissions.setPermission (JahiaBaseACL.ADMIN_RIGHTS, perm.charAt (2) == 'a' ?
                            JahiaACLEntry.ACL_YES :
                            JahiaACLEntry.ACL_NO);
                    String principal = ace.substring(0, colonIndex);

                    String userName = principal.substring(2);
                    if (principal.charAt(0) == 'u') {
                        try {
                            JahiaUser user = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), userName);
                            if (user == null) {
                                user = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userName);
                            }
                            if (user != null) {
                                jAcl.setUserEntry (user, permissions);
                            }
                        } catch (JahiaException e) {
                        }
                    } else {
                        JahiaGroup group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(site.getID(), userName);
                        if (group == null) {
                            group = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(userName);
                        }
                        if (group != null) {
                            jAcl.setGroupEntry (group, permissions);
                        }
                    }
                }
            }
        } catch (JahiaACLException jae) {
            logger.error ("Cannot set user or group ACL entry !!", jae);
        }
    }


    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName.equals("category")) {
            cats.pop();
        }
    }

    public List getUuidProps() {
        return uuidProps;
    }

    public void setUuidProps(List p) {
        if (p == null) {
            return;
        }
        for (Iterator iterator = p.iterator(); iterator.hasNext();) {
            try {
                String[] s = (String[]) iterator.next();
                Category c = Category.getCategory(s[0]);
                List l = findPage(s[2]);
                if (!l.isEmpty()) {
                    String value = "" + ((ContentPage) l.iterator().next()).getID();
                    c.setProperty(s[1], value);
                }
            } catch (JahiaException e) {
                logger.error("Cannot set property for category ",e);
            }
        }
    }

    private List findPage(String v) throws JahiaException {
        List l;
//        if (v.indexOf('/')>0) {
//            l = ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v.substring(0,v.indexOf('/')));
//            l.addAll(ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v.substring(0,v.indexOf('/')+1)));
//        } else {
            l = ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v);
//        }
        return l;
    }

}

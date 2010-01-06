/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.importexport;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.*;
import org.jahia.services.pages.ContentPage;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 * To change this template use File | Settings | File Templates.
 */
public class UsersImportHandler  extends DefaultHandler {
    private static Logger logger = Logger.getLogger(UsersImportHandler.class);
    private JahiaUserManagerService u;
    private JahiaGroupManagerService g;
    private JahiaSite site;
    private List<String[]> uuidProps = new ArrayList<String[]>();

    private JahiaGroup currentGroup = null;
    private boolean member = false;

    public UsersImportHandler(JahiaSite site) {
        this.site = site;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public UsersImportHandler() {        
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (currentGroup == null) {
                if (localName.equals("user")) {
                    String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                    String pass = null;
                    Properties p = new Properties();

                    for (int i = 0; i < attributes.getLength(); i++) {
                        String k = attributes.getLocalName(i);
                        String v = attributes.getValue(i);
                        if (k.equals("name")) {
                            //
                        } else if (k.equals("password")) {
                            pass = v;
                        } else if (k.equals("user_homepage")) {
                            List<ContentPage> l = findPage(v);
                            if (!l.isEmpty()) {
                                p.put(k, ""+((ContentPage) l.iterator().next()).getID());
                            } else {
                                uuidProps.add(new String[] {name,k,v});
                            }
                        } else {
                            p.put(k,v);
                        }
                    }
                    if (name != null && pass != null) {
                        if (u.lookupUser(name) == null) {
                            u.createUser(name, pass, p);
                        }
                    }
                } else if (localName.equals("group")) {
                    String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                    Properties p = new Properties();
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String k = attributes.getLocalName(i);
                        String v = attributes.getValue(i);
                        if (k.equals("name")) {
                            //
                        } else if (k.equals("group_homepage")) {
                            List<ContentPage> l = findPage(v);
                            if (!l.isEmpty()) {
                                p.put(k, ""+((ContentPage) l.iterator().next()).getID());
                            } else {
                                uuidProps.add(new String[] {name,k,v});
                            }
                        } else {
                            p.put(k,v);
                        }
                    }
                    if (name != null) {
                        currentGroup = g.lookupGroup(site.getID(), name);
                        if (currentGroup == null) {
                            currentGroup = g.createGroup(site.getID(), name, p, false);
                        }
                    }
                }
            } else {
                member = true;
                Principal p = null;
                String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                if (localName.equals("user")) {
                    p = u.lookupUser(name);
                } else if (localName.equals("group")) {
                    p = g.lookupGroup(site.getID(), name);
                }
                if (p != null && !currentGroup.getMembers().contains(p)) {
                    currentGroup.addMember(p);
                }
            }
        } catch (JahiaException e) {
            logger.error("Cannot import", e);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (member) {
            member = false;
        } else if (localName.equals("group")) {
            currentGroup = null;
        }
    }


    public List<String[]> getUuidProps() {
        return uuidProps;
    }

    public void setUuidProps(List<String[]> p) {
        if (p == null) {
            return;
        }

        for (Iterator<String[]> iterator = p.iterator(); iterator.hasNext();) {
            try {
                String[] s = (String[]) iterator.next();
                List<ContentPage> l = findPage(s[2]);
                if (!l.isEmpty()) {
                    int id = ((ContentPage) l.iterator().next()).getID();
                    if (s[1].equals("user_homepage")) {
                        JahiaUser user = u.lookupUser(s[0]);
                        user.setHomepageID(id);
                    } else if (s[1].equals("group_homepage")) {
                        JahiaGroup group = g.lookupGroup(site.getID(), s[0]);
                        group.setHomepageID(id);
                    }
                }
            } catch (JahiaException e) {
                logger.error("Cannot set property for category ",e);
            }
        }
    }

    private List<ContentPage> findPage(String v) throws JahiaException {
        List<ContentPage> l;
//        if (v.indexOf('/')>0) {
//            l = ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v.substring(0,v.indexOf('/')));
//            l.addAll(ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v.substring(0,v.indexOf('/')+1)));
//        } else {
            l = ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", v);
//        }
        return l;
    }

}

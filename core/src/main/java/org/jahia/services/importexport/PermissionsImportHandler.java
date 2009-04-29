/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLInfo;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 * To change this template use File | Settings | File Templates.
 */
public class PermissionsImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(UsersImportHandler.class);
    private JahiaUserManagerService u;
    private JahiaGroupManagerService g;
    private JahiaSiteUserManagerService su;
    private JahiaSitesService siteService;
    private int siteId;
    private String prefix;
    private List uuidProps = new ArrayList();

    private String nodeName;
    private Map siteKeyMapping;

    public PermissionsImportHandler(String prefix, String nodeName, int siteId) {
        this.prefix = prefix;
        this.nodeName = nodeName;
        this.siteId = siteId;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        su = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
        siteService = ServicesRegistry.getInstance().getJahiaSitesService();
    }

    public PermissionsImportHandler(String prefix, String nodeName, Map siteKeyMapping) {
        this(prefix, nodeName, 0);
        this.siteKeyMapping = siteKeyMapping != null ? siteKeyMapping : Collections.emptyMap();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName.equals(nodeName)) {
            boolean importingServerPermissions = "serverPermission".equals(nodeName); 
            String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
            String userlist = attributes.getValue(ImportExportBaseService.JAHIA_URI, "acl");

            JahiaAclName aclName = ServicesRegistry.getInstance().getJahiaACLManagerService().getJahiaAclName(prefix+"."+name, siteId);
            JahiaAcl jAcl = aclName.getAcl();
            jAcl.clearEntries(ACLInfo.USER_TYPE_ENTRY);
            jAcl.clearEntries(ACLInfo.GROUP_TYPE_ENTRY);
            JahiaAclEntry permissions = new JahiaAclEntry (1,0); // grant rwa , no tristate

            if (userlist != null && userlist.length()>0) {
                StringTokenizer st = new StringTokenizer(userlist, "|");
                while (st.hasMoreElements()) {
                    String principal = st.nextToken();
                    String username = principal.substring(2);
                    int siteId = this.siteId;
                    int pos = username.indexOf(':');
                    if (importingServerPermissions && pos != -1 && pos < username.length()) {
                        String siteKey = username.substring(pos + 1);
                        username = username.substring(0, pos);
                        if (!siteKey.equals("0")) {
                            try {
                                JahiaSite site = null;
                                if (siteKeyMapping.containsKey(siteKey)) {
                                    // the site key was changed during import
                                    // (there is an existing site with the same
                                    // key)
                                    site = siteService
                                            .getSiteByKey((String) siteKeyMapping
                                                    .get(siteKey));
                                }
                                if (null == site) {
                                    site = siteService.getSiteByKey(siteKey);
                                }
                                if (site != null) {
                                    siteId = site.getID();
                                }
                            } catch (JahiaException e) {
                                logger.warn("Site cannot be found for key '"
                                        + siteKey + "'", e);
                            }
                        } else {
                            siteId = 0;
                        }
                    }
                    
                    if (principal.charAt(0) == 'u') {
                        JahiaUser user = su.getMember(siteId, username);
                        if (user != null) {
                            jAcl.setUserEntry(user, permissions);
                        } else {
                            logger
                                    .warn("User '"
                                            + username
                                            + "' cannot be found for site with ID '"
                                            + siteId + "'");
                        }
                    } else {
                        JahiaGroup group = g.lookupGroup(siteId, username);
                        if (group != null) {
                            jAcl.setGroupEntry(group, permissions);
                        } else {
                            logger
                                    .warn("Group '"
                                            + username
                                            + "' cannot be found for site with ID '"
                                            + siteId + "'");
                        }
                    }
                }
            }
            aclName.setAcl(jAcl);
            ServicesRegistry.getInstance().getJahiaACLManagerService().updateCache(jAcl);
        }
    }

    public void endDocument() throws SAXException {
        ServicesRegistry.getInstance().getJahiaACLManagerService().flushCache();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // do nothing
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
                List l = ServicesRegistry.getInstance().getJahiaPageService().findPagesByPropertyNameAndValue("originalUuid", s[2]);
                if (!l.isEmpty()) {
                    int id = ((ContentPage) l.iterator().next()).getID();
                    if (s[1].equals("user_homepage")) {
                        JahiaUser user = u.lookupUser(s[0]);
                        user.setHomepageID(id);
                    } else if (s[1].equals("group_homepage")) {
                        JahiaGroup group = g.lookupGroup(siteId, s[0]);
                        group.setHomepageID(id);
                    }
                }
            } catch (JahiaException e) {
                logger.error("Cannot set property for category ",e);
            }
        }
    }

}
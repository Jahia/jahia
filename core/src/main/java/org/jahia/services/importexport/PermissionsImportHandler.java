/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.importexport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 */
public class PermissionsImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PermissionsImportHandler.class);
    private JahiaUserManagerService u;
    private JahiaGroupManagerService g;
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
        siteService = ServicesRegistry.getInstance().getJahiaSitesService();
    }

    public PermissionsImportHandler(String prefix, String nodeName, Map siteKeyMapping) {
        this(prefix, nodeName, 0);
        this.siteKeyMapping = siteKeyMapping != null ? siteKeyMapping : Collections.emptyMap();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//        if (localName.equals(nodeName)) {
//            boolean importingServerPermissions = "serverPermission".equals(nodeName); 
//            String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
//            String userlist = attributes.getValue(ImportExportBaseService.JAHIA_URI, "acl");
//
//            JahiaAclName aclName = ServicesRegistry.getInstance().getJahiaACLManagerService().getJahiaAclName(prefix+"."+name, siteId);
//            JahiaAcl jAcl = aclName.getAcl();
//            jAcl.clearEntries(ACLInfo.USER_TYPE_ENTRY);
//            jAcl.clearEntries(ACLInfo.GROUP_TYPE_ENTRY);
//            JahiaAclEntry permissions = new JahiaAclEntry (1,0); // grant rwa , no tristate
//
//            if (userlist != null && userlist.length()>0) {
//                StringTokenizer st = new StringTokenizer(userlist, "|");
//                while (st.hasMoreElements()) {
//                    String principal = st.nextToken();
//                    String username = principal.substring(2);
//                    int siteId = this.siteId;
//                    int pos = username.indexOf(':');
//                    if (importingServerPermissions && pos != -1 && pos < username.length()) {
//                        String siteKey = username.substring(pos + 1);
//                        username = username.substring(0, pos);
//                        if (!siteKey.equals("0")) {
//                            try {
//                                JahiaSite site = null;
//                                if (siteKeyMapping.containsKey(siteKey)) {
//                                    // the site key was changed during import
//                                    // (there is an existing site with the same
//                                    // key)
//                                    site = siteService
//                                            .getSiteByKey((String) siteKeyMapping
//                                                    .get(siteKey));
//                                }
//                                if (null == site) {
//                                    site = siteService.getSiteByKey(siteKey);
//                                }
//                                if (site != null) {
//                                    siteId = site.getID();
//                                }
//                            } catch (JahiaException e) {
//                                logger.warn("Site cannot be found for key '"
//                                        + siteKey + "'", e);
//                            }
//                        } else {
//                            siteId = 0;
//                        }
//                    }
//                    
//                    if (principal.charAt(0) == 'u') {
//                        JahiaUser user = su.getMember(siteId, username);
//                        if (user != null) {
//                            jAcl.setUserEntry(user, permissions);
//                        } else {
//                            logger
//                                    .warn("User '"
//                                            + username
//                                            + "' cannot be found for site with ID '"
//                                            + siteId + "'");
//                        }
//                    } else {
//                        JahiaGroup group = g.lookupGroup(siteId, username);
//                        if (group != null) {
//                            jAcl.setGroupEntry(group, permissions);
//                        } else {
//                            logger
//                                    .warn("Group '"
//                                            + username
//                                            + "' cannot be found for site with ID '"
//                                            + siteId + "'");
//                        }
//                    }
//                }
//            }
//            aclName.setAcl(jAcl);
//            ServicesRegistry.getInstance().getJahiaACLManagerService().updateCache(jAcl);
//        }
    }

    public void endDocument() throws SAXException {
        // ServicesRegistry.getInstance().getJahiaACLManagerService().flushCache();
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        // do nothing
    }

}
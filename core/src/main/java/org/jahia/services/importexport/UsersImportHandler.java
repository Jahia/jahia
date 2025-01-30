/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Import handler for the legacy user/groups.
 *
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 * @deprecated Legacy import code
 */
@Deprecated(since = "8.2.1.0", forRemoval = true)
public class UsersImportHandler extends DefaultHandler {

    private static Logger LOG = LoggerFactory.getLogger(UsersImportHandler.class);

    private JahiaUserManagerService u;
    private JahiaGroupManagerService g;
    private JahiaSite site;
    private List<String[]> uuidProps = new ArrayList<String[]>();

    private JCRGroupNode currentGroup = null;
    private boolean member = false;

    private JCRSessionWrapper session;

    public UsersImportHandler(JahiaSite site, JCRSessionWrapper session) {
        this.site = site;
        this.session = session;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public UsersImportHandler(JCRSessionWrapper session) {
        this.session = session;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
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
                        uuidProps.add(new String[]{name, k, v});
                    } else {
                        p.put(k, v);
                    }
                }
                if (name != null && pass != null) {
                    if (!u.userExists(name)) {
                        u.createUser(name, pass, p, session);
                        LOG.debug("Created user {}", name);
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
                        uuidProps.add(new String[]{name, k, v});
                    } else {
                        p.put(k, v);
                    }
                }
                if (name != null) {
                    String siteKey = site.getSiteKey();
                    currentGroup = g.lookupGroup(siteKey, name, session);
                    if (currentGroup == null) {
                        currentGroup = g.createGroup(siteKey, name, p, false, session);
                        LOG.debug("Created group {} for site {}", name, siteKey);
                    }
                }
            }
        } else {
            member = true;
            JCRNodeWrapper p = null;
            String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
            if (localName.equals("user")) {
                p = u.lookupUser(name);
            } else if (localName.equals("group")) {
                p = g.lookupGroup(site.getSiteKey(), name, session);
            }
            if (p != null && !currentGroup.getMembers().contains(p)) {
                currentGroup.addMember(p);
                LOG.debug("Added member {} for group {}", name, currentGroup.getName());
            }
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
    }

}

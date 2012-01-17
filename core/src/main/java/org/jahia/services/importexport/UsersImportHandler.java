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

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * User: toto
 * Date: 5 juin 2006
 * Time: 17:57:36
 * 
 */
public class UsersImportHandler extends DefaultHandler {
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
                        uuidProps.add(new String[]{name, k, v});
                    } else {
                        p.put(k, v);
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

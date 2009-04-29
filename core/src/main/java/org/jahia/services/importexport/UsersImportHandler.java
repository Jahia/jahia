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
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

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
    private JahiaSiteUserManagerService su;
    private JahiaSiteGroupManagerService sg;
    private JahiaSite site;
    private List<String[]> uuidProps = new ArrayList<String[]>();

    private JahiaGroup currentGroup = null;
    private boolean member = false;

    public UsersImportHandler(JahiaSite site) {
        this.site = site;
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        su = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
        sg = ServicesRegistry.getInstance().getJahiaSiteGroupManagerService();
    }

    public UsersImportHandler() {        
        u = ServicesRegistry.getInstance().getJahiaUserManagerService();
        g = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        su = ServicesRegistry.getInstance().getJahiaSiteUserManagerService();
        sg = ServicesRegistry.getInstance().getJahiaSiteGroupManagerService();
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
                        if (site == null) {
                            u.createUser(name, pass, p);
                        } else {
                            if (su.getMember(site.getID(), name) == null) {
                                JahiaUser user = u.lookupUser(name);
                                if (user == null) {
                                    user = u.createUser(name, pass, p);
                                }
                                su.addMember(site.getID(), user);
                            }
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
                            currentGroup = g.createGroup(site.getID(), name, p);
                            sg.addGroup(site.getID(), currentGroup);
                        }
                    }
                }
            } else {
                member = true;
                Principal p = null;
                String name = attributes.getValue(ImportExportBaseService.JAHIA_URI, "name");
                if (localName.equals("user")) {
                    p = su.getMember(site.getID(), name);
                } else if (localName.equals("group")) {
                    p = g.lookupGroup(site.getID(), name);
                }
                if (p != null) {
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

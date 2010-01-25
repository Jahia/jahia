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
package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNewPortletInstance;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Mashup management helper.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:43:33 PM
 */
public class MashupHelper {
    private static Logger logger = Logger.getLogger(MashupHelper.class);

    private ApplicationsManagerService applicationsManager;

    private NavigationHelper navigation;
    private ACLHelper acl;
    private ContentManagerHelper contentManager;

    public void setApplicationsManager(ApplicationsManagerService applicationsManager) {
        this.applicationsManager = applicationsManager;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setAcl(ACLHelper acl) {
        this.acl = acl;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public List<GWTJahiaPortletDefinition> searchPortlets(String match, JahiaUser user, Locale locale) {
        List<GWTJahiaPortletDefinition> results = new ArrayList<GWTJahiaPortletDefinition>();
        try {
            List<ApplicationBean> appList = new LinkedList<ApplicationBean>();
            if (match != null) {
                ApplicationBean app = applicationsManager.getApplication(match);
                if (app != null) {
                    appList.add(app);
                } else {
                    logger.warn("Application not found for the UUID '" + match + "'");
                }
            } else {
                appList.addAll(applicationsManager.getApplications());
            }
            for (ApplicationBean appBean : appList) {
                if (JCRContentUtils.hasPermission(user, Constants.JCR_READ_RIGHTS, appBean.getID())) {
                    List<EntryPointDefinition> l = appBean.getEntryPointDefinitions();
                    for (EntryPointDefinition aL : l) {
                        results.add(createGWTJahiaPortletDefinition(appBean, aL, locale));
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    /**
     * Create a GWTJahiaPortletDefinition object from an applicationBean and an entryPointDefinition objects
     *
     * @param appBean              the application bean
     * @param entryPointDefinition the entry point definition
     * @param locale
     * @return the portlet definition
     * @throws org.jahia.exceptions.JahiaException
     *          sthg bad happened
     */
    public GWTJahiaPortletDefinition createGWTJahiaPortletDefinition(ApplicationBean appBean, EntryPointDefinition entryPointDefinition, Locale locale) throws JahiaException {
        String portletType = null;
        int expTime = 0;
        String cacheScope = null;
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            PortletEntryPointDefinition portletEntryPointDefinition = ((PortletEntryPointDefinition) entryPointDefinition);
            portletType = portletEntryPointDefinition.getInitParameter("portletType");
            expTime = portletEntryPointDefinition.getExpirationCache();
            cacheScope = portletEntryPointDefinition.getCacheScope();
        }
        if (portletType == null) {
            portletType = "jnt:portlet";
        }
        GWTJahiaNodeACL gwtJahiaNodeACL = new GWTJahiaNodeACL(new ArrayList<GWTJahiaNodeACE>());
        gwtJahiaNodeACL.setAvailablePermissions(JCRPortletNode.getAvailablePermissions(appBean.getContext(), entryPointDefinition.getName()));
        return new GWTJahiaPortletDefinition(appBean.getID(), appBean.getContext(), entryPointDefinition.getName(), entryPointDefinition.getDisplayName(locale), portletType, gwtJahiaNodeACL, entryPointDefinition.getDescription(locale), expTime, cacheScope);
    }

    /**
     * Create a  GWTJahiaNode object that represents a portlet instance.
     *
     * @param parentPath                 where to create the node
     * @param gwtJahiaNewPortletInstance the portlet instance
     * @param currentUserSession
     * @return a node
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public GWTJahiaNode createPortletInstance(String parentPath, GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            String name = gwtJahiaNewPortletInstance.getInstanceName();

            if (name == null) {
                name = gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName().replaceAll("/", "___") + Math.round(Math.random() * 1000000l);
            }
            contentManager.checkName(name);
            if (contentManager.checkExistence(parentPath + "/" + name, currentUserSession)) {
                throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
            }
            JCRNodeWrapper parentNode = currentUserSession.getNode(parentPath);
            if (!parentNode.isCheckedOut()) {
                parentNode.checkout();
            }

            JCRPortletNode node = (JCRPortletNode) contentManager.addNode(parentNode, name, gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getPortletType(), null, gwtJahiaNewPortletInstance.getProperties());

            node.setApplication(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getApplicationId(), gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName());
            node.revokeAllPermissions();

            // set modes permissions
            if (gwtJahiaNewPortletInstance.getModes() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getModes().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }

            // set roles permissions
            if (gwtJahiaNewPortletInstance.getRoles() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getRoles().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }
            node.changePermissions("g:users", "rw");
            node.changePermissions("g:guest", "r-");
            try {
                parentNode.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException("A system error happened");
            }
            return navigation.getGWTJahiaNode(node);
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("error");
        }
    }

    /**
     * Create a portlet instance
     *
     * @param parentPath
     * @param instanceName
     * @param appName
     * @param entryPointName
     * @param gwtJahiaNodeProperties
     * @param site
     * @param currentUserSession
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public GWTJahiaNode createPortletInstance(String parentPath, String instanceName, String appName, String entryPointName, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties, JahiaSite site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            // get RSS GWTJahiaPortletDefinition
            GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, entryPointName, currentUserSession.getLocale(), currentUserSession.getUser());
            if (gwtJahiaPortletDefinition == null) {
                logger.error("[" + appName + "," + entryPointName + "]" + " portlet defintion not found --> Aboard creating  portlet instance");
            }

            GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
            gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

            // add url property
            gwtJahiaNewPortletInstance.getProperties().addAll(gwtJahiaNodeProperties);
            gwtJahiaNewPortletInstance.getProperties().add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));

            GWTJahiaNodeACL acl = gwtJahiaPortletDefinition.getBaseAcl();

            // all modes for users of the current site
            GWTJahiaNodeACL modes = gwtJahiaNewPortletInstance.getModes();
            if (modes == null) {
                modes = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> modeAces = modes.getAce();
            if (modeAces == null) {
                modeAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> modesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.MODES_ACL);
                modeAces.add(this.acl.createUsersGroupACE(modesPermissions, true, site));
            }
            modes.setAce(modeAces);
            gwtJahiaNewPortletInstance.setModes(modes);

            // all rodes for users of the current site
            GWTJahiaNodeACL roles = gwtJahiaNewPortletInstance.getRoles();
            if (roles == null) {
                roles = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> roleAces = roles.getAce();
            if (roleAces == null) {
                roleAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> rolesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.ROLES_ACL);
                roleAces.add(this.acl.createUsersGroupACE(rolesPermissions, true, site));
            }
            roles.setAce(roleAces);
            gwtJahiaNewPortletInstance.setRoles(roles);

            // set name
            gwtJahiaNewPortletInstance.setInstanceName(instanceName);
            return createPortletInstance(parentPath, gwtJahiaNewPortletInstance, currentUserSession);
        } catch (GWTJahiaServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to create an RSS portlet due to: ", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Create an instance of an RSS portlet
     *
     * @param parentPath
     * @param name
     * @param url
     * @param site
     * @param currentUserSession
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public GWTJahiaNode createRSSPortletInstance(String parentPath, String name, String url, JahiaSite site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        final String appName = Jahia.getContextPath().length() > 0 ? Jahia.getContextPath().substring(1) + "/rss" : "rss";
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaRSSPortlet", currentUserSession.getLocale(), currentUserSession.getUser());
        if (gwtJahiaPortletDefinition == null) {
            logger.error("RSS portlet defintion not found --> Abort creating RSS portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("url", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaRSSPortlet", gwtJahiaNodeProperties, site, currentUserSession);
    }

    /**
     * Create a google gadget node
     *
     * @param parentPath
     * @param name
     * @param script
     * @param site
     * @param currentUserSession
     * @return
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *
     */
    public GWTJahiaNode createGoogleGadgetPortletInstance(String parentPath, String name, String script, JahiaSite site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        final String appName = Jahia.getContextPath().length() > 0 ? Jahia.getContextPath().substring(1) + "/googlegadget" : "googlegadget";
        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaGoogleGadget", currentUserSession.getLocale(), currentUserSession.getUser());
        if (gwtJahiaPortletDefinition == null) {
            logger.error("Google gadget portlet defintion not found --> Abort creating Google Gadget portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue("", GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("code", new GWTJahiaNodePropertyValue(script, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaGoogleGadget", gwtJahiaNodeProperties, site, currentUserSession);
    }

    /**
     * @param appName
     * @param entryPointName
     * @param locale
     * @param user
     * @return
     */
    public GWTJahiaPortletDefinition createJahiaGWTPortletDefinitionByName(String appName, String entryPointName, Locale locale, JahiaUser user) {
        if (appName != null && entryPointName != null) {
            try {
                // TO DO: replace this part of the method by a more perfoming one
                List<ApplicationBean> appList = applicationsManager.getApplications();
                for (ApplicationBean anAppList : appList) {
                    if (JCRContentUtils.hasPermission(user, Constants.JCR_READ_RIGHTS, anAppList.getID())) {
                        List<EntryPointDefinition> l = anAppList.getEntryPointDefinitions();
                        for (EntryPointDefinition aL : l) {
                            boolean foundEntryPointDefinition = appName.equalsIgnoreCase(anAppList.getName()) && aL.getName().equalsIgnoreCase(entryPointName);
                            if (foundEntryPointDefinition) {
                                return createGWTJahiaPortletDefinition(anAppList, aL, locale);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("Portlet definition not found: " + appName);
        return null;
    }
}

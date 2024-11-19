/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

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
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Portlet management helper.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:43:33 PM
 */
public class PortletHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PortletHelper.class);

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

    public List<GWTJahiaPortletDefinition> searchPortlets(String match, Locale locale, JCRSessionWrapper session, Locale uiLocale) {
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
                if (JCRContentUtils.hasPermission(session.getWorkspace().getName(), Constants.JCR_READ_RIGHTS, appBean.getID())) {
                    List<EntryPointDefinition> l = appBean.getEntryPointDefinitions();
                    for (EntryPointDefinition aL : l) {
                        results.add(createGWTJahiaPortletDefinition(appBean, aL, locale, uiLocale));
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
    public GWTJahiaPortletDefinition createGWTJahiaPortletDefinition(ApplicationBean appBean, EntryPointDefinition entryPointDefinition, Locale locale, Locale uiLocale) throws JahiaException {
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
// todo : restore portlet permissions
//        gwtJahiaNodeACL.setAvailablePermissions(JCRPortletNode.getAvailablePermissions(appBean.getContext(), entryPointDefinition.getName()));
        return new GWTJahiaPortletDefinition(appBean.getID(), appBean.getContext(), entryPointDefinition.getName(), entryPointDefinition.getDisplayName(locale), portletType, gwtJahiaNodeACL, entryPointDefinition.getDescription(locale), expTime, cacheScope);
    }

    /**
     * Create a  GWTJahiaNode object that represents a portlet instance.
     *
     *
     * @param parentPath                 where to create the node
     * @param gwtJahiaNewPortletInstance the portlet instance
     * @param currentUserSession
     * @param uiLocale
     * @return a node
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public GWTJahiaNode createPortletInstance(String parentPath, GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        try {
            String name = gwtJahiaNewPortletInstance.getInstanceName();

            if (name == null) {
                name = Patterns.SLASH.matcher(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName()).replaceAll("___") + Math.round(Math.random() * 1000000l);
            }
            if (contentManager.checkExistence(parentPath + "/" + name, currentUserSession, uiLocale)) {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.already.exists.with.name",uiLocale, name));
            }
            JCRNodeWrapper parentNode = currentUserSession.getNode(parentPath);
            if (!parentNode.isCheckedOut()) {
                parentNode.checkout();
            }

            JCRPortletNode node = (JCRPortletNode) contentManager.addNode(parentNode, name, gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getPortletType(), null, gwtJahiaNewPortletInstance.getProperties(), uiLocale, httpSessionID);

            node.setApplication(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getApplicationId(), gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName());
            node.revokeAllRoles();

            // set modes permissions
            if (gwtJahiaNewPortletInstance.getModes() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getModes().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
//                        node.changeRoles(user, ace.getPermissions());
                    }
                }
            }

            // set roles permissions
            if (gwtJahiaNewPortletInstance.getRoles() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getRoles().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
//                        node.changeRoles(user, ace.getPermissions());
                    }
                }
            }
            // todo default permissions .. ?
//            node.changeRoles("g:users", "rw");
//            node.changeRoles("g:guest", "r-");
            try {
                parentNode.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.system.error.happened",uiLocale));
            }
            return navigation.getGWTJahiaNode(node);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error",uiLocale));
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
    public GWTJahiaNode createPortletInstance(String parentPath, String instanceName, String appName, String entryPointName, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties, JCRSiteNode site, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        try {
            // get RSS GWTJahiaPortletDefinition
            GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, entryPointName, currentUserSession.getLocale(), currentUserSession.getUser(), currentUserSession.getWorkspace().getName(), uiLocale);
            if (gwtJahiaPortletDefinition == null) {
                throw new GWTJahiaServiceException("[" + appName + "," + entryPointName + "]" + " portlet defintion not found --> Aboard creating  portlet instance");
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
            if (acl != null && acl.getAvailableRoles() != null) {
                List<String> modesPermissions = acl.getAvailableRoles().get(JCRClientUtils.PORTLET_MODES_ROLES);
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
            if (acl != null && acl.getAvailableRoles() != null) {
                List<String> rolesPermissions = acl.getAvailableRoles().get(JCRClientUtils.PORTLET_ROLES);
                roleAces.add(this.acl.createUsersGroupACE(rolesPermissions, true, site));
            }
            roles.setAce(roleAces);
            gwtJahiaNewPortletInstance.setRoles(roles);

            // set name
            gwtJahiaNewPortletInstance.setInstanceName(instanceName);
            return createPortletInstance(parentPath, gwtJahiaNewPortletInstance, currentUserSession, uiLocale, httpSessionID);
        } catch (GWTJahiaServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to create an RSS portlet due to: ", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.create.portlet.instance", uiLocale, e.getLocalizedMessage()));
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
    public GWTJahiaNode createRSSPortletInstance(String parentPath, String name, String url, JCRSiteNode site, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        final String appName = Jahia.getContextPath().length() > 0 ? Jahia.getContextPath().substring(1) + "/rss" : "rss";
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaRSSPortlet", currentUserSession.getLocale(), currentUserSession.getUser(), currentUserSession.getWorkspace().getName(), uiLocale);
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

        return createPortletInstance(parentPath, name, appName, "JahiaRSSPortlet", gwtJahiaNodeProperties, site, currentUserSession, uiLocale, httpSessionID);
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
    public GWTJahiaNode createGoogleGadgetPortletInstance(String parentPath, String name, String script, JCRSiteNode site, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        final String appName = Jahia.getContextPath().length() > 0 ? Jahia.getContextPath().substring(1) + "/googlegadget" : "googlegadget";
        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaGoogleGadget", currentUserSession.getLocale(), currentUserSession.getUser(), currentUserSession.getWorkspace().getName(), uiLocale);
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

        return createPortletInstance(parentPath, name, appName, "JahiaGoogleGadget", gwtJahiaNodeProperties, site, currentUserSession, uiLocale, httpSessionID);
    }

    /**
     *
     * @param appName
     * @param entryPointName
     * @param locale
     * @param user
     * @param workspaceName
     * @return
     */
    public GWTJahiaPortletDefinition createJahiaGWTPortletDefinitionByName(String appName, String entryPointName, Locale locale, JahiaUser user, String workspaceName, Locale uiLocale) {
        if (appName != null && entryPointName != null) {
            try {
                // TO DO: replace this part of the method by a more perfoming one
                List<ApplicationBean> appList = applicationsManager.getApplications();
                for (ApplicationBean anAppList : appList) {
                    if (JCRContentUtils.hasPermission(workspaceName, Constants.JCR_READ_RIGHTS, anAppList.getID())) {
                        List<EntryPointDefinition> l = anAppList.getEntryPointDefinitions();
                        for (EntryPointDefinition aL : l) {
                            boolean foundEntryPointDefinition = appName.equalsIgnoreCase(anAppList.getName()) && aL.getName().equalsIgnoreCase(entryPointName);
                            if (foundEntryPointDefinition) {
                                return createGWTJahiaPortletDefinition(anAppList, aL, locale, uiLocale);
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

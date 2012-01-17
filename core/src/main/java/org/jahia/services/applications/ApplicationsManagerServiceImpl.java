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

package org.jahia.services.applications;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PlutoServices;
import org.apache.pluto.container.driver.PortletRegistryEvent;
import org.apache.pluto.container.driver.PortletRegistryListener;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jahia.api.Constants;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.WebAppContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.InsertionSortedMap;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This Service is used to manage the jahia application definitions.
 *
 * @author Khue ng
 * @version 1.0
 */
public class ApplicationsManagerServiceImpl extends ApplicationsManagerService {

    /**
     * logging
     */
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(
            ApplicationsManagerServiceImpl.class);

    /**
     * the instance *
     */
    private static ApplicationsManagerServiceImpl instance;
    public static final String WEBAPPS_PERMISSION_PATH = "/permissions/webapps/";

    /**
     * dummy comparator application bean *
     */
    private ApplicationBean dummyComparator;

    /**
     * is loaded status *
     */
    private boolean isLoaded = false;

    private List<PortletMode> supportedPortletModes = new ArrayList<PortletMode>();
    private List<WindowState> supportedWindowStates = new ArrayList<WindowState>();

    /**
     * This map contains a mapping of application type names to application
     * management providers for the different application types.
     */
    private Map<String, ApplicationsManagerProvider> managerProviders = new InsertionSortedMap<String, ApplicationsManagerProvider>();

    private JCRTemplate jcrTemplate;

    private CacheService cacheService;
    private JahiaGroupManagerService groupManagerService;

    private ServletContextManager servletContextManager;
    private Cache<String, ApplicationBean> applicationCache;
    private static final String APPLICATION_DEFINITION = "ApplicationDefinition";
    private static final String APPLICATION_DEFINITION_CONTEXT = "ApplicationDefinitionContext";
    private Cache<String, EntryPointInstance> entryPointCache;
    private static final String ENTRY_POINT_INSTANCE = "EntryPointInstance";
    private PlutoServices plutoServices;

    /**
     * This next map is used to map portlet permissions to Jahia's default roles.
     */
    private Map<String, String> defaultPortletPermissionMappings;

    /**
     * This next map is used to map web application roles to Jahia's default roles.
     */
    private Map<String, String> defaultWebAppRoleMappings;

    /**
     * This next map is used to map portlet modes to Jahia's default roles.
     */
    private Map<String, String> defaultPortletModeMappings;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setManagerProviders(Map<String, ApplicationsManagerProvider> managerProviders) {
        this.managerProviders = managerProviders;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setServletContextManager(ServletContextManager servletContextManager) {
        this.servletContextManager = servletContextManager;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public void setPlutoServices(PlutoServices plutoServices) {
        this.plutoServices = plutoServices;
    }

    public void setDefaultPortletPermissionMappings(Map<String, String> defaultPortletPermissionMappings) {
        this.defaultPortletPermissionMappings = defaultPortletPermissionMappings;
    }

    public void setDefaultWebAppRoleMappings(Map<String, String> defaultWebAppRoleMappings) {
        this.defaultWebAppRoleMappings = defaultWebAppRoleMappings;
    }

    public void setDefaultPortletModeMappings(Map<String, String> defaultPortletModeMappings) {
        this.defaultPortletModeMappings = defaultPortletModeMappings;
    }

    /**
     * constructor
     */
    protected ApplicationsManagerServiceImpl() {
        dummyComparator = new ApplicationBean("", "", "", true, "", "");
    }

    /**
     * return the singleton instance
     */
    public static synchronized ApplicationsManagerServiceImpl getInstance() {
        if (instance == null) {
            instance = new ApplicationsManagerServiceImpl();
        }

        return instance;
    }

    /**
     * Initialze disk path
     */
    public void start() throws JahiaInitializationException {
        applicationCache = cacheService.getCache("ApplicationCache", true);
        entryPointCache = cacheService.getCache("ApplicationEntryPointCache", true);

        supportedPortletModes.add(PortletMode.VIEW);
        supportedPortletModes.add(PortletMode.EDIT);
        supportedPortletModes.add(PortletMode.HELP);
        /*
        loadCustomPortletModes(settingsBean.getJahiaEtcDiskPath() + File.separator + "services" +
                                        File.separator + "applications" + File.separator + "portlet-modes.xml");
         */

        supportedWindowStates.add(WindowState.NORMAL);
        supportedWindowStates.add(WindowState.MINIMIZED);
        supportedWindowStates.add(WindowState.MAXIMIZED);
        /*
        loadCustomWindowStates(settingsBean.getJahiaEtcDiskPath() + File.separator + "services" +
                                        File.separator + "applications" + File.separator + "window-states.xml");
        */

        this.isLoaded = true;

        try {
            loadAllApplications();
        } catch (Exception e) {
            throw new JahiaInitializationException(
                    "JahiaApplicationsManagerBaseService.init, exception occured : " + e.getMessage(), e);
        }

    }

    public void stop() {
    }

    /**
     * return an Application Definition get directly from db
     *
     * @param appID the appID
     * @return ApplicationBean, the Application Definition
     */
    public ApplicationBean getApplication(final String appID) throws JahiaException {

        checkIsLoaded();

        ApplicationBean app = applicationCache.get(APPLICATION_DEFINITION + appID);
        if (app == null) {
            // try to load from db
            try {

                app = jcrTemplate.doExecuteWithSystemSession(new JCRCallback<ApplicationBean>() {
                    public ApplicationBean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        return fromNodeToBean(session.getNodeByUUID(appID));
                    }
                });
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            if (app != null) {
                applicationCache.put(APPLICATION_DEFINITION + appID, app);
                applicationCache.put(APPLICATION_DEFINITION_CONTEXT + app.getContext(), app);
            }
        }
        return app;
    }

    private ApplicationBean fromNodeToBean(JCRNodeWrapper wrapper) throws RepositoryException {
        return new ApplicationBean(wrapper.getIdentifier(), wrapper.getPropertyAsString("j:name"),
                wrapper.getPropertyAsString("j:context"), wrapper.getProperty(
                        "j:isVisible").getBoolean(), wrapper.getPropertyAsString("j:description"),
                wrapper.getPropertyAsString("j:type"));
    }

    /**
     * return an Application Definition looking at its context.
     *
     * @param context , the context
     * @return ApplicationBean, the Application Definition
     */
    public ApplicationBean getApplicationByContext(String context) throws JahiaException {

        checkIsLoaded();

        syncPlutoWithDB();

        if (context == null) {
            return null;
        }
        ApplicationBean app = applicationCache.get(APPLICATION_DEFINITION_CONTEXT + context);
        if (app == null) {
            // try to load from db
            app = getApplicationByContextAndJCRCall(context);
            if (app != null) {
                putInApplicationCache(app);
            }
        }
        return app;
    }

    private ApplicationBean getApplicationByContextAndJCRCall(final String context) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<ApplicationBean>() {
                public ApplicationBean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT * FROM [jnt:portletDefinition] as def where def.[j:context] = '" + context + "' ORDER BY def.[j:context]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        final NodeIterator nodes = qr.getNodes();
                        if (nodes.hasNext()) {
                            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.next();
                            return fromNodeToBean(nodeWrapper);
                        }
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving applicaion by context", e);
            return null;
        }
    }

    /**
     * return all application Definitions
     *
     * @return Iterator an enumerations of ApplicationBean or null if empty
     */
    public List<ApplicationBean> getApplications() throws JahiaException {

        checkIsLoaded();

        syncPlutoWithDB();

        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<ApplicationBean>>() {
                public List<ApplicationBean> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<ApplicationBean> apps = new ArrayList<ApplicationBean>();
                    if (session.getWorkspace().getQueryManager() != null) {
                        String query = "SELECT * FROM [jnt:portletDefinition] as def ORDER BY def.[j:context]";
                        Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        final NodeIterator nodes = qr.getNodes();
                        while (nodes.hasNext()) {
                            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.next();
                            apps.add(fromNodeToBean(nodeWrapper));
                        }
                        Collections.sort(apps, dummyComparator);
                    }
                    return apps;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Error while retrieving the list of applications", e);
            return new ArrayList<ApplicationBean>();
        }
    }

    /**
     * set an application Visible to users
     *
     * @param appID
     * @param visible status
     * @return false on error
     */
    public boolean setVisible(String appID, boolean visible) throws JahiaException {

        checkIsLoaded();

        ApplicationBean app = getApplication(appID);
        if (app != null) {
            app.setVisible(visible);
            return saveDefinition(app);
        }
        return false;
    }

    private static String getCompactName(final String name) {
        String appName = name;
        appName = appName.replace("-", "");
        appName = appName.replace("_", "");
        appName = appName.replace(".", "");
        return appName;
    }

    public static String getWebAppNamespaceURI(String appName) {
        return "http://www.jahia.org/portlets/" + appName;
    }

    public static String getWebAppPrefix(final String appName) {
        return JCRContentUtils.encodeJCRNamePrefix(getCompactName(appName));
    }

    public static String getEntryPointNamespaceURI(final String appName, final String entryPointDefinitionName) {
        return getWebAppNamespaceURI(appName) + "/" + entryPointDefinitionName;
    }

    public static String getEntryPointPrefix(final String appName, final String entryPointDefinitionName) {
        return JCRContentUtils.encodeJCRNamePrefix(getCompactName(appName + "_" + entryPointDefinitionName));
    }

    public static String getWebAppQualifiedNodeName(final String appName, final String localName) {
        return getWebAppPrefix(appName) + ":" + localName;
    }

    public static String getPortletQualifiedNodeName(final String appName, final String entryPointDefinitionName, final String localName) {
        return getEntryPointPrefix(appName, entryPointDefinitionName) + ":" + localName;
    }

    /**
     * Add a new Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public boolean addDefinition(final ApplicationBean app) throws JahiaException {

        checkIsLoaded();

        if (app == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final JCRNodeWrapper parentNode = session.getNode("/portletdefinitions");
                    final String name = app.getName().replaceAll("/", "___");
                    session.checkout(parentNode);
                    final JCRNodeWrapper wrapper = parentNode.addNode(name, "jnt:portletDefinition");
                    wrapper.setProperty("j:context", app.getContext());
                    wrapper.setProperty("j:name", app.getName());
                    wrapper.setProperty("j:description", app.getDescription());
                    wrapper.setProperty("j:type", app.getType());
                    wrapper.setProperty("j:isVisible", app.isVisible());
                    session.save();
                    app.setID(wrapper.getIdentifier());

                    // we must now create the corresponding permissions for web application roles as well as portlet
                    // modes, and then assign them to default roles, possibly using some kind of configuration.

                    JCRContentUtils.registerNamespace(session, getWebAppPrefix(app.getName()), getWebAppNamespaceURI(app.getName()));

                    String webappPath = WEBAPPS_PERMISSION_PATH + app.getName();

                    // now let's add the web application roles as Jahia permissions, that we will map to Jahia roles
                    // if a default mapping is provided in the service's configuration.

                    String webappRolesPath = webappPath + "/" + getWebAppQualifiedNodeName(app.getName(), "roles");
                    JCRNodeWrapper webappRolesPermNode = getOrCreatePermissionNode(webappRolesPath, session);
                    session.save();

                    try {
                        WebAppContext appContext = servletContextManager.getApplicationContext(app);
                        Iterator<String> updatedRoles = appContext.getRoles().iterator();
                        String webAppRoleName;
                        while (updatedRoles.hasNext()) {
                            webAppRoleName = updatedRoles.next();

                            JCRNodeWrapper permission = getOrCreatePermissionNode(webappRolesPath + "/" + getWebAppQualifiedNodeName(app.getName(), webAppRoleName), session);
                            session.save();

                            // if a mapping to a Jahia role is defined, let's add it.
                            if (defaultWebAppRoleMappings != null) {
                                String jahiaRolePath = defaultWebAppRoleMappings.get(webAppRoleName);
                                if (jahiaRolePath != null) {
                                    grantPermissionToRole(permission, jahiaRolePath, session);
                                }
                            }

                        }
                    } catch (JahiaException e) {
                        logger.error("Error while retrieving web application roles", e);
                    }

                    // now let's iterate over the portlets and setup the appropriate permission nodes for the default
                    // permissions and portlet modes. Mappings to role if defined in the service's configuration will
                    // also be done.

                    for (EntryPointDefinition entryPointDefinition : app.getEntryPointDefinitions()) {
                        JCRContentUtils.registerNamespace(session, getEntryPointPrefix(app.getName(), entryPointDefinition.getName()), getEntryPointNamespaceURI(app.getName(), entryPointDefinition.getName()));

                        String portletsPath = webappPath + "/" + getWebAppQualifiedNodeName(app.getName(), "portlets");
                        // let's add the default portlet permissions

                        String createInstancesPermPath = portletsPath + "/" + getPortletQualifiedNodeName(app.getName(), entryPointDefinition.getName(), "createInstance");
                        JCRNodeWrapper createInstancesPermNode = getOrCreatePermissionNode(createInstancesPermPath, session);
                        session.save();
                        if (defaultPortletPermissionMappings != null) {
                            String jahiaRolePath = defaultPortletPermissionMappings.get("createInstance");
                            grantPermissionToRole(createInstancesPermNode, jahiaRolePath, session);
                        }

                        String portletModePath = webappPath + "/" + getWebAppQualifiedNodeName(app.getName(), "portlets") + "/" + getPortletQualifiedNodeName(app.getName(), entryPointDefinition.getName(), "modes");
                        JCRNodeWrapper portletModePermNode = getOrCreatePermissionNode(portletModePath, session);
                        session.save();
                        for (PortletMode portletMode : entryPointDefinition.getPortletModes()) {
                            JCRNodeWrapper permission = getOrCreatePermissionNode(portletModePath + "/" + getPortletQualifiedNodeName(app.getName(), entryPointDefinition.getName(), portletMode.toString()), session);
                            session.save();

                            // if a mapping to a Jahia role is defined, let's add it.
                            if (defaultPortletModeMappings != null) {
                                String jahiaRolePath = defaultPortletModeMappings.get(portletMode.toString());
                                if (jahiaRolePath != null) {
                                    grantPermissionToRole(permission, jahiaRolePath, session);
                                }
                            }

                        }
                    }

                    session.save();

                    return true;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        putInApplicationCache(app);
        return ret;
    }

    private void grantPermissionToRole(JCRNodeWrapper permission, String jahiaRolePath, JCRSessionWrapper session) throws RepositoryException {
        try {
            Node n = session.getNode(jahiaRolePath);
            session.checkout(n);

            Value newValue = session.getValueFactory().createValue(permission, true);

            if (n.hasProperty("j:permissions")) {
                List<Value> values = new ArrayList<Value>(Arrays.asList(n.getProperty("j:permissions").getValues()));
                values.add(newValue);
                n.setProperty("j:permissions", values.toArray(new Value[values.size()]));
            } else {
                n.setProperty("j:permissions", new Value[] {newValue});
            }
            session.save();
        } catch (PathNotFoundException e) {
            logger.warn("Couldn't find Jahia role " + jahiaRolePath + " to map permission " + permission.getName() + ", will not map the role");
        }
    }

    private JCRNodeWrapper getOrCreatePermissionNode(String path, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper parentNode = session.getRootNode();

        return JCRContentUtils.getOrAddPath(session, parentNode, path, Constants.JAHIANT_PERMISSION);
    }

    /**
     * Save the Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public boolean saveDefinition(final ApplicationBean app) throws JahiaException {

        checkIsLoaded();

        if (app == null) {
            return false;
        }
        boolean ret = false;
        try {
            ret = jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final JCRNodeWrapper wrapper = session.getNodeByUUID(app.getID());
                    wrapper.setProperty("j:context", app.getContext());
                    wrapper.setProperty("j:name", app.getName());
                    wrapper.setProperty("j:description", app.getDescription());
                    wrapper.setProperty("j:type", app.getType());
                    wrapper.setProperty("j:isVisible", app.isVisible());
                    session.save();
                    app.setID(wrapper.getIdentifier());
                    return true;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        putInApplicationCache(app);
        return ret;
    }

    /**
     * Removes an application from the persistant storage area and from registry.
     *
     * @param appID identifier of the application to remove from the persistant
     *              storage area
     * @throws org.jahia.exceptions.JahiaException
     *          generated if there was an error while removing the application
     *          data from persistant storage area
     */
    public void removeApplication(final String appID) throws JahiaException {

        checkIsLoaded();
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    session.getNodeByUUID(appID).remove();
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        applicationCache.flush();
    }

    //--------------------------------------------------------------------------

    /**
     * delete groups associated with an application.
     * When deleting an Application definition, should call this method to
     * remove unused groups
     */
    public void deleteApplicationGroups(ApplicationBean app) throws JahiaException {

        checkIsLoaded();

        List<String> vec = groupManagerService.getGroupnameList();

        if (app != null && vec != null) {

            String appID = app.getID();
            JahiaGroup grp;
            String grpName;
            String pattern = appID + "_";
            for (String aVec : vec) {
                grpName = aVec;
                if (grpName.startsWith(pattern)) {
                    grp = groupManagerService.lookupGroup(0, grpName);
                    if (grp != null) {
                        groupManagerService.deleteGroup(grp);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * create groups for each context, that is for each field id
     */
    public void createApplicationGroups(EntryPointInstance entryPointInstance) throws JahiaException {
        // update roles
        final String context = entryPointInstance.getContextName();
        WebAppContext appContext = getApplicationContext(getApplicationByContext(context));

        Iterator<String> updatedRoles = appContext.getRoles().iterator();
        String groupName;
        String role;
        while (updatedRoles.hasNext()) {
            role = updatedRoles.next();
            groupName = entryPointInstance.getID() + "_" + role;
            groupManagerService.createGroup(0, groupName, null, true); // Hollis all app role groups are of site 0 !!!
        }

    }

    //--------------------------------------------------------------------------

    /**
     * delete groups associated with a gived context, that is attached to a field id
     * and all its members
     */
    public void deleteApplicationGroups(EntryPointInstance entryPointInstance) throws JahiaException {
        final String context = entryPointInstance.getContextName();

        WebAppContext appContext = getApplicationContext(getApplicationByContext(context));

        Iterator<String> roles = appContext.getRoles().iterator();
        String groupName;
        String role;
        while (roles.hasNext()) {
            role = roles.next();
            groupName = new StringBuffer().append(entryPointInstance.getID()).append("_").append(role).toString();
            JahiaGroup grp = groupManagerService.lookupGroup(0,
                    groupName); // Hollis : All App group roles are in site 0 !!!
            if (grp != null) {
                // delete all members
                grp.removeMembers();
                groupManagerService.deleteGroup(grp);
            }
        }

    }

    //--------------------------------------------------------------------------

    /**
     * Get an ApplicationContext for a given application id
     *
     * @param id , the application id
     * @return the application context , null if not found
     */
    public WebAppContext getApplicationContext(String id) throws JahiaException {
        return servletContextManager.getApplicationContext(id);
    }

    //--------------------------------------------------------------------------

    /**
     * Get an WebAppContext for a given application bean
     *
     * @param appBean the Application bean for which to retrieve the context
     * @return the application context , null if not found
     */
    public WebAppContext getApplicationContext(ApplicationBean appBean) throws JahiaException {
        return servletContextManager.getApplicationContext(appBean);
    }

    /**
     * Creates an instance of an application entry point definition. This
     * creates the entry in the database.
     *
     * @param entryPointDefinition EntryPointDefinition
     * @param path
     * @return the created instance for the entry point definition.
     * @throws JahiaException
     */
    public EntryPointInstance createEntryPointInstance(EntryPointDefinition entryPointDefinition, final String path)
            throws JahiaException {
        if (entryPointDefinition == null) {
            return null;
        }
        final ApplicationBean appBean = getApplication(entryPointDefinition.getApplicationID());
        ApplicationsManagerProvider appProvider = getProvider(appBean);
        EntryPointInstance epInstance = null;
        try {
            epInstance = appProvider.createEntryPointInstance(entryPointDefinition);
            // Create the new entry point instance with its acl id bound to the one of its application parent
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve entry point definitions for application " + appBean.getID(),
                    je);
        }
        if (epInstance != null) {
            try {
                final EntryPointInstance epInstance1 = epInstance;
                JCRSessionWrapper session = jcrTemplate.getSessionFactory().getCurrentUserSession();
                final JCRNodeWrapper parentNode = session.getNode(path);
                session.checkout(parentNode);
                final String name = epInstance1.getResKeyName() != null ? epInstance1.getResKeyName() : appBean.getName().replaceAll(
                        "/", "___") + Math.round(Math.random() * 1000000l);
                final JCRPortletNode wrapper = (JCRPortletNode) parentNode.addNode(name, "jnt:portlet");
                final String scope = epInstance1.getCacheScope();
                if (scope != null) {
                    wrapper.setProperty("j:cacheScope", scope);
                }
                wrapper.setApplication(appBean.getID(), epInstance1.getDefName());
                wrapper.setProperty("j:expirationTime", epInstance1.getExpirationTime());
                session.save();
                epInstance1.setID(wrapper.getUUID());
                return epInstance1;
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            entryPointCache.put(ENTRY_POINT_INSTANCE + epInstance.getID(), epInstance);
        }
        return null;
    }

    /**
     * Get Portlet Window object
     *
     * @param entryPointInstance
     * @param windowID
     * @return
     * @throws JahiaException
     */
    public PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID,
                                          JahiaUser jahiaUser,
                                          HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse,
                                          ServletContext servletContext, String workspaceName)
            throws JahiaException {
        ApplicationBean appBean = getApplicationByContext(entryPointInstance.getContextName());
        ApplicationsManagerProvider appProvider = getProvider(appBean);
        return appProvider.getPortletWindow(entryPointInstance, windowID, jahiaUser, httpServletRequest, httpServletResponse, servletContext, workspaceName);

    }

    /**
     * Get all EntryPointDefinition od the application bean
     *
     * @param appBean ApplicationBean the application for which to retrieve
     *                the entry point definitions
     * @return
     */
    public List<EntryPointDefinition> getAppEntryPointDefinitions(ApplicationBean appBean) {
        ApplicationsManagerProvider appProvider = getProvider(appBean);
        try {
            return appProvider.getAppEntryPointDefinitions(appBean);
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve entry point definitions for application " + appBean.getID(),
                    je);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves an EntryPointInstance object from the persistance system by
     * using it's ID as a search key
     *
     * @param epInstanceID int the unique identifier for the EntryPointInstance
     *                     object in the persistence system
     * @return EntryPointInstance the object if found in the persistance system,
     *         or null otherwise.
     * @throws JahiaException thrown if there was an error communicating with
     *                        the persistence system.
     */
    public EntryPointInstance getEntryPointInstance(final String epInstanceID, final String workspaceName) throws JahiaException {
        final EntryPointInstance[] entryPointInstanceByID = new EntryPointInstance[]{entryPointCache.get(ENTRY_POINT_INSTANCE + epInstanceID)};
        if (entryPointInstanceByID[0] == null && epInstanceID != null && !"".equals(epInstanceID) && !"<empty>".equals(epInstanceID)) {
            try {
                JCRSessionWrapper session = jcrTemplate.getSessionFactory().getCurrentUserSession(workspaceName);
                final JCRPortletNode node = (JCRPortletNode) session.getNodeByUUID(epInstanceID);
                entryPointInstanceByID[0] = getEntryPointInstance(node);
                entryPointCache.put(ENTRY_POINT_INSTANCE + epInstanceID, entryPointInstanceByID[0]);
                return entryPointInstanceByID[0];
            } catch (javax.jcr.ItemNotFoundException e) {
                // user can't not access to portlet instance: intance doen't exist or user has no reqs ACL
                logger.debug("User " + JCRSessionFactory.getInstance().getCurrentUser().getName() + " could not load the portlet instance :" + epInstanceID);
                return null;
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return entryPointInstanceByID[0];
    }

    /**
     * Get entryPoint object from JCRPorletNode
     * @param node
     * @return
     * @throws RepositoryException
     */
    public EntryPointInstance getEntryPointInstance(JCRPortletNode node) throws RepositoryException {
        EntryPointInstance entryPointInstance = new EntryPointInstance(node.getUUID(), node.getContextName(),
                node.getDefinitionName(), node.getName());
        if (node.hasProperty("j:cacheScope")) {
            entryPointInstance.setCacheScope(node.getCacheScope());
        }
        if (node.hasProperty("j:expirationTime")) {
            entryPointInstance.setExpirationTime(node.getExpirationTime());
        }
        return entryPointInstance;
    }

    /**
     * Removes an entry point instance from the persistance system.
     *
     * @param epInstanceID int the unique identifier for the entry point
     *                     instance
     * @throws JahiaException thrown if there was an error communicating with
     *                        the persistence system.
     */
    public void removeEntryPointInstance(final String epInstanceID) throws JahiaException {
        try {
            if (epInstanceID != null) {
                JCRSessionWrapper session = jcrTemplate.getSessionFactory().getCurrentUserSession();
                final Node node = session.getNodeByUUID(epInstanceID);
                node.remove();
                session.save();
                entryPointCache.remove(ENTRY_POINT_INSTANCE + epInstanceID);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }


    //--------------------------------------------------------------------------

    /**
     * load all application Definitions in registry
     */
    private void loadAllApplications() {
        List<ApplicationBean> apps = null;
        try {
            apps = getApplications();
        } catch (JahiaException e) {
            logger.error("Error while loading all the applications", e);
        }
        if (apps != null) {
            ApplicationBean app;
            for (ApplicationBean app1 : apps) {
                app = app1;
                putInApplicationCache(app);
            }
        }
    }

    //--------------------------------------------------------------------------

    /**
     * throw an exception if the service hasn't been loaded successfully
     */
    private void checkIsLoaded() throws JahiaException {

        if (!isLoaded) {
            throw new JahiaException("Error accessing a service that was not initialized successfully",
                    "Error accessing a service that was not initialized successfully",
                    JahiaException.SERVICE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

    }

    /**
     * Sync Pluto with Jahia DB - Is it-possible to find another way ?
     *
     * @throws JahiaException
     */
    private void syncPlutoWithDB() throws JahiaException {
        // here we will compare Pluto's memory state with the state of the database, and add any missing application.
        PortletRegistryService portletRegistryService = plutoServices.getPortletRegistryService();
        Iterator<String> portletApplicationNames = portletRegistryService.getRegisteredPortletApplicationNames();
        while (portletApplicationNames.hasNext()) {
            String currentPortletApplicationName = portletApplicationNames.next();
            String currentContext = !currentPortletApplicationName.startsWith("/") ? "/" + currentPortletApplicationName : currentPortletApplicationName;
            ApplicationBean app = applicationCache.get(APPLICATION_DEFINITION_CONTEXT + currentContext);

            if (app == null) {
                // try to load from db
                app = getApplicationByContextAndJCRCall(currentContext);
                if (app != null) {
                    putInApplicationCache(app);
                }
            }

            if (app == null) {
                // The app was not found in Jahia but is registered in Pluto, so we register it in Jahia.
                logger.info("Registering portlet context " + currentPortletApplicationName + " in Jahia.");
                registerWebApps(currentContext);
            }
        }
    }

    private void registerWebApps(String context) throws JahiaException {
        addDefinition(new ApplicationBean(
                "", // id
                !context.startsWith("/") ? context : context.substring(1),
                context,
                true,
                "",
                "portlet"
        ));
    }
    
    /**
     * Put in application cache
     *
     * @param app
     */
    private void putInApplicationCache(ApplicationBean app) {
        applicationCache.put(APPLICATION_DEFINITION + app.getID(), app);
        applicationCache.put(APPLICATION_DEFINITION_CONTEXT + app.getContext(), app);
    }

    /**
     * Get ApplicationManagerProvider depending on appBean type
     *
     * @param appBean
     * @return
     */
    private ApplicationsManagerProvider getProvider(ApplicationBean appBean) {
        return managerProviders.get(appBean.getType());
    }

    /**
     * load custom portlet mode
     *
     * @param supportedPortletModesConfigFileName
     *
     */
    public void loadCustomPortletModes(String supportedPortletModesConfigFileName) {

        // Add XML file structure to Digester
        Digester digester = new Digester();
        digester.push(this);
        digester.setLogger(LogFactory.getLog(Digester.class));
        digester.addObjectCreate("portlet-modes/portlet-mode", CustomPortletMode.class);
        digester.addBeanPropertySetter("portlet-modes/portlet-mode/name", "name");
        digester.addSetNext("portlet-modes/portlet-mode", "addCustomPortletMode", CustomPortletMode.class.getName());

        // digester.setUseContextClassLoader(true);
        // Let's parse the XML file
        File supportedPortletModesConfigFile = new File(supportedPortletModesConfigFileName);
        try {
            digester.parse(supportedPortletModesConfigFile);
        } catch (IOException ioe) {
            logger.error(
                    "Error while trying to load support portlet modes from file " + supportedPortletModesConfigFile,
                    ioe);
        } catch (SAXException saxe) {
            logger.error(
                    "Error while trying to load support portlet modes from file " + supportedPortletModesConfigFile,
                    saxe);
        }

    }

    /**
     * Add custom portlet modes
     *
     * @param customPortletMode
     */
    public void addCustomPortletModeBean(CustomPortletMode customPortletMode) {
        supportedPortletModes.add(new PortletMode(customPortletMode.getName()));
    }

    /**
     * Get supported portlet modes
     *
     * @return
     */
    public List<PortletMode> getSupportedPortletModes() {
        return supportedPortletModes;
    }

    /**
     * Load custom window states
     *
     * @param supportedWindowStatesConfigFileName
     *
     */
    public void loadCustomWindowStates(String supportedWindowStatesConfigFileName) {

        // Add XML file structure to Digester
        Digester digester = new Digester();
        digester.push(this);
        digester.setLogger(LogFactory.getLog(Digester.class));
        digester.addObjectCreate("window-states/window-state", CustomWindowState.class);
        digester.addBeanPropertySetter("window-states/window-state/name", "name");
        digester.addSetNext("window-states/window-state", "addCustomWindowState", CustomWindowState.class.getName());

        // digester.setUseContextClassLoader(true);
        // Let's parse the XML file
        File supportedWindowStatesConfigFile = new File(supportedWindowStatesConfigFileName);
        try {
            digester.parse(supportedWindowStatesConfigFile);
        } catch (IOException ioe) {
            logger.error(
                    "Error while trying to load support portlet modes from file " + supportedWindowStatesConfigFile,
                    ioe);
        } catch (SAXException saxe) {
            logger.error(
                    "Error while trying to load support portlet modes from file " + supportedWindowStatesConfigFile,
                    saxe);
        }

    }

    /**
     * Add custom window state
     *
     * @param customWindowState
     */
    public void addCustomWindowState(CustomWindowState customWindowState) {
        supportedWindowStates.add(new WindowState(customWindowState.getName()));
    }

    /**
     * Get supported window states
     *
     * @return
     */
    public List<WindowState> getSupportedWindowStates() {
        return supportedWindowStates;
    }

    public void registerListeners() {
        // register listener
        plutoServices.getPortletRegistryService().addPortletRegistryListener(new PortletRegistryListener() {
            public void portletApplicationRegistered(PortletRegistryEvent evt) {
                servletContextManager.removeContextFromCache(evt.getApplicationName());

                try {
                    loadApplicationPrivileges(evt);

                    syncPlutoWithDB();
                } catch (Exception e) {
                    logger.error(
                            "Error registering application '" + evt.getApplicationName() + "'. Cause: " + e.getMessage(),
                            e);
                }
            }

            public void portletApplicationRemoved(PortletRegistryEvent evt) {
                // do nothing
            }
        });
        try {
            syncPlutoWithDB();
        } catch (Exception e) {
            logger.error(
                    "Error synchronizing deployed portlets state with the internal registry. Cause: " + e.getMessage(),
                    e);
        }
    }

    private void loadApplicationPrivileges(PortletRegistryEvent evt) throws RepositoryException {
        // we must now register the namespace for the portlet, as well as refresh the privileges, to make sure
        // we load all the privileges attached to portlets.
        final PortletApplicationDefinition portletApplication = evt.getPortletApplication();
        String applicationName = portletApplication.getName();
        if (applicationName.startsWith("/")) {
            applicationName = applicationName.substring(1);
        }
        final String appName = applicationName;

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                // first we register the namespaces.

                JCRContentUtils.registerNamespace(session, getWebAppPrefix(appName), getWebAppNamespaceURI(appName));

                List<? extends PortletDefinition> portlets = portletApplication.getPortlets();
                for (PortletDefinition portlet : portlets) {
                    JCRContentUtils.registerNamespace(session, getEntryPointPrefix(appName, portlet.getPortletName()), getEntryPointNamespaceURI(appName, portlet.getPortletName()));
                }

                // now let's reload the privileges

                JahiaPrivilegeRegistry.init(session);
                return null;
            }
        });
    }

} // end JahiaApplicationsService

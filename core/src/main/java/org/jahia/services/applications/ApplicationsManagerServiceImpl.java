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
package org.jahia.services.applications;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PlutoServices;
import org.apache.pluto.container.driver.PortletRegistryEvent;
import org.apache.pluto.container.driver.PortletRegistryListener;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.WebAppContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPortletNode;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.utils.InsertionSortedMap;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
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
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ApplicationsManagerServiceImpl.class);

    /**
     * the instance *
     */
    private static ApplicationsManagerServiceImpl instance;

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

    private JCRStoreService jcrStoreService;

    private CacheService cacheService;
    private JahiaGroupManagerService groupManagerService;

    private ServletContextManager servletContextManager;
    private Cache<String, ApplicationBean> applicationCache;
    private static final String APPLICATION_DEFINITION = "ApplicationDefinition";
    private static final String APPLICATION_DEFINITION_CONTEXT = "ApplicationDefinitionContext";
    private Cache<String, EntryPointInstance> entryPointCache;
    private static final String ENTRY_POINT_INSTANCE = "EntryPointInstance";
    private PlutoServices plutoServices;

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

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public PlutoServices getPlutoServices() {
        return plutoServices;
    }

    public void setPlutoServices(PlutoServices plutoServices) {
        this.plutoServices = plutoServices;
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
        applicationCache = cacheService.createCacheInstance("ApplicationCache");
        entryPointCache = cacheService.createCacheInstance("ApplicationEntryPointCache");
        try {

            loadAllApplications();
        } catch (Exception e) {
            throw new JahiaInitializationException(
                    "JahiaApplicationsManagerBaseService.init, exception occured : "
                    + e.getMessage(), e);
        }

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
    }

    public void stop() {
    }

    /**
     * return an Application Definition get directly from db
     *
     * @param appID the appID
     * @return ApplicationBean, the Application Definition
     */
    public ApplicationBean getApplication(String appID)
            throws JahiaException {

        checkIsLoaded();

        ApplicationBean app = applicationCache.get(APPLICATION_DEFINITION + appID);
        if (app == null) {
            // try to load from db
            try {
                final JCRNodeWrapper wrapper = (JCRNodeWrapper) jcrStoreService.getSystemSession().getNodeByUUID(appID);
                app = fromNodeToBean(wrapper);
            } catch (RepositoryException e) {
                logger.error(e, e);
            }
            if (app != null) {
                applicationCache.put(APPLICATION_DEFINITION + appID, app);
                applicationCache.put(APPLICATION_DEFINITION_CONTEXT + app.getContext(), app);
            }
        }
        return app;
    }

    private ApplicationBean fromNodeToBean(JCRNodeWrapper wrapper) throws RepositoryException {
        return new ApplicationBean(wrapper.getUUID(), wrapper.getPropertyAsString("j:name"), wrapper.getPropertyAsString("j:context"),
                                                   wrapper.getProperty("j:isVisible").getBoolean(),
                                                   wrapper.getPropertyAsString("j:description"),
                                                   wrapper.getPropertyAsString("j:type"));
    }

    /**
     * return an Application Definition looking at its context.
     *
     * @param context , the context
     * @return ApplicationBean, the Application Definition
     */
    public ApplicationBean getApplicationByContext(String context)
            throws JahiaException {

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

    private ApplicationBean getApplicationByContextAndJCRCall(String context) {
        ApplicationBean app = null;
        try {
            Session session = jcrStoreService.getSystemSession();
            if (session.getWorkspace().getQueryManager() != null) {
                String query = "SELECT * FROM jnt:portletDefinition where j:context = '"+context+"' ORDER BY j:context";
                Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);
                QueryResult qr = q.execute();
                final NodeIterator nodes = qr.getNodes();
                while (nodes.hasNext()) {
                    JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.next();
                    app = fromNodeToBean(nodeWrapper);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return app;
    }

    /**
     * return all application Definitions
     *
     * @return Iterator an enumerations of ApplicationBean or null if empty
     */
    public List<ApplicationBean> getApplications()
            throws JahiaException {

        checkIsLoaded();

        syncPlutoWithDB();

        List<ApplicationBean> apps = new ArrayList<ApplicationBean>();
        try {
                Session session = jcrStoreService.getSystemSession();
                if (session.getWorkspace().getQueryManager() != null) {
                    String query = "SELECT * FROM jnt:portletDefinition ORDER BY j:context";
                    Query q = session.getWorkspace().getQueryManager().createQuery(query, Query.SQL);
                    QueryResult qr = q.execute();
                    final NodeIterator nodes = qr.getNodes();
                    while (nodes.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.next();
                        apps.add(fromNodeToBean(nodeWrapper));
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e);
            }
        Collections.sort(apps, dummyComparator);
        return apps;
    }

    /**
     * set an application Visible to users
     *
     * @param appID
     * @param visible status
     * @return false on error
     */
    public boolean setVisible(String appID, boolean visible)
            throws JahiaException {

        checkIsLoaded();

        ApplicationBean app = getApplication(appID);
        if (app != null) {
            app.setVisible(visible);
            return saveDefinition(app);
        }
        return false;
    }

    /**
     * Add a new Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public boolean addDefinition(ApplicationBean app)
            throws JahiaException {

        checkIsLoaded();

        if (app == null) {
            return false;
        }
        try {
            final JCRNodeWrapper parentNode = jcrStoreService.getSystemSession().getNode("/content/portletdefinitions");
            final String name = app.getName().replaceAll("/", "___");
            final JCRNodeWrapper wrapper = parentNode.addNode(name, "jnt:portletDefinition");
            wrapper.setProperty("j:context", app.getContext());
            wrapper.setProperty("j:name", app.getName());
            wrapper.setProperty("j:description", app.getDescription());
            wrapper.setProperty("j:type", app.getType());
            wrapper.setProperty("j:isVisible", app.isVisible());
            wrapper.revokeAllPermissions();
            wrapper.changePermissions("g:guest","r-");
            wrapper.changePermissions("u:root","rw");
            parentNode.save();
            app.setID(wrapper.getUUID());
        } catch (RepositoryException e) {
            logger.error(e, e);
        }

        putInApplicationCache(app);
        return true;
    }

    /**
     * Save the Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public boolean saveDefinition(ApplicationBean app)
            throws JahiaException {

        checkIsLoaded();

        if (app == null) {
            return false;
        }
        try {
            final JCRNodeWrapper wrapper = (JCRNodeWrapper) jcrStoreService.getSystemSession().getNodeByUUID(app.getID());
            wrapper.setProperty("j:context", app.getContext());
            wrapper.setProperty("j:name", app.getName());
            wrapper.setProperty("j:description", app.getDescription());
            wrapper.setProperty("j:type", app.getType());
            wrapper.setProperty("j:isVisible", app.isVisible());
            wrapper.save();
            app.setID(wrapper.getUUID());
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
        putInApplicationCache(app);
        return true;
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
    public void removeApplication(String appID)
            throws JahiaException {

        checkIsLoaded();
        try {
            final JCRNodeWrapper wrapper = (JCRNodeWrapper) jcrStoreService.getSystemSession().getNodeByUUID(appID);
            wrapper.remove();
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
        applicationCache.flush();
    }

    //--------------------------------------------------------------------------
    /**
     * delete groups associated with an application.
     * When deleting an Application definition, should call this method to
     * remove unused groups
     */
    public void deleteApplicationGroups(ApplicationBean app)
            throws JahiaException {

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
    public void createApplicationGroups(EntryPointInstance entryPointInstance)
            throws JahiaException {
        // update roles
        final String context = entryPointInstance.getContextName();
        WebAppContext appContext = getApplicationContext(getApplicationByContext(context));

        Iterator<String> updatedRoles = appContext.getRoles().iterator();
        String groupName;
        String role;
        while (updatedRoles.hasNext()) {
            role = updatedRoles.next();
            groupName = entryPointInstance.getID() + "_" + role;
            groupManagerService.createGroup(0,
                                            groupName, null, true); // Hollis all app role groups are of site 0 !!!
        }

    }

    //--------------------------------------------------------------------------
    /**
     * delete groups associated with a gived context, that is attached to a field id
     * and all its members
     */
    public void deleteApplicationGroups(EntryPointInstance entryPointInstance)
            throws JahiaException {
        final String context = entryPointInstance.getContextName();

        WebAppContext appContext = getApplicationContext(getApplicationByContext(context));

        Iterator<String> roles = appContext.getRoles().iterator();
        String groupName;
        String role;
        while (roles.hasNext()) {
            role = roles.next();
            groupName = new StringBuffer().append(entryPointInstance.getID()).append("_").append(role).toString();
            JahiaGroup grp = groupManagerService.lookupGroup(0, groupName); // Hollis : All App group roles are in site 0 !!!
            if (grp != null) {
                // delete all members
                grp.removeMembers();
                groupManagerService.deleteGroup(grp);
            }
        }

    }

    //--------------------------------------------------------------------------
    /**
     * return a DOM document of applications definitions
     *
     * @param siteID the site id
     * @return JahiaDOMObject a DOM representation of this object
     */
    public JahiaDOMObject getApplicationDefsAsDOM(int siteID)
            throws JahiaException {
        return null;
    }

    /**
     * Get an ApplicationContext for a given application id
     *
     * @param id , the application id
     * @return the application context , null if not found
     */
    public WebAppContext getApplicationContext(String id)
            throws JahiaException {
        return servletContextManager.getApplicationContext(id);
    }

    //--------------------------------------------------------------------------
    /**
     * Get an WebAppContext for a given application bean
     *
     * @param appBean the Application bean for which to retrieve the context
     * @return the application context , null if not found
     */
    public WebAppContext getApplicationContext(ApplicationBean appBean)
            throws JahiaException {
        return servletContextManager.getApplicationContext(appBean);
    }

    /**
     * Creates an instance of an application entry point definition. This
     * creates the entry in the database.
     *
     * @param entryPointDefinition EntryPointDefinition
     * @param s
     * @return the created instance for the entry point definition.
     * @throws JahiaException
     */
    public EntryPointInstance createEntryPointInstance(EntryPointDefinition entryPointDefinition, String path)
            throws JahiaException {
        if (entryPointDefinition == null) {
            return null;
        }
        ApplicationBean appBean = getApplication(entryPointDefinition.getApplicationID());
        ApplicationsManagerProvider appProvider = getProvider(appBean);
        EntryPointInstance epInstance = null;
        try {
            epInstance = appProvider.createEntryPointInstance(entryPointDefinition);
            // Create the new entry point instance with its acl id binded to the one of its application parent
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve entry point definitions for application " + appBean.getID(), je);
        }
        if (epInstance != null) {
            try {
                final JCRNodeWrapper parentNode = jcrStoreService.getThreadSession(Jahia.getThreadParamBean().getUser()).getNode(path);
                final String name = epInstance.getResKeyName() != null ? epInstance.getResKeyName() : appBean.getName().replaceAll("/", "___") + Math.round(Math.random() * 1000000l);
                final JCRPortletNode wrapper = (JCRPortletNode) parentNode.addNode(name, "jnt:portlet");
                final String scope = epInstance.getCacheScope();
                if (scope != null) {
                    wrapper.setProperty("j:cacheScope", scope);
                }
                wrapper.setApplication(appBean.getID(),epInstance.getDefName());
//                wrapper.setProperty("j:applicationID",epInstance.getApplicationID());
//                wrapper.setProperty("j:definitionName",epInstance.getDefName());
                wrapper.setProperty("j:expirationTime", epInstance.getExpirationTime());
                parentNode.save();
                epInstance.setID(wrapper.getUUID());
            } catch (RepositoryException e) {
                logger.error(e, e);
            }
            entryPointCache.put(ENTRY_POINT_INSTANCE + epInstance.getID(), epInstance);
        }
        return epInstance;
    }

    /**
     * Get Portlet Window object
     *
     * @param entryPointInstance
     * @param windowID
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID, ParamBean jParams)
            throws JahiaException {
        ApplicationBean appBean = getApplicationByContext(entryPointInstance.getContextName());
        ApplicationsManagerProvider appProvider = getProvider(appBean);
        return appProvider.getPortletWindow(entryPointInstance, windowID, jParams);

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
            logger.error("Error while trying to retrieve entry point definitions for application " + appBean.getID(), je);
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
    public EntryPointInstance getEntryPointInstance(String epInstanceID)
            throws JahiaException {
        EntryPointInstance entryPointInstanceByID = entryPointCache.get(ENTRY_POINT_INSTANCE + epInstanceID);
        if (entryPointInstanceByID == null && epInstanceID != null && !"".equals(epInstanceID) && !"<empty>".equals(epInstanceID)) {
            try {
                final JCRPortletNode node = (JCRPortletNode) jcrStoreService.getNodeByUUID(epInstanceID, Jahia.getThreadParamBean().getUser());
                entryPointInstanceByID = new EntryPointInstance(node.getUUID(), node.getContextName(), node.getDefinitionName(), node.getName());
                if (node.hasProperty("j:cacheScope")) {
                    entryPointInstanceByID.setCacheScope(node.getProperty("j:cacheScope").getString());
                }
                if (node.hasProperty("j:expirationTime")) {
                    entryPointInstanceByID.setExpirationTime(node.getProperty("j:expirationTime").getLong());
                }
                entryPointCache.put(ENTRY_POINT_INSTANCE + epInstanceID, entryPointInstanceByID);
            } catch (javax.jcr.ItemNotFoundException e) {
                // user can't not access to portlet instance: intance doen't exist or user has no reqs ACL
                logger.debug("User " + Jahia.getThreadParamBean().getUser().getName() + " could not load the portlet instance :" + epInstanceID);
                return null;
            }
            catch (RepositoryException e) {
                logger.error(e, e);
            }
        }
        return entryPointInstanceByID;
    }

    /**
     * Removes an entry point instance from the persistance system.
     *
     * @param epInstanceID int the unique identifier for the entry point
     *                     instance
     * @throws JahiaException thrown if there was an error communicating with
     *                        the persistence system.
     */
    public void removeEntryPointInstance(String epInstanceID)
            throws JahiaException {
        try {
            if (epInstanceID != null) {
                final Node node = jcrStoreService.getNodeByUUID(epInstanceID, Jahia.getThreadParamBean().getUser());
                final JCRNodeWrapper parentNode = (JCRNodeWrapper) node.getParent();
                node.remove();
                parentNode.save();
                entryPointCache.remove(ENTRY_POINT_INSTANCE + epInstanceID);
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
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
            logger.error(e);
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
    private void checkIsLoaded()
            throws JahiaException {

        if (!isLoaded) {
            throw new JahiaException(
                    "Error accessing a service that was not initialized successfully",
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
        PortletRegistryService portletRegistryService = PlutoServices.getServices().getPortletRegistryService();
        Iterator<String> portletApplicationNames = portletRegistryService.getRegisteredPortletApplicationNames();
        while (portletApplicationNames.hasNext()) {
            String currentPortletApplicationName = portletApplicationNames.next();
            String currentContext = "/" + currentPortletApplicationName;
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
                ServicesRegistry.getInstance().getJahiaWebAppsDeployerService().registerWebApps(currentContext);
            }
        }
    }

    /**
     * Put in apllication cache
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
            logger.error("Error while trying to load support portlet modes from file " + supportedPortletModesConfigFile, ioe);
        } catch (SAXException saxe) {
            logger.error("Error while trying to load support portlet modes from file " + supportedPortletModesConfigFile, saxe);
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
            logger.error("Error while trying to load support portlet modes from file " + supportedWindowStatesConfigFile, ioe);
        } catch (SAXException saxe) {
            logger.error("Error while trying to load support portlet modes from file " + supportedWindowStatesConfigFile, saxe);
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
            public void portletApplicationRegistered(
                    PortletRegistryEvent evt) {
                servletContextManager.removeContextFromCache(evt.getApplicationName());
                try {
	                syncPlutoWithDB();
                } catch (Exception e) {
	                logger.error("Error registering application '" + evt.getApplicationName() + "'. Cause: " + e.getMessage(), e);
                }
            }

            public void portletApplicationRemoved(
                    PortletRegistryEvent evt) {
            	// do nothing
            }
        });
        try {
            syncPlutoWithDB();
        } catch (Exception e) {
            logger.error("Error synchronizing deployed portlets state with the internal registry. Cause: " + e.getMessage(), e);
        }
    }

} // end JahiaApplicationsService

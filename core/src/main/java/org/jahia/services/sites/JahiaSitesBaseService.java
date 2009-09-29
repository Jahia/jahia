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
//
//  JahiaSitesBaseService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.JahiaSiteManager;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.importexport.ExtendedImportResult;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowInfo;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public class JahiaSitesBaseService extends JahiaSitesService {
    private static Logger logger = Logger.getLogger(JahiaSitesBaseService.class);

    protected static JahiaSitesBaseService instance = null;

    public static final String SITE_CACHE_BYID = "JahiaSiteByIDCache";
    public static final String SITE_CACHE_BYNAME = "JahiaSiteByNameCache";
    public static final String SITE_CACHE_BYKEY = "JahiaSiteByKeyCache";
    /** The cache in memory */
    private Cache<Comparable<?>, JahiaSite> siteCacheByID = null;
    private Cache<String, Serializable> siteCacheByName = null;
    private Cache<String, JahiaSite> siteCacheByKey = null;

    // list of sites going to be deleted.
    // This is used by some services like search engine to avoid useless indexation.
    private List<Integer> sitesToDelete = new ArrayList<Integer>();

    protected JahiaSiteManager siteManager = null;
    private JahiaSitePropertyManager sitePropertyManager = null;

    private CacheService cacheService;

    private boolean started = false;

    protected JahiaGroupManagerService groupService;
    protected JahiaFileWatcherService fileWatcherService;
    protected JahiaACLManagerService jahiaAclService;
    protected JCRSessionFactory sessionFactory;
    public synchronized boolean isStarted() {
        return started;
    }

    public synchronized void setStarted(boolean started) {
        this.started = started;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }
    
    public void setSiteManager(JahiaSiteManager siteManager) {
        this.siteManager = siteManager;
    }

    public void setSitePropertyManager(JahiaSitePropertyManager sitePropertyManager) {
        this.sitePropertyManager = sitePropertyManager;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setFileWatcherService(JahiaFileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    public void setJahiaAclService(JahiaACLManagerService jahiaAclService) {
        this.jahiaAclService = jahiaAclService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Default constructor, creates a new <code>JahiaSitesBaseService</code> instance.
     *
     * @throws JahiaException
     */
    protected JahiaSitesBaseService () throws JahiaException {
    }


    /**
     * Retrieves the unique instance of this singleton class.
     *
     * @return the unique instance of this class
     *
     * @throws JahiaException
     */
    public static synchronized JahiaSitesBaseService getInstance ()
            throws JahiaException {
        if (instance == null) {
            instance = new JahiaSitesBaseService ();
        }
        return instance;
    }


    /**
     * return the list of all sites
     *
     * @return Iterator an Iterator of JahiaSite bean
     *
     * @todo this only returns the entries that are in the cache !! If the
     * cache was flushed in the meantime, this method is FALSE !
     */
    public Iterator<JahiaSite> getSites () throws JahiaException {
        return new ArrayList<JahiaSite>(siteManager.getSites()).iterator();
    }

    /**
     * return the all sites ids
     *
     * @return Iterator an Iterator of JahiaSite bean
     *
     * @todo this only returns the entries that are in the cache !! If the
     * cache was flushed in the meantime, this method is FALSE !
     */
    public Integer[] getSiteIds () throws JahiaException {
        Integer[] siteIds = new Integer[]{};
        List<Integer> siteIdsList = new ArrayList<Integer>();
        Iterator<JahiaSite> sites = getSites();
        while (sites.hasNext()){
            siteIdsList.add(new Integer(sites.next().getID()));
        }
        siteIds = siteIdsList.toArray(siteIds);
        return siteIds;
    }

    public void start()
            throws JahiaInitializationException {

        siteCacheByID = cacheService.createCacheInstance(SITE_CACHE_BYID);
//        siteCacheByID.registerListener(this);
        siteCacheByName = cacheService.createCacheInstance(SITE_CACHE_BYNAME);
//        siteCacheByName.registerListener(this);
        siteCacheByKey = cacheService.createCacheInstance(SITE_CACHE_BYKEY);
        try {
            loadSitesInCache(settingsBean);
        } catch (JahiaException je) {
            throw new JahiaInitializationException("Error while loading sites in cache", je);
        }
        this.setStarted(true);
    }

    public void stop() {}

    /**
     * return a site bean looking at it id
     *
     * @param id the JahiaSite id
     *
     * @return JahiaSite the JahiaSite bean
     */
    public JahiaSite getSite(int id) throws JahiaException {
        JahiaSite site = siteCacheByID.get(new Integer(id));
        if (site != null) {
            return site;
        }
        // try to load from db

        site = siteManager.getSiteById(id);
        // if the site could be loaded, add it into the cache
        if (site != null) {
            addToCache(site);
        }

        return site;
    }


    private synchronized void addToCache(JahiaSite site) {
        siteCacheByID.put(new Integer(site.getID()), site);
        if (site.getServerName() != null) {
            siteCacheByName.put(site.getServerName(), site);
        }
        siteCacheByKey.put(site.getSiteKey(), site);
    }
    
    /**
	 * return a site bean looking at it key
	 * 
	 * @param siteKey
	 *            the site key
	 * 
	 * @return JahiaSite the JahiaSite bean
	 */
    public JahiaSite getSiteByKey(String siteKey) throws JahiaException {
        if (siteKey == null) {
            return null;
        }
        JahiaSite site = siteCacheByKey.get(siteKey);
        if (site != null)
            return site;
        site = siteManager.getSiteByKey(siteKey);
        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        }

        return site;
    }

    /**
	 * Find a site by it's server name value
	 * 
	 * @param serverName
	 *            the server name to look for
	 * 
	 * @return if found, returns the site with the corresponding serverName, or
	 *         the first one if there are multiple, or null if there are none.
	 * 
	 * @throws JahiaException
	 *             thrown if there was a problem communicating with the
	 *             database.
	 */
    public JahiaSite getSiteByServerName(String serverName)
            throws JahiaException {
        if (serverName == null) {
            return null;
        }
        Object cachedObj = siteCacheByName.get(serverName);

        if (cachedObj != null) {
            return cachedObj.equals("") ? null : (JahiaSite)cachedObj;
        }

        // the site was not found in the cache, try to load it from the
        // database.
        JahiaSite site = siteManager.getSiteByName(serverName);
        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        } else {
            siteCacheByName.put(serverName, "");
        }

        return site;
    }


    // --------------------------------------------------------------------------
    // FH 10 May 2001 Cache storing improvments for performance issues.
    /**
     * return a site looking at it's name
     *
     * @param name the site name
     *
     * @return JahiaSite the JahiaSite bean or null
     */
    public JahiaSite getSite(String name) throws JahiaException {
        return getSiteByServerName(name);
    }


    // --------------------------------------------------------------------------
    /**
	 * Add a new site only if there is no other site with same server name
	 * 
	 *
	 * @return boolean false if there is another site using same server name
	 */

//    public synchronized boolean addSite (JahiaSite site)
//            throws JahiaException {
//

    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Properties settings, Locale selectedLocale,
                             String selectTmplSet, String firstImport, File fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, ProcessingContext jParams) throws JahiaException, IOException
    {
        JahiaSearchService searchService = ServicesRegistry.getInstance().getJahiaSearchService();
        
        JahiaBaseACL acl = null;
        acl = new JahiaBaseACL();
        acl.create(0);
        if (settings == null) {
            settings = new Properties();
        }
        settings.setProperty("uuid", ContentObject.idGen.nextIdentifier().toString());
        JahiaSite site = new JahiaSite(-1,title,serverName,siteKey,true,-1,descr,acl,settings);
        site.setTemplatesAutoDeployMode(true);
        site.setWebAppsAutoDeployMode(true);

        // check there is no site with same server name before adding
        if (getSite (site.getServerName ()) == null
                && getSiteByKey (site.getSiteKey ()) == null) {

//            JahiaSitesPersistance.getInstance ().dbAddSite (site);
            siteManager.saveJahiaSite(site);
            if (site.getID () == -1) {
                return null;
            }

            addToCache(site);

            searchService.createSearchHandler(site.getID());
            ServicesRegistry.getInstance().getJahiaEventService().fireSiteAdded(new JahiaEvent(this, Jahia.getThreadParamBean() , site));
        } else {
            return null;
        }



        // continue if the site is added correctly...
        if (site.getID() != -1) {
            if (getNbSites() == 1 && !site.isDefault()) {
                setDefaultSite(site);
            }
            // default value
            site.setStaging(true);
            site.setVersioning(true);

            JahiaUserManagerService jums = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaGroupManagerService jgms = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            JahiaSitesService jsms = ServicesRegistry.getInstance().getJahiaSitesService();

            jsms.updateSite(site);

            site.setMixLanguagesActive(false);

            // settings default permissions
            JahiaAclEntry adminAclEntry = new JahiaAclEntry(7, 0);

            // root admin group
            JahiaGroup adminGrp = jgms.getAdministratorGroup(0);
            site.getACL().setGroupEntry(adminGrp, adminAclEntry);

            // site admin group
            adminGrp = jgms.getAdministratorGroup(site.getID());
            site.getACL().setGroupEntry(adminGrp, adminAclEntry);

            // create default groups...
            JahiaGroup usersGroup = jgms.lookupGroup(0,JahiaGroupManagerService.USERS_GROUPNAME);
            if (usersGroup == null) {
                usersGroup = jgms.createGroup(0, JahiaGroupManagerService.USERS_GROUPNAME, null, false);
            }

            JahiaGroup guestGroup = jgms.lookupGroup(0,JahiaGroupManagerService.GUEST_GROUPNAME);
            if (guestGroup == null) {
                guestGroup = jgms.createGroup(0, JahiaGroupManagerService.GUEST_GROUPNAME, null, false);
            }

            JahiaGroup adminGroup = jgms.lookupGroup(site.getID(), JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
            if (adminGroup == null) {
                adminGroup = jgms.createGroup(site.getID(),
                        JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME, null, false);
            }

            // create groups memberships...
            JahiaSiteTools.addGroup(adminGroup, site);
            JahiaSiteTools.addGroup(usersGroup, site);
            JahiaSiteTools.addGroup(guestGroup, site);

            JahiaUser guestSiteUser = jums.lookupUser(JahiaUserManagerService.GUEST_USERNAME);

            // attach superadmin user (current) to administrators group...
            adminGroup.addMember(currentUser);

            // create creator user membership for this site...
            JahiaSiteTools.addMember(currentUser, site);

            // create guest user membership for this site...
            JahiaSiteTools.addMember(guestSiteUser, site);

            File initialZip = null;
            String initialZipName = null;
            JahiaTemplatesPackage templPackage = null;
            if (selectTmplSet != null) {
                templPackage = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService()
                        .associateTemplatePackageWithSite(selectTmplSet,
                                site);

                if ("defaultImport".equals(firstImport)) {
                    initialZip = templPackage.getInitialImport() != null ? new File(
                            templPackage.getRootFolderPath(), templPackage
                            .getInitialImport())
                            : null;
                }
            }

            jParams.setSite(site);
            jParams.setSiteID(site.getID());
            jParams.setSiteKey(site.getSiteKey());
            jParams.setCurrentLocale(selectedLocale);
            if (selectedLocale != null) {
                jParams.getEntryLoadRequest().setFirstLocale(selectedLocale.toString());
            }
            if ("fileImport".equals(firstImport)) {
                initialZip = fileImport;
                initialZipName = fileImportName;
            }
            if (!"importRepositoryFile".equals(firstImport) && (initialZip == null || !initialZip.exists() || "noImport".equals(firstImport))) {
                // create site language
                SiteLanguageSettings newLanguage =
                        new SiteLanguageSettings(site.getID(), selectedLocale.toString(), true, 1, false);
                JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
                listManager.addSiteLanguageSettings(newLanguage);

                // create the default homepage...
                EntryLoadRequest saveRequest = new EntryLoadRequest(EntryLoadRequest.STAGED);
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(selectedLocale);
                saveRequest.setLocales(locales);
                EntryLoadRequest savedEntryLoadRequest =
                        jParams.getSubstituteEntryLoadRequest();
                jParams.setSubstituteEntryLoadRequest(saveRequest);

                JahiaPageService pageService = ServicesRegistry.getInstance().getJahiaPageService();

                int homePageDefId = ServicesRegistry.getInstance()
                        .getJahiaPageTemplateService()
                        .lookupPageTemplateByName(
                                templPackage.getHomePageTemplate()
                                        .getName(), site.getID()).getID();


                JahiaBaseACL jAcl = new JahiaBaseACL ();
                jAcl.create (site.getAclID());

                JahiaPage page = pageService.createPage(site.getID(),
                        0,
                        PageInfoInterface.TYPE_DIRECT,
                        "Welcome to " + site.getServerName(),
                        homePageDefId,
                        "http://",
                        -1,
                        jParams.getUser().getUserKey(),
                        jAcl.getID(),
                        jParams);

                WorkflowInfo wfInfo = ServicesRegistry.getInstance()
                        .getWorkflowService().getDefaultWorkflowEntry();
                if (WorkflowService.EXTERNAL == wfInfo.getMode()) {
                    ServicesRegistry.getInstance().getWorkflowService()
                            .setWorkflowMode(page.getContentPage(),
                                    wfInfo.getMode(), wfInfo.getWorkflowName(),
                                    wfInfo.getProcessId(), jParams);
                } else if (WorkflowService.INACTIVE == wfInfo.getMode()) {
                    ServicesRegistry.getInstance().getWorkflowService()
                            .setWorkflowMode(page.getContentPage(),
                                    WorkflowService.INACTIVE, null, null,
                                    jParams);
                }
                
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

                logger.debug("Home Page created");

                // enable guest user to access the page
                JahiaAclEntry guestAclEntry = new JahiaAclEntry(1, 0);

                page.getACL().setGroupEntry(guestGroup, guestAclEntry);

                // enable admin group to admin the page
                adminAclEntry = new JahiaAclEntry(7, 0);
                page.getACL().setGroupEntry(adminGroup, adminAclEntry);
                JahiaEvent setRightsEvent = new JahiaEvent(this, jParams, page.getACL());
                ServicesRegistry.getInstance ().getJahiaEventService ().fireSetRights(setRightsEvent);


                try {
                    JCRNodeWrapper source = page.getContentPage().getJCRNode(jParams);
                    Node parent = source.getParent();
                    if (parent.isNodeType(Constants.JAHIANT_VIRTUALSITE)) {
                        Node dest = sessionFactory.getCurrentUserSession().getNode("/content/sites/"+parent.getName());
                        source.copyFile(dest.getPath());
                        dest.save();
                    }
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


            } else {
                // enable admin group to admin the page
                adminAclEntry = new JahiaAclEntry(7, 0);
                site.getACL().setGroupEntry(adminGroup, adminAclEntry);
                JahiaEvent setRightsEvent = new JahiaEvent(this, jParams, site.getACL());
                ServicesRegistry.getInstance ().getJahiaEventService ().fireSetRights(setRightsEvent);
                if (asAJob == null || asAJob.booleanValue()) {

                    // check if we need to import users synchronously
                    if (doImportServerPermissions != null
                            && doImportServerPermissions.booleanValue()) {
                        // import users and groups
                        try {
                            ImportExportBaseService.getInstance()
                                    .importUsers(initialZip, jParams.getSite());
                        } catch (Exception e) {
                            logger.error(
                                    "Unable to import users for the site '"
                                            + site.getTitle() + "'["
                                            + site.getSiteKey() + "]", e);
                        }
                    }

                    JobDetail jobDetail = BackgroundJob.createJahiaJob("Initial import for site "+site.getSiteKey(), ImportJob.class, jParams);
                    JobDataMap jobDataMap;
                    jobDataMap = jobDetail.getJobDataMap();

                    if ("importRepositoryFile".equals(firstImport)) {
                        jobDataMap.put(ImportJob.URI, fileImportName);
                        jobDataMap.put(ImportJob.FILENAME, fileImportName.substring(fileImportName.lastIndexOf('/')+1));
                    } else {
                        String dateOfExport = ImportExportBaseService.DATE_FORMAT.format(new Date());
                        String uploadname = "initialImport_" + dateOfExport.replaceAll(":", "-") + ".zip";
                        List<JCRNodeWrapper> l = ServicesRegistry.getInstance().getJCRStoreService().getImportDropBoxes(null, jParams.getUser());
                        JCRNodeWrapper importFolder = l.iterator().next();
                        try {
                            importFolder.uploadFile(uploadname, new FileInputStream(
                                    initialZip),
                                    "application/zip");
                            importFolder.save();
                        } catch (RepositoryException e) {
                            logger.error("cannot upload file",e);
                        }
                        jobDataMap.put(ImportJob.URI, importFolder.getPath()+"/"+uploadname);
                        if (initialZipName == null) {
                            initialZipName = initialZip.getName();
                        }
                        jobDataMap.put(ImportJob.FILENAME, initialZipName);
                    }
                    jobDataMap.put(ImportJob.CONTENT_TYPE, "application/zip");
                    Set<LockKey> locks = new HashSet<LockKey>();
                    LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_SITE", site.getID());
                    locks.add(lock);
                    LockRegistry.getInstance().acquire(lock, jParams.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime(), false);
                    jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);
                    jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, site.getID());
                    jobDataMap.put(BackgroundJob.JOB_TYPE,ImportJob.IMPORT_TYPE);
                    jobDataMap.put(ImportJob.DELETE_FILE, Boolean.TRUE);
                    jobDataMap.put(ImportJob.COPY_TO_JCR, Boolean.TRUE);

                    ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
                } else {
                    ServicesRegistry.getInstance().getImportExportService().importFile(null, jParams, initialZip, true, new ArrayList<ImportAction>(), new ExtendedImportResult());
                }
            }

            logger.debug("Site updated with Home Page");

            // start and create the site's new templates folder
//                JahiaSiteTools.startTemplateObserver(site,
//                        jParams.settings(),
//                        ServicesRegistry.getInstance().getJahiaTemplatesDeployerService(),
//                        ServicesRegistry.getInstance().getJahiaFileWatcherService());

            // create the search index
            searchService.createSearchHandler(site.getID());
            searchService.indexSite(site.getID(), jParams.getUser());
        } else {
            removeSite(site);      // remove site because the process generate error(s)...
            return null;
        }
        return site;
    }


    /**
     * remove a site
     *
     * @param site the JahiaSite bean
     */
    public synchronized void removeSite (JahiaSite site) throws JahiaException {
//        JahiaSitesPersistance.getInstance ().dbRemoveSite (site.getID ());
        sitePropertyManager.remove(site);
        siteManager.remove(site.getID());
        siteCacheByID.remove (new Integer (site.getID ()));
        siteCacheByName.remove(site.getServerName());
        siteCacheByKey.remove(site.getSiteKey());
        ServicesRegistry.getInstance().getJahiaEventService().fireSiteDeleted(new JahiaEvent(this,null,site));
        cacheService.createCacheInstance(JahiaPageBaseService.CONTENT_PAGE_CACHE).flush();
    }


    /**
     * Update a JahiaSite definition
     *
     * @param site the site bean object
     */
    public synchronized void updateSite (JahiaSite site) throws JahiaException {
//        JahiaSitesPersistance.getInstance ().dbUpdateSite (site);
        JahiaSite defaultSite = this.getDefaultSite();
        Properties props = new Properties();
        if (site.getSettings() != null){
            props = (Properties)site.getSettings().clone();
        }
        siteManager.updateJahiaSite(site);
        if ( defaultSite != null && defaultSite.getSiteKey().equals(site.getSiteKey()) ){
            siteManager.setDefaultSite(site);
        }
        site.setSettings(props);
        sitePropertyManager.save(site);
        siteCacheByName.flush();
        addToCache(site);
    }
    
    /**
     * Update a JahiaSite definition
     *
     * @param site the site bean object
     */
    public synchronized void updateSiteProperties (JahiaSite site, Properties props) throws JahiaException {
        sitePropertyManager.save(site, props);
        site.getSettings().putAll(props);
        addToCache(site);
    }

    public synchronized void removeSiteProperties (JahiaSite site, List<String> propertiesToBeRemoved) throws JahiaException {
        sitePropertyManager.remove(site,propertiesToBeRemoved);
        for (String key : propertiesToBeRemoved) {
            site.getSettings().remove(key);
        }
        addToCache(site);
    }

    /**
     * load all sites from database in cache
     */
    protected void loadSitesInCache (SettingsBean settingsBean) throws JahiaException {

        List<JahiaSite> sites = siteManager.getSites();
        if (sites != null) {
            int size = sites.size ();
            for (int i = 0; i < size; i++) {
                JahiaSite site = sites.get (i);
                // start and create the site's new templates folder if not exists
                // JahiaSiteTools.startTemplateObserver (site, settingsBean, templateDeployerService, fileWatcherService);
                addToCache(site);
            }
        }
    }


    /**
     * Add a site to the list of site going to be deleted
     *
     * @param siteID the site id
     */
    public synchronized void addSiteToDelete (int siteID) {
        Integer I = new Integer (siteID);
        if (!this.sitesToDelete.contains (I)) {
            this.sitesToDelete.add (I);
        }
    }

    /**
     * Remove a given site from the list of site going to be deleted
     *
     * @param siteID the site id
     */
    public synchronized void removeSiteToDelete (int siteID) {
        this.sitesToDelete.remove (new Integer (siteID));
    }

    /**
     * Return true if the given site is going to be deleted
     *
     * @param siteID the site id
     *
     * @return boolean
     */
    public synchronized boolean isSiteToBeDeleted (int siteID) {
        return (this.sitesToDelete.contains (new Integer (siteID)));
    }

    // javadocs automaticaly imported from the JahiaSitesService class.
    //
    public int getNbSites ()
            throws JahiaException {
        return siteManager.getNbSites ();
    }


    /**
     * returns a DOM representation of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getSiteAsDOM (int siteID) throws JahiaException {

//        return JahiaSitesPersistance.getInstance ().getSiteAsDOM (siteID);
        return null;
    }


    /**
     * returns a DOM representation of a site's properties
     *
     * @param siteID
     */
    public JahiaDOMObject getSitePropsAsDOM (int siteID) throws JahiaException {

//        return JahiaSitesPersistance.getInstance ().getSitePropsAsDOM (siteID);
        return null;
    }


    /**
     * Returns a properties as a String.
     *
     * @param siteID
     * @param key    the property name
     */
    public String getProperty (int siteID, String key)
            throws JahiaException {
//        return JahiaSitesPersistance.getInstance ().getProperty (siteID, key);
        return "";
    }

    public JahiaSite getDefaultSite() {
        JahiaSite site = siteCacheByID.get("default");
        if (site != null) {
            return site;
        }
        site = siteManager.getDefaultSite();
        siteCacheByID.put("default", site);
        return site;
    }

    public void setDefaultSite(JahiaSite site) {
        siteManager.setDefaultSite(site);
        siteCacheByID.put("default", site);
    }


    public List<JahiaSite> findSiteByPropertyNameAndValue(String name, String value) throws JahiaException {
        List<JahiaSite> r = new ArrayList<JahiaSite>();
        for (Integer integer : siteManager.findSiteIdByPropertyNameAndValue(name, value)) {
            r.add(getSite(integer));
        }
        return r;
    }
}

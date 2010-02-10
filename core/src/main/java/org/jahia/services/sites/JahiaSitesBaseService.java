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
import org.jahia.bin.Jahia;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.importexport.ExtendedImportResult;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.jcr.JCRSitesProvider;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

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

    protected JCRSitesProvider siteProvider = null;
    private JahiaSitePropertyManager sitePropertyManager = null;

    private CacheService cacheService;

    protected JahiaGroupManagerService groupService;
    protected JahiaFileWatcherService fileWatcherService;
    protected JCRSessionFactory sessionFactory;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setSiteProvider(JCRSitesProvider siteProvider) {
        this.siteProvider = siteProvider;
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
        return new ArrayList<JahiaSite>(siteProvider.getSites()).iterator();
    }

    public void start()
            throws JahiaInitializationException {

        siteCacheByID = cacheService.createCacheInstance(SITE_CACHE_BYID);
//        siteCacheByID.registerListener(this);
        siteCacheByName = cacheService.createCacheInstance(SITE_CACHE_BYNAME);
//        siteCacheByName.registerListener(this);
        siteCacheByKey = cacheService.createCacheInstance(SITE_CACHE_BYKEY);
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

        site = siteProvider.getSiteById(id);
        // if the site could be loaded, add it into the cache
        if (site != null) {
            addToCache(site);
        }

        return site;
    }


    private synchronized void addToCache(JahiaSite site) {
        siteCacheByID.put(site.getID(), site);
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
        site = siteProvider.getSiteByKey(siteKey);
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
        JahiaSite site = siteProvider.getSiteByName(serverName);
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
        if (settings == null) {
            settings = new Properties();
        }
        JahiaSite site = new JahiaSite(-1,title,serverName,siteKey, descr,settings);

        if (selectTmplSet != null) {
            site.setTemplatePackageName(selectTmplSet);
        }
        // create site language
        site.setDefaultLanguage(selectedLocale.toString());
        site.setLanguages(new LinkedHashSet<String>(Arrays.asList(selectedLocale.toString())));
        site.setMandatoryLanguages(site.getLanguages());
        site.setMixLanguagesActive(false);
        // check there is no site with same server name before adding
        if (getSiteByKey (site.getSiteKey ()) == null) {
            siteProvider.addSite(site, jParams.getUser());
            if (site.getID () == -1) {
                return null;
            }

            addToCache(site);

            ServicesRegistry.getInstance().getJahiaEventService().fireSiteAdded(new JahiaEvent(this, Jahia.getThreadParamBean() , site));
        } else {
            throw new IOException("site already exists");
        }



        // continue if the site is added correctly...
        if (site.getID() != -1) {
            if (getNbSites() == 1 && !site.isDefault()) {
                setDefaultSite(site);
            }

            JahiaUserManagerService jums = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaGroupManagerService jgms = ServicesRegistry.getInstance().getJahiaGroupManagerService();

            updateSite(site);

            site.setMixLanguagesActive(false);

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

                // create the default homepage...
                EntryLoadRequest saveRequest = new EntryLoadRequest(EntryLoadRequest.STAGED);
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(selectedLocale);
                saveRequest.setLocales(locales);
                EntryLoadRequest savedEntryLoadRequest =
                        jParams.getSubstituteEntryLoadRequest();
                jParams.setSubstituteEntryLoadRequest(saveRequest);
                jParams.setSubstituteEntryLoadRequest(savedEntryLoadRequest);

            if ("importRepositoryFile".equals(firstImport) || (initialZip != null && initialZip.exists() && !"noImport".equals(firstImport))) {
                // enable admin group to admin the page
                if (asAJob == null || asAJob.booleanValue()) {

                    // check if we need to import users synchronously
                    if (doImportServerPermissions != null
                            && doImportServerPermissions.booleanValue()) {
                        // import users and groups
                        try {
                            ImportExportBaseService.getInstance()
                                    .importUsersFromZip(initialZip, jParams.getSite());
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
                    try {
                        ServicesRegistry.getInstance().getImportExportService().importSiteZip(initialZip, new ArrayList<ImportAction>(), new ExtendedImportResult(), jParams.getSite());
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    JCRSessionWrapper session = sessionFactory.getCurrentUserSession(null, jParams.getLocale());
                    JCRNodeWrapper nodeWrapper = session.getNode("/sites/" + site.getSiteKey());
                    if (!nodeWrapper.hasNode("home")) {
                        session.checkout(nodeWrapper);
                        JCRNodeWrapper page = nodeWrapper.addNode("home", "jnt:page");
                        page.setProperty("jcr:title","Welcome to " + site.getServerName());
                        session.save();
                    }
                } catch (RepositoryException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            logger.debug("Site updated with Home Page");

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
        JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        List<String> groups = groupManagerService.getGroupList(site.getID());
        for (String group : groups) {
            groupManagerService.deleteGroup(groupService.lookupGroup(group));
        }
        sitePropertyManager.remove(site);
        siteProvider.deleteSite(site.getSiteKey());

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
//        JahiaSite defaultSite = this.getDefaultSite();
        Properties props = new Properties();
        if (site.getSettings() != null){
            props = (Properties)site.getSettings().clone();
        }

        //todo update jahia site name/description
//        siteManager.updateJahiaSite(site);
        siteProvider.updateSite(site);
        site.setSettings(props);
        sitePropertyManager.save(site);
        siteCacheByName.flush();
        siteCacheByID.flush();
        siteCacheByKey.flush();
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

        List<JahiaSite> sites = siteProvider.getSites();
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


    // javadocs automaticaly imported from the JahiaSitesService class.
    //
    public int getNbSites ()
            throws JahiaException {
        return siteProvider.getNbSites ();
    }

    public JahiaSite getDefaultSite() {
        JahiaSite site = siteCacheByID.get("default");
        if (site != null) {
            return site;
        }
        site = siteProvider.getDefaultSite();
        siteCacheByID.put("default", site);
        return site;
    }

    public void setDefaultSite(JahiaSite site) {
        siteProvider.setDefaultSite(site);
        siteCacheByID.put("default", site);
    }


}

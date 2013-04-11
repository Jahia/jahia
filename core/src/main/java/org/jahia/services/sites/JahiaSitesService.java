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

//
//  JahiaSitesBaseService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.core.io.Resource;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public class JahiaSitesService extends JahiaService implements JahiaAfterInitializationService {
    // authorized chars
    private static final String AUTHORIZED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789.-";
    private static Logger logger = LoggerFactory.getLogger(JahiaSitesService.class);

    private static final Pattern JCR_KEY_PATTERN = Pattern.compile("{jcr}", Pattern.LITERAL);

    protected static JahiaSitesService instance = null;

    public static final String SITE_CACHE_BYID = "JahiaSiteByIDCache";
    public static final String SITE_CACHE_BYNAME = "JahiaSiteByNameCache";
    public static final String SITE_CACHE_BYKEY = "JahiaSiteByKeyCache";
    public static final String SYSTEM_SITE_KEY = "systemsite";
    public static final String SITES_JCR_PATH = "/sites";
    /**
     * The cache in memory
     */
    private Cache<Comparable<?>, JahiaSite> siteCacheByID = null;
    private Cache<String, Serializable> siteCacheByName = null;
    private Cache<String, JahiaSite> siteCacheByKey = null;

    private CacheService cacheService;

    protected JahiaGroupManagerService groupService;
    protected JCRSessionFactory sessionFactory;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Default constructor, creates a new <code>JahiaSitesBaseService</code> instance.
     *
     * @throws JahiaException
     */
    protected JahiaSitesService() {
        super();
    }


    /**
     * Retrieves the unique instance of this singleton class.
     *
     * @return the unique instance of this class
     */
    public static JahiaSitesService getInstance() {
        if (instance == null) {
            synchronized (JahiaSitesService.class) {
                if (instance == null) {
                    instance = new JahiaSitesService();
                }
            }
        }
        return instance;
    }


    /**
     * return the list of all sites
     *
     * @return List<JCRSiteNode> List of JCRSiteNode
     */
    public List<JCRSiteNode> getSitesNodeList() throws RepositoryException {
        return getSitesNodeList(sessionFactory.getCurrentUserSession());
    }

    public List<JCRSiteNode> getSitesNodeList(JCRSessionWrapper session) throws RepositoryException {
        final List<JCRSiteNode> list = new ArrayList<JCRSiteNode>();
        NodeIterator ni = session.getNode(SITES_JCR_PATH).getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
            if (nodeWrapper.isNodeType("jnt:virtualsite")) {
                list.add((JCRSiteNode) nodeWrapper);
            }
        }
        return list;
    }

    public void start()
            throws JahiaInitializationException {

        siteCacheByID = cacheService.getCache(SITE_CACHE_BYID, true);
        siteCacheByName = cacheService.getCache(SITE_CACHE_BYNAME, true);
        siteCacheByKey = cacheService.getCache(SITE_CACHE_BYKEY, true);
    }

    public void stop() {
    }

    private JahiaSite getSite(JCRNodeWrapper node) throws RepositoryException {
        if (node == null) {
            return null;
        }

        int siteId = (int) node.getProperty("j:siteId").getLong();

        Properties props = new Properties();

        JahiaSite site = new JahiaSite(siteId, node.getProperty("j:title").getString(), node.getProperty("j:serverName").getString(),
                node.getName(), node.getProperty("j:description").getString(), props, node.getPath());
        Value s = node.getProperty("j:templatesSet").getValue();
        final JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAnyDeployedTemplatePackage(s.getString());
        if (aPackage != null) {
            site.setTemplatePackageName(aPackage.getName());
            site.setTemplateFolder(aPackage.getRootFolder());
        }

        List<String> installedModules = new ArrayList<String>();
        Value[] modules = node.getProperty("j:installedModules").getValues();
        for (Value module : modules) {
            installedModules.add(module.getString());
        }
        site.setInstalledModules(installedModules);

        site.setMixLanguagesActive(node.getProperty(SitesSettings.MIX_LANGUAGES_ACTIVE).getBoolean());
        site.setAllowsUnlistedLanguages(node.hasProperty("j:allowsUnlistedLanguages") ? node.getProperty("j:allowsUnlistedLanguages").getBoolean() : Boolean.FALSE);
        site.setDefaultLanguage(node.getProperty(SitesSettings.DEFAULT_LANGUAGE).getString());
        Value[] languages = node.getProperty(SitesSettings.LANGUAGES).getValues();
        Set<String> languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setLanguages(languagesList);

        if (node.hasProperty(SitesSettings.INACTIVE_LANGUAGES)) {
            languagesList = new LinkedHashSet<String>();
            for (Value language : node.getProperty(SitesSettings.INACTIVE_LANGUAGES).getValues()) {
                languagesList.add(language.getString());
            }
            site.setInactiveLanguages(languagesList);
        }

        if (node.hasProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES)) {
            languagesList = new LinkedHashSet<String>();
            for (Value language : node.getProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES).getValues()) {
                languagesList.add(language.getString());
            }
            site.setInactiveLiveLanguages(languagesList);
        }

        languages = node.getProperty(SitesSettings.MANDATORY_LANGUAGES).getValues();
        languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setMandatoryLanguages(languagesList);
        site.setUuid(node.getIdentifier());

        for (String setting : SitesSettings.HTML_SETTINGS) {
            if (node.hasProperty(setting)) {
                site.getSettings().put(setting, JCRContentUtils.getValue(node.getProperty(setting).getValue()));
            }
        }

        return site;
    }

    /**
     * return a site bean looking at it id
     *
     * @param id the JahiaSite id
     * @return JahiaSite the JahiaSite bean
     */
    public JahiaSite getSite(final int id) throws JahiaException {
        JahiaSite site = siteCacheByID.get(Integer.valueOf(id));
        if (site != null) {
            return site;
        }
        // try to load from db

        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getSite(JahiaSitesService.this.getSite(id, session));
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }

        // if the site could be loaded, add it into the cache
        if (site != null) {
            addToCache(site);
        }

        return site;
    }

    private JCRNodeWrapper getSite(int id, JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:siteId]=" + id, Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        if (ni.hasNext()) {
            return (JCRNodeWrapper) ni.next();
        }
        return null;
    }


    private void addToCache(JahiaSite site) {
        siteCacheByID.put(site.getID(), site);
        if (site.getServerName() != null) {
            siteCacheByName.put(site.getServerName(), site);
        }
        siteCacheByKey.put(site.getSiteKey(), site);
    }

    /**
     * return a site bean looking at it key
     *
     * @param siteKey the site key
     * @return JahiaSite the JahiaSite bean
     */
    public JahiaSite getSiteByKey(final String siteKey) throws JahiaException {
        if (StringUtils.isEmpty(siteKey)) {
            return null;
        }

        if (siteCacheByKey.containsKey(siteKey))
            return siteCacheByKey.get(siteKey);

        JahiaSite site = null;

        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getSite(JahiaSitesService.this.getSiteByKey(siteKey, session));
                }
            });
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }

        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        } else {
            siteCacheByKey.put(siteKey, null);
        }

        return site;
    }

    private JCRNodeWrapper getSiteByKey(String siteKey, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper n = session.getNode("/sites/" + siteKey);
        return n;
    }

    /**
     * Find a site by it's server name value
     *
     * @param serverName the server name to look for
     * @return if found, returns the site with the corresponding serverName, or
     *         the first one if there are multiple, or null if there are none.
     * @throws JahiaException thrown if there was a problem communicating with the
     *                        database.
     */
    public JahiaSite getSiteByServerName(final String serverName)
            throws JahiaException {
        if (serverName == null) {
            return null;
        }
        Object cachedObj = siteCacheByName.get(serverName);

        if (cachedObj != null) {
            return cachedObj.equals("") ? null : (JahiaSite) cachedObj;
        }

        // the site was not found in the cache, try to load it from the
        // database.

        JahiaSite site = null;
        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getSite(getSiteByServerName(serverName, session));
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }

        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        } else {
            siteCacheByName.put(serverName, "");
        }

        return site;
    }

    private JCRNodeWrapper getSiteByServerName(String serverName, JCRSessionWrapper session) throws RepositoryException {
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:serverName]='" + serverName + "'", Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        if (ni.hasNext()) {
            return (JCRNodeWrapper) ni.next();
        }
        return null;
    }


    /**
     * return a site looking at it's name
     *
     * @param name the site name
     * @return JahiaSite the JahiaSite bean or null
     */
    public JahiaSite getSite(String name) throws JahiaException {
        return getSiteByServerName(name);
    }


    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, String firstImport, Resource fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException {
        return addSite(currentUser, title, serverName, siteKey, descr, selectedLocale,
                selectTmplSet, null, firstImport, fileImport, fileImportName, asAJob,
                doImportServerPermissions, originatingJahiaRelease, null, null);
    }

    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, final String[] modulesToDeploy, String firstImport, Resource fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException {
        return addSite(currentUser, title, serverName, siteKey, descr, selectedLocale, selectTmplSet, modulesToDeploy, firstImport, fileImport, fileImportName,
                asAJob, doImportServerPermissions, originatingJahiaRelease, null, null);
    }

    public JahiaSite addSite(final JahiaUser currentUser, final String title, final String serverName, final String siteKey, final String descr,
                             final Locale selectedLocale, final String selectTmplSet, final String[] modulesToDeploy, final String firstImport, final Resource fileImport, final String fileImportName,
                             final Boolean asAJob, final Boolean doImportServerPermissions, final String originatingJahiaRelease, final String legacyMappingFilePath, final String legacyDefinitionsFilePath) throws JahiaException, IOException {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        return addSite(currentUser, title, serverName, siteKey, descr, selectedLocale, selectTmplSet, modulesToDeploy, firstImport, fileImport, fileImportName, asAJob, doImportServerPermissions, originatingJahiaRelease, legacyMappingFilePath, legacyDefinitionsFilePath, session);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    } catch (JahiaException e) {
                        throw new RepositoryException(e);
                    }
                }
            });
        } catch (RepositoryException e) {
            try {
                throw e.getCause();
            } catch (IOException ee) {
                throw ee;
            } catch (JahiaException ee) {
                throw ee;
            } catch (Throwable ee) {
                throw new JahiaException("", "", 0, 0, e);
            }
        }
    }

    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, final String[] modulesToDeploy, String firstImport, Resource fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease, String legacyMappingFilePath, String legacyDefinitionsFilePath, JCRSessionWrapper session) throws JahiaException, IOException {
        JahiaSite site = new JahiaSite(-1, title, serverName, siteKey, descr, null, "/sites/" + siteKey);

        final JahiaTemplateManagerService templateService = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();

        if (selectTmplSet != null) {
            site.setTemplatePackageName(selectTmplSet);
            site.setTemplateFolder(templateService.getAnyDeployedTemplatePackage(selectTmplSet).getRootFolder());
        }
        // create site language
        site.setDefaultLanguage(selectedLocale.toString());
        site.setLanguages(new LinkedHashSet<String>(Arrays.asList(selectedLocale.toString())));
        site.setMandatoryLanguages(site.getLanguages());
        site.setAllowsUnlistedLanguages(false);
        site.setMixLanguagesActive(false);
        // check there is no site with same server name before adding
        boolean importingSystemSite = false;

        try {

            if (getSiteByKey(site.getSiteKey()) == null) {
                final String siteKey1 = site.getSiteKey();
                final String templatePackage = site.getTemplateFolder();

                site.setInstalledModules(new ArrayList<String>(Collections.singleton(templatePackage /*+ ":" + aPackage.getLastVersion()*/)));

                final Set<String> languages = site.getLanguages();
                final Set<String> inactiveLiveLanguages = site.getInactiveLiveLanguages();
                final Set<String> inactiveLanguages = site.getInactiveLanguages();
                final Set<String> mandatoryLanguages = site.getMandatoryLanguages();

                int id = 1;
                List<JCRSiteNode> sites = getSitesNodeList();
                for (JCRSiteNode jahiaSite : sites) {
                    if (id <= jahiaSite.getID()) {
                        id = jahiaSite.getID() + 1;
                    }
                }
                final int siteId = id;
                site.setID(id);
                final JahiaSite finalSite = site;

                Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jnt:virtualsitesFolder]", Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();

                while (ni.hasNext()) {
                    JCRNodeWrapper sitesFolder = (JCRNodeWrapper) ni.nextNode();
                    String options = "";
                    if (sitesFolder.hasProperty("j:virtualsitesFolderConfig")) {
                        options = sitesFolder.getProperty("j:virtualsitesFolderConfig").getString();
                    }

                    JCRNodeWrapper f = JCRContentUtils.getPathFolder(sitesFolder, siteKey1, options, "jnt:virtualsitesFolder");
                    try {
                        f.getNode(siteKey1);
                    } catch (PathNotFoundException e) {
                        JCRNodeWrapper siteNode = f.addNode(siteKey1, "jnt:virtualsite");

                        if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                            String skeletons = sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString();
                            try {
                                JCRContentUtils.importSkeletons(skeletons, f.getPath() + "/" + siteKey1, session);
                            } catch (Exception importEx) {
                                logger.error("Unable to import data using site skeleton " + skeletons, importEx);
                            }
                        }

                        siteNode.setProperty("j:title", finalSite.getTitle());
                        siteNode.setProperty("j:description", finalSite.getDescr());
                        siteNode.setProperty("j:serverName", finalSite.getServerName());
                        siteNode.setProperty("j:siteId", siteId);
                        siteNode.setProperty(SitesSettings.DEFAULT_LANGUAGE, finalSite.getDefaultLanguage());
                        siteNode.setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE, finalSite.isMixLanguagesActive());
                        siteNode.setProperty(SitesSettings.LANGUAGES, languages.toArray(new String[languages.size()]));
                        siteNode.setProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES, inactiveLiveLanguages.toArray(new String[inactiveLiveLanguages.size()]));
                        siteNode.setProperty(SitesSettings.INACTIVE_LANGUAGES, inactiveLanguages.toArray(new String[inactiveLanguages.size()]));
                        siteNode.setProperty(SitesSettings.MANDATORY_LANGUAGES, mandatoryLanguages.toArray(new String[mandatoryLanguages
                                .size()]));
                        siteNode.setProperty("j:templatesSet", templatePackage);

                        siteNode.setProperty("j:installedModules", new Value[]{session.getValueFactory().createValue(templatePackage /*+ ":" + aPackage.getLastVersion()*/)});

                        List<JahiaTemplatesPackage> modules = new ArrayList<JahiaTemplatesPackage>(
                                2 + (modulesToDeploy != null ? modulesToDeploy.length : 0));
                        modules.add(templateService.getAnyDeployedTemplatePackage("default"));
                        modules.add(templateService.getAnyDeployedTemplatePackage(templatePackage));
                        if (modulesToDeploy != null) {
                            for (String s : modulesToDeploy) {
                                JahiaTemplatesPackage packageByFileName = templateService.getAnyDeployedTemplatePackage(s);
                                if (!modules.contains(packageByFileName)) {
                                    modules.add(packageByFileName);
                                }
                            }
                        }
                        String target = "/sites/" + siteKey1;
                        try {
                            logger.info("Deploying modules to {}", target);
                            templateService.installModules(modules, target, session);
                        } catch (RepositoryException re) {
                            logger.error("Unable to deploy modules to "
                                    + target + ". Cause: " + re.getMessage(), re);
                        }
                        //Auto deploy all modules that define this behavior on site creation
                        final List<JahiaTemplatesPackage> availableTemplatePackages = templateService.getAvailableTemplatePackages();
                        for (JahiaTemplatesPackage availableTemplatePackage : availableTemplatePackages) {
                            if (availableTemplatePackage.getAutoDeployOnSite() != null) {
                                if ("all".equals(availableTemplatePackage.getAutoDeployOnSite()) || siteKey1.equals(availableTemplatePackage.getAutoDeployOnSite())) {
                                    String source = "/modules/" + availableTemplatePackage.getRootFolder();
                                    try {
                                        logger.info("Deploying module {} to {}", source, target);
                                        templateService.installModule(availableTemplatePackage, target, session);
                                    } catch (RepositoryException re) {
                                        logger.error("Unable to deploy module " + source + " to "
                                                + target + ". Cause: " + re.getMessage(), re);
                                    }
                                }
                            }
                        }
                    }
                }

                session.save();

                if (site.getID() == -1) {
                    return null;
                }

                addToCache(site);
            } else if (siteKey.equals(SYSTEM_SITE_KEY)) {
                site = getSiteByKey(SYSTEM_SITE_KEY);
                importingSystemSite = true;
            } else {
                throw new IOException("site already exists");
            }

            JCRSiteNode siteNode = (JCRSiteNode) session.getNode(site.getJCRLocalPath());

            // continue if the site is added correctly...
            if (siteNode.getID() != -1) {
                if (!site.isDefault() && !site.getSiteKey().equals(SYSTEM_SITE_KEY) && getNbSites() == 2) {
                    setDefaultSite(site, session);
                }
                if (!importingSystemSite) {
                    JahiaGroupManagerService jgms = ServicesRegistry.getInstance().getJahiaGroupManagerService();

                    siteNode.setMixLanguagesActive(false);
                    session.save();
                    updateSite(siteNode);

                    // create default groups...
                    JahiaGroup usersGroup = jgms.lookupGroup(null, JahiaGroupManagerService.USERS_GROUPNAME);
                    if (usersGroup == null) {
                        usersGroup = jgms.createGroup(null, JahiaGroupManagerService.USERS_GROUPNAME, null, false);
                    }

                    JahiaGroup guestGroup = jgms.lookupGroup(null, JahiaGroupManagerService.GUEST_GROUPNAME);
                    if (guestGroup == null) {
                        guestGroup = jgms.createGroup(null, JahiaGroupManagerService.GUEST_GROUPNAME, null, false);
                    }

                    JahiaGroup privGroup = jgms.lookupGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME);
                    if (privGroup == null) {
                        privGroup = jgms.createGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME, null, true);
                    }

                    JahiaGroup adminGroup = jgms.lookupGroup(site.getSiteKey(),
                            JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
                    if (adminGroup == null) {
                        adminGroup = jgms.createGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, null,
                                false);
                    }

                    // attach superadmin user (current) to administrators group...
                    if (currentUser != null) {
                        adminGroup.addMember(currentUser);
                    }

                    if (!siteKey.equals(SYSTEM_SITE_KEY)) {
                        JahiaGroup sitePrivGroup = jgms.lookupGroup(site.getSiteKey(),
                                JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME);
                        if (sitePrivGroup == null) {
                            sitePrivGroup = jgms.createGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, null,
                                    false);
                        }
                        // atach site privileged group to server privileged
                        privGroup.addMember(sitePrivGroup);
                    }

                    if (!siteKey.equals(SYSTEM_SITE_KEY)) {
                        siteNode.grantRoles("g:" + JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, Collections.singleton("privileged"));
                        siteNode.denyRoles("g:" + JahiaGroupManagerService.PRIVILEGED_GROUPNAME, Collections.singleton("privileged"));
                    }
                    siteNode.grantRoles("g:" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, Collections.singleton("site-administrator"));
                    session.save();
                }
                Resource initialZip = null;
                if ("fileImport".equals(firstImport)) {
                    initialZip = fileImport;
                }

                // create the default homepage...
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(selectedLocale);


                if ("importRepositoryFile".equals(firstImport) || (initialZip != null && initialZip.exists() && !"noImport".equals(firstImport))) {
                    try {
                        Map<Object, Object> importInfos = new HashMap<Object, Object>();
                        importInfos.put("originatingJahiaRelease", originatingJahiaRelease);
                        ServicesRegistry.getInstance().getImportExportService().importSiteZip(initialZip, site, importInfos, legacyMappingFilePath, legacyDefinitionsFilePath, session);
                    } catch (RepositoryException e) {
                        logger.warn("Error importing site ZIP", e);
                    }
                }

//                JCRNodeWrapper nodeWrapper = session.getNode("/sites/" + site.getSiteKey());
//                if (nodeWrapper.hasNode("templates")) {
//                    JCRPublicationService.getInstance().publishByMainId(nodeWrapper.getNode("templates").getIdentifier(), "default", "live", null, true, null);
//                }

                cacheService.getCache("JCRGroupCache").flush();

                logger.debug("Site updated with Home Page");
            } else {
                removeSite(site);      // remove site because the process generate error(s)...
                return null;
            }
        } catch (RepositoryException e) {
            logger.warn("Error adding home node", e);
        }

        return site;
    }

    /**
     * remove a site
     *
     * @param site the JahiaSite bean
     */
    public synchronized void removeSite(final JahiaSite site) throws JahiaException {
        List<String> groups = groupService.getGroupList(site.getSiteKey());
        for (String group : groups) {
            groupService.deleteGroup(groupService.lookupGroup(JCR_KEY_PATTERN.matcher(group).replaceAll("")));
        }
        try {
            JCRCallback<Boolean> deleteCallback = new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode(SITES_JCR_PATH);
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper site1 = sites.getNode(site.getSiteKey());
                    if (sites.hasProperty("j:defaultSite")) {
                        final JCRPropertyWrapper defaultSite = sites.getProperty("j:defaultSite");
                        if (defaultSite.getValue().getString().equals(site1.getIdentifier())) {
                            defaultSite.remove();
                        }
                    }
                    site1.remove();
                    session.save();
                    return true;
                }
            };
            JCRTemplate.getInstance().doExecuteWithSystemSession(deleteCallback);
            // Now let's delete the live workspace site.
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCallback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        invalidateCache(site);
    }


    /**
     * Update a JahiaSite definition
     *
     * @param site the site bean object
     */
    public synchronized void updateSite(final JahiaSite site) throws JahiaException {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    updateSite(site, session);
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        siteCacheByName.flush();
        siteCacheByID.flush();
        siteCacheByKey.flush();
    }

    /**
     * Update JahiaSite for the specified site node
     * @param node : JCRSSiteNode of the site to update
     */
    public void updateSite(JCRSiteNode node) {
        try {
            final JahiaSite legacySite = getSiteByKey(node.getName());
            if (legacySite != null) {
                legacySite.setTitle(node.getTitle());
                legacySite.setDescr(node.getDescr());
                legacySite.setServerName(node.getServerName());

                legacySite.setLanguages(new HashSet<String>(node.getLanguages()));
                legacySite.setInactiveLanguages(new HashSet<String>(node.getInactiveLanguages()));
                legacySite.setInactiveLiveLanguages(new HashSet<String>(node.getInactiveLiveLanguages()));
                legacySite.setMandatoryLanguages(new HashSet<String>(node.getMandatoryLanguages()));
                legacySite.setDefaultLanguage(node.getDefaultLanguage());
                legacySite.setMixLanguagesActive(node.isMixLanguagesActive());
                legacySite.setAllowsUnlistedLanguages(node.isAllowsUnlistedLanguages());

                legacySite.setInstalledModules(node.getInstalledModules());
                legacySite.setTemplatePackageName(node.getTemplatePackageName());
                legacySite.setTemplateFolder(node.getTemplateFolder());

                if (node.getName().equals(SYSTEM_SITE_KEY)) {
                    try {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                updateWorkspacePermissions(session, "default", legacySite);
                                updateWorkspacePermissions(session, "live", legacySite);
                                updateTranslatorRoles(session, legacySite);
                                session.save();
                                return null;
                            }
                        });
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    JahiaSite systemSite = getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY);
                    if (!systemSite.getLanguages().containsAll(legacySite.getLanguages())) {
                        systemSite.getLanguages().addAll(legacySite.getLanguages());
                        updateSite(systemSite);
                    }
                }
            }
        } catch (JahiaException e) {
            if (node != null) {
                logger.error("Cannot update site " + node.getPath() + " because of " + e.getMessage(),e);
            }
        }
    }

    private void updateSite(JahiaSite site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper sites = session.getNode(SITES_JCR_PATH);
        if (!sites.isCheckedOut()) {
            session.checkout(sites);
        }
        JCRNodeWrapper siteNode = sites.getNode(site.getSiteKey());
        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }
        siteNode.setProperty("j:title", site.getTitle());
        siteNode.setProperty("j:description", site.getDescr());
        siteNode.setProperty("j:serverName", site.getServerName());
//                    siteNode.setProperty("j:installedModules", new String[]{site.getTemplatePackageName()});
        String defaultLanguage = site.getDefaultLanguage();
        if (defaultLanguage != null)
            siteNode.setProperty(SitesSettings.DEFAULT_LANGUAGE, defaultLanguage);
        siteNode.setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE, site.isMixLanguagesActive());
        siteNode.setProperty("j:allowsUnlistedLanguages", site.isAllowsUnlistedLanguages());
        siteNode.setProperty(SitesSettings.LANGUAGES, site.getLanguages().toArray(
                new String[site.getLanguages().size()]));
        siteNode.setProperty(SitesSettings.INACTIVE_LANGUAGES, site.getInactiveLanguages().toArray(
                new String[site.getInactiveLanguages().size()]));
        siteNode.setProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES, site.getInactiveLiveLanguages().toArray(
                new String[site.getInactiveLiveLanguages().size()]));
        siteNode.setProperty(SitesSettings.MANDATORY_LANGUAGES, site.getMandatoryLanguages().toArray(
                new String[site.getMandatoryLanguages().size()]));

        for (Map.Entry<Object, Object> prop : site.getSettings().entrySet()) {
            if (!SitesSettings.HTML_SETTINGS.contains(prop.getKey())) {
                continue;
            }
            try {
                siteNode.setProperty(prop.getKey().toString(),
                        JCRContentUtils.createValue(prop.getValue(), session.getValueFactory()));
            } catch (RepositoryException e) {
                logger.warn("Error setting site property '" + prop.getKey() + "'", e);
            }
        }

        if (siteNode.getName().equals(SYSTEM_SITE_KEY)) {
            updateWorkspacePermissions(session, "default", site);
            updateWorkspacePermissions(session, "live", site);
            updateTranslatorRoles(session, site);
        }
    }


    // javadocs automaticaly imported from the JahiaSitesService class.
    //

    public int getNbSites()
            throws JahiaException {
        try {
            return getSitesNodeList().size();
        } catch (RepositoryException e) {
            return 0;
        }
    }

    public JahiaSite getDefaultSite() {
        JahiaSite site = siteCacheByID.get("default");
        if (site != null) {
            return site;
        }

        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return JahiaSitesService.this.getDefaultSite(session);
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }

        siteCacheByID.put("default", site);
        return site;
    }

    private JahiaSite getDefaultSite(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(SITES_JCR_PATH);
        if (node.hasProperty("j:defaultSite")) {
            return getSite((JCRNodeWrapper) node.getProperty("j:defaultSite").getNode());
        } else {
            return null;
        }
    }

    public void setDefaultSite(final JahiaSite site) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JahiaSitesService.this.setDefaultSite(site, session);
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot set default site", e);
        }
        siteCacheByID.put("default", site);
    }

    private void setDefaultSite(JahiaSite site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(SITES_JCR_PATH);
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        if (site != null) {
            JCRNodeWrapper s = node.getNode(site.getSiteKey());
            node.setProperty("j:defaultSite", s);
        } else if (node.hasProperty("j:defaultSite")) {
            node.getProperty("j:defaultSite").remove();
        }
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
//        try {
//            JahiaUser jahiaUser = JCRSessionFactory.getInstance().getCurrentUser();
//            if (getNbSites() == 0) {
//                Locale selectedLocale = LanguageCodeConverters.languageCodeToLocale(systemSiteDefaultLanguage);
//                JahiaSite site = addSite(jahiaUser, systemSiteTitle, systemSiteServername, SYSTEM_SITE_KEY, "", selectedLocale,
//                        systemSiteTemplateSetName, null, "noImport", null, null, false, false, null);
//                site.setMixLanguagesActive(true);
//                site.setAllowsUnlistedLanguages(true);
//                site.setLanguages(systemSiteLanguages);
//                updateSite(site);
//                final LinkedHashSet<String> languages = new LinkedHashSet<String>();
//                languages.add(selectedLocale.toString());
//                final List<String> uuids = new ArrayList<String>();
//                try {
//                    JCRNodeWrapper node = (JCRNodeWrapper) sessionFactory.getCurrentUserSession().getNode(SITES_JCR_PATH).getNodes().nextNode();
//                    node.checkout();
////                     node.changeRoles("g:users","re---");
//                    uuids.add(node.getIdentifier());
//                    List<PublicationInfo> publicationInfos = JCRPublicationService.getInstance().getPublicationInfo(node.getIdentifier(), languages, true, true, true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
//                    for (PublicationInfo publicationInfo : publicationInfos) {
//                        if (publicationInfo.needPublication(null)) {
//                            uuids.addAll(publicationInfo.getAllUuids());
//                        }
//                    }
//                    JCRPublicationService.getInstance().publish(uuids, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, false, null);
//                } catch (RepositoryException e) {
//                    logger.error(e.getMessage(), e);
//                } finally {
//                    JCRSessionFactory.getInstance().closeAllSessions();
//                }
//            }
//        } catch (JahiaException e) {
//            logger.error(e.getMessage(), e);
//        } catch (IOException e) {
//            logger.error(e.getMessage(), e);
//        }
    }

    /**
     * Invalidates the cache for the specified site.
     *
     * @param site the site bean object
     */
    public void invalidateCache(JahiaSite site) throws JahiaException {
        siteCacheByID.remove(site.getID());
        siteCacheByName.remove(site.getServerName());
        siteCacheByKey.remove(site.getSiteKey());
    }

    private void updateWorkspacePermissions(JCRSessionWrapper session, String ws, JahiaSite site) throws RepositoryException {
        Node n = session.getNode("/permissions/repository-permissions/jcr:all_" + ws + "/jcr:write_" + ws + "/jcr:modifyProperties_" + ws);
        Set<String> languages = new HashSet<String>();

        NodeIterator ni = n.getNodes();
        while (ni.hasNext()) {
            Node next = (Node) ni.next();
            if (next.getName().startsWith("jcr:modifyProperties_" + ws + "_")) {
                languages.add(StringUtils.substringAfter(next.getName(), "jcr:modifyProperties_" + ws + "_"));
            }
        }
        for (String s : site.getLanguages()) {
            if (!languages.contains(s)) {
                n.addNode("jcr:modifyProperties_" + ws + "_" + s, "jnt:permission");
            }
        }
    }

    private void updateTranslatorRoles(JCRSessionWrapper session, JahiaSite site) throws RepositoryException {
        Node n = session.getNode("/roles");
        Set<String> languages = new HashSet<String>();

        NodeIterator ni = n.getNodes();
        while (ni.hasNext()) {
            Node next = (Node) ni.next();
            if (next.getName().startsWith("translator-")) {
                languages.add(StringUtils.substringAfter(next.getName(), "translator-"));
            }
        }
        for (String s : site.getLanguages()) {
            if (!languages.contains(s)) {
                session.checkout(n);
                Node translator = n.addNode("translator-" + s, "jnt:role");
                List<Value> perms = new LinkedList<Value>();
                perms.add(session.getValueFactory().createValue(session.getNode("/permissions/editMode/editModeAccess"), true));
                perms.add(session.getValueFactory().createValue(session.getNode("/permissions/editMode/editSelector/sitemapSelector"), true));
                perms.add(session.getValueFactory().createValue(session.getNode("/permissions/repository-permissions/jcr:all_default/jcr:versionManagement_default"), true));
                perms.add(session.getValueFactory().createValue(session.getNode("/permissions/repository-permissions/jcr:all_default/jcr:write_default/jcr:modifyProperties_default/jcr:modifyProperties_default_" + s), true));
                for (NodeIterator iterator = session.getNode("/permissions/workflow-tasks").getNodes("start-*-review"); iterator.hasNext(); ) {
                    perms.add(session.getValueFactory().createValue(iterator.nextNode(), true));
                }
                Value[] values = perms.toArray(new Value[]{});

                translator.setProperty("j:permissions", values);
                translator.setProperty("j:roleGroup", "edit-role");
                translator.setProperty("j:privilegedAccess", true);
            }
        }

    }

    /**
     * Returns the the {@link JahiaSite} bean for the specified UUID or <code>null</code> if the corresponding site cannot be found.
     *
     * @param jcrSiteIdentifier the UUID of the site noe
     * @return JahiaSite the {@link JahiaSite} bean or <code>null</code> if the corresponding site cannot be found
     */
    public JahiaSite getSiteByIdenifier(final String jcrSiteIdentifier) throws JahiaException {
        // TODO add cache by UUID

        JahiaSite site = null;
        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getSite(session.getNodeByIdentifier(jcrSiteIdentifier));
                }
            });
        } catch (ItemNotFoundException e) {
            logger.warn("Cannot get site for UUID {}", jcrSiteIdentifier);
        } catch (RepositoryException e) {
            logger.error("Cannot get site for UUID " + jcrSiteIdentifier, e);
        }

        return site;
    }

    public boolean isSiteKeyValid(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }

        if (SYSTEM_SITE_KEY.equals(name)) {
            return false;
        }

        boolean valid = true;
        for (char toBeTested : name.toCharArray()) {
            if (AUTHORIZED_CHARS.indexOf(toBeTested) == -1) {
                valid = false;
                break;
            }
        }

        return valid;
    }

    public boolean isServerNameValid(String serverName) {
        return StringUtils.isNotEmpty(serverName) && !serverName.contains(" ") && !serverName.contains(":");
    }

}

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
//
//  JahiaSitesBaseService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.jcr.*;
import javax.jcr.query.Query;
import java.io.IOException;
import java.util.*;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public class JahiaSitesService extends JahiaService {

    private static final String DEFAULT_SITE_PROPERTY = "j:defaultSite";
    private static Logger logger = LoggerFactory.getLogger(JahiaSitesService.class);

    private static final String[] TRANSLATOR_NODES_PATTERN = new String[] {"translator-*"};

    public static final String SYSTEM_SITE_KEY = "systemsite";
    public static final String SITES_JCR_PATH = "/sites";

    protected JahiaGroupManagerService groupService;
    protected JCRSessionFactory sessionFactory;
    protected EhCacheProvider ehCacheProvider;

    private SelfPopulatingCache siteKeyByServerNameCache;
    private SelfPopulatingCache siteDefaultLanguageBySiteKey;
    private SelfPopulatingCache sitesListCache;
    private String validServerNameRegex;
    private String validSiteKeyCharacters;

    public synchronized void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Default constructor, creates a new <code>JahiaSitesService</code> instance.
     */
    private JahiaSitesService() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final JahiaSitesService INSTANCE = new JahiaSitesService();
    }

    /**
     * Retrieves the unique instance of this singleton class.
     *
     * @return the unique instance of this class
     */
    public static JahiaSitesService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * return the list of all sites names
     *
     * @return List<String> List of site names
     */
    @SuppressWarnings("unchecked")
    public List<String> getSitesNames() {
        return (List<String>) getSitesListCache().get("/").getObjectValue();
    }

    private SelfPopulatingCache getSitesListCache() {
        if (sitesListCache == null) {
            sitesListCache = ehCacheProvider.registerSelfPopulatingCache("org.jahia.sitesService.sitesListCache", new CacheEntryFactory() {

                @Override
                public Object createEntry(Object o) throws Exception {
                    List<String> sites = new ArrayList<String>();
                    for (JCRSiteNode jcrSiteNode : getSitesNodeList(sessionFactory.getCurrentSystemSession(Constants.LIVE_WORKSPACE, null, null))) {
                        sites.add(jcrSiteNode.getName());
                    }
                    return sites;
                }
            });
        }
        return sitesListCache;
    }


    /**
     * return the list of all sites
     *
     * @return List<JCRSiteNode> List of JCRSiteNode
     */
    public List<JCRSiteNode> getSitesNodeList() throws RepositoryException {
        return getSitesNodeList(getUserSession());
    }

    public List<JCRSiteNode> getSitesNodeList(JCRSessionWrapper session) throws RepositoryException {
        final List<JCRSiteNode> list = new ArrayList<JCRSiteNode>();
        NodeIterator ni = session.getNode(SITES_JCR_PATH).getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
            if (nodeWrapper.isNodeType("jnt:virtualsite")) {
                JCRSiteNode siteNode = (JCRSiteNode) nodeWrapper;
                list.add(siteNode);
            }
        }
        return list;
    }

    /**
     * Returns first found site node under <code>/sites</code> considering the list of sites to be skipped.
     *
     * @param session
     *            current JCR session
     * @return first found node under <code>/sites</code> considering the list of sites to be skipped
     * @param skippedSites
     *            an array of site keys for sites to be skipped
     * @throws RepositoryException
     *             in case of an error
     */
    public JCRSiteNode getFirstSiteFound(JCRSessionWrapper session, String... skippedSites) throws RepositoryException {
        NodeIterator ni = session.getNode(SITES_JCR_PATH).getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper siteNode = (JCRNodeWrapper) ni.next();
            if (siteNode.isNodeType(Constants.JAHIANT_VIRTUALSITE) && !ArrayUtils.contains(skippedSites, siteNode.getName())) {
                return (JCRSiteNode) siteNode;
            }
        }
        return null;
    }

    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() {
        // do nothing
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

        JahiaSite site = null;
        try {
            site = getSiteByKey(siteKey, getUserSession());
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return site;
    }

    private JCRSessionWrapper getUserSession() throws RepositoryException {
        return sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE);
    }

    public JCRSiteNode getSiteByKey(String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return (JCRSiteNode) session.getNode("/sites/" + siteKey);
    }

    public boolean siteExists(String siteKey, JCRSessionWrapper session) throws RepositoryException {
        return StringUtils.isNotEmpty(siteKey) && session.nodeExists("/sites/" + siteKey);
    }

    /**
     * Find a site by it's server name value
     *
     * @param serverName the server name to look for
     * @return if found, returns the site with the corresponding serverName, or
     * the first one if there are multiple, or null if there are none.
     * @throws JahiaException thrown if there was a problem communicating with the
     *                        database.
     */
    public JahiaSite getSiteByServerName(final String serverName)
            throws JahiaException {

        if (serverName == null) {
            return null;
        }

        JahiaSite site = null;
        try {
            site = getSiteByServerName(serverName, getUserSession());
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }

        return site;
    }

    public JCRSiteNode getSiteByServerName(String serverName, JCRSessionWrapper session) throws RepositoryException {
        String s = JCRContentUtils.sqlEncode(StringUtils.lowerCase(serverName));
        Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where (lower(s.[j:serverName])='" + s + "' or lower(s.[j:serverNameAliases])='" + s + "')  and ischildnode(s, '/sites/')", Query.JCR_SQL2);
        NodeIterator ni = q.execute().getNodes();
        if (ni.hasNext()) {
            return (JCRSiteNode) ni.next();
        }
        return null;
    }

    public String getSitenameByServerName(final String serverName) throws JahiaException {
        if (serverName == null) {
            return null;
        }
        String siteName = getSiteKeyByServerNameCache().get(serverName).getObjectValue().toString();
        return "".equals(siteName) ? null : siteName;
    }

    private SelfPopulatingCache getSiteKeyByServerNameCache() {
        if (siteKeyByServerNameCache == null) {
            siteKeyByServerNameCache = ehCacheProvider.registerSelfPopulatingCache("org.jahia.sitesService.siteKeyByServerNameCache", new SiteKeyByServerNameCacheEntryFactory());
        }
        return siteKeyByServerNameCache;
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


    /**
     * @deprecated since 7.2.2.0. Use the {@link #addSite(SiteCreationInfo)} instead
     */
    @Deprecated
    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, String firstImport, Resource fileImport, String fileImportName,
                             /* not used now */ Boolean asAJob, /* not used now */ Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException {
        return addSite(currentUser, title, serverName, siteKey, descr, selectedLocale, selectTmplSet, null, firstImport, fileImport, fileImportName, asAJob, doImportServerPermissions, originatingJahiaRelease);
    }

    /**
     * @deprecated since 7.2.2.0. Use the {@link #addSite(SiteCreationInfo)} instead
     */
    @Deprecated
    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, final String[] modulesToDeploy, String firstImport, Resource fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException {
        return addSite(SiteCreationInfo.builder().
                siteKey(siteKey).
                serverName(serverName).
                title(title).
                description(descr).
                templateSet(selectTmplSet).
                modulesToDeploy(modulesToDeploy).
                locale(selectedLocale != null ? selectedLocale.toString() : null).
                siteAdmin(currentUser).
                firstImport(firstImport).
                fileImport(fileImport).
                fileImportName(fileImportName).
                originatingJahiaRelease(originatingJahiaRelease).build());
    }

    /**
     * @deprecated since 7.2.2.0. Use the {@link #addSite(SiteCreationInfo)} instead
     */
    @Deprecated
    public JahiaSite addSite(final JahiaUser currentUser, final String title, final String serverName, final String siteKey, final String descr,
                             final Locale selectedLocale, final String selectTmplSet, final String[] modulesToDeploy, final String firstImport, final Resource fileImport, final String fileImportName,
                             final Boolean asAJob, final Boolean doImportServerPermissions, final String originatingJahiaRelease, final Resource legacyMappingFilePath, final Resource legacyDefinitionsFilePath) throws JahiaException, IOException {

        return addSite(SiteCreationInfo.builder().
                siteKey(siteKey).
                serverName(serverName).
                title(title).
                description(descr).
                templateSet(selectTmplSet).
                modulesToDeploy(modulesToDeploy).
                locale(selectedLocale != null ? selectedLocale.toString() : null).
                siteAdmin(currentUser).
                firstImport(firstImport).
                fileImport(fileImport).
                fileImportName(fileImportName).
                originatingJahiaRelease(originatingJahiaRelease).
                legacyMappingFilePath(legacyMappingFilePath).
                legacyDefinitionsFilePath(legacyDefinitionsFilePath).build());
    }

    /**
     * @deprecated since 7.2.2.0. Use the {@link #addSite(SiteCreationInfo, JCRSessionWrapper)} instead
     */
    @Deprecated
    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, final String[] modulesToDeploy, String firstImport, Resource fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath, JCRSessionWrapper session) throws JahiaException, IOException {

        return addSite(SiteCreationInfo.builder().
                        siteKey(siteKey).
                        serverName(serverName).
                        title(title).
                        description(descr).
                        templateSet(selectTmplSet).
                        modulesToDeploy(modulesToDeploy).
                        locale(selectedLocale != null ? selectedLocale.toString() : null).
                        siteAdmin(currentUser).
                        firstImport(firstImport).
                        fileImport(fileImport).
                        fileImportName(fileImportName).
                        originatingJahiaRelease(originatingJahiaRelease).
                        legacyMappingFilePath(legacyMappingFilePath).
                        legacyDefinitionsFilePath(legacyDefinitionsFilePath).build(),
                session);
    }

    /**
     * Creates the site using the specified data.
     * 
     * @param info the site creation information
     * @return the created site object
     * @throws JahiaException in case of site creation issues
     * @throws IOException in case of I/O errors
     */
    public JahiaSite addSite(final SiteCreationInfo info) throws JahiaException, IOException {
        JahiaSite site = null;
        final List<Exception> errors = new ArrayList<Exception>(1);

        try {

            site = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {

                @Override
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        session.getPathMapping().putAll(JCRSessionFactory.getInstance().getCurrentUserSession().getPathMapping());
                        return addSite(info, session);
                    } catch (IOException e) {
                        errors.add(e);
                    } catch (JahiaException e) {
                        errors.add(e);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            throw new JahiaException("", "", 0, 0, e);
        }

        if (!errors.isEmpty()) {
            Exception e = errors.get(0);
            if (e instanceof JahiaException) {
                throw (JahiaException) e;
            } else if (e instanceof IOException) {
                throw (IOException) e;
            } else {
                // ?
                throw new JahiaException("", "", 0, 0, e);
            }
        }

        return site;
    }

    /**
     * Creates the site using the specified data.
     * 
     * @param info the site creation information
     * @param session the current JCR session to use for site creation
     * @return the created site object
     * @throws JahiaException in case of site creation issues
     * @throws IOException in case of I/O errors
     */
    public JahiaSite addSite(SiteCreationInfo info, JCRSessionWrapper session) throws JahiaException, IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Start creation of a site using data: {}", info);
        
        String siteKey = info.getSiteKey();
        
        if (!isSiteKeyValid(siteKey, true)) {
            String msg = "Site key is not valid. Allowed characters are: " + validSiteKeyCharacters;
            throw new JahiaException(msg, msg, JahiaException.DATA_ERROR, JahiaException.ERROR_SEVERITY);
        }

        // check there is no site with same server name before adding
        boolean importingSystemSite = false;
        final JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        JCRSiteNode site = null;

        try {

            if (!siteExists(siteKey, session)) {

                final JahiaTemplatesPackage templateSet = templateService.getAnyDeployedTemplatePackage(info.getTemplateSet());
                final String templatePackage = templateSet.getId();

                JCRNodeWrapper sitesFolder = session.getNode("/sites");

                try {
                    sitesFolder.getNode(siteKey);
                    throw new IOException("site already exists");
                } catch (PathNotFoundException e) {

                    JCRNodeWrapper siteNode = sitesFolder.addNode(siteKey, "jnt:virtualsite");

                    if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                        String skeletons = sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString();
                        try {
                            JCRContentUtils.importSkeletons(skeletons, sitesFolder.getPath() + "/" + siteKey, session);
                        } catch (Exception importEx) {
                            logger.error("Unable to import data using site skeleton " + skeletons, importEx);
                        }
                    }

                    siteNode.setProperty(Constants.TITLE, info.getTitle());
                    siteNode.setProperty(Constants.DESCRIPTION, info.getDescription());
                    siteNode.setProperty(SitesSettings.SERVER_NAME, info.getServerName());
                    siteNode.setProperty(SitesSettings.SERVER_NAME_ALIASES, info.getServerNameAliases());
                    siteNode.setProperty(SitesSettings.DEFAULT_LANGUAGE, info.getLocale());
                    siteNode.setProperty(SitesSettings.MIX_LANGUAGES_ACTIVE, false);
                    siteNode.setProperty(SitesSettings.LANGUAGES, new String[] { info.getLocale() });
                    siteNode.setProperty(SitesSettings.INACTIVE_LIVE_LANGUAGES, new String[] {});
                    siteNode.setProperty(SitesSettings.INACTIVE_LANGUAGES, new String[] {});
                    siteNode.setProperty(SitesSettings.MANDATORY_LANGUAGES, new String[] {});
                    siteNode.setProperty(SitesSettings.TEMPLATES_SET, templatePackage);

                    siteNode.setProperty(SitesSettings.INSTALLED_MODULES, new Value[]{session.getValueFactory().createValue(templatePackage /*+ ":" + aPackage.getLastVersion()*/)});

                    String target = getTargetString(siteKey);

                    deployModules(target, info.getModulesToDeploy(), templateSet, session, templateService);

                    //Auto deploy all modules that define this behavior on site creation
                    final List<JahiaTemplatesPackage> availableTemplatePackages = templateService.getAvailableTemplatePackages();
                    for (JahiaTemplatesPackage availableTemplatePackage : availableTemplatePackages) {
                        String autoDeployOnSite = availableTemplatePackage.getAutoDeployOnSite();
                        if (autoDeployOnSite != null
                                && ("all".equals(autoDeployOnSite) || siteKey.equals(autoDeployOnSite) || (siteKey.equals("systemsite") && autoDeployOnSite.equals("system")))) {
                            String source = "/modules/" + availableTemplatePackage.getId();
                            try {
                                logger.info("Deploying module {} to {}", source, target);
                                templateService.installModule(availableTemplatePackage, target, session);
                            } catch (RepositoryException re) {
                                logger.error("Unable to deploy module " + source + " to " + target + ". Cause: "
                                        + re.getMessage(), re);
                            }
                        }
                    }
                    site = (JCRSiteNode) siteNode;
                }

                session.save();
            } else if (siteKey.equals(SYSTEM_SITE_KEY)) {
                site = (JCRSiteNode) getSiteByKey(SYSTEM_SITE_KEY);
                importingSystemSite = true;
            } else {
                throw new IOException("site already exists");
            }

            JCRSiteNode siteNode = (JCRSiteNode) session.getNode(site.getPath());

            // continue if the site is added correctly...
            if (!site.isDefault() && !site.getSiteKey().equals(SYSTEM_SITE_KEY) && getNbSites() == 2) {
                setDefaultSite(site, session);
            }

            if (!importingSystemSite) {

                JahiaGroupManagerService jgms = ServicesRegistry.getInstance().getJahiaGroupManagerService();

                siteNode.setMixLanguagesActive(false);
                session.save();

                JCRGroupNode privGroup = jgms.lookupGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME, session);
                if (privGroup == null) {
                    privGroup = jgms.createGroup(null, JahiaGroupManagerService.PRIVILEGED_GROUPNAME, null, true, session);
                }

                JCRGroupNode adminGroup = jgms.lookupGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, session);
                if (adminGroup == null) {
                    adminGroup = jgms.createGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, null, false, session);
                }

                // attach superadmin user (current) to administrators group...
                if (info.getSiteAdmin() != null) {
                    adminGroup.addMember(session.getNode(info.getSiteAdmin().getLocalPath()));
                }

                JCRGroupNode sitePrivGroup = jgms.lookupGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, session);
                if (sitePrivGroup == null) {
                    sitePrivGroup = jgms.createGroup(site.getSiteKey(), JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, null, false, session);
                }
                // atach site privileged group to server privileged
                privGroup.addMember(sitePrivGroup);

                if (!siteKey.equals(SYSTEM_SITE_KEY)) {
                    siteNode.grantRoles("g:" + JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, Collections.singleton("privileged"));
                    siteNode.denyRoles("g:" + JahiaGroupManagerService.PRIVILEGED_GROUPNAME, Collections.singleton("privileged"));
                }
                siteNode.grantRoles("g:" + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, Collections.singleton("site-administrator"));
                session.save();
            }

            Resource initialZip = null;
            if ("fileImport".equals(info.getFirstImport())) {
                initialZip = info.getFileImport();
            }

            if ("importRepositoryFile".equals(info.getFirstImport()) || (initialZip != null && initialZip.exists() && !"noImport".equals(info.getFirstImport()))) {
                try {
                    Map<Object, Object> importInfos = new HashMap<Object, Object>();
                    importInfos.put("originatingJahiaRelease", info.getOriginatingJahiaRelease());
                    ServicesRegistry.getInstance().getImportExportService().importSiteZip(initialZip, site, importInfos, info.getLegacyMappingFilePath(), info.getLegacyDefinitionsFilePath(), session);
                } catch (RepositoryException e) {
                    logger.warn("Error importing site ZIP", e);
                }
            }

            logger.debug("Site updated with Home Page");
        } catch (RepositoryException e) {
            logger.warn("Error adding home node", e);
        } finally {
            logger.info("Done creation of the site {} in {} ms", info.getSiteKey(),
                    System.currentTimeMillis() - startTime);
        }

        return site;
    }
    
    private String getTargetString(String siteKey) {
        return "/sites/" + siteKey;
    }

    public void deployModules(JahiaSite site, String[] modulesToDeploy, JCRSessionWrapper session) {
        final JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        deployModules(getTargetString(site.getSiteKey()), modulesToDeploy, templateService.getAnyDeployedTemplatePackage(site.getTemplatePackageName()), session, templateService);
    }

    public void updateModules(JahiaSite site, List<String> newModuleIds, JCRSessionWrapper session) {

        final JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        final List<String> installedModules = site.getInstalledModules();

        // compute list of modules to uninstall
        List<String> modulesToUninstall = new LinkedList<String>(installedModules);

        // first remove all modules that are in common with the new modules list and still exist
        for (String newModuleId : newModuleIds) {
            if (templateService.getAnyDeployedTemplatePackage(newModuleId) != null) {
                modulesToUninstall.remove(newModuleId);
            }
        }

        // remove default and template set so that they don't get un-installed
        modulesToUninstall.remove(JahiaTemplatesPackage.ID_DEFAULT);
        modulesToUninstall.remove(((JCRSiteNode) site).getTemplatePackage().getId());

        String siteKey = site.getSiteKey();
        for (JahiaTemplatesPackage availableTemplatePackage : templateService.getAvailableTemplatePackages()) {
            String autoDeployOnSite = availableTemplatePackage.getAutoDeployOnSite();
            if (autoDeployOnSite != null
                && ("all".equals(autoDeployOnSite) || siteKey.equals(autoDeployOnSite))) {
                modulesToUninstall.remove(availableTemplatePackage.getId());
            }
        }

        // un-install modules if there are any
        if (!modulesToUninstall.isEmpty()) {
            Set<String> moduleIds = new LinkedHashSet<>();
            for (String moduleToUninstall : modulesToUninstall) {
                JahiaTemplatesPackage pkg = templateService.getAnyDeployedTemplatePackage(moduleToUninstall);
                moduleIds.add(pkg != null ? pkg.getId() : moduleToUninstall);
            }
            List<String> ids = new ArrayList<>(moduleIds);
            logger.info("Uninstalling modules {} from {}", ids.toArray(), siteKey);
            try {
                templateService.uninstallModulesByIds(ids, site.getJCRLocalPath(), session);
            } catch (RepositoryException re) {
                logger.error("Unable to uninstall modules " + ids.toArray() + " from "
                        + siteKey + ". Cause: " + re.getMessage(), re);
            }
        }

        // compute list of new modules to install
        List<String> modulesToInstall = new LinkedList<String>(newModuleIds);
        // first remove the modules that are in common with the installed modules list
        modulesToInstall.removeAll(installedModules);

        // install new modules if there are any
        if (!modulesToInstall.isEmpty()) {
            List<JahiaTemplatesPackage> toInstallModules = moduleIdsToTemplatesPackage(modulesToInstall, templateService);
            // install new modules
            try {
                logger.info("Installing modules " + toInstallModules + " to " + siteKey);
                templateService.installModules(toInstallModules, site.getJCRLocalPath(), session);
            } catch (RepositoryException re) {
                logger.error("Unable to install modules " + toInstallModules + " to "
                        + siteKey + ". Cause: " + re.getMessage(), re);
            }
        }
    }

    private void deployModules(String target, String[] modulesToDeploy, JahiaTemplatesPackage templateSet, JCRSessionWrapper session, JahiaTemplateManagerService templateService) {
        List<JahiaTemplatesPackage> modules = moduleIdsToTemplatesPackage(modulesToDeploy != null ? Arrays.asList(modulesToDeploy) : Collections.<String>emptyList(), templateService);
        modules.add(templateService.getAnyDeployedTemplatePackage(JahiaTemplatesPackage.ID_DEFAULT));
        modules.add(templateSet);
        try {
            logger.info("Deploying modules {} to {}", modules.toString(), target);
            templateService.installModules(modules, target, session);
        } catch (RepositoryException re) {
            logger.error("Unable to deploy modules " + modules.toString() + " to "
                    + target + ". Cause: " + re.getMessage(), re);
        }
    }

    private List<JahiaTemplatesPackage> moduleIdsToTemplatesPackage(List<String> modules, JahiaTemplateManagerService templateService) {
        if (modules != null) {
            List<JahiaTemplatesPackage> packages = new ArrayList<JahiaTemplatesPackage>(2 + modules.size());
            for (String s : modules) {
                JahiaTemplatesPackage packageByFileName = templateService.getAnyDeployedTemplatePackage(s);
                if (!packages.contains(packageByFileName)) {
                    packages.add(packageByFileName);
                }
            }
            return packages;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Remove a site; if the removed site is the default one:
     * - the first other site found will become the default
     * - if no other site found, no default site anymore
     *
     * @param siteToRemove the JahiaSite bean to be removed
     */
    public synchronized void removeSite(final JahiaSite siteToRemove) throws JahiaException {

        final String siteKeyToRemove = siteToRemove.getSiteKey();
        List<String> serverNames = siteToRemove.getAllServerNames();

        try {
            // First delete all groups
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {

                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    for (String groupPath : groupService.getGroupList(siteKeyToRemove)) {
                        if (StringUtils.startsWith(groupPath, siteToRemove.getJCRLocalPath() + "/groups/")
                                && !StringUtils.startsWith(groupPath, siteToRemove.getJCRLocalPath() + "/groups/providers/")) {
                            groupService.deleteGroup(groupPath, session);
                        }
                    }
                    session.save();
                    return true;
                }
            });

            // Now let's delete the site.
            JCRCallback<Boolean> deleteCallback = new JCRCallback<Boolean>() {

                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode(SITES_JCR_PATH);
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper siteNode = sites.getNode(siteKeyToRemove);
                    // check if we need to update the default site
                    if (Constants.EDIT_WORKSPACE.equals(session.getWorkspace().getName()) && StringUtils
                            .equals(sites.getPropertyAsString(DEFAULT_SITE_PROPERTY), siteNode.getIdentifier())) {
                        // our site is the default one: we search for a next candidate for the default site
                        // if there is no one found the "j:defaultSite" is removed from the /sites node
                        sites.setProperty(DEFAULT_SITE_PROPERTY,
                                getFirstSiteFound(session, SYSTEM_SITE_KEY, siteKeyToRemove));
                    }
                    siteNode.remove();
                    session.save();
                    return true;
                }
            };

            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.LIVE_WORKSPACE, null, deleteCallback);
            JCRTemplate.getInstance().doExecuteWithSystemSession(deleteCallback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } finally {
            invalidateCache(siteKeyToRemove, serverNames);
        }
    }

    /**
     * Update a JahiaSite definition
     *
     * @param site the site bean object
     */
    public synchronized void updateSystemSitePermissions(final JahiaSite site) throws JahiaException {

        try {

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    updateSystemSitePermissions(site, session);
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void updateSystemSitePermissions(JahiaSite site, JCRSessionWrapper session) throws RepositoryException {

        JCRNodeWrapper sites = session.getNode(SITES_JCR_PATH);
        if (!sites.isCheckedOut()) {
            session.checkout(sites);
        }
        JCRSiteNode siteNode = (JCRSiteNode) site;
        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }

        if (siteNode.getName().equals(SYSTEM_SITE_KEY)) {
            boolean update = updateWorkspacePermissions(session, "default", site);
            update |= updateWorkspacePermissions(session, "live", site);
            if (update) {
                JahiaPrivilegeRegistry.init(session);
            }
            updateTranslatorRoles(session, site);
        }
    }

    public int getNbSites() throws JahiaException {
        try {
            return getSitesNodeList().size();
        } catch (RepositoryException e) {
            return 0;
        }
    }

    public JahiaSite getDefaultSite() {
        JahiaSite site = null;
        try {
            site = getDefaultSite(getUserSession());
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return site;
    }

    public JahiaSite getDefaultSite(JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(SITES_JCR_PATH);
        if (node.hasProperty(DEFAULT_SITE_PROPERTY)) {
            try {
                return (JCRSiteNode) node.getProperty(DEFAULT_SITE_PROPERTY).getNode();
            } catch (RepositoryException e) {
                List<JCRSiteNode> sitesNodeList = getSitesNodeList(session);
                for (JCRSiteNode jcrSiteNode : sitesNodeList) {
                    if (!"systemsite".equals(jcrSiteNode.getSiteKey())) {
                        return jcrSiteNode;
                    }
                }
            }
        }
        return null;
    }

    public void setDefaultSite(final JahiaSite site) {

        try {

            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JahiaSitesService.this.setDefaultSite(site, session);
                    session.save();
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot set default site", e);
        }
    }

    public void setDefaultSite(JahiaSite site, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper node = session.getNode(SITES_JCR_PATH);
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        if (site != null) {
            JCRNodeWrapper s = node.getNode(site.getSiteKey());
            node.setProperty(DEFAULT_SITE_PROPERTY, s);
        } else if (node.hasProperty(DEFAULT_SITE_PROPERTY)) {
            node.getProperty(DEFAULT_SITE_PROPERTY).remove();
        }
    }

    /**
     * Invalidates the cache for the specified site.
     *
     * @param site the site bean object
     */
    public void invalidateCache(JahiaSite site) throws JahiaException {
        if (site == null) {
            return;
        }
        invalidateCache(site.getSiteKey(), site.getAllServerNames());
    }

    public void invalidateCache(String siteKey, String serverName) {
        invalidateCache(siteKey, serverName != null ? Collections.singletonList(serverName) : null);
    }

    /**
     * Invalidates the cache for the specified site.
     *
     * @param siteKey
     *            the key of the site
     * @param serverNames
     *            the server names of the site
     */
    public void invalidateCache(String siteKey, List<String> serverNames) {
        if (siteDefaultLanguageBySiteKey != null && siteKey != null) {
            siteDefaultLanguageBySiteKey.remove(siteKey);
        }

        if (siteKeyByServerNameCache != null && serverNames != null) {
            for (String serverName : serverNames) {
                siteKeyByServerNameCache.remove(serverName);
            }
        }
    }

    private boolean updateWorkspacePermissions(JCRSessionWrapper session, String ws, JahiaSite site) throws RepositoryException {

        Node n = session.getNode("/permissions/repository-permissions/jcr:all_" + ws + "/jcr:write_" + ws + "/jcr:modifyProperties_" + ws);
        Set<String> languages = new HashSet<String>();

        NodeIterator ni = n.getNodes(new String[]{"jcr:modifyProperties_" + ws + "_*"});
        while (ni.hasNext()) {
            Node next = (Node) ni.next();
            languages.add(StringUtils.substringAfter(next.getName(), "jcr:modifyProperties_" + ws + "_"));
        }
        boolean updated = false;
        for (String s : site.getLanguages()) {
            if (!languages.contains(s)) {
                n.addNode("jcr:modifyProperties_" + ws + "_" + s, "jnt:permission");
                updated = true;
            }
        }
        return updated;
    }

    private void updateTranslatorRoles(JCRSessionWrapper session, JahiaSite site) throws RepositoryException {

        if (!session.itemExists("/roles/translator")) {
            return;
        }
        Node n = session.getNode("/roles/translator");
        Set<String> languages = new HashSet<String>();

        NodeIterator ni = n.getNodes(TRANSLATOR_NODES_PATTERN);
        while (ni.hasNext()) {
            Node next = (Node) ni.next();
            languages.add(StringUtils.substringAfter(next.getName(), "translator-"));
        }
        for (String s : site.getLanguages()) {
            if (!languages.contains(s)) {
                session.checkout(n);
                Node translator = n.addNode("translator-" + s, "jnt:role");
                List<Value> perms = new LinkedList<Value>();
                perms.add(session.getValueFactory().createValue("jcr:modifyProperties_default_" + s));
                Value[] values = perms.toArray(new Value[perms.size()]);

                translator.setProperty("j:permissionNames", values);
                translator.setProperty("j:roleGroup", "edit-role");
                translator.setProperty("j:privilegedAccess", true);
            }
        }

    }

    public boolean isSiteKeyValid(String siteKey) {
        return isSiteKeyValid(siteKey, false);
    }

    private boolean isSiteKeyValid(String siteKey, boolean allowSysstemSite) {
        if (StringUtils.isEmpty(siteKey) || !allowSysstemSite && SYSTEM_SITE_KEY.equals(siteKey)) {
            return false;
        }

        boolean valid = true;
        if (validSiteKeyCharacters != null && validSiteKeyCharacters.length() > 0) {
            for (char toBeTested : siteKey.toCharArray()) {
                if (validSiteKeyCharacters.indexOf(toBeTested) == -1) {
                    valid = false;
                    break;
                }
            }
        }

        return valid;
    }

    public boolean isServerNameValid(String serverName) {
        return StringUtils.isNotEmpty(serverName) && !serverName.contains(" ") && serverName.matches(validServerNameRegex);
    }

    public boolean updateSystemSiteLanguages(JahiaSite site, JCRSessionWrapper session) {
        if (!site.getSiteKey().equals(SYSTEM_SITE_KEY)) {
            try {
                JahiaSite jahiaSite = getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY, session);
                // update the system site only if it does not yet contain at least one of the site languages
                Set<String> jahiaSiteLanguages = new HashSet<String>(jahiaSite.getLanguages());

                Set<String> siteLanguages = new HashSet<>(site.getLanguages());
                siteLanguages.addAll(site.getInactiveLanguages());

                if (!jahiaSiteLanguages.containsAll(siteLanguages)) {
                    jahiaSiteLanguages.addAll(siteLanguages);
                    jahiaSite.setLanguages(jahiaSiteLanguages);
                    return true;
                }
            } catch (PathNotFoundException e) {
                logger.debug(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    public String getSiteDefaultLanguage(String siteKey) throws JahiaException {
        if (siteKey == null) {
            return null;
        }
        return (String) getSiteDefaultLanguageBySiteKeyCache().get(siteKey).getObjectValue();
    }

    private SelfPopulatingCache getSiteDefaultLanguageBySiteKeyCache() {
        if (siteDefaultLanguageBySiteKey == null) {
            siteDefaultLanguageBySiteKey = ehCacheProvider.registerSelfPopulatingCache("org.jahia.sitesService.siteDefaultLanguageBySiteKey", new SiteDefaultLanguageBySiteKeyCacheEntryFactory());
        }
        return siteDefaultLanguageBySiteKey;
    }

    /**
     * Flush the sites internal caches ( site key by server name & default language by site key).
     */
    public static void flushSitesInternalCaches() {
        getInstance().flushCaches();
    }

    private void flushCaches() {
        if (siteKeyByServerNameCache != null) {
            siteKeyByServerNameCache.removeAll(false);
        }
        if (siteDefaultLanguageBySiteKey != null) {
            siteDefaultLanguageBySiteKey.removeAll(false);
        }
        if (sitesListCache != null) {
            sitesListCache.removeAll();
        }
    }

    public void setEhCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    /**
     * Factory to fill the site key by server name cache.
     */
    public class SiteKeyByServerNameCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            JCRSiteNode s = getSiteByServerName((String) key, session);
            if (s != null) {
                return s.getSiteKey();
            } else {
                return "";
            }
        }
    }

    public class SiteDefaultLanguageBySiteKeyCacheEntryFactory implements CacheEntryFactory {

        @Override
        public Object createEntry(final Object key) throws Exception {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            try {
                JCRSiteNode s = StringUtils.isEmpty((String) key) ? null : getSiteByKey((String) key, session);
                if (s != null) {
                    return s.getDefaultLanguage();
                }
            } catch (PathNotFoundException e) {
                // site not found .. do nothing
            } catch (RepositoryException e) {
                // other errors
                logger.error("cannot get site", e);
            }
            return "";
        }
    }

    public void setValidServerNameRegex(String validServerNameRegex) {
        this.validServerNameRegex = validServerNameRegex;
    }

    public String getValidSiteKeyCharacters() {
        return validSiteKeyCharacters;
    }

    public void setValidSiteKeyCharacters(String validSiteKeyCharacters) {
        this.validSiteKeyCharacters = validSiteKeyCharacters;
    }
}

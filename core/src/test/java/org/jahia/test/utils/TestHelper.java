/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.utils;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SitesSettings;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.CollectionUtils;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.VersionException;

import java.io.*;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * 
 */
public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestHelper.class);
    public static final String TCK_TEMPLATES = "Jahia Test";
    public static final String WEB_TEMPLATES = "templates-web";
    public static final String WEB_BLUE_TEMPLATES = "templates-web-blue";
    public static final String WEB_SPACE_TEMPLATES = "templates-web-space";
    public static final String INTRANET_TEMPLATES = "templates-intranet";
    public static final String BOOTSTRAP_ACME_SPACE_TEMPLATES = "bootstrap-acme-space-templates";

    public static JahiaSite createSite(String name) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), getDefaultTemplateSet(), null, null, null);
    }

    public static JahiaSite createSite(String name, String templateSet) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), templateSet, null, null, null);
    }

    public static JahiaSite createSite(String name, Set<String> languages, Set<String> mandatoryLanguages, boolean mixLanguagesActive) throws Exception {
        createSite(name, "localhost" + System.currentTimeMillis(), getDefaultTemplateSet(), null, null, null);
        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRSiteNode site = (JCRSiteNode) session.getNode("/sites/" + name);
        if (!CollectionUtils.isEmpty(languages) && !languages.equals(site.getLanguages())) {
            site.setLanguages(languages);
        }
        if (!CollectionUtils.isEmpty(mandatoryLanguages) && !mandatoryLanguages.equals(site.getMandatoryLanguages())) {
            site.setMandatoryLanguages(mandatoryLanguages);
        }
        if (mixLanguagesActive != site.isMixLanguagesActive()) {
            site.setMixLanguagesActive(mixLanguagesActive);
        }
        session.save();
        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet, String[] modulesToDeploy) throws Exception {
        return createSite(name, serverName, templateSet, null, null,modulesToDeploy);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet) throws Exception {
        return createSite(name, serverName, templateSet, null, null,null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName, String[] modulesToDeploy) throws Exception {
        modulesToDeploy = (modulesToDeploy == null) ? new String[0] : modulesToDeploy;

        JahiaUser admin = JahiaAdminUser.getAdminUser(null);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);

        if (site != null) {
            service.removeSite(site);
        }
        File siteZIPFile = null;
        File sharedZIPFile = null;
        try {
            if (!StringUtils.isEmpty(prepackedZIPFile)) {
                ZipInputStream zis = null;
                OutputStream os = null;
                try {
                    zis = new ZipInputStream(new FileInputStream(new File(prepackedZIPFile)));
                    ZipEntry z = null;
                    while ((z = zis.getNextEntry()) != null) {
                        if (siteZIPName.equalsIgnoreCase(z.getName())
                                || "users.zip".equals(z.getName())) {
                            File zipFile = File.createTempFile("import", ".zip");
                            os = new FileOutputStream(zipFile);
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = zis.read(buf)) > 0) {
                                os.write(buf, 0, r);
                            }
                            os.close();
                            if ("users.zip".equals(z.getName())) {
                                sharedZIPFile = zipFile;
                            } else {
                                siteZIPFile = zipFile;
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    if (zis != null) {
                        try {
                            zis.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (sharedZIPFile != null) {
                try {
                    ImportExportBaseService.getInstance().importSiteZip(sharedZIPFile != null ? new FileSystemResource(sharedZIPFile) : null, null, null);
                } catch (RepositoryException e) {
                    logger.warn("shared.zip could not be imported", e);
                }
            }
            if (templateSet != null) {
                site = service.addSite(admin, name, serverName, name, name,
                        SettingsBean.getInstance().getDefaultLocale(),
                        templateSet, modulesToDeploy,
                        siteZIPFile == null ? "noImport" : "fileImport",
                        siteZIPFile != null ? new FileSystemResource(
                                siteZIPFile) : null, null, false, false, null);
            } else {
                site = addSiteWithoutTemplates(admin, name, serverName, SettingsBean.getInstance().getDefaultLocale());
            }
            site = service.getSiteByKey(name);
        } finally {
            if (sharedZIPFile != null) {
                sharedZIPFile.delete();
            }
            if (siteZIPFile != null) {
                siteZIPFile.delete();
            }
        }

        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName) throws Exception {
            return createSite(name, serverName, templateSet, prepackedZIPFile, siteZIPName, null);
    }
    
    private static String getDefaultTemplateSet() {
        String defaultTemplatesSet = WEB_TEMPLATES;
        if (ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAnyDeployedTemplatePackage(WEB_TEMPLATES) == null) {
            defaultTemplatesSet = null;
        }
        return defaultTemplatesSet;
    }
    
    private static JahiaSite addSiteWithoutTemplates(
            final JahiaUser currentUser, final String name,
            final String serverName, final Locale selectedLocale)
            throws Exception {
        JahiaSite site = null;
        try {
            site = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<JahiaSite>() {
                        public JahiaSite doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            // check there is no site with same server name before adding
                            JCRSiteNode site = null;
                            try {

                                if (!JahiaSitesService.getInstance()
                                        .siteExists(name, session)) {
                                    Query q = session
                                            .getWorkspace()
                                            .getQueryManager()
                                            .createQuery(
                                                    "SELECT * FROM [jnt:virtualsitesFolder]",
                                                    Query.JCR_SQL2);
                                    QueryResult qr = q.execute();
                                    NodeIterator ni = qr.getNodes();

                                    while (ni.hasNext()) {
                                        JCRNodeWrapper sitesFolder = (JCRNodeWrapper) ni
                                                .nextNode();
                                        String options = "";
                                        if (sitesFolder
                                                .hasProperty("j:virtualsitesFolderConfig")) {
                                            options = sitesFolder
                                                    .getProperty(
                                                            "j:virtualsitesFolderConfig")
                                                    .getString();
                                        }

                                        JCRNodeWrapper f = JCRContentUtils
                                                .getPathFolder(sitesFolder,
                                                        name, options,
                                                        "jnt:virtualsitesFolder");
                                        try {
                                            f.getNode(name);
                                            throw new IllegalArgumentException(
                                                    "Site already exists");
                                        } catch (PathNotFoundException e) {
                                            JCRNodeWrapper siteNode = f
                                                    .addNode(name,
                                                            "jnt:virtualsite");

                                            if (sitesFolder
                                                    .hasProperty("j:virtualsitesFolderSkeleton")) {
                                                String skeletons = sitesFolder
                                                        .getProperty(
                                                                "j:virtualsitesFolderSkeleton")
                                                        .getString();
                                                try {
                                                    JCRContentUtils
                                                            .importSkeletons(
                                                                    skeletons,
                                                                    f.getPath()
                                                                            + "/"
                                                                            + name,
                                                                    session);
                                                } catch (Exception importEx) {
                                                    logger.error(
                                                            "Unable to import data using site skeleton "
                                                                    + skeletons,
                                                            importEx);
                                                }
                                            }

                                            siteNode.setProperty("j:title",
                                                    name);
                                            siteNode.setProperty(
                                                    "j:description", name);
                                            siteNode.setProperty(
                                                    "j:serverName", serverName);
                                            siteNode.setProperty(
                                                    SitesSettings.DEFAULT_LANGUAGE,
                                                    selectedLocale.toString());
                                            siteNode.setProperty(
                                                    SitesSettings.MIX_LANGUAGES_ACTIVE,
                                                    false);
                                            siteNode.setProperty(
                                                    SitesSettings.LANGUAGES,
                                                    new String[] { selectedLocale
                                                            .toString() });
                                            siteNode.setProperty(
                                                    SitesSettings.INACTIVE_LIVE_LANGUAGES,
                                                    new String[] {});
                                            siteNode.setProperty(
                                                    SitesSettings.INACTIVE_LANGUAGES,
                                                    new String[] {});
                                            siteNode.setProperty(
                                                    SitesSettings.MANDATORY_LANGUAGES,
                                                    new String[] {});

                                            site = (JCRSiteNode) siteNode;
                                        }
                                    }

                                    session.save();
                                } else {
                                    throw new IllegalArgumentException(
                                            "Site already exists");
                                }

                                JCRSiteNode siteNode = (JCRSiteNode) session
                                        .getNode(site.getPath());

                                // continue if the site is added correctly...
                                if (!site.isDefault()
                                        && !site.getSiteKey()
                                                .equals(JahiaSitesService.SYSTEM_SITE_KEY)
                                        && JahiaSitesService.getInstance()
                                                .getSitesNodeList().size() == 2) {
                                    JahiaSitesService.getInstance()
                                            .setDefaultSite(site, session);
                                }

                                JahiaGroupManagerService jgms = ServicesRegistry
                                        .getInstance()
                                        .getJahiaGroupManagerService();

                                siteNode.setMixLanguagesActive(false);
                                session.save();

                                JCRGroupNode privGroup = jgms
                                        .lookupGroup(
                                                null,
                                                JahiaGroupManagerService.PRIVILEGED_GROUPNAME,
                                                session);
                                if (privGroup == null) {
                                    privGroup = jgms
                                            .createGroup(
                                                    null,
                                                    JahiaGroupManagerService.PRIVILEGED_GROUPNAME,
                                                    null, true, session);
                                }

                                JCRGroupNode adminGroup = jgms.lookupGroup(
                                        site.getSiteKey(),
                                        JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME,
                                        session);
                                if (adminGroup == null) {
                                    adminGroup = jgms.createGroup(
                                            site.getSiteKey(),
                                            JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME,
                                            null, false, session);
                                }

                                // attach superadmin user (current) to administrators group...
                                if (currentUser != null) {
                                    adminGroup.addMember(session
                                            .getNode(currentUser.getLocalPath()));
                                }

                                JCRGroupNode sitePrivGroup = jgms.lookupGroup(
                                        site.getSiteKey(),
                                        JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME,
                                        session);
                                if (sitePrivGroup == null) {
                                    sitePrivGroup = jgms.createGroup(
                                            site.getSiteKey(),
                                            JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME,
                                            null, false, session);
                                }
                                // atach site privileged group to server privileged
                                privGroup.addMember(sitePrivGroup);

                                if (!name
                                        .equals(JahiaSitesService.SYSTEM_SITE_KEY)) {
                                    siteNode.grantRoles(
                                            "g:"
                                                    + JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME,
                                            Collections.singleton("privileged"));
                                    siteNode.denyRoles(
                                            "g:"
                                                    + JahiaGroupManagerService.PRIVILEGED_GROUPNAME,
                                            Collections.singleton("privileged"));
                                }
                                siteNode.grantRoles(
                                        "g:"
                                                + JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME,
                                        Collections
                                                .singleton("site-administrator"));
                                session.save();
                                
                                JCRNodeWrapper home = siteNode.addNode("home", "jnt:page");
                                home.setProperty("j:templateName", "home");
                                home.setProperty("j:isHomePage", true);
                                session.save();                                
                                logger.debug("Site updated with Home Page");
                            } catch (RepositoryException e) {
                                logger.warn("Error adding home node", e);
                            }

                            return site;

                        }
                    });
        } catch (RepositoryException e) {
            throw new JahiaException("", "", 0, 0, e);
        }
        return site;
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);
        if (site != null)
            service.removeSite(site);
    }

    public static int createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
       return createSubPages(currentNode, level, nbChildren, null);
    }
    
    public static int createSubPages(Node currentNode, int level, int nbChildren, String titlePrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        int pagesCreated = 0;
        if (!currentNode.isCheckedOut()) {
            currentNode.getSession().getWorkspace().getVersionManager().checkout(currentNode.getPath());
        }
        for (int i = 0; i < nbChildren; i++) {
            Node newSubPage = currentNode.addNode("child" + Integer.toString(i), "jnt:page");
            newSubPage.setProperty("j:templateName", "simple");
            if (titlePrefix != null) {
                newSubPage.setProperty("jcr:title",
                        titlePrefix + Integer.toString(i));
            }
            pagesCreated++;
        }
        return pagesCreated;
    }

    /**
     * Little utility method to easily create lists of content.
     *
     * @param parentNode
     * @param listName
     * @param elementCount
     * @param textPrefix
     * @throws RepositoryException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws NoSuchNodeTypeException
     * @throws ItemExistsException
     * @throws VersionException
     */
    public static JCRNodeWrapper createList(JCRNodeWrapper parentNode, String listName, int elementCount, String textPrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        JCRNodeWrapper contentList = parentNode.addNode(listName, "jnt:contentList");

        for (int i = 0; i < elementCount; i++) {
            JCRNodeWrapper textNode = contentList.addNode(listName + "_text" + Integer.toString(i), "jnt:mainContent");
            textNode.setProperty("jcr:title", textPrefix + Integer.toString(i));
            textNode.setProperty("body", textPrefix + Integer.toString(i));
        }
        return contentList;
    }

    /**
     * Utility method to dump a part of a content tree into a String.
     *
     * @param stringBuilder
     * @param startNode
     * @param depth         usually 0 when called initially, it is incremented to mark the current depth in the tree.
     * @param logAsError
     * @return
     * @throws RepositoryException
     */
    public static StringBuilder dumpTree(StringBuilder stringBuilder, Node startNode, int depth, boolean logAsError) throws RepositoryException {
        for (int i = 0; i < depth; i++) {
            if (i == 0) {
                stringBuilder.append("+-");
            } else {
                stringBuilder.append("--");
            }
        }
        stringBuilder.append(startNode.getName());
        stringBuilder.append(" = ");
        stringBuilder.append(startNode.getIdentifier());
        stringBuilder.append("\n");
        NodeIterator childNodeIter = startNode.getNodes();
        while (childNodeIter.hasNext()) {
            Node currentChild = childNodeIter.nextNode();
            stringBuilder = dumpTree(stringBuilder, currentChild, depth + 1, logAsError);
        }
        return stringBuilder;
    }


}

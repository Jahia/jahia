/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.osgi.BundleResource;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.zip.DirectoryZipInputStream;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.CollectionUtils;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
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
    public static final String BOOTSTRAP_ACME_SPACE_TEMPLATES = "bootstrap-acme-space-templates";
    public static final String DX_BASE_DEMO_TEMPLATES = "dx-base-demo-templates";

    private TestHelper() {
        throw new IllegalStateException("Utility class");
      }

    public static JahiaSite createSite(SiteCreationInfo info) throws JahiaException, IOException {
        return createSite(info, null, null);
    }

    public static JahiaSite createSite(SiteCreationInfo info, String prepackedZIPFile, String siteZIPName) throws JahiaException, IOException {
        populateDefaults(info);

        deleteSiteIfPresent(info.getSiteKey());

        JahiaSite site = null;
        File siteZIPFile = null;
        try {
            if (!StringUtils.isEmpty(prepackedZIPFile)) {
                String prepackedZipFileOrBundle = ModuleTestHelper.ensurePrepackagedSiteExist(prepackedZIPFile);
                siteZIPFile = importSharedAndGetSiteZipFileOfPrepackagedSite(prepackedZipFileOrBundle, siteZIPName);
                if (siteZIPFile != null) {
                    info.setFirstImport("fileImport");
                    info.setFileImport(new FileSystemResource(siteZIPFile));
                    List<String> modulesToInstall = readInstalledModules(siteZIPFile);
                    Collections.reverse(modulesToInstall);
                    for (String module : modulesToInstall) {
                        ModuleTestHelper.ensureModuleStarted(module);
                    }
                }
            }

            // we ensure that the template set module is deployed and started; if not, it will be resolved and installed + started
            ModuleTestHelper.ensureModuleStarted(info.getTemplateSet());

            JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
            site = service.addSite(info);
            site = service.getSiteByKey(info.getSiteKey());
        } finally {
            FileUtils.deleteQuietly(siteZIPFile);
        }
        return site;
    }

    private static File importSharedAndGetSiteZipFileOfPrepackagedSite(String prepackedZipFileOrBundle, String siteZIPName) {
        File siteZIPFile = null;
        File sharedZIPFile = null;
        NoCloseZipInputStream zis = null;
        try {
            zis = new NoCloseZipInputStream(getPrepackedSiteInputStream(prepackedZipFileOrBundle));
            ZipEntry z = null;
            while ((z = zis.getNextEntry()) != null) {
                boolean isUsersZip = ImportExportBaseService.USERS_ZIP.equals(z.getName());
                if (isUsersZip || siteZIPName.equalsIgnoreCase(z.getName())) {
                    File zipFile = File.createTempFile("import", ".zip");
                    FileUtils.copyInputStreamToFile(zis, zipFile);
                    if (isUsersZip) {
                        sharedZIPFile = zipFile;
                    } else {
                        siteZIPFile = zipFile;
                    }
                }
            }
            if (sharedZIPFile != null) {
                ImportExportBaseService.getInstance().importSiteZip(new FileSystemResource(sharedZIPFile), null, null);
            }
        } catch (RepositoryException e) {
            logger.warn("shared.zip could not be imported", e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (zis != null) {
                try {
                    zis.reallyClose();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            FileUtils.deleteQuietly(sharedZIPFile);
        }
        return siteZIPFile;
    }

    private static InputStream getPrepackedSiteInputStream(String prepackedZipFileOrBundle) throws IOException {
        if (StringUtils.startsWith(prepackedZipFileOrBundle, "bundle")) {
            String[] bundleInfos = StringUtils.split(prepackedZipFileOrBundle, "#");
            BundleResource resource = new BundleResource(new URL(bundleInfos[0]),
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(bundleInfos[1]).getBundle());
            return new BufferedInputStream(resource.getInputStream());
        } else {
            return new FileInputStream(new File(prepackedZipFileOrBundle));
        }
    }

    private static List<String> readInstalledModules(File siteZipFile) throws IOException {
        List<String> modules = new LinkedList<>();
        ZipInputStream zis2 = siteZipFile.isDirectory() ? new DirectoryZipInputStream(siteZipFile)
                        : new NoCloseZipInputStream(new BufferedInputStream(
                                        new FileInputStream(siteZipFile)));
        try {
            ZipEntry z;
            while ((z = zis2.getNextEntry()) != null) {
                try {
                    if (!ImportExportBaseService.SITE_PROPERTIES.equals(z.getName())) {
                        continue;
                    }
                    Properties p = new Properties();
                    p.load(zis2);
                    Map<Integer, String> im = new TreeMap<>();
                    for (Object k : p.keySet()) {
                        String key = String.valueOf(k);
                        if (!key.startsWith("installedModules.")) {
                            continue;
                        }
                        String version = StringUtils.substringAfter(key, ".");
                        if (NumberUtils.isNumber(version)) {
                            im.put(Integer.valueOf(version), p.getProperty(key));
                        }
                    }
                    modules.addAll(im.values());
                } finally {
                    zis2.closeEntry();
                }
            }
        } finally {
            if (zis2 instanceof NoCloseZipInputStream) {
                ((NoCloseZipInputStream) zis2).reallyClose();
            } else {
                zis2.close();
            }
        }

        return modules;
    }

    private static void deleteSiteIfPresent(String siteKey) throws JahiaException {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession(null, null, null);
            JahiaSite existingSite = service.getSiteByKey(siteKey, session);

            if (existingSite != null) {
                service.removeSite(existingSite);
                session.refresh(false);
            }
        } catch (RepositoryException ex) {
            logger.debug("Error while trying to remove the site", ex);
        }
    }

    private static void populateDefaults(SiteCreationInfo info) {
        long timestamp = System.currentTimeMillis();
        info.setSiteKey(StringUtils.defaultString(info.getSiteKey(), "mySite-" + timestamp));
        info.setTitle(StringUtils.defaultString(info.getTitle(), info.getSiteKey()));
        info.setDescription(StringUtils.defaultString(info.getDescription(), info.getTitle()));
        info.setServerName(StringUtils.defaultString(info.getServerName(), "localhost" + timestamp));
        info.setTemplateSet(StringUtils.defaultString(info.getTemplateSet(), WEB_TEMPLATES));
        info.setLocale(
                StringUtils.defaultString(info.getLocale(), SettingsBean.getInstance().getDefaultLanguageCode()));
        if (info.getSiteAdmin() == null) {
            info.setSiteAdmin(JahiaAdminUser.getAdminUser(null));
        }
    }

    public static JahiaSite createSite(String name) throws JahiaException, IOException {
        return createSite(SiteCreationInfo.builder().siteKey(name).build());
    }

    public static JahiaSite createSite(String name, String templateSet) throws JahiaException, IOException {
        return createSite(SiteCreationInfo.builder().siteKey(name).templateSet(templateSet).build());
    }

    public static JahiaSite createSite(String name, Set<String> languages, Set<String> mandatoryLanguages, boolean mixLanguagesActive) throws RepositoryException, JahiaException, IOException {
        createSite(name);
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

    public static JahiaSite createSite(String name, String serverName, String templateSet, String[] modulesToDeploy) throws JahiaException, IOException {
        return createSite(name, serverName, templateSet, null, null,modulesToDeploy);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet) throws JahiaException, IOException {
        return createSite(name, serverName, templateSet, null, null,null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet, String prepackedZIPFile,
            String siteZIPName, String[] modulesToDeploy) throws JahiaException, IOException {
        return createSite(SiteCreationInfo.builder().siteKey(name).serverName(serverName).templateSet(templateSet)
                .modulesToDeploy(modulesToDeploy).build(), prepackedZIPFile, siteZIPName);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName) throws JahiaException, IOException {
        return createSite(name, serverName, templateSet, prepackedZIPFile, siteZIPName, null);
    }

    public static void deleteSite(String name) throws JahiaException {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);
        if (site != null)
            service.removeSite(site);
    }

    public static int createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException {
       return createSubPages(currentNode, level, nbChildren, null);
    }

    public static int createSubPages(Node currentNode, int level, int nbChildren, String titlePrefix) throws RepositoryException {
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
    public static JCRNodeWrapper createList(JCRNodeWrapper parentNode, String listName, int elementCount, String textPrefix) throws RepositoryException {
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

    /**
     * Trigger the execution of background jobs, scheduled at the end of request and wait for the completion of their execution.
     */
    public static void triggerScheduledJobsAndWait() {
        long stepMillis = 100L;
        long maxWaitMillis = 60000L;
        SchedulerService schedulerService = ServicesRegistry.getInstance().getSchedulerService();
        schedulerService.triggerEndOfRequest();
        sleep(stepMillis);
        try {
            long count = 0;
            while (schedulerService.getRAMScheduler().getTriggerNames(SchedulerService.INSTANT_TRIGGER_GROUP).length > 0
                    || schedulerService.getScheduler()
                            .getTriggerNames(SchedulerService.INSTANT_TRIGGER_GROUP).length > 0) {
                if (count == 0) {
                    logger.info("Start waiting for background job completion...");
                }
                count++;
                if (stepMillis * count > maxWaitMillis) {
                    logger.warn("Reached timeout of {} ms waitig for job completion. Stop waiting for them.", maxWaitMillis);
                    break;
                }
                sleep(stepMillis);
            }
            logger.info("...stopped waiting for background job completion.");
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            sleep(5000);
        }
    }

    /**
     * Sleep for the specified amount of milliseconds.
     *
     * @param millis the time to sleep in milliseconds
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}

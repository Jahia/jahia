/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.interceptor.TemplateModuleInterceptor;
import org.jahia.services.content.nodetypes.JahiaCndReaderLegacy;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.importexport.validation.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DateUtils;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.jahia.utils.Url;
import org.jahia.utils.xml.JahiaSAXParserFactory;
import org.jahia.utils.zip.DirectoryZipInputStream;
import org.jahia.utils.zip.DirectoryZipOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


import static org.apache.commons.io.FileUtils.ONE_MB;


/**
 * Service used to perform all import/export operations for content and documents.
 *
 * @author Thomas Draier
 */
@SuppressWarnings({"java:S1192"})
public final class ImportExportBaseService extends JahiaService implements ImportExportService, PropertyChangeListener {

    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_XML = "application/xml";
    public static final String TEXT_XML = "text/xml";
    public static final String REPOSITORY_XML = "repository.xml";
    public static final String LIVE_REPOSITORY_XML = "live-repository.xml";
    public static final String USERS_XML = "users.xml";
    public static final String USERS_ZIP = "users.zip";
    public static final String SERVER_PERMISSIONS_XML = "serverPermissions.xml";
    public static final String SITE_PROPERTIES = "site.properties";
    public static final String EXPORT_PROPERTIES = "export.properties";
    public static final String MOUNTS_ZIP = "mounts.zip";
    public static final String REFERENCES_ZIP = "references.zip";
    public static final String ROLES_ZIP = "roles.zip";
    public static final String STATIC_MOUNT_POINT_ATTR = "j:staticMountPointProviderKey";
    public static final String DYNAMIC_MOUNT_POINT_ATTR = "j:dynamicMountPointProviderPath";
    private static final Logger logger = LoggerFactory.getLogger(ImportExportBaseService.class);
    private static final Set<String> KNOWN_IMPORT_CONTENT_TYPES = ImmutableSet.of(APPLICATION_ZIP, APPLICATION_XML, TEXT_XML);
    private static final String FILESACL_XML = "filesacl.xml";
    private static final String CATEGORIES_XML = "categories.xml";
    private static final String SITE_PERMISSIONS_XML = "sitePermissions.xml";
    private static final String DEFINITIONS_CND = "definitions.cnd";
    private static final String DEFINITIONS_MAP = "definitions.map";
    private static final FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();
    private static final HashSet<String> siteExportNodeTypesToIgnore = Sets.newHashSet("jnt:templatesFolder", "jnt:externalUser", "jnt:workflowTask", "jmix:noImportExport");
    private static final HashSet<String> defaultExportNodeTypesToIgnore = Sets.newHashSet(Constants.JAHIANT_VIRTUALSITE, "jnt:workflowTask", "jmix:noImportExport");
    private static final Path EXPORT_PATH;
    static {
        Path exportsPath = null;
        try {
            exportsPath = new File(SettingsBean.getInstance().getJahiaExportsDiskPath()).getCanonicalFile().toPath();
        } catch (Exception e) {
            logger.error("Invalid server directory of configured jahiaExportsDiskPath {}. Configuration or environment is broken leading to exceptions during content export.", SettingsBean.getInstance().getJahiaExportsDiskPath());
        } finally {
            EXPORT_PATH = exportsPath;
        }
    };
    private final long scannerInterval = SettingsBean.getInstance().getJahiaSiteImportScannerInterval();
    private JahiaSitesService sitesService;
    private JahiaFileWatcherService fileWatcherService;
    private JCRStoreService jcrStoreService;
    private CategoryService categoryService;
    private SchedulerService schedulerService;
    private boolean expandImportedFilesOnDisk;
    private String expandImportedFilesOnDiskPath;
    private List<AttributeProcessor> attributeProcessors;
    private TemplatePackageRegistry templatePackageRegistry;
    private List<XMLContentTransformer> xmlContentTransformers;
    private Map<String, Templates> xsltTemplates = new ConcurrentHashMap<>(2);
    private LegacyPidMappingTool legacyPidMappingTool = null;
    private PostImportPatcher postImportPatcher = null;

    private ImportExportBaseService() {
    }

    public static ImportExportBaseService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Helper method to determine which type of the import the uploaded file represents.
     *
     * @param declaredContentType the declared content type
     * @param fileName            the uploaded file name
     * @return type of the import the uploaded file represents
     */
    public static String detectImportContentType(String declaredContentType, String fileName) {
        String contentType = declaredContentType;
        if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
            contentType = JCRContentUtils.getMimeType(fileName);
            if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
                if (StringUtils.endsWithIgnoreCase(fileName, ".xml")) {
                    contentType = APPLICATION_XML;
                } else if (StringUtils.endsWithIgnoreCase(fileName, ".zip")) {
                    contentType = APPLICATION_ZIP;
                } else {
                    // no chance to detect it
                    logger.error("Unable to detect the content type for file {}. It is neither a ZIP file nor an XML. Skipping import.", fileName);
                }
            }
        }
        return contentType;
    }

    /**
     * Update that the export path to be under ${jahia.data.dir}/exports/
     *
     * @param serverDirectoryPath inputted export path from user
     * @return the canonical path of the server directory
     * @throws IOException if unable to get the canonical path
     */
    public static String updatedServerDirectoryPath(String serverDirectoryPath) throws IOException {
        if (serverDirectoryPath == null) {
            return null;
        }
        File exportPath = new File(serverDirectoryPath);
        return exportPath.getCanonicalFile().toPath().startsWith(EXPORT_PATH)
                ? exportPath.getCanonicalPath()
                : new File(EXPORT_PATH.toFile(), serverDirectoryPath).getCanonicalPath();
    }

    /**
     * Check if a directory is empty
     *
     * @param pathStr
     * @return True if the directory is empty, False otherwise
     * @throws IOException if unable to read files under the path provided
     */
    public static boolean isDirectoryEmpty(String pathStr) throws IOException {
        Path path = new File(pathStr).toPath();
        if (!Files.exists(path)) {
            return true;
        }
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }
        return false;
    }

    /**
     * Validate the server directory a.k.a export path specified by the user
     *
     * @param serverDirectory
     * @return true if no issues found, false otherwise
     */
    public static boolean isValidServerDirectory(String serverDirectory) {
        try {
            File serverDirectoryFile = new File(serverDirectory);
            if (!serverDirectoryFile.getCanonicalFile().toPath().startsWith(EXPORT_PATH)) {
                logger.error("User is trying to export to {} which is outside the allowed location {}",
                        serverDirectory, EXPORT_PATH);
                return false;
            }
            if (!ImportExportBaseService.isDirectoryEmpty(serverDirectory)) {
                logger.error("There are already files in the given path {}. "
                        + "You have to use a path, which is empty or does not exist yet.", serverDirectory);
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("Invalid server directory path {}", serverDirectory);
        }
        return false;
    }

    @Override
    public void start() {
        try {
            new ImportFileObserver(org.jahia.settings.SettingsBean.getInstance().getJahiaImportsDiskPath(), false, scannerInterval, true);
            EXPORT_PATH.toFile().mkdirs();
        } catch (JahiaException je) {
            logger.error("exception with FilesObserver", je);
        }
    }

    public void setExpandImportedFilesOnDisk(boolean expandImportedFilesOnDisk) {
        this.expandImportedFilesOnDisk = expandImportedFilesOnDisk;
    }

    public void setExpandImportedFilesOnDiskPath(String expandImportedFilesOnDiskPath) {
        this.expandImportedFilesOnDiskPath = expandImportedFilesOnDiskPath;
    }

    /**
     * Returns a list of configured attribute processors for the import.
     *
     * @return a list of configured attribute processors for the import
     */
    public List<AttributeProcessor> getAttributeProcessors() {
        return attributeProcessors;
    }

    /**
     * Sets a list of configured attribute processors for the import.
     *
     * @param attributeProcessors a list of configured attribute processors for the import
     */
    public void setAttributeProcessors(List<AttributeProcessor> attributeProcessors) {
        this.attributeProcessors = attributeProcessors;
    }

    @Override
    public void stop() {
        // do nothing
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public void setFileWatcherService(JahiaFileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    @Override
    public void exportAll(OutputStream outputStream, Map<String, Object> params)
            throws JahiaException, RepositoryException, IOException, SAXException, TransformerException {
        exportSites(outputStream, params, sitesService.getSitesNodeList());
    }

    /**
     * Estimate the number of nodes under a list of nodes
     *
     * @param sortedNodes       the nodes to be exported
     * @param session           the session used to export
     * @param nodeTypesToIgnore the node types to ignore in the export
     * @return the estimation of nodes to export
     * @throws RepositoryException in case of JCR-related errors
     */
    private long estimateNodesToExport(Set<JCRNodeWrapper> sortedNodes, JCRSessionWrapper session,
                                       Set<String> nodeTypesToIgnore) throws RepositoryException {

        long result = 0;
        List<String> extraPathsToExport = new ArrayList<>();

        for (JCRNodeWrapper nodesToExport : sortedNodes) {
            // site node are a bit special (languages nodes have to be exported)
            if (nodesToExport instanceof JCRSiteNode) {
                Set<String> languages = ((JCRSiteNode) nodesToExport).getLanguages();
                List<String> sitePaths = Collections.singletonList(nodesToExport.getPath());
                result += estimateSubnodesNumber(sitePaths, session, nodeTypesToIgnore, null);
                if (languages != null && !languages.isEmpty()) {
                    for (String language : languages) {
                        result += estimateSubnodesNumber(sitePaths, session, nodeTypesToIgnore, language);
                    }
                }
            } else {
                extraPathsToExport.add(nodesToExport.getPath());
            }
        }

        // Avoid using all paths within a single query to prevent a stack overflow when number of paths is large.
        // see https://jira.jahia.org/browse/QA-11510
        final int maxBatchSize = 100;
        while (!extraPathsToExport.isEmpty()) {
            int toIndex = Math.min(extraPathsToExport.size(), maxBatchSize);
            List<String> paths = extraPathsToExport.subList(0, toIndex);
            result += estimateSubnodesNumber(paths, session, nodeTypesToIgnore, null);
            paths.clear();
        }

        return result;
    }

    /**
     * Estimates subnodes number to be exported, estimation use a count query using given parameters.
     *
     * @param paths             list of paths in query
     * @param session           session used to execute the query
     * @param nodeTypesToIgnore Set of nodetypes to filter in query
     * @param locale            language to be used when jnt:translation nodes have to be retrieved
     * @return the final estimation
     * @throws RepositoryException in case of JCR-related errors
     */
    private long estimateSubnodesNumber(List<String> paths, JCRSessionWrapper session, Set<String> nodeTypesToIgnore, String locale) throws RepositoryException {
        if (paths.isEmpty()) {
            return 0;
        }

        // create the query count, if a locale is specified the query is adapted to retrieved only jnt:translation nodes
        QueryManagerWrapper queryManagerWrapper = session.getWorkspace().getQueryManager();
        StringBuilder statement = new StringBuilder("SELECT count AS [rep:count(skipChecks=1)] FROM [")
                .append(locale != null ? "jnt:translation" : "nt:base")
                .append("] WHERE (");

        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            if (i > 0) {
                statement.append(" OR ");
            }
            statement.append("isdescendantnode(['").append(JCRContentUtils.sqlEncode(path)).append("'])");
        }
        statement.append(")");

        if (locale == null && nodeTypesToIgnore != null && !nodeTypesToIgnore.isEmpty()) {
            statement.append("AND NOT (");
            Iterator<String> nodeTypesToIgnoreIterator = nodeTypesToIgnore.iterator();
            while (nodeTypesToIgnoreIterator.hasNext()) {
                statement.append("[jcr:primaryType] = '").append(JCRContentUtils.sqlEncode(nodeTypesToIgnoreIterator.next())).append("'");
                if (nodeTypesToIgnoreIterator.hasNext()) {
                    statement.append(" OR ");
                }
            }
            statement.append(")");
        }

        if (locale != null) {
            statement.append(" AND [jcr:language] = '").append(JCRContentUtils.sqlEncode(locale)).append("'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing query: {}", statement);
        }

        return queryManagerWrapper.createQuery(statement.toString(), Query.JCR_SQL2).execute().getRows().nextRow().getValue("count").getLong();
    }

    @Override
    public void exportSites(OutputStream outputStream, Map<String, Object> params, List<JCRSiteNode> sites)
            throws RepositoryException, IOException, SAXException, TransformerException, JahiaForbiddenAccessException {

        logger.info("Sites {} export started", sites);
        long startSitesExportTime = System.currentTimeMillis();
        String serverDirectory = ImportExportBaseService.updatedServerDirectoryPath((String) params.get(SERVER_DIRECTORY));
        if (serverDirectory != null && !ImportExportBaseService.isValidServerDirectory(serverDirectory)) {
            logger.error("Invalid server directory {}", serverDirectory);
            throw new JahiaForbiddenAccessException("The directory " + serverDirectory + " failed the validation check");
        }
        ZipOutputStream zout = getZipOutputStream(outputStream, serverDirectory);
        ZipEntry anEntry = new ZipEntry(EXPORT_PROPERTIES);
        zout.putNextEntry(anEntry);
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout))) {
            bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
            bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
            bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
            bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
            bw.flush();

            Set<String> externalReferences = new HashSet<>();
            JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();

            exportSites(params, sites, serverDirectory, zout, externalReferences);
            exportUsers(params, serverDirectory, zout, externalReferences, session);
            exportRoles(params, serverDirectory, zout, externalReferences, session);
            exportMounts(params, zout, externalReferences, session);
            exportReferences(params, serverDirectory, zout, externalReferences, session);
        }

        logger.info("Total Sites {} export ended in {} seconds", sites, getDuration(startSitesExportTime));
    }

    private void exportSites(Map<String, Object> params, List<JCRSiteNode> sites, String serverDirectory, ZipOutputStream zout,
                             Set<String> externalReferences) throws IOException, RepositoryException, SAXException, TransformerException {
        ZipEntry anEntry;
        for (JCRSiteNode jahiaSite : sites) {
            long startSiteExportTime = System.currentTimeMillis();
            logger.info("Exporting site internal nodes {} content started", jahiaSite.getName());
            if (serverDirectory == null) {
                anEntry = new ZipEntry(jahiaSite.getSiteKey() + ".zip");
                zout.putNextEntry(anEntry);

                exportSite(jahiaSite, zout, externalReferences, params, null);
            } else {
                exportSite(jahiaSite, zout, externalReferences, params, serverDirectory + "/" + jahiaSite.getSiteKey());
            }
            logger.info("Exporting site internal nodes {} ended in {} seconds", jahiaSite.getName(), getDuration(startSiteExportTime));
        }
    }

    private void exportReferences(Map<String, Object> params, String serverDirectory, ZipOutputStream zout, Set<String> externalReferences,
                                  JCRSessionWrapper session) throws RepositoryException, IOException {
        Set<JCRNodeWrapper> refs = new HashSet<>();
        for (String reference : externalReferences) {
            JCRNodeWrapper node = session.getNodeByUUID(reference);
            if (!defaultExportNodeTypesToIgnore.contains(node.getPrimaryNodeTypeName())) {
                refs.add(node);
            }
        }
        if (!refs.isEmpty()) {
            zout.putNextEntry(new ZipEntry(REFERENCES_ZIP));
            ZipOutputStream zzout = getZipOutputStream(zout, serverDirectory + "/" + REFERENCES_ZIP);
            try {
                logger.info("Exporting References Started");
                exportNodesWithBinaries(session.getRootNode(), refs, zzout, defaultExportNodeTypesToIgnore, externalReferences, params,
                        true);
                logger.info("Exporting References Ended");
            } catch (Exception e) {
                logger.error("Cannot export References", e);
            }
            zzout.finish();
        }
        zout.finish();
    }

    private void exportMounts(Map<String, Object> params, ZipOutputStream zout, Set<String> externalReferences, JCRSessionWrapper session)
            throws RepositoryException, IOException {
        if (params.containsKey(INCLUDE_MOUNTS) && session.nodeExists("/mounts")) {
            JCRNodeWrapper mounts = session.getNode("/mounts");
            if (mounts.hasNodes()) {
                // export mounts
                zout.putNextEntry(new ZipEntry(MOUNTS_ZIP));
                ZipOutputStream zzout = new ZipOutputStream(zout);

                try {
                    logger.info("Exporting Mount points Started");
                    exportNodesWithBinaries(session.getRootNode(), Collections.singleton(mounts), zzout, defaultExportNodeTypesToIgnore,
                            externalReferences, params, true);
                    logger.info("Exporting Mount points Ended");
                } catch (Exception e) {
                    logger.error("Cannot export mount points", e);
                }
                zzout.finish();
            }
        }
    }

    private void exportRoles(Map<String, Object> params, String serverDirectory, ZipOutputStream zout, Set<String> externalReferences,
                             JCRSessionWrapper session) throws IOException {
        if (params.containsKey(INCLUDE_ROLES)) {
            // export roles
            ZipOutputStream zzout;
            String rolesPath = "roles";
            if (serverDirectory == null) {
                zout.putNextEntry(new ZipEntry(ROLES_ZIP));
                zzout = getZipOutputStream(zout, null);
            } else {
                zzout = getZipOutputStream(zout, String.format("%s/%s", serverDirectory, rolesPath));
            }

            try {
                logger.info("Exporting Roles Started");
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode(String.format("/%s", rolesPath))), zzout,
                        defaultExportNodeTypesToIgnore, externalReferences, params, true);
                logger.info("Exporting Roles Ended");
            } catch (Exception e) {
                logger.error("Cannot export roles", e);
            }
            zzout.finish();
        }
    }

    private void exportUsers(Map<String, Object> params, String serverDirectory, ZipOutputStream zout, Set<String> externalReferences,
                             JCRSessionWrapper session) throws IOException {
        if (params.containsKey(INCLUDE_USERS)) {
            // export users
            ZipOutputStream zzout;
            String usersPath = "users";
            if (serverDirectory == null) {
                zout.putNextEntry(new ZipEntry(USERS_ZIP));
                zzout = getZipOutputStream(zout, null);
            } else {
                zzout = getZipOutputStream(zout, String.format("%s/%s", serverDirectory, usersPath));
            }

            try {
                logger.info("Exporting Users Started");
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode(String.format("/%s", usersPath))), zzout,
                        defaultExportNodeTypesToIgnore, externalReferences, params, true);
                logger.info("Exporting Users Ended");
            } catch (IOException e) {
                logger.warn("Cannot export due to some IO exception :{}", e.getMessage());
            } catch (Exception e) {
                logger.error("Cannot export Users", e);
            }
            zzout.finish();
        }
    }

    private ZipOutputStream getZipOutputStream(OutputStream outputStream, String serverDirectory) {
        if (serverDirectory != null) {
            File serverDirectoryFile = new File(serverDirectory);
            if (serverDirectoryFile.mkdirs()) {
                return new DirectoryZipOutputStream(serverDirectoryFile, outputStream);
            }
            logger.error("Unable to create directory {}. Check permission", serverDirectory);
            throw new JahiaRuntimeException("Unable to create directory");
        } else {
            return new ZipOutputStream(outputStream);
        }
    }

    private void exportSite(final JCRSiteNode site, OutputStream out, Set<String> externalReferences, Map<String, Object> params, String serverDirectory)
            throws RepositoryException, SAXException, IOException, TransformerException {

        ZipOutputStream zout = getZipOutputStream(out, serverDirectory);

        zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
        exportSiteInfos(zout, site);
        final JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();
        JCRNodeWrapper node = session.getNode(String.format("/sites/%s", site.getSiteKey()));
        Set<JCRNodeWrapper> nodes = Collections.singleton(node);
        exportNodesWithBinaries(session.getRootNode(), nodes, zout, siteExportNodeTypesToIgnore,
                externalReferences, params, true);
        zout.finish();
    }

    @Override
    public void exportZip(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params)
            throws RepositoryException, SAXException, IOException, TransformerException, JahiaForbiddenAccessException {

        String serverDirectory = (String) params.get(SERVER_DIRECTORY);
        if (serverDirectory != null && !ImportExportBaseService.isValidServerDirectory(serverDirectory)) {
            logger.error("Invalid server directory {}", serverDirectory);
            throw new JahiaRuntimeException("The directory " + serverDirectory + " failed the validation check");
        }
        ZipOutputStream zout = getZipOutputStream(out, serverDirectory);
        Set<JCRNodeWrapper> nodes = new HashSet<>();
        nodes.add(node);
        exportNodesWithBinaries(exportRoot == null ? node : exportRoot, nodes, zout, new HashSet<>(), null,
                params, false);
        zout.finish();
    }

    @Override
    public void exportNode(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params)
            throws RepositoryException, SAXException, IOException, TransformerException {

        TreeSet<JCRNodeWrapper> nodes = new TreeSet<>(Comparator.comparing(JCRNodeWrapper::getPath));
        nodes.add(node);
        exportNodes(exportRoot == null ? node : exportRoot, nodes, out, new HashSet<>(), null, params, false);
    }

    private void exportNodesWithBinaries(JCRNodeWrapper rootNode, Set<JCRNodeWrapper> nodes, ZipOutputStream zout,
                                         Set<String> typesToIgnore, Set<String> externalReferences,
                                         Map<String, Object> params, boolean logProgress)
            throws SAXException, IOException, RepositoryException, TransformerException {

        TreeSet<JCRNodeWrapper> liveSortedNodes = new TreeSet<>(Comparator.comparing(JCRNodeWrapper::getPath));

        // handle download as zip a set of nodes
        if (params.containsKey("filesToZip")) {
            String[] filesToZip = (String[]) params.get("filesToZip");
            byte[] buffer = new byte[4096];
            Set<String> exportedFiles = new HashSet<>();
            for (String file : filesToZip) {
                try {
                    final String basePath = rootNode.getParent().getPath();
                    zipFiles(StringUtils.equals(basePath, "/") ? "" : basePath, zout, buffer, exportedFiles, rootNode.getSession().getNode(file));
                } catch (Throwable e) {
                    logger.warn("Unable to add {} to zip file", file);
                }

            }
            return;
        }
        if (params.containsKey(INCLUDE_LIVE_EXPORT) &&
                params.get(INCLUDE_LIVE_EXPORT) != null &&
                Boolean.TRUE.equals(params.get(INCLUDE_LIVE_EXPORT))) {
            final JCRSessionWrapper liveSession = jcrStoreService.getSessionFactory().getCurrentUserSession("live");
            try {
                exportLiveRootNodeIfPossible(rootNode, nodes, zout, typesToIgnore, externalReferences, params, logProgress, liveSortedNodes, liveSession);
            } catch (RepositoryException e) {
                logger.debug("Item not found in live while exporting {}", e.getMessage());
            }
        }

        TreeSet<JCRNodeWrapper> sortedNodes = new TreeSet<>(Comparator.comparing(JCRNodeWrapper::getPath));
        sortedNodes.addAll(nodes);
        for (JCRNodeWrapper liveSortedNode : liveSortedNodes) {
            try {
                sortedNodes.add(rootNode.getSession().getNodeByIdentifier(liveSortedNode.getIdentifier()));
            } catch (ItemNotFoundException e) {
                // Node does not exist in default do nothing
            }
        }
        zout.putNextEntry(new ZipEntry(REPOSITORY_XML));
        logger.info("Exporting default workspace for nodes {} ...", nodes);
        exportNodes(rootNode, sortedNodes, zout, typesToIgnore, externalReferences, params, logProgress);
        zout.closeEntry();
        exportNodesBinary(rootNode, sortedNodes, zout, typesToIgnore, "/content");
        logger.info("Default workspace exported for nodes {}", nodes);
    }

    private void zipFiles(String basePath, ZipOutputStream zout, byte[] buffer, Set<String> exportedFiles, JCRNodeWrapper fileNode) throws RepositoryException {
        if (fileNode.isNodeType("nt:file")) {
            // export file node
            if (exportedFiles.contains(fileNode.getIdentifier())) {
                // File already processed.
                return;
            }
            exportedFiles.add(fileNode.getIdentifier());
            int bytesIn;
            try {
                if (!fileNode.hasNode(Constants.JCR_CONTENT)) {
                    logger.debug("Node {} of type will not be part of the zip ", fileNode.getPath(), fileNode.getPrimaryNodeTypeName());
                    return;
                }
                JCRNodeWrapper child = fileNode.getNode(Constants.JCR_CONTENT);
                JCRPropertyWrapper property = child.getProperty("jcr:data");
                if (child.getProvider().canExportProperty(property)) {
                    try (InputStream is = property.getBinary().getStream()) {
                        if (is != null) {
                            String path = fileNode.getPath().substring(basePath.length());
                            zout.putNextEntry(new ZipEntry(path.substring(1)));
                            while ((bytesIn = is.read(buffer)) != -1) {
                                zout.write(buffer, 0, bytesIn);
                            }
                        }
                    }
                }
            } catch (RepositoryException | AssertionError | IOException e) {
                logger.warn("Unable to export {}", fileNode.getPath());
            }
            return;
        }
        // recurse on children
        if (exportedFiles.contains(fileNode.getIdentifier())) {
            return;
        }
        fileNode.getNodes().forEach(child -> {
            try {
                zipFiles(basePath, zout, buffer, exportedFiles, child);
            } catch (RepositoryException e) {
                logger.warn("Unable to read file {}", child.getPath());
            }
        });
    }

    @SuppressWarnings("java:S107")
    private void exportLiveRootNodeIfPossible(JCRNodeWrapper rootNode, Set<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore, Set<String> externalReferences, Map<String, Object> params, boolean logProgress, TreeSet<JCRNodeWrapper> liveSortedNodes, JCRSessionWrapper liveSession) throws RepositoryException, IOException, SAXException, TransformerException {
        JCRNodeWrapper liveRootNode = liveSession.getNodeByIdentifier(rootNode.getIdentifier());
        for (JCRNodeWrapper node : nodes) {
            try {
                liveSortedNodes.add(liveSession.getNodeByIdentifier(node.getIdentifier()));
            } catch (ItemNotFoundException e) {
                logger.debug("Item not found in live while exporting {}", e.getMessage());
            }
        }
        if (!liveSortedNodes.isEmpty()) {
            zout.putNextEntry(new ZipEntry(LIVE_REPOSITORY_XML));
            logger.info("Exporting live workspace for nodes {} ...", nodes);
            exportNodes(liveRootNode, liveSortedNodes, zout, typesToIgnore, externalReferences, params, logProgress);
            zout.closeEntry();
            exportNodesBinary(liveRootNode, liveSortedNodes, zout, typesToIgnore, "/live-content");
            logger.info("Live workspace exported for nodes {}", nodes);
        }
    }

    private void exportNodes(JCRNodeWrapper rootNode, TreeSet<JCRNodeWrapper> sortedNodes, OutputStream outputStream,
                             Set<String> typesToIgnore, Set<String> externalReferences, Map<String, Object> params, boolean logProgress)
            throws IOException, RepositoryException, SAXException, TransformerException {

        long startSitesExportTime = System.currentTimeMillis();
        ExportContext exportContext = null;
        if (logProgress) {
            // estimate the number of nodes to exports and logs this information
            long estimatedNodes = estimateNodesToExport(sortedNodes, rootNode.getSession(), typesToIgnore);
            logger.info("Approximate number of nodes to export: {}, estimated in: {} seconds", estimatedNodes,
                    getDuration(startSitesExportTime));
            exportContext = new ExportContext(estimatedNodes);
        }

        final String xsl = (String) params.get(XSL_PATH);
        final boolean skipBinary = !Boolean.FALSE.equals(params.get(SKIP_BINARY));
        final boolean noRecurse = Boolean.TRUE.equals(params.get(NO_RECURSE));

        OutputStream tmpOut = outputStream;
        if (xsl != null) {
            String filename = Patterns.SPACE.matcher(rootNode.getName()).replaceAll("_");
            File tempFile = File.createTempFile("exportTemplates-" + filename, ".xml");
            tmpOut = new DeferredFileOutputStream(1024 * 1024 * 10, tempFile);
        }

        DataWriter dw = new DataWriter(new OutputStreamWriter(tmpOut, StandardCharsets.UTF_8));
        if (Boolean.TRUE.equals(params.get(SYSTEM_VIEW))) {
            SystemViewExporter exporter = new SystemViewExporter(rootNode.getSession(), dw, !noRecurse, !skipBinary);
            exporter.export(rootNode);
        } else {
            exportNodesUsingDocumentViewExporter(rootNode, sortedNodes, typesToIgnore, externalReferences, params, exportContext, skipBinary, noRecurse, dw);
        }

        if (exportContext != null) {
            // Nodes are now exported in the .xml, so we log the time difference for this export
            logger.info("Exported {} nodes in {} seconds", exportContext.getExportIndex(), getDuration(startSitesExportTime));
        }

        dw.flush();
        if (xsl != null) {
            try (DeferredFileOutputStream stream = (DeferredFileOutputStream) tmpOut;
                 InputStream inputStream = stream.isInMemory() ? new ByteArrayInputStream(stream.getData()) : new BufferedInputStream(new FileInputStream(stream.getFile()))) {
                fileCleaningTracker.track(stream.getFile(), inputStream);
                Transformer transformer = getTransformer(xsl);
                long startXmlCleanup = System.currentTimeMillis();
                if (logProgress) {
                    // since the xml transformation can be heavy in process depending on the .xml size
                    // we logs some basics data
                    logger.info("Starting cleanup transformation ...");
                    transformer.transform(new StreamSource(inputStream), new StreamResult(outputStream));
                    logger.info("Cleanup transformation finished in {} seconds", getDuration(startXmlCleanup));
                } else {
                    transformer.transform(new StreamSource(inputStream), new StreamResult(outputStream));
                }
            }
        }
    }

    @SuppressWarnings("java:S107")
    private void exportNodesUsingDocumentViewExporter(JCRNodeWrapper rootNode, TreeSet<JCRNodeWrapper> sortedNodes, Set<String> typesToIgnore, Set<String> externalReferences, Map<String, Object> params, ExportContext exportContext, boolean skipBinary, boolean noRecurse, DataWriter dw) throws RepositoryException, SAXException {
        DocumentViewExporter exporter = new DocumentViewExporter(rootNode.getSession(), dw, skipBinary, noRecurse);
        exporter.setExportContext(exportContext);
        exporter.addObserver(this);

        if (externalReferences != null) {
            exporter.setExternalReferences(externalReferences);
        }
        typesToIgnore.add("rep:system");
        if (params.containsKey(INCLUDE_LIVE_EXPORT)) {
            List<String> l = new ArrayList<>(exporter.getPropertiestoIgnore());
            l.remove("jcr:uuid");
            exporter.setPropertiestoIgnore(l);
            if (rootNode.getSession().getWorkspace().getName().equals(Constants.EDIT_WORKSPACE)) {
                exporter.setPublicationStatusSession(jcrStoreService.getSessionFactory().getCurrentUserSession("live"));
            }
        }
        exporter.setTypesToIgnore(typesToIgnore);
        exporter.export(rootNode, sortedNodes);

        sortedNodes.addAll(exporter.getNodesList());
    }

    private Transformer getTransformer(String xsl) throws TransformerConfigurationException {
        Templates templates = xsltTemplates.get(xsl);
        if (templates == null) {
            templates = new TransformerFactoryImpl().newTemplates(new StreamSource(new File(xsl)));
            xsltTemplates.put(xsl, templates);
        }

        return templates.newTransformer();
    }

    private void exportNodesBinary(JCRNodeWrapper root, SortedSet<JCRNodeWrapper> nodes,
                                   ZipOutputStream zout, Set<String> typesToIgnore, String basepath)
            throws IOException, RepositoryException {

        // binary export can be time-consuming, log some basic information
        long startExportingNodesBinary = System.currentTimeMillis();
        logger.info("Exporting binary nodes ...");

        byte[] buffer = new byte[4096];
        for (JCRNodeWrapper file : nodes) {
            exportNodeBinary(root, file, zout, typesToIgnore, buffer, basepath, new HashSet<>());
        }

        logger.info("Binary nodes exported in {} seconds", getDuration(startExportingNodesBinary));
    }

    private void exportNodeBinary(JCRNodeWrapper root, JCRNodeWrapper node, ZipOutputStream zout,
                                  Set<String> typesToIgnore, byte[] buffer, String basepath, Set<String> exportedFiles)
            throws IOException, RepositoryException {

        if (!typesToIgnore.contains(node.getPrimaryNodeTypeName()) && node.getProvider().canExportNode(node)) {
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.nextNode();
                if (child.getProvider().canExportNode(child)) {
                    if (child.isNodeType("nt:resource") && !exportedFiles.contains(child.getPath())) {
                        exportFile(root, node, zout, buffer, basepath, exportedFiles, child);
                    }
                    exportNodeBinary(root, child, zout, typesToIgnore, buffer, basepath, exportedFiles);
                }
            }
        }
    }

    private void exportFile(JCRNodeWrapper root, JCRNodeWrapper node, ZipOutputStream zout, byte[] buffer, String basepath, Set<String> exportedFiles, JCRNodeWrapper child) throws IOException {
        int bytesIn;
        exportedFiles.add(child.getPath());
        try {
            JCRPropertyWrapper property = child.getProperty("jcr:data");
            if (child.getProvider().canExportProperty(property)) {
                try (InputStream is = property.getBinary().getStream()) {
                    if (is != null) {
                        String path = node.getPath();
                        if (root.getPath().equals("/")) {
                            path = basepath + path;
                        } else {
                            path = basepath + path.substring(root.getParent().getPath().length());
                        }
                        String name = JCRContentUtils.replaceColon(child.getName());
                        if (child.getName().equals("jcr:content")) {
                            name = node.getName();
                        }
                        path += "/" + name;
                        zout.putNextEntry(new ZipEntry(path.substring(1)));
                        while ((bytesIn = is.read(buffer)) != -1) {
                            zout.write(buffer, 0, bytesIn);
                        }
                    }
                }
            }
        } catch (RepositoryException | AssertionError ex) {
            logger.warn("Cannot export {}", child.getPath(), ex);
        }
    }

    private void exportSiteInfos(OutputStream out, JCRSiteNode s) throws IOException {
        Properties p = new OrderedProperties();
        p.setProperty("sitetitle", s.getTitle());
        p.setProperty("siteservername", s.getServerName());
        p.setProperty("siteservernamealiases", StringUtils.join(s.getServerNameAliases(), ", "));
        p.setProperty("sitekey", s.getSiteKey());
        p.setProperty("description", s.getDescr());
        p.setProperty("templatePackageName", s.getTemplateFolder());
        p.setProperty("mixLanguage", Boolean.toString(s.isMixLanguagesActive()));
        p.setProperty("defaultLanguage", s.getDefaultLanguage());
        int i = 1;
        for (String s1 : s.getInstalledModules()) {
            p.setProperty("installedModules." + (i++), s1);
        }

        Set<String> v = s.getLanguages();
        for (String sls : v) {
            p.setProperty("language." + sls + ".activated", s.getInactiveLiveLanguages().contains(sls) ? "false" : "true");
            p.setProperty("language." + sls + ".mandatory", "" + s.getMandatoryLanguages().contains(sls));
        }
        for (String sls : s.getInactiveLanguages()) {
            p.setProperty("language." + sls + ".disabledCompletely", "true");
        }

        try {
            final JahiaSite defaultSite = sitesService.getDefaultSite(s.getSession());
            if (defaultSite != null && defaultSite.getSiteKey().equals(s.getName())) {
                p.setProperty("defaultSite", "true");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        p.store(out, "");
    }

    @Override
    public void importSiteZip(JCRNodeWrapper nodeWrapper) throws RepositoryException, IOException, JahiaException {
        String uri = nodeWrapper.getPath();
        Node contentNode = nodeWrapper.getNode(Constants.JCR_CONTENT);
        try (ZipInputStream zis = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream())) {
            importSiteZip(zis, uri, null, nodeWrapper.getSession());
        }
    }

    @Override
    public void importSiteZip(File file, JCRSessionWrapper session) throws RepositoryException, IOException, JahiaException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
            importSiteZip(zis, null, null, session);
        }
    }

    @Override
    public void importSiteZip(Resource file) throws RepositoryException, IOException, JahiaException {
        importSiteZip(file, null);
    }

    @Override
    public void importSiteZip(Resource file, JCRSessionWrapper session) throws RepositoryException, IOException {
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            importSiteZip(zis, null, file, session);
        }
    }

    private void importSiteZip(ZipInputStream zis2, final String uri, final Resource fileImport, JCRSessionWrapper session) throws IOException {
        ZipEntry z;
        final Properties infos = new Properties();
        while ((z = zis2.getNextEntry()) != null) {
            if (SITE_PROPERTIES.equals(z.getName())) {
                infos.load(zis2);
                zis2.closeEntry();
            }
        }
        boolean siteKeyEx = false;
        boolean serverNameEx = false;
        try {
            siteKeyEx = "".equals(
                    infos.get("sitekey")) || sitesService.siteExists((String) infos.get("sitekey"), session);
            String serverName = (String) infos.get("siteservername");
            String serverNameAliases = (String) infos.get("siteservernamealiases");
            serverNameEx = "".equals(serverName) || (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName, session) != null);
            if (!serverNameEx && StringUtils.isNotEmpty(serverNameAliases)) {
                for (String alias : StringUtils.split(serverNameAliases, ", ")) {
                    if (!Url.isLocalhost(alias) && sitesService.getSiteByServerName(alias, session) != null) {
                        serverNameEx = true;
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting site", e);
        }

        if (!siteKeyEx && !serverNameEx) {
            performSiteImport(uri, fileImport, session, infos);
        }
    }

    private void performSiteImport(String uri, Resource fileImport, JCRSessionWrapper session, Properties infos) {
        // site import
        String tpl = (String) infos.get("templatePackageName");
        if ("".equals(tpl)) {
            tpl = null;
        }
        try {
            final Locale finalLocale = getFinalLocale(infos);
            final String finalTpl = tpl;
            try {
                JCRObservationManager.doWithOperationType(session, JCRObservationManager.IMPORT, new JCRCallback<Object>() {

                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        try {
                            SiteCreationInfo siteCreationInfo = SiteCreationInfo.builder().
                                    siteKey(infos.getProperty("sitekey")).
                                    serverName(infos.getProperty("siteservername")).
                                    serverNameAliases(infos.getProperty("siteservernamealiases")).
                                    title(infos.getProperty("sitetitle")).
                                    description(infos.getProperty("description")).
                                    templateSet(finalTpl).
                                    modulesToDeploy(null).
                                    locale(finalLocale != null ? finalLocale.toString() : null).
                                    siteAdmin(JCRSessionFactory.getInstance().getCurrentUser()).
                                    firstImport(fileImport != null ? "fileImport" : "importRepositoryFile").
                                    fileImport(fileImport).
                                    fileImportName(uri).
                                    originatingJahiaRelease(infos.getProperty("originatingJahiaRelease")).build();
                            JahiaSite site = sitesService
                                    .addSite(siteCreationInfo, session);
                            importSiteProperties(site, infos, session);
                        } catch (JahiaException | IOException e) {
                            throw new RepositoryException(e);
                        }
                        return null;
                    }
                });
            } catch (RepositoryException e) {
                if (e.getCause() != null
                        && (e.getCause() instanceof JahiaException || e.getCause() instanceof IOException)) {
                    throw (Exception) e.getCause();
                }
            }
        } catch (Exception e) {
            logger.error("Cannot create site " + infos.get("sitetitle"), e);
        }
    }

    @Nullable
    private Locale getFinalLocale(Properties infos) {
        Locale locale = null;
        if (infos.getProperty("defaultLanguage") != null) {
            locale = LanguageCodeConverters.languageCodeToLocale(infos.getProperty("defaultLanguage"));
        } else {
            for (Object obj : infos.keySet()) {
                String s = (String) obj;
                if (s.startsWith("language.") && s.endsWith(".rank")) {
                    String code = s.substring(s.indexOf('.') + 1, s.lastIndexOf('.'));
                    String rank = infos.getProperty(s);
                    if (rank.equals("1")) {
                        locale = LanguageCodeConverters.languageCodeToLocale(code);
                    }
                }
            }
        }
        return locale;
    }


    @Override
    public void importSiteZip(final Resource file, final JahiaSite site, final Map<Object, Object> infos) throws RepositoryException, IOException {
        importSiteZip(file, site, infos, null, null);
    }

    @Override
    public void importSiteZip(Resource file, JahiaSite site, Map<Object, Object> infos, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath) throws RepositoryException, IOException {
        importSiteZip(file, site, infos, legacyMappingFilePath, legacyDefinitionsFilePath, jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null));
    }

    /**
     * Import a full site zip into a newly created site.
     * <p/>
     * zip file can contain all kind of legacy jahia import files or jcr import format.
     *
     * @param file                      Zip file
     * @param site                      The new site where to import
     * @param infos                     site infos
     * @param legacyMappingFilePath     path to the legacy mappings
     * @param legacyDefinitionsFilePath path for the legacy definitions
     * @param session                   the current JCR session to use for the import
     * @throws RepositoryException in case of JCR-related errors
     * @throws IOException         in case of I/O errors
     */
    @SuppressWarnings("java:S2093")
    public void importSiteZip(Resource file, JahiaSite site, Map<Object, Object> infos, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath, JCRSessionWrapper session) throws RepositoryException, IOException {
        long timerSite = System.currentTimeMillis();
        logger.info("Start import for site {}", site != null ? site.getSiteKey() : "");

        final CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler();
        final UsersImportHandler usersImportHandler = new UsersImportHandler(site, session);

        boolean legacyImport = false;
        List<String[]> catProps = null;
        List<String[]> userProps = null;

        Map<String, Long> sizes = new HashMap<>();
        List<String> fileList = new ArrayList<>();

        logger.info("Start analyzing import file {}", file);
        long timer = System.currentTimeMillis();
        File expandedFolder = getFileList(file, sizes, fileList, false);
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Done analyzing import file {} in {}", file, DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
            }

            Map<String, String> pathMapping = getRegisteredModulesPathMapping(session);


            userProps = importUsersIfPresentInArchive(file, usersImportHandler, userProps, sizes);

            // Check if it is an 5.x or 6.1 import :
            for (Map.Entry<String, Long> entry : sizes.entrySet()) {
                if (entry.getKey().startsWith("export_")) {
                    legacyImport = true;
                    break;
                }
            }

            importSitePropertiesIfPresentInArchive(file, site, session, sizes);

            if (sizes.containsKey(REPOSITORY_XML)) {
                importRepositoryDescriptorIfPresentInArchive(file, site, session, pathMapping);
            } else {
                // No repository descriptor - prepare to import files directly
                pathMapping.put("/", "/sites/" + site.getSiteKey() + "/files/");
            }

            catProps = importAdditionalFilesIfPresentInArchiveOrPerformLegacyImportIfNeeded(file, site, infos, legacyMappingFilePath, legacyDefinitionsFilePath, session, timerSite, categoriesImportHandler, legacyImport, catProps, sizes, fileList, pathMapping);

            categoriesImportHandler.setUuidProps(catProps);
            usersImportHandler.setUuidProps(userProps);

            session.save(JCRObservationManager.IMPORT);
        } finally {
            cleanFilesList(expandedFolder);
        }

        if (legacyImport && this.postImportPatcher != null) {
            final long timerPIP = System.currentTimeMillis();
            logger.info("Executing post import patches");
            this.postImportPatcher.executePatches(site);
            if (logger.isInfoEnabled()) {
                logger.info("Executed post import patches in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerPIP));
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("Done importing site {} in {}", site != null ? site.getSiteKey() : "", DateUtils.formatDurationWords(System.currentTimeMillis() - timerSite));
        }
    }

    @SuppressWarnings("java:S107")
    private List<String[]> importAdditionalFilesIfPresentInArchiveOrPerformLegacyImportIfNeeded(Resource file, JahiaSite site, Map<Object, Object> infos, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath, JCRSessionWrapper session, long timerSite, CategoriesImportHandler categoriesImportHandler, boolean legacyImport, List<String[]> catProps, Map<String, Long> sizes, List<String> fileList, Map<String, String> pathMapping) throws IOException, RepositoryException {
        NodeTypeRegistry reg = NodeTypeRegistry.getInstance();
        DefinitionsMapping mapping = null;

        // Import additional files - site.properties, old cateogries.xml , sitepermissions.xml
        // and eventual plain file from 5.x imports
        if (!sizes.containsKey(REPOSITORY_XML) || sizes.containsKey(SITE_PROPERTIES) || sizes.containsKey(CATEGORIES_XML)
                || sizes.containsKey(SITE_PERMISSIONS_XML) || sizes.containsKey(DEFINITIONS_CND) || sizes.containsKey(DEFINITIONS_MAP)) {
            ZipInputStream zis = getZipInputStream(file);
            try {
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    String name = zipentry.getName();
                    if (name.indexOf('\\') > -1) {
                        name = name.replace('\\', '/');
                    }
                    if (name.indexOf('/') > -1) {
                        importBinaryFileFromOldSiteArchiveFormat(site, session, sizes, pathMapping, zis, zipentry, name);
                    } else if (name.equals(CATEGORIES_XML)) {
                        catProps = importCategoriesAndGetUuidProps(zis, categoriesImportHandler);
                    } else if (name.equals(DEFINITIONS_CND)) {
                        reg = getSafeNodeTypeRegistryFromLegacyArchive(file, legacyImport, zis, zipentry);
                    } else if (name.equals(DEFINITIONS_MAP)) {
                        mapping = new DefinitionsMapping();
                        mapping.load(zis);

                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }
        }

        // Import legacy content from 5.x and 6.x
        if (legacyImport) {
            performLegacyImport(file, site, infos, legacyMappingFilePath, legacyDefinitionsFilePath, session, timerSite, fileList, reg, mapping);
        }
        return catProps;
    }

    private NodeTypeRegistry getSafeNodeTypeRegistryFromLegacyArchive(Resource file, boolean legacyImport, ZipInputStream zis, ZipEntry zipentry) throws IOException {
        NodeTypeRegistry reg;
        reg = new NodeTypeRegistry(); // this is fishy: a new instance is created here when NodeTypeRegistry is meant to be used as a singleton
        try {
            for (Map.Entry<String, File> entry : NodeTypeRegistry.getSystemDefinitionsFiles().entrySet()) {
                reg.addDefinitionsFile(entry.getValue(), entry.getKey());
            }
            if (legacyImport) {
                JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(new InputStreamReader(zis, StandardCharsets.UTF_8), zipentry.getName(),
                        file.getURL().getPath(), reg);
                r.parse();
            } else {
                reg.addDefinitionsFile(new InputStreamResource(zis, zipentry.getName()), file.getURL().getPath());
            }
        } catch (RepositoryException | ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return reg;
    }

    private void importBinaryFileFromOldSiteArchiveFormat(JahiaSite site, JCRSessionWrapper session, Map<String, Long> sizes, Map<String, String> pathMapping, ZipInputStream zis, ZipEntry zipentry, String name) throws RepositoryException {
        if (!sizes.containsKey(REPOSITORY_XML) && !sizes.containsKey(FILESACL_XML)) {
            // No repository descriptor - Old import format only
            name = convertOldEntryName(pathMapping, name);
            if (!zipentry.isDirectory()) {
                try {
                    String filename = name.substring(name.lastIndexOf('/') + 1);
                    ensureFile(session, name, zis, JCRContentUtils.getMimeType(filename), site);
                } catch (Exception e) {
                    logger.error("Cannot upload file " + zipentry.getName(), e);
                }
            } else {
                ensureDir(session, name, site);
            }
        }
    }

    private String convertOldEntryName(Map<String, String> pathMapping, String name) {
        name = "/" + name;
        if (name.startsWith("/content/sites")) {
            name = pathMapping.get("/")
                    + StringUtils.stripStart(name.replaceFirst("/content/sites/[^/]+/files/", ""), "/");
        } else if (name.startsWith("/users")) {
            Matcher m = Pattern.compile("/users/([^/]+)(/.*)?").matcher(name);
            if (m.matches()) {
                name = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule()
                        .getPathForUsername(m.group(1));
                name = name + "/files" + ((m.group(2) != null) ? m.group(2) : "");
            }
        } else if (name.startsWith("/content/users")) {
            Matcher m = Pattern.compile("/content/users/([^/]+)(/.*)?").matcher(name);
            if (m.matches()) {
                name = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule()
                        .getPathForUsername(m.group(1));
                name = name + ((m.group(2) != null) ? m.group(2) : "");
            }
        } else {
            name = pathMapping.get("/") + StringUtils.stripStart(name, "/");
        }
        return name;
    }

    private void importRepositoryDescriptorIfPresentInArchive(Resource file, JahiaSite site, JCRSessionWrapper session, Map<String, String> pathMapping) throws IOException, RepositoryException {
        long timer;
        // Parse import file to detect sites
        ZipInputStream zis = getZipInputStream(file);
        try {
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML)) {
                    timer = System.currentTimeMillis();
                    logger.info("Start importing {}", REPOSITORY_XML);

                    DocumentViewValidationHandler h = new DocumentViewValidationHandler();
                    h.setSession(session);
                    List<ImportValidator> validators = new ArrayList<>();
                    SitesValidator sitesValidator = new SitesValidator();
                    validators.add(sitesValidator);
                    h.setValidators(validators);
                    handleImport(zis, h, name);

                    Map<String, Properties> sites = ((SitesValidatorResult) sitesValidator.getResult()).getSitesProperties();
                    for (String s : sites.keySet()) {
                        // Only the first site returned is mapped (if its not the systemsite, which is always the same key)
                        if (!s.equals("systemsite") && !site.getSiteKey().equals("systemsite")) {
                            // Map to the new sitekey
                            pathMapping.put("/sites/" + s + "/", "/sites/" + site.getSiteKey() + "/");
                            break;
                        }
                    }

                    if (logger.isInfoEnabled()) {
                        logger.info("Done importing " + REPOSITORY_XML + " in {}",
                                DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
                    }
                    break;
                }
                zis.closeEntry();
            }
        } finally {
            closeInputStream(zis);
        }

        importZip(null, file, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, Sets.newHashSet(USERS_XML, CATEGORIES_XML), true);
    }

    private void importSitePropertiesIfPresentInArchive(Resource file, JahiaSite site, JCRSessionWrapper session, Map<String, Long> sizes) throws IOException {
        if (sizes.containsKey(SITE_PROPERTIES)) {
            ZipInputStream zis = getZipInputStream(file);
            try {
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    if (zipentry.getName().equals(SITE_PROPERTIES)) {
                        importSiteProperties(zis, site, session);
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }
        }
    }

    private List<String[]> importUsersIfPresentInArchive(Resource file, UsersImportHandler usersImportHandler, List<String[]> userProps, Map<String, Long> sizes) throws IOException {
        if (sizes.containsKey(USERS_XML)) {
            // Import users first
            ZipInputStream zis = getZipInputStream(file);
            try {
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    if (zipentry.getName().equals(USERS_XML)) {
                        userProps = importUsers(zis, usersImportHandler);
                        break;
                    }
                }
            } finally {
                closeInputStream(zis);
            }
        }
        return userProps;
    }

    private Map<String, String> getRegisteredModulesPathMapping(JCRSessionWrapper session) {
        Map<String, String> pathMapping = session.getPathMapping();
        for (JahiaTemplatesPackage pkg : templatePackageRegistry.getRegisteredModules().values()) {
            String key = "/modules/" + pkg.getId() + "/";
            pathMapping.computeIfAbsent(key, s -> "/modules/" + pkg.getId() + "/" + pkg.getVersion() + "/");
        }
        return pathMapping;
    }

    @SuppressWarnings({"java:S107", "java:S3776"})
    private void performLegacyImport(Resource file, JahiaSite site, Map<Object, Object> infos, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath, JCRSessionWrapper session, long timerSite, List<String> fileList, NodeTypeRegistry reg, DefinitionsMapping mapping) throws IOException, RepositoryException {
        long timerLegacy = System.currentTimeMillis();
        final String originatingJahiaRelease = (String) infos.get("originatingJahiaRelease");
        logger.info("Start legacy import, source version is {}", originatingJahiaRelease);
        if (legacyMappingFilePath != null) {
            mapping = new DefinitionsMapping();
            try (InputStream fileInputStream = legacyMappingFilePath.getInputStream()) {
                mapping.load(fileInputStream);
            }
        }
        if (legacyDefinitionsFilePath != null) {
            reg = getLegacyNodeTypeRegistry(file, legacyDefinitionsFilePath, originatingJahiaRelease);
        }
        // Old import
        JCRNodeWrapper siteFolder = session.getNode("/sites/" + site.getSiteKey());

        ZipInputStream zis = new NoCloseZipInputStream(new BufferedInputStream(file.getInputStream()));
        try {
            int legacyImportHandlerCtnId = 1;
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null)
                    break;
                String name = zipentry.getName();
                if (name.equals(FILESACL_XML)) {
                    logger.info("Importing file " + FILESACL_XML);
                    importFilesAcl(site, file, zis, mapping, fileList);
                } else if (name.startsWith("export")) {
                    logger.info("Importing file {}", name);
                    String languageCode;
                    if (name.indexOf('_') != -1) {
                        languageCode = name.substring(7, name.lastIndexOf('.'));
                    } else {
                        languageCode = site.getLanguagesAsLocales().iterator().next().toString();
                    }
                    zipentry.getSize();

                    LegacyImportHandler importHandler = new LegacyImportHandler(session, siteFolder, reg, mapping, LanguageCodeConverters.languageCodeToLocale(languageCode), infos != null ? originatingJahiaRelease : null, legacyPidMappingTool, legacyImportHandlerCtnId);
                    Map<String, List<String>> references = new LinkedHashMap<>();
                    importHandler.setReferences(references);

                    InputStream documentInput = getDocumentInput(file, site, timerSite, zis, name, languageCode);

                    handleImport(documentInput, importHandler, name);
                    legacyImportHandlerCtnId = importHandler.getCtnId();
                    ReferencesHelper.resolveCrossReferences(session, references);
                    siteFolder.getSession().save(JCRObservationManager.IMPORT);
                }
                zis.closeEntry();
            }
            ReferencesHelper.resolveReferencesKeeper(session);
            siteFolder.getSession().save(JCRObservationManager.IMPORT);
        } finally {
            closeInputStream(zis);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Done legacy import in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerLegacy));
        }
    }

    private InputStream getDocumentInput(Resource file, JahiaSite site, long timerSite, ZipInputStream zis, String name, String languageCode) throws IOException {
        InputStream documentInput = zis;
        if (this.xmlContentTransformers != null && !this.xmlContentTransformers.isEmpty()) {
            documentInput = new ZipInputStream(file.getInputStream());
            while (!name.equals(((ZipInputStream) documentInput).getNextEntry().getName())) ;
            byte[] buffer = new byte[2048];
            final File tmpDirectoryForSite = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-migration"),
                    FastDateFormat.getInstance("yyyy_MM_dd-HH_mm_ss_SSS").format(timerSite) + "_" + site.getSiteKey());
            tmpDirectoryForSite.mkdirs();
            File document = new File(tmpDirectoryForSite, "export_" + languageCode + "_00_extracted.xml");
            try (final OutputStream output = new BufferedOutputStream(new FileOutputStream(document), 2048)) {
                int count = 0;
                while ((count = documentInput.read(buffer, 0, 2048)) > 0) {
                    output.write(buffer, 0, count);
                }
                output.flush();
            }
            documentInput.close();
            for (XMLContentTransformer xct : xmlContentTransformers) {
                document = xct.transform(document, tmpDirectoryForSite);
            }
            documentInput = new FileInputStream(document);
        }
        return documentInput;
    }

    @SuppressWarnings("java:S3776")
    private NodeTypeRegistry getLegacyNodeTypeRegistry(Resource file, Resource legacyDefinitionsFilePath, String originatingJahiaRelease) throws IOException, RepositoryException {
        NodeTypeRegistry reg;
        reg = new NodeTypeRegistry();
        if ("6.1".equals(originatingJahiaRelease)) {
            logger.info("Loading the built in 6.1 definitions before processing the provided custom ones");
            final List<String> builtInLegacyDefs = Arrays.asList(
                    "01-system-nodetypes.cnd",
                    "02-jahiacore-nodetypes.cnd",
                    "03-files-nodetypes.cnd",
                    "04-jahiacontent-nodetypes.cnd",
                    "05-standard-types.cnd",
                    "10-extension-nodetypes.cnd",
                    "11-preferences-nodetypes.cnd"
            );

            for (String builtInLegacyDefsFile : builtInLegacyDefs) {
                try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/jahia/migration/legacyDefinitions/jahia6/" + builtInLegacyDefsFile)) {
                    if (inputStream != null) {
                        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                            final JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(inputStreamReader, builtInLegacyDefsFile,
                                    file.getURL().getPath(), reg);
                            r.parse();
                        } catch (ParseException e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        logger.error("Couldn't load {}", builtInLegacyDefsFile);
                    }
                }
            }
        } else {
            try {
                for (Map.Entry<String, File> entry : NodeTypeRegistry.getSystemDefinitionsFiles().entrySet()) {
                    reg.addDefinitionsFile(entry.getValue(), entry.getKey());
                }
            } catch (ParseException e) {
                logger.error("Cannot parse definitions : " + e.getMessage(), e);
            }
        }
        try (InputStreamReader streamReader = new InputStreamReader(legacyDefinitionsFilePath.getInputStream(), StandardCharsets.UTF_8)) {
            JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(streamReader, legacyDefinitionsFilePath.getFilename(),
                    file.getURL().getPath(), reg);
            r.parse();
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        }
        return reg;
    }

    /**
     * Remove the list of files temporarily expanded to the given folder.
     *
     * @param expandedFolder path to the expanded folder on disk - if null, do nothing
     */
    public void cleanFilesList(File expandedFolder) {
        if (expandedFolder == null) {
            return;
        }
        long timer = System.currentTimeMillis();
        logger.info("Start cleaning files expanded to {}", expandedFolder);
        try {
            FileUtils.deleteDirectory(expandedFolder);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Done file cleanup in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
        }
    }

    public JCRNodeWrapper ensureDir(JCRSessionWrapper session, String path, JahiaSite site) throws RepositoryException {
        JCRNodeWrapper dir;
        try {
            dir = session.getNode(path);

            String current = path;

            while (current.lastIndexOf('/') > 0) {
                JCRNodeWrapper currentNode = session.getNode(current);

                if (Constants.JAHIANT_VIRTUALSITE.equals(currentNode.getPrimaryNodeTypeName())) {
                    if (currentNode.getName().equals(site.getSiteKey())) {
                        break;
                    }
                    String newName = current.substring(0, current.lastIndexOf('/')) + "/" + site.getSiteKey();
                    session.getPathMapping().put(current, newName);
                    path = path.replace(current, newName);

                    return ensureDir(session, path, site);
                }
                int endIndex = current.lastIndexOf('/');
                current = current.substring(0, endIndex);
            }
        } catch (PathNotFoundException pnfe) {
            int endIndex = path.lastIndexOf('/');
            if (endIndex == -1) {
                logger.warn("Cannot create folder {}", path);
                return null;
            }
            JCRNodeWrapper parentDir = ensureDir(session, path.substring(0, endIndex), site);
            if (parentDir == null) {
                return null;
            }
            if (parentDir.isNodeType(Constants.JAHIANT_VIRTUALSITES_FOLDER)) {
                dir = parentDir.getNode(site.getSiteKey());
            } else {
                try {
                    String dirName = path.substring(path.lastIndexOf('/') + 1);
                    if (!StringUtils.isEmpty(dirName)) {
                        session.checkout(parentDir);
                        JCRNodeWrapper createdDir = parentDir.createCollection(dirName);
                        createdDir.saveSession();
                    }
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                }
                dir = session.getNode(path);
                if (logger.isDebugEnabled()) {
                    logger.debug("Folder created {}", path);
                }
            }
        }
        return dir;
    }

    public void ensureFile(JCRSessionWrapper session, String path, InputStream inputStream, String type, JahiaSite destSite) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        try {
            JCRNodeWrapper parentDir = ensureDir(session, path.substring(0, path.lastIndexOf('/')), destSite);
            if (parentDir == null) {
                return;
            }
            if (!parentDir.hasNode(name)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Add file to {}", parentDir.getPath());
                }
                try {
                    if (!parentDir.isCheckedOut()) {
                        session.checkout(parentDir);
                    }
                    JCRNodeWrapper res = parentDir.uploadFile(name, inputStream, type);
                    if (logger.isDebugEnabled()) {
                        logger.debug("File added -> {}", res);
                    }
                    res.saveSession();
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("Try to add file {} - already exists", path);
            }
        } catch (RepositoryException e) {
            logger.debug("Cannot add file", e);
        }

    }

    private void importSiteProperties(final InputStream is, final JahiaSite site, JCRSessionWrapper session) throws IOException {
        if (site.getSiteKey().equals(JahiaSitesService.SYSTEM_SITE_KEY)) {
            return;
        }
        logger.info("Loading properties for site {}", site.getSiteKey());
        long timer = System.currentTimeMillis();

        final Properties p = new Properties();
        p.load(is);
        importSiteProperties(site, p, session);
        logger.info("Done loading properties for site {} in {}", site.getSiteKey(), DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
    }

    private void importFilesAcl(JahiaSite site, Resource file, InputStream is, DefinitionsMapping mapping, List<String> fileList) {
        Map<String, File> filePath = new HashMap<>();
        File temp = null;
        try {
            Path tempPath = Files.createTempDirectory("migration");
            temp = tempPath.toFile();
            ZipInputStream zis = getZipInputStream(file);
            try {
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    String fileName = zipentry.getName();
                    if (!zipentry.isDirectory()) {
                        fileName = fileName.replace('\\', '/');
                        File newFile = new File(temp, fileName);
                        newFile.getParentFile().mkdirs();
                        FileUtils.copyInputStreamToFile(zis, newFile);
                        filePath.put("/" + fileName, newFile);
                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }

            handleImport(is, new FilesAclImportHandler(site, mapping, file, fileList, filePath), file.getFilename());
        } catch (IOException e) {
            logger.error("Cannot extract zip", e);
        } finally {
            FileUtils.deleteQuietly(temp);
        }
    }

    @SuppressWarnings("java:S3776")
    private void importSiteProperties(JahiaSite site, Properties p, JCRSessionWrapper session) {
        Set<Object> keys = p.keySet();
        final Set<String> languages = new HashSet<>();
        final Set<String> inactiveLanguages = new HashSet<>();
        final Set<String> inactiveLiveLanguages = new HashSet<>();
        final Set<String> mandatoryLanguages = new HashSet<>();

        List<String> installedModules = site.getInstalledModules();
        try {
            // site.getInstalledModules() may return outdated data
            installedModules = sitesService.getSiteByKey(site.getSiteKey(), session).getInstalledModules();
        } catch (RepositoryException e) {
            logger.error("Cannot get installed modules ", e);
        }

        String templateSet = site.getTemplateFolder();
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        try {
            if (!installedModules.contains(templateSet)) {
                templateManagerService.installModule(templateManagerService.getAnyDeployedTemplatePackage(templateSet), "/sites/" + site.getSiteKey(), session);
            }
        } catch (RepositoryException e) {
            logger.error("Cannot deploy module " + templateSet, e);
        }

        String defaultLanguage = null;
        String lowestRankLanguage = null;
        int currentRank = 0;

        List<JahiaTemplatesPackage> modules = new ArrayList<>();

        // Build languages set and other properties
        for (Object key : keys) {
            String property = (String) key;
            String value = p.getProperty(property);
            StringTokenizer st = new StringTokenizer(property, ".");
            String firstKey = st.nextToken();

            try {
                switch (firstKey) {
                    case "language":
                        String lang = st.nextToken();

                        if (!languages.contains(lang)) {
                            addNewLanguageToCorrectSet(p, languages, inactiveLanguages, inactiveLiveLanguages, mandatoryLanguages, lang);
                            if (!inactiveLanguages.contains(lang) && (StringUtils.isEmpty(lowestRankLanguage)
                                    || p.containsKey("language." + lang + ".rank"))) {
                                int langRank = NumberUtils.toInt(p
                                        .getProperty("language." + lang + ".rank"));
                                if (currentRank == 0 || langRank < currentRank) {
                                    currentRank = langRank;
                                    lowestRankLanguage = lang;
                                }
                            }
                        }
                        break;
                    case "defaultLanguage":
                        defaultLanguage = value;
                        break;
                    case "mixLanguage":
                        site.setMixLanguagesActive(Boolean.parseBoolean(value));
                        break;
                    case "allowsUnlistedLanguages":
                        site.setAllowsUnlistedLanguages(Boolean.parseBoolean(value));
                        break;
                    case "description":
                        site.setDescription(value);
                        break;
                    case "installedModules":
                        if (!installedModules.contains(value) && !templateSet.equals(value)) {
                            JahiaTemplatesPackage pkg = templateManagerService.getAnyDeployedTemplatePackage(value);
                            if (pkg != null) {
                                modules.add(pkg);
                            } else {
                                logger.info("unable to find module {} in deployed modules", value);
                            }
                        }
                        break;
                    default:
                        if (firstKey.startsWith("defaultSite") && "true".equals(value) && sitesService.getDefaultSite(session) == null) {
                            sitesService.setDefaultSite(site, session);
                        }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot set site property  " + firstKey, e);
            }
        }

        @SuppressWarnings("unchecked")
        Set<String> siteLangs = ListOrderedSet.decorate(new LinkedList<>(languages));
        if (!siteLangs.isEmpty()) {
            site.setLanguages(siteLangs);
            site.setInactiveLanguages(inactiveLanguages);
            site.setInactiveLiveLanguages(inactiveLiveLanguages);
            site.setMandatoryLanguages(mandatoryLanguages);
            if (defaultLanguage == null) {
                defaultLanguage = StringUtils.isEmpty(lowestRankLanguage) ? siteLangs.iterator().next() : lowestRankLanguage;
            }
            site.setDefaultLanguage(defaultLanguage);
        } else {
            logger.error("Unable to find site languages in the provided " + SITE_PROPERTIES + " descriptor. Skip importing site settings.");
        }

        try {
            templateManagerService.installModules(modules, "/sites/" + site.getSiteKey(), session);

            session.save();
        } catch (RepositoryException e) {
            logger.error("Cannot deploy module " + modules, e);
        }

    }

    private void addNewLanguageToCorrectSet(Properties p, Set<String> languages, Set<String> inactiveLanguages, Set<String> inactiveLiveLanguages, Set<String> mandatoryLanguages, String lang) {
        languages.add(lang);
        if (!Boolean.parseBoolean(p.getProperty("language." + lang + ".activated", "true"))) {
            inactiveLiveLanguages.add(lang);
        }
        if (Boolean.parseBoolean(p.getProperty("language." + lang + ".disabledCompletely", "false"))) {
            inactiveLanguages.add(lang);
            languages.remove(lang);
        }
        if (Boolean.parseBoolean(p.getProperty("language." + lang + ".mandatory", "false"))) {
            mandatoryLanguages.add(lang);
        }
    }

    private List<String[]> importCategoriesAndGetUuidProps(InputStream is, CategoriesImportHandler importHandler) {
        handleImport(is, importHandler, null);
        return importHandler.getUuidProps();
    }

    @Override
    public void importCategories(Category rootCategory, InputStream is) {
        CategoriesImportHandler importHandler = new CategoriesImportHandler();
        importHandler.setRootCategory(rootCategory);
        importCategoriesAndGetUuidProps(is, importHandler);
    }

    @Override
    public List<String[]> importUsers(final File file) throws IOException, RepositoryException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(file));) {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String[]>>() {

                @Override
                public List<String[]> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String[]> l = importUsers(is, new UsersImportHandler(session), file.getName());
                    session.save();
                    return l;
                }
            });
        }
    }

    private List<String[]> importUsers(InputStream is, UsersImportHandler importHandler) {
        return importUsers(is, importHandler, USERS_XML);
    }

    private List<String[]> importUsers(InputStream is, UsersImportHandler importHandler, String fileName) {
        long timer = System.currentTimeMillis();
        logger.info("Start importing users");
        handleImport(is, importHandler, fileName);
        logger.info("Done importing users in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
        return importHandler.getUuidProps();
    }

    private void handleImport(InputStream is, DefaultHandler h, String fileName) {
        try {
            SAXParser parser = JahiaSAXParserFactory.newInstance().newSAXParser();

            parser.parse(is, h);
            if (h instanceof DocumentViewImportHandler) {
                DocumentViewImportHandler dh = (DocumentViewImportHandler) h;
                if (!dh.getMissingDependencies().isEmpty()) {
                    for (String s : dh.getMissingDependencies()) {
                        logger.error("Dependency not declared : {} (set debug on DocumentViewImportHandler for more details)", s);
                    }
                }
            }
        } catch (SAXParseException e) {
            logger.error("Cannot import - File contains invalid XML", e);
            throw new RuntimeException("Cannot import " + (fileName != null ? fileName : "") + " file as it contains invalid XML", e);
        } catch (Exception e) {
            logger.error("Cannot import", e);
            throw new RuntimeException("Cannot import " + (fileName != null ? fileName : "") + " file", e);
        }
    }

    /**
     * Detects the type of the import from the provided stream.
     *
     * @param is the input stream to read imported content from
     * @return the type of the import from the provided stream
     * @see XMLFormatDetectionHandler
     */
    public int detectXmlFormat(InputStream is) {
        XMLFormatDetectionHandler handler = new XMLFormatDetectionHandler();
        try {
            SAXParser parser = JahiaSAXParserFactory.newInstance().newSAXParser();

            parser.parse(is, handler);
        } catch (Exception e) {
        }
        return handler.getType();
    }

    @Override
    public void importXML(final String parentNodePath, InputStream content, final int rootBehavior) throws IOException, RepositoryException, JahiaException {
        final JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        final HashMap<String, List<String>> references = new HashMap<>();
        importXML(parentNodePath, content, rootBehavior, references, session);
        ReferencesHelper.resolveCrossReferences(session, references);
    }

    /**
     * Imports the provided content.
     *
     * @param parentNodePath the node to use as a parent for the import
     * @param content        the input stream to read content from
     * @param rootBehavior   the root behaviour (see {@link DocumentViewImportHandler})
     * @param session        current JCR session
     * @throws IOException         in case of an I/O operation error
     * @throws RepositoryException in case of a JCR-related error
     * @throws JahiaException      in case of a processing error
     */
    public void importXML(final String parentNodePath, InputStream content, final int rootBehavior, final Map<String, List<String>> references, JCRSessionWrapper session) throws IOException, RepositoryException, JahiaException {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("import-xml-", ".xml");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                IOUtils.copy(content, fileOutputStream);
            }
            int format;
            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile))) {
                format = detectXmlFormat(inputStream);
            }

            switch (format) {
                case XMLFormatDetectionHandler.JCR_DOCVIEW: {
                    try (InputStream is = new BufferedInputStream(new FileInputStream(tempFile))) {
                        session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, rootBehavior, null, references);
                        session.save(JCRObservationManager.IMPORT);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    }
                    break;
                }

                case XMLFormatDetectionHandler.USERS: {
                    importUsers(tempFile);
                    break;
                }
                case XMLFormatDetectionHandler.CATEGORIES: {
                    Category cat = categoryService.getCategoryByPath(parentNodePath);
                    try (InputStream is = new BufferedInputStream(new FileInputStream(tempFile))) {
                        importCategories(cat, is);
                    }
                    break;
                }
            }
        } finally {
            if (tempFile != null) {
                Files.delete(tempFile.toPath());
            }
        }

    }

    @Override
    public void importZip(final String parentNodePath, final Resource file, final int rootBehavior) throws IOException, RepositoryException {
        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null);
        importZip(parentNodePath, file, rootBehavior, session);
    }

    /**
     * Validates a JCR content import file in document format and returns expected failures.
     *
     * @param session          current JCR session instance
     * @param is               the input stream with a JCR content in document format
     * @param contentType      the content type for the content
     * @param installedModules the list of installed modules, where the first element is a template set name
     * @return the validation result
     * @since Jahia 6.6
     */
    @Override
    @SuppressWarnings("java:S2093")
    public ValidationResults validateImportFile(JCRSessionWrapper session, InputStream is, String contentType, List<String> installedModules) {
        DocumentViewValidationHandler documentViewValidationHandler = (DocumentViewValidationHandler) SpringContextSingleton
                .getBean("DocumentViewValidationHandler");
        if (installedModules != null && !installedModules.isEmpty()) {
            documentViewValidationHandler.initDependencies(
                    installedModules.get(0),
                    installedModules.size() > 1 ? installedModules.subList(1,
                            installedModules.size()) : null);
        }
        documentViewValidationHandler.setSession(session);
        if (contentType.equals(APPLICATION_ZIP)) {
            validateImportZip(is, documentViewValidationHandler);
        } else {
            try {
                handleImport(is, documentViewValidationHandler, null);
            } catch (Exception e) {
                final ValidationResults results = new ValidationResults();
                results.addResult(new ValidationResult.FailedValidationResult(e));
                return results;
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return documentViewValidationHandler.getResults();
    }

    private void validateImportZip(InputStream is, DocumentViewValidationHandler documentViewValidationHandler) {
        final long maxSize = SettingsBean.getInstance().getLong("zipFile.maxSize", 100L * ONE_MB);
        final int maxEntries = SettingsBean.getInstance().getInt("zipFile.maxEntriesCount", 1024);
        final int BUFFER = 4096;

        File tempFile = null;
        ZipFile zipFile = null;

        try {
            // Copy to temp file to validate filename extraction targets
            tempFile = File.createTempFile("import", ".zip");
            FileUtils.copyToFile(is, tempFile);
            zipFile = new ZipFile(tempFile);

            int entries = 0;
            long totalSize = 0;
            for (Enumeration<ZipArchiveEntry> e = zipFile.getEntries(); e.hasMoreElements();) {
                if (entries++ > maxEntries) {
                    throw new IllegalStateException("Too many files/directories to unzip.");
                }

                ZipArchiveEntry entry = e.nextElement();
                String name = validateZipName(entry.getName());
                if (entry.isDirectory()) {
                    continue;
                }

                // Read file entry and check total size
                try (InputStream eis = zipFile.getInputStream(entry)) {
                    byte[] data = new byte[BUFFER];
                    for (int size = 0; (size = eis.read(data, 0, BUFFER)) != -1;) {
                        totalSize += size;
                        if (totalSize > maxSize) {
                            throw new IllegalStateException("Zip file being extracted is too big.");
                        }
                    }
                }

                if (name.endsWith("xml")) {
                    // Get a new input stream to parse xml entry
                    try (InputStream zis = zipFile.getInputStream(entry)) {
                        handleImport(zis, documentViewValidationHandler, name);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Cannot import", e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
            IOUtils.closeQuietly(zipFile);
        }
    }

    private String validateZipName(String filename) throws java.io.IOException {
        String canonicalPath = new File(filename).getCanonicalPath();
        String canonicalID = new File(".").getCanonicalPath();

        if (canonicalPath.startsWith(canonicalID) && canonicalPath.length() > canonicalID.length()) {
            return canonicalPath.substring(canonicalID.length() + 1);
        } else {
            throw new IllegalStateException("File is outside extraction target directory.");
        }
    }

    @Override
    public void importZip(String parentNodePath, Resource file, int rootBehaviour, JCRSessionWrapper session)
            throws IOException, RepositoryException {
        importZip(parentNodePath, file, rootBehaviour, session, null, true);
    }

    /**
     * Imports the content of the specified resource.
     *
     * @param parentNodePath the node to use as a parent for the import
     * @param file           the file with the content to be imported
     * @param rootBehaviour  the root behaviour (see {@link DocumentViewImportHandler})
     * @param session        current JCR session
     * @param filesToIgnore  set of files to be skipped
     * @throws IOException         in case of an I/O operation error
     * @throws RepositoryException in case of a JCR-related error
     */
    @Override
    @SuppressWarnings("java:S3776")
    public void importZip(String parentNodePath, Resource file, int rootBehaviour, final JCRSessionWrapper session, Set<String> filesToIgnore, boolean useReferenceKeeper)
            throws IOException, RepositoryException {
        long timer = System.currentTimeMillis();
        if (filesToIgnore == null) {
            filesToIgnore = Collections.<String>emptySet();
        }
        logger.info("Start importing file {} into path {} ", file, parentNodePath != null ? parentNodePath : "/");

        Map<String, Long> sizes = new HashMap<>();
        List<String> fileList = new ArrayList<>();

        Map<String, List<String>> references = new HashMap<>();

        File expandedFolder = getFileList(file, sizes, fileList, false);
        try {
            Map<String, String> pathMapping = getRegisteredModulesPathMapping(session);

            boolean importLive = sizes.containsKey(LIVE_REPOSITORY_XML);

            List<String> liveUuids = null;
            if (importLive) {
                // Import live content
                ZipInputStream zis = getZipInputStream(file);
                try {
                    ZipEntry zipentry;
                    while ((zipentry = zis.getNextEntry()) != null) {
                        String name = zipentry.getName();
                        if (name.equals(LIVE_REPOSITORY_XML) && !filesToIgnore.contains(name)) {
                            long timerLive = System.currentTimeMillis();
                            logger.info("Start importing " + LIVE_REPOSITORY_XML);

                            final DocumentViewImportHandler documentViewImportHandler = getDocumentViewImportHandlerForLiveWorkspace(parentNodePath, file, rootBehaviour, session, fileList, references, zis);

                            if (rootBehaviour == DocumentViewImportHandler.ROOT_BEHAVIOUR_RENAME) {
                                // Use path mapping to get new name
                                rootBehaviour = DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE;
                            }

                            logger.debug("Resolving cross-references for " + LIVE_REPOSITORY_XML);

                            ReferencesHelper.resolveCrossReferences(session, references, useReferenceKeeper, true);

                            logger.debug("Saving JCR session for " + LIVE_REPOSITORY_XML);

                            session.save(JCRObservationManager.IMPORT);

                            liveUuids = documentViewImportHandler.getUuids();

                            logger.debug("Publishing...");

                            final JCRPublicationService publicationService = ServicesRegistry.getInstance().getJCRPublicationService();
                            final List<String> toPublish = documentViewImportHandler.getUuids();

                            JCRObservationManager.doWithOperationType(null, JCRObservationManager.IMPORT, new JCRCallback<Object>() {
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    publicationService.publish(toPublish, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, false, false, null);
                                    return null;
                                }
                            });

                            logger.debug("publishing done");

                            String label = "published_at_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Calendar.getInstance().getTime());
                            JCRVersionService.getInstance().addVersionLabel(toPublish, label, Constants.LIVE_WORKSPACE);

                            logger.info("Done importing " + LIVE_REPOSITORY_XML + " in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerLive));

                            break;
                        }
                        zis.closeEntry();

                    }
                } catch (RepositoryException e) {
                    throw e;
                } catch (Exception e) {
                    logger.error("Cannot import", e);
                } finally {
                    closeInputStream(zis);
                }
            }

            importRepositoryContent(parentNodePath, file, rootBehaviour, session, filesToIgnore, fileList, references, importLive, liveUuids);

            // during import/export, never try to resolve the references between templates and site.
            resolveReferences(session, useReferenceKeeper, references);

            if (importLive) {
                importUserGeneratedContent(parentNodePath, file, rootBehaviour, fileList, pathMapping);
            }
        } finally {
            cleanFilesList(expandedFolder);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Done importing file {} in {}", file, DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
        }
    }

    private void importUserGeneratedContent(String parentNodePath, Resource file, int rootBehaviour, List<String> fileList, Map<String, String> pathMapping) throws IOException {
        // Import user generated content
        ZipInputStream zis = getZipInputStream(file);
        try {
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                String name = zipentry.getName();
                if (name.equals(LIVE_REPOSITORY_XML) && jcrStoreService.getSessionFactory().getCurrentUser() != null) {
                    long timerUGC = System.currentTimeMillis();
                    logger.info("Start importing user generated content");
                    JCRSessionWrapper liveSession = jcrStoreService.getSessionFactory().getCurrentUserSession(
                            "live", null, null);
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(
                            liveSession, parentNodePath, file, fileList);

                    documentViewImportHandler.setImportUserGeneratedContent(true);
                    documentViewImportHandler.setRootBehavior(rootBehaviour);
                    documentViewImportHandler.setBaseFilesPath("/live-content");
                    documentViewImportHandler.setAttributeProcessors(attributeProcessors);
                    liveSession.getPathMapping().putAll(pathMapping);
                    handleImport(zis, documentViewImportHandler, LIVE_REPOSITORY_XML);

                    logger.debug("Saving JCR session for UGC");

                    liveSession.save(JCRObservationManager.IMPORT);
                    if (logger.isInfoEnabled()) {
                        logger.info("Done importing user generated content in {}",
                                DateUtils.formatDurationWords(System.currentTimeMillis() - timerUGC));
                    }
                    break;
                }
                zis.closeEntry();

            }
        } catch (Exception e) {
            logger.error("Cannot import", e);
        } finally {
            closeInputStream(zis);
        }
    }

    @SuppressWarnings({"java:S107", "java:S3776"})
    private void importRepositoryContent(String parentNodePath, Resource file, int rootBehaviour, JCRSessionWrapper session, Set<String> filesToIgnore, List<String> fileList, Map<String, List<String>> references, boolean importLive, List<String> liveUuids) throws IOException, RepositoryException {
        // Import repository content
        ZipInputStream zis = getZipInputStream(file);
        try {
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML) && !filesToIgnore.contains(name)) {
                    importRepositoryXMLFile(parentNodePath, file, rootBehaviour, session, fileList, references, zis, importLive, liveUuids);
                } else if (name.endsWith(".xml") && !name.equals(REPOSITORY_XML) && !name.equals(LIVE_REPOSITORY_XML) && !filesToIgnore.contains(name) && !name.contains("/")) {
                    long timerOther = System.currentTimeMillis();
                    logger.info("Start importing {}", name);
                    String thisPath = (parentNodePath != null ? getParentNodePath(parentNodePath) : "") + StringUtils.substringBefore(name, ".xml");
                    importXML(thisPath, zis, rootBehaviour, references, session);
                    logger.debug("Saving JCR session for {}", name);
                    session.save(JCRObservationManager.IMPORT);
                    if (logger.isInfoEnabled()) {
                        logger.info("Done importing {} in {}", name, DateUtils.formatDurationWords(System.currentTimeMillis() - timerOther));
                    }
                }
                zis.closeEntry();
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Cannot import", e);
        } finally {
            closeInputStream(zis);
        }
    }

    private void resolveReferences(JCRSessionWrapper session, boolean useReferenceKeeper, Map<String, List<String>> references) throws RepositoryException {
        RenderContext r = TemplateModuleInterceptor.renderContextThreadLocal.get();
        TemplateModuleInterceptor.renderContextThreadLocal.remove();
        ReferencesHelper.resolveCrossReferences(session, references, useReferenceKeeper);
        TemplateModuleInterceptor.renderContextThreadLocal.set(r);
        session.save(JCRObservationManager.IMPORT);
    }

    @SuppressWarnings("java:S107")
    private void importRepositoryXMLFile(String parentNodePath, Resource file, int rootBehaviour, JCRSessionWrapper session, List<String> fileList, Map<String, List<String>> references, ZipInputStream zis, boolean importLive, List<String> liveUuids) throws IOException, RepositoryException {
        long timerDefault = System.currentTimeMillis();
        logger.info("Start importing " + REPOSITORY_XML);
        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);
        if (importLive) {
            // Restore publication status
            Set<String> props = new HashSet<>(documentViewImportHandler.getPropertiesToSkip());
            props.remove(Constants.LASTPUBLISHED);
            props.remove(Constants.LASTPUBLISHEDBY);
            props.remove(Constants.PUBLISHED);
            documentViewImportHandler.setPropertiesToSkip(props);
            documentViewImportHandler.setEnforceUuid(true);
            documentViewImportHandler.setUuidBehavior(DocumentViewImportHandler.IMPORT_UUID_COLLISION_MOVE_EXISTING);
            documentViewImportHandler.setReplaceMultipleValues(true);
            documentViewImportHandler.setRemoveMixins(true);
        }
        documentViewImportHandler.setReferences(references);
        documentViewImportHandler.setRootBehavior(rootBehaviour);
        documentViewImportHandler.setAttributeProcessors(attributeProcessors);
        handleImport(zis, documentViewImportHandler, REPOSITORY_XML);

        if (importLive && liveUuids != null) {
            liveUuids.removeAll(documentViewImportHandler.getUuids());
            Collections.reverse(liveUuids);
            for (String uuid : liveUuids) {
                // Uuids have been imported in live but not in default : need to be removed
                try {
                    JCRNodeWrapper nodeToRemove = session.getNodeByIdentifier(uuid);
                    nodeToRemove.remove();
                } catch (ItemNotFoundException | InvalidItemStateException ex) {
                    logger.debug("Node to remove has already been removed", ex);
                }
            }
        }
        logger.debug("Saving JCR session for {}", REPOSITORY_XML);
        session.save(JCRObservationManager.IMPORT);
        if (logger.isInfoEnabled()) {
            logger.info("Done importing {} in {}", REPOSITORY_XML, DateUtils.formatDurationWords(System.currentTimeMillis() - timerDefault));
        }
    }

    @NotNull
    private DocumentViewImportHandler getDocumentViewImportHandlerForLiveWorkspace(String parentNodePath, Resource file, int rootBehaviour, JCRSessionWrapper session, List<String> fileList, Map<String, List<String>> references, ZipInputStream zis) throws IOException, RepositoryException {
        final DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);

        documentViewImportHandler.setReferences(references);
        documentViewImportHandler.setRootBehavior(rootBehaviour);
        documentViewImportHandler.setBaseFilesPath("/live-content");
        documentViewImportHandler.setAttributeProcessors(attributeProcessors);

        Set<String> props = new HashSet<>(documentViewImportHandler.getPropertiesToSkip());
        props.remove(Constants.LASTPUBLISHED);
        props.remove(Constants.LASTPUBLISHEDBY);
        props.remove(Constants.PUBLISHED);
        documentViewImportHandler.setPropertiesToSkip(props);
        handleImport(zis, documentViewImportHandler, LIVE_REPOSITORY_XML);

        logger.debug("Saving JCR session for " + LIVE_REPOSITORY_XML);

        session.save(JCRObservationManager.IMPORT);
        return documentViewImportHandler;
    }

    @NotNull
    private String getParentNodePath(String parentNodePath) {
        return parentNodePath + (parentNodePath.endsWith("/") ? "" : "/");
    }

    /**
     * Gets the list of files from the given ZIP file.
     * <p>
     * If expandImportedFilesOnDiskPath is set to true (in jahia.properties), then
     * also expand the files to a temporary directory if it is not already expanded.
     *
     * @param file       ZIP file with content to be imported
     * @param sizes      collection holding sizes of uncompressed file elements
     * @param fileList   collection into which the files from ZIP file will be added
     * @param forceClean
     * @return null if files were not expanded to disk (it is just optional), otherwise path to temporary local folder
     * @throws IOException
     */
    public File getFileList(Resource file, Map<String, Long> sizes, List<String> fileList, boolean forceClean) throws IOException {
        File expandedFolder = getExpandedFolder(file, forceClean);
        ZipInputStream zis = getZipInputStream(file);
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) {
                    break;
                }
                String name = zipentry.getName().replace('\\', '/');
                if (expandedFolder != null) {
                    expandZipEntryInExpandedFolderTarget(expandedFolder, zis, zipentry, name);
                }
                storeFileSizeInSizesMap(sizes, zis, zipentry, name);
                if (name.contains("/")) {
                    fileList.add("/" + name);
                }
                zis.closeEntry();
            }
        } finally {
            closeInputStream(zis);
        }
        return expandedFolder;
    }

    private void storeFileSizeInSizesMap(Map<String, Long> sizes, ZipInputStream zis, ZipEntry zipentry, String name) throws IOException {
        if (name.endsWith(".xml")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(zis))) {
                long i = 0;
                while (br.readLine() != null) {
                    i++;
                }
                sizes.put(name, i);
            }
        } else {
            sizes.put(name, zipentry.getSize());
        }
    }

    private File getExpandedFolder(Resource file, boolean forceClean) throws IOException {
        File expandedFolder = null;
        if (expandImportedFilesOnDisk) {
            final File expandFolder = getExpandFolder(file, expandImportedFilesOnDiskPath);
            if (forceClean) {
                FileUtils.deleteDirectory(expandFolder);
            }
            if (!expandFolder.exists()) {
                FileUtils.forceMkdir(expandFolder);
                expandedFolder = expandFolder;
            }
        }
        return expandedFolder;
    }

    private void expandZipEntryInExpandedFolderTarget(File expandedFolder, ZipInputStream zis, ZipEntry zipentry, String name) throws IOException {
        final File importedFile = new File(expandedFolder + File.separator + name);
        if (!importedFile.getCanonicalPath().startsWith(expandedFolder.getCanonicalPath() + File.separator)) {
            throw new IOException("Zip entry is outside of the 'expandedFolder' directory. Potential Zip file attack averted.");
        }
        if (zipentry.isDirectory()) {
            FileUtils.forceMkdir(importedFile);
        } else {
            long timer = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("Expanding {} into {}", zipentry.getName(), importedFile);
            }
            FileUtils.forceMkdir(importedFile.getParentFile());
            try (final OutputStream output = new BufferedOutputStream(new FileOutputStream(importedFile), 1024 * 64)) {
                IOUtils.copyLarge(zis, output);
                if (logger.isDebugEnabled()) {
                    logger.debug("Expanded {} in {}", zipentry.getName(), DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
                }
            }
        }
    }

    public static File getExpandFolder(Resource file, String expandImportedFilesOnDiskPath) throws IOException {
        return new File(expandImportedFilesOnDiskPath + File.separator + "import-" + Base64.getEncoder().encodeToString(file.getURL().toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void closeInputStream(ZipInputStream zis) throws IOException {
        if (zis instanceof NoCloseZipInputStream) {
            ((NoCloseZipInputStream) zis).reallyClose();
        } else {
            zis.close();
        }
    }

    private ZipInputStream getZipInputStream(Resource file) throws IOException {
        ZipInputStream zis;
        if (!file.isReadable() && file instanceof FileSystemResource) {
            zis = new DirectoryZipInputStream(file.getFile());
        } else {
            zis = new NoCloseZipInputStream(new BufferedInputStream(file.getInputStream()));
        }
        return zis;
    }

    /**
     * Injects an instance of the category service
     *
     * @param categoryService category service instance
     */
    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setXmlContentTransformers(final List<XMLContentTransformer> xmlContentTransformers) {
        this.xmlContentTransformers = xmlContentTransformers;
    }

    public void setLegacyPidMappingTool(LegacyPidMappingTool legacyPidMappingTool) {
        this.legacyPidMappingTool = legacyPidMappingTool;
    }

    public void setPostImportPatcher(PostImportPatcher postImportPatcher) {
        this.postImportPatcher = postImportPatcher;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry templatePackageRegistry) {
        this.templatePackageRegistry = templatePackageRegistry;
    }

    private String getDuration(long start) {
        return DateUtils.formatDurationWords(System.currentTimeMillis() - start);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("exportContext") && event.getSource() instanceof DocumentViewExporter) {
            ExportContext exportContext = (ExportContext) event.getNewValue();
            DocumentViewExporter documentViewExporter = (DocumentViewExporter) event.getSource();
            exportContext.setExportIndex(exportContext.getExportIndex() + 1);
            logger.debug("Index: {}}, Exporting  : {}", exportContext.getExportIndex(), exportContext.getActualPath());

            // this will show the percentage of export done by 10% increment will start by 10 and end by 90
            long currentStep = exportContext.getExportIndex() * 10L / exportContext.getNodesToExport();
            if (currentStep > exportContext.getStep() &&
                    exportContext.getStep() < 9) {
                exportContext.setStep(currentStep);
                logger.info("Export {}%", exportContext.getStep() * 10);
                documentViewExporter.setExportContext(exportContext);
            }
        }
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ImportExportBaseService INSTANCE = new ImportExportBaseService();
    }

    class ImportFileObserver implements Observer {

        public ImportFileObserver(String path,
                                  boolean checkDate,
                                  long interval,
                                  boolean fileOnly)
                throws JahiaException {
            if (fileWatcherService != null) {
                try {
                    fileWatcherService.addFileWatcher(path, path, checkDate, interval, fileOnly);
                    fileWatcherService.registerObserver(path, this);
                    fileWatcherService.startFileWatcher(path);
                } catch (JahiaException e) {
                    throw new JahiaInitializationException("ImportObserver::init failed ", e);
                }
            }
        }

        @Override
        public void update(Observable observable, Object args) {
            synchronized (args) {
                @SuppressWarnings("unchecked") final List<File> files = (List<File>) args;
                if (!files.isEmpty()) {
                    JahiaUser user = JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser();

                    if (SettingsBean.getInstance().isProcessingServer()) {
                        for (File file : files) {
                            if (file.isFile()) {
                                try {
                                    logger.info("Detected new file to import: [{}]", file.toPath());
                                    // move file out of file watcher to avoid multiple imports of the same file
                                    Path target = Paths.get(SettingsBean.getInstance().getTmpContentDiskPath()).resolve(file.getName());
                                    Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                                    JobDetail jobDetail = BackgroundJob.createJahiaJob("Import dropped site zip file job", SiteImportJob.class);
                                    logger.info("Import file moved to: [{}]", target);

                                    // start import job
                                    logger.info("Scheduling import job for file: [{}] (This file will be definitely remove at the end of the job execution)", target);
                                    JobDataMap jobDataMap = jobDetail.getJobDataMap();
                                    jobDataMap.put(BackgroundJob.JOB_USERKEY, user.getUserKey());
                                    jobDataMap.put(SiteImportJob.FILE_PATH, target.toString());
                                    jobDataMap.put(SiteImportJob.DELETE_FILE, true);
                                    schedulerService.scheduleJobNow(jobDetail);
                                } catch (SchedulerException | IOException e) {
                                    logger.error("Cannot import file for " + file.getPath(), e);
                                }
                            }
                        }
                    } else {
                        try {
                            JCRSessionFactory.getInstance().setCurrentUser(user);
                            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, new JCRCallback<Object>() {

                                @Override
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    JCRNodeWrapper dest = session.getNode("/imports");
                                    for (File file : files) {
                                        try {
                                            try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                                                dest.uploadFile(file.getName(), is, JCRContentUtils.getMimeType(file.getName()));
                                            }
                                        } catch (Exception t) {
                                            logger.error("file observer error : ", t);
                                        }
                                    }
                                    session.save();
                                    return null;
                                }
                            });
                        } catch (RepositoryException e) {
                            logger.error("error", e);
                        } finally {
                            JCRSessionFactory.getInstance().setCurrentUser(null);
                        }
                        for (File file : files) {
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    private static class OrderedProperties extends Properties {

        private static final long serialVersionUID = -2418536708883832686L;
        private final List<Object> keys = new ArrayList<>();

        @Override
        public synchronized Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        @Override
        public Enumeration<Object> keys() {
            return Collections.enumeration(keys);
        }

        @Override
        public synchronized boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            OrderedProperties that = (OrderedProperties) o;
            return keys.equals(that.keys);
        }

        @Override
        public synchronized int hashCode() {
            return Objects.hash(super.hashCode(), keys);
        }
    }
}

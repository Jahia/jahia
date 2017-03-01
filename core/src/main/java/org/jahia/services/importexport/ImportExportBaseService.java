/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.sf.saxon.TransformerFactoryImpl;
import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.io.Charsets;
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
import org.jahia.exceptions.JahiaInitializationException;
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
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.DateUtils;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.jahia.utils.Url;
import org.jahia.utils.xml.JahiaSAXParserFactory;
import org.jahia.utils.zip.DirectoryZipInputStream;
import org.jahia.utils.zip.DirectoryZipOutputStream;
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
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Service used to perform all import/export operations for content and documents.
 *
 * @author Thomas Draier
 */
public class ImportExportBaseService extends JahiaService implements ImportExportService, Observer {

    private static Logger logger = LoggerFactory.getLogger(ImportExportBaseService.class);
    private static final Set<String> KNOWN_IMPORT_CONTENT_TYPES = ImmutableSet.of("application/zip", "application/xml", "text/xml");

    private static final String FILESACL_XML = "filesacl.xml";

    private static final String REPOSITORY_XML = "repository.xml";
    private static final String LIVE_REPOSITORY_XML = "live-repository.xml";
    private static final String CATEGORIES_XML = "categories.xml";
    private static final String SITE_PERMISSIONS_XML = "sitePermissions.xml";
    private static final String USERS_XML = "users.xml";
    private static final String SITE_PROPERTIES = "site.properties";
    private static final String EXPORT_PROPERTIES = "export.properties";
    private static final String DEFINITIONS_CND = "definitions.cnd";
    private static final String DEFINITIONS_MAP = "definitions.map";

    public static final String STATIC_MOUNT_POINT_ATTR = "j:staticMountPointProviderKey";
    public static final String DYNAMIC_MOUNT_POINT_ATTR = "j:dynamicMountPointProviderPath";

    private JahiaSitesService sitesService;
    private JahiaFileWatcherService fileWatcherService;
    private JCRStoreService jcrStoreService;
    private CategoryService categoryService;

    private long observerInterval = 10000;
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();
    private boolean expandImportedFilesOnDisk;
    private String expandImportedFilesOnDiskPath;

    private List<AttributeProcessor> attributeProcessors;
    private TemplatePackageRegistry templatePackageRegistry;

    private static final HashSet<String> siteExportNodeTypesToIgnore = Sets.newHashSet("jnt:templatesFolder", "jnt:externalUser", "jnt:workflowTask");
    private static final HashSet<String> defaultExportNodeTypesToIgnore = Sets.newHashSet(Constants.JAHIANT_VIRTUALSITE, "jnt:workflowTask");

    private ImportExportBaseService() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ImportExportBaseService INSTANCE = new ImportExportBaseService();
    }

    public static ImportExportBaseService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Helper method to determine which type of the import the uploaded file represents.
     *
     * @param declaredContentType the declared content type
     * @param fileName the uploaded file name
     * @return type of the import the uploaded file represents
     */
    public static String detectImportContentType(String declaredContentType, String fileName) {
        String contentType = declaredContentType;
        if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
            contentType = JCRContentUtils.getMimeType(fileName);
            if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
                if (StringUtils.endsWithIgnoreCase(fileName, ".xml")) {
                    contentType = "application/xml";
                } else if (StringUtils.endsWithIgnoreCase(fileName, ".zip")) {
                    contentType = "application/zip";
                } else {
                    // no chance to detect it
                    logger.error("Unable to detect the content type for file {}. It is neither a ZIP file nor an XML. Skipping import.", fileName);
                }
            }
        }
        return contentType;
    }

    @Override
    public void start() {
        try {
            new ImportFileObserver(org.jahia.settings.SettingsBean.getInstance().getJahiaImportsDiskPath(), false, observerInterval, true);
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
                    logger.error("init:: ", e);
                    throw new JahiaInitializationException(
                            "ImportObserver::init failed ", e);
                }
            }
        }

        @Override
        public void update(Observable observable, Object args) {
            synchronized (args) {
                @SuppressWarnings("unchecked")
                final List<File> files = (List<File>) args;
                if (!files.isEmpty()) {
                    try {
                        JahiaUser user = JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser();
                        JCRSessionFactory.getInstance().setCurrentUser(user);
                        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, null, null, new JCRCallback<Object>() {

                            @Override
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                JCRNodeWrapper dest = session.getNode("/imports");
                                for (File file : files) {
                                    try {
                                        InputStream is = new BufferedInputStream(new FileInputStream(file));
                                        try {
                                            dest.uploadFile(file.getName(), is, JCRContentUtils.getMimeType(file.getName()));
                                        } finally {
                                            IOUtils.closeQuietly(is);
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

    @Override
    public void stop() {
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
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
     * @param sortedNodes the nodes to be exported
     * @param session the session used to export
     * @param nodeTypesToIgnore the node types to ignore in the export
     * @return the estimation of nodes to export
     * @throws RepositoryException
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
                if(languages != null && languages.size() > 0) {
                    for (String language : languages) {
                        result += estimateSubnodesNumber(sitePaths, session, nodeTypesToIgnore, language);
                    }
                }
            } else {
                extraPathsToExport.add(nodesToExport.getPath());
            }
        }

        result += estimateSubnodesNumber(extraPathsToExport, session, nodeTypesToIgnore, null);
        return result;
    }

    /**
     * Estimates subnodes number to be exported, estimation use a count query using given parameters.
     * @param paths list of paths in query
     * @param session session used to execute the query
     * @param nodeTypesToIgnore Set of nodetypes to filter in query
     * @param locale language to be used when jnt:translation nodes have to be retrieved
     * @return the final estimation
     * @throws RepositoryException
     */
    private long estimateSubnodesNumber(List<String> paths, JCRSessionWrapper session, Set<String> nodeTypesToIgnore, String locale) throws RepositoryException {
        if(paths == null || paths.size() == 0) {
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
            statement.append("isdescendantnode(['").append(path).append("'])");
        }
        statement.append(")");

        if (locale == null && nodeTypesToIgnore != null && nodeTypesToIgnore.size() > 0) {
            statement.append("AND NOT (");
            Iterator nodeTypesToIgnoreIterator = nodeTypesToIgnore.iterator();
            while (nodeTypesToIgnoreIterator.hasNext()) {
                statement.append("[jcr:primaryType] = '").append(nodeTypesToIgnoreIterator.next()).append("'");
                if (nodeTypesToIgnoreIterator.hasNext()) {
                    statement.append(" OR ");
                }
            }
            statement.append(")");
        }

        if (locale != null) {
            statement.append(" AND [jcr:language] = '").append(locale).append("'");
        }

        return queryManagerWrapper.createQuery(statement.toString(), Query.JCR_SQL2).execute().getRows().nextRow().getValue("count").getLong();
    }

    @Override
    public void exportSites(OutputStream outputStream, Map<String, Object> params, List<JCRSiteNode> sites)
            throws RepositoryException, IOException, SAXException, TransformerException {

        logger.info("Sites " + sites + " export started");
        long startSitesExportTime = System.currentTimeMillis();
        String serverDirectory = (String) params.get(SERVER_DIRECTORY);
        ZipOutputStream zout = getZipOutputStream(outputStream, serverDirectory);
        ZipEntry anEntry = new ZipEntry(EXPORT_PROPERTIES);
        zout.putNextEntry(anEntry);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout));
        bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
        bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
        bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
        bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
        bw.flush();

        Set<String> externalReferences = new HashSet<>();

        for (JCRSiteNode jahiaSite : sites) {
            long startSiteExportTime = System.currentTimeMillis();
            logger.info("Exporting site internal nodes " + jahiaSite.getName() +" content started");
            if (serverDirectory == null) {
                anEntry = new ZipEntry(jahiaSite.getSiteKey() + ".zip");
                zout.putNextEntry(anEntry);

                exportSite(jahiaSite, zout, externalReferences, params, null);
            } else {
                exportSite(jahiaSite, zout, externalReferences, params, serverDirectory + "/" + jahiaSite.getSiteKey());
            }
            logger.info("Exporting site internal nodes {} ended in {} seconds", jahiaSite.getName(), getDuration(startSiteExportTime));
        }

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();

        if (params.containsKey(INCLUDE_USERS)) {
            // export users
            ZipOutputStream zzout;
            if (serverDirectory == null) {
                zout.putNextEntry(new ZipEntry("users.zip"));
                zzout = getZipOutputStream(zout, null);
            } else {
                zzout = getZipOutputStream(zout, serverDirectory + "/users");
            }

            try {
                logger.info("Exporting Users Started");
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode("/users")), zzout,
                        defaultExportNodeTypesToIgnore, externalReferences, params, true);
                logger.info("Exporting Users Ended");
            } catch (IOException e) {
                logger.warn("Cannot export due to some IO exception :"+e.getMessage());
            } catch (Exception e) {
                logger.error("Cannot export Users", e);
            }
            zzout.finish();
        }
        if (params.containsKey(INCLUDE_ROLES)) {
            // export roles
            ZipOutputStream zzout;
            if (serverDirectory == null) {
                zout.putNextEntry(new ZipEntry("roles.zip"));
                zzout = getZipOutputStream(zout, null);
            } else {
                zzout = getZipOutputStream(zout, serverDirectory + "/roles");
            }


            try {
                logger.info("Exporting Roles Started");
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode("/roles")), zzout,
                        defaultExportNodeTypesToIgnore, externalReferences, params, true);
                logger.info("Exporting Roles Ended");
            } catch (Exception e) {
                logger.error("Cannot export roles", e);
            }
            zzout.finish();
        }
        if (params.containsKey(INCLUDE_MOUNTS) && session.nodeExists("/mounts")) {
            JCRNodeWrapper mounts = session.getNode("/mounts");
            if (mounts.hasNodes()) {
                // export mounts
                zout.putNextEntry(new ZipEntry("mounts.zip"));
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

        Set<JCRNodeWrapper> refs = new HashSet<JCRNodeWrapper>();
        for (String reference : externalReferences) {
            JCRNodeWrapper node = session.getNodeByUUID(reference);
            if (!defaultExportNodeTypesToIgnore.contains(node.getPrimaryNodeTypeName())) {
                refs.add(node);
            }
        }
        if (!refs.isEmpty()) {
            zout.putNextEntry(new ZipEntry("references.zip"));
            ZipOutputStream zzout = getZipOutputStream(zout, serverDirectory + "/references.zip");
            try {
                logger.info("Exporting References Started");
                exportNodesWithBinaries(session.getRootNode(), refs, zzout, defaultExportNodeTypesToIgnore, externalReferences, params, true);
                logger.info("Exporting References Ended");
            } catch (Exception e) {
                logger.error("Cannot export References", e);
            }
            zzout.finish();
        }
        zout.finish();

        logger.info("Total Sites {} export ended in {} seconds", sites, getDuration(startSitesExportTime));
    }

    private ZipOutputStream getZipOutputStream(OutputStream outputStream, String serverDirectory) {
        ZipOutputStream zout = null;
        if (serverDirectory != null) {
            File serverDirectoryFile = new File(serverDirectory);
            if (serverDirectoryFile.getParentFile().exists()) {
                if (!serverDirectoryFile.exists()) {
                    if (!serverDirectoryFile.mkdir()) {
                        serverDirectoryFile = null;
                    }
                } else {
                    if (!serverDirectoryFile.isDirectory()) {
                        serverDirectoryFile = null;
                    }
                }
            } else {
                // parent directory doesn't exist, we fail the export to avoid potential security issues using the
                // export functionality to write on the server file system.
                serverDirectoryFile = null;
            }
            if (serverDirectoryFile != null) {
                zout = new DirectoryZipOutputStream(serverDirectoryFile, outputStream);
            }
        }
        if (zout == null) {
            zout = new ZipOutputStream(outputStream);
        }
        return zout;
    }

    private void exportSite(final JCRSiteNode site, OutputStream out, Set<String> externalReferences, Map<String, Object> params, String serverDirectory)
            throws RepositoryException, SAXException, IOException, TransformerException {

        ZipOutputStream zout = getZipOutputStream(out, serverDirectory);

        zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
        exportSiteInfos(zout, site);
        final JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();
        JCRNodeWrapper node = session.getNode("/sites/" +
                site.getSiteKey());
        Set<JCRNodeWrapper> nodes = Collections.singleton(node);
        exportNodesWithBinaries(session.getRootNode(), nodes, zout, siteExportNodeTypesToIgnore,
                externalReferences, params, true);
        zout.finish();
    }

    @Override
    public void exportZip(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params)
            throws RepositoryException, SAXException, IOException, TransformerException {

        ZipOutputStream zout = getZipOutputStream(out, (String) params.get(SERVER_DIRECTORY));
        Set<JCRNodeWrapper> nodes = new HashSet<JCRNodeWrapper>();
        nodes.add(node);
        exportNodesWithBinaries(exportRoot == null ? node : exportRoot, nodes, zout, new HashSet<String>(), null,
                params, false);
        zout.finish();
    }

    @Override
    public void exportNode(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params)
            throws RepositoryException, SAXException, IOException, TransformerException {

        TreeSet<JCRNodeWrapper> nodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {

            @Override
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        nodes.add(node);
        exportNodes(exportRoot == null ? node : exportRoot, nodes, out, new HashSet<String>(), null, params, false);
    }

    private void exportNodesWithBinaries(JCRNodeWrapper rootNode, Set<JCRNodeWrapper> nodes, ZipOutputStream zout,
                                         Set<String> typesToIgnore, Set<String> externalReferences,
                                         Map<String, Object> params, boolean logProgress)
            throws SAXException, IOException, RepositoryException, TransformerException {

        TreeSet<JCRNodeWrapper> liveSortedNodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {

            @Override
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });

//        final String xsl = (String) params.get(XSL_PATH);
        if (params.containsKey(INCLUDE_LIVE_EXPORT)) {
            final JCRSessionWrapper liveSession = jcrStoreService.getSessionFactory().getCurrentUserSession("live");
            JCRNodeWrapper liveRootNode = null;
            try {
                liveRootNode = liveSession.getNodeByIdentifier(rootNode.getIdentifier());
            } catch (RepositoryException e) {
            }
            if (liveRootNode != null) {
                for (JCRNodeWrapper node : nodes) {
                    try {
                        liveSortedNodes.add(liveSession.getNodeByIdentifier(node.getIdentifier()));
                    } catch (ItemNotFoundException e) {
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
        }
        TreeSet<JCRNodeWrapper> sortedNodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {

            @Override
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
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
        DataWriter dw = new DataWriter(new OutputStreamWriter(tmpOut, "UTF-8"));
        if (Boolean.TRUE.equals(params.get(SYSTEM_VIEW))) {
            SystemViewExporter exporter = new SystemViewExporter(rootNode.getSession(), dw, !noRecurse, !skipBinary);
            exporter.export(rootNode);
        } else {
            DocumentViewExporter exporter = new DocumentViewExporter(rootNode.getSession(), dw, skipBinary,
                    noRecurse);
            exporter.setExportContext(exportContext);
            exporter.addObserver(this);

            if (externalReferences != null) {
                exporter.setExternalReferences(externalReferences);
            }
            typesToIgnore.add("rep:system");
            if (params.containsKey(INCLUDE_LIVE_EXPORT)) {
                List<String> l = new ArrayList<String>(exporter.getPropertiestoIgnore());
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

        if (exportContext != null) {
            // Nodes are now exported in the .xml, so we log the time difference for this export
            logger.info("Exported {} nodes in {} seconds", exportContext.getExportIndex(), getDuration(startSitesExportTime));
        }

        dw.flush();
        if (xsl != null) {
            DeferredFileOutputStream stream = (DeferredFileOutputStream) tmpOut;
            InputStream inputStream = new BufferedInputStream(new FileInputStream(stream.getFile()));
            fileCleaningTracker.track(stream.getFile(), inputStream);
            if (stream.isInMemory()) {
                inputStream.close();
                inputStream = new ByteArrayInputStream(stream.getData());
            }
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

        // binary export can be time consuming, log some basic information
        long startExportingNodesBinary = System.currentTimeMillis();
        logger.info("Exporting binary nodes ...");

        byte[] buffer = new byte[4096];
        for (Iterator<JCRNodeWrapper> iterator = nodes.iterator(); iterator.hasNext(); ) {
            JCRNodeWrapper file = iterator.next();
            exportNodeBinary(root, file, zout, typesToIgnore, buffer, basepath, new HashSet<String>());
        }

        logger.info("Binary nodes exported in {} seconds", getDuration(startExportingNodesBinary));
    }

    private void exportNodeBinary(JCRNodeWrapper root, JCRNodeWrapper node, ZipOutputStream zout,
                                  Set<String> typesToIgnore, byte[] buffer, String basepath, Set<String> exportedFiles)
            throws IOException, RepositoryException {

        int bytesIn;
        if (!typesToIgnore.contains(node.getPrimaryNodeTypeName()) && node.getProvider().canExportNode(node)) {
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.nextNode();
                if (!child.getProvider().canExportNode(child)) {
                    continue;
                }
                if (child.isNodeType("nt:resource")) {
                    if (!exportedFiles.contains(child.getPath())) {
                        exportedFiles.add(child.getPath());
                        InputStream is = null;
                        try {
                            JCRPropertyWrapper property = child.getProperty("jcr:data");
                            if (child.getProvider().canExportProperty(property)) {
                                is = property.getBinary().getStream();
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
                        } catch(RepositoryException ex){
                            logger.warn("Cannot export " + child.getPath(), ex);
                        } catch (AssertionError ex) {
                            logger.warn("Cannot export " + child.getPath(), ex);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                    }
                }
                exportNodeBinary(root, child, zout, typesToIgnore, buffer, basepath, exportedFiles);
            }
        }
    }

    private void exportSiteInfos(OutputStream out, JCRSiteNode s) throws IOException {
        Properties p = new OrderedProperties();
        p.setProperty("sitetitle", s.getTitle());
        p.setProperty("siteservername", s.getServerName());
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
            if (defaultSite!= null && defaultSite.getSiteKey().equals(s.getName())) {
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
        ZipInputStream zis = new ZipInputStream(contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream());

        importSiteZip(zis, uri, null, nodeWrapper.getSession());
    }

    @Override
    public void importSiteZip(Resource file) throws RepositoryException, IOException, JahiaException {
        importSiteZip(file, null);
    }

    @Override
    public void importSiteZip(Resource file, JCRSessionWrapper session) throws RepositoryException, IOException {
        ZipInputStream zis = new ZipInputStream(file.getInputStream());

        importSiteZip(zis, null, file, session);
    }

    private void importSiteZip(ZipInputStream zis2, final String uri, final Resource fileImport, JCRSessionWrapper session) throws IOException {
        ZipEntry z;
        final Properties infos = new Properties();
        while ((z = zis2.getNextEntry()) != null) {
            if ("site.properties".equals(z.getName())) {
                infos.load(zis2);
                zis2.closeEntry();

                boolean siteKeyEx = false;
                boolean serverNameEx = false;
                try {
                    siteKeyEx = "".equals(
                            infos.get("sitekey")) || sitesService.siteExists((String) infos.get("sitekey"), session);
                    String serverName = (String) infos.get("siteservername");
                    serverNameEx = "".equals(serverName) || (!Url.isLocalhost(serverName) && sitesService.getSiteByServerName(serverName, session) != null);
                } catch (RepositoryException e) {
                    logger.error("Error when getting site", e);
                }

                if (!siteKeyEx && !serverNameEx) {
                    // site import
                    String tpl = (String) infos.get("templatePackageName");
                    if ("".equals(tpl)) {
                        tpl = null;
                    }
                    try {
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
                        final Locale finalLocale = locale;
                        final String finalTpl = tpl;
                        try {
                            JCRObservationManager.doWithOperationType(session, JCRObservationManager.IMPORT, new JCRCallback<Object>() {

                                @Override
                                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    try {
                                        JahiaSite site = sitesService.addSite(JCRSessionFactory.getInstance().getCurrentUser(), infos.getProperty("sitetitle"), infos.getProperty(
                                                        "siteservername"), infos.getProperty("sitekey"), infos.getProperty(
                                                        "description"), finalLocale, finalTpl, null, fileImport != null ? "fileImport" : "importRepositoryFile", fileImport, uri, true,
                                                false, infos.getProperty("originatingJahiaRelease"), null, null, session);
                                        importSiteProperties(site, infos, session);
                                    } catch (JahiaException e) {
                                        throw new RepositoryException(e);
                                    } catch (IOException e) {
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
            }
        }
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
     * @throws RepositoryException
     * @throws IOException
     */
    public void importSiteZip(Resource file, JahiaSite site, Map<Object, Object> infos, Resource legacyMappingFilePath, Resource legacyDefinitionsFilePath, JCRSessionWrapper session) throws RepositoryException, IOException {
        long timerSite = System.currentTimeMillis();
        logger.info("Start import for site {}", site != null ? site.getSiteKey() : "");

        final CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler();
        final UsersImportHandler usersImportHandler = new UsersImportHandler(site, session);

        boolean legacyImport = false;
        List<String[]> catProps = null;
        List<String[]> userProps = null;

        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        logger.info("Start analyzing import file {}", file);
        long timer = System.currentTimeMillis();
        getFileList(file, sizes, fileList);
        logger.info("Done analyzing import file {} in {}", file, DateUtils.formatDurationWords(System.currentTimeMillis() - timer));

        Map<String, String> pathMapping = session.getPathMapping();
        for (JahiaTemplatesPackage pkg : templatePackageRegistry.getRegisteredModules().values()) {
            String key = "/modules/" + pkg.getId() + "/";
            if (!pathMapping.containsKey(key)) {
                pathMapping.put(key, "/modules/" + pkg.getId() + "/" + pkg.getVersion() + "/");
            }
        }

        ZipInputStream zis;
        if (sizes.containsKey(USERS_XML)) {
            // Import users first
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(USERS_XML)) {
                        userProps = importUsers(zis, usersImportHandler);
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }
        }

        // Check if it is an 5.x or 6.1 import :
        for (Map.Entry<String, Long> entry : sizes.entrySet()) {
            if (entry.getKey().startsWith("export_")) {
                legacyImport = true;
                break;
            }
        }

        if (sizes.containsKey(SITE_PROPERTIES)) {
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(SITE_PROPERTIES)) {
                        importSiteProperties(zis, site, session);
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }
        }

        if (sizes.containsKey(REPOSITORY_XML)) {
            // Parse import file to detect sites
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(REPOSITORY_XML)) {
                        timer = System.currentTimeMillis();
                        logger.info("Start importing " + REPOSITORY_XML);

                        DocumentViewValidationHandler h = new DocumentViewValidationHandler();
                        h.setSession(session);
                        List<ImportValidator> validators = new ArrayList<ImportValidator>();
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

                        if (!sizes.containsKey(SITE_PROPERTIES)) {
                            // todo : site properties can be removed and properties get from here
                        }
                        logger.info("Done importing " + REPOSITORY_XML + " in {}",
                                DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                closeInputStream(zis);
            }

            importZip(null, file, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, Sets.newHashSet(USERS_XML, CATEGORIES_XML), true);
        } else {
            // No repository descriptor - prepare to import files directly
            pathMapping.put("/", "/sites/" + site.getSiteKey() + "/files/");
        }

        NodeTypeRegistry reg = NodeTypeRegistry.getInstance();
        DefinitionsMapping mapping = null;

        // Import additional files - site.properties, old cateogries.xml , sitepermissions.xml
        // and eventual plain file from 5.x imports
        if (!sizes.containsKey(REPOSITORY_XML) || sizes.containsKey(SITE_PROPERTIES) || sizes.containsKey(CATEGORIES_XML)
                || sizes.containsKey(SITE_PERMISSIONS_XML) || sizes.containsKey(DEFINITIONS_CND) || sizes.containsKey(DEFINITIONS_MAP)) {
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.indexOf('\\') > -1) {
                        name = name.replace('\\', '/');
                    }
                    if (name.indexOf('/') > -1) {
                        if (!sizes.containsKey(REPOSITORY_XML) && !sizes.containsKey(FILESACL_XML)) {
                            // No repository descriptor - Old import format only
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
                    } else if (name.equals(CATEGORIES_XML)) {
                        catProps = importCategoriesAndGetUuidProps(zis, categoriesImportHandler);
                    } else if (name.equals(DEFINITIONS_CND)) {
                        reg = new NodeTypeRegistry(); // this is fishy: a new instance is created here when NodeTypeRegistry is meant to be used as a singleton
                        try {
                            for (Map.Entry<String, File> entry : NodeTypeRegistry.getSystemDefinitionsFiles().entrySet()) {
                                reg.addDefinitionsFile(entry.getValue(), entry.getKey());
                            }
                            if (legacyImport) {
                                JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(new InputStreamReader(zis, Charsets.UTF_8), zipentry.getName(),
                                        file.getURL().getPath(), reg);
                                r.parse();
                            } else {
                                reg.addDefinitionsFile(new InputStreamResource(zis, zipentry.getName()), file.getURL().getPath());
                            }
                        } catch (RepositoryException | ParseException e) {
                            logger.error(e.getMessage(), e);
                        }
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
            long timerLegacy = System.currentTimeMillis();
            final String originatingJahiaRelease = (String) infos.get("originatingJahiaRelease");
            logger.info("Start legacy import, source version is " + originatingJahiaRelease);
            if (legacyMappingFilePath != null) {
                mapping = new DefinitionsMapping();
                final InputStream fileInputStream = legacyMappingFilePath.getInputStream();
                try {
                    mapping.load(fileInputStream);
                } finally {
                    IOUtils.closeQuietly(fileInputStream);
                }
            }
            if (legacyDefinitionsFilePath != null) {
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
                        InputStreamReader inputStreamReader = null;
                        try {
                            final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("org/jahia/migration/legacyDefinitions/jahia6/" + builtInLegacyDefsFile);
                            if (inputStream != null) {
                                inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                                final JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(inputStreamReader, builtInLegacyDefsFile,
                                        file.getURL().getPath(), reg);
                                r.parse();
                            } else {
                                logger.error("Couldn't load " + builtInLegacyDefsFile);
                            }
                        } catch (ParseException e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            IOUtils.closeQuietly(inputStreamReader);
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
                InputStreamReader streamReader = null;
                try {
                    streamReader = new InputStreamReader(legacyDefinitionsFilePath.getInputStream(), "UTF-8");
                    JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(streamReader, legacyDefinitionsFilePath.getFilename(),
                            file.getURL().getPath(), reg);
                    r.parse();
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(streamReader);
                }
            }
            // Old import
            JCRNodeWrapper siteFolder = session.getNode("/sites/" + site.getSiteKey());

            zis = new NoCloseZipInputStream(new BufferedInputStream(file.getInputStream()));
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
                        logger.info("Importing file " + name);
                        String languageCode;
                        if (name.indexOf("_") != -1) {
                            languageCode = name.substring(7, name.lastIndexOf("."));
                        } else {
                            languageCode = site.getLanguagesAsLocales().iterator().next().toString();
                        }
                        zipentry.getSize();

                        LegacyImportHandler importHandler = new LegacyImportHandler(session, siteFolder, reg, mapping, LanguageCodeConverters.languageCodeToLocale(languageCode), infos != null ? originatingJahiaRelease : null, legacyPidMappingTool, legacyImportHandlerCtnId);
                        Map<String, List<String>> references = new LinkedHashMap<String, List<String>>();
                        importHandler.setReferences(references);

                        InputStream documentInput = zis;
                        if (this.xmlContentTransformers != null && this.xmlContentTransformers.size() > 0) {
                            documentInput = new ZipInputStream(file.getInputStream());
                            while (!name.equals(((ZipInputStream) documentInput).getNextEntry().getName())) ;
                            byte[] buffer = new byte[2048];
                            final File tmpDirectoryForSite = new File(new File(System.getProperty("java.io.tmpdir"), "jahia-migration"),
                                    FastDateFormat.getInstance("yyyy_MM_dd-HH_mm_ss_SSS").format(timerSite) + "_" + site.getSiteKey());
                            tmpDirectoryForSite.mkdirs();
                            File document = new File(tmpDirectoryForSite, "export_" + languageCode + "_00_extracted.xml");
                            final OutputStream output = new BufferedOutputStream(new FileOutputStream(document), 2048);
                            int count = 0;
                            while ((count = documentInput.read(buffer, 0, 2048)) > 0) {
                                output.write(buffer, 0, count);
                            }
                            output.flush();
                            output.close();
                            documentInput.close();
                            for (XMLContentTransformer xct : xmlContentTransformers) {
                                document = xct.transform(document, tmpDirectoryForSite);
                            }
                            documentInput = new FileInputStream(document);
                        }

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
            logger.info("Done legacy import in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerLegacy));
        }

        categoriesImportHandler.setUuidProps(catProps);
        usersImportHandler.setUuidProps(userProps);

        // session.save();
        session.save(JCRObservationManager.IMPORT);
        cleanFilesList(fileList);

        if (legacyImport && this.postImportPatcher != null) {
            final long timerPIP = System.currentTimeMillis();
            logger.info("Executing post import patches");
            this.postImportPatcher.executePatches(site);
            logger.info("Executed post import patches in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerPIP));
        }

        logger.info("Done importing site {} in {}", site != null ? site.getSiteKey() : "", DateUtils.formatDurationWords(System.currentTimeMillis() - timerSite));
    }

    private void cleanFilesList(List<String> fileList) {
        if (expandImportedFilesOnDisk) {
            long timer = System.currentTimeMillis();
            logger.info("Start cleaning {} files", fileList.size());
            for (String fileName : fileList) {
                try {
                    File toBeDeleted = new File(expandImportedFilesOnDiskPath + fileName);
                    if (toBeDeleted.exists()) {
                        if (toBeDeleted.isDirectory()) {
                            FileUtils.deleteDirectory(toBeDeleted);
                        } else {
                            toBeDeleted.delete();
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
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
//            if ("jnt:virtualsite".equals(dir.getType())) {
//                int endIndex = name.lastIndexOf('/');
//                dir = JahiaWebdavBaseService.getInstance().getDAVFileAccess(name.substring(0, endIndex)+"/"+site.getSiteKey(), jParams.getUser());
//            }
        } catch (PathNotFoundException pnfe) {
            int endIndex = path.lastIndexOf('/');
            if (endIndex == -1) {
                logger.warn("Cannot create folder " + path);
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
        Map<String, File> filePath = new HashMap<String, File>();
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

    private void importSiteProperties(JahiaSite site, Properties p, JCRSessionWrapper session) {
        Set<Object> keys = p.keySet();
        final Set<String> languages = new HashSet<String>();
        final Set<String> inactiveLanguages = new HashSet<String>();
        final Set<String> inactiveLiveLanguages = new HashSet<String>();
        final Set<String> mandatoryLanguages = new HashSet<String>();

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

        List<JahiaTemplatesPackage> modules = new ArrayList<JahiaTemplatesPackage>();

        for (Object key : keys) {
            String property = (String) key;
            String value = p.getProperty(property);
            StringTokenizer st = new StringTokenizer(property, ".");
            String firstKey = st.nextToken();

            try {
                if (firstKey.equals("language")) {
                    String lang = st.nextToken();

                    if (!languages.contains(lang)) {
                        languages.add(lang);
                        if (!Boolean.valueOf(p.getProperty("language." + lang + ".activated", "true"))) {
                            inactiveLiveLanguages.add(lang);
                        }
                        if (Boolean.valueOf(p.getProperty("language." + lang + ".disabledCompletely", "false"))) {
                            inactiveLanguages.add(lang);
                            languages.remove(lang);
                        }
                        if (Boolean.valueOf(p.getProperty("language." + lang + ".mandatory", "false"))) {
                            mandatoryLanguages.add(lang);
                        }
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
                } else if (firstKey.equals("defaultLanguage")) {
                    defaultLanguage = value;
                } else if (firstKey.equals("mixLanguage")) {
                    site.setMixLanguagesActive(Boolean.parseBoolean(value));
                } else if (firstKey.equals("allowsUnlistedLanguages")) {
                    site.setAllowsUnlistedLanguages(Boolean.parseBoolean(value));
                } else if (firstKey.equals("description")) {
                    site.setDescription(value);
                } else if (firstKey.startsWith("defaultSite") && "true".equals(value) && sitesService.getDefaultSite(session) == null) {
                    sitesService.setDefaultSite(site, session);
                } else if (firstKey.equals("installedModules")) {
                    if (!installedModules.contains(value) && !templateSet.equals(value)) {
                        JahiaTemplatesPackage pkg = templateManagerService.getAnyDeployedTemplatePackage(value);
                        if (pkg != null) {
                            modules.add(pkg);
                        } else {
                            logger.info("unable to find module {} in deployed modules", value);
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot set site property  " + firstKey, e);
            }
        }

        @SuppressWarnings("unchecked")
        Set<String> siteLangs = ListOrderedSet.decorate(new LinkedList<String>(languages));
        if (!siteLangs.isEmpty()) {
            site.setLanguages(siteLangs);
            site.setInactiveLanguages(inactiveLanguages);
            site.setInactiveLiveLanguages(inactiveLiveLanguages);
            site.setMandatoryLanguages(mandatoryLanguages);
            if (defaultLanguage == null) {
                defaultLanguage = StringUtils.isEmpty(lowestRankLanguage) ? siteLangs.iterator().next() : lowestRankLanguage;
            }
            site.setDefaultLanguage(defaultLanguage);
            /*try {
                JahiaSite jahiaSite = sitesService.getSiteByKey(JahiaSitesService.SYSTEM_SITE_KEY,session);
                // update the system site only if it does not yet contain at least one of the site languages
                Set<String> jahiaSiteLanguages = new HashSet<String>(jahiaSite.getLanguages());
                if (!jahiaSiteLanguages.containsAll(site.getLanguages())) {
                    jahiaSiteLanguages.addAll(site.getLanguages());
                    jahiaSite.setLanguages(jahiaSiteLanguages);
                }
                sitesService.updateSystemSitePermissions(jahiaSite, session);
            } catch (RepositoryException e) {
                logger.error("Cannot update system site", e);
            }*/
        } else {
            logger.error("Unable to find site languages in the provided site.properties descriptor. Skip importing site settings.");
        }

        try {
            templateManagerService.installModules(modules, "/sites/" + site.getSiteKey(), session);

            session.save();
        } catch (RepositoryException e) {
            logger.error("Cannot deploy module " + modules, e);
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
        final InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<String[]>>() {

                @Override
                public List<String[]>  doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<String[]> l = importUsers(is, new UsersImportHandler(session), file.getName());
                    session.save();
                    return l;
                }
            });
        } finally {
            IOUtils.closeQuietly(is);
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
                if (dh.getMissingDependencies().size() > 0) {
                    for (String s : dh.getMissingDependencies()) {
                        logger.error("Dependency not declared : " + s + " (set debug on DocumentViewImportHandler for more details)");
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
        final HashMap<String, List<String>> references = new HashMap<String, List<String>>();
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
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(content, fileOutputStream);
            fileOutputStream.close();
            InputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile));
            int format;
            try {
                format = detectXmlFormat(inputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

            switch (format) {
                case XMLFormatDetectionHandler.JCR_DOCVIEW: {
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(tempFile));
                        session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, rootBehavior, null, references);
                        session.save(JCRObservationManager.IMPORT);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                    break;
                }

                case XMLFormatDetectionHandler.USERS: {
                    importUsers(tempFile);
                    break;
                }
                case XMLFormatDetectionHandler.CATEGORIES: {
                    Category cat = categoryService.getCategoryByPath(parentNodePath);
                    InputStream is = new BufferedInputStream(new FileInputStream(tempFile));
                    try {
                        importCategories(cat, is);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                    break;
                }
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
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
        if (contentType.equals("application/zip")) {
            NoCloseZipInputStream zis = new NoCloseZipInputStream(new BufferedInputStream(is));
            try {
                ZipEntry zipentry = zis.getNextEntry();
                while (zipentry != null) {
                    final String name = zipentry.getName();
                    if (name.endsWith("xml")) {
                        handleImport(zis, documentViewValidationHandler, name);
                    }
                    zipentry = zis.getNextEntry();
                }
            } catch (IOException e) {
                logger.error("Cannot import", e);
            } finally {
                try {
                    zis.reallyClose();
                } catch (IOException e) {
                    logger.error("Cannot import", e);
                }
            }
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
    public void importZip(String parentNodePath, Resource file, int rootBehaviour, final JCRSessionWrapper session, Set<String> filesToIgnore, boolean useReferenceKeeper)
            throws IOException, RepositoryException {
        long timer = System.currentTimeMillis();
        if (filesToIgnore == null) {
            filesToIgnore = Collections.<String>emptySet();
        }
        logger.info("Start importing file {} into path {} ", file, parentNodePath != null ? parentNodePath : "/");

        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, List<String>> references = new HashMap<String, List<String>>();

        getFileList(file, sizes, fileList);
        ZipInputStream zis;

        Map<String, String> pathMapping = session.getPathMapping();
        for (JahiaTemplatesPackage pkg : templatePackageRegistry.getRegisteredModules().values()) {
            String key = "/modules/" + pkg.getId() + "/";
            if (!pathMapping.containsKey(key)) {
                pathMapping.put(key, "/modules/" + pkg.getId() + "/" + pkg.getVersion() + "/");
            }
        }

        boolean importLive = sizes.containsKey(LIVE_REPOSITORY_XML);

        List<String> liveUuids = null;
        if (importLive) {
            // Import live content
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(LIVE_REPOSITORY_XML) && !filesToIgnore.contains(name)) {
                        long timerLive = System.currentTimeMillis();
                        logger.info("Start importing " + LIVE_REPOSITORY_XML);

                        final DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);

                        documentViewImportHandler.setReferences(references);
                        documentViewImportHandler.setRootBehavior(rootBehaviour);
                        documentViewImportHandler.setBaseFilesPath("/live-content");
                        documentViewImportHandler.setAttributeProcessors(attributeProcessors);

                        Set<String> props = new HashSet<String>(documentViewImportHandler.getPropertiesToSkip());
                        props.remove(Constants.LASTPUBLISHED);
                        props.remove(Constants.LASTPUBLISHEDBY);
                        props.remove(Constants.PUBLISHED);
                        documentViewImportHandler.setPropertiesToSkip(props);
                        handleImport(zis, documentViewImportHandler, LIVE_REPOSITORY_XML);

                        logger.debug("Saving JCR session for " + LIVE_REPOSITORY_XML);

                        session.save(JCRObservationManager.IMPORT);

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

                        String label = "published_at_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
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

        // Import repository content
        zis = getZipInputStream(file);
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML) && !filesToIgnore.contains(name)) {
                    long timerDefault = System.currentTimeMillis();
                    logger.info("Start importing " + REPOSITORY_XML);
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);
                    if (importLive) {
                        // Restore publication status
                        Set<String> props = new HashSet<String>(documentViewImportHandler.getPropertiesToSkip());
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
                    logger.debug("Saving JCR session for " + REPOSITORY_XML);
                    session.save(JCRObservationManager.IMPORT);
                    logger.info("Done importing " + REPOSITORY_XML + " in {}", DateUtils.formatDurationWords(System.currentTimeMillis() - timerDefault));
                } else if (name.endsWith(".xml") && !name.equals(REPOSITORY_XML) && !name.equals(LIVE_REPOSITORY_XML) && !filesToIgnore.contains(name) && !name.contains("/")) {
                    long timerOther = System.currentTimeMillis();
                    logger.info("Start importing {}", name);
                    String thisPath = (parentNodePath != null ? (parentNodePath + (parentNodePath.endsWith("/") ? "" : "/")) : "") + StringUtils.substringBefore(name, ".xml");
                    importXML(thisPath, zis, rootBehaviour, references, session);
                    logger.info("Done importing {} in {}", name, DateUtils.formatDurationWords(System.currentTimeMillis() - timerOther));
                }
                zis.closeEntry();

                // during import/export, never try to resolve the references between templates and site.
                RenderContext r;
                r = TemplateModuleInterceptor.renderContextThreadLocal.get();
                TemplateModuleInterceptor.renderContextThreadLocal.remove();

                ReferencesHelper.resolveCrossReferences(session, references, useReferenceKeeper);

                TemplateModuleInterceptor.renderContextThreadLocal.set(r);

                session.save(JCRObservationManager.IMPORT);
            }
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Cannot import", e);
        } finally {
            closeInputStream(zis);
        }

        if (importLive) {
            // Import user generated content
            zis = getZipInputStream(file);
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
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

                        // ReferencesHelper.resolveCrossReferences(liveSession, references);
                        // liveSession.save(JCRObservationManager.IMPORT);

                        logger.info("Done importing user generated content in {}",
                                DateUtils.formatDurationWords(System.currentTimeMillis() - timerUGC));
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
        cleanFilesList(fileList);

        logger.info("Done importing file {} in {}", file, DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
    }

    public void getFileList(Resource file, Map<String, Long> sizes, List<String> fileList) throws IOException {
        ZipInputStream zis = getZipInputStream(file);
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName().replace('\\', '/');
                if (expandImportedFilesOnDisk) {
                    final File file1 = new File(expandImportedFilesOnDiskPath + File.separator + name);
                    if (zipentry.isDirectory()) {
                        file1.mkdirs();
                    } else {
                        long timer = System.currentTimeMillis();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Expanding {} into {}", zipentry.getName(), file1);
                        }
                        file1.getParentFile().mkdirs();
                        final OutputStream output = new BufferedOutputStream(new FileOutputStream(
                                file1), 1024 * 64);
                        try {
                            IOUtils.copyLarge(zis, output);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Expanded {} in {}", zipentry.getName(), DateUtils.formatDurationWords(System.currentTimeMillis() - timer));
                            }
                        } finally {
                            output.close();
                        }
                    }
                }
                if (name.endsWith(".xml")) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(zis));
                    try {
                        long i = 0;
                        while (br.readLine() != null) {
                            i++;
                        }
                        sizes.put(name, i);
                    } finally {
                        IOUtils.closeQuietly(br);
                    }
                } else {
                    sizes.put(name, zipentry.getSize());
                }
                if (name.contains("/")) {
                    fileList.add("/" + name);
                }
                zis.closeEntry();
            }
        } finally {
            closeInputStream(zis);
        }
    }

    private void closeInputStream(ZipInputStream zis) throws IOException {
        if (zis instanceof NoCloseZipInputStream) {
            ((NoCloseZipInputStream)zis).reallyClose();
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

    private class OrderedProperties extends Properties {

        private static final long serialVersionUID = -2418536708883832686L;
        Vector<Object> keys = new Vector<Object>();

        @Override
        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        @Override
        public Enumeration<Object> keys() {
            return keys.elements();
        }
    }

    private List<XMLContentTransformer> xmlContentTransformers;

    private Map<String, Templates> xsltTemplates = new ConcurrentHashMap<String, Templates>(2);

    public void setXmlContentTransformers(final List<XMLContentTransformer> xmlContentTransformers) {
        this.xmlContentTransformers = xmlContentTransformers;
    }

    private LegacyPidMappingTool legacyPidMappingTool = null;

    public void setLegacyPidMappingTool(LegacyPidMappingTool legacyPidMappingTool) {
        this.legacyPidMappingTool = legacyPidMappingTool;
    }

    private PostImportPatcher postImportPatcher = null;

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
    public void update(Observable o, Object arg) {
        if(arg instanceof ExportContext && o instanceof DocumentViewExporter) {
            ExportContext exportContext = (ExportContext) arg;
            DocumentViewExporter documentViewExporter = (DocumentViewExporter) o;
            exportContext.setExportIndex(exportContext.getExportIndex() + 1);
            logger.debug("Index: " + exportContext.getExportIndex() + ", Exporting  : " + exportContext.getActualPath());

            // this will show the percentage of export done by 10% increment will start by 10 and end by 90
            long currentStep = exportContext.getExportIndex() * 10 / exportContext.getNodesToExport();
            if (currentStep > exportContext.getStep() &&
                    exportContext.getStep() < 9) {
                exportContext.setStep(currentStep);
                logger.info("Export " + exportContext.getStep() * 10 + "%");
                documentViewExporter.setExportContext(exportContext);
            }
        }
    }
}

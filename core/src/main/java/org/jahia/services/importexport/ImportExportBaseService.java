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

package org.jahia.services.importexport;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.importexport.validation.*;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.jcr.JCRUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.utils.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.JahiaCndReaderLegacy;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.JahiaCndReader;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.zip.ZipEntry;
import org.jahia.utils.zip.ZipOutputStream;
import org.jahia.utils.LanguageCodeConverters;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.transform.XSLTransformer;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import javax.jcr.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service used to perform all import/export operations for content and documents.
 *
 * @author Thomas Draier
 */
public class ImportExportBaseService extends JahiaService implements ImportExportService {

    private static Logger logger = LoggerFactory.getLogger(ImportExportBaseService.class);

    private static ImportExportBaseService instance;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(ImportExportService.DATE_FORMAT);

    private static final Set<String> KNOWN_IMPORT_CONTENT_TYPES = ImmutableSet.of(
            "application/zip", "application/xml", "text/xml");

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


    private JahiaSitesService sitesService;
    private JahiaFileWatcherService fileWatcherService;
    private JCRStoreService jcrStoreService;
    private CategoryService categoryService;

	private long observerInterval = 10000;
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();

    public static ImportExportBaseService getInstance() {
        if (instance == null) {
            synchronized (ImportExportBaseService.class) {
                if (instance == null) {
                    instance = new ImportExportBaseService();
                }
            }
        }
        return instance;
    }

    public static String detectImportContentType(GWTFileManagerUploadServlet.Item item) {
        String contentType = item.getContentType();
        if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
            contentType = Jahia.getStaticServletConfig().getServletContext()
                    .getMimeType(item.getOriginalFileName());
            if (!KNOWN_IMPORT_CONTENT_TYPES.contains(contentType)) {
                if (StringUtils.endsWithIgnoreCase(item.getOriginalFileName(), ".xml")) {
                    contentType = "application/xml";
                } else {

                }
                if (StringUtils.endsWithIgnoreCase(item.getOriginalFileName(), ".zip")) {
                    contentType = "application/zip";
                } else {
                    // no chance to detect it
                    logger.error("Unable to detect the content type for file {}."
                            + " It is neither a ZIP file nor an XML. Skipping import.",
                            item.getOriginalFileName());
                }
            }
        }

        return contentType;
    }

    protected ImportExportBaseService() {
    }

    public void start() {
        try {
            new ImportFileObserver(org.jahia.settings.SettingsBean.getInstance().getJahiaImportsDiskPath(), false, observerInterval , true);
        } catch (JahiaException je) {
            logger.error("exception with FilesObserver", je);
        }


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

        public void update(Observable observable, Object args) {
            synchronized (args) {
                @SuppressWarnings("unchecked")
                final List<File> files = (List<File>) args;
                if (!files.isEmpty()) {
                    try {
                        JCRUser user = JCRUserManagerProvider.getInstance().lookupRootUser();
                        JCRSessionFactory.getInstance().setCurrentUser(user);
                        JCRTemplate.getInstance().doExecuteWithSystemSession(user.getUsername(), new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                JCRNodeWrapper dest = session.getNode("/imports");
                                for (File file : files) {
                                    try {
                                        InputStream is = new BufferedInputStream(new FileInputStream(file));
                                        try {
                                            dest.uploadFile(file.getName(), is,
                                                    JahiaContextLoaderListener.getServletContext().getMimeType(
                                                            file.getName()));
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

    public void exportAll(OutputStream outputStream, Map<String, Object> params)
            throws JahiaException, RepositoryException, IOException, SAXException, JDOMException {
        Iterator<JahiaSite> en = sitesService.getSites();
        List<JahiaSite> l = new ArrayList<JahiaSite>();
        while (en.hasNext()) {
            JahiaSite jahiaSite = (JahiaSite) en.next();
            l.add(jahiaSite);
        }
        exportSites(outputStream, params, l);
    }

    public void exportSites(OutputStream outputStream, Map<String, Object> params, List<JahiaSite> sites)
            throws RepositoryException, IOException, SAXException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(outputStream);

        ZipEntry anEntry = new ZipEntry(EXPORT_PROPERTIES);
        zout.putNextEntry(anEntry);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout));
        bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
        bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
        bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
        bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
        bw.flush();

        Set<String> externalReferences = new HashSet<String>();

        for (JahiaSite jahiaSite : sites) {
            anEntry = new ZipEntry(jahiaSite.getSiteKey() + ".zip");
            zout.putNextEntry(anEntry);
            exportSite(jahiaSite, zout, externalReferences, params);
        }

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();

        Set<String> tti = new HashSet<String>();
        tti.add(Constants.JAHIANT_VIRTUALSITE);

        if (params.containsKey(INCLUDE_USERS)) {
            // export users
            zout.putNextEntry(new ZipEntry("users.zip"));
            ZipOutputStream zzout = new ZipOutputStream(zout);

            try {
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode("/users")), zzout, tti, externalReferences, params);
            } catch (Exception e) {
                logger.error("Cannot export",e);
            }
            zzout.finish();
        }
        if (params.containsKey(INCLUDE_ROLES)) {
            // export roles
            zout.putNextEntry(new ZipEntry("roles.zip"));
            ZipOutputStream zzout = new ZipOutputStream(zout);

            try {
                exportNodesWithBinaries(session.getRootNode(), Collections.singleton(session.getNode("/roles")), zzout, tti, externalReferences, params);
            } catch (Exception e) {
                logger.error("Cannot export",e);
            }
            zzout.finish();
        }

        Set<JCRNodeWrapper> refs = new HashSet<JCRNodeWrapper>();
        for (String reference : externalReferences) {
            JCRNodeWrapper node = session.getNodeByUUID(reference);
            if (!tti.contains(node.getPrimaryNodeTypeName())) {
                refs.add(node);
            }
        }
        if (!refs.isEmpty()) {
            zout.putNextEntry(new ZipEntry("references.zip"));
            ZipOutputStream zzout = new ZipOutputStream(zout);
            try {
                exportNodesWithBinaries(session.getRootNode(), refs, zzout, tti, externalReferences, params);
            } catch (Exception e) {
                logger.error("Cannot export",e);
            }
            zzout.finish();
        }


        zout.finish();
    }

    private void exportSite(final JahiaSite site, OutputStream out, Set<String> externalReferences, Map<String, Object> params)
            throws RepositoryException, SAXException, IOException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(out);

        zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
        exportSiteInfos(zout, site);
        final JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();
        JCRNodeWrapper node = session.getNode("/sites/" +
                site.getSiteKey());
        Set<JCRNodeWrapper> nodes = Collections.singleton(node);
        final HashSet<String> tti = new HashSet<String>();
        tti.add("jnt:templatesFolder");
        tti.add(Constants.JAHIANT_USER);
        exportNodesWithBinaries(session.getRootNode(), nodes, zout, tti,
                externalReferences, params);
        zout.finish();
    }

    public void exportZip(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws RepositoryException, SAXException, IOException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(out);
        Set<JCRNodeWrapper> nodes = new HashSet<JCRNodeWrapper>();
        nodes.add(node);
        exportNodesWithBinaries(exportRoot == null ? node : exportRoot, nodes, zout, new HashSet<String>(), null, params);
        zout.finish();
    }

    public void exportNode(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws RepositoryException, SAXException, IOException, JDOMException {
        TreeSet<JCRNodeWrapper> nodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        nodes.add(node);
        exportNodes(exportRoot == null ? node : exportRoot, nodes, out, new HashSet<String>(), null, params);
    }

    private void exportNodesWithBinaries(JCRNodeWrapper rootNode, Set<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore, Set<String> externalReferences, Map<String, Object> params)
            throws SAXException, IOException, RepositoryException, JDOMException {
        TreeSet<JCRNodeWrapper> liveSortedNodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });

//        final String xsl = (String) params.get(XSL_PATH);
        if (params.containsKey(INCLUDE_LIVE_EXPORT)) {
            final JCRSessionWrapper liveSession = jcrStoreService.getSessionFactory().getCurrentUserSession("live");
            JCRNodeWrapper liveRootNode = liveSession.getNodeByIdentifier(rootNode.getIdentifier());
            for (JCRNodeWrapper node : nodes) {
                try {
                    liveSortedNodes.add(liveSession.getNodeByIdentifier(node.getIdentifier()));
                } catch (ItemNotFoundException e) {
                }
            }

            zout.putNextEntry(new ZipEntry(LIVE_REPOSITORY_XML));

            exportNodes(liveRootNode, liveSortedNodes, zout, typesToIgnore, externalReferences, params);
            zout.closeEntry();
            exportNodesBinary(liveRootNode, liveSortedNodes, zout, typesToIgnore, "/live-content");
        }
        TreeSet<JCRNodeWrapper> sortedNodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
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
        exportNodes(rootNode, sortedNodes, zout, typesToIgnore, externalReferences, params);
        zout.closeEntry();
        exportNodesBinary(rootNode, sortedNodes, zout, typesToIgnore, "/content");
    }

    private void exportNodes(JCRNodeWrapper rootNode, TreeSet<JCRNodeWrapper> sortedNodes, OutputStream outputStream,
                             Set<String> typesToIgnore, Set<String> externalReferences, Map<String, Object> params)
            throws IOException, RepositoryException, SAXException, JDOMException {
        final String xsl = (String) params.get(XSL_PATH);
        final boolean skipBinary = !Boolean.FALSE.equals(params.get(SKIP_BINARY));
        final boolean noRecurse = Boolean.TRUE.equals(params.get(NO_RECURSE));

        OutputStream tmpOut = outputStream;
        if (xsl != null) {
            String filename = rootNode.getName().replace(" ", "_");
            File tempFile = File.createTempFile("exportTemplates-" + filename, "xml");
            tmpOut = new DeferredFileOutputStream(1024 * 1024 * 10, tempFile);
        }
        DataWriter dw = new DataWriter(new OutputStreamWriter(tmpOut, "UTF-8"));
        if (Boolean.TRUE.equals(params.get(SYSTEM_VIEW))) {
            SystemViewExporter exporter = new SystemViewExporter(rootNode.getSession(), dw, !noRecurse, !skipBinary);
            exporter.export(rootNode);
        } else {
            DocumentViewExporter exporter = new DocumentViewExporter(rootNode.getSession(), dw, skipBinary, noRecurse);
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

        dw.flush();
        if (xsl != null) {
            DeferredFileOutputStream stream = (DeferredFileOutputStream) tmpOut;
            InputStream inputStream = new BufferedInputStream(new FileInputStream(stream.getFile()));
            fileCleaningTracker.track(stream.getFile(), inputStream);
            if (stream.isInMemory()) {
                inputStream.close();
                inputStream = new ByteArrayInputStream(stream.getData());
            }
            XSLTransformer xslTransformer = new XSLTransformer(xsl);
            SAXBuilder saxBuilder = new SAXBuilder(false);
            Document document = saxBuilder.build(inputStream);
            Document document1 = xslTransformer.transform(document);
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            xmlOutputter.output(document1, outputStream);
        }
    }

    private void exportNodesBinary(JCRNodeWrapper root, SortedSet<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore, String basepath) throws IOException, RepositoryException {
        byte[] buffer = new byte[4096];
        for (Iterator<JCRNodeWrapper> iterator = nodes.iterator(); iterator.hasNext();) {
            JCRNodeWrapper file = iterator.next();
            exportNodeBinary(root, file, zout, typesToIgnore, buffer, basepath, new HashSet<String>());
        }
    }

    private void exportNodeBinary(JCRNodeWrapper root, JCRNodeWrapper node, ZipOutputStream zout, Set<String> typesToIgnore, byte[] buffer, String basepath, Set<String> exportedFiles) throws IOException, RepositoryException {
        int bytesIn;
        if (node.getProvider().isExportable() && !typesToIgnore.contains(node.getPrimaryNodeTypeName())) {
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                Node child = ni.nextNode();
                if (child.isNodeType("nt:resource")) {
                    if (!exportedFiles.contains(child.getPath())) {
                        exportedFiles.add(child.getPath());
                        InputStream is = child.getProperty("jcr:data").getBinary().getStream();
                        if (is != null) {
                            try {
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
                            } finally {
                                IOUtils.closeQuietly(is);
                            }
                        }
                    }
                }
                if (child instanceof JCRNodeWrapper) {
                    exportNodeBinary(root, (JCRNodeWrapper) child, zout, typesToIgnore, buffer, basepath, exportedFiles);
                }
            }
        }
    }

    private void exportSiteInfos(OutputStream out, JahiaSite s) throws IOException {
        Properties p = new OrderedProperties();
        p.setProperty("sitetitle", s.getTitle());
        p.setProperty("siteservername", s.getServerName());
        p.setProperty("sitekey", s.getSiteKey());
        p.setProperty("description", s.getDescr());
        p.setProperty("templatePackageName", s.getTemplateFolder());
        p.setProperty("mixLanguage", Boolean.toString(s.isMixLanguagesActive()));
        p.setProperty("defaultLanguage", s.getDefaultLanguage());
        int i=1;
        for (String s1 : s.getInstalledModules()) {
            p.setProperty("installedModules."+(i++), s1);
        }

        Set<String> v = s.getLanguages();
        for (String sls : v) {
            p.setProperty("language." + sls + ".activated", s.getInactiveLiveLanguages().contains(sls) ? "false" : "true");
            p.setProperty("language." + sls + ".mandatory", "" + s.getMandatoryLanguages().contains(sls));
        }
        for (String sls : s.getInactiveLanguages()) {
            p.setProperty("language." + sls + ".disabledCompletely", "true");
        }

        if (s.isDefault()) {
            p.setProperty("defaultSite", "true");
        }

        p.store(out, "");
    }

    public void importSiteZip(final File file, final JahiaSite site, final Map<Object, Object> infos) throws RepositoryException, IOException {
        importSiteZip(file, site, infos, null, null);
    }

    public void importSiteZip(File file, JahiaSite site, Map<Object, Object> infos, String legacyMappingFilePath, String legacyDefinitionsFilePath) throws RepositoryException, IOException {
        final CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler();
        final UsersImportHandler usersImportHandler = new UsersImportHandler(site);
        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null);
        boolean legacyImport = false;
        List<String[]> catProps = null;
        List<String[]> userProps = null;

        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, List<String>> references = new HashMap<String, List<String>>();

        getFileList(file, sizes, fileList);

        Map<String, String> pathMapping = session.getPathMapping();
        NoCloseZipInputStream zis;
        if (sizes.containsKey(USERS_XML)) {
            // Import users first
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
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
                zis.reallyClose();
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
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(SITE_PROPERTIES)) {
                        importSiteProperties(zis, site);
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                zis.reallyClose();
            }
        }

        if (sizes.containsKey(REPOSITORY_XML)) {
            // Parse import file to detect sites
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(REPOSITORY_XML)) {
                        DocumentViewValidationHandler h = new DocumentViewValidationHandler();
                        h.setSession(session);
                        List<ImportValidator> validators = new ArrayList<ImportValidator>();
                        SitesValidator sitesValidator = new SitesValidator();
                        validators.add(sitesValidator);
                        h.setValidators(validators);
                        handleImport(zis, h);

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
                        break;
                    }
                    zis.closeEntry();
                }
            } finally {
                zis.reallyClose();
            }

            importZip(null, file, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE, session, Sets.newHashSet(USERS_XML, CATEGORIES_XML));
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
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
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
                                    String contentType = JahiaContextLoaderListener.getServletContext().getMimeType(filename);
                                    ensureFile(session, name, zis, contentType, site);
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
                        reg = new NodeTypeRegistry();
                        try {
                            if (legacyImport) {
                                JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(new InputStreamReader(zis, "UTF-8"), zipentry.getName(),
                                        file.getName(), reg);
                                r.parse();
                            } else {
                                JahiaCndReader r = new JahiaCndReader(new InputStreamReader(zis, "UTF-8"), zipentry.getName(),
                                        file.getName(), reg);
                                r.parse();
                            }
                        } catch (ParseException e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else if (name.equals(DEFINITIONS_MAP)) {
                        mapping = new DefinitionsMapping();
                        mapping.load(zis);

                    }
                    zis.closeEntry();
                }
            } finally {
                zis.reallyClose();
            }
        }

        // Import legacy content from 5.x and 6.x
        if (legacyImport) {
            if(legacyMappingFilePath!=null) {
                mapping = new DefinitionsMapping();
                mapping.load(new FileInputStream(legacyMappingFilePath));
            }
            if(legacyDefinitionsFilePath!=null) {
                reg = new NodeTypeRegistry();
                try {
                    File cndFile = new File(legacyDefinitionsFilePath);
                    JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(new InputStreamReader(new FileInputStream(legacyDefinitionsFilePath), "UTF-8"), cndFile.getName(),
                            file.getName(), reg);
                    r.parse();
                } catch (ParseException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            // Old import
            JCRNodeWrapper siteFolder = session.getNode("/sites/" + site.getSiteKey());

            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null)
                        break;
                    String name = zipentry.getName();
                    if (name.equals(FILESACL_XML)) {
                        importFilesAcl(site, file, zis, mapping, fileList);
                    } else if (name.startsWith("export")) {
                        String languageCode;
                        if (name.indexOf("_") != -1) {
                            languageCode = name.substring(7, name.lastIndexOf("."));
                        } else {
                            languageCode = site.getLanguagesAsLocales().iterator().next().toString();
                        }
                        zipentry.getSize();

                        LegacyImportHandler importHandler = new LegacyImportHandler(session, siteFolder, reg, mapping,LanguageCodeConverters.languageCodeToLocale(languageCode),infos != null ? (String) infos.get("originatingJahiaRelease") : null,legacyPidMappingTool);
                        importHandler.setReferences(references);

                        InputStream documentInput = zis;
                        if (this.xmlContentTransformers != null && this.xmlContentTransformers.size() > 0) {
                            documentInput = new ZipInputStream(new FileInputStream(file));
                            while (!name.equals(((ZipInputStream) documentInput).getNextEntry().getName())) ;
                            byte[] buffer = new byte[2048];
                            File document = File.createTempFile("export_" + languageCode + "_initial_", ".xml");
                            final OutputStream output = new BufferedOutputStream(new FileOutputStream(document), 2048);
                            int count = 0;
                            while ((count = documentInput.read(buffer, 0, 2048)) > 0) {
                                output.write(buffer, 0, count);
                            }
                            output.flush();
                            output.close();
                            documentInput.close();
                            for (XMLContentTransformer xct : xmlContentTransformers) {
                                document = xct.transform(document);
                            }
                            documentInput = new FileInputStream(document);
                        }

                        handleImport(documentInput, importHandler);
                        siteFolder.getSession().save(JCRObservationManager.IMPORT);
                    }
                    zis.closeEntry();
                }
            } finally {
                zis.reallyClose();
            }

        }

        categoriesImportHandler.setUuidProps(catProps);
        usersImportHandler.setUuidProps(userProps);

        // session.save();
        ReferencesHelper.resolveCrossReferences(session, references);
        session.save(JCRObservationManager.IMPORT);
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
                        if (!parentDir.isCheckedOut()) {
                            session.getWorkspace().getVersionManager()
                                    .checkout(parentDir.getPath());
                        }
                        JCRNodeWrapper createdDir = parentDir.createCollection(dirName);
                        createdDir.saveSession();
                    }
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                }
                dir = session.getNode(path);
                logger.debug("Folder created " + path);
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
                logger.debug("Add file to " + parentDir.getPath());
                try {
                    if (!parentDir.isCheckedOut()) {
                        session.getWorkspace().getVersionManager()
                                .checkout(parentDir.getPath());
                    }
                    JCRNodeWrapper res = parentDir.uploadFile(name, inputStream, type);
                    logger.debug("File added -> " + res);
                    res.saveSession();
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                }
            } else {
                logger.debug("Try to add file " + path + " - already exists");
            }
        } catch (RepositoryException e) {
            logger.debug("Cannot add file", e);
        }

    }

    private void importFilesAcl(JahiaSite site, File file, InputStream is, DefinitionsMapping mapping, List<String> fileList) {
        handleImport(is, new FilesAclImportHandler(site, mapping, file, fileList));
    }

    private void importSiteProperties(InputStream is, JahiaSite site) throws IOException {
        if (site.getSiteKey().equals(JahiaSitesBaseService.SYSTEM_SITE_KEY)) {
            return;
        }
        Properties p = new Properties();
        p.load(is);
        Set<Object> keys = p.keySet();
        boolean isMultiLang = true;
        final Set<String> languages = new HashSet<String>();
        final Set<String> inactiveLanguages = site.getInactiveLanguages();
        inactiveLanguages.clear();
        final Set<String> inactiveLiveLanguages = site.getInactiveLiveLanguages();
        inactiveLiveLanguages.clear();
        final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
        mandatoryLanguages.clear();

        String templateSet = site.getTemplatePackageName();
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        try {
            templateManagerService.deployModule("/templateSets/" + templateSet, "/sites/" + site.getSiteKey(), JCRSessionFactory.getInstance().getCurrentUser().getUsername());
        } catch (RepositoryException e) {
            logger.error("Cannot deploy module "+templateSet,e);
        }

        String defaultLanguage = null;
        String lowestRankLanguage = null;
        int currentRank = 0;
        for (Object key : keys) {
            String property = (String) key;
            String value = p.getProperty(property);
            StringTokenizer st = new StringTokenizer(property, ".");
            String firstKey = st.nextToken();

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
            } else if (firstKey.equals("description")) {
                site.setDescr(value);
            } else if (firstKey.startsWith("defaultSite") && "true".equals(
                    value) && sitesService.getDefaultSite() == null) {
                sitesService.setDefaultSite(site);
            } else if (firstKey.equals("installedModules")) {
                if (!site.getInstalledModules().contains(value) && !templateSet.equals(value)) {
                    try {
                        templateManagerService.deployModule("/templateSets/" + value, "/sites/" + site.getSiteKey(), JCRSessionFactory.getInstance().getCurrentUser().getUsername());
                    } catch (RepositoryException e) {
                        logger.error("Cannot deploy module "+value,e);
                    }
                }
            }
        }
        @SuppressWarnings("unchecked")
        Set<String> siteLangs = ListOrderedSet.decorate(new LinkedList<String>(languages));
        if (!siteLangs.isEmpty()) {
            if (!isMultiLang) {
                Set<String> singleLang = new HashSet<String>();
                singleLang.add(siteLangs.iterator().next());
                site.setLanguages(singleLang);
                site.setMandatoryLanguages(singleLang);
            } else {
                site.setLanguages(siteLangs);
                site.setInactiveLanguages(inactiveLanguages);
                site.setInactiveLiveLanguages(inactiveLiveLanguages);
                site.setMandatoryLanguages(mandatoryLanguages);
            }
            if (defaultLanguage == null) {
                defaultLanguage = StringUtils.isEmpty(lowestRankLanguage) ? siteLangs.iterator().next() : lowestRankLanguage;
            }
            site.setDefaultLanguage(defaultLanguage);
            try {
                sitesService.updateSite(site);
                JahiaSite jahiaSite = sitesService.getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);
                jahiaSite.getLanguages().addAll(site.getLanguages());
                sitesService.updateSite(jahiaSite);
            } catch (JahiaException e) {
                logger.error("Cannot update site", e);
            }
        } else {
            logger.error("Unable to find site languages in the provided site.properties descriptor. Skip importing site settings.");
        }
    }


    private List<String[]> importCategoriesAndGetUuidProps(InputStream is, CategoriesImportHandler importHandler) {
        handleImport(is, importHandler);
        return importHandler.getUuidProps();
    }

    public void importCategories(Category rootCategory, InputStream is) {
        CategoriesImportHandler importHandler = new CategoriesImportHandler();
        importHandler.setRootCategory(rootCategory);
        importCategoriesAndGetUuidProps(is, importHandler);
    }

    public List<String[]> importUsers(File file) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        try {
            return importUsers(is, new UsersImportHandler());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private List<String[]> importUsers(InputStream is, UsersImportHandler importHandler) {
        handleImport(is, importHandler);
        return importHandler.getUuidProps();
    }

    public List<String[]> importUsersFromZip(File file, JahiaSite site) throws IOException  {
        return importUsersFromZip(file, new UsersImportHandler(site));
    }

    private List<String[]> importUsersFromZip(File file, UsersImportHandler usersImportHandler)
            throws IOException {
        NoCloseZipInputStream zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        List<String[]> userProps = null;
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null)
                    break;
                String name = zipentry.getName();
                if (name.equals("users.xml")) {
                    userProps = importUsers(zis, usersImportHandler);
                    break;
                }
                zis.closeEntry();
            }
        } finally {
            zis.reallyClose();
        }
        return userProps;
    }

    private void handleImport(InputStream is, DefaultHandler h) {
        try {
            SAXParserFactory factory;

            factory = new SAXParserFactoryImpl();

            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();

            parser.parse(is, h);
        } catch (SAXParseException e) {
            logger.error("Cannot import - File is not a valid XML", e);
        } catch (Exception e) {
            logger.error("Cannot import", e);
        }
    }

    public int detectXmlFormat(InputStream is) {
        XMLFormatDetectionHandler handler = new XMLFormatDetectionHandler();
        try {
            SAXParserFactory factory;

            factory = new SAXParserFactoryImpl();

            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();

            parser.parse(is, handler);
        } catch (Exception e) {
        }
        return handler.getType();
    }

    public void importXML(final String parentNodePath, InputStream content, final int rootBehavior) throws IOException, RepositoryException, JahiaException {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("import-xml-", "");
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
                    final File contentFile = tempFile;
                    if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null);
                        InputStream is = null;
                        try {
                            is = new BufferedInputStream(new FileInputStream(contentFile));
                            session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, rootBehavior);
                        } catch (IOException e) {
                            throw new RepositoryException(e);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        session.save(JCRObservationManager.IMPORT);
                    } else {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                InputStream is = null;
                                try {
                                    is = new BufferedInputStream(new FileInputStream(contentFile));
                                    session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, rootBehavior);
                                    session.save(JCRObservationManager.IMPORT);
                                } catch (IOException e) {
                                    throw new RepositoryException(e);
                                } finally {
                                    IOUtils.closeQuietly(is);
                                }
                                return Boolean.TRUE;
                            }
                        });
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

    public void importZip(final String parentNodePath, final File file, final int rootBehavior) throws IOException, RepositoryException {
        if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
            JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null);
            importZip(parentNodePath, file, rootBehavior, session);
        } else {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        importZip(parentNodePath, file, rootBehavior, session);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    }
                    return null;
                }
            });
        }
    }

    /**
     * Validates a JCR content import file in document format and returns expected failures.
     *
     * @param session
     *            current JCR session instance
     * @param is
     *            the input stream with a JCR content in document format
     * @param contentType the content type for the content
     * @param installedModules the list of installed modules, where the first element is a template set name
     * @return the validation result
     * @since Jahia 6.6
     */
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
                    if (zipentry.getName().endsWith("xml")) {
                        handleImport(zis, documentViewValidationHandler);
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
            handleImport(is, documentViewValidationHandler);
        }
        return documentViewValidationHandler.getResults();
    }

    public void importZip(String parentNodePath, File file, int rootBehaviour, JCRSessionWrapper session)
            throws IOException, RepositoryException {
        importZip(parentNodePath, file, rootBehaviour, session, Collections.<String>emptySet());
    }

    public void importZip(String parentNodePath, File file, int rootBehaviour, JCRSessionWrapper session, Set<String> filesToIgnore)
            throws IOException, RepositoryException {
        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, List<String>> references = new HashMap<String, List<String>>();

        getFileList(file, sizes, fileList);
        NoCloseZipInputStream zis;

        boolean importLive = sizes.containsKey(LIVE_REPOSITORY_XML);

        List<String> liveUuids = null;
        if (importLive) {
            // Import live content
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(LIVE_REPOSITORY_XML)) {
                        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);

                        documentViewImportHandler.setReferences(references);
                        documentViewImportHandler.setRootBehavior(rootBehaviour);
                        documentViewImportHandler.setBaseFilesPath("/live-content");

                        handleImport(zis, documentViewImportHandler);
                        session.save(JCRObservationManager.IMPORT);

                        if (rootBehaviour == DocumentViewImportHandler.ROOT_BEHAVIOUR_RENAME) {
                            // Use path mapping to get new name
                            rootBehaviour = DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE;
                        }

                        ReferencesHelper.resolveCrossReferences(session, references);
                        session.save(JCRObservationManager.IMPORT);

                        liveUuids = documentViewImportHandler.getUuids();

                        ServicesRegistry.getInstance().getJCRPublicationService().publish(documentViewImportHandler.getUuids(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null);
                        String label = "published_at_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(GregorianCalendar.getInstance().getTime());
                        JCRVersionService.getInstance().addVersionLabel(documentViewImportHandler.getUuids(), label, Constants.LIVE_WORKSPACE);
                        break;
                    }
                    zis.closeEntry();

                }
            } catch (Exception e) {
                logger.error("Cannot import", e);
            } finally {
                zis.reallyClose();
            }
        }

        // Import repository content
        zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML)) {
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList);
                    if (importLive) {
                        // Restore publication status
                        Set<String> props = new HashSet<String>(documentViewImportHandler.getPropertiesToSkip());
                        props.remove("j:lastPublished");
                        props.remove("j:lastPublishedBy");
                        props.remove("j:published");
                        documentViewImportHandler.setPropertiesToSkip(props);
                        documentViewImportHandler.setEnforceUuid(true);
                        documentViewImportHandler.setUuidBehavior(DocumentViewImportHandler.IMPORT_UUID_COLLISION_MOVE_EXISTING);
                    }
                    documentViewImportHandler.setReferences(references);
                    documentViewImportHandler.setRootBehavior(rootBehaviour);

                    handleImport(zis, documentViewImportHandler);

                    if (importLive) {
                        liveUuids.removeAll(documentViewImportHandler.getUuids());
                        Collections.reverse(liveUuids);
                        for (String uuid : liveUuids) {
                            // Uuids have been imported in live but not in default : need to be removed
                            session.getNodeByIdentifier(uuid).remove();
                        }
                    }
                    session.save(JCRObservationManager.IMPORT);
                } else if (name.endsWith(".xml") && !name.equals(REPOSITORY_XML) && !name.equals(LIVE_REPOSITORY_XML) && !filesToIgnore.contains(name)) {
                    String thisPath = (parentNodePath != null ? (parentNodePath + (parentNodePath.endsWith("/") ? "" : "/")) : "") + StringUtils.substringBefore(name,".xml");
                    importXML(thisPath, zis, rootBehaviour);
                }
                zis.closeEntry();
            }
        } catch (Exception e) {
            logger.error("Cannot import", e);
        } finally {
            zis.reallyClose();
        }

        if (importLive) {
            // Import user generated content
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(LIVE_REPOSITORY_XML) && jcrStoreService.getSessionFactory().getCurrentUser()!=null) {
                        JCRSessionWrapper liveSession = jcrStoreService.getSessionFactory().getCurrentUserSession("live",null,null);

                        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(liveSession, parentNodePath, file, fileList);

                        documentViewImportHandler.setImportUserGeneratedContent(true);
                        documentViewImportHandler.setReferences(references);
                        documentViewImportHandler.setRootBehavior(rootBehaviour);
                        documentViewImportHandler.setBaseFilesPath("/live-content");
                        liveSession.getPathMapping().putAll(session.getPathMapping());
                        handleImport(zis, documentViewImportHandler);
                        liveSession.save(JCRObservationManager.IMPORT);

//                        ReferencesHelper.resolveCrossReferences(liveSession, references);
//                        liveSession.save(JCRObservationManager.IMPORT);

                        break;
                    }
                    zis.closeEntry();

                }
            } catch (Exception e) {
                logger.error("Cannot import", e);
            } finally {
                zis.reallyClose();
            }
        }
    }

    private void getFileList(File file, Map<String, Long> sizes, List<String> fileList) throws IOException {
        NoCloseZipInputStream zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(file)));
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName().replace('\\', '/');
                if (name.endsWith(".xml")) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(zis));
                    long i = 0;
                    while (br.readLine() != null) {
                        i++;
                    }
                    sizes.put(name, i);
                } else {
                    sizes.put(name, zipentry.getSize());
                }
                if (name.contains("/")) {
                    fileList.add("/" + name);
                }
                zis.closeEntry();
            }
        } finally {
            zis.reallyClose();
        }
    }

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

	private void setObserverInterval(long observerInterval) {
    	this.observerInterval = observerInterval;
    }

    private List<XMLContentTransformer> xmlContentTransformers;

    public void setXmlContentTransformers(final List<XMLContentTransformer> xmlContentTransformers) {
        this.xmlContentTransformers = xmlContentTransformers;
    }

    private LegacyPidMappingTool legacyPidMappingTool = null;

    public void setLegacyPidMappingTool(LegacyPidMappingTool legacyPidMappingTool) {
        this.legacyPidMappingTool = legacyPidMappingTool;
    }
}

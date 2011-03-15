/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.importexport;

import org.apache.commons.collections.set.ListOrderedSet;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.slf4j.Logger;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
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

import javax.jcr.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service used to perform all import/export operations for content and documents.
 * User: toto
 * Date: 9 dÔøΩc. 2004
 * Time: 15:01:31
 */
public class ImportExportBaseService extends JahiaService implements ImportExportService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ImportExportBaseService.class);

    private static ImportExportBaseService instance;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(ImportExportService.DATE_FORMAT);

    private static final String FILESACL_XML = "filesacl.xml";
    private static final String REPOSITORY_XML = "repository.xml";
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
                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                JCRNodeWrapper dest = session.getNode("/imports");
                                for (File file : files) {
                                    try {
                                        FileInputStream is = new FileInputStream(file);
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
            throws JahiaException, RepositoryException, IOException, SAXException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(outputStream);

        ZipEntry anEntry = new ZipEntry(EXPORT_PROPERTIES);
        zout.putNextEntry(anEntry);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout));
        bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
        bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
        bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
        bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
        bw.flush();

        // Add system site for export
        boolean systemFound = false;
        for (JahiaSite jahiaSite : sites) {
            if (jahiaSite.getSiteKey().equals("systemsite")) {
                systemFound = true;
                break;
            }
        }
        if (!systemFound) {
            anEntry = new ZipEntry("systemsite.zip");
            zout.putNextEntry(anEntry);
            exportSystemSite(zout, params);
        }

        for (JahiaSite jahiaSite : sites) {
            anEntry = new ZipEntry(jahiaSite.getSiteKey() + ".zip");
            zout.putNextEntry(anEntry);
            exportSite(jahiaSite, zout, params);
        }

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();

        // export shared files -->
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
        try {
            files.add(session.getNode("/users"));
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        zout.putNextEntry(new ZipEntry("users.zip"));
        ZipOutputStream zzout = new ZipOutputStream(zout);
        Set<String> tti = new HashSet<String>();
        tti.add(Constants.JAHIANT_VIRTUALSITE);
        try {
            exportNodesWithBinaries(session.getRootNode(), files, zzout, tti, params);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        zzout.finish();
        zout.finish();
    }

    private void exportSite(final JahiaSite site, OutputStream out, Map<String, Object> params)
            throws JahiaException, RepositoryException, SAXException, IOException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(out);

        zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
        exportSiteInfos(zout, site);
        final JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();
        Set<JCRNodeWrapper> nodes = Collections.singleton(session.getNode("/sites/"+
                site.getSiteKey()));
        final HashSet<String> tti = new HashSet<String>();
        tti.add("jnt:templatesFolder");
        exportNodesWithBinaries(session.getRootNode(), nodes, zout, tti,
                params);
        zout.finish();
    }

    private void exportSystemSite(OutputStream out, Map<String, Object> params)
            throws JahiaException, RepositoryException, SAXException, IOException, JDOMException {
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);

        ZipOutputStream zout = new ZipOutputStream(out);

        final JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();
        Set<JCRNodeWrapper> nodes = new HashSet<JCRNodeWrapper>();
        nodes.add(session.getNode("/sites/" + site.getSiteKey() + "/files"));
        nodes.add(session.getNode("/sites/" + site.getSiteKey() + "/contents"));
        nodes.add(session.getNode("/sites/" + site.getSiteKey() + "/portlets"));
        nodes.add(session.getNode("/sites/" + site.getSiteKey() + "/categories"));

        final HashSet<String> tti = new HashSet<String>();
        tti.add("jnt:templatesFolder");
        exportNodesWithBinaries(session.getRootNode(), nodes, zout, tti, params);
        zout.finish();
    }

    public void exportZip(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws JahiaException, RepositoryException, SAXException, IOException, JDOMException {
        ZipOutputStream zout = new ZipOutputStream(out);
        Set<JCRNodeWrapper> nodes = new HashSet<JCRNodeWrapper>();
        nodes.add(node);
        exportNodesWithBinaries(exportRoot == null ? node : exportRoot, nodes, zout, new HashSet<String>(),params);
        zout.finish();
    }

    public void exportNode(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws JahiaException, RepositoryException, SAXException, IOException, JDOMException {
        TreeSet<JCRNodeWrapper> nodes = new TreeSet<JCRNodeWrapper>();
        nodes.add(node);
        exportNodes(exportRoot == null ? node : exportRoot, nodes, out, new HashSet<String>(), params);
    }

    private void exportNodesWithBinaries(JCRNodeWrapper rootNode, Set<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore, Map<String, Object> params)
            throws SAXException, IOException, RepositoryException, JDOMException {
        TreeSet<JCRNodeWrapper> sortedNodes = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        sortedNodes.addAll(nodes);
        zout.putNextEntry(new ZipEntry(REPOSITORY_XML));

        OutputStream outputStream = zout;
//        final String xsl = (String) params.get(XSL_PATH);
        exportNodes(rootNode, sortedNodes, outputStream, typesToIgnore, params);
        zout.closeEntry();
        exportNodesBinary(rootNode, sortedNodes, zout, typesToIgnore);
    }

    private void exportNodes(JCRNodeWrapper rootNode, TreeSet<JCRNodeWrapper> sortedNodes, OutputStream outputStream,
                             Set<String> typesToIgnore, Map<String, Object> params)
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
            typesToIgnore.add("rep:system");
            exporter.setTypesToIgnore(typesToIgnore);
            exporter.export(rootNode, sortedNodes);
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

    private void exportNodesBinary(JCRNodeWrapper root, SortedSet<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore) throws IOException, RepositoryException {
        byte[] buffer = new byte[4096];
        for (Iterator<JCRNodeWrapper> iterator = nodes.iterator(); iterator.hasNext();) {
            JCRNodeWrapper file = iterator.next();
            exportNodeBinary(root, file, zout, typesToIgnore, buffer);
        }
    }

    private void exportNodeBinary(JCRNodeWrapper root, JCRNodeWrapper node, ZipOutputStream zout, Set<String> typesToIgnore, byte[] buffer) throws IOException, RepositoryException {
        int bytesIn;
        if (node.getProvider().isExportable() && !typesToIgnore.contains(node.getPrimaryNodeTypeName())) {
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                Node child = ni.nextNode();
                if (child.isNodeType("nt:resource")) {
                    InputStream is = child.getProperty("jcr:data").getBinary().getStream();
                    if (is != null) {
                        try {
                            String path = node.getPath();
                            if (root.getPath().equals("/")) {
                                path = "/content" + path;
                            } else {
                                path = path.substring(root.getParent().getPath().length());
                            }
                            String name = child.getName().replace(":", "_");
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
                if (child instanceof JCRNodeWrapper) {
                    exportNodeBinary(root, (JCRNodeWrapper) child, zout, typesToIgnore, buffer);
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


        // get all jahiaGAprofiles
//        final String cntProfile = s.getSettings().getProperty("profileCnt_" + s.getSiteKey());
//        if (cntProfile != null) {
//            p.setProperty("profileCnt_" + s.getSiteKey(), cntProfile);
//        }
//        Iterator<Object> it = s.getSettings().keySet().iterator();
//        while (it.hasNext()) {
//            String key = (String) it.next();
//            if (key.startsWith("jahiaGAprofile")) {
//                String jahiaProfileName = (String) s.getSettings().get(key);
//                p.setProperty(key, jahiaProfileName);
//                Iterator<Object> it2 = s.getSettings().keySet().iterator();
//                while (it2.hasNext()) {
//                    String gaProp = (String) it2.next();
//                    if (gaProp.startsWith(jahiaProfileName)) {
//                        if (gaProp.endsWith("gaPassword")) {
//                            if (SettingsBean.getInstance().isGmailPasswordExported()) {
//                                p.setProperty(gaProp, (String) s.getSettings().get(gaProp));
//                            } else {
//                                p.setProperty(gaProp, "");
//                            }
//                        }
//                        p.setProperty(gaProp, (String) s.getSettings().get(gaProp));
//                    }
//                }
//            }
//        }

        Set<String> v = s.getLanguages();
        for (String sls : v) {
            p.setProperty("language." + sls + ".activated", "true");
            p.setProperty("language." + sls + ".mandatory", "" + s.getMandatoryLanguages().contains(sls));
        }


//        Properties settings = s.getSettings();
//
//        for (Iterator<Object> iterator = settings.keySet().iterator(); iterator.hasNext();) {
//            String s1 = (String) iterator.next();
//            if (JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY.equals(s1)
//                    || s1.startsWith("prod_") || s1.startsWith("html_")
//                    || s1.startsWith("wai_") || s1.startsWith("url_")) {
//                p.setProperty(s1, settings.getProperty(s1));
//            }
//        }
        if (s.isDefault()) {
            p.setProperty("defaultSite", "true");
        }

        p.save(out, "");
    }

    public void importSiteZip(File file, JahiaSite site, Map<Object, Object> infos) throws RepositoryException, IOException {
        boolean legacyImport = false;
        CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler();
        UsersImportHandler usersImportHandler = new UsersImportHandler(site);

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null,null,null);
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
            zis = new NoCloseZipInputStream(new FileInputStream(file));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
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

        if (sizes.containsKey(REPOSITORY_XML)) {
            // Import repository content
            zis = new NoCloseZipInputStream(new FileInputStream(file));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(REPOSITORY_XML)) {
                        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, null, file, fileList, (site != null ? site.getSiteKey(): null));

                        documentViewImportHandler.setReferences(references);
                        documentViewImportHandler.setNoRoot(true);

                        handleImport(zis, documentViewImportHandler);
                        session.save();
                        break;
                    }
                    zis.closeEntry();
                }
            } catch (Exception e) {
                logger.error("Cannot import", e);
            } finally {
                zis.reallyClose();
            }
        } else {
            // No repository descriptor - prepare to import files directly
            pathMapping = session.getPathMapping();
            pathMapping.put("/", "/sites/"+site.getSiteKey()+"/files/");
        }

        NodeTypeRegistry reg = null;
        DefinitionsMapping mapping = null;

        // Import additional files - site.properties, old cateogries.xml , sitepermissions.xml
        // and eventual plain file from 5.x imports
        if (!sizes.containsKey(REPOSITORY_XML) || sizes.containsKey(SITE_PROPERTIES) ||
                sizes.containsKey(CATEGORIES_XML)|| sizes.containsKey(SITE_PERMISSIONS_XML) ||
                sizes.containsKey(DEFINITIONS_CND)|| sizes.containsKey(DEFINITIONS_MAP)
                ) {
            zis = new NoCloseZipInputStream(new FileInputStream(file));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
					if (name.indexOf('\\') > -1) {
						name = name.replace('\\', '/');
					}
                    if (name.indexOf('/') > -1) {
                        if (!sizes.containsKey(REPOSITORY_XML)) {
                            // No repository descriptor - Old import format only
                            name = "/" + name;
                            if (!zipentry.isDirectory()) {
                                try {
                                    if (name.startsWith("/content/sites")) {
                                        name = pathMapping.get("/")
                                                + StringUtils.stripStart(name
                                                        .replaceFirst("/content/sites/[^/]+/files/", ""),
                                                        "/");
                                    } else if (name.startsWith("/content/users")) {
                                        name = name.replaceFirst(
                                                        "/content/users/([^/]+)/",
                                                        "/users/$1/files/");
                                    } else if (name.startsWith("/users")) {
                                        name = name.replaceFirst(
                                                        "/users/([^/]+)/",
                                                        "/users/$1/files/");                                        
                                    } else {
                                        name = pathMapping.get("/")
                                                + StringUtils.stripStart(name, "/");
                                    }
                                    String filename = name.substring(name.lastIndexOf('/') + 1);
                                    String contentType = JahiaContextLoaderListener.getServletContext().getMimeType(filename);
                                    ensureFile(jcrStoreService.getSessionFactory().getCurrentUserSession(), name, zis, contentType, site);
                                } catch (Exception e) {
                                    logger.error("Cannot upload file " + zipentry.getName(), e);
                                }
                            } else {
                                ensureDir(jcrStoreService.getSessionFactory().getCurrentUserSession(), name, site);
                            }
                        }
                    } else if (name.equals(SITE_PROPERTIES)) {
                        importSiteProperties(zis, site);
                    } else if (name.equals(CATEGORIES_XML)) {
                        catProps = importCategoriesAndGetUuidProps(zis, categoriesImportHandler);
                    } else if (name.equals(DEFINITIONS_CND)) {
                        reg = new NodeTypeRegistry();
                        try {
                            if (legacyImport) {
                                JahiaCndReaderLegacy r = new JahiaCndReaderLegacy(new InputStreamReader(zis, "UTF-8"),zipentry.getName(), file.getName(), reg);
                                r.parse();
                            } else {
                                JahiaCndReader r = new JahiaCndReader(new InputStreamReader(zis, "UTF-8"),zipentry.getName(), file.getName(), reg);
                                r.parse();
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
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
            // Old import
            JCRNodeWrapper siteFolder = jcrStoreService.getSessionFactory().getCurrentUserSession().getNode("/sites/" + site.getSiteKey());

            zis = new NoCloseZipInputStream(new FileInputStream(file));
            try {
                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(FILESACL_XML)) {
                        importFilesAcl(site, zis, mapping);
                    } else if (name.startsWith("export")) {
                        String languageCode;
                        if (name.indexOf("_") != -1) {
                            languageCode = name.substring(7, name.lastIndexOf("."));
                        } else {
                            languageCode = site.getLanguagesAsLocales().iterator().next().toString();
                        }
                        zipentry.getSize();

                        LegacyImportHandler importHandler = new LegacyImportHandler(session,
                                siteFolder, reg, mapping, LanguageCodeConverters
                                        .languageCodeToLocale(languageCode),
                                infos != null ? (String)infos.get("originatingJahiaRelease")
                                        : null);
                        importHandler.setReferences(references);
                        handleImport(zis, importHandler);
                        siteFolder.getSession().save();
                    }
                    zis.closeEntry();
                }
            } finally {
                zis.reallyClose();
            }

        }

        categoriesImportHandler.setUuidProps(catProps);
        usersImportHandler.setUuidProps(userProps);

//        session.save();
        ReferencesHelper.resolveCrossReferences(session, references);
        session.save();
    }


    private JCRNodeWrapper ensureDir(JCRSessionWrapper session, String name, JahiaSite site) throws RepositoryException {
        JCRNodeWrapper dir;
        try {
            dir = session.getNode(name);

            String current = name;

            while (current.lastIndexOf('/') > 0) {
                JCRNodeWrapper currentNode = session.getNode(current);

                if (Constants.JAHIANT_VIRTUALSITE.equals(currentNode.getPrimaryNodeTypeName())) {
                    if (currentNode.getName().equals(site.getSiteKey())) {
                        break;
                    }
                    String newName = current.substring(0, current.lastIndexOf('/')) + "/" + site.getSiteKey();
                    session.getPathMapping().put(current, newName);
                    name = name.replace(current, newName);

                    return ensureDir(session, name, site);
                }
                int endIndex = current.lastIndexOf('/');
                current = current.substring(0, endIndex);
            }
//            if ("jnt:virtualsite".equals(dir.getType())) {
//                int endIndex = name.lastIndexOf('/');
//                dir = JahiaWebdavBaseService.getInstance().getDAVFileAccess(name.substring(0, endIndex)+"/"+site.getSiteKey(), jParams.getUser());
//            }
        } catch (PathNotFoundException pnfe) {
            int endIndex = name.lastIndexOf('/');
            if (endIndex == -1) {
                logger.warn("Cannot create folder " + name);
                return null;
            }
            JCRNodeWrapper parentDir = ensureDir(session, name.substring(0, endIndex), site);
            if (parentDir == null) {
                return null;
            }
            if (parentDir.isNodeType(Constants.JAHIANT_VIRTUALSITES_FOLDER)) {
                dir = parentDir.getNode(site.getSiteKey());
            } else {
                try {
                    parentDir.createCollection(name.substring(name.lastIndexOf('/') + 1));
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                }
                dir = session.getNode(name);
                logger.debug("Folder created " + name);
            }
        }
        return dir;
    }

    private void ensureFile(JCRSessionWrapper session, String path, InputStream inputStream, String type, JahiaSite destSite) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        try {
            JCRNodeWrapper parentDir = ensureDir(session, path.substring(0, path.lastIndexOf('/')), destSite);
            if (parentDir == null) {
                return;
            }
            if (!parentDir.hasNode(name)) {
                logger.debug("Add file to " + parentDir.getPath());
                try {
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

    private void importFilesAcl(JahiaSite site, InputStream is, DefinitionsMapping mapping) {
        handleImport(is, new FilesAclImportHandler(site, mapping));
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
        final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
        mandatoryLanguages.clear();
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
                    if (Boolean.valueOf(p.getProperty("language." + lang + ".mandatory", "false"))) {
                        mandatoryLanguages.add(lang);
                    }
                    if (StringUtils.isEmpty(lowestRankLanguage)
                            || p.containsKey("language." + lang + ".rank")) {
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
                site.setMixLanguagesActive(Boolean.getBoolean(value));
            } else if (firstKey.startsWith("defaultSite") && "true".equals(
                    value) && sitesService.getDefaultSite() == null) {
                sitesService.setDefaultSite(site);
            } else if (firstKey.equals("installedModules")) {
                if (!site.getInstalledModules().contains(value)) {
                    try {
                        ServicesRegistry.getInstance().getJahiaTemplateManagerService().deployTemplates("/templateSets/"+value, "/sites/"+site.getSiteKey(), JCRSessionFactory.getInstance().getCurrentUser().getUsername());
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
        FileInputStream is = new FileInputStream(file);
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
        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
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

    public void importXML(final String parentNodePath, InputStream content, final boolean noRoot) throws IOException, RepositoryException, JahiaException {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("import-xml-", "");
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            IOUtils.copy(content, fileOutputStream);
            fileOutputStream.close();
            FileInputStream inputStream = new FileInputStream(tempFile);
            int format = detectXmlFormat(inputStream);
            inputStream.close();

            switch (format) {
                case XMLFormatDetectionHandler.JCR_DOCVIEW: {
                    if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, null);
                        InputStream is = new FileInputStream(tempFile);
                        try {
                            session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                        } finally {
                            IOUtils.closeQuietly(is);
                        }
                        session.save();
                    } else {
                        final File contentFile = tempFile;
                        JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                InputStream is = null;
                                try {
                                    is = new FileInputStream(contentFile);
                                    session.importXML(parentNodePath, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, noRoot);
                                    session.save();
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
                    FileInputStream is = new FileInputStream(tempFile);
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

    public void importZip(final String parentNodePath, final File file, final boolean noRoot) throws IOException, RepositoryException, JahiaException {
        if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
            JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null,null,null);
            importZip(parentNodePath, file, noRoot, session);
        } else {
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        importZip(parentNodePath, file, noRoot, session);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    } catch (JahiaException e) {
                        throw new RepositoryException(e);
                    }
                    return null;
                }
            });
        }
    }

    private void importZip(String parentNodePath, File file, boolean noRoot, JCRSessionWrapper session)
            throws IOException, RepositoryException, JahiaException {
        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, List<String>> references = new HashMap<String, List<String>>();

        getFileList(file, sizes, fileList);
        NoCloseZipInputStream zis;

//        if (sizes.containsKey(REPOSITORY_XML)) {
        // Import repository content
        zis = new NoCloseZipInputStream(new FileInputStream(file));
        try {
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML)) {
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList, null);

                    documentViewImportHandler.setReferences(references);
                    documentViewImportHandler.setNoRoot(noRoot);

                    handleImport(zis, documentViewImportHandler);
                    session.save();
                } else if (name.endsWith(".xml")) {
                    importXML(parentNodePath, zis, false);
                }
                zis.closeEntry();
            }
        } finally {
            zis.reallyClose();
        }
//        }

        session.save();
    }

    private void getFileList(File file, Map<String, Long> sizes, List<String> fileList) throws IOException {
        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
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
        Vector keys = new Vector();

        @Override
        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        @Override
        public Enumeration keys() {
            return keys.elements();
        }
    }

	private void setObserverInterval(long observerInterval) {
    	this.observerInterval = observerInterval;
    }
}
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
package org.jahia.services.importexport;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.jahia.admin.roles.ManageSiteRoles;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.JahiaService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.JahiaCndReader;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.zip.ZipEntry;
import org.jahia.utils.zip.ZipOutputStream;
import org.jahia.utils.LanguageCodeConverters;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
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

    private static Logger logger = Logger.getLogger(ImportExportBaseService.class);

    private static ImportExportBaseService instance;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(ImportExportService.DATE_FORMAT);

    private static final String CDATA = "CDATA";
    private static final String FILESACL_XML = "filesacl.xml";
    private static final String REPOSITORY_XML = "repository.xml";
    private static final String CATEGORIES_XML = "categories.xml";
    private static final String SITE_PERMISSIONS_XML = "sitePermissions.xml";
    private static final String SERVER_PERMISSIONS_XML = "serverPermissions.xml";
    private static final String USERS_XML = "users.xml";
    private static final String SITE_PROPERTIES = "site.properties";
    private static final String EXPORT_PROPERTIES = "export.properties";
    private static final String DEFINITIONS_CND = "definitions.cnd";
    private static final String DEFINITIONS_MAP = "definitions.map";


    private JahiaSitesService sitesService;
    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaFileWatcherService fileWatcherService;
    private JCRStoreService jcrStoreService;
    private CategoryService categoryService;

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
            new ImportFileObserver(org.jahia.settings.SettingsBean.getInstance().getJahiaImportsDiskPath(), false, 10000, true);
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
                final List<File> files = (List<File>) args;
                if (!files.isEmpty()) {
                    try {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                JCRNodeWrapper dest = session.getNode("/imports");
                                for (File file : files) {
                                    try {
                                        dest.uploadFile(file.getName(), new FileInputStream(file),
                                                Jahia.getStaticServletConfig().getServletContext().getMimeType(
                                                        file.getName()));
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

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public void setFileWatcherService(JahiaFileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    public void exportAll(OutputStream outputStream, Map<String, Object> params, ProcessingContext processingContext) throws JahiaException, RepositoryException, IOException, SAXException {
        Iterator<JahiaSite> en = sitesService.getSites();
        List<JahiaSite> l = new ArrayList<JahiaSite>();
        while (en.hasNext()) {
            JahiaSite jahiaSite = (JahiaSite) en.next();
            l.add(jahiaSite);
        }
        exportSites(outputStream, params, processingContext, l);
    }

    public void exportSites(OutputStream outputStream, Map<String, Object> params, ProcessingContext processingContext, List<JahiaSite> sites) throws JahiaException, RepositoryException, IOException, SAXException {
        ZipOutputStream zout = new ZipOutputStream(outputStream);

        ZipEntry anEntry = new ZipEntry(EXPORT_PROPERTIES);
        zout.putNextEntry(anEntry);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout));
        bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
        bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
        bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
        bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
        bw.flush();

        DataWriter dw;

        JahiaSite s = processingContext.getSite();

        for (Iterator<JahiaSite> iterator = sites.iterator(); iterator.hasNext();) {
            JahiaSite jahiaSite = iterator.next();
            anEntry = new ZipEntry(jahiaSite.getSiteKey() + ".zip");
            zout.putNextEntry(anEntry);
            exportSite(jahiaSite, zout, processingContext, params);
        }

        processingContext.setSite(s);
        processingContext.setSiteID(s.getID());
        processingContext.setSiteKey(s.getSiteKey());

        anEntry = new ZipEntry(SERVER_PERMISSIONS_XML);
        zout.putNextEntry(anEntry);
        dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        exportServerPermissions(dw);

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession();

        // export shared files -->
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
        try {
            files.add(session.getNode("/"));
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

        zout.putNextEntry(new ZipEntry("shared.zip"));
        ZipOutputStream zzout = new ZipOutputStream(zout);
        Set<String> tti = new HashSet<String>();
        tti.add(Constants.JAHIANT_VIRTUALSITE);
        try {
            exportNodes((JCRNodeWrapper) session.getRootNode(), session, files, zzout, tti);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        zzout.finish();
        zout.finish();
    }

    private void exportSite(JahiaSite jahiaSite, OutputStream out, ProcessingContext processingContext, Map<String, Object> params) throws JahiaException, RepositoryException, SAXException, IOException {
        processingContext.setSite(jahiaSite);
        processingContext.setSiteID(jahiaSite.getID());
        processingContext.setSiteKey(jahiaSite.getSiteKey());

        ZipOutputStream zout = new ZipOutputStream(out);

        zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
        exportSiteInfos(zout, processingContext.getSite());
        zout.putNextEntry(new ZipEntry(SITE_PERMISSIONS_XML));
        DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        exportSitePermissions(dw, processingContext.getSite());
        dw.flush();
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>(jcrStoreService.getSiteFolders(processingContext.getSite().getSiteKey()));
        exportNodes((JCRNodeWrapper) jcrStoreService.getSessionFactory().getCurrentUserSession().getRootNode(), jcrStoreService.getSessionFactory().getCurrentUserSession(), files, zout, new HashSet<String>());
        zout.finish();
    }

    public void exportZip(JCRNodeWrapper node, OutputStream out) throws JahiaException, RepositoryException, SAXException, IOException {
        ZipOutputStream zout = new ZipOutputStream(out);
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
        files.add(node);
        exportNodes(node, jcrStoreService.getSessionFactory().getCurrentUserSession(), files, zout, new HashSet<String>());
        zout.finish();
    }

    private void exportNodes(JCRNodeWrapper rootNode, JCRSessionWrapper session, Set<JCRNodeWrapper> nodes, ZipOutputStream zout, Set<String> typesToIgnore) throws SAXException, IOException, RepositoryException {
        TreeSet<JCRNodeWrapper> sorted = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        sorted.addAll(nodes);

        DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        zout.putNextEntry(new ZipEntry(REPOSITORY_XML));

        DocumentViewExporter exporter = new DocumentViewExporter(session, dw, true, false);
        typesToIgnore.add("rep:system");
        exporter.setTypesToIgnore(typesToIgnore);
        exporter.export(rootNode, sorted);
        dw.flush();
        exportNodesBinary(rootNode,sorted, zout, typesToIgnore);
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
        Properties p = new Properties();
        p.setProperty("sitetitle", s.getTitle());
        p.setProperty("siteservername", s.getServerName());
        p.setProperty("sitekey", s.getSiteKey());
        p.setProperty("description", s.getDescr());
        p.setProperty("templatePackageName", s.getTemplatePackageName());
        p.setProperty("mixLanguage", Boolean.toString(s.isMixLanguagesActive()));


        // get all jahiaGAprofiles
        final String cntProfile = s.getSettings().getProperty("profileCnt_" + s.getSiteKey());
        if (cntProfile != null) {
            p.setProperty("profileCnt_" + s.getSiteKey(), cntProfile);
        }
        Iterator<Object> it = s.getSettings().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith("jahiaGAprofile")) {
                String jahiaProfileName = (String) s.getSettings().get(key);
                p.setProperty(key, jahiaProfileName);
                Iterator<Object> it2 = s.getSettings().keySet().iterator();
                while (it2.hasNext()) {
                    String gaProp = (String) it2.next();
                    if (gaProp.startsWith(jahiaProfileName)) {
                        if (gaProp.endsWith("gaPassword")) {
                            if (SettingsBean.getInstance().isGmailPasswordExported()) {
                                p.setProperty(gaProp, (String) s.getSettings().get(gaProp));
                            } else {
                                p.setProperty(gaProp, "");
                            }
                        }
                        p.setProperty(gaProp, (String) s.getSettings().get(gaProp));
                    }
                }
            }
        }

        Set<String> v = s.getLanguages();
        for (String sls : v) {
            p.setProperty("language." + sls + ".activated", "true");
            p.setProperty("language." + sls + ".mandatory", "" + s.getMandatoryLanguages().contains(sls));
        }


        Properties settings = s.getSettings();

        for (Iterator<Object> iterator = settings.keySet().iterator(); iterator.hasNext();) {
            String s1 = (String) iterator.next();
            if (JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY.equals(s1)
                    || s1.startsWith("prod_") || s1.startsWith("html_")
                    || s1.startsWith("wai_") || s1.startsWith("url_")) {
                p.setProperty(s1, settings.getProperty(s1));
            }
        }
        if (s.isDefault()) {
            p.setProperty("defaultSite", "true");
        }


        p.store(out, "");
    }

    private void exportServerPermissions(ContentHandler ch) throws JahiaException, SAXException {
        exportPermissions(ch, "org.jahia.actions.server", "serverPermission");
    }

    private void exportSitePermissions(ContentHandler ch, JahiaSite site) throws JahiaException, SAXException {
        exportPermissions(ch, ManageSiteRoles.SITE_PERMISSIONS_PREFIX + site.getID(), "sitePermission");
    }

    private void exportPermissions(ContentHandler ch, String prefix, String nodeName) throws SAXException {
        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", CDATA, JAHIA_URI);

        ch.startElement(JAHIA_URI, nodeName + "s", "jahia:" + nodeName + "s", attr);

        List<JahiaAclName> list = ServicesRegistry.getInstance().getJahiaACLManagerService().getAclNamesStartingWith(prefix);
        for (Iterator<JahiaAclName> iterator = list.iterator(); iterator.hasNext();) {
            attr = new AttributesImpl();
            JahiaAclName jahiaAclName = (JahiaAclName) iterator.next();

            String name = jahiaAclName.getAclName().substring(prefix.length() + 1);
            attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, name);

            String perms = "";
            JahiaAcl acl = jahiaAclName.getAcl();
            Collection<JahiaAclEntry> entries = acl.getEntries();
            for (Iterator<JahiaAclEntry> iterator1 = entries.iterator(); iterator1.hasNext();) {
                JahiaAclEntry ace = (JahiaAclEntry) iterator1.next();
                if (ace.getPermission(JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_YES) {
                    if (ace.getComp_id().getType().intValue() == 1) {
                        JahiaUser user = userManagerService.lookupUserByKey(ace.getComp_id().getTarget());
                        if (user != null) {
                            perms += "|u:" + user.getUsername();
                        }
                    } else {
                        JahiaGroup group = groupManagerService.lookupGroup(ace.getComp_id().getTarget());
                        if (group != null) {
                            perms += "|g:" + group.getGroupname();
                            if ("serverPermission".equals(nodeName)) {
                                // also add site key information
                                try {
                                    perms += ":"
                                            + (group.getSiteID() == 0 ? "0"
                                            : sitesService.getSite(
                                            group.getSiteID())
                                            .getSiteKey());
                                } catch (Exception e) {
                                    logger.error(
                                            "Unable to obtain site key for site with ID '"
                                                    + group.getSiteID() + "'", e);
                                }
                            }
                        }
                    }
                }
            }
            if (perms.length() > 0) {
                perms = perms.substring(1);
                attr.addAttribute(JAHIA_URI, "acl", "jahia:acl", CDATA, perms);
            }

            ch.startElement(JAHIA_URI, nodeName, "jahia:" + nodeName, attr);
            ch.endElement(JAHIA_URI, nodeName, "jahia:" + nodeName);
        }
        ch.endElement(JAHIA_URI, nodeName + "s", "jahia:" + nodeName + "s");
        ch.endDocument();
    }

    // *****************************************
    // Import methods
    // *****************************************

    public void importSiteZip(File file, List<ImportAction> actions, ExtendedImportResult result, JahiaSite site) throws RepositoryException, IOException {
        CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler();
        UsersImportHandler usersImportHandler = new UsersImportHandler(site);

        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null,null,true,null);
        List<String[]> catProps = null;
        List<String[]> userProps = null;

        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, String> uuidMapping = new HashMap<String, String>();
        Map<String, List<String>> references = new HashMap<String, List<String>>();

        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
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
        zis.reallyClose();

        Map<String, String> pathMapping = new HashMap<String, String>();

        if (sizes.containsKey(USERS_XML)) {
            // Import users first
            zis = new NoCloseZipInputStream(new FileInputStream(file));
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
            zis.reallyClose();
        }


        if (sizes.containsKey(REPOSITORY_XML)) {
            // Import repository content
            zis = new NoCloseZipInputStream(new FileInputStream(file));
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML)) {
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, null, file, fileList, (site != null ? site.getSiteKey(): null));

                    documentViewImportHandler.setUuidMapping(uuidMapping);
                    documentViewImportHandler.setReferences(references);
                    documentViewImportHandler.setPathMapping(pathMapping);
                    documentViewImportHandler.setNoRoot(true);
                    documentViewImportHandler.setResolveReferenceAtEnd(false);

                    handleImport(zis, documentViewImportHandler);
                    session.save();
                    break;
                }
                zis.closeEntry();
            }
            zis.reallyClose();
        } else {
            // No repository descriptor - prepare to import files directly
            pathMapping = new HashMap<String, String>();
            List<JCRNodeWrapper> sitesFolder = jcrStoreService.getSiteFolders(site.getSiteKey());
            if (!sitesFolder.isEmpty()) {
                pathMapping.put("/", sitesFolder.iterator().next().getPath() + "/");
            } else {
                pathMapping.put("/", "/");
            }
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
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.indexOf('/') > -1) {
                    if (sizes.containsKey(REPOSITORY_XML)) {
                        // No repository descriptor - Old import format only
                        name = "/" + name;
                        if (!zipentry.isDirectory()) {
                            try {
                                name = pathMapping.get("/") + name.substring(1);
                                String filename = name.substring(name.lastIndexOf('/') + 1);
                                String contentType = Jahia.getStaticServletConfig().getServletContext().getMimeType(filename);
                                ensureFile(jcrStoreService.getSessionFactory().getCurrentUserSession(), name, zis, contentType, site, pathMapping);
                            } catch (Exception e) {
                                logger.error("Cannot upload file " + zipentry.getName(), e);
                            }
                        }
                    }
                } else if (name.equals(SITE_PROPERTIES)) {
                    importSiteProperties(zis, site);
                } else if (name.equals(CATEGORIES_XML)) {
                    catProps = importCategoriesAndGetUuidProps(zis, categoriesImportHandler);
                } else if (name.equals(SITE_PERMISSIONS_XML)) {
                    importSitePermissions(site, zis);
                } else if (name.equals(DEFINITIONS_CND)) {
                    reg = new NodeTypeRegistry(false);

                    try {
                        JahiaCndReader r = new JahiaCndReader(new InputStreamReader(zis, "UTF-8"),zipentry.getName(), file.getName(), reg);
                        r.parse();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else if (name.equals(DEFINITIONS_MAP)) {
                    mapping = new DefinitionsMapping();
                    mapping.load(zis);

                }
                zis.closeEntry();
            }
            zis.reallyClose();
        }

        // Import legacy content from 5.x
        for (Map.Entry<String, Long> entry : sizes.entrySet()) {
            if (entry.getKey().startsWith("export_")) {
                // Old import
                JCRNodeWrapper siteFolder = jcrStoreService.getSiteFolders(site.getSiteKey()).get(0);

                zis = new NoCloseZipInputStream(new FileInputStream(file));

                while (true) {
                    ZipEntry zipentry = zis.getNextEntry();
                    if (zipentry == null) break;
                    String name = zipentry.getName();
                    if (name.equals(FILESACL_XML)) {
                        importFilesAcl(site, zis);
                    } else if (name.startsWith("export")) {
                        String languageCode;
                        if (name.indexOf("_") != -1) {
                            languageCode = name.substring(7, name.lastIndexOf("."));
                        } else {
                            languageCode = site.getLanguagesAsLocales().iterator().next().toString();
                        }
                        zipentry.getSize();

                        LegacyImportHandler importHandler = new LegacyImportHandler(siteFolder, reg, mapping, LanguageCodeConverters.languageCodeToLocale(languageCode));
                        importHandler.setUuidMapping(uuidMapping);
                        importHandler.setReferences(references);
                        importHandler.setPathMapping(pathMapping);
                        handleImport(zis, importHandler);
                        siteFolder.getSession().save();
                    }
                    zis.closeEntry();
                }
                zis.reallyClose();

                break;
            }
        }

        categoriesImportHandler.setUuidProps(catProps);
        usersImportHandler.setUuidProps(userProps);

//        session.save();
        ReferencesHelper.resolveCrossReferences(session, uuidMapping, references);
        session.save();
    }


    private JCRNodeWrapper ensureDir(JCRSessionWrapper session, String name, JahiaSite site, Map<String, String> pathMapping) throws RepositoryException {
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
                    pathMapping.put(current, newName);
                    name = name.replace(current, newName);

                    return ensureDir(session, name, site, pathMapping);
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
            JCRNodeWrapper parentDir = ensureDir(session, name.substring(0, endIndex), site, pathMapping);
            if (parentDir == null) {
                return null;
            }
            if (Constants.JAHIANT_VIRTUALSITES_FOLDER.equals(parentDir.getPrimaryNodeTypeName())) {
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

    private void ensureFile(JCRSessionWrapper session, String path, InputStream inputStream, String type, JahiaSite destSite, Map<String, String> pathMapping) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        try {
            JCRNodeWrapper parentDir = ensureDir(session, path.substring(0, path.lastIndexOf('/')), destSite, pathMapping);

            if (!parentDir.hasNode(name)) {
                if (parentDir == null) {
                    logger.warn("Cannot create folder " + path.lastIndexOf('/'));
                    return;
                }

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

    private void importFilesAcl(JahiaSite site, InputStream is) {
        handleImport(is, new FilesAclImportHandler(site));
    }

    private void importSiteProperties(InputStream is, JahiaSite site) throws IOException {
        Properties p = new Properties();
        p.load(is);
        Set<Object> keys = p.keySet();
        boolean isMultiLang = LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.admin.languages.ManageSiteLanguages", 0);
        boolean siteSettings = false;
        final Set<String> languages = site.getLanguages();
        languages.clear();
        final Set<String> mandatoryLanguages = site.getMandatoryLanguages();
        mandatoryLanguages.clear();
        for (Object key : keys) {
            String property = (String) key;
            String value = p.getProperty(property);
            StringTokenizer st = new StringTokenizer(property, ".");
            String firstKey = st.nextToken();
            if (JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY.equals(property)) {
                siteSettings = true;
                site.getSettings().put(property, value);
            }

            if (firstKey.equals("language")) {
                String lang = st.nextToken();
                String t = st.nextToken();

                if (!languages.contains(lang)) {
                    if (isMultiLang || languages.isEmpty()) {
                        siteSettings = true;
                        if(languages.isEmpty()) {
                            site.setDefaultLanguage(lang);
                        }
                        languages.add(lang);
                    } else {
                        logger.warn("Multilanguage is not authorized by license, " + lang + " will be ignored");
                        continue;
                    }
                }
                if ("mandatory".equals(t)) {
                    mandatoryLanguages.add(lang);
                    siteSettings = true;
                }
            } else if (firstKey.equals("mixLanguage")) {
                site.setMixLanguagesActive(Boolean.getBoolean(value));
            } else if (firstKey.startsWith("prod_") || firstKey.startsWith("html_") || firstKey.startsWith(
                    "wai_") || firstKey.startsWith("url_")) {
                siteSettings = true;
                site.getSettings().put(firstKey, value);
            } else if (firstKey.startsWith("defaultSite") && "true".equals(
                    value) && sitesService.getDefaultSite() == null) {
                sitesService.setDefaultSite(site);
            } else if (firstKey.startsWith("jahiaGAprofile")) {
                siteSettings = true;
                site.getSettings().put("jahiaGAprofile_" + firstKey.split("_")[1] + "_" + site.getSiteKey(), value);
            } else if (firstKey.startsWith("profileCnt")) {
                siteSettings = true;
                site.getSettings().put("profileCnt_" + site.getSiteKey(), value);
            } else if (firstKey.endsWith("gaUserAccount") || firstKey.endsWith("gaProfile") || firstKey.endsWith(
                    "gaLogin") || firstKey.endsWith("gaPassword") || firstKey.endsWith(
                    "trackedUrls") || firstKey.endsWith("trackingEnabled") || firstKey.endsWith("profileId")) {
                siteSettings = true;
                String profileName = firstKey.split("_")[0];
                site.getSettings().put(profileName + "_" + site.getSiteKey() + "_" + firstKey.split("_")[2], value);
            }
        }
        site.setLanguages(languages);
        site.setMandatoryLanguages(mandatoryLanguages);
        if (siteSettings) {
            try {
                sitesService.updateSite(site);
            } catch (JahiaException e) {
                logger.error("Cannot update site", e);
            }
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

    public void importServerPermissions(ProcessingContext jParams, InputStream is) {
        handleImport(is, new PermissionsImportHandler("org.jahia.actions.server", "serverPermission", (Map) jParams.getAttribute("sitePermissions_siteKeyMapping")));
    }

    private void importSitePermissions(JahiaSite site, InputStream is) {
        handleImport(is, new PermissionsImportHandler(ManageSiteRoles.SITE_PERMISSIONS_PREFIX + site.getID(), "sitePermission", site.getID()));
    }

    public List<String[]> importUsers(File file) throws IOException {
        return importUsers(new FileInputStream(file), new UsersImportHandler());
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
        zis.reallyClose();
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
                    if (JcrSessionFilter.getCurrentUser() != null) {
                        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null, null, true,null);
                        session.importXML(parentNodePath, new FileInputStream(tempFile), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                        session.save();
                    } else {
                        final File contentFile = tempFile;
                        JCRTemplate.getInstance().doExecuteWithSystemSession(null, null, null, true, new JCRCallback<Boolean>() {
                            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                try {
                                    session.importXML(parentNodePath, new FileInputStream(contentFile), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, noRoot);
                                    session.save();
                                } catch (IOException e) {
                                    throw new RepositoryException(e);
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
                    importCategories(cat, new FileInputStream(tempFile));
                    break;
                }
            }
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

    }

    public void importZip(String parentNodePath, File file, boolean noRoot) throws IOException, RepositoryException, JahiaException {
        JCRSessionWrapper session = jcrStoreService.getSessionFactory().getCurrentUserSession(null,null,true,null);
        Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> fileList = new ArrayList<String>();

        Map<String, String> uuidMapping = new HashMap<String, String>();
        Map<String, List<String>> references = new HashMap<String, List<String>>();

        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
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
        zis.reallyClose();

        Map<String, String> pathMapping = new HashMap<String, String>();

//        if (sizes.containsKey(REPOSITORY_XML)) {
            // Import repository content
            zis = new NoCloseZipInputStream(new FileInputStream(file));
            while (true) {
                ZipEntry zipentry = zis.getNextEntry();
                if (zipentry == null) break;
                String name = zipentry.getName();
                if (name.equals(REPOSITORY_XML)) {
                    DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(session, parentNodePath, file, fileList, null);

                    documentViewImportHandler.setUuidMapping(uuidMapping);
                    documentViewImportHandler.setReferences(references);
                    documentViewImportHandler.setPathMapping(pathMapping);
                    documentViewImportHandler.setNoRoot(noRoot);
                    
                    handleImport(zis, documentViewImportHandler);
                    session.save();
                    break;
                } else if (name.endsWith(".xml")) {
                    importXML(parentNodePath, zis, false);
                }
                zis.closeEntry();
            }
            zis.reallyClose();
//        }

        session.save();
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
}
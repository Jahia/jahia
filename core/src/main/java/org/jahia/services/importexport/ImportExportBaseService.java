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

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.log4j.Logger;
import org.apache.webdav.lib.WebdavResource;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xml.utils.DOMBuilder;
import org.jahia.admin.permissions.ManageSitePermissions;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.StructuralRelationship;
import org.jahia.content.TreeOperationResult;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.importexport.ManageImportExport;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.operations.valves.ThemeValve;
import org.jahia.params.BasicURLGeneratorImpl;
import org.jahia.params.ProcessingContext;
import org.jahia.params.URLGenerator;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.JahiaService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.mail.GroovyMimeMessagePreparator;
import org.jahia.services.mail.MailService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteLanguageMapping;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.zip.ExclusionWildcardFilter;
import org.jahia.utils.zip.PathFilter;
import org.jahia.utils.zip.ZipEntry;
import org.jahia.utils.zip.ZipOutputStream;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.ItemNotFoundException;
import javax.jcr.nodetype.NodeType;
import javax.transaction.Status;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Principal;
import java.text.ParseException;
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
    private static final String USERS_XML = "users.xml";
    private static final String SITE_PROPERTIES = "site.properties";

    private JahiaSitesService sitesService;
    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private CategoryService categoryService;
    private SchedulerService schedulerService;
    private JahiaFileWatcherService fileWatcherService;
    private boolean overwriteResourcesByImport;
    private PathFilter excludedResourcesFilter;

    private Map<String, Exporter> exporters;

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
        exporters.put(LEGACY_EXPORTER, new JahiaLegacyExporter());

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
                List<File> files = (List<File>) args;
                if (!files.isEmpty()) {
                    JCRNodeWrapper dest = ServicesRegistry.getInstance().getJCRStoreService().getFileNode("/content/imports", JahiaAdminUser.getAdminUser(0));
                    for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
                        try {
                            File file = (File) iterator.next();
                            dest.uploadFile(file.getName(), new FileInputStream(file), Jahia.getStaticServletConfig().getServletContext().getMimeType(file.getName()));
                        } catch (Exception t) {
                            logger.error("file observer error : ", t);
                        }
                    }
                    try {
                        dest.save();
                    } catch (RepositoryException e) {
                        logger.error("error", e);
                    }
                    for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
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

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setFileWatcherService(JahiaFileWatcherService fileWatcherService) {
        this.fileWatcherService = fileWatcherService;
    }

    public Map<String, Exporter> getExporters() {
        return exporters;
    }

    public void setExporters(Map<String, Exporter> exporters) {
        this.exporters = exporters;
    }

    public void exportAll(OutputStream outputStream, Map<String, Object> params, ProcessingContext processingContext) throws JahiaException, IOException, SAXException {
        Iterator<JahiaSite> en = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
        List<JahiaSite> l = new ArrayList<JahiaSite>();
        while (en.hasNext()) {
            JahiaSite jahiaSite = (JahiaSite) en.next();
            l.add(jahiaSite);
        }
        exportSites(outputStream, params, processingContext, l);
    }

    public void exportSites(OutputStream outputStream, Map<String, Object> params, ProcessingContext processingContext, List<JahiaSite> sites) throws JahiaException, IOException, SAXException {
        ZipOutputStream zout = new ZipOutputStream(outputStream);

        ZipEntry anEntry = new ZipEntry("export.properties");
        zout.putNextEntry(anEntry);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zout));
        bw.write("JahiaRelease = " + Jahia.getReleaseNumber() + "\n");
        bw.write("Patch = " + Jahia.getPatchNumber() + "\n");
        bw.write("BuildNumber = " + Jahia.getBuildNumber() + "\n");
        bw.write("ExportDate = " + new SimpleDateFormat(ImportExportService.DATE_FORMAT).format(new Date()) + "\n");
        bw.flush();

        DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        zout.putNextEntry(new ZipEntry(USERS_XML));
        exportUsers(dw);
        dw.flush();

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

        anEntry = new ZipEntry("serverPermissions.xml");
        zout.putNextEntry(anEntry);
        dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        exportServerPermissions(dw);

        // export shared files -->
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
        files.add(ServicesRegistry.getInstance().getJCRStoreService().getFileNode("/content", processingContext.getUser()));

        zout.putNextEntry(new ZipEntry("shared.zip"));
        ZipOutputStream zzout = new ZipOutputStream(zout);
        Set<String> tti = new HashSet<String>();
        tti.add(Constants.JAHIANT_VIRTUALSITE);
        try {
            exportFiles(files, zzout, tti);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        zzout.finish();
        zout.finish();
    }

    public void exportSite(JahiaSite jahiaSite, OutputStream out, ProcessingContext processingContext, Map<String, Object> params) throws JahiaException, SAXException, IOException {
        ContentPage home = jahiaSite.getHomeContentPage();
        List<Locale> locs = jahiaSite.getLanguageSettingsAsLocales(true);
        Set<String> langs = new HashSet<String>();
        for (Iterator<Locale> iterator = locs.iterator(); iterator.hasNext();) {
            Locale locale = (Locale) iterator.next();
            langs.add(locale.toString());
        }

        processingContext.setSite(jahiaSite);
        processingContext.setSiteID(jahiaSite.getID());
        processingContext.setSiteKey(jahiaSite.getSiteKey());
        exportZip(home, langs, out, processingContext, params);
    }

    public Document exportDocument(ContentObject object, String languageCodes, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException {
        DocumentImpl doc = new DocumentImpl();
        DOMBuilder db = new DOMBuilder(doc, doc);
        export(object, languageCodes, db, new HashSet<JCRNodeWrapper>(), jParams, params);
        return doc;
    }

    public void exportFile(ContentObject object, String languageCode, OutputStream out, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException, IOException {
        DataWriter dw = new DataWriter(new OutputStreamWriter(out, "UTF-8"));
        export(object, languageCode, dw, new HashSet<JCRNodeWrapper>(), jParams, params);
    }

    public void exportCategories(OutputStream out, ProcessingContext jParams) throws JahiaException, SAXException, IOException {
        DataWriter dw = new DataWriter(new OutputStreamWriter(out, "UTF-8"));
        exportCategories(dw, jParams.getUser());
    }

    public void exportVersions(OutputStream out, ProcessingContext jParams) throws JahiaException, SAXException, IOException {

        Map<String, String> m = ServicesRegistry.getInstance().getJahiaPageService().getVersions(jParams.getSiteID(), jParams.getLocale().toString());
        m.putAll(ServicesRegistry.getInstance().getJahiaContainersService().getVersions(jParams.getSiteID()));
        m.putAll(ServicesRegistry.getInstance().getJahiaFieldService().getVersions(jParams.getSiteID(), jParams.getLocale().toString()));

        DataWriter ch = new DataWriter(new OutputStreamWriter(out, "UTF-8"));

        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", "CDATA", JAHIA_URI);
        ch.startElement(JAHIA_URI, "versions", "jahia:versions", attr);
        for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            String v = (String) m.get(s);
            attr = new AttributesImpl();
            attr.addAttribute(JAHIA_URI, "originalUuid", "jahia:originalUuid", "CDATA", s);
            attr.addAttribute(JAHIA_URI, "lastImportedVersion", "jcr:lastImportedVersion", "CDATA", v);
            ch.startElement(JAHIA_URI, "version", "jahia:version", attr);
            ch.endElement(JAHIA_URI, "version", "jahia:version");
        }

        ch.endElement(JAHIA_URI, "versions", "jahia:versions");
        ch.endDocument();

    }

    public void exportZip(ContentObject object, Set<String> languageCodes, OutputStream out, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException, IOException {
        Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();

        ZipOutputStream zout = new ZipOutputStream(out);

        for (Iterator<String> iterator = languageCodes.iterator(); iterator.hasNext();) {
            String l = iterator.next();

            ZipEntry anEntry = new ZipEntry("export_" + l + ".xml");
            zout.putNextEntry(anEntry);

            DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
            export(object, l, dw, files, jParams, params);
        }

        if (params != null && Boolean.TRUE.equals(params.get(INCLUDE_ALL_FILES))) {
            files.clear();
            JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();
            files = new HashSet<JCRNodeWrapper>(service.getSiteFolders(jParams.getSiteKey(), jParams.getUser()));
            params.put(INCLUDE_FILES, Boolean.TRUE);
        }

        if (params != null && Boolean.TRUE.equals(params.get(INCLUDE_FILES))) {
            DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));

            if (object.getParent(null) == null) {
                // site export
                if (params.containsKey(INCLUDE_SITE_INFOS)) {
                    zout.putNextEntry(new ZipEntry(SITE_PROPERTIES));
                    exportSiteInfos(zout, jParams, jParams.getSite());
                }

                zout.putNextEntry(new ZipEntry(CATEGORIES_XML));
                exportCategories(dw, jParams.getUser());
                dw.flush();

                zout.putNextEntry(new ZipEntry(USERS_XML));
                exportSiteUsers(dw, jParams.getSite());
                dw.flush();

                zout.putNextEntry(new ZipEntry(SITE_PERMISSIONS_XML));
                exportSitePermissions(dw, jParams.getSite());
                dw.flush();
            }

            exportFiles(files, zout, new HashSet<String>());
        }
        zout.finish();
    }

    private void exportFiles(Set<JCRNodeWrapper> files, ZipOutputStream zout, Set<String> typesToIgnore) throws SAXException, IOException {
        TreeSet<JCRNodeWrapper> sorted = new TreeSet<JCRNodeWrapper>(new Comparator<JCRNodeWrapper>() {
            public int compare(JCRNodeWrapper o1, JCRNodeWrapper o2) {
                return o1.getPath().compareTo(o2.getPath());
            }
        });
        sorted.addAll(files);

        DataWriter dw = new DataWriter(new OutputStreamWriter(zout, "UTF-8"));
        zout.putNextEntry(new ZipEntry(REPOSITORY_XML));
        exportFilesInfo(dw, sorted, typesToIgnore);
        dw.flush();
        exportFilesBinary(sorted, zout, typesToIgnore);
    }

    private void exportFilesBinary(SortedSet<JCRNodeWrapper> files, ZipOutputStream zout, Set<String> typesToIgnore) throws SAXException {
        byte[] buffer = new byte[4096];
        for (Iterator<JCRNodeWrapper> iterator = files.iterator(); iterator.hasNext();) {
            JCRNodeWrapper file = iterator.next();
            exportFileBinary(file, zout, typesToIgnore, buffer);
        }
    }

    private void exportFileBinary(JCRNodeWrapper file, ZipOutputStream zout, Set<String> typesToIgnore, byte[] buffer) {
        int bytesIn;
        try {
            if (file.getProvider().isExportable() && !typesToIgnore.contains(file.getPrimaryNodeTypeName())) {
                NodeIterator ni = file.getNodes();
                while (ni.hasNext()) {
                    Node child = ni.nextNode();
                    if (child.isNodeType("nt:resource")) {
                        InputStream is = child.getProperty("jcr:data").getStream();
                        if (is != null) {
                            try {
                                String path = file.getPath();
                                String name = child.getName().replace(":","_"); 
                                if (child.getName().equals("jcr:content")) {
                                    name = file.getName();
                                }
                                path += "/"+name;
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
                        exportFileBinary((JCRNodeWrapper) child, zout, typesToIgnore, buffer);
                    }
                }
            }            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private void exportFilesInfo(ContentHandler ch, SortedSet<JCRNodeWrapper> files, Set<String> typesToIgnore) throws SAXException {
        ch.startDocument();
        AttributesImpl attr = new AttributesImpl();

        Map<String, String> prefixes = new HashMap<String, String>();
        prefixes.put(Constants.NT_PREF, Constants.NT_NS);
        prefixes.put(Constants.JCR_PREF, Constants.JCR_NS);
        prefixes.put(Constants.MIX_PREF, Constants.MIX_NS);
        prefixes.put(Constants.JAHIANT_PREF, Constants.JAHIANT_NS);
        prefixes.put(Constants.JAHIA_PREF, Constants.JAHIA_NS);
        prefixes.put(Constants.JAHIAMIX_PREF, Constants.JAHIAMIX_NS);

        for (Iterator<String> iterator = prefixes.keySet().iterator(); iterator.hasNext();) {
            String prefix = iterator.next();
            String uri = prefixes.get(prefix);
            attr.addAttribute(NS_URI, prefix, "xmlns:" + prefix, CDATA, uri);
            ch.startPrefixMapping(prefix, uri);
            ch.endPrefixMapping(prefix);
        }

        ch.startElement("", "content", "content", attr);

        Stack<String> stack = new Stack<String>();
        stack.push("/content");
        for (Iterator<JCRNodeWrapper> iterator = files.iterator(); iterator.hasNext();) {
            JCRNodeWrapper file = iterator.next();
            exportFileInfo(file, ch, stack, prefixes, typesToIgnore);
        }
        while (!stack.isEmpty()) {
            String end = stack.pop();
            String name = end.substring(end.lastIndexOf('/') + 1);
            String encodedName = ISO9075.encode(name);
            ch.endElement("", encodedName, encodedName);
        }

        ch.endDocument();
    }

    private void exportFileInfo(JCRNodeWrapper file, ContentHandler ch, Stack<String> stack, Map<String, String> prefixes, Set<String> typesToIgnore) throws SAXException {
        try {
            if (file.getProvider().isExportable() && !typesToIgnore.contains(file.getPrimaryNodeTypeName())) {

                String path = "";
                Node current = file;
                while (!current.getPath().equals("/")) {
                    path = "/" + current.getName() + path;
                    current = current.getParent();
                }

                if (!path.equals("/content")) {

                    String parentpath = path.substring(0, path.lastIndexOf('/'));

                    while (!parentpath.startsWith(stack.peek())) {
                        String end = stack.pop();
                        String name = end.substring(end.lastIndexOf('/') + 1);
                        String encodedName = ISO9075.encode(name);
                        ch.endElement("", encodedName, encodedName);
                    }
                    while (!stack.peek().equals(parentpath)) {
                        String name = parentpath.substring(stack.peek().length() + 1);
                        if (name.contains("/")) {
                            name = name.substring(0, name.indexOf('/'));
                        }
                        String encodedName = ISO9075.encode(name);
                        String currentpath = stack.peek() + "/" + name;
                        String pt = JCRStoreService.getInstance().getFileNode(currentpath, file.getUser()).getPrimaryNodeTypeName();
                        AttributesImpl atts = new AttributesImpl();
                        atts.addAttribute(Constants.JCR_NS, "primaryType", "jcr:primaryType", CDATA, pt);
                        ch.startElement("", encodedName, encodedName, atts);
                        stack.push(currentpath);
                    }

                    AttributesImpl attrs = new AttributesImpl();
                    Map<String, String> props = file.getPropertiesAsString();

                    Iterator<String> propsIterator = props.keySet().iterator();
                    while (propsIterator.hasNext()) {
                        String key = (String) propsIterator.next();
                        String prefix = null;
                        String localname = key;
                        if (key.indexOf(':') > -1) {
                            prefix = key.substring(0, key.indexOf(':'));
                            localname = key.substring(key.indexOf(':') + 1);
                        }
                        String attrName = ISO9075.encode(localname);
                        String value = (String) props.get(key);
                        if (prefix == null) {
                            attrs.addAttribute("", localname, attrName, CDATA, value);
                        } else {
                            attrs.addAttribute(prefixes.get(prefix), localname, prefix + ":" + attrName, CDATA, value);
                        }
                    }

                    String encodedName = ISO9075.encode(file.getName());
                    ch.startElement("", encodedName, encodedName, attrs);
                    stack.push(path);
                }
                List<JCRNodeWrapper> l = file.getChildren();
                for (Iterator<JCRNodeWrapper> iterator = l.iterator(); iterator.hasNext();) {
                    JCRNodeWrapper c = (JCRNodeWrapper) iterator.next();
                    exportFileInfo(c, ch, stack, prefixes, typesToIgnore);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Exception", e);
        }
    }

    private void exportSiteInfos(OutputStream out, ProcessingContext jParams, JahiaSite s) throws IOException {
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
            String key = (String)it.next();
            if (key.startsWith("jahiaGAprofile")) {
                String jahiaProfileName = (String)s.getSettings().get(key);
                p.setProperty(key, jahiaProfileName);
                Iterator<Object> it2 = s.getSettings().keySet().iterator();
                while (it2.hasNext()) {
                    String gaProp = (String)it2.next();
                    if (gaProp.startsWith(jahiaProfileName)) {
                        if (gaProp.endsWith("gaPassword")) {
                            if (jParams.settings().isGmailPasswordExported()) {
                                p.setProperty(gaProp, (String)s.getSettings().get(gaProp));
                            } else {
                                p.setProperty(gaProp, "");
                            }
                        }
                        p.setProperty(gaProp, (String)s.getSettings().get(gaProp));
                    }
                }
            }
        }

        try {
            List<SiteLanguageSettings> v = s.getLanguageSettings(true);
            for (Iterator<SiteLanguageSettings> iterator = v.iterator(); iterator.hasNext();) {
                SiteLanguageSettings sls = (SiteLanguageSettings) iterator.next();
                p.setProperty("language." + sls.getCode() + ".activated", "" + sls.isActivated());
                p.setProperty("language." + sls.getCode() + ".mandatory", "" + sls.isMandatory());
                p.setProperty("language." + sls.getCode() + ".rank", "" + sls.getRank());
            }
            List<SiteLanguageMapping> l = s.getLanguageMappings();
            for (Iterator<SiteLanguageMapping> iterator = l.iterator(); iterator.hasNext();) {
                SiteLanguageMapping slm = (SiteLanguageMapping) iterator.next();
                p.setProperty("languageMapping." + slm.getFromLanguageCode(), slm.getToLanguageCode());
            }
        } catch (JahiaException e) {
        }

        Properties settings = s.getSettings();

        for (Iterator<Object> iterator = settings.keySet().iterator(); iterator.hasNext();) {
            String s1 = (String)iterator.next();
            if (JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY.equals(s1)
                    || ThemeValve.THEME_ATTRIBUTE_NAME.equals(s1)
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

    private void addFiles(File dir, String path, PathFilter pathFilter, ZipOutputStream zout) throws IOException {
        if (!pathFilter.accept(path)) {
            return;
        }

        File[] f = dir.listFiles();

        byte[] buffer = new byte[4096];
        int bytesIn;

        for (int i = 0; i < f.length; i++) {
            File file = f[i];
            String entryPath = (path.length() > 0 ? path + "/" : "") + file.getName();
            if (file.isDirectory()) {
                addFiles(file, entryPath, pathFilter, zout);
            } else if (pathFilter.accept(entryPath)) {
                zout.putNextEntry(new ZipEntry(entryPath));
                InputStream is = new FileInputStream(file);
                while ((bytesIn = is.read(buffer)) != -1) {
                    zout.write(buffer, 0, bytesIn);
                }
                is.close();
            }
        }
    }

    public void exportClasses(OutputStream out, ProcessingContext jParams) throws IOException {
        String path = org.jahia.settings.SettingsBean.getInstance().getClassDiskPath();

        ZipOutputStream zout = new ZipOutputStream(out);
        addFiles(new File(path), "", excludedResourcesFilter, zout);
        zout.finish();
    }

    public void export(ContentObject object, String languageCode, ContentHandler h, Set<JCRNodeWrapper> files, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException {
        String format = (String) params.get(EXPORT_FORMAT);
        if (format == null) {
            format = LEGACY_EXPORTER;
        }
        exporters.get(format).export(object, languageCode, h, files, jParams, params);
    }

    public void getFilesForField(ContentObject object, ProcessingContext jParams, String language, EntryLoadRequest loadRequest, Set<JCRNodeWrapper> files) throws JahiaException {
        exporters.get(LEGACY_EXPORTER).getFilesForField(object, jParams, language, loadRequest, files);
    }

    public ContentObject importFile(ContentObject parent, final ProcessingContext jParams, InputStream inputStream, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException {
        File tmp = File.createTempFile("import", "zip");
        OutputStream os = new FileOutputStream(tmp);
        byte[] buf = new byte[4096];
        int r;
        while ((r = inputStream.read(buf)) > 0) {
            os.write(buf, 0, r);
        }
        ContentObject o = importFile(parent, jParams, tmp, setUuid, actions, result);
        tmp.delete();
        return o;
    }

    public ContentObject importFile(ContentObject parent, final ProcessingContext jParams, File file, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException {
        CategoriesImportHandler categoriesImportHandler = new CategoriesImportHandler(jParams);
        UsersImportHandler usersImportHandler = new UsersImportHandler(jParams.getSite());

        List<String[]> catProps = null;        
        List<String[]> userProps = null;        

        Map<String, Integer> sizes = new HashMap<String, Integer>();
        List<String> fileList = new ArrayList<String>();

        Map<String,String> uuidMapping = new HashMap<String,String>();
        Map<String,String> references = new HashMap<String,String>();

        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
            if (name.endsWith(".xml")) {
                BufferedReader br = new BufferedReader(new InputStreamReader(zis));
                int i = 0;
                while (br.readLine() != null) {
                    i++;
                }
                sizes.put(name, i);
            }
            if (name.startsWith("content/")) {
                fileList.add("/" + name);
            }
            zis.closeEntry();
        }
        zis.reallyClose();

        boolean hasRepositoryFile = false;
        Map<String, String> pathMapping = new HashMap<String, String>();

        zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
            if (name.equals(USERS_XML)) {
                userProps = importUsersAndGetUuidProps(zis, usersImportHandler);
                break;
            }
            zis.closeEntry();
        }
        zis.reallyClose();
        zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
            if (name.equals(REPOSITORY_XML)) {
                hasRepositoryFile = true;
                DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(jParams, file, fileList);

                documentViewImportHandler.setUuidMapping(uuidMapping);
                documentViewImportHandler.setReferences(references);
                documentViewImportHandler.setPathMapping(pathMapping);

                handleImport(zis, documentViewImportHandler);

                break;
            }
            zis.closeEntry();
        }
        zis.reallyClose();

        userProps = importUsers(file, usersImportHandler);

        if (!hasRepositoryFile) {
            pathMapping = new HashMap<String, String>();
            List<JCRNodeWrapper> sitesFolder = JCRStoreService.getInstance().getSiteFolders(jParams.getSiteKey(), jParams.getUser());
            if (!sitesFolder.isEmpty()) {
                pathMapping.put("/", sitesFolder.iterator().next().getPath() + "/");
            } else {
                pathMapping.put("/", "/content/");
            }
        }

        zis = new NoCloseZipInputStream(new FileInputStream(file));
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
            JahiaSite site = jParams.getSite();
            if (name.indexOf('/') > -1) {
                if (!hasRepositoryFile) {
                    // Old import format only
                    name = "/" + name;
                    if (!zipentry.isDirectory()) {
                        try {
                            name = pathMapping.get("/") + name.substring(1);
                            String filename = name.substring(name.lastIndexOf('/') + 1);
                            String contentType = Jahia.getStaticServletConfig().getServletContext().getMimeType(filename);
                            ensureFile(name, zis, contentType, jParams, site, pathMapping);
                        } catch (Exception e) {
                            logger.error("Cannot upload file " + zipentry.getName(), e);
                        }
                    }
                }
            } else if (name.equals(SITE_PROPERTIES)) {
                Properties p = new Properties();
                p.load(zis);
                zis.closeEntry();
                Set<Object> keys = p.keySet();

                Map<String, SiteLanguageSettings> m = new HashMap<String, SiteLanguageSettings>();
                Set<String> old = new HashSet<String>();

                try {
                    List<SiteLanguageSettings> languageSettings = site.getLanguageSettings(false);
                    for (Iterator<SiteLanguageSettings> iterator = languageSettings.iterator(); iterator.hasNext();) {
                        SiteLanguageSettings setting = (SiteLanguageSettings) iterator.next();
                        m.put(setting.getCode(), setting);
                        old.add(setting.getCode());
                    }
                } catch (JahiaException e) {
                    logger.error("Cannot get languages", e);
                }
                boolean isMultiLang = LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.admin.languages.ManageSiteLanguages", 0);
                boolean siteSettings = false;
                for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
                    String property = (String) iterator.next();
                    String value = p.getProperty(property);
                    StringTokenizer st = new StringTokenizer(property, ".");
                    String firstKey = st.nextToken();
                    if (property.equals(ThemeValve.THEME_ATTRIBUTE_NAME)) {
                        siteSettings = true;
                        site.getSettings().put(property, value);
                    } else if (JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY.equals(property)) {
                        siteSettings = true;
                        site.getSettings().put(property, value);
                    }

                    if (firstKey.equals("language")) {
                        String lang = st.nextToken();
                        String t = st.nextToken();
                        SiteLanguageSettings set;

                        if (!m.containsKey(lang)) {
                            if (isMultiLang || m.isEmpty()) {
                                set = new SiteLanguageSettings(site.getID(), lang, true, m.size() + 1, false);
                                m.put(lang, set);
                            } else {
                                logger.warn("Multilanguage is not authorized by license, " + lang + " will be ignored");
                                continue;
                            }
                        } else {
                            set = m.get(lang);
                        }
                        if ("rank".equals(t)) {
                            set.setRank(Integer.parseInt(value));
                        } else if ("mandatory".equals(t)) {
//                            set.setMandatory(Boolean.valueOf(value).booleanValue());
                        } else if ("activated".equals(t)) {
                            set.setActivated(Boolean.valueOf(value).booleanValue());
                        }
                    } else if (firstKey.equals("mixLanguage")) {
                        site.setMixLanguagesActive(Boolean.getBoolean(value));
                    } else if (firstKey.startsWith("prod_") || firstKey.startsWith("html_") || firstKey.startsWith("wai_") || firstKey.startsWith("url_")) {
                        siteSettings = true;
                        site.getSettings().put(firstKey, value);
                    } else if (firstKey.startsWith("defaultSite") && "true".equals(value) && sitesService.getDefaultSite() == null) {
                        sitesService.setDefaultSite(site);
                    } else if (firstKey.startsWith("jahiaGAprofile")) {
                        siteSettings = true;
                        site.getSettings().put("jahiaGAprofile_"+firstKey.split("_")[1]+"_"+site.getSiteKey(), value);
                    }else if(firstKey.startsWith("profileCnt")){
                        siteSettings = true;
                        site.getSettings().put("profileCnt_"+site.getSiteKey(), value);
                    }
                    else if (firstKey.endsWith("gaUserAccount") || firstKey.endsWith("gaProfile") ||
                            firstKey.endsWith("gaLogin") || firstKey.endsWith("gaPassword") ||
                            firstKey.endsWith("trackedUrls") || firstKey.endsWith("trackingEnabled") ||
                            firstKey.endsWith("profileId")
                            ) {
                        siteSettings = true;
                        String profileName = firstKey.split("_")[0];
                        site.getSettings().put(profileName + "_" + site.getSiteKey() + "_" + firstKey.split("_")[2], value);
                    }
                }

                if (siteSettings) {
                    try {
                        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
                        startProductionJob(site, jParams);
                    } catch (ParseException e) {
                        logger.error("Cannot update site", e);
                    } catch (JahiaException e) {
                        logger.error("Cannot update site", e);
                    }
                }

                JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
                for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext();) {
                    String code = iterator.next();
                    SiteLanguageSettings set = m.get(code);
                    if (old.contains(code)) {
                        listManager.updateSiteLanguageSettings(set);
                    } else {
                        listManager.addSiteLanguageSettings(set);
                    }
                }
            } else if (name.equals(CATEGORIES_XML)) {
                catProps = importCategoriesAndGetUuidProps(zis, categoriesImportHandler);
            } else if (name.equals(SITE_PERMISSIONS_XML)) {
                importSitePermissions(jParams, zis);
            }
            zis.closeEntry();
        }
        zis.reallyClose();
        zis = new NoCloseZipInputStream(new FileInputStream(file));
        ContentObject obj = null;
        Map<String, String> importedMapping = new HashMap<String, String>();

//        File tplFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), jParams.getSite().getTemplateFolder());
//        File map = new File(tplFolder, "mapping")
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null) break;
            String name = zipentry.getName();
            if (name.equals(FILESACL_XML)) {
                importFilesAcl(jParams, zis);
            } else if (name.startsWith("export")) {
                String locale;
                if (name.indexOf("_") != -1) {
                    locale = name.substring(7, name.lastIndexOf("."));
                } else {
                    locale = jParams.getLocale().toString();
                }
                zipentry.getSize();
                if (obj == null) {
                    obj = importDocument(parent, locale, jParams, zis, false, setUuid, actions, result, uuidMapping, pathMapping, null, null, importedMapping);
                } else {
                    importDocument(obj, locale, jParams, zis, true, setUuid, actions, result, uuidMapping, pathMapping, null, null, importedMapping);
                }
            }
            zis.closeEntry();
        }
        zis.reallyClose();

        categoriesImportHandler.setUuidProps(catProps);
        usersImportHandler.setUuidProps(userProps);

        resolveCrossReferences(uuidMapping, references, jParams.getUser());

        return obj;
    }

    private JCRNodeWrapper ensureDir(String name, ProcessingContext jParams, JahiaSite site, Map<String, String> pathMapping) {
        JCRNodeWrapper dir = JCRStoreService.getInstance().getFileNode(name, jParams.getUser());
        if (!dir.isValid()) {
            int endIndex = name.lastIndexOf('/');
            if (endIndex == -1) {
                logger.warn("Cannot create folder " + name);
                return null;
            }
            JCRNodeWrapper parentDir = ensureDir(name.substring(0, endIndex), jParams, site, pathMapping);
            if (parentDir == null) {
                return null;
            }
            if (Constants.JAHIANT_VIRTUALSITES_FOLDER.equals(parentDir.getPrimaryNodeTypeName())) {
                dir = JCRStoreService.getInstance().getFileNode(parentDir.getPath() + "/" + site.getSiteKey(), jParams.getUser());
            } else {
                try {
                    parentDir.createCollection(name.substring(name.lastIndexOf('/') + 1));
                } catch (RepositoryException e) {
                    logger.error("RepositoryException", e);
                } finally {
                    if (parentDir.getTransactionStatus() == Status.STATUS_ACTIVE) {
                        try {
                            parentDir.refresh(false);
                        } catch (RepositoryException e) {
                            logger.error("error", e);
                        }
                    }
                }
                dir = JCRStoreService.getInstance().getFileNode(name, jParams.getUser());
                logger.debug("Folder created " + name);
            }
        } else {
            String current = name;

            while (current.lastIndexOf('/') > 0) {
                JCRNodeWrapper currentNode = JCRStoreService.getInstance().getFileNode(current, jParams.getUser());

                if (Constants.JAHIANT_VIRTUALSITE.equals(currentNode.getPrimaryNodeTypeName())) {
                    if (currentNode.getName().equals(site.getSiteKey())) {
                        break;
                    }
                    String newName = current.substring(0, current.lastIndexOf('/')) + "/" + site.getSiteKey();
                    pathMapping.put(current, newName);
                    name = name.replace(current, newName);

                    return ensureDir(name, jParams, site, pathMapping);
                }
                int endIndex = current.lastIndexOf('/');
                current = current.substring(0, endIndex);
            }
//            if ("jnt:virtualsite".equals(dir.getType())) {
//                int endIndex = name.lastIndexOf('/');
//                dir = JahiaWebdavBaseService.getInstance().getDAVFileAccess(name.substring(0, endIndex)+"/"+site.getSiteKey(), jParams.getUser());
//            }
        }
        return dir;
    }

    public void ensureFile(String path, InputStream inputStream, String type, ProcessingContext jParams, JahiaSite destSite, Map<String, String> pathMapping) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        JCRNodeWrapper parentDir = ensureDir(path.substring(0, path.lastIndexOf('/')), jParams, destSite, pathMapping);
        JCRNodeWrapper dest = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(parentDir.getPath() + "/" + name, jParams.getUser());
        logger.debug("Try to add file " + path + " - already exists=" + dest.isValid());
        if (!dest.isValid()) {
            if (parentDir == null) {
                logger.warn("Cannot create folder " + path.lastIndexOf('/'));
                return;
            }

            logger.debug("Add file to " + parentDir.getPath() + " (valid=" + parentDir.isValid() + ")");
            try {
                JCRNodeWrapper res = parentDir.uploadFile(name, inputStream, type);
                logger.debug("File added -> " + res);
                res.saveSession();
            } catch (RepositoryException e) {
                logger.error("RepositoryException", e);
            } finally {
                if (parentDir.getTransactionStatus() == Status.STATUS_ACTIVE) {
                    try {
                        parentDir.refresh(false);
                    } catch (RepositoryException e) {
                        logger.error("error", e);
                    }
                }
            }
        }
    }

    public ContentObject importDocument(ContentObject parent, String lang, ProcessingContext jParams, InputStream inputStream, boolean updateOnly, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result, Map<String, String> uuidMapping, Map<String, String> pathMapping, Map<String, Map<String, String>> typeMapping, Map<String, String> tplMapping, Map<String, String> importedMapping) {
        InputSource is = new InputSource(inputStream);
        JahiaUser oldUser = jParams.getUser();
        try {
            SAXParserFactory factory;

            factory = new SAXParserFactoryImpl();

            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            SAXParser parser = factory.newSAXParser();

            JahiaUser user = JahiaAdminUser.getAdminUser(jParams.getSiteID());
            jParams.setTheUser(user);

            ImportHandler handler = new ImportHandler(parent, jParams, lang, actions, result);
            handler.setUpdateOnly(updateOnly);
            handler.setCopyUuid(setUuid);
            handler.setPathMapping(pathMapping);

            handler.setTypeMappings(typeMapping);
            handler.setTemplateMappings(tplMapping);
            handler.setImportedMappings(importedMapping);

            handler.setUuidMapping(uuidMapping);

            parser.parse(is, handler);
            return handler.getLastObject();
        } catch (Exception e) {
            if ("languages".equals(e.getMessage())) {
                logger.warn("Languages do not match");
                result.setStatus(TreeOperationResult.FAILED_OPERATION_STATUS);
                final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.languages", new Object[]{lang});
                result.appendError(new NodeImportResult(null, null, msg, null, null, null, null, e));
            } else {
                logger.warn("Cannot import document", e);
                result.setStatus(TreeOperationResult.FAILED_OPERATION_STATUS);
                final EngineMessage msg = new EngineMessage("org.jahia.engines.importexport.import.fileerror", new Object[]{});
                result.appendError(new NodeImportResult(null, null, msg, null, null, null, null, e));
            }
            return null;
        } finally {
            jParams.setTheUser(oldUser);
        }
    }

    public String getUuid(ContentObject object) throws JahiaException {
        return object.getUUID();
    }

//    public String getUuid(ContentObject object) throws JahiaException {
//        String uuid = object.getProperty("uuid");
//        if (uuid == null) {
//            uuid = idGen.nextIdentifier().toString();
//            object.setProperty("uuid", uuid);
//        }
//        String orig = object.getProperty("originalUuid");
//        if (orig != null) {
//            uuid+= "/"+orig;
//        }
//        return uuid;
//    }

    //

    private void exportCategories(ContentHandler ch, JahiaUser user) throws JahiaException, SAXException {
        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", CDATA, JAHIA_URI);
        ch.startElement(JAHIA_URI, "categories", "jahia:categories", attr);

        Category c = categoryService.getRootCategory();
        exportCategories(ch, c, user);

        ch.endElement(JAHIA_URI, "categories", "jahia:categories");
        ch.endDocument();
    }

    private void exportCategories(ContentHandler ch, Category c, JahiaUser user) throws JahiaException, SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute(JAHIA_URI, "key", "jahia:key", CDATA, c.getKey());
        ((JahiaLegacyExporter) exporters.get(LEGACY_EXPORTER)).exportAcl(c.getACL(), "acl", attr, false, false);
        Map<String, String> titles = categoryService.getTitlesForCategory(c);
        for (Iterator<String> iterator = titles.keySet().iterator(); iterator.hasNext();) {
            String lang = (String) iterator.next();
            String value = (String) titles.get(lang);
            if (value != null) {
                attr.addAttribute(JAHIA_URI, "title_" + lang, "jahia:title_" + lang, CDATA, value);
            }
        }

        Properties p = c.getProperties();

        ch.startElement(JAHIA_URI, "category", "jahia:category", attr);

        for (Iterator<Object> iterator = p.keySet().iterator(); iterator.hasNext();) {
            String k = (String) iterator.next();
            String property = p.getProperty(k);
            if (XMLChar.isValidNCName(k) && property != null) {
                AttributesImpl attrProp = new AttributesImpl();
                attrProp.addAttribute(JAHIA_URI, "key", "jahia:key", CDATA, k);
                if (k.startsWith("homepage")) {
                    try {
                        ContentPage contentPage = ContentPage.getPage(Integer.parseInt((String) p.get(k)));
                        if (contentPage != null) {
                            property = getUuid(contentPage);
                        } else {
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                attrProp.addAttribute(JAHIA_URI, "value", "jahia:key", CDATA, property);
                ch.startElement(JAHIA_URI, "property", "jahia:property", attrProp);
                ch.endElement(JAHIA_URI, "property", "jahia:property");
            }
        }

        List<Category> children = c.getChildCategories(user);
        for (Iterator<Category> iterator = children.iterator(); iterator.hasNext();) {
            Category child = (Category) iterator.next();
            exportCategories(ch, child, user);
        }
        ch.endElement(JAHIA_URI, "category", "jahia:category");
    }

    public void exportUsersFile(OutputStream out) throws JahiaException, SAXException, IOException {
        DataWriter dw = new DataWriter(new OutputStreamWriter(out, "UTF-8"));
        exportUsers(dw);
    }

    private void exportUsers(ContentHandler ch) throws JahiaException, SAXException {
        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", CDATA, JAHIA_URI);

        ch.startElement(JAHIA_URI, "users", "jahia:users", attr);

        List<String> l = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserList("jahia_db");
        for (Iterator<String> iterator = l.iterator(); iterator.hasNext();) {
            String userkey = (String) iterator.next();
            JahiaDBUser jahiaUser = (JahiaDBUser) userManagerService.lookupUserByKey(userkey);
            if (JahiaUserManagerService.isNotGuest(jahiaUser)) {
                Properties p = jahiaUser.getUserProperties().getProperties();
                attr = new AttributesImpl();
                for (Iterator<Object> iterator1 = p.keySet().iterator(); iterator1.hasNext();) {
                    String k = (String) iterator1.next();
                    if (k.equals("user_homepage")) {
                        try {
                            ContentPage contentPage = ContentPage.getPage(Integer.parseInt((String) p.get(k)));
                            if (contentPage != null) {
                                attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, getUuid(contentPage));
                            } else {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    } else if (XMLChar.isValidNCName(k)) {
                        attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, (String) p.get(k));
                    }
                }
                attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, jahiaUser.getUsername());
                attr.addAttribute(JAHIA_URI, "password", "jahia:password", CDATA, "SHA-1:" + jahiaUser.getPassword());
                ch.startElement(JAHIA_URI, "user", "jahia:user", attr);
                ch.endElement(JAHIA_URI, "user", "jahia:user");
            }
        }


        ch.endElement(JAHIA_URI, "users", "jahia:users");
    }

    private void exportSiteUsers(ContentHandler ch, JahiaSite site) throws JahiaException, SAXException {
        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", CDATA, JAHIA_URI);

        ch.startElement(JAHIA_URI, "users", "jahia:users", attr);

        List<Principal> l = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMembers(site.getID());
        for (Iterator<Principal> iterator = l.iterator(); iterator.hasNext();) {
            JahiaDBUser jahiaUser = (JahiaDBUser) iterator.next();
            jahiaUser = (JahiaDBUser) userManagerService.lookupUserByKey(jahiaUser.getUserKey());
            if (JahiaUserManagerService.isNotGuest(jahiaUser)) {
                Properties p = jahiaUser.getUserProperties().getProperties();
                attr = new AttributesImpl();
                for (Iterator<Object> iterator1 = p.keySet().iterator(); iterator1.hasNext();) {
                    String k = (String) iterator1.next();
                    if (k.equals("user_homepage")) {
                        try {
                            ContentPage contentPage = ContentPage.getPage(Integer.parseInt((String) p.get(k)));
                            if (contentPage != null) {
                                attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, getUuid(contentPage));
                            } else {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    } else if (XMLChar.isValidNCName(k)) {
                        attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, (String) p.get(k));
                    }
                }
                attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, jahiaUser.getUsername());
                attr.addAttribute(JAHIA_URI, "password", "jahia:password", CDATA, "SHA-1:" + jahiaUser.getPassword());
                ch.startElement(JAHIA_URI, "user", "jahia:user", attr);
                ch.endElement(JAHIA_URI, "user", "jahia:user");
            }
        }
        Collection<String> c = ServicesRegistry.getInstance().getJahiaSiteGroupManagerService().getGroups(site.getID()).values();
        for (Iterator<String> iterator = c.iterator(); iterator.hasNext();) {
            String n = (String) iterator.next();
            JahiaGroup g = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(site.getID(), n);
            if (g.getSiteID() != 0 && !g.getGroupname().equals(JahiaGroupManagerService.GUEST_GROUPNAME) &&
                    !g.getGroupname().equals(JahiaGroupManagerService.USERS_GROUPNAME)) {
                Properties p = g.getProperties();
                attr = new AttributesImpl();
                for (Iterator<Object> iterator1 = p.keySet().iterator(); iterator1.hasNext();) {
                    String k = (String) iterator1.next();
                    if (k.equals("group_homepage")) {
                        try {
                            ContentPage contentPage = ContentPage.getPage(Integer.parseInt((String) p.get(k)));
                            if (contentPage != null) {
                                attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, getUuid(contentPage));
                            } else {
                                continue;
                            }
                        } catch (Exception e) {
                            continue;
                        }
                    } else if (XMLChar.isValidNCName(k)) {
                        attr.addAttribute(JAHIA_URI, k, "jahia:" + k, CDATA, (String) p.get(k));
                    }
                }
                attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, g.getGroupname());
                ch.startElement(JAHIA_URI, "group", "jahia:group", attr);

                Enumeration<Principal> en = g.members();
                while (en.hasMoreElements()) {
                    Principal principal = (Principal) en.nextElement();
                    attr = new AttributesImpl();
                    if (principal instanceof JahiaUser) {
                        attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, ((JahiaUser) principal).getUsername());
                        ch.startElement(JAHIA_URI, "user", "jahia:user", attr);
                        ch.endElement(JAHIA_URI, "user", "jahia:user");
                    } else {
                        attr.addAttribute(JAHIA_URI, "name", "jahia:name", CDATA, ((JahiaGroup) principal).getGroupname());
                        ch.startElement(JAHIA_URI, "group", "jahia:group", attr);
                        ch.endElement(JAHIA_URI, "group", "jahia:group");
                    }
                }

                ch.endElement(JAHIA_URI, "group", "jahia:group");
            }
        }

        ch.endElement(JAHIA_URI, "users", "jahia:users");
    }

    private void exportServerPermissions(ContentHandler ch) throws JahiaException, SAXException {
        exportPermissions(ch, "org.jahia.actions.server", "serverPermission");
    }

    private void exportSitePermissions(ContentHandler ch, JahiaSite site) throws JahiaException, SAXException {
        exportPermissions(ch, ManageSitePermissions.SITE_PERMISSIONS_PREFIX + site.getID(), "sitePermission");
    }

    private void exportPermissions(ContentHandler ch, String prefix, String nodeName) throws SAXException {
        ch.startDocument();
        ch.startPrefixMapping("jahia", JAHIA_URI);
        ch.endPrefixMapping("jahia");
        AttributesImpl attr = new AttributesImpl();

        attr.addAttribute(NS_URI, "jahia", "xmlns:jahia", CDATA, JAHIA_URI);

        ch.startElement(JAHIA_URI, nodeName + "s", "jahia:" + nodeName + "s", attr);

        List<JahiaAclName> list = ServicesRegistry.getInstance().getJahiaACLManagerService().getAclNamesStartingWith(prefix);
        JahiaSitesService siteService = ServicesRegistry.getInstance().getJahiaSitesService();
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
//                            if ("serverPermission".equals(nodeName)) {
//                                // also add site key information
//                                try {
//                                    perms += ":"
//                                            + (user.getSiteID() == 0 ? "0"
//                                            : siteService.getSite(
//                                            user.getSiteID())
//                                            .getSiteKey());
//                                } catch (Exception e) {
//                                    logger.error(
//                                            "Unable to obtain site key for site with ID '"
//                                                    + user.getSiteID() + "'", e);
//                                }
//                            }
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
                                            : siteService.getSite(
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

    private void importFilesAcl(ProcessingContext jParams, InputStream is) {
        handleImport(is, new FilesAclImportHandler(jParams));
    }

    private List<String[]> importCategoriesAndGetUuidProps(InputStream is, CategoriesImportHandler importHandler) {
        handleImport(is, importHandler);
        return importHandler.getUuidProps();
    }

    public void importCategories(ProcessingContext jParams, InputStream is) {
        importCategoriesAndGetUuidProps(is, new CategoriesImportHandler(jParams));
    }

    private List<String[]> importUsersAndGetUuidProps(InputStream is, UsersImportHandler importHandler) {
        handleImport(is, importHandler);
        return importHandler.getUuidProps();
    }

    public void importServerPermissions(ProcessingContext jParams, InputStream is) {
        handleImport(is, new PermissionsImportHandler("org.jahia.actions.server", "serverPermission", (Map) jParams.getAttribute("sitePermissions_siteKeyMapping")));
    }

    public void importSitePermissions(ProcessingContext jParams, InputStream is) {
        handleImport(is, new PermissionsImportHandler(ManageSitePermissions.SITE_PERMISSIONS_PREFIX + jParams.getSite().getID(), "sitePermission", jParams.getSite().getID()));
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
        }
        catch (Exception e) {
            logger.error("Cannot import", e);
        }
    }

    public ContentObject copy(ContentObject source, ContentObject parentDest, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result) {
        try {
            return copy(source, parentDest, getSiteLanguages(jParams.getSite()), jParams, loadRequest, link, actions, result);
        } catch (JahiaException e) {
            logger.error("Error during copying", e);
            return null;
        }
    }

    public ContentObject copy(ContentObject source, ContentObject parentDest, Set<String> languages, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result) {
        JahiaUser oldUser = jParams.getUser();
        try {
            JahiaSite destSite = sitesService.getSite(parentDest.getSiteID());
            JahiaUser user = JahiaAdminUser.getAdminUser(destSite.getID());
            jParams.setTheUser(user);

            Set<JCRNodeWrapper> files = new HashSet<JCRNodeWrapper>();
            Map<String, String> pathMapping = new HashMap<String, String>();
            if (source.getSiteID() != parentDest.getSiteID()) {
                logger.debug("Copying files from site " + source.getSiteID() + " to " + parentDest.getSiteID());
                for (Iterator<String> iterator = languages.iterator(); iterator.hasNext();) {
                    String lang = iterator.next();
                    exporters.get(LEGACY_EXPORTER).getFiles(source, lang, files, jParams, null, loadRequest);
                }
                for (Iterator<JCRNodeWrapper> fiterator = files.iterator(); fiterator.hasNext();) {
                    JCRNodeWrapper file = fiterator.next();
                    //if (file.isValid()) {
                    InputStream inputStream = file.getFileContent().downloadFile();
                    String type = file.getFileContent().getContentType();
                    ensureFile(file.getPath(), inputStream, type, jParams, destSite, pathMapping);
                    //}
                }
            }

            Iterator<String> iterator = languages.iterator();
            String lang = iterator.next();

            Map<String, Object> params = new HashMap<String, Object>();
            boolean copyReadAccessOnly = false;
            params.put(LINK, link);
            params.put(TO, loadRequest);
            if (link != null) {
                params.put(VIEW_PICKERS, Boolean.FALSE);
                copyReadAccessOnly = true;
            }

            // Copy first lang only
            ImportHandler handler = new ImportHandler(parentDest, jParams, lang, actions, result);
            handler.setCopyUuid(true);
            handler.setCopyReadAccessOnly(copyReadAccessOnly);
            handler.setRestoreAcl(false);
            handler.setPathMapping(pathMapping);
            export(source, lang, handler, new HashSet<JCRNodeWrapper>(), jParams, params);
            ContentObject main = handler.getLastObject();
            String topAcl = handler.getTopAcl();
            for (; iterator.hasNext();) {
                String nextLang = iterator.next();
                handler = new ImportHandler(main, jParams, nextLang, actions, result);
                handler.setUpdateOnly(true);
                handler.setCopyUuid(true);
                handler.setCopyReadAccessOnly(copyReadAccessOnly);
                handler.setRestoreAcl(false);
                handler.setPathMapping(pathMapping);
                export(source, nextLang, handler, new HashSet<JCRNodeWrapper>(), jParams, params);
            }
            handler.restoreAcl(topAcl, main);
            if (StructuralRelationship.ACTIVATION_PICKER_LINK.equals(link)) {
                main.setProperty("lastImportedVersion", Long.toString(System.currentTimeMillis() / 1000));
            }

            return handler.getLastObject();
        } catch (Exception e) {
            logger.error("Error during copying", e);
        } finally {
            jParams.setTheUser(oldUser);
        }
        return null;
    }


    public boolean isCompatible(JahiaContainerDefinition dest, JahiaContainerDefinition source) {
        if (dest.getID() == source.getID()) {
            return true;
        }
        // ok if both types are sames, or  if one inherit from the other
        if (dest.getNodeType().isNodeType(source.getNodeType().getName()) || source.getNodeType().isNodeType(dest.getNodeType().getName())) {
            return true;
        }

        return false;
    }

    public boolean isCompatible(JahiaContainerDefinition dest, NodeType sourceType, ProcessingContext context) {
        // ok if both types are sames, or  if one inherit from the other
        if (dest.getNodeType().isNodeType(sourceType.getName()) || sourceType.isNodeType(dest.getNodeType().getName())) {
            String s = dest.getContainerListNodeDefinition().getSelectorOptions().get("availableTypes");
            if (s != null) {
                if (s.contains(sourceType.getName())) {
                    return true;
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isCompatible(JahiaContainerDefinition dest, ContentContainer source, ProcessingContext context) {
        try {
            final JahiaContainerDefinition sDef = (JahiaContainerDefinition) ContentDefinition.getContentDefinitionInstance(source.getDefinitionKey());

            if (dest.getID() == sDef.getID()) {
                return true;
            }
            // ok if both types are sames, or  if one inherit from the other
            if (dest.getNodeType().isNodeType(sDef.getNodeType().getName()) || sDef.getNodeType().isNodeType(dest.getNodeType().getName())) {
                String s = dest.getContainerListNodeDefinition().getSelectorOptions().get("availableTypes");
                if (s != null) {
                    try {
                        NodeType sourceNodeType = source.getJCRNode(context).getPrimaryNodeType();
                        if (s.contains(sourceNodeType.getName())) {
                            return true;
                        }
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (JahiaException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return false;
                }
                return true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public boolean isPicker(ContentObject object) throws JahiaException {
        if (object == null) {
            return false;
        }
        if (object.getPickedObject() != null) {
            return true;
        }
        return false;
    }

    public void startProductionJob(JahiaSite site, ProcessingContext jParams) throws ParseException {
        Properties siteSettings = site.getSettings();
        String s = siteSettings.getProperty(PRODUCTION_TARGET_LIST_PROPERTY, "");
        if ("".equals(s)) {
            return;
        }
        String[] targetSites = s.split(",");
        if (targetSites.length > 0) {
            for (int i = 0; i < targetSites.length; i++) {
                String targetSite = targetSites[i];
                try {
                    JobDetail jobDetail = schedulerService.getJobDetail(ProductionJob.JOB_NAME_PREFIX + site.getID() + "_" + targetSite,
                            BackgroundJob.getGroupName(ProductionJob.class));

                    if (jobDetail == null) {
                        jobDetail = BackgroundJob.createJahiaJob(ProductionJob.JOB_NAME_PREFIX, ProductionJob.class, jParams);
                        jobDetail.setName(ProductionJob.JOB_NAME_PREFIX + site.getID() + "_" + targetSite);
                        jobDetail.setRequestsRecovery(true);
                    } else {
                        logger.debug("delete previous production  job" + ProductionJob.JOB_NAME_PREFIX + site.getID() + "_" + targetSite);
                        schedulerService.deleteJob(jobDetail.getName(), jobDetail.getGroup());

                        jobDetail = BackgroundJob.createJahiaJob(ProductionJob.JOB_NAME_PREFIX, ProductionJob.class, jParams);
                        jobDetail.setName(ProductionJob.JOB_NAME_PREFIX + site.getID() + "_" + targetSite);
                        jobDetail.setRequestsRecovery(true);
                    }

                    JobDataMap jobDataMap = jobDetail.getJobDataMap();

                    //set the type of job
                    jobDataMap.put(BackgroundJob.JOB_TYPE, ProductionJob.PRODUCTION_TYPE); //production job

                    // set user to selected profile instead of current user
                    JahiaUser profile = ServicesRegistry.getInstance()
                            .getJahiaSiteUserManagerService()
                            .getMember(site.getID(), siteSettings.getProperty(PRODUCTION_PROFILE_PROPERTY + targetSite));

                    if (profile != null) {
                        jobDataMap.put(BackgroundJob.JOB_USERKEY, profile.getUserKey());
                    } else {
                        logger.warn("Invalid profile : " + siteSettings.getProperty(PRODUCTION_PROFILE_PROPERTY + targetSite));
                    }

                    jobDataMap.put(ProductionJob.SITE_ID, site.getID());
                    jobDataMap.put(ProductionJob.TARGET, siteSettings.getProperty(PRODUCTION_TARGET + targetSite, ""));
                    jobDataMap.put(ProductionJob.USERNAME, siteSettings.getProperty(PRODUCTION_USERNAME_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.PASSWORD, siteSettings.getProperty(PRODUCTION_PASSWORD_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.PROFILE, siteSettings.getProperty(PRODUCTION_PROFILE_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.SITE_NAME, siteSettings.getProperty(PRODUCTION_SITE_NAME_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.ALIAS, siteSettings.getProperty(PRODUCTION_ALIAS_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.JOB_TITLE, siteSettings.getProperty(PRODUCTION_ALIAS_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.METADATA, siteSettings.getProperty(PRODUCTION_METADATA_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.WORKFLOW, siteSettings.getProperty(PRODUCTION_WORKFLOW_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.ACL, siteSettings.getProperty(PRODUCTION_ACL_PROPERTY + targetSite, ""));
                    jobDataMap.put(ProductionJob.AUTO_PUBLISH, siteSettings.getProperty(PRODUCTION_AUTO_PUBLISH_PROPERTY + targetSite, ""));

                    CronTrigger trigger = null;

                    String cronExpession = siteSettings.getProperty(PRODUCTION_CRON_PROPERTY + targetSite, "");

                    if (!"".equals(cronExpession)) {
                        trigger = new CronTrigger(ProductionJob.TRIGGER_NAME_PREFIX + site.getID() + "_" + targetSite,
                                SchedulerService.REPEATED_TRIGGER_GROUP, cronExpession);
                        schedulerService.scheduleJob(jobDetail, trigger);
                    }
                } catch (JahiaException e) {
                    logger.warn("Error during starting of production Job for site " + site.getID(), e);
                }

            }
        }
    }


    /**
     * Export all modification on a site from a specified date, with a particular profile.
     *
     * @param site         the site to export
     * @param targetName   url of the site to upload the export result
     * @param exportTime   date of export
     * @param username     user on target site
     * @param password     password on target site
     * @param member       profile for export
     * @param sitename
     * @param withMetadata
     * @param withWorkflow
     * @param withAcl      @return a webdav resource containing the result of the import @throws IOException
     * @param publishAtEnd @throws IOException
     * @throws JahiaException
     * @throws SAXException
     */
    public WebdavResource exportToSite(JahiaSite site, String targetName, Date exportTime, String username,
                                       String password, JahiaUser member, String sitename, boolean withMetadata, boolean withWorkflow, boolean withAcl, boolean publishAtEnd)
            throws IOException, JahiaException, SAXException {
        String dateOfExport = ManageImportExport.DATE_FORMAT.format(exportTime);
        // todo use some parameters to configure the upload url
        password = new String(Base64.decode(password));
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

        String baseTarget = targetName.substring(0, targetName.lastIndexOf('/') + 1);

        sitename = sitename.substring(sitename.lastIndexOf('/') + 1);

        String folder = baseTarget + "repository/default/content/users/" + username + "/files/private/imports";
        HttpURL folderURL = new HttpURL(folder);
        WebdavResource folderRes = new WebdavResource(folderURL, credentials, WebdavResource.DEFAULT, 0);
        int code = folderRes.getStatusCode();
        if (code < 200 || code >= 300) {
            if (!folderRes.mkcolMethod()) {
                logger.error("Cannot create folder at : "+folderRes);
                throw new IOException();
            }
        }

        SortedSet<WebdavResource> set = new TreeSet<WebdavResource>(new Comparator<WebdavResource>() {
            public int compare(WebdavResource o1, WebdavResource o2) {
                return (int) (o1.getGetLastModified() - o2.getGetLastModified());
            }
        });
        WebdavResource[] resources = folderRes.listWebdavResources();
        for (WebdavResource resource : resources) {
            if (resource.getName().startsWith("importFromSite_" + site.getSiteKey() + "_")) {
                set.add(resource);
            }
        }
        Object[] array = set.toArray();
        for (int i = 0; i < array.length - 9; i++) {
            (new WebdavResource(((WebdavResource) array[i]).getHttpURL(), credentials, WebdavResource.DEFAULT, 0)).deleteMethod();
        }

        StringBuffer name = new StringBuffer("importFromSite_").append(site.getSiteKey()).append("_toSite_").append(sitename);
        if (publishAtEnd) {
            name.append("_AndPublish_");
        } else name.append("_");
        name.append(dateOfExport).append(".zip");
        String url = folder + "/" + name.toString();
        HttpURL httpURL = new HttpURL(url);
        WebdavResource webdavSession = new WebdavResource(httpURL, credentials, WebdavResource.NOACTION, 0);
        //might be necessary for ESI
        webdavSession.setFollowRedirects(true);

        final ContentPage homeContentPage = site.getHomeContentPage();
        // There is a previous call so export only modification from this point in time
        final List<Locale> languageSettingsAsLocales = site.getLanguageSettingsAsLocales(true);

        EntryLoadRequest toLoadRequest = new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE, new Long(System.currentTimeMillis() / 1000).intValue(), languageSettingsAsLocales);
        toLoadRequest.setWithDeleted(true);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(TO, toLoadRequest);
        params.put(VIEW_VERSION, Boolean.TRUE);
        params.put(INCLUDE_FILES, Boolean.TRUE);
        params.put(VIEW_METADATA, Boolean.valueOf(withMetadata));
        params.put(VIEW_WORKFLOW, Boolean.valueOf(withWorkflow));
        params.put(VIEW_ACL, Boolean.valueOf(withAcl));
        params.put(VIEW_JAHIALINKS, Boolean.valueOf(withMetadata));
        params.put(VIEW_PICKERS, Boolean.FALSE);


//        URL exportFilesDateUrl = new URL(targetName + "/" + contextname + servletname + "/engineName/export/site/" + sitename + "/op/edit/export.xml?exportformat=filesacl");
        InputStream st; // = JahiaTools.makeJahiaRequest(exportFilesDateUrl, member, username, password, 5);
        SAXParserFactory factory = new SAXParserFactoryImpl();
//        try {
//            SAXParser parser = factory.newSAXParser();
//            FilesDateImportHandler handler = new FilesDateImportHandler();
//            parser.parse(st, handler);
//            Map<String, Date> filesToDate = handler.getFileToDate();
//            params.put(FILES_DATE, filesToDate);
//        } catch (ParserConfigurationException e) {
//            throw new SAXException(e);
//        }


        Map<String, Map<String, EntryLoadRequest>> froms = new HashMap<String, Map<String, EntryLoadRequest>>();
        for (Iterator<Locale> iterator = languageSettingsAsLocales.iterator(); iterator.hasNext();) {
            Locale locale = iterator.next();
            logger.info("Getting " + targetName + " status for " + locale);
//            URL exportUrl = new URL(targetName + "/" + contextname + servletname + "/engineName/export/site/" + sitename + "/op/edit/lang/" + locale + "/export.xml?exporttype=staging&exportformat=xml&viewVersion=true&viewMetadata=false&viewContent=false&viewAcl=false&enforceLanguage=true&lock="+name);
            URL exportUrl = new URL(targetName + "/engineName/export/site/" + sitename + "/op/edit/lang/" + locale + "/export.xml?exportformat=versions&enforceLanguage=true&lock=" + name);
            if (logger.isDebugEnabled())
                logger.debug("Export Url is : " + exportUrl);

            try {
                st = JahiaTools.makeJahiaRequest(exportUrl, member, username, password, 5);
                SAXParser parser = null;
                try {
                    parser = factory.newSAXParser();
                } catch (ParserConfigurationException e) {
                    throw new SAXException(e);
                }
                VersionNumberHandler handler = new VersionNumberHandler(languageSettingsAsLocales);
                parser.parse(st, handler);
                Map<String, EntryLoadRequest> uuidToVersions = handler.getUuidToVersions();
                if (uuidToVersions.isEmpty()) {
                    // first call
                    ContentObjectEntryState es = (ContentObjectEntryState) homeContentPage.getEntryStates().first();
                    uuidToVersions.put(getUuid(homeContentPage), new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE, es.getVersionID() - 1, languageSettingsAsLocales));
                }
                logger.debug("Infos for locale " + locale + ", " + uuidToVersions.size() + " objects.");
                froms.put(locale.toString(), uuidToVersions);
            } catch (HttpException e) {
                if (e.getReasonCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    logger.info("Target is locked [" + targetName + "] / " + locale);
                    sendMail(targetName);
                } else {
                    logger.info("Cannot get infos for [" + targetName + "] / " + locale);
                }
            } catch (IOException e) {
                logger.info("Cannot get infos for [" + targetName + "] / " + locale);
            } catch (SAXException e) {
                logger.error("Cannot parse infos for [" + targetName + "] / " + locale);
            }
        }

        params.put(FROM, froms);
        if (!froms.isEmpty()) {
            File tempFile = File.createTempFile("exportFromSite_" + site.getSiteKey() + "_" + dateOfExport, ".zip");
            try {
                FileOutputStream out = new FileOutputStream(tempFile);
                ProcessingContext threadParamBean = new ProcessingContext(null, exportTime.getTime(), site, member, homeContentPage);
                final URLGenerator urlGenerator = new BasicURLGeneratorImpl();
                threadParamBean.setUrlGenerator(urlGenerator);
                logger.info("Exporting differences to " + httpURL + " for languages " + froms.keySet());
                exportZip(homeContentPage, froms.keySet(), out, threadParamBean, params);
                out.close();
                FileInputStream inputStream = new FileInputStream(tempFile);
                webdavSession.addRequestHeader("Expect", "100-continue");
                webdavSession.putMethod(inputStream);
                inputStream.close();
            } catch (Exception e) {
                URL unlockUrl = new URL(targetName + "/engineName/export/site/" + sitename + "/op/edit/lang/export.xml?unlock=" + name);
                JahiaTools.makeJahiaRequest(unlockUrl, member, username, password, 5);
                logger.error("Error when exporting content", e);
            } finally {
                tempFile.delete();
            }
        }
        return webdavSession;
    }

    public void sendMail(String target) throws JahiaException {
        MailService mailService = ServicesRegistry.getInstance().getMailService();

        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.engines.workflow.MailNotifications", 0)
                || !mailService.isEnabled()) {
            return;
        }

        GroovyScriptEngine groovyScriptEngine = (GroovyScriptEngine) SpringContextSingleton.getInstance().getContext().getBean("groovyScriptEngine");
        GroovyMimeMessagePreparator workflowMimePreparator = new GroovyMimeMessagePreparator();
        workflowMimePreparator.setGroovyScriptEngine(groovyScriptEngine);

        GroovyMimeMessagePreparator adminMessageMimePreparator = new GroovyMimeMessagePreparator();
        adminMessageMimePreparator.setGroovyScriptEngine(groovyScriptEngine);
        Binding binding = new Binding();
        binding.setVariable("from", mailService.defaultSender());
        binding.setVariable("to", mailService.defaultRecipient());
        binding.setVariable("target", target);

        adminMessageMimePreparator.setBinding(binding);
        adminMessageMimePreparator.setTemplatePath("autoexport_cancelled.groovy");
        mailService.sendTemplateMessage(adminMessageMimePreparator);
    }

    private Set<String> getSiteLanguages(JahiaSite site) throws JahiaException {
        Set<String> languages = new HashSet<String>();
        List<SiteLanguageSettings> v = site.getLanguageSettings(true);
        for (Iterator<SiteLanguageSettings> iterator = v.iterator(); iterator.hasNext();) {
            SiteLanguageSettings sls = (SiteLanguageSettings) iterator.next();
            languages.add(sls.getCode());
        }

        return languages;
    }

    public void setExcludedResources(final List<String> resources) {
        excludedResourcesFilter = new ExclusionWildcardFilter(resources);
    }

    public PathFilter getExcludedResourcesFilter() {
        return excludedResourcesFilter;
    }

    public boolean isOverwriteResourcesByImport() {
        return overwriteResourcesByImport;
    }

    public void setOverwriteResourcesByImport(boolean overwrtiteResourcesByImport) {
        this.overwriteResourcesByImport = overwrtiteResourcesByImport;
    }

    private List<String[]> importUsers(File file, UsersImportHandler usersImportHandler)
            throws IOException {
        NoCloseZipInputStream zis = new NoCloseZipInputStream(new FileInputStream(file));
        List<String[]>userProps = null;
        while (true) {
            ZipEntry zipentry = zis.getNextEntry();
            if (zipentry == null)
                break;
            String name = zipentry.getName();
            if (name.equals("users.xml")) {
                userProps = importUsersAndGetUuidProps(zis, usersImportHandler);
                break;
            }
            zis.closeEntry();
        }
        zis.reallyClose();
        try {
            ServicesRegistry.getInstance().getCacheKeyGeneratorService().start();
        } catch (JahiaInitializationException e) {
            logger.error(e.getMessage(), e);
        }
        return userProps;
    }

    public List<String[]> importUsers(File file)
            throws IOException {
        return importUsersAndGetUuidProps(new FileInputStream(file), new UsersImportHandler());
    }

    public List<String[]> importUsers(File file, JahiaSite site)
            throws IOException {
        return importUsers(file, new UsersImportHandler(site));
    }

    private void resolveCrossReferences(Map<String,String> uuidMapping, Map<String,String> references, JahiaUser user) {
        try {
            JCRNodeWrapper refRoot = JCRStoreService.getInstance().getFileNode("/content/referencesKeeper", user);
            NodeIterator ni = refRoot.getNodes();
            while (ni.hasNext()) {
                Node refNode = ni.nextNode();
                String uuid = refNode.getProperty("j:originalUuid").getString();
                if (uuidMapping.containsKey(uuid)) {
                    String pName = refNode.getProperty("j:propertyName").getString();
                    String refuuid = refNode.getProperty("j:node").getString();
                    Node n = JCRStoreService.getInstance().getNodeByUUID(refuuid, user);
                    n.setProperty(pName,uuidMapping.get(uuid));
                    n.save();
                    refNode.remove();
                    refRoot.save();
                }
            }
            for (String uuid : references.keySet()) {
                if (uuidMapping.containsKey(uuid)) {
                    String path = references.get(uuid);
                    Node n = JCRStoreService.getInstance().getNodeByUUID(path.substring(0,path.lastIndexOf("/")), user);
                    String pName = path.substring(path.lastIndexOf("/")+1);

                    try {
                        n.setProperty(pName,uuidMapping.get(uuid));
                        n.save();
                    } catch (ItemNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    // store reference
                    String path = references.get(uuid);
                    JCRNodeWrapper r = refRoot.addNode("j:reference","jnt:reference");
                    String refuuid = path.substring(0,path.lastIndexOf("/"));
                    String pName = path.substring(path.lastIndexOf("/")+1);
                    r.setProperty("j:node", refuuid);
                    r.setProperty("j:propertyName", pName);
                    r.setProperty("j:originalUuid", uuid);
                    refRoot.save();
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

}
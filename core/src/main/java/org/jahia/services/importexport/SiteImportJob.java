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
package org.jahia.services.importexport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.rules.Service;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.jahia.services.importexport.ImportExportBaseService.*;

public class SiteImportJob extends BackgroundJob {
    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    public static final String NODEPATH = "nodepath";
    public static final String FILE_PATH = "filePath";
    public static final String DELETE_FILE = "deleteFile";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();

        BaseSiteImporter siteImporter;
        if (jobDataMap.get(NODEPATH) != null) {
            siteImporter = new NodeSiteImporter(jobDataMap.getString(NODEPATH), user);
        } else if (jobDataMap.get(FILE_PATH) != null) {
            siteImporter = new FileSiteImporter(jobDataMap.getString(FILE_PATH), jobDataMap.getBoolean(DELETE_FILE), user);
        } else {
            return;
        }

        siteImporter.execute();
        siteImporter.onImportFinish();
    }

    private abstract class BaseSiteImporter {
        String uri;
        String name;
        JahiaUser user;
        JCRSessionWrapper session;

        public BaseSiteImporter(String uri, String name, JahiaUser user) throws RepositoryException {
            this.uri = uri;
            this.name = name;
            this.user = user;
            this.session = JCRSessionFactory.getInstance().getCurrentUserSession();
        }

        public void execute() throws RepositoryException {
            StringTokenizer st = new StringTokenizer(name, "_");

            String type = st.nextToken();
            if (type.equals("siteImport")) {
                try {
                    logger.info("Import site " + uri);
                    //String sitename = st.nextToken() + "_" + st.nextToken();

                    if (!user.isRoot()) {
                        return;
                    }

                    importSiteZip();
                } catch (Exception e) {
                    logger.error("Error during import of file " + uri, e);
                    ServicesRegistry.getInstance().getCacheService().flushAllCaches();
                }

            } else if (name.endsWith(".zip")) {
                try {
                    processFileImport(prepareFileImports());
                } catch (IOException | JahiaException | ServletException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (name.endsWith(".xml")) {
                try {
                    session.importXML("/", getInputStream(), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                    session.save();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        private List<Map<Object, Object>> prepareFileImports() throws IOException, RepositoryException {
            Properties exportProps = new Properties();
            ZipInputStream zis = new ZipInputStream(getInputStream());
            ZipEntry z;
            Map<File, String> imports = new HashMap<File, String>();
            List<File> importList = new ArrayList<File>();
            while ((z = zis.getNextEntry()) != null) {
                File i = File.createTempFile("import", ".zip");
                OutputStream os = new BufferedOutputStream(new FileOutputStream(i));
                try {
                    IOUtils.copy(zis, os);
                } finally {
                    IOUtils.closeQuietly(os);
                }

                String n = z.getName();
                if (n.equals(EXPORT_PROPERTIES)) {
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(i), 1024);
                        exportProps.load(is);
                    } finally {
                        IOUtils.closeQuietly(is);
                        FileUtils.deleteQuietly(i);
                    }
                } else if (n.equals("classes.jar")) {
                    FileUtils.deleteQuietly(i);
                } else if (n.equals(SITE_PROPERTIES) || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                    // this is a single site import, stop everything and import
                    FileUtils.deleteQuietly(i);
                    for (File file : imports.keySet()) {
                        FileUtils.deleteQuietly(file);
                    }
                    imports.clear();
                    importList.clear();
                    File tempFile = File.createTempFile("import", ".zip");
                    InputStream is = getInputStream();
                    OutputStream tos = new BufferedOutputStream(new FileOutputStream(tempFile));
                    try {
                        IOUtils.copy(is, tos);
                    } finally {
                        IOUtils.closeQuietly(is);
                        IOUtils.closeQuietly(tos);
                    }

                    imports.put(tempFile, name);
                    importList.add(tempFile);
                    break;
                } else {
                    imports.put(i, n);
                    importList.add(i);
                }
            }

            List<Map<Object, Object>> importsInfos = new ArrayList<Map<Object, Object>>();
            Map<Object, Object> users = null;
            Map<Object, Object> serverPermissions = null;
            for (Iterator<File> iterator = importList.iterator(); iterator.hasNext(); ) {
                File i = iterator.next();
                String fileName = imports.get(i);
                Map<Object, Object> value = prepareSiteImport(i, imports.get(i));
                if (value != null) {
                    if (USERS_XML.equals(fileName) || USERS_ZIP.equals(fileName)) {
                        users = value;
                    } else if (SERVER_PERMISSIONS_XML.equals(fileName)) {
                        serverPermissions = value;
                    } else {
                        importsInfos.add(value);
                    }
                }
            }

            if (serverPermissions != null) {
                importsInfos.add(0, serverPermissions);
            }
            if (users != null) {
                importsInfos.add(0, users);
            }
            return importsInfos;
        }

        private Map<Object, Object> prepareSiteImport(File i, String filename) throws IOException {
            Map<Object, Object> importInfos = new HashMap<Object, Object>();
            importInfos.put("importFile", i);
            importInfos.put("importFileName", filename);
            importInfos.put("selected", Boolean.TRUE);
            if (filename.endsWith(".xml")) {
                importInfos.put("type", "xml");
            } else {
                ZipEntry z;
                ZipInputStream zis2 = new ZipInputStream(new BufferedInputStream(new FileInputStream(i)));
                boolean isSite = false;
                boolean isLegacySite = false;
                try {
                    while ((z = zis2.getNextEntry()) != null) {
                        if (SITE_PROPERTIES.equals(z.getName())) {
                            Properties p = new Properties();
                            p.load(zis2);
                            zis2.closeEntry();
                            importInfos.putAll(p);
                            importInfos.put("templates", importInfos.containsKey("templatePackageName") ? importInfos.get(
                                    "templatePackageName") : "");
                            importInfos.put("oldsitekey", importInfos.get("sitekey"));
                            isSite = true;
                        } else if (z.getName().startsWith("export_")) {
                            isLegacySite = true;
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(zis2);
                }
                importInfos.put("isSite", Boolean.valueOf(isSite));
                // todo import ga parameters
                if (isSite || isLegacySite) {
                    importInfos.put("type", "site");
                    if (!importInfos.containsKey("sitekey")) {
                        importInfos.put("sitekey", "");
                        importInfos.put("siteservername", "");
                        importInfos.put("sitetitle", "");
                        importInfos.put("description", "");
                        importInfos.put("mixLanguage", "false");
                        importInfos.put("templates", "");
                        importInfos.put("siteKeyExists", Boolean.TRUE);
                        importInfos.put("siteServerNameExists", Boolean.TRUE);
                    } else {
                        try {
                            JahiaSitesService sitesService = ServicesRegistry.getInstance().getJahiaSitesService();
                            importInfos.put("siteKeyExists", sitesService.getSiteByKey((String) importInfos.get("sitekey")) != null || "".equals(importInfos.get("sitekey")));
                            importInfos.put("siteServerNameExists", sitesService.getSite((String) importInfos.get("siteservername")) != null || "".equals(importInfos.get("siteservername")));
                        } catch (JahiaException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } else {
                    importInfos.put("type", "files");
                }

            }
            return importInfos;
        }

        private void processFileImport(List<Map<Object, Object>> importsInfos)
                throws IOException, RepositoryException, ServletException, JahiaException {

            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (infos.get("importFileName").equals(USERS_XML)) {
                    ImportExportBaseService.getInstance().importUsers(file);
                    break;
                }
            }

            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (file != null && infos.get("type").equals("files")) {
                    try {
                        ImportExportBaseService.getInstance().importSiteZip(new FileSystemResource(file), null, infos);
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else if (infos.get("type").equals("xml") && (infos.get("importFileName").equals(
                        SERVER_PERMISSIONS_XML) || infos.get("importFileName").equals(USERS_XML))) {

                } else if (infos.get("type").equals("site")) {
                    // site import
                    String tpl = (String) infos.get("templates");
                    if ("".equals(tpl)) {
                        tpl = null;
                    }
                    try {
                        SiteCreationInfo siteCreationInfo = SiteCreationInfo.builder().
                                siteKey((String) infos.get("sitekey")).
                                serverName((String) infos.get("siteservername")).
                                serverNameAliases((String) infos.get("siteservernamealiases")).
                                title((String) infos.get("sitetitle")).
                                description("").
                                templateSet(tpl).
                                modulesToDeploy(null).
                                locale(infos.containsKey("defaultLanguage") ? (String) infos.get("defaultLanguage") : SettingsBean.getInstance().getDefaultLanguageCode()).
                                siteAdmin(user).
                                firstImport("fileImport").
                                fileImport(file == null ? null : new FileSystemResource(file)).
                                fileImportName((String) infos.get("importFileName")).
                                originatingJahiaRelease((String) infos.get("originatingJahiaRelease")).build();
                        ServicesRegistry.getInstance().getJahiaSitesService().addSite(siteCreationInfo);

                    } catch (Exception e) {
                        logger.error("Cannot create site " + infos.get("sitetitle"), e);
                    }
                }
            }
        }

        abstract void importSiteZip() throws RepositoryException, JahiaException, IOException;

        abstract InputStream getInputStream() throws RepositoryException, FileNotFoundException;

        abstract void onImportFinish();
    }

    private class FileSiteImporter extends BaseSiteImporter {

        File file;
        boolean deleteFile;

        FileSiteImporter(String filePath, boolean deleteFile, JahiaUser user) throws RepositoryException, URISyntaxException {
            super(filePath, StringUtils.substringAfterLast(filePath, "/"), user);
            this.file = Paths.get(filePath).toFile();
            this.deleteFile = deleteFile;
            if (!file.exists()) {
                throw new RuntimeException("Unable to import file: (" + filePath + "). Because it doesn't exists");
            }
        }

        @Override
        void importSiteZip() throws RepositoryException, JahiaException, IOException {
            ImportExportBaseService.getInstance().importSiteZip(file, session);
        }

        @Override
        InputStream getInputStream() throws FileNotFoundException {
            return new FileInputStream(file);
        }

        @Override
        void onImportFinish() {
            if (deleteFile) {
                file.delete();
            }
        }
    }

    private class NodeSiteImporter extends BaseSiteImporter {

        JCRNodeWrapper node;

        NodeSiteImporter(String nodePath, JahiaUser user) throws RepositoryException, URISyntaxException {
            super(nodePath, StringUtils.substringAfterLast(nodePath, "/"), user);
            this.node = session.getNode(nodePath);
        }

        @Override
        void importSiteZip() throws RepositoryException, JahiaException, IOException {
            ImportExportBaseService.getInstance().importSiteZip(node);
        }

        @Override
        InputStream getInputStream() throws RepositoryException {
            return node.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_DATA).getBinary().getStream();
        }

        @Override
        void onImportFinish() {
            // nothing to do
        }
    }
}

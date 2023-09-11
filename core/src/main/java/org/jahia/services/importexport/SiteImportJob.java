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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSiteImportException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.rules.Service;
import org.jahia.services.importexport.validation.*;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteCreationInfo;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.zip.DirectoryZipInputStream;
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
    public static final String NO_VALIDATION_MARKER = "-novalidation.zip";

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

    /**
     * This method will try to resolve the file name from the path.
     * For Backward compatibility reason and since this code is not documented, we use the original check first
     * and if we don't get a result then we check with File.separator
     *
     * @param path  to the file or the node to resolve
     * @return      the name of the file
     */
    private String resolveFileName(String path) {
        String fileName = StringUtils.substringAfterLast(path, "/");
        if (StringUtils.isBlank(fileName)) {
            fileName = StringUtils.substringAfterLast(path, File.separator);
        }
        return fileName;
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

        public void execute() throws RepositoryException, JahiaSiteImportException {
            StringTokenizer st = new StringTokenizer(name, "_");

            String type = st.nextToken();
            if (type.equals("siteImport")) {
                try {
                    logger.info("Import site " + uri);

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
                } catch (JahiaSiteImportException e) {
                    logger.error("Site import failure, see logs for details");
                    throw e;
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
                throws IOException, RepositoryException, ServletException, JahiaException, JahiaSiteImportException {

            handleValidation(importsInfos);

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
                    //Do nothing here?
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

        private void handleValidation(List<Map<Object, Object>> importsInfos) throws IOException, JahiaSiteImportException {
            if (!noValidation()) {
                Map<Object, Object> siteInfo = getSiteInfo(importsInfos);

                if (siteInfo.isEmpty()) notifyUserOfError(siteInfo, "No site found in the import file");

                String message = validateSite(siteInfo);

                if (message != null)  notifyUserOfError(siteInfo, message);

                List<String> neededModules = neededModules(siteInfo);

                ValidationResults results = validateImport(importsInfos, neededModules, Boolean.valueOf((String) siteInfo.get("islegacyimport")));
                StringBuilder builder = new StringBuilder();
                Locale locale = SettingsBean.getInstance().getDefaultLocale();
                List<String> memo = new ArrayList<>(); //Attempt to prevent showing duplicate entries. Are there any issues with it?
                for (ValidationResult result : results.getResults()) {
                    String validationType = result.getClass().getName();
                    if (!result.isSuccessful() && !memo.contains(validationType)) {
                        memo.add(validationType);
                        if (result instanceof MissingModulesValidationResult) {
                            memo.add(MissingModulesValidationResult.class.getName());
                            importErrorMessageLineStepper(builder);
                            MissingModulesValidationResult missingModule = ((MissingModulesValidationResult) result);
                            if (missingModule.isTargetTemplateSetPresent()) {
                                builder.append(Messages.getInternalWithArguments("failure.import.missingTemplateSet", locale, missingModule.getTargetTemplateSet()));
                            }
                            if (!missingModule.getMissingModules().isEmpty()) {
                                builder.append(Messages.getInternalWithArguments("failure.import.missingModules", locale, missingModule.getMissingModules().size())).append(missingModule.getMissingModules());
                            }
                        } else if (result instanceof MissingNodetypesValidationResult) {
                            importErrorMessageLineStepper(builder);
                            builder.append(Messages.getInternalWithArguments("failure.import.missingNodetypes", locale, ((MissingNodetypesValidationResult) result).getMissingNodetypes(), ((MissingNodetypesValidationResult) result).getMissingMixins()));
                        } else if (result instanceof MissingTemplatesValidationResult) {
                            importErrorMessageLineStepper(builder);
                            MissingTemplatesValidationResult missingTemplates = ((MissingTemplatesValidationResult) result);
                            builder.append(Messages.getInternalWithArguments("failure.import.missingTemplates", locale, missingTemplates.getMissingTemplates().size()))
                                    .append(missingTemplates.getMissingTemplates().keySet());
                        } else if (result instanceof ProviderAvailabilityValidatorResult) {
                            importErrorMessageLineStepper(builder);
                            ProviderAvailabilityValidatorResult providerAvailabilityValidatorResult = ((ProviderAvailabilityValidatorResult) result);
                            builder.append(Messages.getInternalWithArguments("failure.import.unavailableProviders", locale, providerAvailabilityValidatorResult.getUnavailableProviders().size()))
                                    .append(providerAvailabilityValidatorResult.getUnavailableProviders());
                        } else if (result instanceof ConstraintsValidatorResult) {
                            ConstraintsValidatorResult constraintsValidatorResult = (ConstraintsValidatorResult) result;

                            // missing properties
                            importErrorMissingPropertiesAppender(builder, constraintsValidatorResult.getMissingMandatoryProperties(), locale);
                            importErrorMissingPropertiesAppender(builder, constraintsValidatorResult.getMissingMandatoryI18NProperties(), locale);

                            // other constraint validations
                            if (constraintsValidatorResult.getOtherConstraintViolations().size() > 0) {
                                importErrorMessageLineStepper(builder);
                                builder.append(Messages.getInternalWithArguments("failure.import.constraintViolation", locale, constraintsValidatorResult.getOtherConstraintViolations().size()))
                                        .append(constraintsValidatorResult.getOtherConstraintViolations().keySet());
                            }
                            importErrorMessageLineStepper(builder);
                        }
                    }
                }

                if (builder.length() != 0) {
                    notifyUserOfError(siteInfo, builder.toString());
                }
            }
        }

        private ValidationResults validateImport(List<Map<Object, Object>> importInfos, List<String> neededModules, Boolean isLegacyImport) throws IOException {
            ValidationResults results = new ValidationResults();

            for (Map<Object, Object> infos : importInfos) {
                File i = (File) infos.get("importFile");
                ZipEntry z;
                ZipInputStream zis2 = i.isDirectory()
                        ? new DirectoryZipInputStream(i)
                        : new NoCloseZipInputStream(new BufferedInputStream(
                        new FileInputStream(i)));

                try {
                    while ((z = zis2.getNextEntry()) != null) {
                        final String name = z.getName().replace('\\', '/');
                        if (!isLegacyImport && name.contains(REPOSITORY_XML) && !name.contains("/")) {
                            ValidationResults validationResults =
                                    ImportExportBaseService.getInstance()
                                            .validateImportFile(
                                                    JCRSessionFactory
                                                            .getInstance()
                                                            .getCurrentUserSession(),
                                                    zis2,
                                                    "application/xml",
                                                    neededModules);

                            for (ValidationResult r : validationResults.getResults()) {
                                if (!r.isSuccessful()) {
                                    results.addResult(r);
                                }
                            }
                        }

                    }
                    zis2.closeEntry();
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (zis2 instanceof NoCloseZipInputStream) {
                        ((NoCloseZipInputStream) zis2).reallyClose();
                    } else {
                        zis2.close();
                    }
                }
            }

            return results;
        }

        private String validateSite(Map<Object, Object> siteInfo) {
            Locale locale = SettingsBean.getInstance().getDefaultLocale();
            String siteTitle = (String) siteInfo.get("sitetitle");
            String siteKey = (String) siteInfo.get("sitekey");
            String serverName = (String) siteInfo.get("siteservername");
            String serverNameAliases = (String) siteInfo.get("siteservernamealiases");
            if (StringUtils.isEmpty(siteTitle) || StringUtils.isEmpty(siteKey) || StringUtils.isEmpty(serverName)) {
                return Messages.get("resources.JahiaServerSettings", "serverSettings.manageWebProjects.warningMsg.completeRequestInfo", locale);
            }

            try {
                JahiaSitesService sitesService = JahiaSitesService.getInstance();
                boolean valid = sitesService.isSiteKeyValid(siteKey);
                if (!valid) {
                    return Messages.get("resources.JahiaServerSettings", "serverSettings.manageWebProjects.warningMsg.onlyLettersDigitsUnderscore", locale);
                }
                if (sitesService.getSiteByKey(siteKey) != null) {
                    return Messages.get("resources.JahiaServerSettings", "serverSettings.manageWebProjects.siteKeyExists", locale);
                }

                Boolean isLegacy = Boolean.valueOf((String) siteInfo.get("islegacyimport"));
                if (isLegacy && (StringUtils.startsWithIgnoreCase(serverName, "http://")
                        || StringUtils.startsWithIgnoreCase(serverName, "https://"))) {
                    serverName = StringUtils.substringAfter(serverName, "://");
                }

                String serverNameValidation = validateServerName(serverName, locale);

                if (StringUtils.isNotEmpty(serverNameAliases)) {
                    for (String serverNameAlias : StringUtils.split(serverNameAliases, ", ")) {
                        serverNameValidation = validateServerName(serverNameAlias, locale);
                        if (serverNameValidation != null) return serverNameValidation;
                    }
                }

                return serverNameValidation;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }

            return null;
        }

        private String validateServerName(String serverNameToValidate, Locale locale) throws JahiaException {
            JahiaSitesService sitesService = JahiaSitesService.getInstance();
            if (!sitesService.isServerNameValid(serverNameToValidate)) {
                return Messages.get("resources.JahiaServerSettings", "serverSettings.manageWebProjects.warningMsg.invalidServerName", locale);
            } else if (!Url.isLocalhost(serverNameToValidate)) {
                JahiaSite existingSite = sitesService.getSiteByServerName(serverNameToValidate);
                if (existingSite != null) {
                    return Messages.get("resources.JahiaServerSettings", "serverSettings.manageWebProjects.warningMsg.chooseAnotherServerName", locale);
                }
            }
            return null;
        }

        private List<String> neededModules(Map<Object, Object> siteInfo) {
            List<String> neededModules = new ArrayList<>();
            String template = "";

            template = (String) siteInfo.get("templates");

            for (Map.Entry<Object, Object> entry : siteInfo.entrySet()) {
                if (entry.getKey().toString().contains("installedModules")) {
                    neededModules.add((String) entry.getValue());
                }
            }

            //Make templates set first entry
            neededModules.remove(template);
            neededModules.add(0, template);
            return neededModules;
        }

        private Map<Object, Object> getSiteInfo(List<Map<Object, Object>> importsInfos) {
            for (Map<Object, Object> ii : importsInfos) {
                if (ii.get("type").equals("site")) return ii;
            }
            return Collections.emptyMap();
        }

        private void importErrorMessageLineStepper(StringBuilder errorMessage) {
            errorMessage.append(errorMessage.length() == 0 ? "\n" : "\n\n");
        }

        private void importErrorMissingPropertiesAppender(StringBuilder errorMessage, Map<String, Set<String>> missingProperties, Locale uiLocale) {
            for (Map.Entry<String, Set<String>> missingPropertiesEntry : missingProperties.entrySet()) {
                for (String missingProperty : missingPropertiesEntry.getValue()) {
                    importErrorMessageLineStepper(errorMessage);
                    errorMessage.append(Messages.getInternalWithArguments("failure.import.missingProperty", uiLocale, missingProperty, missingPropertiesEntry.getKey()));
                }
            }
        }

        private void notifyUserOfError(Map<Object, Object> siteInfo, String message) throws JahiaSiteImportException {
            ValidationNotificationHandler.notifyAdministratorWhenValidationFailed(siteInfo, message);
            throw new JahiaSiteImportException(message);
        }

        abstract void importSiteZip() throws RepositoryException, JahiaException, IOException;

        abstract InputStream getInputStream() throws RepositoryException, FileNotFoundException;

        abstract void onImportFinish();

        abstract boolean noValidation();
    }

    private class FileSiteImporter extends BaseSiteImporter {

        File file;
        boolean deleteFile;

        FileSiteImporter(String filePath, boolean deleteFile, JahiaUser user) throws RepositoryException, URISyntaxException {
            super(filePath, resolveFileName(filePath), user);
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

        @Override
        boolean noValidation() {
            return file.getName().contains(NO_VALIDATION_MARKER);
        }
    }

    private class NodeSiteImporter extends BaseSiteImporter {

        JCRNodeWrapper node;

        NodeSiteImporter(String nodePath, JahiaUser user) throws RepositoryException, URISyntaxException {
            super(nodePath,  resolveFileName(nodePath), user);
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

        @Override
        boolean noValidation() {
            return false;
        }
    }
}

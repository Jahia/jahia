package org.jahia.modules.serversettings.flow;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.users.admin.AdminProperties;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.importexport.SiteImportDefaults;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.search.spell.CompositeSpellChecker;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.execution.RequestContext;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handle creation of webprojects in webflow
 */
public class WebprojectHandler implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(WebprojectHandler.class);

    private static final HashSet<String> NON_SITE_IMPORTS = new HashSet<String>(Arrays.asList("serverPermissions.xml",
            "users.xml", "users.zip", JahiaSitesBaseService.SYSTEM_SITE_KEY + ".zip", "references.zip", "roles.zip"));
    private static final Pattern LANGUAGE_RANK_PATTERN = Pattern.compile("(?:language.)(\\w+)(?:.rank)");
    private static final Map<String, Integer> RANK;

    static {
        RANK = new HashMap<String, Integer>(3);
        RANK.put("roles.xml", 5);
        RANK.put("roles.zip", 5);
        RANK.put("users.zip", 10);
        RANK.put("users.xml", 10);
        RANK.put("serverPermissions.xml", 20);
        RANK.put("shared.zip", 30);
        RANK.put(JahiaSitesBaseService.SYSTEM_SITE_KEY + ".zip", 40);
    }

    private List<JahiaSite> sites;

    private List<File> prepackagedSites;

    private String selectedPrepackagedSite;

    private MultipartFile importFile;
    private String importPath;
    private boolean validityCheckOnImport = true;

    @Autowired
    private transient JahiaSitesBaseService sitesService;

    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    @Autowired
    private transient ImportExportBaseService importExportBaseService;

    private Properties importProperties;
    private Map<String, ImportInfo> importsInfos;
    private String cleanupXsl;

    public WebprojectHandler() {
        File[] files = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites").listFiles();
        if (files != null) {
            prepackagedSites = Arrays.asList(files);
        }
    }

    public void setSitesService(JahiaSitesBaseService sitesService) {
        this.sitesService = sitesService;
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public SiteBean getNewSite() {
        return new SiteBean();
    }

    public List<JahiaSite> getSites() {
        return sites;
    }

    public void setSites(List<String> sites) {
        this.sites = new ArrayList<JahiaSite>();
        if (sites != null) {
            for (String site : sites) {
                try {
                    this.sites.add(sitesService.getSiteByKey(site));
                } catch (JahiaException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
    }

    public List<File> getPrepackagedSites() {
        return prepackagedSites;
    }

    public String getSelectedPrepackagedSite() {
        return selectedPrepackagedSite;
    }

    public void setSelectedPrepackagedSite(String selectedPrepackagedSite) {
        this.selectedPrepackagedSite = selectedPrepackagedSite;
    }

    public MultipartFile getImportFile() {
        return importFile;
    }

    public void setImportFile(MultipartFile importFile) {
        this.importFile = importFile;
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    public Map<String, ImportInfo> getImportsInfos() {
        return importsInfos;
    }

    public void setImportsInfos(Map<String, ImportInfo> importsInfos) {
        this.importsInfos = importsInfos;
    }

    public void createSite(SiteBean bean) {

        try {
            JahiaSite site = sitesService.addSite(JCRSessionFactory.getInstance().getCurrentUser(), bean.getTitle(),
                    bean.getServerName(), bean.getSiteKey(), bean.getDescription(),
                    LanguageCodeConverters.getLocaleFromCode(bean.getLanguage()), bean.getTemplateSet(),
                    bean.getModules().toArray(new String[bean.getModules().size()]), null, null, null, null, null,
                    null);

            // set as default site
            if (bean.isDefaultSite()) {
                sitesService.setDefaultSite(site);
            }

            if (bean.createAdmin) {
                AdminProperties admin = bean.getAdminProperties();
                JahiaUser adminSiteUser = userManagerService.createUser(admin.getUserName(), admin.getPassword(),
                        admin.getUserProperties());
                groupManagerService.getAdministratorGroup(site.getSiteKey()).addMember(adminSiteUser);
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(),e);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
        }

    }

    public void deleteSites() {
        if (sites != null) {
            JahiaSite defSite = sitesService.getDefaultSite();

            for (JahiaSite site : sites) {
                try {
                    sitesService.removeSite(site);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(),e);
                }
            }

            if (sites.contains(defSite)) {
                try {
                    Iterator<JahiaSite> siteIterator = sitesService.getSites();
                    if (siteIterator.hasNext()) {
                        sitesService.setDefaultSite(siteIterator.next());
                    } else {
                        sitesService.setDefaultSite(null);
                    }
                } catch (JahiaException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }

    }

    public void exportSites(RequestContext requestContext) {
        /*HttpServletResponse response = (HttpServletResponse) requestContext.getExternalContext().getNativeResponse();
        HttpServletRequest request = (HttpServletRequest) requestContext.getExternalContext().getNativeRequest();
        response.reset();
        response.setContentType("application/zip");
        //make sure this file is not cached by the client (or a proxy middleman)
        WebUtils.setNoCacheHeaders(response);
        Map<String,Object> params = new LinkedHashMap<String, Object>();
        params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
        params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
        params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
        params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
        if (request.getParameter("live") == null || Boolean.valueOf(request.getParameter("live"))) {
            params.put(ImportExportService.INCLUDE_LIVE_EXPORT, Boolean.TRUE);
        }
//                    if (request.getParameter("users") == null || Boolean.valueOf(request.getParameter("users"))) {
        params.put(ImportExportService.INCLUDE_USERS, Boolean.TRUE);
//                    }
        params.put(ImportExportService.INCLUDE_ROLES, Boolean.TRUE);
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
        params.put(ImportExportService.XSL_PATH, cleanupXsl);

        try {
            OutputStream outputStream = response.getOutputStream();
            importExportBaseService.exportSites(outputStream, params, sites);
            outputStream.close();
            requestContext.getExternalContext().recordResponseComplete();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (TransformerException e) {
            logger.error(e.getMessage(), e);
        }*/
    }

    public void prepareImport(MessageContext messageContext) {
        if (!StringUtils.isEmpty(importPath)) {
            File f = new File(importPath);

            if (f.exists()) {
                prepareFileImports(f, f.getName(), messageContext);
            }
        } else if (!importFile.isEmpty()) {
            File file = null;
            try {
                file = File.createTempFile(importFile.getOriginalFilename(), ".tmp");
                importFile.transferTo(file);
                prepareFileImports(file, importFile.getName(), messageContext);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } finally {
                FileUtils.deleteQuietly(file);
            }
        }
    }

    public void preparePrepackageImport(MessageContext messageContext) {
        if (!StringUtils.isEmpty(selectedPrepackagedSite)) {
            File f = new File(selectedPrepackagedSite);

            if (f.exists()) {
                prepareFileImports(f, f.getName(), messageContext);
            }
        }
    }

    private static final Comparator<ImportInfo> IMPORTS_COMPARATOR = new Comparator<ImportInfo>() {
        public int compare(ImportInfo o1, ImportInfo o2) {
            Integer rank1 = RANK.get(o1.getImportFileName());
            Integer rank2 = RANK.get(o2.getImportFileName());
            rank1 = rank1 != null ? rank1 : 100;
            rank2 = rank2 != null ? rank2 : 100;
            return rank1.compareTo(rank2);
        }
    };

    private void prepareFileImports(File f, String name, MessageContext messageContext) {
        if (f != null && f.exists()) {
            ZipInputStream zis = null;
            try {
                importProperties = new Properties();
                zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
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
                    if (n.equals("export.properties")) {
                        InputStream is = new BufferedInputStream(new FileInputStream(i));
                        try {
                            importProperties.load(is);
                        } finally {
                            IOUtils.closeQuietly(is);
                            FileUtils.deleteQuietly(i);
                        }
                    } else if (n.equals("classes.jar")) {
                        FileUtils.deleteQuietly(i);
                    } else if (n.equals("site.properties") || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                        // this is a single site import, stop everything and import
                        FileUtils.deleteQuietly(i);
                        for (File file : imports.keySet()) {
                            FileUtils.deleteQuietly(file);
                        }
                        imports.clear();
                        importList.clear();
                        File tempFile = File.createTempFile("import", ".zip");
                        FileUtils.copyFile(f, tempFile);
                        imports.put(tempFile, name);
                        importList.add(tempFile);
                        break;
                    } else {
                        imports.put(i, n);
                        importList.add(i);
                    }
                }

                this.importsInfos = new LinkedHashMap<String, ImportInfo>();
                List<ImportInfo> importsInfosList = new ArrayList<ImportInfo>();
                for (File i : importList) {
                    ImportInfo value = prepareSiteImport(i, imports.get(i), messageContext);
                    if (value != null) {
                        if (value.isLegacyImport()) {
                            final String defaultMappingsFolderPath = JahiaContextLoaderListener.getServletContext().getRealPath(
                                    "/WEB-INF/var/legacyMappings");
                            final File defaultMappingsFolder = defaultMappingsFolderPath != null ? new File(
                                    defaultMappingsFolderPath) : null;
                            Collection<File> legacyMappings = null;
                            Collection<File> legacyDefinitions = null;
                            if (defaultMappingsFolder != null && defaultMappingsFolder.exists()) {
                                try {
                                    legacyMappings = FileUtils.listFiles(defaultMappingsFolder, new String[]{"map"},
                                            false);
                                } catch (Exception e) {
                                    logger.debug("Legacy mappings not found", e);
                                }
                                try {
                                    legacyDefinitions = FileUtils.listFiles(defaultMappingsFolder, new String[]{"cnd"},
                                            false);
                                } catch (Exception e) {
                                    logger.debug("Legacy definitions not found", e);
                                }
                            }

                            Resource[] modulesLegacyMappings = SpringContextSingleton.getInstance().getResources(
                                    "/modules/**/META-INF/legacyMappings/*.map");
                            if (legacyMappings == null && modulesLegacyMappings.length > 0) {
                                legacyMappings = new ArrayList<File>();
                            }
                            for (Resource modulesLegacyMapping : modulesLegacyMappings) {
                                legacyMappings.add(modulesLegacyMapping.getFile());
                            }

                            Resource[] modulesLegacyDefinitions = SpringContextSingleton.getInstance().getResources(
                                    "/modules/**/META-INF/legacyMappings/*.cnd");
                            if (legacyDefinitions == null && modulesLegacyDefinitions.length > 0) {
                                legacyDefinitions = new ArrayList<File>();
                            }
                            for (Resource modulesLegacyDefinition : modulesLegacyDefinitions) {
                                legacyDefinitions.add(modulesLegacyDefinition.getFile());
                            }

                            if (legacyMappings != null && !legacyMappings.isEmpty()) {
                                value.setLegacyMappings(legacyMappings);
                            }
                            if (legacyDefinitions != null && !legacyDefinitions.isEmpty()) {
                                value.setLegacyDefinitions(legacyDefinitions);
                            }
                        }
                        importsInfosList.add(value);
                    }
                }

                Collections.sort(importsInfosList, IMPORTS_COMPARATOR);

                for (ImportInfo info : importsInfosList) {
                    importsInfos.put(info.getImportFileName(), info);
                }

            } catch (IOException e) {
                logger.error("Cannot read import file :" + e.getMessage());
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
    }


    private ImportInfo prepareSiteImport(File i, String filename, MessageContext messageContext) throws IOException {
        ImportInfo importInfos = new ImportInfo();
        importInfos.setImportFile(i);
        importInfos.setImportFileName(filename);
        importInfos.setSelected(Boolean.TRUE);
        if (importProperties != null) {
            importInfos.setOriginatingJahiaRelease(importProperties.getProperty("JahiaRelease"));
        }
        if (filename.endsWith(".xml")) {
            importInfos.setType("xml");
        } else if (filename.endsWith("systemsite.zip")) {
            importInfos.setType("files");
        } else {
            List<String> installedModules = readInstalledModules(i);
            org.jahia.utils.zip.ZipEntry z;
            NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(i)));

            boolean isSite = false;
            boolean isLegacySite = false;
            try {
                while ((z = zis2.getNextEntry()) != null) {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        importInfos.loadSiteProperties(p);

                        importInfos.setTemplates("");
                        if (p.containsKey("templatePackageName")) {
                            JahiaTemplateManagerService templateManager = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
                            JahiaTemplatesPackage pack = templateManager.getTemplatePackageByFileName((String) p.get(
                                    "templatePackageName"));
                            if (pack == null) {
                                pack = templateManager.getTemplatePackage((String) p.get("templatePackageName"));
                            }
                            if (pack != null) {
                                importInfos.setTemplates(pack.getRootFolder());
                            }
                        }
                        importInfos.setOldSiteKey(importInfos.getSiteKey());
                        isSite = true;
                    } else if (z.getName().startsWith("export_")) {
                        isLegacySite = true;
                    } else if (validityCheckOnImport && z.getName().contains("repository.xml")) {
                        try {
                            long timer = System.currentTimeMillis();
                            ValidationResults validationResults = ImportExportBaseService.getInstance().validateImportFile(
                                    JCRSessionFactory.getInstance().getCurrentUserSession(), zis2, "application/xml",
                                    installedModules);
                            if (!validationResults.isSuccessful()) {
                                logger.error("Failed Import {}/{} validated in {} ms: {}",
                                        new String[]{filename, z.getName(), String.valueOf(
                                                (System.currentTimeMillis() - timer)), validationResults.toString()});
                            } else {
                                logger.info("Successful Import {}/{} validated in {} ms: {}",
                                        new String[]{filename, z.getName(), String.valueOf(
                                                (System.currentTimeMillis() - timer)), validationResults.toString()});
                            }
                            if (!validationResults.isSuccessful()) {
                                if (importInfos.getValidationResult()!=null) {
                                    // merge results
                                    importInfos.setValidationResult(importInfos.getValidationResult().merge(
                                            validationResults));
                                } else {
                                    importInfos.setValidationResult(validationResults);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error when validating import file", e);
                        }
                    }
                    zis2.closeEntry();
                }
            } finally {
                zis2.reallyClose();
            }
            importInfos.setSite(isSite);
            // todo import ga parameters
            if (isSite || isLegacySite) {
                importInfos.setType("site");
                if (importInfos.getSiteKey()==null) {
                    importInfos.setSiteKey("");
                    importInfos.setSiteServername("");
                    importInfos.setSiteTitle("");
                    importInfos.setDescription("");
                    importInfos.setMixLanguage(Boolean.FALSE);
                    importInfos.setTemplates("");
                } else {
                    validateSite(messageContext, importInfos);
                }
                importInfos.setLegacyImport(isLegacySite);
            } else {
                importInfos.setType("files");
            }

        }
        return importInfos;
    }

    private List<String> readInstalledModules(File i) throws IOException {
        List<String> modules = new LinkedList<String>();
        org.jahia.utils.zip.ZipEntry z;
        NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(i)));

        try {
            while ((z = zis2.getNextEntry()) != null) {
                try {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        Map<Integer, String> im = new TreeMap<Integer, String>();
                        for (Object k : p.keySet()) {
                            String key = String.valueOf(k);
                            if (key.startsWith("installedModules.")) {
                                try {
                                    im.put(Integer.valueOf(StringUtils.substringAfter(key, ".")), p.getProperty(key));
                                } catch (NumberFormatException e) {
                                    logger.warn("Unable to parse installed module from key {}", key);
                                }
                            }
                        }
                        modules.addAll(im.values());
                    }
                } finally {
                    zis2.closeEntry();
                }
            }
        } finally {
            zis2.reallyClose();
        }
        return modules;
    }

    public void validateDisplayImportContent(ValidationContext context) {
        context.getMessageContext();

        for (ImportInfo infos : importsInfos.values()) {
            File file = (File) infos.getImportFile();
            if (infos.isSelected()) {
                if (NON_SITE_IMPORTS.contains(infos.getImportFileName())) {

                } else {
                    validateSite(context.getMessageContext(), infos);
                }
            }
        }

    }

    private void validateSite(MessageContext messageContext, ImportInfo infos) {
        try {
            infos.setSiteTitleInvalid(StringUtils.isEmpty((String) infos.getSiteTitle()));

            String siteKey = (String) infos.getSiteKey();
            boolean valid = sitesService.isSiteKeyValid(siteKey);
            if (!valid) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                        Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.invalidSiteKey",
                                LocaleContextHolder.getLocale())).build());
            }
            if (valid && sitesService.getSiteByKey(siteKey) != null) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                        Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.siteKeyExists",
                                LocaleContextHolder.getLocale())).build());
            }

            String serverName = (String) infos.getSiteServername();
            valid = sitesService.isServerNameValid(serverName);
            if (!valid) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                        Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.invalidServerName",
                                LocaleContextHolder.getLocale())).build());
            }

            if (valid && !Url.isLocalhost(serverName) && sitesService.getSite(serverName) != null) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                        Messages.get("resources.JahiaServerSettings","serverSettings.manageWebProjects.serverNameExists",
                                LocaleContextHolder.getLocale())).build());
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(),e);
        }
    }

    public boolean processImport(final JahiaUser user, MessageContext context) {
        logger.info("Processing Import");

        boolean doImportServerPermissions = false;
        for (ImportInfo infos : importsInfos.values()) {
            File file = infos.getImportFile();
            if (infos.isSelected()
                && infos.getImportFileName().equals("serverPermissions.xml")) {
                doImportServerPermissions = true;
                break;
            }
        }

        for (ImportInfo infos : importsInfos.values()) {
            File file = infos.getImportFile();
            if (infos.isSelected() &&
                infos.getImportFileName().equals("users.xml")) {
                try {
                    importExportBaseService.importUsers(file);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                   FileUtils.deleteQuietly(file);
                }
                break;
            }
        }

        try {
            for (final ImportInfo infos : importsInfos.values()) {
                final File file = infos.getImportFile();
                if (infos.isSelected()) {
                    String type = infos.getType();
                    if (type.equals("files")) {
                        try {
                            JahiaSite system = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);

                            Map<String,String> pathMapping = JCRSessionFactory.getInstance().getCurrentUserSession().getPathMapping();
                            pathMapping.put("/shared/files/", "/sites/" + system.getSiteKey() + "/files/");
                            pathMapping.put("/shared/mashups/", "/sites/" + system.getSiteKey() + "/portlets/");

                            importExportBaseService.importSiteZip(file == null ? null : new FileSystemResource(file),
                                    system, infos.asMap());
                        } catch (Exception e) {
                            logger.error("Error when getting templates", e);
                        }
                    } else if (type.equals("xml") &&
                               (infos.getImportFileName().equals("serverPermissions.xml") ||
                                infos.getImportFileName().equals("users.xml"))) {

                    } else if (type.equals("site")) {
                        // site import
                        String tpl = infos.getTemplates();
                        if ("".equals(tpl)) {
                            tpl = null;
                        }
                        String legacyImportFilePath = null;
                        String legacyDefinitionsFilePath = null;
                        if(infos.isLegacyImport()) {
                            legacyImportFilePath = infos.getSelectedLegacyMapping();
                            if (legacyImportFilePath != null && "".equals(legacyImportFilePath.trim())){
                                legacyImportFilePath = null;
                            }
                            legacyDefinitionsFilePath = infos.getSelectedLegacyDefinitions();
                            if (legacyDefinitionsFilePath != null && "".equals(legacyDefinitionsFilePath.trim())){
                                legacyDefinitionsFilePath = null;
                            }
                        }
                        final Locale defaultLocale = determineDefaultLocale(LocaleContextHolder.getLocale(), infos);
                        try {
                            try {
                                final String finalTpl = tpl;
                                final String finalLegacyImportFilePath = legacyImportFilePath;
                                final String finalLegacyDefinitionsFilePath = legacyDefinitionsFilePath;

                                final boolean finalDoImportServerPermissions = doImportServerPermissions;
                                JCRObservationManager.doWithOperationType(null, JCRObservationManager.IMPORT,
                                        new JCRCallback<Object>() {
                                            public Object doInJCR(JCRSessionWrapper jcrSession)
                                                    throws RepositoryException {
                                                try {
                                                    sitesService.addSite(user, infos.getSiteTitle(),
                                                            infos.getSiteServername(), infos.getSiteKey(), "",
                                                            defaultLocale, finalTpl, null, "fileImport",
                                                            file == null ? null : new FileSystemResource(file),
                                                            infos.getImportFileName(), false,
                                                            finalDoImportServerPermissions,
                                                            infos.getOriginatingJahiaRelease(),
                                                            finalLegacyImportFilePath, finalLegacyDefinitionsFilePath);
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
                            logger.error("Cannot create site " + infos.getSiteTitle(), e);
                            context.addMessage(new MessageBuilder().error().defaultText("Cannot create site " + infos.getSiteTitle()+".<br/>"+e.getMessage()).build());
                            return false;
                        }
                    }
                }
            }
        } finally {
            for (ImportInfo infos : importsInfos.values()) {
                FileUtils.deleteQuietly(infos.getImportFile());
            }
        }

        CompositeSpellChecker.updateSpellCheckerIndex();

        return true;
    }

    private Locale determineDefaultLocale(Locale defaultLocale, ImportInfo infos) {
        SortedMap<Integer, String> activeLanguageCodesByRank = new TreeMap<Integer, String>();
        Map<Object, Object> map = infos.asMap();
        for (Map.Entry<Object, Object> info : map.entrySet()) {
            if (info.getKey() instanceof String) {
                Matcher m = LANGUAGE_RANK_PATTERN.matcher((String) info.getKey());
                if (m.find()) {
                    String languageCode = m.group(1);
                    boolean activated =
                            Boolean.parseBoolean((String) map.get("language." + languageCode + ".activated"));

                    if (activated) {
                        if ("1".equals(info.getValue())) {
                            return LanguageCodeConverters.languageCodeToLocale(languageCode);
                        } else {
                            activeLanguageCodesByRank.put(new Integer((String) info.getValue()), languageCode);
                        }
                    }
                }
            }
        }
        if (!activeLanguageCodesByRank.isEmpty()) {
            defaultLocale = LanguageCodeConverters
                    .languageCodeToLocale(activeLanguageCodesByRank.get(activeLanguageCodesByRank.firstKey()));
        }
        return defaultLocale;
    }

    public class SiteBean implements Serializable {
        private String siteKey = "mySite";
        private String title = "My Site";
        private String serverName = "localhost";
        private String description;

        private boolean defaultSite = false;

        private boolean createAdmin = false;
        private AdminProperties adminProperties;

        private String templateSet;
        private List<String> modules = new ArrayList<String>();
        private String language;

        public String getSiteKey() {
            return siteKey;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getServerName() {
            return serverName;
        }

        public void setServerName(String serverName) {
            this.serverName = serverName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isDefaultSite() {
            return defaultSite;
        }

        public void setDefaultSite(boolean defaultSite) {
            this.defaultSite = defaultSite;
        }

        public boolean isCreateAdmin() {
            return createAdmin;
        }

        public void setCreateAdmin(boolean createAdmin) {
            this.createAdmin = createAdmin;
        }

        public AdminProperties getAdminProperties() {
            if (adminProperties == null) {
                adminProperties = new AdminProperties(siteKey + "-admin");
            }
            return adminProperties;
        }

        public void setAdminProperties(AdminProperties adminProperties) {
            this.adminProperties = adminProperties;
        }

        public String getTemplateSet() {
            return templateSet;
        }

        public void setTemplateSet(String templateSet) {
            this.templateSet = templateSet;
        }

        public List<String> getModules() {
            return modules;
        }

        public void setModules(List<String> modules) {
            if (modules == null) {
                this.modules = new ArrayList<String>();
            } else {
                this.modules = modules;
            }
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public void validateCreateSite(ValidationContext context) {
            // check validity...
            String title = (String) context.getUserValue("title");
            String serverName = (String) context.getUserValue("serverName");
            String siteKey = (String) context.getUserValue("siteKey");

            MessageContext messages = context.getMessageContext();

            try {
                if (title != null && (title.length() > 0) && serverName != null &&
                    (serverName.length() > 0) && siteKey != null && (siteKey.length() > 0)) {
                    if (!sitesService.isSiteKeyValid(siteKey)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label",
                                        LocaleContextHolder.getLocale())).build());
                    } else if (siteKey.equals("site")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label",
                                        LocaleContextHolder.getLocale())).build());
                    } else if (!sitesService.isServerNameValid(serverName)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.invalidServerName.label",
                                        LocaleContextHolder.getLocale())).build());
                    } else if (serverName.equals("default")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                                        LocaleContextHolder.getLocale())).build());
                    } else if (!Url.isLocalhost(serverName) && sitesService.getSite(serverName) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                                        LocaleContextHolder.getLocale())).build());
                    } else if (sitesService.getSiteByKey(siteKey) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(
                                Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label",
                                        LocaleContextHolder.getLocale())).build());
                    }
                } else {
                    messages.addMessage(new MessageBuilder().error().source("siteKey").defaultText(Messages.getInternal(
                            "org.jahia.admin.warningMsg.completeRequestInfo.label",
                            LocaleContextHolder.getLocale())).build());
                }
            } catch (JahiaException e) {
                e.printStackTrace();
            }
        }

        public void validateCreateSiteSelectModules(ValidationContext context) {
        }
    }

    public class ImportInfo implements Serializable {

        private File fileToBeImported;
        private boolean selected;
        private String importFileName;
        private String siteTitle;
        private boolean siteTitleInvalid;
        private String siteKey;
        private String siteServername;
        private String originatingJahiaRelease;
        private Collection<File> legacyMappings;
        private boolean legacyImport;
        private Collection<File> legacyDefinitions;
        private String type;
        private Properties siteProperties;
        private String templates;
        private String oldSiteKey;
        private String description;
        private String templatePackageName;
        private Boolean mixLanguage;
        private String defaultLanguage;
        private Boolean defaultSite;
        private ValidationResults validationResult;
        private boolean site;
        private String selectedLegacyMapping;
        private String selectedLegacyDefinitions;

        public File getImportFile() {
            return fileToBeImported;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getImportFileName() {
            return importFileName;
        }

        public String getSiteTitle() {
            return siteTitle;
        }

        public void setSiteTitleInvalid(boolean invalid) {
            siteTitleInvalid = invalid;
        }

        public String getSiteKey() {
            return siteKey;
        }

        public String getSiteServername() {
            return siteServername;
        }

        public void setImportFile(File fileToBeImported) {
            this.fileToBeImported = fileToBeImported;
        }

        public void setImportFileName(String importFileName) {
            this.importFileName = importFileName;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void setSiteKey(String siteKey) {
            this.siteKey = siteKey;
        }

        public void setSiteServername(String siteServername) {
            this.siteServername = siteServername;
        }

        public void setSiteTitle(String siteTitle) {
            this.siteTitle = siteTitle;
        }

        public boolean isSiteTitleInvalid() {
            return siteTitleInvalid;
        }

        public void setOriginatingJahiaRelease(String originatingJahiaRelease) {
            this.originatingJahiaRelease = originatingJahiaRelease;
        }

        public void setLegacyMappings(Collection<File> legacyMappings) {
            this.legacyMappings = legacyMappings;
        }

        public boolean isLegacyImport() {
            return legacyImport;
        }

        public void setLegacyDefinitions(Collection<File> legacyDefinitions) {
            this.legacyDefinitions = legacyDefinitions;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void loadSiteProperties(Properties siteProperties) {
            this.siteProperties = siteProperties;
            siteKey = siteProperties.getProperty("sitekey");
            siteTitle = siteProperties.getProperty("sitetitle");
            siteServername = siteProperties.getProperty("siteservername");
            description = siteProperties.getProperty("description");
            templatePackageName=siteProperties.getProperty("templatePackageName");
            mixLanguage=Boolean.valueOf(siteProperties.getProperty("mixLanguage", "false"));
            defaultLanguage=siteProperties.getProperty("defaultLanguage");            
            defaultSite=Boolean.valueOf(siteProperties.getProperty("defaultSite", "false"));
        }

        public Collection<File> getLegacyDefinitions() {
            return legacyDefinitions;
        }

        public Collection<File> getLegacyMappings() {
            return legacyMappings;
        }

        public String getOriginatingJahiaRelease() {
            return originatingJahiaRelease;
        }

        public Properties getSiteProperties() {
            return siteProperties;
        }

        public String getType() {
            return type;
        }

        public void setLegacyImport(boolean legacyImport) {
            this.legacyImport = legacyImport;
            if(legacyImport) {
                Map<String, SiteImportDefaults> siteImportDefaultsMap = SpringContextSingleton.getInstance().getContext().getBeansOfType(
                        SiteImportDefaults.class);
                if (siteImportDefaultsMap != null && siteImportDefaultsMap.size() > 0) {
                    if (siteImportDefaultsMap.size() > 1) {
                        logger.error(
                                "Found several beans of type org.jahia.services.importexport.SiteImportDefaults whereas only one is allowed, skipping");
                    } else {
                        SiteImportDefaults siteImportDefaults = siteImportDefaultsMap.values().iterator().next();
                        templates = siteImportDefaults.getDefaultTemplateSet(siteKey);
                        selectedLegacyDefinitions = siteImportDefaults.getDefaultSourceDefinitionsFile(siteKey);
                        selectedLegacyMapping = siteImportDefaults.getDefaultMappingFile(siteKey);
                    }
                }
            }
        }

        public void setTemplates(String templates) {
            this.templates = templates;
        }

        public String getTemplates() {
            return templates;
        }

        public void setOldSiteKey(String oldSiteKey) {
            this.oldSiteKey = oldSiteKey;
        }

        public String getOldSiteKey() {
            return oldSiteKey;
        }

        public void setValidationResult(ValidationResults validationResult) {
            this.validationResult = validationResult;
        }

        public void setDefaultLanguage(String defaultLanguage) {
            this.defaultLanguage = defaultLanguage;
        }

        public void setDefaultSite(Boolean defaultSite) {
            this.defaultSite = defaultSite;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMixLanguage(Boolean mixLanguage) {
            this.mixLanguage = mixLanguage;
        }

        public void setSiteProperties(Properties siteProperties) {
            this.siteProperties = siteProperties;
        }

        public void setTemplatePackageName(String templatePackageName) {
            this.templatePackageName = templatePackageName;
        }

        public String getDefaultLanguage() {
            return defaultLanguage;
        }

        public Boolean getDefaultSite() {
            return defaultSite;
        }

        public String getDescription() {
            return description;
        }

        public Boolean getMixLanguage() {
            return mixLanguage;
        }

        public String getTemplatePackageName() {
            return templatePackageName;
        }

        public ValidationResults getValidationResult() {
            return validationResult;
        }

        public boolean isSite() {
            return site;
        }

        public void setSite(boolean site) {
            this.site = site;
        }

        public Map<Object, Object> asMap() {
            Map<Object,Object> map = new LinkedHashMap<Object, Object>();
            if(siteProperties!=null) {
                map.putAll(siteProperties);
                map.put("sitekey",siteKey);
                map.put("sitetitle",siteTitle);
                map.put("siteservername",siteServername);
                map.put("templates",templates);
            }
            return map;
        }

        public String getSelectedLegacyMapping() {
            return selectedLegacyMapping;
        }

        public String getSelectedLegacyDefinitions() {
            return selectedLegacyDefinitions;
        }

        public void setSelectedLegacyDefinitions(String selectedLegacyDefinitions) {
            this.selectedLegacyDefinitions = selectedLegacyDefinitions;
        }

        public void setSelectedLegacyMapping(String selectedLegacyMapping) {
            this.selectedLegacyMapping = selectedLegacyMapping;
        }
    }
}

package org.jahia.modules.serversettings.flow;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.adminproperties.AdminProperties;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.validation.ValidationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handle creation of webprojects in webflow
 */
public class WebprojectHandler implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(WebprojectHandler.class);

    private static final HashSet<String> NON_SITE_IMPORTS = new HashSet<String>(Arrays.asList("serverPermissions.xml", "users.xml", "users.zip", JahiaSitesBaseService.SYSTEM_SITE_KEY + ".zip", "references.zip", "roles.zip"));

    private static final Map<String, Integer> RANK;
    static {
        RANK = new HashMap<String, Integer>(3);
        RANK.put("roles.xml", 5);
        RANK.put("roles.zip", 5);
        RANK.put("users.zip", 10);
        RANK.put("users.xml", 10);
        RANK.put("serverPermissions.xml", 20);
        RANK.put("shared.zip", 30);
        RANK.put(JahiaSitesBaseService.SYSTEM_SITE_KEY+".zip", 40);
    }

    private List<JahiaSite> sites;

    private List<File> prepackagedSites;

    private String selectedPrepackagedSite;

    private String importFile;
    private String importPath;
    private boolean validityCheckOnImport = true;

    @Autowired
    private transient JahiaSitesBaseService sitesService;

    @Autowired
    private transient JahiaUserManagerService userManagerService;

    @Autowired
    private transient JahiaGroupManagerService groupManagerService;

    private Properties importProperties;
    private Map<String,Map<Object,Object>> importsInfos;

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
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    public String getImportFile() {
        return importFile;
    }

    public void setImportFile(String importFile) {
        this.importFile = importFile;
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    public Map<String,Map<Object, Object>> getImportsInfos() {
        return importsInfos;
    }

    public void createSite(SiteBean bean) {

        try {
            JahiaSite site = sitesService.addSite(JCRSessionFactory.getInstance().getCurrentUser(), bean.getTitle(), bean.getServerName(), bean.getSiteKey(), bean.getDescription(), LanguageCodeConverters.getLocaleFromCode(bean.getLanguage()),
                    bean.getTemplateSet(), bean.getModules().toArray(new String[bean.getModules().size()]), null, null, null, null, null, null);

            // set as default site
            if (bean.isDefaultSite()) {
                sitesService.setDefaultSite(site);
            }

            if (bean.createAdmin) {
                AdminProperties admin = bean.getAdminProperties();
                JahiaUser adminSiteUser = userManagerService.createUser(admin.getUserName(), admin.getPassword(), admin.getUserProperties());
                groupManagerService.getAdministratorGroup(site.getSiteKey()).addMember(adminSiteUser);
            }
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void deleteSites() {
        if (sites != null) {
            JahiaSite defSite = sitesService.getDefaultSite();

            for (JahiaSite site : sites) {
                try {
                    sitesService.removeSite(site);
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            if (sites.contains(defSite.getSiteKey())) {
                try {
                    Iterator<JahiaSite> siteIterator = sitesService.getSites();
                    if (siteIterator.hasNext()) {
                        sitesService.setDefaultSite(siteIterator.next());
                    } else {
                        sitesService.setDefaultSite(null);
                    }
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }

    public void exportSites() {
        System.out.println("Export "+sites);

    }

    public void prepareImport(MessageContext messageContext) {
        if (StringUtils.isEmpty(importFile) && !StringUtils.isEmpty(importPath)) {
            File f = new File(importPath);

            if (f.exists()) {
                prepareFileImports(f, f.getName(), messageContext);
            }
        }
    }

    private static final Comparator<Map<Object, Object>> IMPORTS_COMPARATOR = new Comparator<Map<Object,Object>>() {
        public int compare(Map<Object, Object> o1, Map<Object, Object> o2) {
            Integer rank1 = RANK.get((String) o1.get("importFileName"));
            Integer rank2 = RANK.get((String) o2.get("importFileName"));
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

                this.importsInfos = new LinkedHashMap<String, Map<Object, Object>>();
                List<Map<Object, Object>> importsInfosList = new ArrayList<Map<Object, Object>>();
                for (Iterator<File> iterator = importList.iterator(); iterator.hasNext();) {
                    File i = iterator.next();
                    Map<Object, Object> value = prepareSiteImport(i, imports.get(i), messageContext);
                    if (value != null) {
                        Object legacyImport = value.get("legacyImport");
                        if (legacyImport!=null && (Boolean) legacyImport) {
                            final String defaultMappingsFolderPath = JahiaContextLoaderListener.getServletContext().getRealPath("/WEB-INF/var/legacyMappings");
                            final File defaultMappingsFolder = defaultMappingsFolderPath != null ? new File(defaultMappingsFolderPath) : null;
                            Collection<File> legacyMappings = null;
                            Collection<File> legacyDefinitions = null;
                            if (defaultMappingsFolder != null && defaultMappingsFolder.exists()) {
                                try {
                                    legacyMappings = FileUtils.listFiles(defaultMappingsFolder, new String[]{"map"}, false);
                                } catch (Exception e) {
                                    logger.debug("Legacy mappings not found", e);
                                }
                                try {
                                    legacyDefinitions = FileUtils.listFiles(defaultMappingsFolder, new String[]{"cnd"}, false);
                                } catch (Exception e) {
                                    logger.debug("Legacy definitions not found", e);
                                }
                            }

                            org.springframework.core.io.Resource[] modulesLegacyMappings = SpringContextSingleton.getInstance().getResources("/modules/**/META-INF/legacyMappings/*.map");
                            if (legacyMappings == null && modulesLegacyMappings.length > 0) {
                                legacyMappings = new ArrayList<File>();
                            }
                            for (int j=0; j<modulesLegacyMappings.length; j++) {
                                legacyMappings.add(modulesLegacyMappings[j].getFile());
                            }

                            org.springframework.core.io.Resource[] modulesLegacyDefinitions = SpringContextSingleton.getInstance().getResources("/modules/**/META-INF/legacyMappings/*.cnd");
                            if (legacyDefinitions == null && modulesLegacyDefinitions.length > 0) {
                                legacyDefinitions = new ArrayList<File>();
                            }
                            for (int j=0; j<modulesLegacyDefinitions.length; j++) {
                                legacyDefinitions.add(modulesLegacyDefinitions[j].getFile());
                            }

                            if (legacyMappings != null && !legacyMappings.isEmpty()) {
                                value.put("legacyMappings", legacyMappings);
                            }
                            if (legacyDefinitions != null && !legacyDefinitions.isEmpty()) {
                                value.put("legacyDefinitions", legacyDefinitions);
                            }
                        }
                        importsInfosList.add(value);
                    }
                }

                Collections.sort(importsInfosList, IMPORTS_COMPARATOR);

                List<File> sorted = new LinkedList<File>();
                for (Map<Object, Object> info : importsInfosList) {
                    importsInfos.put((String) info.get("importFileName"), info);
                }

            } catch (IOException e) {
                logger.error("Cannot read import file :" + e.getMessage());
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
    }


    private Map<Object, Object> prepareSiteImport(File i, String filename, MessageContext messageContext) throws IOException {
        Map<Object, Object> importInfos = new HashMap<Object, Object>();
        importInfos.put("importFile", i);
        importInfos.put("importFileName", filename);
        importInfos.put("selected", Boolean.TRUE);
        if (importProperties != null) {
            importInfos.put("originatingJahiaRelease", importProperties.getProperty("JahiaRelease"));
        }
        if (filename.endsWith(".xml")) {
            importInfos.put("type", "xml");
        } else if (filename.endsWith("systemsite.zip")) {
            importInfos.put("type", "files");
        } else {
            List<String> installedModules = readInstalledModules(i);
            org.jahia.utils.zip.ZipEntry z;
            NoCloseZipInputStream zis2 = new NoCloseZipInputStream(
                    new BufferedInputStream(new FileInputStream(i)));

            boolean isSite = false;
            boolean isLegacySite = false;
            try {
                while ((z = zis2.getNextEntry()) != null) {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        importInfos.putAll(p);

                        importInfos.put("templates", "");
                        if (importInfos.containsKey("templatePackageName")) {
                            JahiaTemplateManagerService templateManager = ServicesRegistry
                                    .getInstance()
                                    .getJahiaTemplateManagerService();
                            JahiaTemplatesPackage pack = templateManager
                                    .getTemplatePackageByFileName((String) importInfos
                                            .get("templatePackageName"));
                            if (pack == null) {
                                pack = templateManager
                                        .getTemplatePackage((String) importInfos
                                                .get("templatePackageName"));
                            }
                            if (pack != null) {
                                importInfos.put("templates", pack.getRootFolder());
                            }
                        }
                        importInfos.put("oldsitekey",
                                importInfos.get("sitekey"));
                        isSite = true;
                    } else if (z.getName().startsWith("export_")) {
                        isLegacySite = true;
                    } else if (validityCheckOnImport && z.getName().contains("repository.xml")) {
                        try {
                            long timer = System.currentTimeMillis();
                            ValidationResults validationResults = ImportExportBaseService
                                    .getInstance()
                                    .validateImportFile(
                                            JCRSessionFactory.getInstance().getCurrentUserSession(),
                                            zis2, "application/xml", installedModules);
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
                                if (importInfos.containsKey("validationResult")) {
                                    // merge results
                                    importInfos.put("validationResult",
                                            ((ValidationResults) importInfos
                                                    .get("validationResult"))
                                                    .merge(validationResults));
                                } else {
                                    importInfos.put("validationResult", validationResults);
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
                } else {
                    validateSite(messageContext, importInfos);
                }
                importInfos.put("legacyImport",isLegacySite);
            } else {
                importInfos.put("type", "files");
            }

        }
        return importInfos;
    }

    private List<String> readInstalledModules(File i) throws IOException {
        List<String> modules = new LinkedList<String>();
        org.jahia.utils.zip.ZipEntry z;
        NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(
                new FileInputStream(i)));

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
                                    im.put(Integer.valueOf(StringUtils.substringAfter(key, ".")),
                                            p.getProperty(key));
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

    public void validateDisplayImportContent (ValidationContext context) {
        context.getMessageContext();

        for (Map<Object, Object> infos : importsInfos.values()) {
            File file = (File) infos.get("importFile");
            if (infos.get("selected") != null) {
                if (NON_SITE_IMPORTS.contains(infos.get("importFileName"))) {

                } else {
                    validateSite(context.getMessageContext(), infos);
                }
            }
        }

    }

    private void validateSite(MessageContext messageContext, Map<Object, Object> infos) {
        try {
            infos.put("siteTitleInvalid", StringUtils.isEmpty((String) infos.get("sitetitle")));

            String siteKey = (String) infos.get("sitekey");
            boolean valid = sitesService.isSiteKeyValid(siteKey);
            if (!valid) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey")
                        .defaultText(Messages.getInternal("serverSettings.manageWebProjects.invalidSiteKey", LocaleContextHolder.getLocale()))
                        .build());
            }
            if (valid && sitesService.getSiteByKey(siteKey) != null) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey")
                        .defaultText(Messages.getInternal("serverSettings.manageWebProjects.siteKeyExists", LocaleContextHolder.getLocale()))
                        .build());
            }

            String serverName = (String) infos.get("siteservername");
            valid = sitesService.isServerNameValid(serverName);
            if (!valid) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey")
                        .defaultText(Messages.getInternal("serverSettings.manageWebProjects.invalidServerName", LocaleContextHolder.getLocale()))
                        .build());
            }

            if (valid && !Url.isLocalhost(serverName) && sitesService.getSite(serverName) != null) {
                messageContext.addMessage(new MessageBuilder().error().source("siteKey")
                        .defaultText(Messages.getInternal("serverSettings.manageWebProjects.serverNameExists", LocaleContextHolder.getLocale()))
                        .build());
            }
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    public class SiteBean implements Serializable {
        private String siteKey = "mySite" ;
        private String title = "localhost";
        private String serverName = "My Site";
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

        public void validateCreateSite (ValidationContext context) {
            // check validity...
            String title = (String) context.getUserValue("title");
            String serverName = (String) context.getUserValue("serverName");
            String siteKey = (String) context.getUserValue("siteKey");

            MessageContext messages = context.getMessageContext();

            try {
                if (title != null && (title.length() > 0) && serverName != null &&
                        (serverName.length() > 0) && siteKey != null && (siteKey.length() > 0)) {
                    if (!sitesService.isSiteKeyValid(siteKey)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (siteKey.equals("site")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (!sitesService.isServerNameValid(serverName)) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.invalidServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (serverName.equals("default")) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (!Url.isLocalhost(serverName) && sitesService.getSite(serverName) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherServerName.label", LocaleContextHolder.getLocale()))
                                .build());
                    } else if (sitesService.getSiteByKey(siteKey) != null) {
                        messages.addMessage(new MessageBuilder().error().source("siteKey")
                                .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label", LocaleContextHolder.getLocale()))
                                .build());
                    }
                } else {
                    messages.addMessage(new MessageBuilder().error().source("siteKey")
                            .defaultText(Messages.getInternal("org.jahia.admin.warningMsg.completeRequestInfo.label", LocaleContextHolder.getLocale()))
                            .build());
                }
            } catch (JahiaException e) {
                e.printStackTrace();
            }
        }

        public void validateCreateSiteSelectModules (ValidationContext context) {
        }
    }
}

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
package org.jahia.services.templates;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.dom4j.io.OutputFormat;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.commons.Version;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.data.templates.ModuleState;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DateUtils;
import org.jahia.utils.PomUtils;
import org.jahia.utils.i18n.ResourceBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.FileSystemResource;
import org.xml.sax.SAXException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Template and template set deployment and management service.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaTemplateManagerService extends JahiaService implements ApplicationEventPublisherAware, ApplicationListener<ApplicationEvent>, JahiaAfterInitializationService {

    private static final Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerService.class);

    public static final Set<String> DEFAULT_MODULES_WITH_NO_DEFAUL_DEPENDENCY = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("default", "jquery", "ckeditor", "assets")));
    public static final String MODULE_TYPE_MODULE = "module";
    public static final String MODULE_TYPE_SYSTEM = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_SYSTEM;
    public static final String MODULE_TYPE_TEMPLATES_SET = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_TEMPLATES_SET;

    public static final Comparator<JahiaTemplatesPackage> TEMPLATE_PACKAGE_NAME_COMPARATOR = new Comparator<JahiaTemplatesPackage>() {

        @Override
        public int compare(JahiaTemplatesPackage o1, JahiaTemplatesPackage o2) {
            return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
        }
    };

    private Map<Bundle, ModuleState> moduleStates = new ConcurrentSkipListMap<>();
    private final Map<Bundle, JahiaTemplatesPackage> registeredBundles = new ConcurrentHashMap<>();
    private final Set<Bundle> installedBundles = Sets.newConcurrentHashSet();
    private final Set<Bundle> initializedBundles = Sets.newConcurrentHashSet();
    private final Map<String, List<Bundle>> toBeResolved = new ConcurrentHashMap<>();
    private final OutputFormat prettyPrint = OutputFormat.createPrettyPrint();
    private TemplatePackageDeployer templatePackageDeployer;
    private TemplatePackageRegistry templatePackageRegistry;
    private JahiaSitesService siteService;
    private ApplicationEventPublisher applicationEventPublisher;
    private ModuleBuildHelper moduleBuildHelper;
    private ModuleInstallationHelper moduleInstallationHelper;
    private SourceControlHelper scmHelper;
    private ForgeHelper forgeHelper;
    private List<String> nonManageableModules;
    private Set<String> modulesWithNoDefaultDependency = DEFAULT_MODULES_WITH_NO_DEFAUL_DEPENDENCY;
    private Set<String> knownFragmentHosts = Collections.emptySet();
    private ModuleManager moduleManager;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void setTemplatePackageDeployer(TemplatePackageDeployer deployer) {
        templatePackageDeployer = deployer;
        deployer.setService(this);
    }

    public TemplatePackageDeployer getTemplatePackageDeployer() {
        return templatePackageDeployer;
    }

    public TemplatePackageRegistry getTemplatePackageRegistry() {
        return templatePackageRegistry;
    }

    public SourceControlFactory getSourceControlFactory() {
        return scmHelper.getSourceControlFactory();
    }

    public void setXmlIndentation(int i) {
        prettyPrint.setIndentSize(i);
    }

    public List<String> getNonManageableModules() {
        return nonManageableModules;
    }

    public void setNonManageableModules(List<String> nonManageableModules) {
        this.nonManageableModules = nonManageableModules;
    }

    @Override
    public void start() throws JahiaInitializationException {
        // do nothing
    }

    @Override
    public void stop() throws JahiaException {
        // do nothing
    }

    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            // flush resource bundle cache
            ResourceBundles.flushCache();
            NodeTypeRegistry.getInstance().flushLabels();
        }
    }

    /**
     * Checkout module sources, compile and deploy
     *
     * @param moduleSources sources folder
     * @param scmURI        scm uri ( in mvn format , scm:<type>:<url>
     * @param branchOrTag   branch or tag
     * @param moduleId      name of the module to checkout, if there are multiple modules in the repository
     * @param version       version of the module to checkout, if there are multiple modules in the repository
     * @param session       session
     * @return the module node
     * @throws IOException
     * @throws RepositoryException in case of JCR-related errors
     * @throws BundleException
     */
    public JCRNodeWrapper checkoutModule(File moduleSources, String scmURI, String branchOrTag, String moduleId,
                                         String version, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        return scmHelper.checkoutModule(moduleSources, scmURI, branchOrTag, moduleId, version, session);
    }

    public File checkoutTempModule(String scmURI, String branchOrTag, String moduleId,
                                   String version) throws RepositoryException, XmlPullParserException, DocumentException, IOException {
        return scmHelper.checkoutTmpModule(moduleId, version, scmURI, branchOrTag);
    }

    public JahiaTemplatesPackage duplicateModule(String moduleName, String moduleId, String groupId, String srcPath, String scmURI, String branchOrTag, String srcModuleId, String srcModuleVersion, boolean uninstallSrcModule, String dstPath, boolean deleteSrcFolder, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        return moduleBuildHelper.duplicateModule(moduleName, moduleId, groupId, srcPath, scmURI, branchOrTag, srcModuleId, srcModuleVersion, uninstallSrcModule, dstPath, deleteSrcFolder, session);
    }

    public JCRNodeWrapper createModule(String moduleName, String artifactId, String groupId, String moduleType, File sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        return moduleBuildHelper.createModule(moduleName, artifactId, groupId, moduleType, sources, session);
    }

    public JahiaTemplatesPackage deployModule(File warFile, JCRSessionWrapper session) throws RepositoryException {
        return templatePackageDeployer.deployModule(warFile, session);
    }

    public JahiaTemplatesPackage compileAndDeploy(final String moduleId, File sources, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {
        JahiaTemplatesPackage templatesPackage = moduleBuildHelper.compileAndDeploy(moduleId, sources, session);
        File file = new File(sources, "src/main/import/repository.xml.generated");
        if (file.exists()) {
            file.delete();
        }
        regenerateImportFile(moduleId, sources, session);
        return templatesPackage;
    }

    public File compileModule(File sources) throws IOException {
        return moduleBuildHelper.compileModule(sources).getFile();
    }

    public JCRNodeWrapper installFromSources(File sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        if (!sources.exists()) {
            return null;
        }

        File pom = new File(sources, "pom.xml");
        try {
            Model model = PomUtils.read(pom);
            JahiaTemplatesPackage pack = compileAndDeploy(model.getArtifactId(), sources, session);
            pack.setSourcesFolder(sources);
            JCRNodeWrapper node = session.getNode("/modules/" + pack.getIdWithVersion());
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
            session.save();

            return node;
        } catch (XmlPullParserException e) {
            throw new IOException("Cannot parse pom.xml file at " + pom, e);
        }
    }

    public File getSources(JahiaTemplatesPackage pack, JCRSessionWrapper session) throws RepositoryException {
        if (pack.getSourcesFolder() != null) {
            return pack.getSourcesFolder();
        }
        JCRNodeWrapper n = session.getNode("/modules/" + pack.getIdWithVersion());
        if (n.hasNode("j:versionInfo")) {
            JCRNodeWrapper vi = n.getNode("j:versionInfo");
            if (vi.hasProperty("j:sourcesFolder")) {
                File sources = new File(vi.getProperty("j:sourcesFolder").getString());
                if (checkValidSources(pack, sources)) {
                    pack.setSourcesFolder(sources);
                    return sources;
                }
            }
        }
        return null;
    }

    public void sendToSourceControl(String moduleId, String scmURI, String scmType, JCRSessionWrapper session) throws RepositoryException, IOException {
        scmHelper.sendToSourceControl(moduleId, scmURI, scmType, session);
    }

    public Map<String, String> listTags(String scmURI) throws IOException {
        return scmHelper.listTags(scmURI);
    }

    public Map<String, String> listBranches(String scmURI) throws IOException {
        return scmHelper.listBranches(scmURI);
    }

    public String guessBranchOrTag(String moduleVersion, String scm, Set<String> branchOrTags) {
        return scmHelper.guessBranchOrTag(moduleVersion, scm, branchOrTags);
    }

    public boolean checkValidSources(JahiaTemplatesPackage pack, File sources) {
        return scmHelper.checkValidSources(pack, sources);
    }

    public File releaseModule(String moduleId, ModuleReleaseInfo releaseInfo, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupById(moduleId);
        if (pack.getVersion().isSnapshot() && releaseInfo != null && releaseInfo.getNextVersion() != null) {
            File sources = getSources(pack, session);
            if (sources != null) {
                JCRNodeWrapper vi = session.getNode("/modules/" + pack.getIdWithVersion() + "/j:versionInfo");
                regenerateImportFile(moduleId, sources, session);
                if (vi.hasProperty("j:scmURI")) {
                    SourceControlManagement scm;
                    scm = pack.getSourceControl();
                    if (scm != null) {
                        scm.update();
                        scm.commit("Release");
                        return releaseModule(pack, releaseInfo, sources, vi.getProperty("j:scmURI").getString(), session);
                    }
                }
                return releaseModule(pack, releaseInfo, sources, null, session);
            }
        }
        return null;
    }

    public File releaseModule(final JahiaTemplatesPackage module, ModuleReleaseInfo releaseInfo, File sources, String scmUrl, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {

        File pom = new File(sources, "pom.xml");
        Model model;
        try {
            model = PomUtils.read(pom);
            if (scmUrl != null && !StringUtils.equals(model.getScm().getConnection(), scmUrl)) {
                PomUtils.updateScm(pom, scmUrl);
                module.getSourceControl().add(pom);
                module.getSourceControl().commit("restore pom scm uri before release");
            }
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
        String lastVersion = PomUtils.getVersion(model);
        if (!lastVersion.endsWith("-SNAPSHOT")) {
            throw new IOException("Cannot release a non-SNAPSHOT version");
        }
        String releaseVersion = StringUtils.substringBefore(lastVersion, "-SNAPSHOT");

        File generatedWar;
        try {
            generatedWar = moduleBuildHelper.releaseModuleInternal(model, lastVersion, releaseVersion, releaseInfo, sources, scmUrl);
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }

        File releasedModules = new File(settingsBean.getJahiaVarDiskPath(), "released-modules");
        if (generatedWar.exists()) {
            FileUtils.moveFileToDirectory(generatedWar, releasedModules, true);
            generatedWar = new File(releasedModules, generatedWar.getName());
        } else {
            throw new IOException("Module release failed.");
        }

        moduleManager.install(new FileSystemResource(generatedWar), null);

        JahiaTemplatesPackage pack = compileAndDeploy(module.getId(), sources, session);
        JCRNodeWrapper node = session.getNode("/modules/" + pack.getIdWithVersion());
        node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
        if (scmUrl != null) {
            node.getNode("j:versionInfo").setProperty("j:scmURI", scmUrl);
        }
        session.save();

        undeployModule(module);

        activateModuleVersion(module.getId(), releaseInfo.getNextVersion());

        if (releaseInfo.isPublishToMaven() || releaseInfo.isPublishToForge()) {
            releaseInfo.setArtifactUrl(forgeHelper.computeModuleJarUrl(releaseVersion, releaseInfo, model));
            if (releaseInfo.isPublishToForge() && releaseInfo.getForgeUrl() != null) {
                String forgeModuleUrl = forgeHelper.createForgeModule(releaseInfo, generatedWar);
                releaseInfo.setForgeModulePageUrl(forgeModuleUrl);
            } else if (releaseInfo.isPublishToMaven() && releaseInfo.getRepositoryUrl() != null) {
                deployToMaven(PomUtils.getGroupId(model), model.getArtifactId(), releaseInfo, generatedWar);
            }
        }

        return generatedWar;
    }

    public void deployToMaven(String groupId, String artifactId, ModuleReleaseInfo releaseInfo, File generatedWar) throws IOException {
        moduleBuildHelper.deployToMaven(groupId, artifactId, releaseInfo, generatedWar);
    }

    public List<File> regenerateImportFile(final String moduleId, final File sources, JCRSessionWrapper session) throws RepositoryException {
        logger.info("Re-generating initial import file for module {} in source folder {}", moduleId, sources);
        long startTime = System.currentTimeMillis();
        final List<File> modifiedFiles = new ArrayList<>();

        if (session.getLocale() != null) {
            logger.error("Cannot generated export with i18n session");
            return modifiedFiles;
        }

        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(session.getUser(), null, null, new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper localSession) throws RepositoryException {
                JahiaTemplatesPackage aPackage = getTemplatePackageById(moduleId);
                SourceControlManagement scm = null;
                try {
                    scm = getTemplatePackageById(moduleId).getSourceControl();
                } catch (Exception e) {
                    logger.error("Cannot get SCM", e);
                }
                try {
                    moduleBuildHelper.regenerateImportFile(localSession, modifiedFiles, sources, moduleId, aPackage.getIdWithVersion());

                    if (scm != null) {
                        try {
                            scm.add(modifiedFiles);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } catch (RepositoryException | SAXException | TransformerException | IOException e1) {
                    logger.error(e1.getMessage(), e1);
                }
                return null;
            }
        });


        logger.info("Initial import for module {} re-generated in {}", moduleId, DateUtils.formatDurationWords(System.currentTimeMillis() - startTime));

        return modifiedFiles;
    }

    private void setDependenciesInPom(File sources, List<String> dependencies, boolean useProperties) {
        File pom = new File(sources, "pom.xml");
        try {
            PomUtils.updateJahiaDepends(pom, StringUtils.join(dependencies, ","), useProperties);
        } catch (Exception e) {
            logger.error("Unable to updated dependencies in pom file: " + pom, e);
        }
    }

    public void updateDependencies(JahiaTemplatesPackage pack, List<String> depends) {
        pack.getDepends().clear();
        pack.getDepends().addAll(depends);
        templatePackageRegistry.computeDependencies(pack);

        if (pack.getSourcesFolder() != null) {
            setDependenciesInPom(pack.getSourcesFolder(), depends, shouldUsePropertiesInPom(pack));
        }
        applicationEventPublisher.publishEvent(new ModuleDependenciesEvent(pack.getId(), this));
    }

    private boolean shouldUsePropertiesInPom(JahiaTemplatesPackage pack) {
        boolean useProperties = false;
        String jahiaRequiredVersion = pack.getBundle().getHeaders().get("Jahia-Required-Version");
        if (StringUtils.isNotEmpty(jahiaRequiredVersion)) {
            if (new Version(jahiaRequiredVersion).compareTo(new Version("7.1.0.0")) >= 0) {
                useProperties = true;
            }
        }
        return useProperties;
    }

    /**
     * Fires a Spring event in core context and contexts of all modules to notify listeners about the fact that a module bundle is either
     * started or stopped.
     *
     * @param aPackage the module that generated this event
     */
    public void fireTemplatePackageRedeployedEvent(JahiaTemplatesPackage aPackage) {
        SpringContextSingleton.getInstance().publishEvent(new TemplatePackageRedeployedEvent(aPackage.getId()));
    }

    public void setSourcesFolderInPackage(JahiaTemplatesPackage pack, File sources) {
        scmHelper.setSourcesFolderInPackage(pack, sources);
    }

    /**
     * get List of installed modules for a site.
     *
     * @param siteKey                       key of the site
     * @param includeTemplateSet            if true (default is false) include dependencies of the template set
     * @param includeDirectDependencies     if true (default is false) include dependencies of dependencies
     * @param includeTransitiveDependencies if true (default is false) include all dependencies
     * @return list of template packages
     * @throws JahiaException
     */
    public List<JahiaTemplatesPackage> getInstalledModulesForSite(String siteKey,
                                                                  boolean includeTemplateSet, boolean includeDirectDependencies,
                                                                  boolean includeTransitiveDependencies) throws JahiaException {

        JahiaSite site = siteService.getSiteByKey(siteKey);
        if (site == null) {
            throw new JahiaException("Site cannot be found for key " + siteKey,
                    "Site cannot be found for key " + siteKey, JahiaException.SITE_NOT_FOUND,
                    JahiaException.ERROR_SEVERITY);
        }

        List<String> installedModules = site.getInstalledModules();
        if (!includeTemplateSet) {
            if (installedModules.size() > 1) {
                installedModules = installedModules.subList(1, installedModules.size());
                Collections.sort(installedModules);
            } else {
                installedModules = Collections.emptyList();
            }
        }

        Set<String> modules = new TreeSet<>();

        if (includeDirectDependencies) {
            modules.addAll(installedModules);
        }

        if (includeTransitiveDependencies) {
            includeTransitiveModuleDependencies(installedModules, modules);
        }
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> all = templatePackageRegistry.getAllModuleVersions();
        List<JahiaTemplatesPackage> packages = new LinkedList<>();
        for (String m : modules) {
            JahiaTemplatesPackage pkg = getTemplatePackageById(m);
            pkg = pkg != null ? pkg : getTemplatePackage(m);
            if (pkg == null && all.containsKey(m)) {
                pkg = all.get(m).get(all.get(m).firstKey());
            }
            if (pkg != null) {
                packages.add(pkg);
            }
        }

        return packages.isEmpty() ? Collections.<JahiaTemplatesPackage>emptyList() : packages;
    }

    private void includeTransitiveModuleDependencies(List<String> installedModules, Set<String> modules) {
        for (String m : installedModules) {
            JahiaTemplatesPackage pkg = getTemplatePackageById(m);
            pkg = pkg != null ? pkg : getTemplatePackage(m);
            if (pkg != null) {
                for (JahiaTemplatesPackage deps : pkg.getDependencies()) {
                    if (!installedModules.contains(deps.getId())) {
                        modules.add(deps.getId());
                    }
                }
            }
        }
    }

    public void autoInstallModulesToSites(JahiaTemplatesPackage module, JCRSessionWrapper session)
            throws RepositoryException {
        moduleInstallationHelper.autoInstallModulesToSites(module, session);
    }

    /**
     * Install module in provided list of site
     *
     * @param module         : module to install
     * @param sessionWrapper : session to use
     * @param sites          : list of sites on which deploy the module
     * @throws RepositoryException in case of JCR-related errors
     */
    public void installModuleOnAllSites(JahiaTemplatesPackage module, JCRSessionWrapper sessionWrapper, List<JCRNodeWrapper> sites) throws RepositoryException {
        moduleInstallationHelper.installModuleOnAllSites(module, sessionWrapper, sites);
    }

    public void installModule(final String moduleId, final String sitePath, String username)
            throws RepositoryException {
        moduleInstallationHelper.installModule(moduleId, sitePath, username);
    }

    public void installModule(final String moduleId, final String version, final String sitePath, String username) throws RepositoryException {
        moduleInstallationHelper.installModule(moduleId, version, sitePath, username);
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        installModules(Collections.singletonList(module), sitePath, session);
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        moduleInstallationHelper.installModules(modules, sitePath, session);
    }

    public void synchro(JCRNodeWrapper source, JCRNodeWrapper destinationNode, JCRSessionWrapper session, String moduleName,
                        Map<String, List<String>> references) throws RepositoryException {
        moduleInstallationHelper.synchro(source, destinationNode, session, moduleName, references);
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, Map<String, List<String>> references, boolean doUpdate, boolean doChildren)
            throws RepositoryException {
        moduleInstallationHelper.templatesSynchro(source, destinationNode, session, references, doUpdate, doChildren);
    }

    public void uninstallModule(final String module, final String sitePath, String username, final boolean purgeAllContent)
            throws RepositoryException {
        moduleInstallationHelper.uninstallModule(module, sitePath, username, purgeAllContent);
    }

    public void uninstallModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        uninstallModules(Collections.singletonList(module), sitePath, session);
    }

    public void uninstallModules(final List<JahiaTemplatesPackage> modules, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        List<String> moduleIds = new ArrayList<>();
        for (JahiaTemplatesPackage module : modules) {
            moduleIds.add(module.getId());
        }
        moduleInstallationHelper.uninstallModules(moduleIds, sitePath, session);
    }

    public void uninstallModulesByIds(final List<String> moduleIds, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        moduleInstallationHelper.uninstallModules(moduleIds, sitePath, session);
    }

    public void uninstallModulesFromAllSites(final String module, final String username, final boolean purgeAllContent) throws RepositoryException {
        moduleInstallationHelper.uninstallModulesFromAllSites(module, username, purgeAllContent);
    }

    public void uninstallModulesFromAllSites(final String module, final JCRSessionWrapper session) throws RepositoryException {
        uninstallModulesFromAllSites(Collections.singletonList(module), session);
    }

    public void uninstallModulesFromAllSites(final List<String> modules, final JCRSessionWrapper session) throws RepositoryException {
        moduleInstallationHelper.uninstallModulesFromAllSites(modules, session);
    }

    /**
     * Check if any content is created with definitions in this module
     *
     * @param module
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    public boolean checkExistingContent(final String module) throws RepositoryException {
        return moduleInstallationHelper.checkExistingContent(module);
    }

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailableTemplatePackages() {
        return templatePackageRegistry.getAvailablePackages();
    }

    public List<JahiaTemplatesPackage> getNonSystemTemplateSetPackages() {
        final int packagesCount = getAvailableTemplatePackagesCount();
        if (packagesCount > 0) {
            List<JahiaTemplatesPackage> result = new ArrayList<>(packagesCount);
            for (JahiaTemplatesPackage templatePackage : getAvailableTemplatePackages()) {
                if (templatePackage.getModuleType().equals(TemplatePackageRegistry.TEMPLATES_SET) && !templatePackage.getId().equals("templates-system")) {
                    result.add(templatePackage);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    public List<JahiaTemplatesPackage> getNonSystemModulePackages() {
        final int packagesCount = getAvailableTemplatePackagesCount();
        if (packagesCount > 0) {
            List<JahiaTemplatesPackage> result = new ArrayList<>(packagesCount);
            for (JahiaTemplatesPackage templatePackage : getAvailableTemplatePackages()) {
                if (!templatePackage.getModuleType().equals(TemplatePackageRegistry.TEMPLATES_SET) && !templatePackage.getModuleType().equals("system")) {
                    result.add(templatePackage);
                }
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailableTemplatePackagesCount() {
        return templatePackageRegistry.getAvailablePackagesCount();
    }

    public Map<String, Action> getActions() {
        return templatePackageRegistry.getActions();
    }

    public Map<String, BackgroundAction> getBackgroundActions() {
        return templatePackageRegistry.getBackgroundActions();
    }

    public List<ErrorHandler> getErrorHandler() {
        return templatePackageRegistry.getErrorHandlers();
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified fileName is not
     * registered in the repository.
     *
     * @param fileName the template package fileName to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the
     * repository
     * @deprecated use {@link #getTemplatePackageById(String)} instead
     */
    public JahiaTemplatesPackage getTemplatePackageByFileName(String fileName) {
        return getTemplatePackageById(fileName);
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified Id is not
     * registered in the repository.
     *
     * @param moduleId the template package Id to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified Id is not registered in the
     * repository
     */
    public JahiaTemplatesPackage getTemplatePackageById(String moduleId) {
        return templatePackageRegistry.lookupById(moduleId);
    }

    /**
     * Returns a list of {@link RenderFilter} instances, configured for the specified templates package.
     *
     * @return a list of {@link RenderFilter} instances, configured for the specified templates package
     */
    public List<RenderFilter> getRenderFilters() {
        return templatePackageRegistry.getRenderFilters();
    }

    /**
     * Returns a set of all available template packages having templates for a module.
     *
     * @return a set of all available template packages
     */
    public Set<JahiaTemplatesPackage> getModulesWithViewsForComponent(String componentName) {
        Set<JahiaTemplatesPackage> r = templatePackageRegistry.getModulesWithViewsPerComponents().get(StringUtils.replaceChars(componentName, ':', '_'));
        return r != null ? r : Collections.<JahiaTemplatesPackage>emptySet();
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified name is not
     * registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the
     * repository
     */
    public JahiaTemplatesPackage getTemplatePackage(String packageName) {
        return templatePackageRegistry.lookup(packageName);
    }

    /**
     * Returns the lookup map for template packages by the JCR node name.
     *
     * @return the lookup map for template packages by the JCR node name
     */
    @SuppressWarnings("unchecked")
    public Map<String, JahiaTemplatesPackage> getTemplatePackageByNodeName() {

        return LazyMap.decorate(new HashMap<String, JahiaTemplatesPackage>(), new Transformer() {

            @Override
            public Object transform(Object input) {
                return templatePackageRegistry.lookupById(String.valueOf(input));
            }
        });
    }

    public JahiaTemplatesPackage getAnyDeployedTemplatePackage(String templatePackage) {
        JahiaTemplatesPackage pack = getTemplatePackageById(templatePackage);
        if (pack == null) {
            Set<ModuleVersion> versions = getTemplatePackageRegistry().getAvailableVersionsForModule(templatePackage);
            if (!versions.isEmpty()) {
                pack = getTemplatePackageRegistry().lookupByIdAndVersion(templatePackage, versions.iterator().next());
                if (pack == null) {
                    pack = getTemplatePackageRegistry().lookupByNameAndVersion(templatePackage, versions.iterator().next());
                }
            }
        }
        return pack;
    }

    /**
     * Returns a set of existing template sets that are available for site creation.
     *
     * @return a set of existing template sets that are available for site creation
     */
    public Set<String> getTemplateSetNames() {

        try {

            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Set<String>>() {

                @Override
                public Set<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    QueryManager qm = session.getWorkspace().getQueryManager();
                    Set<String> templateSets = new TreeSet<>();
                    for (NodeIterator nodes = qm
                            .createQuery(
                                    "select * from [jnt:module] as module " +
                                            "inner join [jnt:moduleVersion] as version on ischildnode(version,module) " +
                                            "where isdescendantnode(module,'/modules') " +
                                            "and name(module) <> 'templates-system' " +
                                            "and version.[j:moduleType]='templatesSet'",
                                    Query.JCR_SQL2
                            ).execute().getNodes(); nodes.hasNext(); ) {
                        Node node = nodes.nextNode();
                        if (getTemplatePackageById(node.getName()) != null) {
                            templateSets.add(node.getName());
                        }
                    }

                    return templateSets;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Unable to get template set names. Cause: " + e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    public JahiaTemplatesPackage activateModuleVersion(String moduleId, String version) throws RepositoryException, BundleException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByIdAndVersion(moduleId, new ModuleVersion(version));
        module.getBundle().start();
        return module;
    }

    public JahiaTemplatesPackage stopModule(String moduleId) throws RepositoryException, BundleException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupById(moduleId);
        module.getBundle().stop();
        return module;
    }

    public void undeployModule(String moduleId, String version) throws RepositoryException {
        templatePackageDeployer.undeployModule(moduleId, version);
    }

    public void undeployModule(JahiaTemplatesPackage pack) throws RepositoryException {
        templatePackageDeployer.undeployModule(pack.getId(), pack.getVersion().toString());
    }

    /**
     * Checks if the specified template is available either in the requested template set or in one of the deployed modules.
     *
     * @param templateName    the path of the template to be checked
     * @param templateSetName the name of the target template set
     * @return <code>true</code> if the specified template is present; <code>false</code> otherwise
     */
    public boolean isTemplatePresent(String templateName, String templateSetName) {
        return isTemplatePresent(templateName, ImmutableSet.of(templateSetName));
    }

    /**
     * Checks if the specified template is available either in one of the requested template sets or modules.
     *
     * @param templateName     the path of the template to be checked
     * @param templateSetNames the set of template sets and modules we should check for the presence of the specified template
     * @return <code>true</code> if the specified template is present; <code>false</code> otherwise
     */
    public boolean isTemplatePresent(final String templateName, final Set<String> templateSetNames) {

        long timer = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Checking presense of the template {} in modules {}", templateName,
                    templateSetNames);
        }

        if (StringUtils.isEmpty(templateName)) {
            throw new IllegalArgumentException("Template path is either null or empty");
        }
        if (templateSetNames == null || templateSetNames.isEmpty()) {
            throw new IllegalArgumentException("The template/module set to check is empty");
        }

        boolean present = true;
        try {
            present = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {

                @Override
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return isTemplatePresent(templateName, templateSetNames, session);
                }
            });
        } catch (RepositoryException e) {
            logger.error("Unable to check presence of the template '" + templateName
                    + "' in the modules '" + templateSetNames + "'. Cause: " + e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Template {} {} in modules {} in {} ms",
                    new String[]{templateName, present ? "found" : "cannot be found",
                            templateSetNames.toString(),
                            String.valueOf(System.currentTimeMillis() - timer)}
            );
        }

        return present;
    }

    private boolean isTemplatePresent(String templateName, Set<String> templateSetNames, JCRSessionWrapper session) throws RepositoryException {

        QueryManager queryManager = session.getWorkspace().getQueryManager();

        StringBuilder query = new StringBuilder(512);
        query.append("select * from [jnt:template] as t inner join ["
                + Constants.JAHIANT_VIRTUALSITE
                + "]"
                + " as ts on isdescendantnode(t, ts) where isdescendantnode(ts, '/modules') and"
                + " name(t)='");
        query.append(templateName).append("' and (");

        boolean first = true;
        for (String module : templateSetNames) {
            if (!first) {
                query.append(" OR ");
            } else {
                first = false;
            }
            query.append("name(ts)='").append(module).append("'");
        }
        query.append(")");

        if (logger.isDebugEnabled()) {
            logger.debug("Executing query {}", query.toString());
        }
        return queryManager.createQuery(query.toString(), Query.JCR_SQL2).execute().getNodes().hasNext();
    }


    public Map<Bundle, ModuleState> getModuleStates() {
        return moduleStates;
    }

    public Map<Bundle, JahiaTemplatesPackage> getRegisteredBundles() {
        return registeredBundles;
    }

    public Set<Bundle> getInstalledBundles() {
        return installedBundles;
    }

    public Set<Bundle> getInitializedBundles() {
        return initializedBundles;
    }

    public Map<String, List<Bundle>> getToBeResolved() {
        return toBeResolved;
    }

    /**
     * Returns list of module bundles in the specified state.
     *
     * @param state the state of the module to be considered
     * @return list of module bundles in the specified state or an empty list if there no modules in that state
     */
    public List<Bundle> getModulesByState(ModuleState.State state) {
        List<Bundle> modules = new LinkedList<>();
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            if (entry.getValue().getState().equals(state)) {
                modules.add(entry.getKey());
            }
        }

        return !modules.isEmpty() ? modules : Collections.<Bundle>emptyList();
    }

    public void setModuleStates(Map<Bundle, ModuleState> moduleStates) {
        this.moduleStates = moduleStates;
    }

    public boolean differentModuleWithSameIdExists(String symbolicName, String groupId) {
        SortedMap<ModuleVersion, JahiaTemplatesPackage> moduleVersions = templatePackageRegistry.getAllModuleVersions().get(symbolicName);
        return moduleVersions != null && !moduleVersions.isEmpty() && !moduleVersions.get(moduleVersions.firstKey()).getGroupId().equals(groupId);
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        clearModuleTemplateNodes();
    }

    private void clearModuleTemplateNodes() {
        if (settingsBean.isProcessingServer() && Boolean.parseBoolean(settingsBean.getPropertiesFile().getProperty("modules.purgeAtStartup", "true"))) {
            logger.info("Start checking module template nodes");
            long start = System.currentTimeMillis();
            int count = 0;

            try {
                count = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
                    @Override
                    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        int innerCount = 0;

                        QueryResult qr = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:moduleVersion] as version where isdescendantnode(version,'/modules/')", Query.JCR_SQL2).execute();

                        for (NodeIterator moduleVersions = qr.getNodes(); moduleVersions.hasNext(); ) {
                            JCRNodeWrapper moduleVersionNode = (JCRNodeWrapper) moduleVersions.next();

                            // this module have templates node, check if it's still in the list of deployed module
                            if (moduleVersionNode.hasNode("templates")) {

                                String packageId = moduleVersionNode.getParent().getName();
                                ModuleVersion moduleVersion = new ModuleVersion(moduleVersionNode.getName());

                                if (BundleUtils.getBundle(packageId, moduleVersion.toString()) == null) {
                                    // no module found for this version, clean module nodes
                                    try {
                                        templatePackageDeployer.clearModuleNodes(packageId, moduleVersion);
                                        logger.info("Template nodes have been purged for module {} v{}", packageId, moduleVersion);
                                        innerCount++;
                                    } catch (RepositoryException e) {
                                        logger.error("Error while purging module template nodes for module " + packageId + " v" + moduleVersion, e);
                                    }
                                }
                            }
                        }

                        return innerCount;
                    }
                });
            } catch (RepositoryException e) {
                logger.error("Error while cleaning module template nodes", e);
            }

            String timeTaken = DateUtils.formatDurationWords(System.currentTimeMillis() - start);
            if (count > 0) {
                logger.info("Module template nodes cleanup finished for {} module(s) in {}", count, timeTaken);
            } else {
                logger.info("Module template nodes check finished in {}. No cleanup needed.", timeTaken);
            }
        }
    }

    /**
     * This event is fired when a template module is re-deployed (in runtime, not on the server startup).
     *
     * @author Sergiy Shyrkov
     */
    public static class TemplatePackageRedeployedEvent extends ApplicationEvent {

        private static final long serialVersionUID = 789720524077775537L;

        public TemplatePackageRedeployedEvent(Object source) {
            super(source);
        }
    }

    /**
     * Event indicates that a module was either installed to the specified site or uninstalled from it.
     */
    public static class ModuleDeployedOnSiteEvent extends ApplicationEvent {

        private static final long serialVersionUID = -6693201714720533228L;
        private final String targetSitePath;

        public ModuleDeployedOnSiteEvent(String targetSitePath, Object source) {
            super(source);
            this.targetSitePath = targetSitePath;
        }

        public String getTargetSitePath() {
            return targetSitePath;
        }
    }

    /**
     * Is fired when the module dependencies are changed.
     */
    public static class ModuleDependenciesEvent extends ApplicationEvent {
        private static final long serialVersionUID = -6693201714720533228L;
        private final String moduleName;

        public ModuleDependenciesEvent(String moduleName, Object source) {
            super(source);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }
    }

    public void setModuleInstallationHelper(ModuleInstallationHelper moduleInstallationHelper) {
        this.moduleInstallationHelper = moduleInstallationHelper;
    }

    /**
     * Indicates if any issue related to the definitions has been encountered since the last startup. When this method
     * returns true, the only way to get back false as a return value is to restart Jahia.
     *
     * @return true if an issue with the def has been encountered, false otherwise.
     * @since 6.6.2.0
     */
    public final boolean hasEncounteredIssuesWithDefinitions() {
        return this.templatePackageRegistry.hasEncounteredIssuesWithDefinitions();
    }

    /**
     * Injects an instance of the SCM helper.
     *
     * @param scmHelper an instance of the SCM helper
     */
    public void setSourceControlHelper(SourceControlHelper scmHelper) {
        this.scmHelper = scmHelper;
    }

    public void setModuleBuildHelper(ModuleBuildHelper moduleBuildHelper) {
        this.moduleBuildHelper = moduleBuildHelper;
    }

    /**
     * Injects an instance of the helper class for Private App Store related operations.
     *
     * @param forgeHelper an instance helper class for Private App Store related operations
     */
    public void setForgeHelper(ForgeHelper forgeHelper) {
        this.forgeHelper = forgeHelper;
    }

    public Set<String> getModulesWithNoDefaultDependency() {
        return modulesWithNoDefaultDependency;
    }

    public void setModulesWithNoDefaultDependency(Set<String> modulesWithNoDefaultDependency) {
        if (modulesWithNoDefaultDependency != null && !modulesWithNoDefaultDependency.isEmpty()) {
            HashSet<String> modules = new HashSet<>(modulesWithNoDefaultDependency);
            modules.add("default");
            this.modulesWithNoDefaultDependency = Collections.unmodifiableSet(modules);
        } else {
            this.modulesWithNoDefaultDependency = DEFAULT_MODULES_WITH_NO_DEFAUL_DEPENDENCY;
        }
    }

    /**
     * Returns a set of symbolic names for bundles, which are known to be target hosts for fragments.
     *
     * @return a set of symbolic names for bundles, which are known to be target hosts for fragments
     */
    public Set<String> getKnownFragmentHosts() {
        return knownFragmentHosts;
    }

    /**
     * Defines a set of symbolic names for bundles, which are known to be target hosts for fragments.
     *
     * @param knownFragmentHosts a set of symbolic names for bundles, which are known to be target hosts for fragments
     */
    public void setKnownFragmentHosts(Set<String> knownFragmentHosts) {
        this.knownFragmentHosts = knownFragmentHosts;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}

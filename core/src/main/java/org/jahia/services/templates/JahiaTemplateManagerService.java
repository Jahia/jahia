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

package org.jahia.services.templates;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.cli.MavenCli;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;
import org.jdom.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;

import com.google.common.collect.ImmutableSet;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import java.io.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Template and template set deployment and management service.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaTemplateManagerService extends JahiaService implements ApplicationEventPublisherAware, ApplicationListener<ApplicationEvent>, JahiaAfterInitializationService {

    public static final String MODULE_TYPE_JAHIAPP = "jahiapp";

    public static final String MODULE_TYPE_MODULE = "module";

    public static final String MODULE_TYPE_PROFILE_MODULE = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_PROFILE_MODULE;

    public static final String MODULE_TYPE_SYSTEM = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_SYSTEM;

    public static final String MODULE_TYPE_TEMPLATES_SET = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_TEMPLATES_SET;

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("/modules/[^/]*/templates/(.*)");

    private static Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerService.class);

    private TemplatePackageDeployer templatePackageDeployer;

    private TemplatePackageRegistry templatePackageRegistry;

    private JahiaSitesBaseService siteService;

    private ApplicationEventPublisher applicationEventPublisher;

    private MavenCli cli = new MavenCli(new ClassWorld("plexus.core", getClass().getClassLoader()));

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void setSiteService(JahiaSitesBaseService siteService) {
        this.siteService = siteService;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void setTemplatePackageDeployer(TemplatePackageDeployer deployer) {
        templatePackageDeployer = deployer;
        deployer.setService(this);
    }

    public void start() throws JahiaInitializationException {
        logger.info("Starting JahiaTemplateManagerService ...");

        // deploy shared templates if not deployed yet
        templatePackageDeployer.deploySharedTemplatePackages();

        // scan the directory for templates
        templatePackageDeployer.registerTemplatePackages();

        logger.info("JahiaTemplateManagerService started successfully."
                + " Total number of found modules: "
                + templatePackageRegistry.getAvailablePackagesCount());
    }

    public void stop() throws JahiaException {
        logger.info("Stopping JahiaTemplateManagerService ...");

        // stop template package watcher
        templatePackageDeployer.stopWatchdog();

        templatePackageRegistry.reset();

        logger.info("... JahiaTemplateManagerService stopped successfully");
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        // start template package watcher
        templatePackageDeployer.startWatchdog();
    }

    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof JahiaContextLoaderListener.RootContextInitializedEvent) {
            if (SettingsBean.getInstance().isProcessingServer()) {
                templatePackageDeployer.initializeModulesContent();
            }
        } else if (event instanceof TemplatePackageRedeployedEvent) {
            // flush resource bundle cache
            JahiaTemplatesRBLoader.clearCache();
            JahiaResourceBundle.flushCache();
            NodeTypeRegistry.flushLabels();
        }
    }

    /**
     * **************************************************************************************************************
     * Module creation / compilation
     * ***************************************************************************************************************
     */

    public JCRNodeWrapper checkoutModule(File sources, String scmURI, String scmType, JCRSessionWrapper session) throws IOException, RepositoryException {
        String tempName = null;

        if (sources == null) {
            tempName = UUID.randomUUID().toString();
            sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources", tempName);
        }

        if (sources.exists()) {
            return null;
        }

        sources.getParentFile().mkdirs();

        try {
            SourceControlManagement scm = SourceControlManagement.checkoutRepository(sources, scmType, scmURI);
            File path = scm.getRootFolder();

            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(path, "pom.xml"));
            Element n = (Element) document.getRootElement().elementIterator("artifactId").next();
            String moduleName = n.getText();

            if (tempName != null) {
                File newPath = new File(sources.getParentFile(), moduleName);
                if (!newPath.exists()) {
                    FileUtils.moveDirectory(path, newPath);
                    path = newPath;
                }
            }

            setSCMConfigInPom(path, scmType, scmURI);

            JahiaTemplatesPackage pack = compileAndDeploy(moduleName, path);

            JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", path.getPath());
            node.getNode("j:versionInfo").setProperty("j:scmUrl", scmURI);
            session.save();

            return node;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public JCRNodeWrapper createModule(String moduleName, String moduleType, File sources, String scmURI, String scmType, JCRSessionWrapper session) throws IOException, RepositoryException {
        if (sources == null) {
            sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources");
            sources.mkdirs();
        }

        String finalFolderName = null;

        if (!sources.exists()) {
            finalFolderName = sources.getName();
            sources = sources.getParentFile();
            if (sources == null) {
                sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources");
            }
            sources.mkdirs();
        }

        if (moduleType.equals("jahiapp")) {
            moduleType = "app";
        }

        String[] archetypeParams = {"archetype:generate",
                "-DarchetypeCatalog=http://maven.jahia.org/maven2/archetype-catalog.xml", //"file:///Users/toto/Downloads/archetype-catalog.xml",
                "-DarchetypeGroupId=org.jahia.archetypes",
                "-DarchetypeArtifactId=jahia-" + moduleType + "-archetype",
                "-DmoduleName=" + moduleName,
                "-DartifactId=" + moduleName,
                "-DjahiaPackageVersion=" + Constants.JAHIA_PROJECT_VERSION,
                "-DinteractiveMode=false"};

        int ret = cli.doMain(archetypeParams, sources.getPath(),
                System.out, System.err);
        if (ret > 0) {
            return null;
        }

        File path = new File(sources, moduleName);
        if (finalFolderName != null && !path.getName().equals(finalFolderName)) {
            try {
                File newPath = new File(sources, finalFolderName);
                FileUtils.moveDirectory(path, newPath);
                path = newPath;
            } catch (IOException e) {
                logger.error("Cannot rename folder", e);
            }
        }

        if (scmURI != null) {
            setSCMConfigInPom(path, scmURI, scmType);
            try {
                SourceControlManagement.createNewRepository(path, scmType, scmURI);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        JahiaTemplatesPackage pack = compileAndDeploy(moduleName, path);

        JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        node.getNode("j:versionInfo").setProperty("j:sourcesFolder", path.getPath());
        node.getNode("j:versionInfo").setProperty("j:scmUrl", scmURI);
        session.save();

        return node;
    }

    public void duplicateModule(String moduleName, String moduleType, final String sourceModule) {
        final File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), moduleName + "/1.0-SNAPSHOT");
        if (tmplRootFolder.exists()) {
            return;
        }
        if (!tmplRootFolder.exists()) {
            logger.info("Start duplicating template package '" + sourceModule + "' to moduleName + '" + moduleName + "'");

            try {
                final List<String> files = new ArrayList<String>();
                final File source = new File(settingsBean.getJahiaTemplatesDiskPath(), sourceModule);
                FileUtils.copyDirectory(source, tmplRootFolder, new FileFilter() {
                    public boolean accept(File pathname) {
                        if (pathname.toString().endsWith("." + sourceModule + ".jsp")) {
                            files.add(pathname.getPath().replace(source.getPath(), tmplRootFolder.getPath()));
                        }
                        return true;
                    }
                });
                for (String file : files) {
                    FileUtils.moveFile(new File(file), new File(file.replace("." + sourceModule + ".jsp", "." + moduleName + ".jsp")));
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            new File(tmplRootFolder, "META-INF").mkdirs();
            new File(tmplRootFolder, "WEB-INF").mkdirs();
            new File(tmplRootFolder, "resources").mkdirs();
            new File(tmplRootFolder, "css").mkdirs();

            createManifest(moduleName, moduleName, tmplRootFolder, moduleType, "1.0", templatePackageRegistry.lookupByFileName(sourceModule).getDepends());
            templatePackageRegistry.register(templatePackageDeployer.getPackage(tmplRootFolder));
            logger.info("Package '" + moduleName + "' successfully created");
        }
    }

    public JahiaTemplatesPackage compileAndDeploy(final String moduleName, File sources) {
        File warFile = compileModule(moduleName, sources);
        return templatePackageDeployer.deployModule(warFile);
    }

    public File compileModule(final String moduleName, File sources) {
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(sources, "pom.xml"));
            Element n;
            if (document.getRootElement().elementIterator("version").hasNext()) {
                n = (Element) document.getRootElement().elementIterator("version").next();
            } else if (document.getRootElement().elementIterator("parent").hasNext()) {
                n = (Element) document.getRootElement().elementIterator("parent").next();
                n = (Element) n.elementIterator("version").next();
            } else {
                return null;
            }
            String version = n.getText();

            String[] installParams = {"clean", "install"};
            cli.doMain(installParams, sources.getPath(), System.out, System.err);

            return new File(sources.getPath() + "/target/" + moduleName + "-" + version + ".war");
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void createManifest(String rootFolder, String packageName, File tmplRootFolder, String moduleType, String version, List<String> depends) {
        try {
            File manifestFile = new File(tmplRootFolder + "/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest();
            if (manifestFile.exists()) {
                InputStream manifestStream = new BufferedInputStream(new FileInputStream(manifestFile), 1024);
                manifest = new Manifest(manifestStream);
            }
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(new Attributes.Name("Manifest-Version"), "1.0");
            attributes.put(new Attributes.Name("Created-By"), "Jahia");
            if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                attributes.put(new Attributes.Name("Built-By"), JCRSessionFactory.getInstance().getCurrentUser().getName());
            }
            attributes.put(new Attributes.Name("Implementation-Version"), version);
            if (!depends.isEmpty()) {
                attributes.put(new Attributes.Name("depends"), StringUtils.substringBetween(depends.toString(), "[", "]"));
            }
            attributes.put(new Attributes.Name("module-type"), moduleType);
            attributes.put(new Attributes.Name("package-name"), packageName);
            attributes.put(new Attributes.Name("root-folder"), rootFolder);

            FileOutputStream out = new FileOutputStream(manifestFile);
            manifest.write(out);
            out.close();

            templatePackageDeployer.setTimestamp(manifestFile.getPath(), manifestFile.lastModified());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public JCRNodeWrapper installFromSources(File sources, JCRSessionWrapper session) throws IOException, RepositoryException {
        if (!sources.exists()) {
            return null;
        }

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(sources, "pom.xml"));
            Element n = (Element) document.getRootElement().elementIterator("artifactId").next();
            String moduleName = n.getText();

            JahiaTemplatesPackage pack = compileAndDeploy(moduleName, sources);

            JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
            session.save();

            return node;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public File getSources(JahiaTemplatesPackage pack, JCRSessionWrapper session) throws RepositoryException {
        JCRNodeWrapper n = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        if (n.hasNode("j:versionInfo")) {
            JCRNodeWrapper vi = n.getNode("j:versionInfo");
            if (vi.hasProperty("j:sourcesFolder")) {
                File sources = new File(vi.getProperty("j:sourcesFolder").getString());

                SAXReader reader = new SAXReader();
                File pom = new File(sources, "pom.xml");
                try {
                    Document document = reader.read(pom);
                    Element versionElement = (Element) document.getRootElement().elementIterator("version").next();
                    String lastVersion = versionElement.getText();
                    if (lastVersion.equals(pack.getVersion().toString())) {
                        return sources;
                    }
                } catch (DocumentException e) {
                    logger.error("Cannot parse pom file", e);
                }
            }
        }
        return null;
    }

    public File releaseModule(String moduleName, String nextVersion, JCRSessionWrapper session) throws RepositoryException, IOException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupByFileName(moduleName);
        if (pack.getVersion().isSnapshot() && nextVersion != null) {
            File sources = getSources(pack, session);
            if (sources != null) {
                JCRNodeWrapper vi = session.getNode("/modules/" + pack.getRootFolderWithVersion() + "/j:versionInfo");
                saveModule(moduleName, sources, session);

                if (vi.hasProperty("j:scmUrl")) {
                    SourceControlManagement scm = null;
                    try {
                        scm = SourceControlManagement.getSourceControlManagement(sources);
                        if (scm != null) {
                            scm.commit("Release");
                        }
                        return releaseModule(pack, nextVersion, sources, vi.getProperty("j:scmUrl").getString(), session);
                    } catch (Exception e) {
                        logger.error("Cannot get SCM", e);
                    }
                } else {
                    return releaseModule(pack, nextVersion, sources, null, session);
                }
            }
        }
        // Old way
        return generateWar(moduleName, session);
    }

    public File generateWar(String moduleName, JCRSessionWrapper session) throws RepositoryException, IOException {
        JahiaTemplatesPackage aPackage = getTemplatePackageByFileName(moduleName);

        ServicesRegistry.getInstance().getJahiaTemplateManagerService().regenerateManifest(aPackage, session);
        ServicesRegistry.getInstance().getJahiaTemplateManagerService().regenerateImportFile(aPackage, session);

        File f = File.createTempFile(moduleName + "-" + aPackage.getVersion(), ".war");
        File templateDir = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), moduleName);

        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
        zip(templateDir, templateDir, zos);
        zos.close();
        return f;
    }

    private void zip(File dir, File rootDir, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                ZipEntry ze = new ZipEntry(Patterns.BACKSLASH.matcher(file.getPath().substring(rootDir.getPath().length() + 1)).replaceAll("/"));
                zos.putNextEntry(ze);
                final InputStream input = new BufferedInputStream(new FileInputStream(file));
                try {
                    IOUtils.copy(input, zos);
                } finally {
                    IOUtils.closeQuietly(input);
                }
            }

            if (file.isDirectory()) {
                ZipEntry ze = new ZipEntry(Patterns.BACKSLASH.matcher(file.getPath().substring(rootDir.getPath().length() + 1)).replaceAll("/") + "/");
                zos.putNextEntry(ze);
                zip(file, rootDir, zos);
            }
        }
    }

    public File releaseModule(final JahiaTemplatesPackage module, String nextVersion, File sources, String scmUrl, JCRSessionWrapper session) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element versionElement = (Element) document.getRootElement().elementIterator("version").next();
            String lastVersion = versionElement.getText();

            String releaseVersion = StringUtils.substringBefore(lastVersion, "-SNAPSHOT");

//            String lastVersion = pack.getLastVersion().toString();
            if (lastVersion.endsWith("-SNAPSHOT")) {
                File generatedWar = null;
                if (scmUrl != null) {
                    String tag = module.getRootFolder() + "-" + releaseVersion;

                    int ret;
                    String MAVEN_HOME = System.getenv().get("MAVEN_HOME") != null ? System.getenv().get("MAVEN_HOME") : "/usr/share/maven";

                    String[] installParams = {"release:prepare", "release:perform",
                            "-Dmaven.home=" + MAVEN_HOME,
                            "-Dtag=" + tag,
                            "-DreleaseVersion=" + releaseVersion,
                            "-DdevelopmentVersion=" + nextVersion,
                            "-DignoreSnapshots=true",
                            "-Dgoals=install",
                            "--batch-mode"
                    };
                    ret = cli.doMain(installParams, sources.getPath(), System.out, System.err);
                    if (ret > 0) {
                        cli.doMain(new String[]{"release:rollback"}, sources.getPath(), System.out, System.err);
                        return null;
                    }

                    File oldWar = new File(settingsBean.getJahiaSharedTemplatesDiskPath(), module + "-" + lastVersion + ".war");
                    if (oldWar.exists()) {
                        oldWar.delete();
                    }

                    generatedWar = new File(sources.getPath() + "/target/checkout/target/" + module.getRootFolder() + "-" + releaseVersion + ".war");
                } else {
                    versionElement.setText(releaseVersion);
                    File modifiedPom = new File(sources, "pom-modified.xml");
                    XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
                    writer.write(document);
                    writer.close();
                    saveFile(new FileInputStream(modifiedPom), pom);

                    generatedWar = compileModule(module.getRootFolder(), sources);

                    versionElement.setText(nextVersion);
                    writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
                    writer.write(document);
                    writer.close();
                    saveFile(new FileInputStream(modifiedPom), pom);
                }

                File releasedModules = new File(settingsBean.getJahiaVarDiskPath(), "released-modules");
                if (generatedWar.exists()) {
                    FileUtils.moveFileToDirectory(generatedWar, releasedModules, true);
                    generatedWar = new File(releasedModules, generatedWar.getName());
                } else {
                    generatedWar = null;
                }


                if (generatedWar != null) {
                    JahiaTemplatesPackage pack = compileAndDeploy(module.getRootFolder(), sources);

                    JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
                    node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
                    if (scmUrl != null) {
                        node.getNode("j:versionInfo").setProperty("j:scmUrl", scmUrl);
                    }
                    session.save();
                }

                return generatedWar;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public List<File> saveModule(String moduleName, File sources, JCRSessionWrapper session) throws RepositoryException {
        List<File> modifiedFiles = new ArrayList<File>();

        SourceControlManagement scm = null;
        try {
            scm = SourceControlManagement.getSourceControlManagement(sources);
        } catch (Exception e) {
            logger.error("Cannot get SCM", e);
        }

        // Handle import
        File sourcesImportFolder = new File(sources, "src/main/import");

        JahiaTemplatesPackage aPackage = getTemplatePackageByFileName(moduleName);

        regenerateManifest(aPackage, session);

        JCRNodeWrapper node = session.getNode("/modules/" + moduleName + "/" + aPackage.getVersion());
        List<String> dependencies = getDependencies(node);
        setDependenciesInPom(sources, dependencies);

        regenerateImportFile(aPackage, session);
        File f = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath() + "/" + moduleName + "/" + aPackage.getVersion() + "/META-INF/import.zip");
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(f));
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                if (!zipentry.isDirectory()) {
                    try {
                        File sourceFile = new File(sourcesImportFolder, zipentry.getName());
                        if (saveFile(zis, sourceFile)) {
                            modifiedFiles.add(sourceFile);
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cannot patch import file", e);
        } finally {
            if (zis != null) {
                IOUtils.closeQuietly(zis);
            }
        }

        // Handle webapp files
        File sourcesWebappFolder = new File(sources, "src/main/webapp");

        File root = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), aPackage.getRootFolderWithVersion());

        try {
            saveFolder(root, sourcesWebappFolder, root, modifiedFiles);
        } catch (Exception e) {
            logger.error("Cannot patch sources", e);
        }

        if (scm != null) {
            try {
                scm.setModifiedFile(modifiedFiles);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return modifiedFiles;
    }

    public void regenerateImportFile(JahiaTemplatesPackage aPackage, JCRSessionWrapper session) throws RepositoryException {
        try {
            File xmlImportFile = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), aPackage.getRootFolder() + "/" + aPackage.getVersion() + "/META-INF/import.xml");
            if (xmlImportFile.exists()) {
                xmlImportFile.delete();
            }
            File importFile = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), aPackage.getRootFolder() + "/" + aPackage.getVersion() + "/META-INF/import.zip");
            if (importFile.exists()) {
                importFile.delete();
            }
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ImportExportService.XSL_PATH, SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/export/templatesCleanup.xsl");
            synchronized (templatePackageDeployer.getTemplatesWatcher()) {
                templatePackageDeployer.setTimestamp(importFile.getPath(), Long.MAX_VALUE);

                ImportExportBaseService
                        .getInstance().exportZip(session.getNode("/modules/" + aPackage.getRootFolderWithVersion()), session.getRootNode(),
                        new FileOutputStream(importFile), params);

                templatePackageDeployer.setTimestamp(importFile.getPath(), importFile.lastModified());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (JDOMException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void regenerateManifest(final JahiaTemplatesPackage aPackage, JCRSessionWrapper session) throws RepositoryException {
        File tmplRootFolder = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), aPackage.getRootFolderWithVersion());

        String version = aPackage.getVersion().toString();
        JCRNodeWrapper node = session.getNode("/modules/" + aPackage.getRootFolderWithVersion());
        List<String> dependencies = getDependencies(node);

        createManifest(aPackage.getRootFolder(), aPackage.getName(), tmplRootFolder, node.getProperty("j:moduleType").getString(), version, dependencies);
    }

    private void saveFolder(File sourceRoot, File targetRoot, File currentSource, List<File> modifiedFiles) throws IOException, PatchFailedException {
        FileInputStream fileInputStream = null;
        try {
            FileFilter filter = new NotFileFilter(new NameFileFilter(new String[]{"versions", "import.zip", "MANIFEST.MF", "deployed.xml"}));
            for (File sourceFile : currentSource.listFiles(filter)) {
                if (sourceFile.isDirectory()) {
//                    if (sourceFile.getName().equals(VERSIONS_FOLDER_NAME)) {
//                        continue;
//                    }
                    saveFolder(sourceRoot, targetRoot, sourceFile, modifiedFiles);
                } else {
                    if (sourceFile.getName().equals("import.zip") || sourceFile.getName().equals("MANIFEST.MF") || sourceFile.getName().equals("deployed.xml")) {
                        continue;
                    }
                    fileInputStream = new FileInputStream(sourceFile);
                    File targetFile = new File(targetRoot, sourceFile.getPath().substring(sourceRoot.getPath().length() + 1));
                    if (saveFile(fileInputStream, targetFile)) {
                        modifiedFiles.add(targetFile);
                    }
                    IOUtils.closeQuietly(fileInputStream);
                }
            }
        } finally {
            if (fileInputStream != null) {
                IOUtils.closeQuietly(fileInputStream);
            }
        }
    }

    private void setDependenciesInPom(File sources, List<String> dependencies) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element root = document.getRootElement();
            // todo : try to use xpath or a better way to get depends node
            root = (Element) root.elements("build").get(0);
            root = (Element) root.elements("plugins").get(0);
            root = (Element) root.elements("plugin").get(0);
            root = (Element) root.elements("configuration").get(0);
            root = (Element) root.elements("archive").get(0);
            root = (Element) root.elements("manifestEntries").get(0);
            root = (Element) root.elements("depends").get(0);
            root.setText(StringUtils.join(dependencies, ","));
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
            saveFile(new FileInputStream(modifiedPom), pom);
            modifiedPom.delete();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setSCMConfigInPom(File sources, String scmURI, String scmType) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element root = document.getRootElement();
            Iterator it = root.elementIterator("scm");
            if (!it.hasNext()) {
                Element scm = root.addElement("scm");
                scm.addElement("connection").addText("scm:" + scmType + ":" + scmURI);
                scm.addElement("developerConnection").addText("scm:" + scmType + ":" + scmURI);
                scm.addElement("url").addText(scmURI);
                List list = root.elements();
                list.remove(scm);
                list.add(list.indexOf(root.elementIterator("description").next()) + 1, scm);
            }
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
            writer.write(document);
            writer.close();
            saveFile(new FileInputStream(modifiedPom), pom);
            modifiedPom.delete();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean saveFile(InputStream source, File target) throws IOException, PatchFailedException {
        if (!target.exists()) {
            target.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(target);
            IOUtils.copy(source, output);
            output.close();
            return true;
        } else {
            List<String> targetContent = FileUtils.readLines(target);
            if (!isBinary(targetContent)) {
                List<String> sourceContent = IOUtils.readLines(source, "UTF-8");
                Patch patch = DiffUtils.diff(targetContent, sourceContent, new MyersDiff(new Equalizer() {
                    public boolean equals(Object o, Object o1) {
                        String s1 = (String) o;
                        String s2 = (String) o1;
                        return s1.trim().equals(s2.trim());
                    }
                }));
                if (!patch.getDeltas().isEmpty()) {
                    targetContent = (List<String>) patch.applyTo(targetContent);
                    FileUtils.writeLines(target, "UTF-8", targetContent, "\n");
                    return true;
                }
            } else {
                byte[] sourceArray = IOUtils.toByteArray(source);
                byte[] targetArray = IOUtils.toByteArray(new FileInputStream(target));
                if (!Arrays.equals(sourceArray, targetArray)) {
                    FileOutputStream output = new FileOutputStream(target);
                    IOUtils.write(sourceArray, output);
                    output.close();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isBinary(List<String> text) {
        for (String s : text) {
            if (s.contains("\u0000")) {
                return true;
            }
        }
        return false;
    }

    private List<String> getDependencies(JCRNodeWrapper node) throws RepositoryException {
        List<String> dependencies = new ArrayList<String>();
        if (node.hasProperty("j:dependencies")) {
            Value[] deps = node.getProperty("j:dependencies").getValues();
            for (Value dep : deps) {
                dependencies.add(dep.getString());
            }
        }
        return dependencies;
    }

    public void updateDependencies(JahiaTemplatesPackage pack, List<String> depends) {
        pack.getDepends().clear();
        pack.getDepends().addAll(depends);
        templatePackageRegistry.computeDependencies(pack);
        applicationEventPublisher.publishEvent(new ModuleDependenciesEvent(pack.getRootFolder(), this));
    }


    /**
     * **************************************************************************************************************
     * Module installation on sites
     * ***************************************************************************************************************
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

        Set<String> modules = new TreeSet<String>();

        if (includeDirectDependencies) {
            modules.addAll(installedModules);
        }

        if (includeTransitiveDependencies) {
            for (String m : installedModules) {
                JahiaTemplatesPackage pkg = getTemplatePackageByFileName(m);
                pkg = pkg != null ? pkg : getTemplatePackage(m);
                if (pkg != null) {
                    for (JahiaTemplatesPackage deps : pkg.getDependencies()) {
                        if (!installedModules.contains(deps.getRootFolder())) {
                            modules.add(deps.getRootFolder());
                        }
                    }
                }
            }
        }

        List<JahiaTemplatesPackage> packages = new LinkedList<JahiaTemplatesPackage>();
        for (String m : modules) {
            JahiaTemplatesPackage pkg = getTemplatePackageByFileName(m);
            pkg = pkg != null ? pkg : getTemplatePackage(m);
            if (pkg != null) {
                packages.add(pkg);
            }
        }

        return packages.isEmpty() ? Collections.<JahiaTemplatesPackage>emptyList() : packages;
    }

    public void autoInstallModulesToSites(JahiaTemplatesPackage module, JCRSessionWrapper session)
            throws RepositoryException {
        if (module.getAutoDeployOnSite() != null) {
            if ("system".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModule(module, "/sites/systemsite", session);
                }
            } else if ("all".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModuleOnAllSites(module, session, null);
                }
            }
        }

        List<JCRNodeWrapper> sites = new ArrayList<JCRNodeWrapper>();
        NodeIterator ni = session.getNode("/sites").getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (next.hasProperty("j:installedModules")) {
                Value[] v = next.getProperty("j:installedModules").getValues();
                for (Value value : v) {
                    if (value.getString().equals(module.getRootFolder())) {
                        sites.add(next);
                    }
                }
            }
        }
        if (!sites.isEmpty()) {
            installModuleOnAllSites(module, session, sites);
        }
    }

    public void installModuleOnAllSites(JahiaTemplatesPackage module, JCRSessionWrapper sessionWrapper, List<JCRNodeWrapper> sites) throws RepositoryException {
        if (sites == null) {
            sites = new ArrayList<JCRNodeWrapper>();
            NodeIterator ni = sessionWrapper.getNode("/sites").getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                sites.add(next);
            }
        }

        JCRNodeWrapper tpl = sessionWrapper.getNode("/modules/" + module.getRootFolderWithVersion());
        for (JCRNodeWrapper site : sites) {
            if (tpl.hasProperty("j:moduleType") && MODULE_TYPE_TEMPLATES_SET.equals(tpl.getProperty("j:moduleType").getString())) {
                if (tpl.getName().equals(site.getResolveSite().getTemplateFolder())) {
                    installModule(module, site.getPath(), sessionWrapper);
                }
            } else {
                installModule(module, site.getPath(), sessionWrapper);
            }
        }
    }

    public void installModule(final String module, final String sitePath, String username)
            throws RepositoryException {
        installModule(templatePackageRegistry.lookupByFileName(module), sitePath, username);
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, String username)
            throws RepositoryException {
        installModules(Arrays.asList(module), sitePath, username);
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath, String username)
            throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecuteWithSystemSession(username, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        installModules(modules, sitePath, session);
                        session.save();
                        return null;
                    }
                });
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        installModules(Arrays.asList(module), sitePath, session);
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        final JCRSiteNode siteNode = (JCRSiteNode) session.getNode(sitePath);

        HashMap<String, List<String>> references = new HashMap<String, List<String>>();
        for (JahiaTemplatesPackage module : modules) {
            logger.info("Deploying " + modules + " on " + sitePath);
            JCRNodeWrapper moduleNode = null;
            try {
                moduleNode = session.getNode("/modules/" + module.getRootFolder());

                String moduleName = moduleNode.getName();

                synchro(moduleNode, siteNode, session, moduleName, references);

                ReferencesHelper.resolveCrossReferences(session, references);

                addDependencyValue(moduleNode, siteNode, "j:installedModules");
                logger.info("Done deploying " + modules + " on " + sitePath);
            } catch (PathNotFoundException e) {
                logger.warn("Cannot find module for path {}. Skipping deployment to site {}.",
                        module, sitePath);
                return;
            }

        }

        siteService.updateSite(siteNode);
        applicationEventPublisher.publishEvent(new ModuleDeployedOnSiteEvent(sitePath, JahiaTemplateManagerService.class.getName()));
    }

    private boolean addDependencyValue(JCRNodeWrapper originalNode, JCRNodeWrapper destinationNode, String propertyName) throws RepositoryException {
//        Version v = templatePackageRegistry.lookupByFileName(originalNode.getName()).getLastVersion();
        String newStringValue = originalNode.getName();
        Value newValue = originalNode.getSession().getValueFactory().createValue(newStringValue);
        if (destinationNode.hasProperty(propertyName)) {
            JCRPropertyWrapper installedModules = destinationNode.getProperty(propertyName);
            List<Value> newValues = new ArrayList<Value>();
            Value[] values = installedModules.getValues();
            for (Value value : values) {
                String stringValue = value.getString();
                if (stringValue.equals(newStringValue)) {
                    return true;
                }
                if (!stringValue.startsWith(originalNode.getName() + ":")) {
                    newValues.add(value);
                } else {
                    newValues.add(newValue);
                }
            }

            if (!newValues.contains(newValue)) {
                newValues.add(newValue);
            }

            destinationNode.getSession().checkout(destinationNode);

            installedModules.setValue(newValues.toArray(new Value[newValues.size()]));
        } else {
            destinationNode.setProperty(propertyName, new String[]{newStringValue});
        }
        return false;
    }

    public void synchro(JCRNodeWrapper source, JCRNodeWrapper destinationNode, JCRSessionWrapper session, String moduleName,
                        Map<String, List<String>> references) throws RepositoryException {
        if (source.isNodeType("jnt:module")) {
            source = source.getNode(getTemplatePackageByFileName(source.getName()).getVersion().toString());
        }
        if (source.isNodeType("jnt:moduleVersion")) {
            session.getUuidMapping().put(source.getIdentifier(), destinationNode.getIdentifier());
            NodeIterator ni = source.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (child.isNodeType("jnt:versionInfo")) {
                    continue;
                }
                JCRNodeWrapper node;
                boolean newNode = false;
                String childName = child.getName();
                if (destinationNode.hasNode(childName)) {
                    node = destinationNode.getNode(childName);
                } else {
                    session.checkout(destinationNode);
                    String primaryNodeTypeName = child.getPrimaryNodeTypeName();
                    node = destinationNode.addNode(childName, primaryNodeTypeName);
                    newNode = true;
                }

                if (!child.isNodeType("jnt:templatesFolder")) {
                    templatesSynchro(child, node, session, references, newNode, false, true, moduleName, child.isNodeType("jnt:componentFolder"));
                }
            }
        }
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, Map<String, List<String>> references, boolean doUpdate, boolean doRemove, boolean doChildren, String moduleName, boolean inTemplatesFolder)
            throws RepositoryException {
        if ("j:acl".equals(destinationNode.getName())) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Synchronizing node : " + destinationNode.getPath() + ", update=" + doUpdate + "/remove=" + doRemove + "/children=" + doChildren);
        }

        // Set for jnt:template nodes : declares if the template was originally created with that module, false otherwise
//        boolean isCurrentModule = (!destinationNode.hasProperty("j:moduleTemplate") && moduleName == null) || (destinationNode.hasProperty("j:moduleTemplate") && destinationNode.getProperty("j:moduleTemplate").getString().equals(moduleName));

        session.checkout(destinationNode);

        final Map<String, String> uuidMapping = session.getUuidMapping();

        ExtendedNodeType[] mixin = source.getMixinNodeTypes();
        List<ExtendedNodeType> destMixin = Arrays.asList(destinationNode.getMixinNodeTypes());
        for (ExtendedNodeType aMixin : mixin) {
            if (!destMixin.contains(aMixin)) {
                destinationNode.addMixin(aMixin.getName());
            }
        }

        uuidMapping.put(source.getIdentifier(), destinationNode.getIdentifier());

        List<String> names = new ArrayList<String>();

        if (doUpdate) {
            if (source.hasProperty(Constants.JCR_LANGUAGE) && (!destinationNode.hasProperty(Constants.JCR_LANGUAGE) ||
                    (!destinationNode.getProperty(Constants.JCR_LANGUAGE).getString().equals(source.getProperty(Constants.JCR_LANGUAGE).getString())))) {
                destinationNode.setProperty(Constants.JCR_LANGUAGE, source.getProperty(Constants.JCR_LANGUAGE).getString());
            }

            PropertyIterator props = source.getProperties();

            while (props.hasNext()) {
                Property property = props.nextProperty();
                names.add(property.getName());
                try {
                    if (!property.getDefinition().isProtected() &&
                            !Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                        if (property.getType() == PropertyType.REFERENCE ||
                                property.getType() == PropertyType.WEAKREFERENCE) {
                            if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                                if (!destinationNode.hasProperty(property.getName()) ||
                                        !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(), property.getValues())) {
                                    destinationNode.setProperty(property.getName(), new Value[0]);
                                    Value[] values = property.getValues();
                                    for (Value value : values) {
                                        keepReference(destinationNode, references, property, value.getString());
                                    }
                                }
                            } else {
                                if (!destinationNode.hasProperty(property.getName()) ||
                                        !destinationNode.getProperty(property.getName()).getValue().equals(property.getValue())) {
                                    keepReference(destinationNode, references, property, property.getValue().getString());
                                }
                            }
                        } else if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            if (!destinationNode.hasProperty(property.getName()) ||
                                    !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(), property.getValues())) {
                                destinationNode.setProperty(property.getName(), property.getValues());
                            }
                        } else if (!destinationNode.hasProperty(property.getName()) ||
                                !destinationNode.getProperty(property.getName()).getValue().equals(property.getValue())) {
                            destinationNode.setProperty(property.getName(), property.getValue());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
                }
            }

            PropertyIterator pi = destinationNode.getProperties();
            while (pi.hasNext()) {
                JCRPropertyWrapper oldChild = (JCRPropertyWrapper) pi.next();
                if (!oldChild.getDefinition().isProtected()) {
                    if (!names.contains(oldChild.getName()) && !oldChild.getName().equals("j:published") && !oldChild.getName().equals(Constants.JAHIA_MODULE_TEMPLATE) && !oldChild.getName().equals("j:sourceTemplate")) {
                        oldChild.remove();
                    }
                }
            }

            mixin = destinationNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                if (!source.isNodeType(aMixin.getName())) {
                    destinationNode.removeMixin(aMixin.getName());
                }
            }
        }

        NodeIterator ni = source.getNodes();

        names.clear();

        JCRNodeWrapper templatesDestinationNode = destinationNode;
        if (source.isNodeType("jnt:templatesFolder") && source.hasProperty("j:rootTemplatePath")) {
            String rootTemplatePath = source.getProperty("j:rootTemplatePath").getString();
            if (rootTemplatePath.startsWith("/")) {
                rootTemplatePath = rootTemplatePath.substring(1);
            }
            if (!rootTemplatePath.equals("")) {
                templatesDestinationNode = templatesDestinationNode.getNode(rootTemplatePath);
            }
            templatesDestinationNode.getSession().checkout(templatesDestinationNode);
        }

        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            if (child.isNodeType(ComponentRegistry.JMIX_STUDIO_ONLY)
                    && (child.isNodeType(ComponentRegistry.JNT_COMPONENT) || child
                    .isNodeType(ComponentRegistry.JNT_COMPONENT_FOLDER))) {
                // we do not deploy components which are dedicated for the Studio only
                continue;
            }
            boolean isTemplateNode = child.isNodeType("jnt:template");
            boolean isPageNode = child.isNodeType("jnt:page");

            JCRNodeWrapper currentDestination = isTemplateNode ? templatesDestinationNode : destinationNode;

            if (doChildren || isTemplateNode) {
                names.add(child.getName());

                boolean currentModule = false;
                boolean newNode = false;
                JCRNodeWrapper node = null;
                if (currentDestination.hasNode(child.getName())) {
                    node = currentDestination.getNode(child.getName());
                    currentModule = (!node.hasProperty(Constants.JAHIA_MODULE_TEMPLATE) && moduleName == null) || (node.hasProperty(Constants.JAHIA_MODULE_TEMPLATE) && node.getProperty(Constants.JAHIA_MODULE_TEMPLATE).getString().equals(moduleName));
                } else {
                    // Handle template move
                    PropertyIterator ref = child.getWeakReferences(Constants.JAHIA_SOURCE_TEMPLATE);
                    while (ref.hasNext()) {
                        JCRPropertyWrapper next = (JCRPropertyWrapper) ref.next();
                        if (next.getPath().startsWith(destinationNode.getAncestor(3).getPath())) {
                            session.move(next.getParent().getPath(), currentDestination.getPath() + "/" + child.getName());
                            node = currentDestination.getNode(child.getName());
                            break;
                        }
                    }
                    if (node == null) {
                        node = currentDestination.addNode(child.getName(), child.getPrimaryNodeTypeName());
                        newNode = true;
                        if (moduleName != null && node.isNodeType("jnt:template")) {
                            node.setProperty(Constants.JAHIA_MODULE_TEMPLATE, moduleName);
                            node.setProperty(Constants.JAHIA_SOURCE_TEMPLATE, child);
                            currentModule = true;
                        }
                    }
                }
                if (isTemplateNode) {
                    templatesSynchro(child, node, session, references, currentModule, currentModule, currentModule, moduleName, inTemplatesFolder);
                } else {
                    templatesSynchro(child, node, session, references, inTemplatesFolder || newNode, doRemove, doChildren && !(isPageNode && !newNode), moduleName, inTemplatesFolder);
                }
            }
        }
        if (doRemove) {
            logger.debug("Remove unwanted child of : " + destinationNode.getPath());
            ni = destinationNode.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper oldDestChild = (JCRNodeWrapper) ni.next();
                if (!names.contains(oldDestChild.getName()) &&
                        ((!oldDestChild.isNodeType("jnt:template")) ||
                                (!oldDestChild.hasProperty(Constants.JAHIA_MODULE_TEMPLATE) && moduleName == null) ||
                                (oldDestChild.hasProperty(Constants.JAHIA_MODULE_TEMPLATE) && oldDestChild.getProperty(Constants.JAHIA_MODULE_TEMPLATE).getString().equals(moduleName)))) {
                    logger.debug(oldDestChild.getPath());
                    if (oldDestChild.hasProperty("j:sourceTemplate")) {
                        try {
                            oldDestChild.getProperty("j:sourceTemplate").getNode();
                            // Do not delete if source still exists somewhere
                            continue;
                        } catch (ItemNotFoundException e) {
                        }
                    }
                    oldDestChild.remove();
                }
            }
        }
        if (doUpdate) {
            List<String> destNames = new ArrayList<String>();
            ni = destinationNode.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper oldChild = (JCRNodeWrapper) ni.next();
                destNames.add(oldChild.getName());
            }
            if (destinationNode.getPrimaryNodeType().hasOrderableChildNodes() && !names.equals(destNames)) {
                Collections.reverse(names);
                String previous = null;
                for (String name : names) {
                    destinationNode.orderBefore(name, previous);
                    previous = name;
                }
            }
        }
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property,
                               String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }


    /*****************************************************************************************************************
     * Registry access
     *****************************************************************************************************************/

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailableTemplatePackages() {
        return templatePackageRegistry.getAvailablePackages();
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
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage getTemplatePackageByFileName(String fileName) {
        return templatePackageRegistry.lookupByFileName(fileName);
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
     * Returns a sorted set of all available template packages having templates for a module.
     *
     * @return a sorted set of all available template packages
     * @deprecated since Jahia 6.6 use {@link #getAvailableTemplatePackagesForModule(String)} instead
     */
    @Deprecated
    public Set<JahiaTemplatesPackage> getSortedAvailableTemplatePackagesForModule(String moduleName, final RenderContext context) {
        return getAvailableTemplatePackagesForModule(moduleName);
    }

    /**
     * Returns a set of all available template packages having templates for a module.
     *
     * @return a set of all available template packages
     */
    public Set<JahiaTemplatesPackage> getAvailableTemplatePackagesForModule(String moduleName) {
        Set<JahiaTemplatesPackage> r = templatePackageRegistry.getPackagesPerModule().get(moduleName);
        if (r == null) {
            return Collections.emptySet();
        }
        return r;
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified name is not
     * registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
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
            public Object transform(Object input) {
                return templatePackageRegistry.lookupByNodeName(String.valueOf(input));
            }
        });
    }

    /**
     * Returns a set of existing template sets that are available for site creation.
     *
     * @return a set of existing template sets that are available for site creation
     */
    public Set<String> getTemplateSetNames() {
        try {
            return JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Set<String>>() {
                        public Set<String> doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            QueryManager qm = session.getWorkspace().getQueryManager();
                            if (qm == null) {
                                return Collections.emptySet();
                            }
                            Set<String> templateSets = new TreeSet<String>();
                            for (NodeIterator nodes = qm
                                    .createQuery(
                                            "select * from [jnt:module]"
                                                    + " where ischildnode('/modules')"
                                                    + " and name() <> 'templates-system'"
                                                    + " and [j:moduleType] = 'templatesSet'",
                                            Query.JCR_SQL2).execute().getNodes(); nodes.hasNext(); ) {
                                Node node = nodes.nextNode();
                                if (getTemplatePackageByFileName(node.getName()) != null) {
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

    public JahiaTemplatesPackage activateModuleVersion(String rootFolder, ModuleVersion version, JCRSessionWrapper session) throws RepositoryException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByFileNameAndVersion(rootFolder, version);

        autoInstallModulesToSites(module, session);
        templatePackageRegistry.activateModuleVersion(module);
        return module;
    }

    public void undeployModule(String rootFolder, String version, JCRSessionWrapper session) throws RepositoryException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupByFileNameAndVersion(rootFolder, new ModuleVersion(version));
        if (!pack.isActiveVersion() && !pack.isLastVersion()) {
            templatePackageDeployer.undeployModule(pack, session);
        }
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
            present = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
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
                            String.valueOf(System.currentTimeMillis() - timer)});
        }

        return present;
    }

    private boolean isTemplatePresent(String templateName, Set<String> templateSetNames,
                                      JCRSessionWrapper session) throws InvalidQueryException, ValueFormatException,
            PathNotFoundException, RepositoryException {
        boolean found = false;
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        if (queryManager == null) {
            return true;
        }

        StringBuilder query = new StringBuilder(256);
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
        for (NodeIterator nodes = queryManager.createQuery(query.toString(), Query.JCR_SQL2)
                .execute().getNodes(); nodes.hasNext(); ) {
            JCRNodeWrapper node = (JCRNodeWrapper) nodes.nextNode();
            Matcher matcher = TEMPLATE_PATTERN.matcher(node.getPath());
            String pathToCheck = matcher.matches() ? matcher.group(1) : null;
            if (StringUtils.isEmpty(pathToCheck)) {
                continue;
            }
            pathToCheck = "/" + pathToCheck;
            if (templateName.equals(pathToCheck)) {
                // got it
                found = true;
                break;
            } else {
                String basePath = null;
                JCRNodeWrapper folder = JCRContentUtils
                        .getParentOfType(node, "jnt:templatesFolder");
                if (folder != null && folder.hasProperty("j:rootTemplatePath")) {
                    basePath = folder.getProperty("j:rootTemplatePath").getString();
                }
                if (StringUtils.isNotEmpty(basePath) && !"/".equals(basePath)
                        && templateName.equals(basePath + pathToCheck)) {
                    // matched it considering the base path
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

// -------------------------- INNER CLASSES --------------------------

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

    public static class ModuleDeployedOnSiteEvent extends ApplicationEvent {
        private static final long serialVersionUID = -6693201714720533228L;
        private String targetSitePath;

        public ModuleDeployedOnSiteEvent(String targetSitePath, Object source) {
            super(source);
            this.targetSitePath = targetSitePath;
        }

        public String getTargetSitePath() {
            return targetSitePath;
        }
    }

    public static class ModuleDependenciesEvent extends ApplicationEvent {
        private static final long serialVersionUID = -6693201714720533228L;
        private String moduleName;

        public ModuleDependenciesEvent(String moduleName, Object source) {
            super(source);
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return moduleName;
        }
    }

}
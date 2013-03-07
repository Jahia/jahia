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

import com.google.common.collect.ImmutableSet;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
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
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
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
import org.jahia.utils.i18n.ResourceBundles;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Template and template set deployment and management service.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaTemplateManagerService extends JahiaService implements ApplicationEventPublisherAware, ApplicationListener<ApplicationEvent> {

    public static final String MODULE_TYPE_JAHIAPP = "jahiapp";

    public static final String MODULE_TYPE_MODULE = "module";

    public static final String MODULE_TYPE_PROFILE_MODULE = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_PROFILE_MODULE;

    public static final String MODULE_TYPE_SYSTEM = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_SYSTEM;

    public static final String MODULE_TYPE_TEMPLATES_SET = org.jahia.ajax.gwt.client.util.Constants.MODULE_TYPE_TEMPLATES_SET;

    private static final MyersDiff MYERS_DIFF = new MyersDiff(new Equalizer() {
        public boolean equals(Object o, Object o1) {
            String s1 = (String) o;
            String s2 = (String) o1;
            return s1.trim().equals(s2.trim());
        }
    });

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("/modules/[^/]*/templates/(.*)");

    private static Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerService.class);

    private static Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

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

    public TemplatePackageDeployer getTemplatePackageDeployer() {
        return templatePackageDeployer;
    }

    public TemplatePackageRegistry getTemplatePackageRegistry() {
        return templatePackageRegistry;
    }

    public void start() throws JahiaInitializationException {
        // do nothing
    }

    public void stop() throws JahiaException {
        logger.info("Stopping JahiaTemplateManagerService ...");

        templatePackageRegistry.reset();

        logger.info("... JahiaTemplateManagerService stopped successfully");
    }

    public void onApplicationEvent(final ApplicationEvent event) {
        if (event instanceof JahiaContextLoaderListener.RootContextInitializedEvent) {
            if (SettingsBean.getInstance().isProcessingServer()) {
                templatePackageDeployer.initializeModulesContent();
            }
        } else if (event instanceof TemplatePackageRedeployedEvent) {
            // flush resource bundle cache
            ResourceBundles.flushCache();
            NodeTypeRegistry.getInstance().flushLabels();
        }
    }

    /**
     * **************************************************************************************************************
     * Module creation / compilation
     * ***************************************************************************************************************
     */

    public JCRNodeWrapper checkoutModule(File sources, String scmURI, String branchOrTag, String moduleName, String version, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        String tempName = null;

        if (sources == null) {
            tempName = UUID.randomUUID().toString();
            sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources", tempName);
        }

        if (sources.exists()) {
            throw new IOException("Sources folder already exist");
        }

        sources.getParentFile().mkdirs();

        try {
            SourceControlManagement scm = SourceControlManagement.checkoutRepository(sources, scmURI, branchOrTag);
            File modulePath = null;
            File pom = null;
            if (!StringUtils.isEmpty(moduleName)) {
                // Find the root folder of the module inside the repository, if in a subfolder
                Collection<File> files = FileUtils.listFiles(sources, new NameFileFilter("pom.xml"), TrueFileFilter.INSTANCE);
                for (File file : files) {
                    SAXReader reader = new SAXReader();
                    pom = file;
                    Document document = reader.read(pom);
                    Element n = (Element) document.getRootElement().elementIterator("artifactId").next();
                    String artifactId = n.getText();

                    if (moduleName.equals(artifactId)) {
                        if (document.getRootElement().elementIterator("version").hasNext()) {
                            n = (Element) document.getRootElement().elementIterator("version").next();
                        } else if (document.getRootElement().elementIterator("parent").hasNext()) {
                            n = (Element) document.getRootElement().elementIterator("parent").next();
                            n = (Element) n.elementIterator("version").next();
                        }
                        if (version != null) {
                            if (n != null && version.equals(n.getText())) {
                                modulePath = pom.getParentFile();
                                break;
                            }
                        } else {
                            if (n != null) {
                                version = n.getText();
                            }
                            modulePath = pom.getParentFile();
                            break;
                        }
                    }
                }
            } else {
                SAXReader reader = new SAXReader();
                pom = new File(sources, "pom.xml");
                Document document = reader.read(pom);
                Element n = (Element) document.getRootElement().elementIterator("artifactId").next();
                moduleName = n.getText();
                if (document.getRootElement().elementIterator("version").hasNext()) {
                    n = (Element) document.getRootElement().elementIterator("version").next();
                } else if (document.getRootElement().elementIterator("parent").hasNext()) {
                    n = (Element) document.getRootElement().elementIterator("parent").next();
                    n = (Element) n.elementIterator("version").next();
                }
                if (n != null) {
                    version = n.getText();
                }

                modulePath = sources;
            }

            if (modulePath == null) {
                FileUtils.deleteDirectory(sources);
                logger.error("Sources were not found for " + moduleName + "  " + version + " in " + scmURI + " " + branchOrTag);
                throw new IOException("Sources were not found for " + moduleName + "  " + version + " in " + scmURI + " " + branchOrTag);
            }

            if (tempName != null) {
                File newPath = new File(sources.getParentFile(), moduleName + "_" + version);
                int i = 0;
                while (newPath.exists()) {
                    newPath = new File(sources.getParentFile(), moduleName + "_" + version + "_" + (++i));
                }

                FileUtils.moveDirectory(sources, newPath);
                modulePath = new File(modulePath.getPath().replace(sources.getPath(), newPath.getPath()));
                sources = newPath;
                scm = SourceControlManagement.getSourceControlManagement(sources);
            }

            if (sources.equals(modulePath)) {
                setSCMConfigInPom(sources, scmURI);
            }

            JahiaTemplatesPackage pack = compileAndDeploy(moduleName, modulePath, session);
            if (pack != null) {
                JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
                pack.setSourceControl(scm);
                setSourcesFolderInPackageAndNode(pack, modulePath, node);
                session.save();

                // flush resource bundle cache
                ResourceBundles.flushCache();
                NodeTypeRegistry.getInstance().flushLabels();

                return node;
            } else {
                FileUtils.deleteDirectory(sources);
            }
        } catch (BundleException e) {
            FileUtils.deleteDirectory(sources);
            throw e;
        } catch (RepositoryException e) {
            FileUtils.deleteDirectory(sources);
            throw e;
        } catch (IOException e) {
            FileUtils.deleteDirectory(sources);
            throw e;
        } catch (DocumentException e) {
            FileUtils.deleteDirectory(sources);
            throw new IOException(e);
        }

        return null;
    }

    public JCRNodeWrapper createModule(String moduleName, String moduleType, File sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
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
                "-DarchetypeCatalog=https://devtools.jahia.com/nexus/content/repositories/jahia-snapshots/archetype-catalog.xml,local",
                "-DarchetypeGroupId=org.jahia.archetypes",
                "-DarchetypeArtifactId=jahia-" + moduleType + "-archetype",
                "-DmoduleName=" + moduleName,
                "-DartifactId=" + moduleName,
                "-DjahiaPackageVersion=" + Constants.JAHIA_PROJECT_VERSION,
                "-DinteractiveMode=false"};

        int ret = cli.doMain(archetypeParams, sources.getPath(),
                System.out, System.err);
        if (ret > 0) {
            logger.error("Maven archetype call returned " + ret);
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

//        if (scmURI != null) {
//            setSCMConfigInPom(path, scmURI);
//            try {
//                SourceControlManagement.createNewRepository(path, scmURI);
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//            }
//        }

        JahiaTemplatesPackage pack = compileAndDeploy(moduleName, path, session);

        JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        setSourcesFolderInPackageAndNode(pack, path, node);
        session.save();

        return node;
    }

    public void duplicateModule(String moduleName, String moduleType, final String sourceModule) {
        /*final File tmplRootFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), moduleName + "/1.0-SNAPSHOT");
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
        }*/
    }

    public JahiaTemplatesPackage deployModule(File warFile, JCRSessionWrapper session) throws RepositoryException {
        return templatePackageDeployer.deployModule(warFile, session);
    }

    public JahiaTemplatesPackage compileAndDeploy(final String moduleName, File sources, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {
        CompiledModuleInfo moduleInfo = compileModule(moduleName, sources);
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            if (bundle.getSymbolicName().equals(moduleName) && bundle.getHeaders().get("Implementation-Version") != null && bundle.getHeaders().get("Implementation-Version").toString().equals(moduleInfo.getVersion())) {
                // Update existing module
                bundle.update(new FileInputStream(moduleInfo.getFile()));
                return templatePackageRegistry.lookupByFileNameAndVersion(moduleInfo.getModuleName(), new ModuleVersion(moduleInfo.getVersion()));
            }
        }
        // No existing module found, deploy new one
        Bundle bundle = FrameworkService.getBundleContext().installBundle(moduleInfo.getFile().toURI().toString(), new FileInputStream(moduleInfo.getFile()));
        bundle.start();
        return templatePackageRegistry.lookupByFileNameAndVersion(moduleInfo.getModuleName(), new ModuleVersion(moduleInfo.getVersion()));
    }

    public CompiledModuleInfo compileModule(final String moduleName, File sources) throws IOException {
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
                throw new IOException("No version found in pom.xml file");
            }
            String version = n.getText();

            String[] installParams = {"clean", "install"};
            int r = cli.doMain(installParams, sources.getPath(), System.out, System.err);
            if (r > 0) {
                logger.error("Compilation error, returned status " + r);
                throw new IOException("Compilation error, status "+r);
            }
            File file = new File(sources.getPath() + "/target/" + moduleName + "-" + version + ".war");
            if (!file.exists()) {
                file = new File(sources.getPath() + "/target/" + moduleName + "-" + version + ".jar");
            }
            if (file.exists()) {
                return new CompiledModuleInfo(file, moduleName, version);
            } else {
                throw new IOException("Cannot find a module archive to deploy in folder "+file.getParentFile().getAbsolutePath());
            }
        } catch (DocumentException e) {
            logger.error(e.getMessage(), e);
            throw new IOException("Cannot parse pom file",e);
        }
    }

    public JCRNodeWrapper installFromSources(File sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        if (!sources.exists()) {
            return null;
        }

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new File(sources, "pom.xml"));
            Element n = (Element) document.getRootElement().elementIterator("artifactId").next();
            String moduleName = n.getText();

            JahiaTemplatesPackage pack = compileAndDeploy(moduleName, sources, session);
            pack.setSourcesFolder(sources);
            JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
            session.save();

            return node;
        } catch (DocumentException e) {
            throw new IOException("Cannot parse pom file",e);
        }
    }

    public File getSources(JahiaTemplatesPackage pack, JCRSessionWrapper session) throws RepositoryException {
        if (pack.getSourcesFolder() != null) {
            return pack.getSourcesFolder();
        }
        JCRNodeWrapper n = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        if (n.hasNode("j:versionInfo")) {
            JCRNodeWrapper vi = n.getNode("j:versionInfo");
            if (vi.hasProperty("j:sourcesFolder")) {
                File sources = new File(vi.getProperty("j:sourcesFolder").getString());

                if (checkValidSources(pack, sources)) {
                    pack.setSourcesFolder(sources);
//                    templatePackageRegistry.mountSourcesProvider(pack);
                    return sources;
                }
            }
        }
        return null;
    }

    public void sendToSourceControl(String moduleName, String scmURI, String scmType, JCRSessionWrapper session) throws Exception {
        JahiaTemplatesPackage pack = getTemplatePackageByFileName(moduleName);
        String fullUri = "scm:" + scmType + ":" + scmURI;
        final File sources = getSources(pack, session);

        String tempName = UUID.randomUUID().toString();
        final File tempSources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources", tempName);
        FileUtils.moveDirectory(sources, tempSources);
        SourceControlManagement scm = SourceControlManagement.checkoutRepository(sources, fullUri, null);
        final List<File> modifiedFiles = new ArrayList<File>();
        FileUtils.copyDirectory(tempSources, sources, new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getPath().startsWith(tempSources.getPath() + "/target") || pathname.getPath().startsWith(tempSources.getPath() + "/.git")) {
                    return false;
                }
                modifiedFiles.add(new File(pathname.getPath().replace(tempSources.getPath(), sources.getPath())));
                return true;
            }
        });

        scm.setModifiedFile(modifiedFiles);

        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        pack.setSourceControl(scm);
        setSCMConfigInPom(sources, fullUri);
        JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        templateManagerService.setSourcesFolderInPackageAndNode(pack, sources, node);
        session.save();
        scm.commit("Initial commit");
    }


    public boolean checkValidSources(JahiaTemplatesPackage pack, File sources) {
        SAXReader reader = new SAXReader();
        if (!new File(sources,"src/main/resources").exists() && !new File(sources,"src/main/webapp").exists()) {
            return false;
        }
        File pom = new File(sources, "pom.xml");
        if (pom.exists()) {
            try {
                Document document = reader.read(pom);
                Element element = document.getRootElement();
                Iterator iterator = element.elementIterator("version");
                if (iterator.hasNext()) {
                    element = (Element) iterator.next();
                } else {
                    element = (Element) element.elements("parent").get(0);
                    element = (Element) element.elements("version").get(0);
                }
                String sourceVersion = element.getText();
                if (sourceVersion.equals(pack.getVersion().toString())) {
                    return true;
                }
            } catch (DocumentException e) {
                logger.error("Cannot parse pom file", e);
            }
        }
        return false;
    }

    public File releaseModule(String moduleName, String nextVersion, JCRSessionWrapper session) throws RepositoryException, IOException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupByFileName(moduleName);
        if (pack.getVersion().isSnapshot() && nextVersion != null) {
            File sources = getSources(pack, session);
            if (sources != null) {
                JCRNodeWrapper vi = session.getNode("/modules/" + pack.getRootFolderWithVersion() + "/j:versionInfo");
                regenerateImportFile(moduleName, sources, session);

                if (vi.hasProperty("j:scmURI")) {
                    SourceControlManagement scm = null;
                    try {
                        scm = SourceControlManagement.getSourceControlManagement(sources);
                        if (scm != null) {
                            scm.commit("Release");
                        }
                        return releaseModule(pack, nextVersion, sources, vi.getProperty("j:scmURI").getString(), session);
                    } catch (Exception e) {
                        logger.error("Cannot get SCM", e);
                    }
                } else {
                    return releaseModule(pack, nextVersion, sources, null, session);
                }
            }
        }
        return null;
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

                    File oldWar = new File(settingsBean.getJahiaModulesDiskPath(), module + "-" + lastVersion + ".war");
                    if (oldWar.exists()) {
                        oldWar.delete();
                    }

                    generatedWar = new File(sources.getPath() + "/target/checkout/target/" + module.getRootFolder() + "-" + releaseVersion + ".war");
                } else {
                    versionElement.setText(releaseVersion);
                    File modifiedPom = new File(sources, "pom-modified.xml");
                    XMLWriter writer = null;
                    try {
                        writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
                        writer.write(document);
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                    FileInputStream source = new FileInputStream(modifiedPom);
                    try {
                        saveFile(source, pom);
                    } finally {
                        IOUtils.closeQuietly(source);
                    }

                    generatedWar = compileModule(module.getRootFolder(), sources).getFile();

                    versionElement.setText(nextVersion);
                    writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
                    try {
                        writer.write(document);
                    } finally {
                        writer.close();
                    }
                    source = new FileInputStream(modifiedPom);
                    try {
                        saveFile(source, pom);
                    } finally {
                        IOUtils.closeQuietly(source);
                    }
                }

                File releasedModules = new File(settingsBean.getJahiaVarDiskPath(), "released-modules");
                if (generatedWar.exists()) {
                    FileUtils.moveFileToDirectory(generatedWar, releasedModules, true);
                    generatedWar = new File(releasedModules, generatedWar.getName());
                } else {
                    generatedWar = null;
                }


                if (generatedWar != null) {
                    JahiaTemplatesPackage pack = compileAndDeploy(module.getRootFolder(), sources, session);

                    JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
                    node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
                    if (scmUrl != null) {
                        node.getNode("j:versionInfo").setProperty("j:scmURI", scmUrl);
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

    public List<File> regenerateImportFile(String moduleName, File sources, JCRSessionWrapper session) throws RepositoryException {
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

        try {
            File f = File.createTempFile("import", null);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ImportExportService.XSL_PATH, SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/export/templatesCleanup.xsl");
            ImportExportBaseService
                    .getInstance().exportZip(session.getNode("/modules/" + aPackage.getRootFolderWithVersion()), session.getRootNode(),
                    new FileOutputStream(f), params);
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

            if (scm != null) {
                try {
                    scm.setModifiedFile(modifiedFiles);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (RepositoryException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (SAXException e11) {
            logger.error(e11.getMessage(), e11);
        } catch (IOException e12) {
            logger.error(e12.getMessage(), e12);
        } catch (TransformerException e13) {
            logger.error(e13.getMessage(), e13);
        }

        return modifiedFiles;
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
            if (root.elements("archive").size() > 0) {
                root = (Element) root.elements("archive").get(0);
                root = (Element) root.elements("manifestEntries").get(0);
            } else {
                root = (Element) root.elements("instructions").get(0);
            }
            if (root.elements("Jahia-Depends").size() == 0) {
                root = root.addElement("Jahia-Depends");
            } else {
                root = (Element) root.elements("Jahia-Depends").get(0);
            }
            root.setText(StringUtils.join(dependencies, ","));
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
            try {
                writer.write(document);
            } finally {
                writer.close();
            }
            FileInputStream source = new FileInputStream(modifiedPom);
            try {
                saveFile(source, pom);
            } finally {
                IOUtils.closeQuietly(source);
                modifiedPom.delete();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setSCMConfigInPom(File sources, String uri) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element root = document.getRootElement();
            Iterator it = root.elementIterator("scm");
            if (!it.hasNext()) {
                Element scm = root.addElement("scm");
                scm.addElement("connection").addText(uri);
                scm.addElement("developerConnection").addText(uri);
                List list = root.elements();
                list.remove(scm);
                list.add(list.indexOf(root.elementIterator("description").next()) + 1, scm);
            }
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), OutputFormat.createPrettyPrint());
            try {
                writer.write(document);
            } finally {
                writer.close();
            }
            FileInputStream source = new FileInputStream(modifiedPom);
            try {
                saveFile(source, pom);
            } finally {
                IOUtils.closeQuietly(source);
                modifiedPom.delete();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean saveFile(InputStream source, File target) throws IOException, PatchFailedException {
        Charset transCodeTarget = null;
        if (target.getParentFile().getName().equals("resources") && target.getName().endsWith(".properties")) {
            transCodeTarget = Charsets.ISO_8859_1;
        }

        if (!target.exists()) {
            target.getParentFile().mkdirs();
            if (transCodeTarget != null) {
                FileUtils.writeLines(target, transCodeTarget.name(), convertToNativeEncoding(IOUtils.readLines(source, Charsets.UTF_8), transCodeTarget), "\n");
            } else {
                FileUtils.copyInputStreamToFile(source, target);
            }
            return true;
        } else {
            List<String> targetContent = FileUtils.readLines(target, transCodeTarget != null ? transCodeTarget : Charsets.UTF_8);
            if (!isBinary(targetContent)) {
                List<String> sourceContent = IOUtils.readLines(source, Charsets.UTF_8);
                if (transCodeTarget != null) {
                    sourceContent = convertToNativeEncoding(sourceContent, transCodeTarget);
                }
                Patch patch = DiffUtils.diff(targetContent, sourceContent, MYERS_DIFF);
                if (!patch.getDeltas().isEmpty()) {
                    targetContent = (List<String>) patch.applyTo(targetContent);
                    FileUtils.writeLines(target, transCodeTarget != null ? transCodeTarget.name() : "UTF-8", targetContent, "\n");
                    return true;
                }
            } else {
                byte[] sourceArray = IOUtils.toByteArray(source);
                FileInputStream input = new FileInputStream(target);
                FileOutputStream output = null;
                try {
                    byte[] targetArray = IOUtils.toByteArray(input);
                    if (!Arrays.equals(sourceArray, targetArray)) {
                        output = new FileOutputStream(target);
                        IOUtils.write(sourceArray, output);
                        return true;
                    }
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
            }
        }
        return false;
    }

    private List<String> convertToNativeEncoding(List<String> sourceContent, Charset charset) throws UnsupportedEncodingException {
        List<String> targetContent = new ArrayList<String>();
        for (String s : sourceContent) {
            Matcher m;
            int start = 0;
            while ((m = UNICODE_PATTERN.matcher(s)).find(start)) {
                String replacement = new String(new byte[]{(byte) Integer.parseInt(m.group(1), 16), (byte) Integer.parseInt(m.group(2), 16)}, "UTF-16");
                if (charset.decode(charset.encode(replacement)).toString().equals(replacement)) {
                    s = m.replaceFirst(replacement);
                }
                start = m.start() + 1;
            }
            targetContent.add(s);
        }
        return targetContent;
    }

    private boolean isBinary(List<String> text) {
        for (String s : text) {
            if (s.contains("\u0000")) {
                return true;
            }
        }
        return false;
    }

    public void updateDependencies(JahiaTemplatesPackage pack, List<String> depends) {
        pack.getDepends().clear();
        pack.getDepends().addAll(depends);
        templatePackageRegistry.computeDependencies(pack);

        if (pack.getSourcesFolder() != null) {
            setDependenciesInPom(pack.getSourcesFolder(), depends);
        }

        applicationEventPublisher.publishEvent(new ModuleDependenciesEvent(pack.getRootFolder(), this));
    }


    public void setSourcesFolderInPackageAndNode(JahiaTemplatesPackage pack, File sources, JCRNodeWrapper node) throws RepositoryException {
        setSourcesFolderInPackage(pack, sources);
        if (pack.getSourcesFolder() != null) {
//            templatePackageRegistry.unmountSourcesProvider(pack);
//            templatePackageRegistry.mountSourcesProvider(pack);
            node.getNode("j:versionInfo").setProperty("j:sourcesFolder", pack.getSourcesFolder().getPath());
            if (pack.getSourceControl() != null) {
                try {
                    node.getNode("j:versionInfo").setProperty("j:scmURI", pack.getSourceControl().getURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setSourcesFolderInPackage(JahiaTemplatesPackage pack, File sources) {
        if (checkValidSources(pack, sources)) {
            pack.setSourcesFolder(sources);
            try {
                SourceControlManagement sourceControlManagement = SourceControlManagement.getSourceControlManagement(sources);
                if (sourceControlManagement != null) {
                    pack.setSourceControl(sourceControlManagement);
                }
            } catch (Exception e) {
                logger.error("Cannot get source control", e);
            }
        }
    }

    /**
     * **************************************************************************************************************
     * Module installation on sites
     * ***************************************************************************************************************
     */

    /**
     * get List of installed modules for a site.
     * @param siteKey key of the site
     * @param includeTemplateSet if true (default is false) include dependencies of the template set
     * @param includeDirectDependencies if true (default is false) include dependencies of dependencies
     * @param includeTransitiveDependencies   if true (default is false) include all dependencies
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
        if (StringUtils.isNotBlank(module.getAutoDeployOnSite())) {
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
            logger.info("Installing " + module.getName() + " on " + sitePath);
            JCRNodeWrapper moduleNode = null;
            try {
                moduleNode = session.getNode("/modules/" + module.getRootFolder());

                String moduleName = moduleNode.getName();

                if (moduleNode.isNodeType("jnt:module")) {
                    moduleNode = moduleNode.getNode(module.getVersion().toString());
                }
                synchro(moduleNode, siteNode, session, moduleName, references);

                ReferencesHelper.resolveCrossReferences(session, references);

                addDependencyValue(moduleNode.getParent(), siteNode, "j:installedModules");
                logger.info("Done installing " + module.getName() + " on " + sitePath);
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
        if (destinationNode.hasProperty(propertyName)) {
            JCRPropertyWrapper installedModules = destinationNode.getProperty(propertyName);
            Value[] values = installedModules.getValues();
            for (Value value : values) {
                if (value.getString().equals(originalNode.getName())) {
                    return true;
                }
            }

            destinationNode.getSession().checkout(destinationNode);
            installedModules.addValue(originalNode.getName());
        } else {
            destinationNode.setProperty(propertyName, new String[]{newStringValue});
        }
        return false;
    }

    public void synchro(JCRNodeWrapper source, JCRNodeWrapper destinationNode, JCRSessionWrapper session, String moduleName,
                        Map<String, List<String>> references) throws RepositoryException {
        if (source.isNodeType("jnt:moduleVersion")) {
            session.getUuidMapping().put(source.getIdentifier(), destinationNode.getIdentifier());
            NodeIterator ni = source.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (child.isNodeType("jnt:versionInfo") || child.isNodeType("jnt:moduleVersionFolder")) {
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

                if (!child.isNodeType("jnt:templatesFolder") && !child.isNodeType("jnt:componentFolder")) {
                    templatesSynchro(child, node, session, references, newNode, true);
                }
            }
        }
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
                                 JCRSessionWrapper session, Map<String, List<String>> references, boolean doUpdate, boolean doChildren)
            throws RepositoryException {
        if ("j:acl".equals(destinationNode.getName())) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Synchronizing node : " + destinationNode.getPath() + ", update=" + doUpdate + "/children=" + doChildren);
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

        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            boolean isPageNode = child.isNodeType("jnt:page");

            if (doChildren) {
                names.add(child.getName());

                boolean newNode = false;
                JCRNodeWrapper node = null;
                if (destinationNode.hasNode(child.getName())) {
                    node = destinationNode.getNode(child.getName());
                } else if (node == null) {
                    node = destinationNode.addNode(child.getName(), child.getPrimaryNodeTypeName());
                    newNode = true;
                }
                templatesSynchro(child, node, session, references, newNode, doChildren && (!isPageNode || newNode));
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
     * @deprecated since Jahia 6.6 use {@link #getModulesWithViewsForComponent} instead
     */
    @Deprecated
    public Set<JahiaTemplatesPackage> getSortedAvailableTemplatePackagesForModule(String moduleName, final RenderContext context) {
        return getModulesWithViewsForComponent(moduleName);
    }

    /**
     * Returns a set of all available template packages having templates for a module.
     *
     * @return a set of all available template packages
     */
    public Set<JahiaTemplatesPackage> getModulesWithViewsForComponent(String componentName) {
        componentName = componentName.replace(":", "_");
        Set<JahiaTemplatesPackage> r = templatePackageRegistry.getModulesWithViewsPerComponents().get(componentName);
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
                return templatePackageRegistry.lookupByFileName(String.valueOf(input));
            }
        });
    }

    public JahiaTemplatesPackage getAnyDeployedTemplatePackage(String templatePackage) {
        JahiaTemplatesPackage pack = getTemplatePackageByFileName(templatePackage);
        if (pack == null) {
            Set<ModuleVersion> versions = getTemplatePackageRegistry().getAvailableVersionsForModule(templatePackage);
            if (!versions.isEmpty()) {
                pack = getTemplatePackageRegistry().lookupByFileNameAndVersion(templatePackage, versions.iterator().next());
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

    public JahiaTemplatesPackage activateModuleVersion(String rootFolder, ModuleVersion version, JCRSessionWrapper session) throws RepositoryException, BundleException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByFileNameAndVersion(rootFolder, version);

        autoInstallModulesToSites(module, session);
        templatePackageRegistry.activateModuleVersion(module);
        return module;
    }

    public void undeployModule(JahiaTemplatesPackage pack, JCRSessionWrapper session) throws RepositoryException {
        templatePackageDeployer.undeployModule(pack, session, false);
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

    public class CompiledModuleInfo {
        private final File file;
        private final String moduleName;
        private final String version;

        public CompiledModuleInfo(File file, String moduleName, String version) {
            this.file = file;
            this.moduleName = moduleName;
            this.version = version;
        }

        public File getFile() {
            return file;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getVersion() {
            return version;
        }
    }
}
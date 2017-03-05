/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import name.fraser.neil.plaintext.DiffMatchPatch;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.DocumentException;
import org.jahia.api.Constants;
import org.jahia.commons.Version;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.security.license.LicenseCheckException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRValueWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.services.notification.ToolbarWarningsService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PomUtils;
import org.jahia.utils.ProcessHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for module compilation and build.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleBuildHelper implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ModuleBuildHelper.class);
    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    private String mavenExecutable;
    private String ignoreSnapshots;
    private boolean ignoreSnapshotsFlag;
    private String mavenArchetypePlugin;
    private String mavenArchetypeCatalog;
    private String mavenArchetypeVersion;
    private String mavenMinRequiredVersion;
    private String mavenReleasePlugin;
    private String mavenWarnIfVersionIsOlderThan;
    private ModuleManager moduleManager;
    private int moduleStartLevel;
    private SourceControlHelper scmHelper;
    private SettingsBean settingsBean;
    private TemplatePackageRegistry templatePackageRegistry;
    private ToolbarWarningsService toolbarWarningsService;

    private static void warnOldMavenVersion(String mvnVersionString) {
        logger.warn("");
        logger.warn("************************* DEPRECATION *************************");
        logger.warn("*                                                             *");
        logger.warn("* The version of Maven ({}), you are using, is deprecated. *", mvnVersionString);
        logger.warn("* Please, switch to a more recent one (e.g. 3.3.x).           *");
        logger.warn("*                                                             *");
        logger.warn("***************************************************************");
        logger.warn("");
    }

    public JahiaTemplatesPackage compileAndDeploy(final String moduleId, File sources, JCRSessionWrapper session)
            throws RepositoryException, IOException, BundleException {

        CompiledModuleInfo moduleInfo = compileModule(sources);
        Bundle bundle = BundleUtils.getBundle(moduleId, moduleInfo.getVersion());
        if (bundle != null) {
            // we deal with an existing bundle
            FileInputStream is = new FileInputStream(moduleInfo.getFile());
            try {
                bundle.update(ModuleUtils.addModuleDependencies(is));

                // start the bundle to make sure its template is available to be displayed
                bundle.start();

                // refresh wirings
                BundleLifecycleUtils.refreshBundle(bundle);
            } finally {
                IOUtils.closeQuietly(is);
            }
            if (BundleUtils.getContextStartException(bundle.getSymbolicName()) != null && BundleUtils.getContextStartException(bundle.getSymbolicName()) instanceof LicenseCheckException) {
                throw new IOException(BundleUtils.getContextStartException(bundle.getSymbolicName()).getLocalizedMessage());
            }
            return templatePackageRegistry.lookupByIdAndVersion(moduleInfo.getModuleName(), new ModuleVersion(
                    moduleInfo.getVersion()));
        } else {
            // No existing bundle found, deploy new one
            OperationResult operationResult = moduleManager.install(new FileSystemResource(moduleInfo.getFile()), null, true);
            BundleInfo bundleInfo = operationResult.getBundleInfos().get(0);
            bundle = BundleUtils.getBundleBySymbolicName(bundleInfo.getSymbolicName(), bundleInfo.getVersion());

            JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
            if (pkg == null) {
                throw new IOException("Cannot deploy module");
            }
            if (BundleUtils.getContextStartException(bundle.getSymbolicName()) != null && BundleUtils.getContextStartException(bundle.getSymbolicName()) instanceof LicenseCheckException) {
                throw new IOException(BundleUtils.getContextStartException(bundle.getSymbolicName()).getLocalizedMessage());
            }
            return templatePackageRegistry.lookupByIdAndVersion(moduleInfo.getModuleName(), new ModuleVersion(moduleInfo.getVersion()));
        }
    }

    public CompiledModuleInfo compileModule(File sources) throws IOException {

        if (!isMavenConfigured()) {
            throw new JahiaRuntimeException("Cannot compile module, either current instance is not " +
                    "in development mode or maven configuration is not good");
        }

        File pom = new File(sources, "pom.xml");
        try {
            Model model = PomUtils.read(pom);
            String artifactId = model.getArtifactId();
            String version = PomUtils.getVersion(model);
            if (StringUtils.isEmpty(version)) {
                throw new IOException("No version found in pom.xml file " + pom);
            }

            StringBuilder out = new StringBuilder();
            int r = 0;
            try {
                r = ProcessHelper.execute(mavenExecutable, new String[]{"clean", "install", "-Dmaven.test.skip=true"}, null, sources, out, null);
            } catch (JahiaRuntimeException e) {
                logger.error(e.getCause().getMessage(), e.getCause());
                throw e;
            }

            if (r > 0) {
                logger.error("Compilation error, returned status " + r);
                logger.error("Maven out : " + out);
                throw new IOException(out.toString());
            }
            File file = new File(sources.getPath() + "/target/" + artifactId + "-" + version + ".jar");
            if (file.exists()) {
                return new CompiledModuleInfo(file, artifactId, version);
            } else {
                throw new IOException("Cannot find a module archive to deploy in folder "
                        + file.getParentFile().getAbsolutePath());
            }
        } catch (XmlPullParserException e) {
            logger.error("Error parsing pom.xml file at " + pom, e);
            throw new IOException("Cannot parse pom.xml file " + pom, e);
        }
    }

    public JCRNodeWrapper createModule(final String moduleName, String artifactId, final String groupId, final String moduleType, final File moduleSources,
                                       final JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {

        if (!isMavenConfigured()) {
            throw new JahiaRuntimeException("Cannot create module, either current instance is not " +
                    "in development mode or maven configuration is not good");
        }
        if (StringUtils.isBlank(moduleName)) {
            throw new RepositoryException("Cannot create module because no module name has been specified");
        }
        if (StringUtils.isBlank(artifactId)) {
            artifactId = JCRContentUtils.generateNodeName(moduleName);
        }
        if (templatePackageRegistry.containsId(artifactId)) {
            throw new RepositoryException("Cannot create module " + artifactId + " because another module with the same artifactId exists");
        }

        File sources = moduleSources;
        if (sources == null) {
            sources = new File(SettingsBean.getInstance().getModulesSourcesDiskPath());
            if (!sources.exists() && !sources.mkdirs()) {
                throw new IOException("Unable to create path for: " + sources);
            }
        }

        String finalFolderName = null;

        if (!sources.exists()) {
            finalFolderName = sources.getName();
            sources = sources.getParentFile();
            if (sources == null) {
                sources = new File(SettingsBean.getInstance().getModulesSourcesDiskPath());
            }
            if (!sources.exists() && !sources.mkdirs()) {
                throw new IOException("Unable to create path for: " + sources);
            }
        }

        List<String> archetypeParams = new ArrayList<String>();
        archetypeParams.add(mavenArchetypePlugin + ":generate");
        archetypeParams.add("-DarchetypeCatalog=" + mavenArchetypeCatalog + ",local");
        archetypeParams.add("-DarchetypeGroupId=org.jahia.archetypes");
        archetypeParams.add("-DarchetypeArtifactId=jahia-" + moduleType + "-archetype");
        archetypeParams.add("-DarchetypeVersion=" + mavenArchetypeVersion);
        archetypeParams.add("-Dversion=1.0-SNAPSHOT");
        archetypeParams.add("\"-DmoduleName=" + moduleName + "\"");
        archetypeParams.add("-DartifactId=" + artifactId);
        if (StringUtils.isNotBlank(groupId)) {
            archetypeParams.add("-DgroupId=" + groupId);
        }
        archetypeParams.add("-DdigitalFactoryVersion=" + Constants.JAHIA_PROJECT_VERSION);
        archetypeParams.add("-DinteractiveMode=false");

        StringBuilder out = new StringBuilder();
        int ret = ProcessHelper.execute(mavenExecutable, archetypeParams.toArray(new String[archetypeParams.size()]), null, sources, out,
                out);

        if (ret > 0) {
            logger.error("Maven archetype call returned " + ret);
            logger.error("Maven out : " + out);
            return null;
        }

        File path = new File(sources, artifactId);
        if (finalFolderName != null && !path.getName().equals(finalFolderName)) {
            try {
                File newPath = new File(sources, finalFolderName);
                FileUtils.moveDirectory(path, newPath);
                path = newPath;
            } catch (IOException e) {
                logger.error("Cannot rename folder", e);
            }
        }

        JahiaTemplatesPackage pack = compileAndDeploy(artifactId, path, session);

        JCRNodeWrapper node = session.getNode("/modules/" + pack.getIdWithVersion());
        scmHelper.setSourcesFolderInPackageAndNode(pack, path, node);
        session.save();

        return node;
    }

    public void deployToMaven(String groupId, String artifactId, ModuleReleaseInfo releaseInfo, File generatedJar) throws IOException {

        if (!isMavenConfigured()) {
            throw new JahiaRuntimeException("Cannot deploy module to maven, either current instance is not " +
                    "in development mode or maven configuration is not good");
        }

        File settings = null;
        File pomFile = null;
        try {
            if (!StringUtils.isEmpty(releaseInfo.getUsername()) && !StringUtils.isEmpty(releaseInfo.getPassword())) {
                settings = File.createTempFile("settings", ".xml");
                BufferedWriter w = new BufferedWriter(new FileWriter(settings));
                w.write("<settings><servers><server><id>" + releaseInfo.getRepositoryId() + "</id><username>");
                w.write(releaseInfo.getUsername());
                w.write("</username><password>");
                w.write(releaseInfo.getPassword());
                w.write("</password></server></servers></settings>");
                w.close();
            }
            JarFile jar = new JarFile(generatedJar);
            pomFile = PomUtils.extractPomFromJar(jar, groupId, artifactId);
            jar.close();

            Model pom;
            try {
                pom = PomUtils.read(pomFile);
            } catch (XmlPullParserException e) {
                throw new IOException(e);
            }
            String version = pom.getVersion();
            if (version == null) {
                version = pom.getParent().getVersion();
            }
            if (version == null) {
                throw new IOException("unable to read project version");
            }
            String[] deployParams = {"deploy:deploy-file", "-Dfile=" + generatedJar,
                    "-DrepositoryId=" + releaseInfo.getRepositoryId(), "-Durl=" + releaseInfo.getRepositoryUrl(),
                    "-DpomFile=" + pomFile.getPath(),
                    "-Dpackaging=" + StringUtils.substringAfterLast(generatedJar.getName(), "."),
                    "-DgroupId=" + PomUtils.getGroupId(pom), "-DartifactId=" + pom.getArtifactId(),
                    "-Dversion=" + version};
            if (settings != null) {
                deployParams = (String[]) ArrayUtils.addAll(deployParams,
                        new String[]{"--settings", settings.getPath()});
            }
            StringBuilder out = new StringBuilder();
            int ret = ProcessHelper.execute(mavenExecutable, deployParams, null,
                    generatedJar.getParentFile(), out, out);

            if (ret > 0) {
                String s = getMavenError(out.toString());
                logger.error("Maven archetype call returned " + ret);
                logger.error("Maven out : " + out);
                throw new IOException("Maven invocation failed\n" + s);
            }
        } finally {
            FileUtils.deleteQuietly(settings);
            FileUtils.deleteQuietly(pomFile);
        }
    }

    private String getMavenError(String out) {
        Matcher m = Pattern.compile("^\\[ERROR\\](.*)$", Pattern.MULTILINE).matcher(out);
        StringBuilder s = new StringBuilder();
        while (m.find()) {
            s.append(m.group(1)).append("\n");
        }
        return s.toString();
    }

    private String getMavenHome() throws IOException {
        String home = System.getenv().get("M2_HOME") != null ? System.getenv().get("M2_HOME") : "/usr/share/maven";
        if (!new File(home).exists()) {
            throw new IOException("Maven home not found, please set your M2_HOME environment variable");
        }
        return home;
    }

    protected File releaseModuleInternal(Model model, String lastVersion, String releaseVersion,
                                         ModuleReleaseInfo releaseInfo, File sources, String scmUrl) throws IOException, XmlPullParserException {

        if(!isMavenConfigured()) {
            throw new JahiaRuntimeException("Cannot release module, either current instance is not " +
                    "in development mode or maven configuration is not good");
        }

        String nextVersion = releaseInfo.getNextVersion();
        String artifactId = model.getArtifactId();
        File pom = new File(sources, "pom.xml");
        File generatedJar;

        if (scmUrl != null) {
            // release using maven-release-plugin
            String tag = StringUtils.replace(releaseVersion, ".", "_");

            int ret;

            File tmpRepo = new File(System.getProperty("java.io.tmpdir"), "repo");
            tmpRepo.mkdir();
            String[] installParams = new String[]{mavenReleasePlugin + ":prepare", mavenReleasePlugin + ":stage", mavenReleasePlugin + ":clean",
                    "-Dmaven.home=" + getMavenHome(), "-Dtag=" + tag, "-DreleaseVersion=" + releaseVersion,
                    "-DdevelopmentVersion=" + nextVersion, "-DignoreSnapshots=" + ignoreSnapshotsFlag,
                    "-DstagingRepository=tmp::default::" + tmpRepo.toURI().toString(), "--batch-mode"};
            StringBuilder out = new StringBuilder();
            ret = ProcessHelper.execute(mavenExecutable, installParams, null, sources, out, out);

            FileUtils.deleteDirectory(tmpRepo);

            if (ret > 0) {
                String s = getMavenError(out.toString());
                logger.error("Maven release call returnedError release, maven out : " + out);
                logger.error("Error when releasing, maven out : " + out);
                ProcessHelper.execute(mavenExecutable, new String[]{mavenReleasePlugin + ":rollback"}, null, sources, out, out);
                logger.error("Rollback release : " + out);
                throw new IOException("Maven invocation failed\n" + s);
            }

            File oldJar = new File(settingsBean.getJahiaModulesDiskPath(), artifactId + "-" + lastVersion + ".jar");
            if (oldJar.exists()) {
                oldJar.delete();
            }

            generatedJar = new File(sources.getPath() + "/target/checkout/target/" + artifactId + "-"
                        + releaseVersion + ".jar");
        } else {
            // modify the version in the pom.xml and compile/install module
            PomUtils.updateVersion(pom, releaseVersion);

            generatedJar = compileModule(sources).getFile();

            PomUtils.updateVersion(pom, nextVersion);
        }
        return generatedJar;
    }

    public void setIgnoreSnapshots(String ignoreSnapshots) {
        this.ignoreSnapshots = ignoreSnapshots;
    }

    public void setMavenArchetypeCatalog(String mavenArchetypeCatalog) {
        this.mavenArchetypeCatalog = mavenArchetypeCatalog;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setSourceControlHelper(SourceControlHelper scmHelper) {
        this.scmHelper = scmHelper;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    private boolean isMavenConfigured() {
        return settingsBean.isDevelopmentMode() && settingsBean.isMavenExecutableSet();
    }

    private void checkMavenExecutable() {

        if (settingsBean.isDevelopmentMode()) {
            settingsBean.setMavenExecutableSet(false);

            String mavenExecutable = this.mavenExecutable;
            StringBuilder resultOut = new StringBuilder();
            try {
                String[] args = new String[]{"-version"};
                int res = 0;
                if (System.getProperty("os.name").toLowerCase().startsWith("windows")
                        && !mavenExecutable.endsWith(".bat") && !mavenExecutable.endsWith(".cmd")) {
                    // check for Maven 3.3.x
                    try {
                        res = ProcessHelper.execute(mavenExecutable + ".cmd", args, null, null, resultOut, null);
                        if (res == 0) {
                            // we are dealing with Maven 3.3.x
                            mavenExecutable = mavenExecutable + ".cmd";
                        }
                    } catch (JahiaRuntimeException e) {
                        // assume Maven < 3.3.x
                        mavenExecutable = mavenExecutable + ".bat";
                    }
                }
                if (res > 0 || resultOut.length() == 0) {
                    res = ProcessHelper.execute(mavenExecutable, args, null, null, resultOut, null);
                }
                if (res > 0) {
                    toolbarWarningsService.addMessage("warning.maven.missing");
                    logger.error("Cannot set maven executable to " + mavenExecutable + ", please check your configuration");
                    return;
                }
                String mvnVersionString = StringUtils.substringBefore(StringUtils.substringBetween(resultOut.toString(), "Apache Maven ", "\n"), " ");
                String[] mvnVersion = StringUtils.split(mvnVersionString, ".");
                String[] requiredVersion = StringUtils.split(mavenMinRequiredVersion, ".");
                boolean isValid = true;
                for (int i = 0; i < mvnVersion.length; i++) {
                    isValid = Integer.parseInt(mvnVersion[i]) >= Integer.parseInt(requiredVersion[i]);
                    if (!isValid || i == requiredVersion.length - 1) {
                        break;
                    }
                }

                if (isValid) {
                    this.mavenExecutable = mavenExecutable;
                    settingsBean.setMavenExecutableSet(true);
                    if (new Version(mvnVersionString).compareTo(new Version(mavenWarnIfVersionIsOlderThan)) < 0) {
                        warnOldMavenVersion(mvnVersionString);
                    }
                } else {
                    toolbarWarningsService.addMessage("warning.maven.wrong.version");
                    logger.error("Detected Maven Version: " + StringUtils.join(mvnVersion, ".") + " do not match the minimum required version " + mavenMinRequiredVersion);
                }
            } catch (Exception e) {
                toolbarWarningsService.addMessage("warning.maven.missing");
                logger.error("Cannot set maven executable to " + mavenExecutable + ", please check your configuration", e);
            }
            if(!settingsBean.isMavenExecutableSet()) {
                logger.error("Until maven executable is correctly set, the studio will not be available");
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkMavenExecutable();

        if (mavenArchetypeCatalog == null || mavenArchetypeCatalog.length() == 0) {
            mavenArchetypeCatalog = mavenArchetypeVersion != null && mavenArchetypeVersion.contains("-SNAPSHOT")
                    ? "https://devtools.jahia.com/nexus/content/repositories/jahia-snapshots/archetype-catalog.xml"
                    : "https://devtools.jahia.com/nexus/content/repositories/jahia-releases/archetype-catalog.xml";
        }

        logger.info("Using version {} for the module archetypes from catalog {}", mavenArchetypeVersion,
                mavenArchetypeCatalog);

        if (ignoreSnapshots == null || ignoreSnapshots.length() == 0) {
            ignoreSnapshotsFlag = Constants.JAHIA_PROJECT_VERSION.contains("-SNAPSHOT");
        } else {
            ignoreSnapshotsFlag = Boolean.valueOf(ignoreSnapshots.trim());
        }
        if (mavenArchetypePlugin == null || mavenArchetypePlugin.length() == 0) {
            mavenArchetypePlugin = "archetype";
        }
        if (mavenReleasePlugin == null || mavenReleasePlugin.length() == 0) {
            mavenReleasePlugin = "release";
        }
    }

    public void setMavenReleasePlugin(String mavenReleasePlugin) {
        this.mavenReleasePlugin = mavenReleasePlugin;
    }

    public void setMavenMinRequiredVersion(String mavenRequiredVersion) {
        this.mavenMinRequiredVersion = mavenRequiredVersion;
    }

    public void setMavenExecutable(String mavenExecutable) {
        this.mavenExecutable = mavenExecutable;
    }

    public void setToolbarWarningsService(ToolbarWarningsService toolbarWarningsService) {
        this.toolbarWarningsService = toolbarWarningsService;
    }

    public JahiaTemplatesPackage duplicateModule(String dstModuleName, String dstModuleId, String dstGroupId, String srcPath, String scmURI, String branchOrTag,
                                                 String srcModuleId, String srcModuleVersion, boolean uninstallSrcModule, String dstPath, boolean deleteSrcFolder, JCRSessionWrapper session)
            throws IOException, RepositoryException, BundleException {

        if (StringUtils.isBlank(dstModuleName)) {
            throw new SourceControlException("Cannot create module because no module name has been specified");
        }
        if (StringUtils.isBlank(dstModuleId)) {
            dstModuleId = JCRContentUtils.generateNodeName(dstModuleName);
        }
        if (StringUtils.isBlank(dstGroupId)) {
            dstGroupId = "org.jahia.modules";
        }
        if (templatePackageRegistry.containsId(dstModuleId)) {
            throw new ScmUnavailableModuleIdException("Cannot create module " + dstModuleId + " because another module with the same artifactId exists");
        }

        if (StringUtils.isBlank(dstPath)) {
            dstPath = SettingsBean.getInstance().getModulesSourcesDiskPath();
        }
        File parentDir = new File(dstPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new SourceControlException("Unable to create path for: " + parentDir);
        }

        File dstFolder = new File(parentDir, dstModuleId);
        int i = 0;
        while (dstFolder.exists()) {
            dstFolder = new File(parentDir, dstModuleId + "_" + (++i));
        }

        File srcFolder;
        if (srcPath == null) {
            try {
                srcFolder = scmHelper.checkoutTmpModule(srcModuleId, srcModuleVersion, scmURI, branchOrTag);
            } catch (XmlPullParserException e) {
                throw new IOException(e);
            } catch (DocumentException e) {
                throw new IOException(e);
            }
            deleteSrcFolder = true;
        } else {
            srcFolder = new File(srcPath);
        }

        FileUtils.copyDirectory(srcFolder, dstFolder);
        if (deleteSrcFolder) {
            FileUtils.deleteQuietly(srcFolder);
        }

        CompiledModuleInfo compiledModuleInfo;
        try {

            String dstVersion = "1.0-SNAPSHOT";
            updateDuplicatedPom(dstModuleName, dstModuleId, dstGroupId, dstVersion, dstFolder);

            cleanScmFiles(dstFolder);

            JahiaTemplatesPackage srcModule = templatePackageRegistry.lookupByIdAndVersion(srcModuleId, new ModuleVersion(srcModuleVersion));
            updateDuplicatedImportFiles(srcModule, dstModuleName, dstModuleId, dstVersion, dstFolder, session);

            renameDuplicatedResourceBundle(srcModule, dstModuleId, dstFolder);

            compiledModuleInfo = compileModule(dstFolder);

            if (uninstallSrcModule) {
                undeployAllModuleVersions(srcModuleId);
            }

        } catch (IOException e) {
            FileUtils.deleteQuietly(dstFolder);
            throw e;
        } catch (RepositoryException e) {
            FileUtils.deleteQuietly(dstFolder);
            throw e;
        } catch (BundleException e) {
            FileUtils.deleteQuietly(dstFolder);
            throw e;
        } catch (RuntimeException e) {
            FileUtils.deleteQuietly(dstFolder);
            throw e;
        }

        OperationResult operationResult = moduleManager.install(new FileSystemResource(compiledModuleInfo.getFile()), null, true);
        BundleInfo bundleInfo = operationResult.getBundleInfos().get(0);
        Bundle bundle = BundleUtils.getBundleBySymbolicName(bundleInfo.getSymbolicName(), bundleInfo.getVersion());

        JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
        if (pkg == null) {
            FileUtils.deleteQuietly(dstFolder);
            throw new IOException("Cannot deploy module");
        }
        if (BundleUtils.getContextStartException(bundle.getSymbolicName()) != null && BundleUtils.getContextStartException(bundle.getSymbolicName()) instanceof LicenseCheckException) {
            throw new BundleException(BundleUtils.getContextStartException(bundle.getSymbolicName()).getLocalizedMessage());
        }

        return templatePackageRegistry.lookupByIdAndVersion(compiledModuleInfo.getModuleName(), new ModuleVersion(
                compiledModuleInfo.getVersion()));
    }

    private void undeployAllModuleVersions(String srcModuleId) throws BundleException {
        Set<ModuleVersion> availableVersionsForModule = templatePackageRegistry.getAvailableVersionsForModule(srcModuleId);
        ModuleVersion[] versions = availableVersionsForModule.toArray(new ModuleVersion[availableVersionsForModule.size()]);
        for (int j = 0; j < versions.length; j++) {
            Bundle bundle = templatePackageRegistry.lookupByIdAndVersion(srcModuleId, versions[j]).getBundle();
            int state = bundle.getState();
            if (state == Bundle.ACTIVE || state == Bundle.STARTING) {
                bundle.stop();
            }
            bundle.uninstall();
        }
    }

    private void renameDuplicatedResourceBundle(JahiaTemplatesPackage srcModule, String dstModuleId, File dstFolder) {
        File rbFolder = new File(dstFolder, "src/main/resources/resources");
        if (rbFolder.exists()) {
            Pattern rbPattern = Pattern.compile("(" + srcModule.getId()
                    + "|" + StringUtils.replace(srcModule.getName(), " ", "")
                    + "|" + StringUtils.replace(srcModule.getName(), " ", "_") + ")(_[a-z]{2}(-[A-Z]{2})?)?.properties");
            File[] files = rbFolder.listFiles();
            if (files != null) {
                for (File f : files) {
                    Matcher m = rbPattern.matcher(f.getName());
                    if (m.matches()) {
                        if (m.group(2) == null) {
                            f.renameTo(new File(rbFolder, dstModuleId + ".properties"));
                        } else {
                            f.renameTo(new File(rbFolder, dstModuleId + m.group(2) + ".properties"));
                        }
                    }
                }
            }
        }
    }

    private void updateDuplicatedImportFiles(JahiaTemplatesPackage srcModule, String dstModuleName, String dstModuleId, String dstVersion, File dstFolder, JCRSessionWrapper session) throws RepositoryException, IOException {

        JCRNodeWrapper srcModuleNode = session.getNode("/modules/" + srcModule.getIdWithVersion());
        JCRNodeWrapper dstModuleNode = session.getNode("/modules");
        if (dstModuleNode.hasNode(dstModuleId)) {
            dstModuleNode = dstModuleNode.getNode(dstModuleId);
        } else {
            dstModuleNode = dstModuleNode.addNode(dstModuleId, "jnt:module");
        }
        if (dstModuleNode.hasNode(dstVersion)) {
            dstModuleNode = dstModuleNode.getNode(dstVersion);
        } else {
            dstModuleNode = dstModuleNode.addNode(dstVersion, "jnt:moduleVersion");
        }
        for (JCRNodeWrapper node : srcModuleNode.getNodes()) {
            if (!node.isNodeType("jnt:moduleVersionFolder") && !node.isNodeType("jnt:versionInfo")) {
                node.copy(dstModuleNode.getPath());
            }
        }
        dstModuleNode.setProperty("j:title", dstModuleName);
        if (srcModuleNode.hasProperty("j:installedModules")) {
            List<Value> newValues = new ArrayList<Value>();
            for (JCRValueWrapper value : srcModuleNode.getProperty("j:installedModules").getValues()) {
                if (srcModule.getId().equals(value.getString())) {
                    newValues.add(new ValueImpl(dstModuleId));
                } else {
                    newValues.add(value);
                }
            }
            dstModuleNode.setProperty("j:installedModules", newValues.toArray(new Value[newValues.size()]));
        }
        session.save();

        FileUtils.deleteQuietly(new File(dstFolder, "src/main/import/content/modules/" + srcModule.getId()));
        try {
            regenerateImportFile(session, new ArrayList<File>(), dstFolder, dstModuleId, dstModuleId + "/" + dstVersion);
        } catch (SAXException | TransformerException e) {
            throw new IOException("Unable to generate import files in " + dstFolder);
        } finally {
            // clean up dest repository
            session.getNode("/modules/" + dstModuleId).remove();
            session.save();
        }
    }

    private void cleanScmFiles(File dstFolder) throws IOException {
        FileUtils.deleteQuietly(new File(dstFolder, ".git"));
        FileUtils.deleteQuietly(new File(dstFolder, ".gitignore"));
        new SvnCleaner().clean(dstFolder);
    }

    private void updateDuplicatedPom(String moduleName, String artifactId, String groupId, String dstVersion, File dstFolder) throws IOException {
        Model pom = null;
        try {
            pom = PomUtils.read(new File(dstFolder, "pom.xml"));
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
        if (!"bundle".equals(pom.getPackaging())) {
            throw new IOException("This module is not compatible with the current version of Jahia.");
        }
        pom.setArtifactId(artifactId);
        pom.setGroupId(groupId);
        pom.setVersion(dstVersion);
        pom.setName(moduleName);
        Scm scm = new Scm();
        scm.setConnection(Constants.SCM_DUMMY_URI);
        scm.setDeveloperConnection(Constants.SCM_DUMMY_URI);
        pom.setScm(scm);
        pom.setDistributionManagement(null);
        pom.getProperties().remove("jahia-private-app-store");
        PomUtils.write(pom, new File(dstFolder, "pom.xml"));
    }

    public void regenerateImportFile(JCRSessionWrapper session, List<File> modifiedFiles, File sources,
                                     String moduleId, String moduleIdWithVersion) throws RepositoryException, SAXException, IOException, TransformerException {
        File f = File.createTempFile("import", null);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ImportExportService.XSL_PATH, SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/export/templatesCleanup.xsl");
        FileOutputStream out = new FileOutputStream(f);
        try {
            ImportExportBaseService.getInstance().exportZip(
                    session.getNode("/modules/" + moduleIdWithVersion), session.getRootNode(), out,
                    params);
        } finally {
            IOUtils.closeQuietly(out);
        }

        final String importFileBasePath = "content/modules/" + moduleId + "/";
        String filesNodePath = "/modules/" + moduleIdWithVersion;
        JCRNodeWrapper filesNode = null;
        if (session.nodeExists(filesNodePath)) {
            filesNode = session.getNode(filesNodePath);
        }
        // clean up files folder before unziping in it
        File sourcesImportFolder = new File(sources, "src/main/import");
        sourcesImportFolder.mkdirs();
        File filesDirectory = new File(sourcesImportFolder.getPath() + "/" + importFileBasePath);
        Collection<File> files = null;
        if (filesDirectory.exists()) {
            files = FileUtils.listFiles(filesDirectory, null, true);
        } else {
            files = new ArrayList<File>();
        }
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(f));
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                if (!zipentry.isDirectory()) {
                    try {
                        String name = zipentry.getName();
                        name = name.replace(moduleIdWithVersion, moduleId);
                        File sourceFile = new File(sourcesImportFolder, name);
                        boolean nodeMoreRecentThanSourceFile = true;
                        if (sourceFile.exists() && name.startsWith(importFileBasePath)) {
                            String relPath = name.substring(importFileBasePath.length());
                            if (relPath.endsWith(sourceFile.getName() + "/" + sourceFile.getName())) {
                                relPath = StringUtils.substringBeforeLast(relPath, "/");
                            }
                            if (filesNode != null && filesNode.hasNode(relPath)) {
                                JCRNodeWrapper node = filesNode.getNode(relPath);
                                if (node.hasProperty("jcr:lastModified")) {
                                    nodeMoreRecentThanSourceFile = node.getProperty("jcr:lastModified").getDate().getTimeInMillis() > sourceFile.lastModified();
                                }
                            }
                        }
                        if (nodeMoreRecentThanSourceFile && saveFile(zis, sourceFile)) {
                            modifiedFiles.add(sourceFile);
                        }

                        files.remove(sourceFile);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            for (File file : files) {
                try {
                    deleteFileAndEmptyParents(file, sourcesImportFolder.getPath());
                } catch (Exception e) {
                    logger.error("Cannot delete file " + file, e);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot patch import file", e);
        } finally {
            if (zis != null) {
                IOUtils.closeQuietly(zis);
            }
        }
    }

    private boolean saveFile(InputStream source, File target) throws IOException {
        Charset transCodeTarget = null;
        if (target.getParentFile().getName().equals("resources") && target.getName().endsWith(".properties")) {
            transCodeTarget = Charsets.ISO_8859_1;
        }

        if (!target.exists()) {
            if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
                throw new IOException("Unable to create path for: " + target.getParentFile());
            }
            if (target.getParentFile().isFile()) {
                target.getParentFile().delete();
                target.getParentFile().mkdirs();
            }
            if (transCodeTarget != null) {
                FileUtils.writeLines(target, transCodeTarget.name(), convertToNativeEncoding(IOUtils.readLines(source, Charsets.UTF_8), transCodeTarget), "\n");
            } else {
                FileOutputStream output = FileUtils.openOutputStream(target);
                try {
                    IOUtils.copy(source, output);
                    output.close();
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }

            // Save repository.xml file after first generation
            if (target.getName().equals("repository.xml")) {
                FileUtils.copyFile(target, new File(target.getPath() + ".generated"));
            }

            return true;
        } else {
            List<String> targetContent = FileUtils.readLines(target, transCodeTarget != null ? transCodeTarget : Charsets.UTF_8);
            if (!isBinary(targetContent)) {
                File previouslyGenerated = new File(target.getPath() + ".generated");
                List<String> previouslyGeneratedContent = targetContent;
                if (previouslyGenerated.exists()) {
                    previouslyGeneratedContent = FileUtils.readLines(previouslyGenerated, transCodeTarget != null ? transCodeTarget : Charsets.UTF_8);
                }
                DiffMatchPatch dmp = new DiffMatchPatch();
                List<String> sourceContent = IOUtils.readLines(source, Charsets.UTF_8);
                if (transCodeTarget != null) {
                    sourceContent = convertToNativeEncoding(sourceContent, transCodeTarget);
                }

                LinkedList<DiffMatchPatch.Patch> l = dmp.patch_make(StringUtils.join(previouslyGeneratedContent, "\n"), StringUtils.join(sourceContent, "\n"));

                if (target.getName().equals("repository.xml")) {
                    // Keep generated file uptodate
                    FileUtils.writeLines(new File(target.getPath() + ".generated"), transCodeTarget != null ? transCodeTarget.name() : "UTF-8", sourceContent);
                }

                if (!l.isEmpty()) {
                    Object[] objects = dmp.patch_apply(l, StringUtils.join(targetContent, "\n"));

                    for (boolean b : ((boolean[]) objects[1])) {
                        if (!b) {
                            throw new IOException("Cannot apply modification on "+target.getName() + ", check generated file at : " + target.getPath() + ".generated");
                        }
                    }
                    FileUtils.write(target, (CharSequence) objects[0], transCodeTarget != null ? transCodeTarget.name() : "UTF-8");
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

    private void deleteFileAndEmptyParents(File file, final String rootPath) throws IOException {
        if (rootPath.equals(file.getPath())) {
            return;
        }
        FileUtils.forceDelete(file);
        File parentFile = file.getParentFile();
        File[] files = parentFile.listFiles((FileFilter) HiddenFileFilter.VISIBLE);
        if (files == null || files.length == 0) {
            deleteFileAndEmptyParents(parentFile, rootPath);
        }
    }

    private List<String> convertToNativeEncoding(List<String> sourceContent, Charset charset) throws UnsupportedEncodingException {
        List<String> targetContent = new ArrayList<String>();
        for (String s : sourceContent) {
            Matcher m = UNICODE_PATTERN.matcher(s);
            int start = 0;
            while (m.find(start)) {
                String replacement = new String(new byte[]{(byte) Integer.parseInt(m.group(1), 16), (byte) Integer.parseInt(m.group(2), 16)}, "UTF-16");
                if (charset.decode(charset.encode(replacement)).toString().equals(replacement)) {
                    s = m.replaceFirst(replacement);
                }
                start = m.start() + 1;
                m = UNICODE_PATTERN.matcher(s);
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

    static class CompiledModuleInfo {

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

    static public class SvnCleaner extends DirectoryWalker<File> {

        public List<File> clean(File startDirectory) throws IOException {
            ArrayList<File> results = new ArrayList<File>();
            walk(startDirectory, results);
            return results;
        }

        @Override
        protected boolean handleDirectory(File directory, int depth, Collection<File> results) {
            if (".svn".equals(directory.getName())) {
                FileUtils.deleteQuietly(directory);
                results.add(directory);
                return false;
            } else {
                return true;
            }

        }
    }

    /**
     * Supplies the exact version of the Maven archetypes to use when creating a module.
     *
     * @param mavenArchetypeVersion
     *            the exact version of the Maven archetypes to use when creating a module
     */
    public void setMavenArchetypeVersion(String mavenArchetypeVersion) {
        this.mavenArchetypeVersion = mavenArchetypeVersion;
    }

    public int getModuleStartLevel() {
        return moduleStartLevel;
    }

    public void setModuleStartLevel(int moduleStartLevel) {
        this.moduleStartLevel = moduleStartLevel;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    public void setMavenWarnIfVersionIsOlderThan(String mavenWarnIfVersionIsOlderThan) {
        this.mavenWarnIfVersionIsOlderThan = mavenWarnIfVersionIsOlderThan;
    }

    public void setMavenArchetypePlugin(String mavenArchetypePlugin) {
        this.mavenArchetypePlugin = mavenArchetypePlugin;
    }
}
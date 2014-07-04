/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.templates;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;
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
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.data.templates.ModuleState;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.security.license.LicenseCheckException;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.notification.ToolbarWarningsService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PomUtils;
import org.jahia.utils.ProcessHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
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

    private static Logger logger = LoggerFactory.getLogger(ModuleBuildHelper.class);
    private static final MyersDiff MYERS_DIFF = new MyersDiff(new Equalizer() {
        public boolean equals(Object o, Object o1) {
            String s1 = (String) o;
            String s2 = (String) o1;
            return s1.trim().equals(s2.trim());
        }
    });
    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    private String mavenExecutable;
    private String ignoreSnapshots;
    private boolean ignoreSnapshotsFlag;
    private String mavenArchetypeCatalog;
    private String mavenMinRequiredVersion;
    private String mavenReleasePlugin;
    private SourceControlHelper scmHelper;
    private SettingsBean settingsBean;
    private TemplatePackageRegistry templatePackageRegistry;
    private ToolbarWarningsService toolbarWarningsService;

    public JahiaTemplatesPackage compileAndDeploy(final String moduleId, File sources, JCRSessionWrapper session)
            throws RepositoryException, IOException, BundleException {
        CompiledModuleInfo moduleInfo = compileModule(sources);
        Bundle bundle = BundleUtils.getBundle(moduleId, moduleInfo.getVersion());
        if (bundle != null) {
            FileInputStream is = new FileInputStream(moduleInfo.getFile());
            try {
                bundle.update(is);

                // start the bundle to make sure its template is available to be displayed
                bundle.start();
            } finally {
                IOUtils.closeQuietly(is);
            }
            if (BundleUtils.getContextStartException(bundle.getSymbolicName()) != null && BundleUtils.getContextStartException(bundle.getSymbolicName()) instanceof LicenseCheckException) {
                throw new IOException(BundleUtils.getContextStartException(bundle.getSymbolicName()).getLocalizedMessage());
            }
            return templatePackageRegistry.lookupByIdAndVersion(moduleInfo.getModuleName(), new ModuleVersion(
                    moduleInfo.getVersion()));
        }
        // No existing module found, deploy new one
        FileInputStream is = new FileInputStream(moduleInfo.getFile());
        try {
            bundle = FrameworkService.getBundleContext().installBundle(moduleInfo.getFile().toURI().toString(), is);
            bundle.adapt(BundleStartLevel.class).setStartLevel(2);
        } finally {
            IOUtils.closeQuietly(is);
        }
        JahiaTemplatesPackage pkg = BundleUtils.getModule(bundle);
        if (pkg == null) {
            throw new IOException("Cannot deploy module");
        }
        if (pkg.getState().getState() == ModuleState.State.WAITING_TO_BE_PARSED) {
            throw new IOException("Missing dependency : " + pkg.getState().getDetails().toString());
        }
        bundle.start();
        if (BundleUtils.getContextStartException(bundle.getSymbolicName()) != null && BundleUtils.getContextStartException(bundle.getSymbolicName()) instanceof LicenseCheckException) {
            throw new IOException(BundleUtils.getContextStartException(bundle.getSymbolicName()).getLocalizedMessage());
        }

        return templatePackageRegistry.lookupByIdAndVersion(moduleInfo.getModuleName(), new ModuleVersion(
                moduleInfo.getVersion()));
    }

    public CompiledModuleInfo compileModule(File sources) throws IOException {
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
                r = ProcessHelper.execute(mavenExecutable, new String[]{"clean", "install"}, null, sources, out, null);
            } catch (JahiaRuntimeException e) {
                logger.error(e.getCause().getMessage(), e.getCause());
                throw e;
            }

            if (r > 0) {
                logger.error("Compilation error, returned status " + r);
                logger.error("Maven out : " + out);
                throw new IOException(out.toString());
            }
            File file = new File(sources.getPath() + "/target/" + artifactId + "-" + version + ".war");
            if (!file.exists()) {
                file = new File(sources.getPath() + "/target/" + artifactId + "-" + version + ".jar");
            }
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
        archetypeParams.add("archetype:generate");
        archetypeParams.add("-DarchetypeCatalog=" + mavenArchetypeCatalog + ",local");
        archetypeParams.add("-DarchetypeGroupId=org.jahia.archetypes");
        archetypeParams.add("-DarchetypeArtifactId=jahia-" + (moduleType.equals("jahiapp") ? "app" : moduleType) + "-archetype");
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
        String nextVersion = releaseInfo.getNextVersion();
        String artifactId = model.getArtifactId();
        File pom = new File(sources, "pom.xml");
        File generatedWar;

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

            File oldWar = new File(settingsBean.getJahiaModulesDiskPath(), artifactId + "-" + lastVersion + ".war");
            if (oldWar.exists()) {
                oldWar.delete();
            }
            oldWar = new File(settingsBean.getJahiaModulesDiskPath(), artifactId + "-" + lastVersion + ".jar");
            if (oldWar.exists()) {
                oldWar.delete();
            }

            generatedWar = new File(sources.getPath() + "/target/checkout/target/" + artifactId + "-" + releaseVersion
                    + ".war");
            if (!generatedWar.exists()) {
                generatedWar = new File(sources.getPath() + "/target/checkout/target/" + artifactId + "-"
                        + releaseVersion + ".jar");
            }
        } else {
            // modify the version in the pom.xml and compile/install module
            PomUtils.updateVersion(pom, releaseVersion);

            generatedWar = compileModule(sources).getFile();

            PomUtils.updateVersion(pom, nextVersion);
        }
        return generatedWar;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mavenArchetypeCatalog == null || mavenArchetypeCatalog.length() == 0) {
            mavenArchetypeCatalog = Constants.JAHIA_PROJECT_VERSION.contains("-SNAPSHOT") ? "https://devtools.jahia.com/nexus/content/repositories/jahia-snapshots/archetype-catalog.xml"
                    : "https://devtools.jahia.com/nexus/content/repositories/jahia-releases/archetype-catalog.xml";
        }
        if (ignoreSnapshots == null || ignoreSnapshots.length() == 0) {
            ignoreSnapshotsFlag = Constants.JAHIA_PROJECT_VERSION.contains("-SNAPSHOT");
        } else {
            ignoreSnapshotsFlag = Boolean.valueOf(ignoreSnapshots.trim());
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

    /**
     * before setting the executable, test if can be executed and if it matches the required version
     * @param mavenExecutable
     */
    public void setMavenExecutable(String mavenExecutable) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !mavenExecutable.endsWith(".bat")) {
            mavenExecutable = mavenExecutable + ".bat";
        }
        // test maven version
        if (settingsBean.isDevelopmentMode()) {
            StringBuilder resultOut = new StringBuilder();
            try {
                int res = ProcessHelper.execute(mavenExecutable, new String[]{"-version"}, null, null, resultOut, null);
                if (res > 0) {
                    toolbarWarningsService.addMessage("warning.maven.missing");
                    logger.error("Cannot set maven executable to " + mavenExecutable + ", please check your configuration");
                    return;
                }
                String[] mvnVersion = StringUtils.split(StringUtils.substringBetween(resultOut.toString(), "Apache Maven ", "\n"), ".");
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
                } else {
                    toolbarWarningsService.addMessage("warning.maven.wrong.version");
                    logger.error("Detected Maven Version: " + StringUtils.join(mvnVersion, ".") + " do not match the minimum required version " + mavenMinRequiredVersion);
                }
            } catch (Exception e) {
                toolbarWarningsService.addMessage("warning.maven.missing");
                logger.error("Cannot set maven executable to " + mavenExecutable + ", please check your configuration", e);
            }
            if (StringUtils.isEmpty(this.mavenExecutable)) {
                logger.error("Until maven executable is correctly set, the studio will not be available");
                settingsBean.setMavenExecutableSet(false);
            }
        }
    }

    public void setToolbarWarningsService(ToolbarWarningsService toolbarWarningsService) {
        this.toolbarWarningsService = toolbarWarningsService;
    }

    public JahiaTemplatesPackage duplicateModule(String moduleName, String artifactId, String groupId, String srcPath, String scmURI,
                                                 String srcModuleId, String srcModuleVersion, String dstPath, JCRSessionWrapper session)
            throws IOException, RepositoryException, BundleException {
        if (StringUtils.isBlank(moduleName)) {
            throw new RepositoryException("Cannot create module because no module name has been specified");
        }
        if (StringUtils.isBlank(artifactId)) {
            artifactId = JCRContentUtils.generateNodeName(moduleName);
        }
        if (StringUtils.isBlank(groupId)) {
            groupId = "org.jahia.modules";
        }
        if (templatePackageRegistry.containsId(artifactId)) {
            throw new RepositoryException("Cannot create module " + artifactId + " because another module with the same artifactId exists");
        }

        if (StringUtils.isBlank(dstPath)) {
            dstPath = SettingsBean.getInstance().getModulesSourcesDiskPath();
        }
        File parentDir = new File(dstPath);
        if (!parentDir.exists() && !parentDir.mkdirs()) {
            throw new IOException("Unable to create path for: " + parentDir);
        }

        File dstFolder = new File(parentDir, artifactId);
        int i = 0;
        while (dstFolder.exists()) {
            dstFolder = new File(parentDir, artifactId + "_" + (++i));
        }

        File srcFolder;
        boolean deleteSrcFolder = false;
        if (srcPath == null) {
            try {
                srcFolder = scmHelper.checkoutTmpModule(srcModuleId, null, scmURI, null);
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

        // Update pom information
        Model pom = null;
        try {
            pom = PomUtils.read(new File(dstFolder, "pom.xml"));
        } catch (XmlPullParserException e) {
            throw new IOException(e);
        }
        pom.setArtifactId(artifactId);
        pom.setGroupId(groupId);
        String dstVersion = "1.0-SNAPSHOT";
        pom.setVersion(dstVersion);
        pom.setName(moduleName);
        Scm scm = new Scm();
        scm.setConnection(Constants.SCM_DUMMY_URI);
        scm.setDeveloperConnection(Constants.SCM_DUMMY_URI);
        pom.setScm(scm);
        pom.setDistributionManagement(null);
        pom.getProperties().remove("jahia-private-app-store");
        PomUtils.write(pom, new File(dstFolder, "pom.xml"));

        // Remove any SCM files
        FileUtils.deleteQuietly(new File(dstFolder, ".git"));
        FileUtils.deleteQuietly(new File(dstFolder, ".gitignore"));
        new SvnCleaner().clean(dstFolder);

        // Update import files
        JahiaTemplatesPackage srcModule = templatePackageRegistry.lookupByIdAndVersion(srcModuleId, new ModuleVersion(srcModuleVersion));
        JCRNodeWrapper srcModuleNode = session.getNode("/modules/" + srcModule.getIdWithVersion());
        JCRNodeWrapper dstModuleNode = session.getNode("/modules");
        if (dstModuleNode.hasNode(artifactId)) {
            dstModuleNode = dstModuleNode.getNode(artifactId);
        } else {
            dstModuleNode = dstModuleNode.addNode(artifactId, "jnt:module");
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
        dstModuleNode.setProperty("j:title", moduleName);
        List<Value> newValues = new ArrayList<Value>();
        for (JCRValueWrapper value : srcModuleNode.getProperty("j:installedModules").getValues()) {
            if (srcModuleId.equals(value.getString())) {
                newValues.add(new ValueImpl(artifactId));
            } else {
                newValues.add(value);
            }
        }
        dstModuleNode.setProperty("j:installedModules", newValues.toArray(new Value[newValues.size()]));
        session.save();

        FileUtils.deleteQuietly(new File(dstFolder, "src/main/import/content/modules/" + srcModuleId));
        try {
            regenerateImportFile(session, new ArrayList<File>(), dstFolder, artifactId, artifactId + "/" + dstVersion);
        } catch (SAXException e) {
            throw new IOException("Unable to generate import files in " + dstFolder);
        } catch (TransformerException e) {
            throw new IOException("Unable to generate import files in " + dstFolder);
        }

        return compileAndDeploy(artifactId, dstFolder, session);
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

    @SuppressWarnings("unchecked")
    private boolean saveFile(InputStream source, File target) throws IOException, PatchFailedException {
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

    private void deleteFileAndEmptyParents(File file, final String rootPath) throws IOException {
        if (rootPath.equals(file.getPath())) {
            return;
        }
        FileUtils.forceDelete(file);
        File parentFile = file.getParentFile();
        if (parentFile.listFiles((FileFilter) HiddenFileFilter.VISIBLE).length == 0) {
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

    static public class SvnCleaner extends DirectoryWalker {

        public List<File> clean(File startDirectory) throws IOException {
            ArrayList<File> results = new ArrayList<File>();
            walk(startDirectory, results);
            return results;
        }

        protected boolean handleDirectory(File directory, int depth, Collection results) {
            if (".svn".equals(directory.getName())) {
                FileUtils.deleteQuietly(directory);
                results.add(directory);
                return false;
            } else {
                return true;
            }

        }

    }
}
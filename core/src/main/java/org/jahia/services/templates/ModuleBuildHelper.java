/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PomUtils;
import org.jahia.utils.ProcessHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for module compilation and build.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleBuildHelper {

    class CompiledModuleInfo {
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

    private static Logger logger = LoggerFactory.getLogger(ModuleBuildHelper.class);

    private String mavenArchetypeCatalog;

    private String mavenExecutable;

    private SourceControlHelper scmHelper;

    private SettingsBean settingsBean;

    private TemplatePackageRegistry templatePackageRegistry;

    public JahiaTemplatesPackage compileAndDeploy(final String moduleName, File sources, JCRSessionWrapper session)
            throws RepositoryException, IOException, BundleException {
        CompiledModuleInfo moduleInfo = compileModule(sources);
        Bundle bundle = BundleUtils.getBundle(moduleName, moduleInfo.getVersion());
        if (bundle != null) {
            FileInputStream is = new FileInputStream(moduleInfo.getFile());
            try {
                bundle.update(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
            return templatePackageRegistry.lookupByFileNameAndVersion(moduleInfo.getModuleName(), new ModuleVersion(
                    moduleInfo.getVersion()));
        }
        // No existing module found, deploy new one
        FileInputStream is = new FileInputStream(moduleInfo.getFile());
        try {
            bundle = FrameworkService.getBundleContext().installBundle(moduleInfo.getFile().toURI().toString(), is);
        } finally {
            IOUtils.closeQuietly(is);
        }
        bundle.start();
        return templatePackageRegistry.lookupByFileNameAndVersion(moduleInfo.getModuleName(), new ModuleVersion(
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
                r = ProcessHelper.execute(mavenExecutable, "clean install", null, sources, out, out);
            } catch (JahiaRuntimeException e) {
                logger.error(e.getCause().getMessage(), e.getCause());
                throw e;
            }

            if (r > 0) {
                logger.error("Compilation error, returned status " + r);
                logger.error("Maven out : " + out);
                throw new IOException("Compilation error, status " + r);
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

    public JCRNodeWrapper createModule(final String moduleName, final String artifactId, final String moduleType, final File moduleSources,
            final JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        File sources = moduleSources; 
        if (sources == null) {
            sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources");
            if (!sources.exists() && !sources.mkdirs()) {
                throw new IOException("Unable to create path for: " + sources);
            }
        }

        String finalFolderName = null;

        if (!sources.exists()) {
            finalFolderName = sources.getName();
            sources = sources.getParentFile();
            if (sources == null) {
                sources = new File(SettingsBean.getInstance().getJahiaVarDiskPath() + "/sources");
            }
            if (!sources.exists() && !sources.mkdirs()) {
                throw new IOException("Unable to create path for: " + sources);
            }
        }

        String[] archetypeParams = { "archetype:generate", "-DarchetypeCatalog=" + mavenArchetypeCatalog + ",local",
                "-DarchetypeGroupId=org.jahia.archetypes", "-DarchetypeArtifactId=jahia-" + (moduleType.equals("jahiapp") ? "app" : moduleType) + "-archetype",
                "-Dversion=1.0-SNAPSHOT", "-DmoduleName=" + moduleName, "-DartifactId=" + artifactId,
                "-DjahiaPackageVersion=" + Constants.JAHIA_PROJECT_VERSION, "-DinteractiveMode=false" };

        StringBuilder out = new StringBuilder();
        int ret = ProcessHelper.execute(mavenExecutable, StringUtils.join(archetypeParams, " "), null, sources, out,
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

        JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        scmHelper.setSourcesFolderInPackageAndNode(pack, path, node);
        session.save();

        return node;
    }

    public void deployToMaven(ModuleReleaseInfo releaseInfo, File generatedWar) throws IOException {
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
            JarFile jar = new JarFile(generatedWar);
            pomFile = extractPomFromJar(jar);
            jar.close();

            Model pom;
            try {
                pom = PomUtils.read(pomFile);
            } catch (XmlPullParserException e) {
                throw new IOException(e);
            }

            String[] deployParams = { "deploy:deploy-file", "-Dfile=" + generatedWar,
                    "-DrepositoryId=" + releaseInfo.getRepositoryId(), "-Durl=" + releaseInfo.getRepositoryUrl(),
                    "-DpomFile=" + pomFile.getPath(),
                    "-Dpackaging=" + StringUtils.substringAfterLast(generatedWar.getName(), "."),
                    "-DgroupId=" + pom.getGroupId(), "-DartifactId=" + pom.getArtifactId(),
                    "-Dversion=" + pom.getVersion() };
            if (settings != null) {
                deployParams = (String[]) ArrayUtils.addAll(deployParams,
                        new String[] { "--settings", settings.getPath() });
            }
            StringBuilder out = new StringBuilder();
            int ret = ProcessHelper.execute(mavenExecutable, StringUtils.join(deployParams, " "), null,
                    generatedWar.getParentFile(), out, out);

            if (ret > 0) {
                logger.error("Maven archetype call returned " + ret);
                logger.error("Maven out : " + out);
                throw new IOException("Maven invocation failed");
            }
        } finally {
            FileUtils.deleteQuietly(settings);
            FileUtils.deleteQuietly(pomFile);
        }
    }

    private File extractPomFromJar(JarFile jar) throws IOException {
        // deploy artifacts to Maven distribution server
        Enumeration<JarEntry> jarEntries = jar.entries();
        JarEntry jarEntry = null;
        boolean found = false;
        String moduleName = jar.getManifest().getMainAttributes().getValue("Jahia-Root-Folder");
        if (StringUtils.isEmpty(moduleName)) {
            moduleName = jar.getManifest().getMainAttributes().getValue("root-folder");
        }
        while (jarEntries.hasMoreElements()) {
            jarEntry = jarEntries.nextElement();
            String name = jarEntry.getName();
            if (StringUtils.startsWith(name, "META-INF/maven/") && StringUtils.endsWith(name, moduleName + "/pom.xml")) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IOException("unable to find pom.xml file within while looking for " + moduleName);
        }
        InputStream is = jar.getInputStream(jarEntry);
        File pomFile = File.createTempFile("pom", ".xml");
        FileUtils.copyInputStreamToFile(is, pomFile);
        return pomFile;
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
            String[] installParams = new String[] { "release:prepare", "release:stage",
                    "-Dmaven.home=" + getMavenHome(), "-Dtag=" + tag, "-DreleaseVersion=" + releaseVersion,
                    "-DdevelopmentVersion=" + nextVersion, "-DignoreSnapshots=true",
                    "-DstagingRepository=tmp::default::" + tmpRepo.toURI().toString(), "--batch-mode" };
            StringBuilder out = new StringBuilder();
            ret = ProcessHelper.execute(mavenExecutable, StringUtils.join(installParams, " "), null, sources, out, out);

            FileUtils.deleteDirectory(tmpRepo);

            if (ret > 0) {
                logger.error("Maven release call returnedError release, maven out : " + out);
                logger.error("Error when releasing, maven out : " + out);
                ProcessHelper.execute(mavenExecutable, "release:rollback", null, sources, out, out);
                logger.error("Rollback release : " + out);
                throw new IOException("Maven invocation failed");
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

    public void setMavenArchetypeCatalog(String mavenArchetypeCatalog) {
        this.mavenArchetypeCatalog = mavenArchetypeCatalog;
    }

    public void setMavenExecutable(String mavenExecutable) {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !mavenExecutable.endsWith(".bat")) {
            mavenExecutable = mavenExecutable + ".bat";
        }
        this.mavenExecutable = mavenExecutable;
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
}
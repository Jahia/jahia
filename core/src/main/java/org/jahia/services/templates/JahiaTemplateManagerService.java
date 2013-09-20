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

import com.google.common.collect.ImmutableSet;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.apache.xerces.impl.dv.util.Base64;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.bin.errors.ErrorHandler;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.data.templates.ModuleState;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.JahiaService;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.rules.BackgroundAction;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.PomUtils;
import org.jahia.utils.i18n.ResourceBundles;
import org.json.JSONException;
import org.json.JSONObject;
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
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.xml.transform.TransformerException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.apache.commons.httpclient.HttpStatus.SC_OK;

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

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("/modules/[^/]*/[^/]*/templates/(.*)");

    private static Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerService.class);

    private static final Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    private Map<Bundle, ModuleState> moduleStates = new TreeMap<Bundle, ModuleState>();

    private OutputFormat prettyPrint = OutputFormat.createPrettyPrint();

    private TemplatePackageDeployer templatePackageDeployer;

    private TemplatePackageRegistry templatePackageRegistry;

    private JahiaSitesService siteService;

    private HttpClientService httpClientService;

    private ApplicationEventPublisher applicationEventPublisher;

    private ModuleBuildHelper moduleBuildHelper;
    
    private ModuleInstallationHelper moduleInstallationHelper;
    
    private SourceControlHelper scmHelper;

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

    public JCRNodeWrapper checkoutModule(File moduleSources, String scmURI, String branchOrTag, String moduleName,
            String version, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        return scmHelper.checkoutModule(moduleSources, scmURI, branchOrTag, moduleName, version, session);
    }

    public JCRNodeWrapper createModule(String moduleName, String artifactId, String moduleType, File sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        return moduleBuildHelper.createModule(moduleName, artifactId, moduleType, sources, session);
    }

    public JahiaTemplatesPackage deployModule(File warFile, JCRSessionWrapper session) throws RepositoryException {
        return templatePackageDeployer.deployModule(warFile, session);
    }

    public JahiaTemplatesPackage compileAndDeploy(final String moduleName, File sources, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {
        return moduleBuildHelper.compileAndDeploy(moduleName, sources, session);
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
            String moduleName = model.getArtifactId();

            JahiaTemplatesPackage pack = compileAndDeploy(moduleName, sources, session);
            pack.setSourcesFolder(sources);
            JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
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
        JCRNodeWrapper n = session.getNode("/modules/" + pack.getRootFolderWithVersion());
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

    public void sendToSourceControl(String moduleName, String scmURI, String scmType, JCRSessionWrapper session) throws RepositoryException, IOException {
        scmHelper.sendToSourceControl(moduleName, scmURI, scmType, session);
    }


    public boolean checkValidSources(JahiaTemplatesPackage pack, File sources) {
        return scmHelper.checkValidSources(pack, sources);
    }

    public File releaseModule(String moduleName, ModuleReleaseInfo releaseInfo, JCRSessionWrapper session) throws RepositoryException, IOException, BundleException {
        JahiaTemplatesPackage pack = templatePackageRegistry.lookupByFileName(moduleName);
        if (pack.getVersion().isSnapshot() && releaseInfo != null && releaseInfo.getNextVersion() != null) {
            File sources = getSources(pack, session);
            if (sources != null) {
                JCRNodeWrapper vi = session.getNode("/modules/" + pack.getRootFolderWithVersion() + "/j:versionInfo");
                regenerateImportFile(moduleName, sources, session);

                if (vi.hasProperty("j:scmURI")) {
                    SourceControlManagement scm = null;
                    scm = scmHelper.getSourceControlFactory().getSourceControlManagement(sources);
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
        Model model = null;
        try {
            model = PomUtils.read(pom);
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

        FileInputStream is = new FileInputStream(generatedWar);
        try {
            FrameworkService.getBundleContext().installBundle(generatedWar.toURI().toString(), is);
        } finally {
            IOUtils.closeQuietly(is);
        }
        JahiaTemplatesPackage pack = compileAndDeploy(module.getRootFolder(), sources, session);
        JCRNodeWrapper node = session.getNode("/modules/" + pack.getRootFolderWithVersion());
        node.getNode("j:versionInfo").setProperty("j:sourcesFolder", sources.getPath());
        if (scmUrl != null) {
            node.getNode("j:versionInfo").setProperty("j:scmURI", scmUrl);
        }
        session.save();

        undeployModule(module);

        activateModuleVersion(module.getRootFolder(), releaseInfo.getNextVersion());

        if (releaseInfo.isPublishToMaven() || releaseInfo.isPublishToForge()) {
            releaseInfo.setArtifactUrl(computeModuleJarUrl(releaseVersion, releaseInfo, model));
            if (releaseInfo.isPublishToForge() && releaseInfo.getForgeUrl() != null) {
                String forgeModuleUrl = createForgeModule(releaseInfo, generatedWar);
                releaseInfo.setForgeModulePageUrl(forgeModuleUrl);
            } else if (releaseInfo.isPublishToMaven() && releaseInfo.getRepositoryUrl() != null) {
                deployToMaven(releaseInfo, generatedWar);
            }
        }

        return generatedWar;
    }

    /**
     * Manage forge
     */
    public String createForgeModule(ModuleReleaseInfo releaseInfo, File jar) throws IOException {
        String moduleUrl = null;

        Part[] parts = { new StringPart("action","action"), new FilePart("file",jar) };

        final String url = releaseInfo.getForgeUrl();
        PostMethod postMethod = new PostMethod(url + "/contents/forge-modules-repository.createModuleFromJar.do");
        postMethod.getParams().setSoTimeout(0);
        postMethod.addRequestHeader("Authorization", "Basic " + Base64.encode((releaseInfo.getUsername() + ":" + releaseInfo.getPassword()).getBytes()));
        postMethod.addRequestHeader("accept", "application/json");
        postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        HttpClient client = httpClientService.getHttpClient();

        String result = null;
        try {
            client.executeMethod(null, postMethod);
            StatusLine statusLine = postMethod.getStatusLine();

            if (statusLine != null && statusLine.getStatusCode() == SC_OK) {
                result = postMethod.getResponseBodyAsString();
            } else {
                logger.warn("Connection to URL: " + url + " failed with status " + statusLine);
            }

        } catch (HttpException e) {
            logger.error("Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Unable to get the content of the URL: " + url + ". Cause: " + e.getMessage(), e);
        } finally {
            postMethod.releaseConnection();
        }

        if (StringUtils.isNotEmpty(result)) {
            try {
                JSONObject json = new JSONObject(result);
                if (!json.isNull("moduleAbsoluteUrl")) {
                    moduleUrl = json.getString("moduleAbsoluteUrl");
                } else if (!json.isNull("error")) {
                    throw new IOException(json.getString("error"));
                } else {
                    logger.warn("Cannot find 'moduleAbsoluteUrl' entry in the create module actin response: {}", result);
                    throw new IOException("unknown");
                }
            } catch (JSONException e) {
                logger.error("Unable to parse the response of the module creation action. Cause: " + e.getMessage(), e);
            }
        }

        return moduleUrl;
    }

    public void deployToMaven(ModuleReleaseInfo releaseInfo, File generatedWar) throws IOException {
        moduleBuildHelper.deployToMaven(releaseInfo, generatedWar);
    }

    public File extractPomFromJar(JarFile jar) throws IOException {
        return extractPomFromJar(jar,null);
    }

    public File extractPomFromJar(JarFile jar, String groupId) throws IOException {
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
            String path = groupId!=null?groupId:"";
            if (StringUtils.startsWith(name, "META-INF/maven/" + path) && StringUtils.endsWith(name, moduleName + "/pom.xml")) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw  new IOException("unable to find pom.xml file within while looking for " + moduleName);
        }
        InputStream is = jar.getInputStream(jarEntry);
        File pomFile = File.createTempFile("pom",".xml");
        FileUtils.copyInputStreamToFile(is, pomFile);
        return pomFile;
    }


    private String computeModuleJarUrl(String releaseVersion, ModuleReleaseInfo releaseInfo, Model model) {
        StringBuilder url = new StringBuilder(64);
        url.append(releaseInfo.getRepositoryUrl());
        if (!releaseInfo.getRepositoryUrl().endsWith("/")) {
            url.append("/");
        }
        String groupId = model.getGroupId();
        if (groupId == null && model.getParent() != null) {
            groupId = model.getParent().getGroupId();
        }
        url.append(StringUtils.replace(groupId, ".", "/"));
        url.append("/");
        url.append(model.getArtifactId());
        url.append("/");
        url.append(releaseVersion);
        url.append("/");
        url.append(model.getArtifactId());
        url.append("-");
        url.append(releaseVersion);
        url.append(".");
        String packaging = model.getPackaging();
        url.append(packaging == null || packaging.equals("bundle") ? "jar" : packaging);

        return url.toString();
    }

    public List<File> regenerateImportFile(String moduleName, File sources, JCRSessionWrapper session) throws RepositoryException {
        List<File> modifiedFiles = new ArrayList<File>();

        if (session.getLocale() != null) {
            logger.error("Cannot generated export with i18n session");
            return modifiedFiles; 
        }
        
        SourceControlManagement scm = null;
        try {
            scm = scmHelper.getSourceControlFactory().getSourceControlManagement(sources);
        } catch (Exception e) {
            logger.error("Cannot get SCM", e);
        }

        // Handle import
        File sourcesImportFolder = new File(sources, "src/main/import");

        JahiaTemplatesPackage aPackage = getTemplatePackageByFileName(moduleName);

        try {
            regenerateImportFileInternal(session, modifiedFiles, sourcesImportFolder, aPackage);

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

    private void regenerateImportFileInternal(JCRSessionWrapper session, List<File> modifiedFiles, File sourcesImportFolder,
            JahiaTemplatesPackage aPackage) throws FileNotFoundException, RepositoryException, SAXException,
            IOException, TransformerException, PathNotFoundException {
        File f = File.createTempFile("import", null);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(ImportExportService.XSL_PATH, SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/export/templatesCleanup.xsl");
        FileOutputStream out = new FileOutputStream(f);
        try {
            ImportExportBaseService.getInstance().exportZip(
                    session.getNode("/modules/" + aPackage.getRootFolderWithVersion()), session.getRootNode(), out,
                    params);
        } finally {
            IOUtils.closeQuietly(out);
        }
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(f));
            ZipEntry zipentry;
            while ((zipentry = zis.getNextEntry()) != null) {
                if (!zipentry.isDirectory()) {
                    try {
                        String name = zipentry.getName();
                        name = name.replace(aPackage.getRootFolderWithVersion(), aPackage.getRootFolder());
                        File sourceFile = new File(sourcesImportFolder, name);
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
    }

    private void setDependenciesInPom(File sources, List<String> dependencies) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element e = document.getRootElement();
            List<?> elements = e.elements("build");
            if (elements.isEmpty()) {
                e = e.addElement("build");
            } else {
                e = (Element) elements.get(0);
            }
            elements = e.elements("plugins");
            if (elements.isEmpty()) {
                e = e.addElement("plugins");
            } else {
                e = (Element) elements.get(0);
            }
            Element pluginArtifactId = (Element) e.selectSingleNode("*[name()='plugin']/*[name()='artifactId' and text()='maven-bundle-plugin']");
            if (pluginArtifactId == null) {
                pluginArtifactId = (Element) e.selectSingleNode("*[name()='plugin']/*[name()='artifactId' and text()='maven-war-plugin']");
                if (pluginArtifactId == null) {
                    e = e.addElement("plugin");
                    e.addElement("groupId").setText("org.apache.felix");
                    e.addElement("artifactId").setText("maven-bundle-plugin");
                    e.addElement("extensions").setText("true");
                    e = e.addElement("configuration");
                    e = e.addElement("instructions");
                    e = e.addElement("Jahia-Depends");
                } else {
                    e = pluginArtifactId.getParent();
                    e = (Element) e.elements("configuration").get(0);
                    e = (Element) e.elements("archive").get(0);
                    e = (Element) e.elements("manifestEntries").get(0);
                    e = (Element) e.elements("depends").get(0);
                }
            } else {
                e = pluginArtifactId.getParent();
                elements = e.elements("configuration");
                if (elements.isEmpty()) {
                    e = e.addElement("configuration");
                } else {
                    e = (Element) elements.get(0);
                }
                elements = e.elements("instructions");
                if (elements.isEmpty()) {
                    e = e.addElement("instructions");
                } else {
                    e = (Element) elements.get(0);
                }
                elements = e.elements("Jahia-Depends");
                if (elements.isEmpty()) {
                    e = e.addElement("Jahia-Depends");
                } else {
                    e = (Element) elements.get(0);
                }
            }
            e.setText(StringUtils.join(dependencies, ","));
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileOutputStream(modifiedPom), prettyPrint);
            try {
                writer.write(document);
            } finally {
                writer.close();
            }
            InputStream source = new BufferedInputStream(new FileInputStream(modifiedPom));
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
            if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
                throw new IOException("Unable to create path for: " + target.getParentFile());
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
        Map<String, SortedMap<ModuleVersion, JahiaTemplatesPackage>> all = templatePackageRegistry.getAllModuleVersions();
        List<JahiaTemplatesPackage> packages = new LinkedList<JahiaTemplatesPackage>();
        for (String m : modules) {
            JahiaTemplatesPackage pkg = getTemplatePackageByFileName(m);
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

    public void autoInstallModulesToSites(JahiaTemplatesPackage module, JCRSessionWrapper session)
            throws RepositoryException {
        moduleInstallationHelper.autoInstallModulesToSites(module, session);
    }

    /**
     * Install module in provided list of site
     * @param module : module to install
     * @param sessionWrapper : session to use
     * @param sites : list of sites on which deploy the module
     * @throws RepositoryException
     */
    public void installModuleOnAllSites(JahiaTemplatesPackage module, JCRSessionWrapper sessionWrapper, List<JCRNodeWrapper> sites) throws RepositoryException {
        moduleInstallationHelper.installModuleOnAllSites(module, sessionWrapper, sites);
    }

    public void installModule(final String module, final String sitePath, String username)
            throws RepositoryException {
        moduleInstallationHelper.installModule(module, sitePath, username);
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        installModules(Arrays.asList(module), sitePath, session);
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
        uninstallModules(Arrays.asList(module), sitePath, session);
    }


    public void uninstallModules(final List<JahiaTemplatesPackage> modules, final String sitePath, final JCRSessionWrapper session) throws RepositoryException {
        moduleInstallationHelper.uninstallModules(modules, sitePath, session);
    }

    public void uninstallModulesFromAllSites(final String module, final String username, final boolean purgeAllContent) throws RepositoryException {
        moduleInstallationHelper.uninstallModulesFromAllSites(module, username, purgeAllContent);
    }

    public void uninstallModulesFromAllSites(final JahiaTemplatesPackage module, final JCRSessionWrapper session) throws RepositoryException {
        uninstallModulesFromAllSites(Arrays.asList(module), session);
    }

    public void uninstallModulesFromAllSites(final List<JahiaTemplatesPackage> modules, final JCRSessionWrapper session) throws RepositoryException {
        moduleInstallationHelper.uninstallModulesFromAllSites(modules, session);
    }

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
                                            "select * from [jnt:module] as module " +
                                                    "inner join [jnt:moduleVersion] as version on ischildnode(version,module) " +
                                                    "where isdescendantnode(module,'/modules') " +
                                                    "and name(module) <> 'templates-system' " +
                                                    "and version.[j:moduleType]='templatesSet'",
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

    public JahiaTemplatesPackage activateModuleVersion(String rootFolder, String version) throws RepositoryException, BundleException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByFileNameAndVersion(rootFolder, new ModuleVersion(
                version));
        module.getBundle().start();
        return module;
    }

    public JahiaTemplatesPackage stopModule(String rootFolder) throws RepositoryException, BundleException {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByFileName(rootFolder);
        module.getBundle().stop();
        return module;
    }

    public void undeployModule(String module, String version) throws RepositoryException {
        undeployModule(templatePackageRegistry.lookupByFileNameAndVersion(module, new ModuleVersion(version)));
    }

    public void undeployModule(JahiaTemplatesPackage pack) throws RepositoryException {
        templatePackageDeployer.undeployModule(pack);
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
                String basePath = "/base";
                JCRNodeWrapper folder = JCRContentUtils
                        .getParentOfType(node, "jnt:templatesFolder");
                if (folder != null && folder.hasProperty("j:rootTemplatePath")) {
                    basePath = folder.getProperty("j:rootTemplatePath").getString();
                }
                if (StringUtils.isNotEmpty(basePath) && !"/".equals(basePath)
                        && pathToCheck.equals(basePath + "/" + templateName)) {
                    // matched it considering the base path
                    found = true;
                    break;
                }
            }
        }

        return found;
    }


    public Map<Bundle, ModuleState> getModuleStates() {
        return moduleStates;
    }

    /**
     * Returns list of module bundles in the specified state.
     *
     * @param state
     *            the state of the module to be considered
     * @return list of module bundles in the specified state or an empty list if there no modules in that state
     */
    public List<Bundle> getModulesByState(ModuleState.State state) {
        List<Bundle> modules = new LinkedList<Bundle>();
        for (Map.Entry<Bundle, ModuleState> entry : moduleStates.entrySet()) {
            if (entry.getValue().getState().equals(state)) {
                modules.add(entry.getKey());
            }
        }

        return !modules.isEmpty() ? modules : Collections.<Bundle> emptyList();
    }

    public void setModuleStates(Map<Bundle, ModuleState> moduleStates) {
        this.moduleStates = moduleStates;
    }

    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
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
     * @param scmHelper
     *            an instance of the SCM helper
     */
    public void setSourceControlHelper(SourceControlHelper scmHelper) {
        this.scmHelper = scmHelper;
    }

    public void setModuleBuildHelper(ModuleBuildHelper moduleBuildHelper) {
        this.moduleBuildHelper = moduleBuildHelper;
    }
}
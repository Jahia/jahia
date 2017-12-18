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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.tika.io.IOUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleReleaseInfo;
import org.jahia.data.templates.ModuleState;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.JahiaCndReader;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.SourceControlManagement;
import org.jahia.utils.PomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Module life cycle related methods.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleHelper {

    private static Logger logger = LoggerFactory.getLogger(ModuleHelper.class);

    private static ModuleReleaseInfo toModuleReleaseInfo(GWTModuleReleaseInfo gwtInfo) {
        ModuleReleaseInfo info = new ModuleReleaseInfo();
        info.setNextVersion(gwtInfo.getNextVersion());

        info.setPublishToMaven(gwtInfo.isPublishToMaven());
        info.setRepositoryId(gwtInfo.getRepositoryId());
        info.setRepositoryUrl(gwtInfo.getRepositoryUrl());

        info.setPublishToForge(gwtInfo.isPublishToForge());
        info.setForgeUrl(gwtInfo.getForgeUrl());
        info.setUsername(gwtInfo.getUsername());
        info.setPassword(gwtInfo.getPassword());

        return info;
    }

    private HttpClientService httpClient;

    private NavigationHelper navigation;

    private JahiaTemplateManagerService templateManagerService;

    public void addToSourceControl(String moduleId, GWTJahiaNode node, JCRSessionWrapper session) throws IOException,
            RepositoryException, BundleException {
        JahiaTemplatesPackage templatePackage = templateManagerService.getTemplatePackageById(moduleId);
        SourceControlManagement sourceControl = templatePackage.getSourceControl();
        if (sourceControl != null) {
            String path = node.getPath();
            path = path.substring(path.indexOf("/sources/") + 8);
            String sourcesFolderPath = templatePackage.getSourcesFolder().getAbsolutePath();
            sourceControl.add(new File(sourcesFolderPath + path));
        }
    }

    public GWTJahiaNode checkoutModule(String moduleId, String scmURI, String scmType, String branchOrTag,
                                       String sources, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        GWTJahiaNode node = null;

        String fullUri = "scm:" + scmType + ":" + scmURI;
        JCRNodeWrapper nodeWrapper = templateManagerService.checkoutModule(sources != null ? new File(sources) : null, fullUri, branchOrTag, moduleId,
                null, session);
        if (nodeWrapper != null) {
            node = navigation.getGWTJahiaNode(nodeWrapper.getParent(), GWTJahiaNode.DEFAULT_SITE_FIELDS);
        }
        return node;
    }

    public void compileAndDeploy(String moduleId, JCRSessionWrapper session) throws IOException, RepositoryException,
            BundleException {
        File sources = getSources(moduleId, session);
        JahiaTemplatesPackage templatePackage = templateManagerService.getTemplatePackageById(moduleId);
        for (String def : templatePackage.getDefinitionsFiles()) {
            File defFile = new File(sources,"src" + File.separator + "main" + File.separator + "resources" + File.separator + def);
            JahiaCndReader r = new JahiaCndReader(new FileReader(defFile),defFile.getName(),moduleId, NodeTypeRegistry.getInstance());
            try {
                r.parse();
            } catch (ParseException e) {
                throw new IOException(e.getMessage());
            }
        }
        templateManagerService.regenerateImportFile(moduleId, sources, session);
        JahiaTemplatesPackage jahiaTemplatesPackage = templateManagerService.compileAndDeploy(moduleId, sources, session);
        final ModuleState state = jahiaTemplatesPackage.getState();
        if (state.getState() == ModuleState.State.SPRING_NOT_STARTED) {
            final Throwable details = (Throwable) state.getDetails();
            throw new IOException(details.getMessage(), details);
        }
    }

    public GWTJahiaNode createModule(String moduleName, String artifactId, String groupId, final String siteType, String sources,
                                     JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = templateManagerService.createModule(moduleName, artifactId, groupId, siteType,
                    sources != null ? new File(sources) : null, session);
            return node != null ? navigation.getGWTJahiaNode(node.getParent(), GWTJahiaNode.DEFAULT_SITE_FIELDS)
                    : null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot create module " + moduleName + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    public void deployModule(final String moduleName, final String sitePath, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        try {
            templateManagerService.installModule(moduleName, sitePath, currentUserSession.getUser().getName());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Cannot deploy module " + moduleName + ". Cause: " + e.getLocalizedMessage(), e);
        }
    }

    private String updateRepositoryUrlFromForge(File pomFile, String forgeUrl, String username, String password) throws XmlPullParserException, IOException {
        String forgeFileUrl = forgeUrl + ".json";
        logger.info("Trying to retrieve Jahia repository information from resource at {}", forgeFileUrl);
        long timer = System.currentTimeMillis();

        String info = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            if (username != null && password != null) {
                headers.put("Authorization", "Basic " + Base64.encode((username + ":" + password).getBytes()));
            }
            info = httpClient.executeGet(forgeFileUrl, headers);
        } catch (IllegalArgumentException e) {
            // Malformed URL
            return null;
        }

        if (info != null) {
            info = info.trim();
            try {
                JSONObject obj = new JSONObject(info);

                logger.info("Retrieved Jahia Private App Store information in {} ms. The URL to the Private App Store is: {}", System.currentTimeMillis() - timer,
                        info);

                PomUtils.updateDistributionManagement(pomFile, obj.getString("forgeSettingsId"), obj.getString("forgeSettingsUrl"));
            } catch (JSONException e) {
                throw new IOException("Cannot get info from Private App Store", e);
            }
        } else {
            throw new IOException("Cannot connect to Private App Store server");
        }
        return null;
    }

    public GWTModuleReleaseInfo getModuleDistributionInfo(String moduleId, JCRSessionWrapper session)
            throws RepositoryException, IOException, XmlPullParserException {
        JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById(moduleId);
        if (pack == null || !pack.getVersion().isSnapshot()) {
            return null;
        }

        File sources = templateManagerService.getSources(pack, session);
        if (sources == null) {
            return null;
        }

        Model pom = PomUtils.read(new File(sources, "pom.xml"));
        DistributionManagement distributionManagement = pom.getDistributionManagement();

        GWTModuleReleaseInfo info = new GWTModuleReleaseInfo();

        if (distributionManagement != null && distributionManagement.getRepository() != null) {
            String repositoryUrl = distributionManagement.getRepository().getUrl();

            info.setRepositoryId(distributionManagement.getRepository().getId());
            info.setRepositoryUrl(repositoryUrl);

        }
        if (pom.getProperties().containsKey("jahia-private-app-store")) {
            final String property = pom.getProperties().getProperty("jahia-private-app-store");
            info.setForgeUrl(property);
            String pageUrl = property + "/contents/modules-repository/";
            if (StringUtils.isNotEmpty(pack.getGroupId())) {
                pageUrl += pack.getGroupId().replace(".", "/") + "/";
            }
            pageUrl += moduleId + ".html";
            info.setForgeModulePageUrl(pageUrl);
        }

        return info;
    }

    private File getSources(String moduleId, JCRSessionWrapper session) throws RepositoryException {
        return templateManagerService.getSources(templateManagerService.getTemplatePackageById(moduleId),
                session);
    }

    public GWTJahiaNode releaseModule(String moduleId, GWTModuleReleaseInfo gwtReleaseInfo, JCRSessionWrapper session)
            throws RepositoryException, IOException, BundleException {
        String nextVersion = gwtReleaseInfo != null ? gwtReleaseInfo.getNextVersion() : null;
        File f;
        if (nextVersion != null) {
            ModuleReleaseInfo releaseInfo = toModuleReleaseInfo(gwtReleaseInfo);
            f = templateManagerService.releaseModule(moduleId, releaseInfo, session);
            gwtReleaseInfo.setForgeModulePageUrl(releaseInfo.getForgeModulePageUrl());
            gwtReleaseInfo.setArtifactUrl(releaseInfo.getArtifactUrl());
        } else {
            JahiaTemplatesPackage previous = templateManagerService.getTemplatePackageById(moduleId);
            f = templateManagerService.compileModule(previous.getSourcesFolder());
        }
        if (f == null) {
            return null;
        }

        JCRNodeWrapper privateFolder = JCRContentUtils.getInstance().getUserPrivateFilesFolder(session);

        if (!privateFolder.hasNode("modules")) {
            session.checkout(privateFolder);
            privateFolder.addNode("modules", Constants.JAHIANT_FOLDER);
        }
        JCRNodeWrapper parent = privateFolder.getNode("modules");
        session.checkout(parent);
        InputStream is = new BufferedInputStream(new FileInputStream(f));
        try {
            JCRNodeWrapper res = parent.uploadFile(f.getName(), is, "application/x-zip");
            session.save();

            return navigation.getGWTJahiaNode(res);
        } finally {
            IOUtils.closeQuietly(is);
            FileUtils.deleteQuietly(f);
        }
    }

    public boolean saveAndCommitModule(String moduleId, String message, JCRSessionWrapper session)
            throws RepositoryException, IOException {
        SourceControlManagement scm = null;
        File sources = getSources(moduleId, session);
        try {
            scm = templateManagerService.getTemplatePackageById(moduleId).getSourceControl();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (scm != null) {
            templateManagerService.regenerateImportFile(moduleId, sources, session);
            return scm.commit(message);
        } else {
            throw new IOException("No SCM configured");
        }
    }

    public GWTJahiaNode sendToSourceControl(String moduleName, String scmURI, String scmType, JCRSessionWrapper session)
            throws IOException, RepositoryException {
        templateManagerService.sendToSourceControl(moduleName, scmURI, scmType, session);
        return navigation.getGWTJahiaNode(session.getNode("/modules/" + moduleName), GWTJahiaNode.DEFAULT_SITE_FIELDS);
    }

    public void setHttpClient(HttpClientService httpClient) {
        this.httpClient = httpClient;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    /**
     * Updates the module's pom.xml file with the specified Private App Store server details.
     *
     * @param module   the module to update distribution management information
     * @param forgeUrl the target Private App Store URL
     * @param session  current JCR session
     * @param username
     * @param password @throws XmlPullParserException
     *                 in case of pom.xml parsing error
     * @throws IOException         in case of an I/O failure
     * @throws RepositoryException when a repository access exception occurs
     */
    public void updateForgeUrlForModule(String module, String forgeUrl,
                                        JCRSessionWrapper session, String username, String password) throws IOException, XmlPullParserException, RepositoryException {
        File sources = getSources(module, session);
        if (sources != null) {
            JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById(module);
            if (pack != null && templateManagerService.checkValidSources(pack, sources)) {
                File pomFile = new File(sources, "pom.xml");

                // fetch repository info from Private App Store
                updateRepositoryUrlFromForge(pomFile, forgeUrl, username, password);

                PomUtils.updateForgeUrl(pomFile, forgeUrl);

                SourceControlManagement scm = pack.getSourceControl();

                if (scm != null) {
                    scm.add(pomFile);
                    scm.commit("Updated distribution server information");
                }
            }
        }
    }

    /**
     * Updates the module's pom.xml file with the specified distribution server details.
     *
     * @param module        the module to update distribution management information
     * @param repositoryId  the server ID for the repository
     * @param repositoryUrl the target repository URL
     * @param session       current JCR session
     * @throws XmlPullParserException in case of pom.xml parsing error
     * @throws IOException            in case of an I/O failure
     * @throws RepositoryException    when a repository access exception occurs
     */
    public void updateDistributionServerForModule(String module, String repositoryId, String repositoryUrl,
                                                  JCRSessionWrapper session) throws IOException, XmlPullParserException, RepositoryException {
        File sources = getSources(module, session);
        if (sources != null) {
            JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById(module);
            if (pack != null && templateManagerService.checkValidSources(pack, sources)) {
                File pomFile = new File(sources, "pom.xml");
                PomUtils.updateDistributionManagement(pomFile, repositoryId, repositoryUrl);
                SourceControlManagement scm = pack.getSourceControl();
                if (scm != null) {
                    scm.add(pomFile);
                    scm.commit("Updated distribution server information");
                }
            }
        }
    }

    public String updateModule(String moduleId, JCRSessionWrapper session) throws IOException, RepositoryException,
            BundleException {
        String output = null;
        File sources = getSources(moduleId, session);

        SourceControlManagement scm = templateManagerService.getTemplatePackageById(moduleId).getSourceControl();
        if (scm != null) {
            templateManagerService.regenerateImportFile(moduleId, sources, session);
            output = scm.update();
            templateManagerService.compileAndDeploy(moduleId, sources, session);
        } else {
            throw new IOException("No SCM configured");
        }

        return output;
    }

}

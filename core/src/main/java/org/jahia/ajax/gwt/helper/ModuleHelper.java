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

package org.jahia.ajax.gwt.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
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
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.notification.HttpClientService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.SourceControlManagement;
import org.jahia.utils.PomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

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

    public void addToSourceControl(String moduleName, GWTJahiaNode node, JCRSessionWrapper session) throws IOException,
            RepositoryException, BundleException {
        JahiaTemplatesPackage templatePackage = templateManagerService.getTemplatePackage(moduleName);
        SourceControlManagement sourceControl = templatePackage.getSourceControl();
        if (sourceControl != null) {
            String path = node.getPath();
            path = path.substring(path.indexOf("/sources/") + 8);
            String sourcesFolderPath = templatePackage.getSourcesFolder().getAbsolutePath();
            File file = new File(sourcesFolderPath + "/src/main/resources" + path);
            if (!file.exists()) {
                file = new File(sourcesFolderPath + "/src/main/webapp" + path);
            }
            sourceControl.setModifiedFile(Arrays.asList(file));
        }
    }

    public GWTJahiaNode checkoutModule(String moduleName, String scmURI, String scmType, String branchOrTag,
            JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        GWTJahiaNode node = null;

        String fullUri = "scm:" + scmType + ":" + scmURI;
        JCRNodeWrapper nodeWrapper = templateManagerService.checkoutModule(null, fullUri, branchOrTag, moduleName,
                null, session);
        if (nodeWrapper != null) {
            node = navigation.getGWTJahiaNode(nodeWrapper.getParent(), GWTJahiaNode.DEFAULT_SITE_FIELDS);
        }
        return node;
    }

    public void compileAndDeploy(String moduleName, JCRSessionWrapper session) throws IOException, RepositoryException,
            BundleException {
        File sources = getSources(moduleName, session);

        templateManagerService.regenerateImportFile(moduleName, sources, session);
        templateManagerService.compileAndDeploy(moduleName, sources, session);
    }

    public GWTJahiaNode createModule(String key, String baseSet, final String siteType, String sources,
            JCRSessionWrapper session) throws GWTJahiaServiceException {
        String shortName = JCRContentUtils.generateNodeName(key);
        if (baseSet == null) {
            try {
                JCRNodeWrapper node = templateManagerService.createModule(key, shortName, siteType,
                        sources != null ? new File(sources) : null, session);
                return node != null ? navigation.getGWTJahiaNode(node.getParent(), GWTJahiaNode.DEFAULT_SITE_FIELDS)
                        : null;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            // todo : add support for duplicate sources
        }
        return null;
    }

    public void deployModule(final String moduleName, final String sitePath, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        try {
            templateManagerService.installModule(moduleName, sitePath, currentUserSession.getUser().getUsername());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private String updateRepositoryUrlFromForge(File pomFile, String forgeUrl, String username, String password) throws XmlPullParserException, IOException {
        String forgeFileUrl = forgeUrl + ".json";
        logger.info("Trying to retrieve Jahia repository information from resource at {}", forgeFileUrl);
        long timer = System.currentTimeMillis();

        String info = null;
        try {
            Map<String,String> headers = new HashMap<String,String>();
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

                logger.info("Retrieved Jahia Catalog information in {} ms. The URL to the catalog is: {}", System.currentTimeMillis() - timer,
                        info);

                PomUtils.updateDistributionManagement(pomFile, obj.getString("forgeSettingsId"), obj.getString("forgeSettingsUrl"));
            } catch (JSONException e) {
                throw new IOException("Cannot get info from forge",e);
            }
        } else {
            throw new IOException("Cannot connect to forge server");
        }
        return null;
    }

    private String getJahiaForgeUrl(String repositoryUrl) {
        String forgeFileUrl = repositoryUrl.endsWith("/") ? (repositoryUrl + "jahia-catalog.properties")
                : (repositoryUrl + "/jahia-catalog.properties");
        logger.info("Trying to retrieve Jahia Catalog information from resourec at {}", forgeFileUrl);
        long timer = System.currentTimeMillis();

        String catalogInfo = null;
        try {
            catalogInfo = httpClient.executeGet(forgeFileUrl);
        } catch (IllegalArgumentException e) {
            // Malformed URL
            return null;
        }

        if (catalogInfo != null) {
            catalogInfo = catalogInfo.trim();
        }

        logger.info("Retrieved Jahia Catalog information in {} ms. The URL to the catalog is: {}", System.currentTimeMillis() - timer,
                catalogInfo);
        
        return catalogInfo;
    }

    public GWTModuleReleaseInfo getModuleDistributionInfo(String moduleName, JCRSessionWrapper session)
            throws RepositoryException, IOException, XmlPullParserException {
        JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageByFileName(moduleName);
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

//            if (repositoryUrl != null) {
//                String forgeUrl = getJahiaForgeUrl(repositoryUrl);
//                info.setForgeUrl(forgeUrl);
//                info.setForgeModulePageUrl(forgeUrl + "/contents/forge-modules-repository/" + moduleName + ".html");
//            }
        }
        if (pom.getProperties().containsKey("jahia-forge")) {
            final String property = pom.getProperties().getProperty("jahia-forge");
            info.setForgeUrl(property);
            info.setForgeModulePageUrl(property + "/contents/forge-modules-repository/" + moduleName + ".html");
        }

        return info;
    }

    private File getSources(String moduleName, JCRSessionWrapper session) throws RepositoryException {
        return templateManagerService.getSources(templateManagerService.getTemplatePackageByFileName(moduleName),
                session);
    }

    public GWTJahiaNode releaseModule(String moduleName, GWTModuleReleaseInfo gwtReleaseInfo, JCRSessionWrapper session)
            throws RepositoryException, IOException, BundleException {
        String nextVersion = gwtReleaseInfo != null ? gwtReleaseInfo.getNextVersion() : null;
        File f;
        if (nextVersion != null) {
            ModuleReleaseInfo releaseInfo = toModuleReleaseInfo(gwtReleaseInfo);
            f = templateManagerService.releaseModule(moduleName, releaseInfo, session);
            gwtReleaseInfo.setForgeModulePageUrl(releaseInfo.getForgeModulePageUrl());
            gwtReleaseInfo.setArtifactUrl(releaseInfo.getArtifactUrl());
        } else {
            JahiaTemplatesPackage previous = templateManagerService.getTemplatePackageByFileName(moduleName);
            f = templateManagerService.compileModule(previous.getSourcesFolder()).getFile();
        }
        if (f == null) {
            return null;
        }

        JCRNodeWrapper privateFolder = session.getNode(session.getUser().getLocalPath() + "/files/private");

        if (!privateFolder.hasNode("modules")) {
            if (!privateFolder.isCheckedOut()) {
                session.getWorkspace().getVersionManager().checkout(privateFolder.getPath());
            }
            privateFolder.addNode("modules", Constants.JAHIANT_FOLDER);
        }
        JCRNodeWrapper parent = privateFolder.getNode("modules");
        if (!parent.isCheckedOut()) {
            session.getWorkspace().getVersionManager().checkout(parent.getPath());
        }
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

    public void saveAndCommitModule(String moduleName, String message, JCRSessionWrapper session)
            throws RepositoryException, IOException {
        SourceControlManagement scm = null;
        File sources = getSources(moduleName, session);
        try {
            scm = templateManagerService.getSourceControlFactory().getSourceControlManagement(sources);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (scm != null) {
            scm.update();
            templateManagerService.regenerateImportFile(moduleName, sources, session);
            scm.commit(message);
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
     * Updates the module's pom.xml file with the specified forge server details.
     * 
     *
     * @param module
     *            the module to update distribution management information
     * @param forgeUrl
     *            the target forge URL
     * @param session
     *            current JCR session
     * @param username
     *@param password @throws XmlPullParserException
     *             in case of pom.xml parsing error
     * @throws IOException
     *             in case of an I/O failure
     * @throws RepositoryException
     *             when a repository access exception occurs
     */
    public void updateForgeUrlForModule(String module, String forgeUrl,
                                        JCRSessionWrapper session, String username, String password) throws IOException, XmlPullParserException, RepositoryException {
        File sources = getSources(module, session);
        if (sources != null) {
            JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageByFileName(module);
            if (pack != null && templateManagerService.checkValidSources(pack, sources)) {
                File pomFile = new File(sources, "pom.xml");

                // fetch repository info from forge
                updateRepositoryUrlFromForge(pomFile, forgeUrl, username, password);

                PomUtils.updateForgeUrl(pomFile, forgeUrl);

                SourceControlManagement scm = templateManagerService.getSourceControlFactory()
                        .getSourceControlManagement(sources);
                if (scm != null) {
                    scm.setModifiedFile(Lists.newArrayList(pomFile));
                    scm.commit("Updated distribution server information");
                }
            }
        }
    }

    /**
     * Updates the module's pom.xml file with the specified distribution server details.
     *
     * @param module
     *            the module to update distribution management information
     * @param repositoryId
     *            the server ID for the repository
     * @param repositoryUrl
     *            the target repository URL
     * @param session
     *            current JCR session
     * @throws XmlPullParserException
     *             in case of pom.xml parsing error
     * @throws IOException
     *             in case of an I/O failure
     * @throws RepositoryException
     *             when a repository access exception occurs
     */
    public void updateDistributionServerForModule(String module, String repositoryId, String repositoryUrl,
            JCRSessionWrapper session) throws IOException, XmlPullParserException, RepositoryException {
        File sources = getSources(module, session);
        if (sources != null) {
            JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageByFileName(module);
            if (pack != null && templateManagerService.checkValidSources(pack, sources)) {
                File pomFile = new File(sources, "pom.xml");
                PomUtils.updateDistributionManagement(pomFile, repositoryId, repositoryUrl);
                SourceControlManagement scm = templateManagerService.getSourceControlFactory()
                        .getSourceControlManagement(sources);
                if (scm != null) {
                    scm.setModifiedFile(Lists.newArrayList(pomFile));
                    scm.commit("Updated distribution server information");
                }
            }
        }
    }

    public void updateModule(String moduleName, JCRSessionWrapper session) throws IOException, RepositoryException,
            BundleException {
        File sources = getSources(moduleName, session);

        SourceControlManagement scm = templateManagerService.getSourceControlFactory().getSourceControlManagement(
                sources);
        if (scm != null) {
            templateManagerService.regenerateImportFile(moduleName, sources, session);
            scm.update();
            templateManagerService.compileAndDeploy(moduleName, sources, session);
        } else {
            throw new IOException("No SCM configured");
        }
    }

}

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.content.server;

import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.helper.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.tools.imageprocess.ImageProcess;
import org.jahia.utils.FileUtils;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

/**
 * GWT server code implementation for the DMS repository services.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public class JahiaContentManagementServiceImpl extends JahiaRemoteService implements JahiaContentManagementService {
    private static final transient Logger logger = Logger.getLogger(JahiaContentManagementServiceImpl.class);

    private JahiaPreferencesService preferencesService;
    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;
    private SearchHelper search;
    private PublicationHelper publication;
    private VersioningHelper versioning;
    private MashupHelper mashup;
    private ContentDefinitionHelper contentDefinition;
    private ContentHubHelper contentHub;
    private PropertiesHelper properties;
    private TemplateHelper template;
    private ZipHelper zip;
    private ACLHelper acl;

    public void setPreferencesService(JahiaPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public void setSearch(SearchHelper search) {
        this.search = search;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setVersioning(VersioningHelper versioning) {
        this.versioning = versioning;
    }

    public void setMashup(MashupHelper mashup) {
        this.mashup = mashup;
    }

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public void setContentHub(ContentHubHelper contentHub) {
        this.contentHub = contentHub;
    }

    public void setProperties(PropertiesHelper properties) {
        this.properties = properties;
    }

    public void setTemplate(TemplateHelper template) {
        this.template = template;
    }

    public void setZip(ZipHelper zip) {
        this.zip = zip;
    }

    public void setAcl(ACLHelper acl) {
        this.acl = acl;
    }

    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
        return navigation.ls(folder, nodeTypes, mimeTypes, filters, noFolders, true, retrieveParamBean());
    }

    public ListLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
        return new BaseListLoadResult<GWTJahiaNode>(navigation.ls(folder, nodeTypes, mimeTypes, filters, noFolders, true, retrieveParamBean()));
    }

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.size() == 0) {
            openPaths = getOpenPathsForRepository(repositoryType);
        }

        logger.debug(new StringBuilder("retrieving open paths for ").append(repositoryType).append(" :\n").append(openPaths).toString());

        return navigation.retrieveRoot(repositoryType, retrieveParamBean(), nodeTypes, mimeTypes, filters, selectedNodes, openPaths,
                false, null);
    }

    public void saveSelectedPath(String path) throws GWTJahiaServiceException {
        getThreadLocalRequest().getSession().setAttribute(navigation.SELECTED_PATH, path);
    }

    public String getSelecetedPath() throws GWTJahiaServiceException {
        return (String) getThreadLocalRequest().getSession().getAttribute(navigation.SELECTED_PATH);
    }

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths) throws GWTJahiaServiceException {
        getThreadLocalRequest().getSession().setAttribute(navigation.SAVED_OPEN_PATHS + repositoryType, paths);
    }

    private List<String> getOpenPathsForRepository(String repositoryType) {
        return (List<String>) getThreadLocalRequest().getSession().getAttribute(navigation.SAVED_OPEN_PATHS + repositoryType);
    }

    public List<GWTJahiaNode> search(String searchString, int limit) throws GWTJahiaServiceException {
        return search.search(searchString, limit, retrieveParamBean());
    }

    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters) throws GWTJahiaServiceException {
        return search.search(searchString, limit, nodeTypes, mimeTypes, filters, retrieveParamBean());
    }

    public GWTJahiaNode saveSearch(String searchString, String name) throws GWTJahiaServiceException {
        return search.saveSearch(searchString, name, retrieveParamBean());
    }

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException {
        return search.getSavedSearch(retrieveParamBean());
    }

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException {
        return navigation.getMountpoints(retrieveParamBean());
    }

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException {
        contentManager.setLock(paths, locked, getUser());
    }

    public void checkExistence(String path) throws GWTJahiaServiceException {
        if (contentManager.checkExistence(path)) {
            throw new ExistingFileException(path);
        }
    }

    public void createFolder(String parentPath, String name) throws GWTJahiaServiceException {

        contentManager.createFolder(parentPath, name, retrieveParamBean());
    }

    public void deletePaths(List<String> paths) throws GWTJahiaServiceException {
        contentManager.deletePaths(paths, getUser());
    }

    public String getDownloadPath(String path) throws GWTJahiaServiceException {
        return navigation.getDownloadPath(path, getUser());
    }

    public String getAbsolutePath(String path) throws GWTJahiaServiceException {
        return navigation.getAbsolutePath(path, retrieveParamBean());
    }

    public void checkWriteable(List<String> paths) throws GWTJahiaServiceException {
        contentManager.checkWriteable(paths, getUser());
    }

    public void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut) throws GWTJahiaServiceException {
        contentManager.pasteOnTopOf(pathsToCopy, destinationPath, newName, false, cut, false);
    }

    public void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException {
        contentManager.pasteOnTopOf(pathsToCopy, destinationPath, newName, false, false, true);
    }

    public void rename(String path, String newName) throws GWTJahiaServiceException {
        contentManager.rename(path, newName, getUser());
    }

    public GWTJahiaGetPropertiesResult getProperties(String path) throws GWTJahiaServiceException {
        ParamBean jParams = retrieveParamBean();
        GWTJahiaNode node = navigation.getNode(path, "default", jParams);
        try {
            JCRNodeWrapper nodeWrapper = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession().getNode(node.getPath());
            jParams.setAttribute("contextNode", nodeWrapper);
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
        }
        List<GWTJahiaNodeType> nodeTypes = contentDefinition.getNodeTypes(node.getNodeTypes(), jParams);
        Map<String, GWTJahiaNodeProperty> props = properties.getProperties(path, jParams);
        GWTJahiaGetPropertiesResult result = new GWTJahiaGetPropertiesResult(nodeTypes, props);
        result.setNode(node);
        return result;
    }

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, String captcha) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        if (captcha != null && !contentManager.checkCaptcha(context, captcha)) {
            throw new GWTJahiaServiceException("Invalid captcha");
        }

        if (captcha != null) {
            return contentManager.unsecureCreateNode(parentPath, name, nodeType, mixin, props);
        } else {
            return contentManager.createNode(parentPath, name, nodeType, mixin, props, context);
        }
    }

    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException {
        properties.saveProperties(nodes, newProps, retrieveParamBean());
    }

    public void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException {
        saveProperties(nodes, newProps);
        if (acl != null) {
            for (GWTJahiaNode node : nodes) {
                setACL(node.getPath(), acl);
            }
        }
    }


    public GWTJahiaNodeACL getACL(String path) throws GWTJahiaServiceException {
        return contentManager.getACL(path, retrieveParamBean());
    }

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException {
        contentManager.setACL(path, acl);
    }

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException {
        return navigation.getUsages(path);
    }

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException {
        zip.zip(paths, archiveName);
    }

    public void unzip(List<String> paths) throws GWTJahiaServiceException {
        zip.unzip(paths, false);
    }

    public String getFileManagerUrl() throws GWTJahiaServiceException {
        ParamBean jParams = retrieveParamBean();
        try {
            return jParams.composeEngineUrl("filemanager");
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void mount(String path, String target, String root) throws GWTJahiaServiceException {
        contentHub.mount(target, root, getUser());
    }

    private JahiaUser getUser() {
        return getRemoteJahiaUser();
    }

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target)) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();

            processor.setRoi(left, top, width, height);
            processor = processor.crop();
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target)) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();
            processor = processor.resize(width, height);
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (contentManager.checkExistence(node.getPath().replace(node.getName(), target)) && !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }

            File tmp = File.createTempFile("image", "tmp");
            FileUtils.copyStream(node.getFileContent().downloadFile(), new FileOutputStream(tmp));
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            ImageProcessor processor = ip.getProcessor();
            if (clockwise) {
                processor = processor.rotateRight();
            } else {
                processor = processor.rotateLeft();
            }
            ip.setProcessor(null, processor);

            File f = File.createTempFile("image", "tmp");
            ImageProcess.save(op.getFileType(tmp.getPath()), ip, f);
            ((JCRNodeWrapper) node.getParent()).uploadFile(target, new FileInputStream(f), node.getFileContent().getContentType());
            node.getParent().save();
            tmp.delete();
            f.delete();
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException {
        try {
            return mashup.searchPortlets(match, retrieveParamBean());
        } catch (Exception e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException {
        versioning.activateVersioning(path);
    }

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException {
        try {
            return navigation.getVersions(JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path), retrieveParamBean());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException {
        return mashup.createPortletInstance(path, wiz);
    }

    public GWTJahiaNode createRSSPortletInstance(String path, String name, String url) throws GWTJahiaServiceException {
        return mashup.createRSSPortletInstance(path, name, url, retrieveParamBean());
    }

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException {
        return mashup.createGoogleGadgetPortletInstance(path, name, script, retrieveParamBean());
    }

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException {
        return acl.createUsersGroupACE(permissions, grand, retrieveParamBean());
    }

    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion) throws GWTJahiaServiceException {
        String nodeUuid = gwtJahiaNodeVersion.getNode().getUUID();
        String versiOnUuid = gwtJahiaNodeVersion.getUUID();
        versioning.restoreNode(nodeUuid, versiOnUuid, retrieveParamBean());
    }

    public void uploadedFile(String location, String tmpName, int operation, String newName) throws GWTJahiaServiceException {
        contentManager.uploadedFile(location, tmpName, operation, newName);
    }

    public String getRenderedContent(String path, String workspace, String locale, String template, String templateWrapper, Map<String, String> contextParams, boolean editMode) throws GWTJahiaServiceException {
        return this.template.getRenderedContent(path, workspace, LanguageCodeConverters.languageCodeToLocale(locale), template, templateWrapper, contextParams, editMode, retrieveParamBean());
    }

    public Boolean isFileAccessibleForCurrentContainer(String path) throws GWTJahiaServiceException {
        return true;//Boolean.valueOf(contentManager.isFileAccessibleForCurrentContainer(retrieveParamBean(), path));
    }

    public Map<String, String> getStoredPasswordsProviders() {
        return contentHub.getStoredPasswordsProviders(getUser());
    }

    public void storePasswordForProvider(String providerKey, String username, String password) {
        contentHub.storePasswordForProvider(getUser(), providerKey, username, password);
    }

    public String getExportUrl(String path) throws GWTJahiaServiceException {
        return retrieveParamBean().composeSiteUrl() + "/engineName/export" + path + ".xml?path=" + path;
    }

    public void importContent(String parentPath, String fileKey) throws GWTJahiaServiceException {
        contentManager.importContent(parentPath, fileKey);
    }

    public void move(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.move(sourcePath, targetPath);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void moveOnTopOf(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.moveOnTopOf(getUser(), sourcePath, targetPath);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void moveAtEnd(String sourcePath, String targetPath) throws GWTJahiaServiceException {
        try {
            contentManager.moveAtEnd(sourcePath, targetPath);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaNode> getNodesWithPublicationInfo(List<String> pathes) throws GWTJahiaServiceException {
        String workspace = "default";
        ParamBean jParams = retrieveParamBean();
        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
        for (String path : pathes) {
            try {
                GWTJahiaNode gwtJahiaNode = navigation.getNode(path, workspace, jParams);
                gwtJahiaNode.setPublicationInfo(getPublicationInfo(gwtJahiaNode.getPath(), false));
                list.add(gwtJahiaNode);
            } catch (GWTJahiaServiceException e) {
                logger.debug(e, e);
            }
        }
        return list;
    }

    public List<String[]> getTemplatesPath(String path) throws GWTJahiaServiceException {
        return template.getTemplatesSet(path, retrieveParamBean());
    }


    public void pasteReferencesOnTopOf(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException {
        contentManager.pasteOnTopOf(pathsToCopy, destinationPath, newName, true, false, true);
    }

    public void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> properties, String captcha) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        final GWTJahiaNode parentNode = navigation.getParentNode(path, "default", context);
        final GWTJahiaNode jahiaNode = contentManager.createNode(parentNode.getPath(), name, nodeType, mixin, properties, context);
        try {
            contentManager.moveOnTopOf(context.getUser(), jahiaNode.getPath(), path);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void saveSearch(String searchString, String path, String name) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        final GWTJahiaNode jahiaNode = search.saveSearch(searchString, path, name, context);
    }

    public void saveSearchOnTopOf(String searchString, String path, String name) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        final GWTJahiaNode parentNode = navigation.getParentNode(path, "default", context);
        final GWTJahiaNode jahiaNode = search.saveSearch(searchString, parentNode.getPath(), name, context);
        try {
            contentManager.moveOnTopOf(context.getUser(), jahiaNode.getPath(), path);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


    public void saveNodeTemplate(String path, String template) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(path);
            if (!node.isCheckedOut()) {
                node.checkout();
            }
//jmix:renderableReference

            if ("--unset--".equals(template)) {
                if (node.hasProperty("j:template")) {
                    node.getProperty("j:template").remove();
                }
            } else {
                if (!node.isNodeType("jmix:renderable")) {
                    node.addMixin("jmix:renderable");
                    node.save();
                }
                node.setProperty("j:template", template);
            }
            node.save();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

    }

    /**
     * Publish the specified path.
     *
     * @param path the path to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    public void publish(String path, Set<String> languages, boolean allSubTree, String comments) throws GWTJahiaServiceException {
        publication.publish(path, languages, allSubTree);
    }

    /**
     * Publish the specified paths.
     *
     * @param paths the list of node paths to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    public void publish(List<String> paths) throws GWTJahiaServiceException {
        publication.publish(paths, null, retrieveParamBean().getUser(), false);
    }

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param path the path to unpublish, will not unpublish the references
     * @throws GWTJahiaServiceException
     */
    public void unpublish(String path) throws GWTJahiaServiceException {
        long l = System.currentTimeMillis();
        publication.unpublish(path, null, retrieveParamBean().getUser());
        System.out.println("-->" + (System.currentTimeMillis() - l));
    }

    /**
     * Get the publication status information for a particular path.
     *
     * @param path path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaPublicationInfo getPublicationInfo(String path, boolean includeReferences) throws GWTJahiaServiceException {
        return publication.getPublicationInfo(path, null, includeReferences);
    }

    /**
     * Get the publication status information for multiple pathes.
     *
     * @param pathes path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    public Map<String, GWTJahiaPublicationInfo> getPublicationInfo(List<String> pathes) throws GWTJahiaServiceException {
        Map<String, GWTJahiaPublicationInfo> map = new HashMap<String, GWTJahiaPublicationInfo>();
        for (String path : pathes) {
            map.put(path, publication.getPublicationInfo(path, null, false));
        }

        return map;
    }

    /**
     * Get a node by its path if existing.
     *
     * @param path path o fthe node you want
     * @return the founded node if existing
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          if node does not exist
     */
    public GWTJahiaNode getNode(String path) throws GWTJahiaServiceException {
        return navigation.getNode(path, "default", retrieveParamBean());
    }

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths, boolean forceCreate) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.size() == 0) {
            openPaths = getOpenPathsForRepository(repositoryType);
        }

        logger.debug(new StringBuilder("retrieving open paths for ").append(repositoryType).append(" :\n").append(openPaths).toString());

        return navigation.retrieveRoot(repositoryType, retrieveParamBean(), nodeTypes, mimeTypes, filters, selectedNodes, openPaths, forceCreate, contentManager);
    }

    public List<GWTJahiaNode> getNodesOfType(String nodeType) throws GWTJahiaServiceException {
        return search.getNodesOfType(nodeType, retrieveParamBean());
    }

    public void pasteOnTopOf(List<String> nodes, String path, String newName, boolean cut) throws GWTJahiaServiceException {
        contentManager.pasteOnTopOf(nodes, path, newName, true, cut, false);
    }
}
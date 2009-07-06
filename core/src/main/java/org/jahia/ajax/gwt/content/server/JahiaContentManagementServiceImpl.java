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
import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.definitions.server.ContentDefinitionHelper;
import org.jahia.ajax.gwt.content.server.helper.ContentManagerHelper;
import org.jahia.ajax.gwt.content.server.helper.JCRVersioningHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.tools.imageprocess.ImageProcess;
import org.jahia.utils.FileUtils;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

/**
 * GWT server code implementation for the DMS repository services.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public class JahiaContentManagementServiceImpl extends JahiaRemoteService implements JahiaContentManagementService {
    private static final transient Logger logger = Logger.getLogger(JahiaContentManagementServiceImpl.class);

    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders) throws GWTJahiaServiceException {
        return ContentManagerHelper.ls(folder, nodeTypes, mimeTypes, filters, openPaths, noFolders, retrieveParamBean());
    }

    public ListLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders) throws GWTJahiaServiceException {
        return new BaseListLoadResult<GWTJahiaNode>(ContentManagerHelper.ls(folder, nodeTypes, mimeTypes, filters, openPaths, noFolders, retrieveParamBean())) ;
    }

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, String openPaths) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.length() == 0) {
            openPaths = getOpenPathsForRepository(repositoryType);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(new StringBuilder("retrieving open paths for ").append(repositoryType).append(" :\n").append(openPaths).toString());
        }
        return ContentManagerHelper.retrieveRoot(repositoryType, retrieveParamBean(), nodeTypes, mimeTypes, filters, openPaths);
    }

    public void saveOpenPaths(Map<String, List<String>> pathsForRepositoryType) throws GWTJahiaServiceException {
        for (String repositoryType : pathsForRepositoryType.keySet()) {
            List<String> paths = pathsForRepositoryType.get(repositoryType);
            if (paths != null && paths.size() > 0) {
                if (logger.isDebugEnabled()) {
                    StringBuilder s = new StringBuilder("saving open paths for ").append(repositoryType).append(" :");
                    for (String p : paths) {
                        s.append("\n\t").append(p);
                    }
                    logger.debug(s.toString());
                }
                setGenericPreferenceValue(ContentManagerHelper.SAVED_OPEN_PATHS + repositoryType, ContentManagerHelper.concatOpenPathsList(paths));
            } else if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("no paths to save for ").append(repositoryType).toString());
                //deleteGenericPreferenceValue(ContentManagerHelper.SAVED_OPEN_PATHS + repositoryType);
            }
        }
    }

    private String getOpenPathsForRepository(String repositoryType) {
        return getGenericPreferenceValue(ContentManagerHelper.SAVED_OPEN_PATHS + repositoryType);
    }

    public List<GWTJahiaNode> search(String searchString, int limit) throws GWTJahiaServiceException {
        return ContentManagerHelper.search(searchString, limit, retrieveParamBean());
    }

    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters) throws GWTJahiaServiceException {
        return ContentManagerHelper.search(searchString, limit, nodeTypes, mimeTypes, filters, retrieveParamBean());
    }

    public GWTJahiaNode saveSearch(String searchString, String name) throws GWTJahiaServiceException {
        return ContentManagerHelper.saveSearch(searchString, name, retrieveParamBean());
    }

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException {
        return ContentManagerHelper.getSavedSearch(retrieveParamBean());
    }

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException {
        return ContentManagerHelper.getMountpoints(retrieveParamBean());
    }

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException {
        ContentManagerHelper.setLock(paths, locked, getUser());
    }

    public void checkExistence(String path) throws GWTJahiaServiceException {
        if (ContentManagerHelper.checkExistence(path, getUser())) {
            throw new ExistingFileException(path);
        }
    }

    public void createFolder(String parentPath, String name) throws GWTJahiaServiceException {
        ContentManagerHelper.createFolder(parentPath, name, retrieveParamBean());
    }

    public void deletePaths(List<String> paths) throws GWTJahiaServiceException {
        ContentManagerHelper.deletePaths(paths, getUser());
    }

    public String getDownloadPath(String path) throws GWTJahiaServiceException {
        return ContentManagerHelper.getDownloadPath(path, getUser());
    }

    public String getAbsolutePath(String path) throws GWTJahiaServiceException {
        return ContentManagerHelper.getAbsolutePath(path, retrieveParamBean());
    }

    public void copy(List<GWTJahiaNode> paths) throws GWTJahiaServiceException {
        ContentManagerHelper.copy(paths, getUser());
    }

    public void cut(List<GWTJahiaNode> paths) throws GWTJahiaServiceException {
        ContentManagerHelper.cut(paths, getUser());
    }

    public void paste(List<GWTJahiaNode> pathsToCopy, String destinationPath, boolean cut) throws GWTJahiaServiceException {
        ContentManagerHelper.paste(pathsToCopy, destinationPath, cut, getUser());
    }

    public void pasteReference(List<GWTJahiaNode> pathsToCopy, String destinationPath) throws GWTJahiaServiceException {
        ContentManagerHelper.pasteReference(pathsToCopy, destinationPath, getUser());
    }

    public void rename(String path, String newName) throws GWTJahiaServiceException {
        ContentManagerHelper.rename(path, newName, getUser());
    }

    public GWTJahiaGetPropertiesResult getProperties(String path) throws GWTJahiaServiceException {
        ParamBean jParams = retrieveParamBean();
        GWTJahiaNode node = ContentManagerHelper.getNode(path, jParams.getUser());
        List<GWTJahiaNodeType> nodeTypes = ContentDefinitionHelper.getNodeTypes(node.getNodeTypes(), jParams);
        Map<String, GWTJahiaNodeProperty> props = ContentManagerHelper.getProperties(path, jParams);
        return new GWTJahiaGetPropertiesResult(nodeTypes, props);
    }

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, String captcha) throws GWTJahiaServiceException {
        ParamBean context = retrieveParamBean();
        if (captcha != null && !ContentManagerHelper.checkCaptcha(context, captcha)) {
            throw new GWTJahiaServiceException("Invalid captcha");
        }

        if (captcha != null) {
            return ContentManagerHelper.unsecureCreateNode(parentPath, name, nodeType, props, context);
        } else {
            return ContentManagerHelper.createNode(parentPath, name, nodeType, props, context);
        }
    }

    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException {
        ContentManagerHelper.saveProperties(nodes, newProps, getUser());
    }

    public GWTJahiaNodeACL getACL(String path) throws GWTJahiaServiceException {
        return ContentManagerHelper.getACL(path, retrieveParamBean());
    }

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException {
        ContentManagerHelper.setACL(path, acl, retrieveParamBean());
    }

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException {
        return ContentManagerHelper.getUsages(path, retrieveParamBean());
    }

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException {
        ContentManagerHelper.zip(paths, archiveName, getUser());
    }

    public void unzip(List<String> paths) throws GWTJahiaServiceException {
        ContentManagerHelper.unzip(paths, false, getUser());
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
        ContentManagerHelper.mount(target, root, getUser());
    }

    private JahiaUser getUser() {
        return getRemoteJahiaUser();
    }

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException {
        JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
        try {
            JCRNodeWrapper node = jcr.getThreadSession(getUser()).getNode(path);
            if (ContentManagerHelper.checkExistence(node.getPath().replace(node.getName(), target), getUser()) && !forceReplace) {
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
        JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
        try {
            JCRNodeWrapper node = jcr.getThreadSession(getUser()).getNode(path);
            if (ContentManagerHelper.checkExistence(node.getPath().replace(node.getName(), target), getUser()) && !forceReplace) {
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
        JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
        try {
            JCRNodeWrapper node = jcr.getThreadSession(getUser()).getNode(path);
            if (ContentManagerHelper.checkExistence(node.getPath().replace(node.getName(), target), getUser()) && !forceReplace) {
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
            return ContentManagerHelper.searchPortlets(retrieveParamBean());
        } catch (Exception e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException {
        JCRVersioningHelper.activateVersioning(path, retrieveParamBean());
    }

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException {
        try {
            JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
            return JCRVersioningHelper.getVersions(jcr.getThreadSession(getUser()).getNode(path), retrieveParamBean());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException {
        return ContentManagerHelper.createPortletInstance(path, wiz, retrieveParamBean());
    }

    public GWTJahiaNode createRSSPortletInstance(String path,String name,String url) throws GWTJahiaServiceException {
        return ContentManagerHelper.createRSSPortletInstance(path, name, url, retrieveParamBean());
    }

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException{
      return ContentManagerHelper.createGoogleGadgetPortletInstance(path, name, script, retrieveParamBean());
    }

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException {
        return ACLHelper.createUsersGroupACE(permissions, grand, retrieveParamBean());
    }

    public void renameUploadedFile(String location, String tmpName, int operation, String newName)  throws GWTJahiaServiceException {
        ContentManagerHelper.renameUploadedFile(location, tmpName, operation, newName, retrieveParamBean());
    }

    public String getRenderedContent(String path) throws GWTJahiaServiceException {
        return ContentManagerHelper.getRenderedContent(path, retrieveParamBean());
    }

}
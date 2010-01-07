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
package org.jahia.ajax.gwt.client.service.content;

import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaContentManagementServiceAsync {

    void ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders, AsyncCallback<List<GWTJahiaNode>> async);

    void lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders, AsyncCallback<ListLoadResult<GWTJahiaNode>> async);

    void getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths, AsyncCallback<List<GWTJahiaNode>> async);

    void getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths,boolean forceCreate, AsyncCallback<List<GWTJahiaNode>> async);

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths, AsyncCallback async);
    
    void search(String searchString, int limit, AsyncCallback<List<GWTJahiaNode>> async);

    void search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, AsyncCallback<List<GWTJahiaNode>> async);

    void searchPortlets(String match, AsyncCallback<List<GWTJahiaPortletDefinition>> async);

    void saveSearch(String searchString, String name, AsyncCallback<GWTJahiaNode> async);

    void getSavedSearch(AsyncCallback<List<GWTJahiaNode>> async);

    void getMountpoints(AsyncCallback<List<GWTJahiaNode>> async);

    void setLock(List<String> paths, boolean locked, AsyncCallback async);

    void checkExistence(String path,AsyncCallback async);

    void createFolder(String parentPath, String name, AsyncCallback async);

    void deletePaths(List<String> path, AsyncCallback async);

    void getDownloadPath(String path, AsyncCallback<String> async);

    void getAbsolutePath(String path, AsyncCallback<String> async);

    void copy(List<GWTJahiaNode> paths, AsyncCallback async);

    void cut(List<GWTJahiaNode> paths, AsyncCallback async);

    void paste(List<GWTJahiaNode> pathsToCopy, String destinationPath, boolean cut, AsyncCallback async);

    void pasteReferences(List<GWTJahiaNode> pathsToCopy, String destinationPath, AsyncCallback async);

    void rename(String path, String newName, AsyncCallback async);

    void getProperties(String path, AsyncCallback<GWTJahiaGetPropertiesResult> async);

    void createNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, String captcha, AsyncCallback<GWTJahiaNode> async);

    void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, AsyncCallback async);

    void savePropertiesAndACL(List<GWTJahiaNode> nodes,GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> newProps, AsyncCallback async);

    void getACL(String path, AsyncCallback<GWTJahiaNodeACL> async);

    void setACL(String path, GWTJahiaNodeACL acl, AsyncCallback async);

    void getUsages(String path, AsyncCallback<List<GWTJahiaNodeUsage>> async);

    void zip(List<String> paths, String archiveName, AsyncCallback async);

    void unzip(List<String> paths, AsyncCallback async);

    void getFileManagerUrl(AsyncCallback<String> async);

    void mount(String path, String target, String root, AsyncCallback async);

    void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace, AsyncCallback async);

    void resizeImage(String path, String target, int width, int height, boolean forceReplace, AsyncCallback async);

    void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, AsyncCallback async);

    void activateVersioning(List<String> path, AsyncCallback async);

    void getVersions(String path, AsyncCallback<List<GWTJahiaNodeVersion>> async);

    void createPortletInstance(String path, GWTJahiaNewPortletInstance wiz, AsyncCallback<GWTJahiaNode> async);

    void createRSSPortletInstance(String path,String name, String url, AsyncCallback<GWTJahiaNode> async);

    void createGoogleGadgetPortletInstance(String path, String name, String script, AsyncCallback<GWTJahiaNode> async);

    void createDefaultUsersGroupACE(List<String> permissions, boolean grand, AsyncCallback<GWTJahiaNodeACE> async);

    void uploadedFile(String location, String tmpName, int operation, String newName, AsyncCallback async);

    void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion,AsyncCallback async);    

    void getRenderedContent(String path, String workspace, String locale, String template, String templateWrapper, Map<String,String> contextParams, boolean editMode, AsyncCallback<String> async);

    void isFileAccessibleForCurrentContainer(String path, AsyncCallback<Boolean> async);

    void getStoredPasswordsProviders(AsyncCallback<Map<String, String>> async) ;

    void storePasswordForProvider(String providerKey, String username, String password, AsyncCallback async);

    void getExportUrl(String path, AsyncCallback<String> async);

    void importContent(String parentPath, String fileKey, AsyncCallback async);

    void move(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveOnTopOf(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveAtEnd(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void getNodesWithPublicationInfo(List<String> list, AsyncCallback<List<GWTJahiaNode>> async);

    void pasteReferencesOnTopOf(List<GWTJahiaNode> pathsToCopy, String destinationPath, AsyncCallback async);

    void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> properties, String captcha, AsyncCallback asyncCallback);

    void saveSearch(String searchString, String path, String name, AsyncCallback asyncCallback);

    void saveSearchOnTopOf(String searchString, String path, String name, AsyncCallback asyncCallback);

    void getTemplatesPath(String path, AsyncCallback<List<String[]>> async);

    void saveNodeTemplate(String path, String template , AsyncCallback async);

    /**
     * Publish the specified path.
     * @param path the path to publish, will not auto publish the parents
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     * @throws forward GWTJahiaServiceException
     */
    void publish(String path, Set<String> languages, boolean allSubTree, String comments, AsyncCallback async);

    /**
     * Publish the specified paths.
     * @param paths the list of node paths to publish
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     * @throws forward GWTJahiaServiceException
     */
    void publish(List<String> paths, AsyncCallback async);

    /**
     * Unpublish the specified path and its subnodes.
     * @param path the path to unpublish, will not unpublish the references
     * @param async Local implementation of callback to react on return for asynchronous call to unpublish
     * @throws forward GWTJahiaServiceException
     */
    void unpublish(String path, AsyncCallback async);

    /**
     * Get the publication status information for a particular path.
     * @param path path to get publication info from
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     * @throws forward GWTJahiaServiceException
     */
    void getPublicationInfo(String path, boolean includeReferences, AsyncCallback<GWTJahiaPublicationInfo> async);

    /**
     * Get the publication status information for a particular path.
     * @param pathes path to get publication info from
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     * @throws forward GWTJahiaServiceException
     */
    void getPublicationInfo(List<String> pathes, AsyncCallback<Map<String,GWTJahiaPublicationInfo>> async);

    /**
     * Get a node by its path if existing.
     * @param path of hte node we want
     * @param async callback to execute
     */
    void getNode(String path,AsyncCallback<GWTJahiaNode> async);

    void getNodesOfType(String nodeType,AsyncCallback<List<GWTJahiaNode>> async);

    void pasteOnTopOf(List<GWTJahiaNode> nodes, String path, AsyncCallback async);
}

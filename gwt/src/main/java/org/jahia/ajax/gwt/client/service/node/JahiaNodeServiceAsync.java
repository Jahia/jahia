/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.service.node;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.extjs.gxt.ui.client.data.ListLoadResult;

import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaNodeServiceAsync {

    void ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders, AsyncCallback<List<GWTJahiaNode>> async);

    void lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders, AsyncCallback<ListLoadResult<GWTJahiaNode>> async);

    void getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, String openPaths, AsyncCallback<List<GWTJahiaNode>> async);

    void saveOpenPaths(Map<String, List<String>> pathsForRepositoryType, AsyncCallback async);

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

    void rename(String path, String newName, AsyncCallback async);

    void getProperties(String path, AsyncCallback<GWTJahiaGetPropertiesResult> async);

    void createNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, String captcha, AsyncCallback<GWTJahiaNode> async);

    void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, AsyncCallback async);

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

    void createPortletInstance(String path, GWTJahiaNewPortletInstance wiz, AsyncCallback<GWTJahiaNode> async);

    void createRSSPortletInstance(String path,String name, String url, AsyncCallback<GWTJahiaNode> async);

    void createGoogleGadgetPortletInstance(String path, String name, String script, AsyncCallback<GWTJahiaNode> async);

    void createDefaultUsersGroupACE(List<String> permissions, boolean grand, AsyncCallback<GWTJahiaNodeACE> async);

}

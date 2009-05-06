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

import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.extjs.gxt.ui.client.data.ListLoadResult;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaNodeService extends RemoteService {

    public static class App {
        private static JahiaNodeServiceAsync app = null;

        public static synchronized JahiaNodeServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "node/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (JahiaNodeServiceAsync) GWT.create(JahiaNodeService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
            }
            return app;
        }
    }

    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders) throws GWTJahiaServiceException;

    public ListLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, String openPaths) throws GWTJahiaServiceException;

    public void saveOpenPaths(Map<String, List<String>> pathsForRepositoryType) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> search(String searchString, int limit) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters) throws GWTJahiaServiceException;

    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException;

    public GWTJahiaNode saveSearch(String searchString, String name) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException;

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException;

    public void checkExistence(String path) throws GWTJahiaServiceException;

    public void createFolder(String parentPath, String name) throws GWTJahiaServiceException;

    public void deletePaths(List<String> path) throws GWTJahiaServiceException;

    public String getDownloadPath(String path) throws GWTJahiaServiceException;

    public String getAbsolutePath(String path) throws GWTJahiaServiceException;

    public void copy(List<GWTJahiaNode> paths) throws GWTJahiaServiceException;

    public void cut(List<GWTJahiaNode> paths) throws GWTJahiaServiceException;

    public void paste(List<GWTJahiaNode> pathsToCopy, String destinationPath, boolean cut) throws GWTJahiaServiceException;

    public void rename(String path, String newName) throws GWTJahiaServiceException;

    public GWTJahiaGetPropertiesResult getProperties(String path) throws GWTJahiaServiceException;

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, String captcha) throws GWTJahiaServiceException;

    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException;

    public GWTJahiaNodeACL getACL(String path) throws GWTJahiaServiceException;

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException;

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException;

    public void unzip(List<String> paths) throws GWTJahiaServiceException;

    public String getFileManagerUrl() throws GWTJahiaServiceException;

    public void mount(String path, String target, String root) throws GWTJahiaServiceException;

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace) throws GWTJahiaServiceException;

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException;

    public GWTJahiaNode createRSSPortletInstance(String path,String name, String url) throws GWTJahiaServiceException;

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException;

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException;

}

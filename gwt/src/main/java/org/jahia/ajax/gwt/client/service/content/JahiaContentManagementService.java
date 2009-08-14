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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaContentManagementService extends RemoteService {

    public static class App {
        private static JahiaContentManagementServiceAsync app = null;

        public static synchronized JahiaContentManagementServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "contentManager.gwt";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                app = (JahiaContentManagementServiceAsync) GWT.create(JahiaContentManagementService.class);
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

    public void pasteReference(GWTJahiaNode pathsToCopy, String destinationPath, String name) throws GWTJahiaServiceException;

    public void pasteReferences(List<GWTJahiaNode> pathsToCopy, String destinationPath) throws GWTJahiaServiceException;

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

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException;

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException;

    public GWTJahiaNode createRSSPortletInstance(String path,String name, String url) throws GWTJahiaServiceException;

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException;

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException;

    public void uploadedFile(String location, String tmpName, int operation, String newName)  throws GWTJahiaServiceException;

    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion) throws GWTJahiaServiceException;    

    public String getRenderedContent(String path) throws GWTJahiaServiceException;

    public Boolean isFileAccessibleForCurrentContainer(String path) throws GWTJahiaServiceException;
}

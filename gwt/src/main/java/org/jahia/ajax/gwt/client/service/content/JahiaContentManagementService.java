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
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.URL;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Remote GWT service for content management tasks.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaContentManagementService extends RemoteService, RoleRemoteService {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JahiaContentManagementServiceAsync ---------------------

    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException;

    public ListLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getRoot(String repositoryType, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths, boolean forceCreate) throws GWTJahiaServiceException;

    /**
     * Get a node by its path if existing.
     *
     * @param path path o fthe node you want
     * @return the founded node if existing
     * @throws GWTJahiaServiceException if node does not exist
     */
    public GWTJahiaNode getNode(String path) throws GWTJahiaServiceException;

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> search(String searchString, int limit) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNode> search(GWTJahiaSearchQuery search,int limit,int offset) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters) throws GWTJahiaServiceException;

    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException;

    public GWTJahiaNode saveSearch(String searchString, String name) throws GWTJahiaServiceException;

    public void saveSearch(String searchString, String path, String name) throws GWTJahiaServiceException;

    public void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name)  throws GWTJahiaServiceException;

    public void saveSearchOnTopOf(String searchString, String path, String name) throws GWTJahiaServiceException;

    public void mount(String path, String target, String root) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException;

    public void storePasswordForProvider(String providerKey, String username, String password) throws GWTJahiaServiceException;

    public Map<String, String> getStoredPasswordsProviders() throws GWTJahiaServiceException;

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException;

    public void deletePaths(List<String> path) throws GWTJahiaServiceException;

    public String getAbsolutePath(String path) throws GWTJahiaServiceException;

    public void checkWriteable(List<String> paths) throws GWTJahiaServiceException;

    public void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut) throws GWTJahiaServiceException;

    public void copyAndSaveProperties(List<String> pathsToCopy, String destinationPath, List<String> mixin, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> newsProps) throws GWTJahiaServiceException;

    public void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException;

    public void pasteOnTopOf(List<String> nodes, String path, String newName, boolean cut) throws GWTJahiaServiceException;

    public void pasteReferencesOnTopOf(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException;

    public GWTJahiaGetPropertiesResult getProperties(String path, String langCode) throws GWTJahiaServiceException;

    public GWTJahiaGetPropertiesResult getProperties(String path) throws GWTJahiaServiceException;

    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps) throws GWTJahiaServiceException;

    void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties) throws GWTJahiaServiceException;

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, String captcha) throws GWTJahiaServiceException;

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha) throws GWTJahiaServiceException;


    public void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha) throws GWTJahiaServiceException;

    public void createFolder(String parentPath, String name) throws GWTJahiaServiceException;

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException;

    public GWTJahiaNode createRSSPortletInstance(String path, String name, String url) throws GWTJahiaServiceException;

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException;

    public void checkExistence(String path) throws GWTJahiaServiceException;

    public void rename(String path, String newName) throws GWTJahiaServiceException;

    public void move(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public void moveAtEnd(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public void moveOnTopOf(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public GWTJahiaNodeACL getACL(String path) throws GWTJahiaServiceException;

    public GWTJahiaNodeACL getNewACL(String parentPath) throws GWTJahiaServiceException;

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException;

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException;

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException;

    public void unzip(List<String> paths) throws GWTJahiaServiceException;

    public String getExportUrl(String path) throws GWTJahiaServiceException;

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace) throws GWTJahiaServiceException;

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNodeVersion> getVersions(GWTJahiaNode node,String workspace,int limit,int offset)  throws GWTJahiaServiceException ;    

    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion) throws GWTJahiaServiceException;

    public void uploadedFile(String location, String tmpName, int operation, String newName) throws GWTJahiaServiceException;

    public GWTRenderResult getRenderedContent(String path, String workspace, String locale, String template, String templateWrapper, Map<String, String> contextParams, boolean editMode) throws GWTJahiaServiceException;

    public String getNodeURL(String path, String locale, int mode) throws GWTJahiaServiceException;

    public String getNodeURL(String path,String version,String workspace, String locale, int mode) throws GWTJahiaServiceException;

    public void importContent(String parentPath, String fileKey) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getNodesWithPublicationInfo(List<String> list) throws GWTJahiaServiceException;

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition) throws GWTJahiaServiceException;

    public void assignAndCompleteTask(String path, GWTJahiaWorkflowAction action, GWTJahiaWorkflowOutcome outcome) throws GWTJahiaServiceException;

    /**
     * Publish the specified path.
     *
     * @param path the path to publish, will not auto publish the parents
     */
    public void publish(String path, boolean allSubTree, String comments, boolean reverse) throws GWTJahiaServiceException;

    /**
     * Publish the specified paths.
     *
     * @param path the list of node paths to publish, will not auto publish the parents
     */
    public void publish(List<String> path, boolean reverse) throws GWTJahiaServiceException;

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param path the path to unpublish, will not unpublish the references
     */
    public void unpublish(String path) throws GWTJahiaServiceException;

    /**
     * Get the publication status information for a particular path.
     *
     * @param path path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     */
    public GWTJahiaPublicationInfo getPublicationInfo(String path, boolean includeReferences) throws GWTJahiaServiceException;

    /**
     * Get the publication status information for multiple pathes.
     *
     * @param pathes path to get publication info from
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     */
    public Map<String, GWTJahiaPublicationInfo> getPublicationInfo(List<String> pathes) throws GWTJahiaServiceException;

    public String getHighlighted(String original, String amendment)  throws GWTJahiaServiceException;


    public List<GWTJahiaLanguage> getSiteLanguages() throws GWTJahiaServiceException;
    
    /**
     * Retrieves a list of URL mapping objects for current node and locale.
     * @param node node to retrieve mapping for
     * @param locale current locale
     * @return a list of URL mapping objects for current node and locale
     * @throws GWTJahiaServiceException in case of an error
     */
    List<GWTJahiaUrlMapping> getUrlMappings(GWTJahiaNode node, String locale) throws GWTJahiaServiceException;
    
    /**
     * Updates the URL mapping for the specified node. 
     * @param node the node to be updated
     * @param updatedLocales locales that were edited
     * @param mappings URL mapping list to store
     * @throws GWTJahiaServiceException in case of an error
     */
    void saveUrlMappings(GWTJahiaNode node, Set<String> updatedLocales, List<GWTJahiaUrlMapping> mappings) throws GWTJahiaServiceException;

// -------------------------- INNER CLASSES --------------------------

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
}

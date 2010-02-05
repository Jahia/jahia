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
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowAction;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowOutcome;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
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

    /**
     * Get a node by its path if existing.
     * @param path of hte node we want
     * @param async callback to execute
     */
    void getNode(String path,AsyncCallback<GWTJahiaNode> async);

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths, AsyncCallback async);

    void search(String searchString, int limit, AsyncCallback<List<GWTJahiaNode>> async);

    void search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, AsyncCallback<List<GWTJahiaNode>> async);

    void searchPortlets(String match, AsyncCallback<List<GWTJahiaPortletDefinition>> async);

    void getSavedSearch(AsyncCallback<List<GWTJahiaNode>> async);

    void saveSearch(String searchString, String name, AsyncCallback<GWTJahiaNode> async);

    void saveSearch(String searchString, String path, String name, AsyncCallback asyncCallback);

    void saveSearchOnTopOf(String searchString, String path, String name, AsyncCallback asyncCallback);

    void mount(String path, String target, String root, AsyncCallback async);

    void getMountpoints(AsyncCallback<List<GWTJahiaNode>> async);

    void storePasswordForProvider(String providerKey, String username, String password, AsyncCallback async);

    void getStoredPasswordsProviders(AsyncCallback<Map<String, String>> async) ;

    void setLock(List<String> paths, boolean locked, AsyncCallback async);

    void deletePaths(List<String> path, AsyncCallback async);

    void getAbsolutePath(String path, AsyncCallback<String> async);

    void checkWriteable(List<String> paths, AsyncCallback async);

    void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut, AsyncCallback async);

    void copyAndSaveProperties(List<String> pathsToCopy, String destinationPath, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> newsProps, AsyncCallback async);

    void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName, AsyncCallback async);

    void pasteOnTopOf(List<String> nodes, String path, String newName, boolean cut, AsyncCallback async);

    void pasteReferencesOnTopOf(List<String> pathsToCopy, String destinationPath, String newName, AsyncCallback async);

    void getProperties(String path,String langCode,AsyncCallback<GWTJahiaGetPropertiesResult> async) ;

    void getProperties(String path, AsyncCallback<GWTJahiaGetPropertiesResult> async);

    void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, AsyncCallback async);

    void savePropertiesAndACL(List<GWTJahiaNode> nodes,GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, AsyncCallback async);

    void createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, String captcha, AsyncCallback<GWTJahiaNode> async);

    void createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props,Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha, AsyncCallback<GWTJahiaNode> async);


    void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties,Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, String captcha, AsyncCallback asyncCallback);

    void createFolder(String parentPath, String name, AsyncCallback async);

    void createPortletInstance(String path, GWTJahiaNewPortletInstance wiz, AsyncCallback<GWTJahiaNode> async);

    void createRSSPortletInstance(String path,String name, String url, AsyncCallback<GWTJahiaNode> async);

    void createGoogleGadgetPortletInstance(String path, String name, String script, AsyncCallback<GWTJahiaNode> async);

    void checkExistence(String path,AsyncCallback async);

    void rename(String path, String newName, AsyncCallback async);

    void move(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveAtEnd(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveOnTopOf(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void getACL(String path, AsyncCallback<GWTJahiaNodeACL> async);

    void getNewACL(String parentPath, AsyncCallback<GWTJahiaNodeACL> async) throws GWTJahiaServiceException;

    void setACL(String path, GWTJahiaNodeACL acl, AsyncCallback async);

    void createDefaultUsersGroupACE(List<String> permissions, boolean grand, AsyncCallback<GWTJahiaNodeACE> async);

    void getUsages(String path, AsyncCallback<List<GWTJahiaNodeUsage>> async);

    void zip(List<String> paths, String archiveName, AsyncCallback async);

    void unzip(List<String> paths, AsyncCallback async);

    void getExportUrl(String path, AsyncCallback<String> async);

    void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace, AsyncCallback async);

    void resizeImage(String path, String target, int width, int height, boolean forceReplace, AsyncCallback async);

    void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, AsyncCallback async);

    void activateVersioning(List<String> path, AsyncCallback async);

    void getVersions(String path, AsyncCallback<List<GWTJahiaNodeVersion>> async);

    void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion,AsyncCallback async);

    void uploadedFile(String location, String tmpName, int operation, String newName, AsyncCallback async);

    void getRenderedContent(String path, String workspace, String locale, String template, String templateWrapper, Map<String,String> contextParams, boolean editMode, AsyncCallback<GWTRenderResult> async);

    void getNodeURL(String path, String locale,  int mode,AsyncCallback<String> async);

    void importContent(String parentPath, String fileKey, AsyncCallback async);

    void getNodesWithPublicationInfo(List<String> list, AsyncCallback<List<GWTJahiaNode>> async);

    void getPermission(boolean currentSite, AsyncCallback<List<GWTJahiaPermission>> async) ;

    void getGrantedPermissions(AsyncCallback<List<GWTJahiaPermission>> async) ;


    void getRoles(String siteKey,boolean isGroup,String principalKey, AsyncCallback<List<GWTJahiaRole>> async);

    void getPrincipalsInRole(GWTJahiaRole role, AsyncCallback<List<GWTJahiaPrincipal>> async);

    void getRolesAndPermissions(String site, AsyncCallback<GWTRolesPermissions> async);


    void addRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions, AsyncCallback async);

    void removeRolePermissions(GWTJahiaRole role, List<GWTJahiaPermission> permissions, AsyncCallback async);

    void grantRoleToUser(GWTJahiaRole role, String principalKey, AsyncCallback async);

    void removeRoleToPrincipal(GWTJahiaRole role, String principalKey, AsyncCallback async);

    void grantRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals, AsyncCallback async);

    void removeRoleToPrincipals(GWTJahiaRole role, List<GWTJahiaPrincipal> principals, AsyncCallback async);

    void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition, AsyncCallback async);

    void assignAndCompleteTask(String path, GWTJahiaWorkflowAction action, GWTJahiaWorkflowOutcome outcome, AsyncCallback async); 

    /**
     * Publish the specified path.
     * @param path the path to publish, will not auto publish the parents
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     */
    void publish(String path, Set<String> languages, boolean allSubTree, String comments, boolean reverse, AsyncCallback async);

    /**
     * Publish the specified paths.
     * @param paths the list of node paths to publish
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     */
    void publish(List<String> paths, boolean reverse, AsyncCallback async);

    /**
     * Unpublish the specified path and its subnodes.
     * @param path the path to unpublish, will not unpublish the references
     * @param async Local implementation of callback to react on return for asynchronous call to unpublish
     */
    void unpublish(String path, AsyncCallback async);

    /**
     * Get the publication status information for a particular path.
     * @param path path to get publication info from
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     */
    void getPublicationInfo(String path, boolean includeReferences, AsyncCallback<GWTJahiaPublicationInfo> async);

    /**
     * Get the publication status information for a particular path.
     * @param pathes path to get publication info from
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     */
    void getPublicationInfo(List<String> pathes, AsyncCallback<Map<String,GWTJahiaPublicationInfo>> async);

    /**
     * Get site languages
     * @param async
     */
    void getSiteLanguages(AsyncCallback<List<GWTJahiaLanguage>> async);

}

/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.service.content;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Remote GWT service for content management tasks.
 *
 * @author rfelden
 * @version 5 mai 2008 - 17:23:39
 */
public interface JahiaContentManagementServiceAsync {

    void drawPortletInstanceOutput(String windowID, String entryPointIDStr, String pathInfo, String queryString, AsyncCallback<GWTJahiaPortletOutputBean> async);

    void getAvailableSites(AsyncCallback<List<GWTJahiaSite>> asyncCallback);

    void getManagerConfiguration(String name, AsyncCallback<GWTManagerConfiguration> async);

    void getEditConfiguration(String path, String name, AsyncCallback<GWTEditConfiguration> async);

    public void lsLoad(GWTJahiaNode folder, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, boolean checkSubChild, int limit, int offset, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex, boolean showOnlyNodesWithTemplates, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async) throws GWTJahiaServiceException;

    void getRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, List<String> selectedNodes, List<String> openPaths, boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex, AsyncCallback<List<GWTJahiaNode>> async);

    void getNodes(List<String> paths, List<String> fields, AsyncCallback<List<GWTJahiaNode>> async);

    void getNodesAndTypes(List<String> paths, List<String> fields, List<String> types, AsyncCallback<Map<String,List<? extends ModelData>>> async);

    void getTagNode(String tagName, boolean create, AsyncCallback<GWTJahiaNode> async);

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths, AsyncCallback async);

    void search(GWTJahiaSearchQuery search, int limit, int offset, boolean showOnlyNodesWithTemplates, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, AsyncCallback<List<GWTJahiaNode>> async);

    void searchSQL(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, boolean sortOnDisplayName, AsyncCallback<List<GWTJahiaNode>> async);

    void searchPortlets(String match, AsyncCallback<List<GWTJahiaPortletDefinition>> async);

    void getSavedSearch(AsyncCallback<List<GWTJahiaNode>> async);

    void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name, boolean onTopOf, AsyncCallback asyncCallback);

    void mount(String path, String target, String root, AsyncCallback async);

    void getMountpoints(AsyncCallback<List<GWTJahiaNode>> async);

    void storePasswordForProvider(String providerKey, String username, String password, AsyncCallback async);

    void getStoredPasswordsProviders(AsyncCallback<Map<String, String>> async);

    void setLock(List<String> paths, boolean locked, AsyncCallback async);

    void clearAllLocks(String path, boolean processChildNodes, AsyncCallback async);

    void deletePaths(List<String> paths, AsyncCallback async);

    void markForDeletion(List<String> paths, String comment, AsyncCallback async);

    void undeletePaths(List<String> path, AsyncCallback async);

    void getAbsolutePath(String path, AsyncCallback<String> async);

    void checkWriteable(List<String> paths, AsyncCallback async);

    void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut, AsyncCallback async);

    void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName, AsyncCallback async);

    void getProperties(String path, String langCode, AsyncCallback<GWTJahiaGetPropertiesResult> async);

    void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, Set<String> removedTypes, AsyncCallback async);

    void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes, AsyncCallback async);

    void saveNode(GWTJahiaNode node, List<GWTJahiaNode> orderedChilden, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes, AsyncCallback async);

    void createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, AsyncCallback<GWTJahiaNode> async);

    void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, AsyncCallback<GWTJahiaNode> asyncCallback);

    void createFolder(String parentPath, String name, AsyncCallback<GWTJahiaNode> async);

    void createPortletInstance(String path, GWTJahiaNewPortletInstance wiz, AsyncCallback<GWTJahiaNode> async);

    void createRSSPortletInstance(String path, String name, String url, AsyncCallback<GWTJahiaNode> async);

    void createGoogleGadgetPortletInstance(String path, String name, String script, AsyncCallback<GWTJahiaNode> async);

    void checkExistence(String path, AsyncCallback async);

    void rename(String path, String newName, AsyncCallback<String> async);

    void move(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveAtEnd(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void moveOnTopOf(String sourcePath, String targetPath, AsyncCallback asyncCallback);

    void createDefaultUsersGroupACE(List<String> permissions, boolean grand, AsyncCallback<GWTJahiaNodeACE> async);

    void getUsages(List<String> paths, AsyncCallback<List<GWTJahiaNodeUsage>> async);

    void getNodesByCategory(GWTJahiaNode category, int offset, int limit, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void zip(List<String> paths, String archiveName, AsyncCallback async);

    void unzip(List<String> paths, AsyncCallback async);

    void getExportUrl(String path, AsyncCallback<String> async);

    void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace, AsyncCallback async);

    void resizeImage(String path, String target, int width, int height, boolean forceReplace, AsyncCallback async);

    void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, AsyncCallback async);

    void activateVersioning(List<String> path, AsyncCallback async);

    void getVersions(String path, AsyncCallback<List<GWTJahiaNodeVersion>> async);

    void getVersions(GWTJahiaNode node, int limit, int offset, AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> async);

    void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion, boolean allSubTree, AsyncCallback async);

    void uploadedFile(List<String[]> uploadeds, AsyncCallback async);

    void getRenderedContent(String path, String workspace, String locale, String template, String configuration, Map<String, List<String>> contextParams, boolean editMode, String configName, AsyncCallback<GWTRenderResult> async);

    void getNodeURL(String servlet, String path, Date versionDate, String versionLabel, String workspace, String locale, AsyncCallback<String> async);

    void getNodeURLByIdentifier(String servlet, String identifier, Date versionDate, String versionLabel, String workspace, String locale, AsyncCallback<String> async);

    void importContent(String parentPath, String fileKey, Boolean asynchronously, AsyncCallback async);

    void importContent(String parentPath, String fileKey, Boolean asynchronously, Boolean replaceContent, AsyncCallback async);

    void getWorkflowDefinitions(List<String> workflowDefinitionIds, AsyncCallback<Map<String,GWTJahiaWorkflowDefinition>> async);

    void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition, List<GWTJahiaNodeProperty> properties, List<String> comments, AsyncCallback async);

    void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def,
                              List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args, AsyncCallback async);

    void abortWorkflow(String processId, String provider, AsyncCallback async);

    void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome, List<GWTJahiaNodeProperty> properties, AsyncCallback async);

    /**
     * Publish the specified paths.
     *
     * @param uuids the list of node uuids to publish
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     */
    void publish(List<String> uuids, List<GWTJahiaNodeProperty> properties, List<String> comments, AsyncCallback async);

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param uuids the list of node uuids to publish
     * @param async Local implementation of callback to react on return for asynchronous call to unpublish
     */
    void unpublish(List<String> uuids, AsyncCallback async);

    /**
     * Get the publication status information for a particular path.
     *
     * @param uuids uuids to get publication info from
     * @param checkForUnpublication
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     */
    void getPublicationInfo(List<String> uuids, boolean allSubTree, boolean checkForUnpublication, AsyncCallback<List<GWTJahiaPublicationInfo>> async);

    /**
     * Get higthligthed
     *
     * @param original
     * @param amendment
     * @param async
     */
    void getHighlighted(String original, String amendment, AsyncCallback<String> async);

    /**
     * Retrieves list of URL mapping objects for current node and locale.
     *
     * @param node   node to retrieve mapping for
     * @param locale current locale
     * @param async  the callback
     */
    void getUrlMappings(GWTJahiaNode node, String locale, AsyncCallback<List<GWTJahiaUrlMapping>> async);

    void deployTemplates(String templatesPath, String sitePath, AsyncCallback asyncCallback);

    void createTemplateSet(String key, String baseSet, String siteType, AsyncCallback<GWTJahiaNode> asyncCallback);

    void generateWar(String moduleName, AsyncCallback<GWTJahiaNode> asyncCallback);

    void addCommentToWorkflow(GWTJahiaWorkflow task, String comment,
                              AsyncCallback<List<GWTJahiaWorkflowComment>> asyncCallback);

    void getWorkflowComments(GWTJahiaWorkflow workflow, AsyncCallback<List<GWTJahiaWorkflowComment>> async);

    public void getWorkflowHistoryProcesses(String nodeId, String locale, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    public void getWorkflowHistoryTasks(String provider, String processId, String locale, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    void getWorkflowHistoryForUser(AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    void isValidSession(AsyncCallback<Integer> async) throws GWTJahiaServiceException;

    void initializeCreateEngine(String typeName, String parentPath, String targetName, AsyncCallback<GWTJahiaCreateEngineInitBean> async);

    void initializeCreatePortletEngine(String typeName, String parentPath, AsyncCallback<GWTJahiaCreatePortletInitBean> async);

    void initializeEditEngine(String nodePath, boolean tryToLockNode, AsyncCallback<GWTJahiaEditEngineInitBean> async);

    void initializeEditEngine(List<String> paths, boolean tryToLockNode, AsyncCallback<GWTJahiaEditEngineInitBean> async);

    void closeEditEngine(String nodepath, AsyncCallback async);

    void compareAcl(GWTJahiaNodeACL nodeAcl, List<GWTJahiaNode> reference, AsyncCallback<Set<String>> async);

    void getWorkflowRules(String path, AsyncCallback<Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>>> async);

    void flush(String path, AsyncCallback<Void> asyncCallback);

    void flushAll(AsyncCallback<Void> asyncCallback);

    void getPollData(Set<String> keys, AsyncCallback<Map<String,Object>> async);

    void getJobs(int offset, int limit, String sortField, String sortDir, List<String> groupNames, AsyncCallback<PagingLoadResult<GWTJahiaJobDetail>> async);

    void deleteAllCompletedJobs(AsyncCallback<Integer> async);
    
    void deleteJob(String jobName, String groupName, AsyncCallback<Boolean> async);

    void getAllJobGroupNames(AsyncCallback<List<String>> async);

    void getContentHistory(String nodeIdentifier, int offset, int limit, AsyncCallback<PagingLoadResult<GWTJahiaContentHistoryEntry>> async);

    void cleanReferences(String path, AsyncCallback callback);

    void getFieldInitializerValues(String typeName, String propertyName, String parentPath, Map<String, List<GWTJahiaNodePropertyValue>> dependentValues, AsyncCallback<GWTJahiaFieldInitializer> async);

    void getPortalNodes(String targetAreaName, AsyncCallback<List<GWTJahiaNode>> asyncCallback);

	void validateWCAG(Map<String, String> richTexts,
	        AsyncCallback<Map<String, WCAGValidationResult>> asyncCallback);

    void getNumberOfTasksForUser(AsyncCallback<Integer> asyncCallback);

    void getGWTToolbars(String toolbarGroup, AsyncCallback<GWTJahiaToolbar> async);

    void createRemotePublication(String nodeName, Map<String, String> props, boolean validateConnectionSettings, AsyncCallback<Boolean> async);

    void restoreNodeByIdentifierAndDate(String identifier, Date versionDate, String versionLabel, boolean allSubTree,
                                        AsyncCallback<Void> async);

    void flushSite(String siteUUID, AsyncCallback<Void> asyncCallback);

    void getNodeType(String names, AsyncCallback<GWTJahiaNodeType> async);

    void getNodeTypes(List<String> names, AsyncCallback<List<GWTJahiaNodeType>> async);

    void getSubNodeTypes(List<String> names, AsyncCallback<List<GWTJahiaNodeType>> async);

    void getContentTypes(List<String> baseTypes, boolean includeSubTypes, boolean displayStudioElement, AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>> async);

    void getContentTypesAsTree(List<String> paths, List<String> nodeTypes, List<String> fields,
                               boolean includeSubTypes, boolean includeNonDependentModules, AsyncCallback<List<GWTJahiaNode>> async);

    void getWFFormForNodeAndNodeType(String formResourceName,
                                     AsyncCallback<GWTJahiaNodeType> asyncCallback);

    void getVisibilityInformation(String path, AsyncCallback<ModelData> asyncCallback);

}

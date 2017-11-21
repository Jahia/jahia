/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.service.content;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
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
import org.jahia.ajax.gwt.client.util.SessionValidationResult;

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
// -------------------------- OTHER METHODS --------------------------

    void abortWorkflow(String processId, String provider, AsyncCallback async);

    void activateVersioning(List<String> path, AsyncCallback async);

    void addCommentToWorkflow(GWTJahiaWorkflow task, String comment,
                              AsyncCallback<List<GWTJahiaWorkflowComment>> asyncCallback);

    void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome, List<GWTJahiaNodeProperty> properties, AsyncCallback async);

    void checkExistence(String path, AsyncCallback async);

    void checkWriteable(List<String> paths, AsyncCallback async);

    void cleanReferences(String path, AsyncCallback callback);

    void clearAllLocks(String path, boolean processChildNodes, AsyncCallback async);

    void closeEditEngine(String nodepath, AsyncCallback async);

    void compareAcl(GWTJahiaNodeACL nodeAcl, List<GWTJahiaNode> reference, AsyncCallback<Set<String>> async);

    void createDefaultUsersGroupACE(List<String> permissions, boolean grand, AsyncCallback<GWTJahiaNodeACE> async);

    void createFolder(String parentPath, String name, AsyncCallback<GWTJahiaNode> async);

    void createGoogleGadgetPortletInstance(String path, String name, String script, AsyncCallback<GWTJahiaNode> async);

    void createNode(String parentPath, GWTJahiaNode node, AsyncCallback<GWTJahiaNode> async);

    void createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNode> subNodes, Map<String, String> parentNodesType, boolean forceCreation, AsyncCallback<GWTJahiaNode> async);

    void createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, AsyncCallback<GWTJahiaNode> asyncCallback);

    void createPageFromPageModel(String sourcePath, String destinationPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, AsyncCallback<GWTJahiaNode> asyncCallback);

    void createPortletInstance(String path, GWTJahiaNewPortletInstance wiz, AsyncCallback<GWTJahiaNode> async);

    void createRSSPortletInstance(String path, String name, String url, AsyncCallback<GWTJahiaNode> async);

    void createRemotePublication(String nodeName, Map<String, String> props, boolean validateConnectionSettings, AsyncCallback<Boolean> async);

    void createModule(String moduleName, String artifactId, String groupId, String siteType, String sources, AsyncCallback<GWTJahiaNode> asyncCallback);

    void checkoutModule(String moduleId, String scmURI, String scmType, String branchOrTag, String sources, AsyncCallback<GWTJahiaNode> asyncCallback);

    void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace, AsyncCallback async);

    void deleteAllCompletedJobs(AsyncCallback<Integer> async);

    void deleteJob(String jobName, String groupName, AsyncCallback<Boolean> async);

    void deletePaths(List<String> paths, AsyncCallback async);

    void deployTemplates(String templatesPath, String sitePath, AsyncCallback asyncCallback);

    void drawPortletInstanceOutput(String windowID, String entryPointIDStr, String pathInfo, String queryString, AsyncCallback<GWTJahiaPortletOutputBean> async);

    void flush(String path, AsyncCallback<Void> asyncCallback);

    void flushAll(AsyncCallback<Void> asyncCallback);

    void flushSite(String siteUUID, AsyncCallback<Void> asyncCallback);

    void generateWar(String moduleId, AsyncCallback<GWTJahiaNode> asyncCallback);

    void releaseModule(String moduleId, GWTModuleReleaseInfo releaseInfo, AsyncCallback<RpcMap> asyncCallback);

    void getInfoForModuleRelease(String moduleId, AsyncCallback<GWTModuleReleaseInfo> asyncCallback);

    void getAbsolutePath(String path, AsyncCallback<String> async);

    void getAllJobGroupNames(AsyncCallback<List<String>> async);

    void getAvailableSites(AsyncCallback<List<GWTJahiaSite>> asyncCallback);

    void getChannels(AsyncCallback<List<GWTJahiaChannel>> asyncCallback);

    void getContentHistory(String nodeIdentifier, int offset, int limit, AsyncCallback<PagingLoadResult<GWTJahiaContentHistoryEntry>> async);

    void getContentTypes(List<String> baseTypes, boolean includeSubTypes, boolean displayStudioElement, AsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>> async);

    void getContentTypesAsTree(List<String> nodeTypes, List<String> excludedNodeTypes,
                               boolean includeSubTypes, AsyncCallback<List<GWTJahiaNodeType>> async);

    void getEditConfiguration(String path, String name, String enforcedWorkspace, AsyncCallback<GWTEditConfiguration> async);

    void getExportUrl(String path, AsyncCallback<String> async);

    void getFieldInitializerValues(String typeName, String propertyName, String parentPath, Map<String, List<GWTJahiaNodePropertyValue>> dependentValues, AsyncCallback<GWTChoiceListInitializer> async);

    void getGWTToolbars(String toolbarGroup, AsyncCallback<GWTJahiaToolbar> async);

    /**
     * Get higthligthed
     *
     * @param original
     * @param amendment
     * @param async
     */
    void getHighlighted(String original, String amendment, AsyncCallback<String> async);

    void getJobs(int offset, int limit, String sortField, String sortDir, List<String> groupNames, AsyncCallback<PagingLoadResult<GWTJahiaJobDetail>> async);

    void getManagerConfiguration(String name, String path, AsyncCallback<GWTManagerConfiguration> async);

    void getNodeType(String names, AsyncCallback<GWTJahiaNodeType> async);

    void getNodeTypes(List<String> names, AsyncCallback<List<GWTJahiaNodeType>> async);

    void getNodeURL(String servlet, String path, Date versionDate, String versionLabel, String workspace, String locale, boolean findDisplayable, AsyncCallback<String> async);

    void getNodeURLByIdentifier(String servlet, String identifier, Date versionDate, String versionLabel, String workspace, String locale, AsyncCallback<String> async);

    void getNodes(List<String> paths, List<String> fields, AsyncCallback<List<GWTJahiaNode>> async);

    void getNodesAndTypes(List<ModelData> getNodesParams, List<String> types, AsyncCallback<Map<String, List<? extends ModelData>>> async);

    void getNodesByCategory(GWTJahiaNode category, int offset, int limit, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void getNumberOfTasksForUser(AsyncCallback<Integer> asyncCallback);

    void getPollData(Set<String> keys, AsyncCallback<Map<String, Object>> async);

    void getPortalNodes(String targetAreaName, AsyncCallback<List<GWTJahiaNode>> asyncCallback);

    void getProperties(String path, String langCode, AsyncCallback<GWTJahiaGetPropertiesResult> async);

    /**
     * Get the publication status information for multiple nodes by their identifier.
     * Check is done against the current session locale.
     *
     * @param uuids                 uuids to get publication info from
     * @param allSubTree check on the whole subtree or no.
     * @param checkForUnpublication allow to check for element which have been unpublished
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     */
    void getPublicationInfo(List<String> uuids, boolean allSubTree, boolean checkForUnpublication, AsyncCallback<List<GWTJahiaPublicationInfo>> async);

    /**
     * Get the publication status information for multiple nodes by their identifier.
     * Check is done against the set of languages provided.
     *
     * @param uuids                 uuids to get publication info from
     * @param allSubTree check on the whole subtree or no.
     * @param checkForUnpublication allow to check for element which have been unpublished
     * @param languages Set of languages from which we want information
     * @param async Local implementation of callback to react on return for asynchronous call to getPublicationInfo
     */
    void getPublicationInfo(List<String> uuids, boolean allSubTree, boolean checkForUnpublication, Set<String> languages, AsyncCallback<List<GWTJahiaPublicationInfo>> async);

    void getRenderedContent(String path, String workspace, String locale, String template, String configuration, Map<String, List<String>> contextParams, boolean editMode, String configName,
                            String channelIdentifier, String channelVariant, AsyncCallback<GWTRenderResult> async);

    void getRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, List<String> selectedNodes, List<String> openPaths, boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex, boolean useUILocale, AsyncCallback<List<GWTJahiaNode>> async);

    void getSavedSearch(AsyncCallback<List<GWTJahiaNode>> async);

    void getStoredPasswordsProviders(AsyncCallback<Map<String, String>> async);

    void getSubNodeTypes(List<String> names, AsyncCallback<List<GWTJahiaNodeType>> async);

    /**
     * Retrieves list of URL mapping objects for current node and locale.
     *
     * @param node   node to retrieve mapping for
     * @param locale current locale
     * @param async  the callback
     */
    void getUrlMappings(GWTJahiaNode node, String locale, AsyncCallback<List<GWTJahiaUrlMapping>> async);

    void getUsages(List<String> paths, AsyncCallback<List<GWTJahiaNodeUsage>> async);

    void getVersions(String path, AsyncCallback<List<GWTJahiaNodeVersion>> async);

    void getVersions(GWTJahiaNode node, int limit, int offset, AsyncCallback<PagingLoadResult<GWTJahiaNodeVersion>> async);

    void getVisibilityInformation(String path, AsyncCallback<ModelData> asyncCallback);

    void getWFFormForNodeAndNodeType(String formResourceName,
                                     AsyncCallback<GWTJahiaNodeType> asyncCallback);

    void getWorkflowComments(GWTJahiaWorkflow workflow, AsyncCallback<List<GWTJahiaWorkflowComment>> async);

    void getWorkflowDefinitions(List<String> workflowDefinitionIds, AsyncCallback<Map<String, GWTJahiaWorkflowDefinition>> async);

    void getWorkflowHistoryForUser(AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    public void getWorkflowHistoryProcesses(String nodeId, String locale, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    public void getWorkflowHistoryTasks(String provider, String processId, AsyncCallback<List<GWTJahiaWorkflowHistoryItem>> async);

    void getWorkflowRules(String path, AsyncCallback<Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>>> async);

    void importContent(String parentPath, String fileKey, Boolean replaceContent, AsyncCallback<List<GWTJahiaJobDetail>> async);

    void initializeCreateEngine(String typeName, String parentPath, String targetName, AsyncCallback<GWTJahiaCreateEngineInitBean> async);

    void initializeCreatePortletEngine(String typeName, String parentPath, AsyncCallback<GWTJahiaCreatePortletInitBean> async);

    void initializeEditEngine(String nodePath, boolean tryToLockNode, AsyncCallback<GWTJahiaEditEngineInitBean> async);

    void initializeEditEngine(List<String> paths, boolean tryToLockNode, AsyncCallback<GWTJahiaEditEngineInitBean> async);

    void isValidSession(AsyncCallback<SessionValidationResult> async);

    void lsLoad(String parentPath, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, boolean checkSubChild, int limit, int offset, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex, boolean showOnlyNodesWithTemplates, boolean useUILocale, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void markForDeletion(List<String> paths, String comment, AsyncCallback async);

    void move(List<String> sourcePaths, String targetPath, AsyncCallback asyncCallback);

    void moveAtEnd(List<String> sourcePaths, String targetPath, AsyncCallback asyncCallback);

    void moveOnTopOf(List<String> sourcePaths, String targetPath, AsyncCallback asyncCallback);

    void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut, List<String> childNodeTypesToSkip, AsyncCallback async);

    void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName, AsyncCallback async);

    /**
     * Publish the specified paths.
     *
     * @param uuids the list of node uuids to publish
     * @param async Local implementation of callback to react on return for asynchronous call to publish
     */
    void publish(List<String> uuids, List<GWTJahiaNodeProperty> properties, List<String> comments, AsyncCallback async);

    void rename(String path, String newName, AsyncCallback<GWTJahiaNode> async);

    void resizeImage(String path, String target, int width, int height, boolean forceReplace, AsyncCallback async);

    void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion, boolean allSubTree, AsyncCallback async);

    void restoreNodeByIdentifierAndDate(String identifier, Date versionDate, String versionLabel, boolean allSubTree,
                                        AsyncCallback<Void> async);

    void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, AsyncCallback async);

    void sendToSourceControl(String moduleId, String scmURI, String scmType, AsyncCallback<GWTJahiaNode> asyncCallback);

    void saveModule(String moduleId, String message, AsyncCallback asyncCallback);

    void saveNode(GWTJahiaNode node, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes, AsyncCallback<RpcMap> async);

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths, AsyncCallback async);

    void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, Set<String> removedTypes, AsyncCallback async);

    void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes, AsyncCallback async);

    void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name, boolean onTopOf, AsyncCallback asyncCallback);

    void search(GWTJahiaSearchQuery search, int limit, int offset, boolean showOnlyNodesWithTemplates, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, AsyncCallback<List<GWTJahiaNode>> async);

    void searchPortlets(String match, AsyncCallback<List<GWTJahiaPortletDefinition>> async);

    void searchSQL(String searchString, int limit, int offset, List<String> nodeTypes, List<String> fields, boolean sortOnDisplayName, AsyncCallback<PagingLoadResult<GWTJahiaNode>> async);

    void setDistributionServerForModule(String module, GWTModuleReleaseInfo info,
            AsyncCallback<GWTModuleReleaseInfo> async);

    void setLock(List<String> paths, boolean locked, AsyncCallback async);

    void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition, List<GWTJahiaNodeProperty> properties, List<String> comments, AsyncCallback async);

    void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def,
                       List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args, String locale, AsyncCallback async);

    void storePasswordForProvider(String providerKey, String username, String password, AsyncCallback async);

    void undeletePaths(List<String> path, AsyncCallback async);

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param uuids the list of node uuids to publish
     * @param async Local implementation of callback to react on return for asynchronous call to unpublish
     */
    void unpublish(List<String> uuids, AsyncCallback async);

    void unzip(List<String> paths, AsyncCallback async);

    void updateModule(String moduleId, AsyncCallback<String> asyncCallback);

    void addToSourceControl(String moduleId, GWTJahiaNode node, AsyncCallback asyncCallback);

    void markConflictAsResolved(String moduleId, GWTJahiaNode node, AsyncCallback asyncCallback);

    void compileAndDeploy(String moduleId, AsyncCallback async);

    void uploadedFile(List<String[]> uploadeds, AsyncCallback async);

    void validateWCAG(Map<String, String> richTexts,
            AsyncCallback<Map<String, WCAGValidationResult>> asyncCallback);

    void zip(List<String> paths, String archiveName, AsyncCallback async);

    /**
     * Request to an online service the translations for all the values of a list of properties
     *
     * @param properties a list of properties
     * @param definitions the corresponding list of property definitions
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param siteUUID the site UUID
     * @param async callback to handle the properties with their values translated
     */
    void translate(List<GWTJahiaNodeProperty> properties, List<GWTJahiaItemDefinition> definitions, String srcLanguage, String destLanguage, String siteUUID, AsyncCallback<List<GWTJahiaNodeProperty>> async);

    /**
     * Request to an online service the translations for the values of a property
     *
     * @param property a property
     * @param definition the corresponding property definition
     * @param srcLanguage the source language code
     * @param destLanguage the destination language code
     * @param siteUUID the site UUID
     * @param async callback to handle the property with its values translated
     */
    void translate(GWTJahiaNodeProperty property, GWTJahiaItemDefinition definition, String srcLanguage, String destLanguage, String siteUUID, AsyncCallback<GWTJahiaNodeProperty> async);

    void initializeCodeEditor(String path, boolean isNew, String nodeType, String fileType, AsyncCallback<RpcMap> async);

    void getNamespaces(AsyncCallback<List<String>> async);

    /**
     * Retrieve tags regarding a given prefix and using the TagSuggester service
     *
     * @param prefix The text used to match the tags to retrieve
     * @param startPath The path used to search the tags
     * @param minCount Minimum usage count for a tag to be return
     * @param limit Limit of tags return
     * @param offset Offset used in the query
     * @param sortByCount Sort tags by count
     * @param async Callback to handle the tags returned
     */
    void getTags(String prefix, String startPath, Long minCount, Long limit, Long offset, boolean sortByCount, AsyncCallback<List<GWTJahiaValueDisplayBean>> async);

    /**
     * Convert a given tag using the TagHandler service,
     * used to made transformation on the tags before save or display them.
     *
     * @param tag The tag to convert
     * @param async Callback to handle the converted tag returned
     */
    void convertTag(String tag, AsyncCallback<String> async);

    /**
     * Retrieve all currently available registered permissions.
     *
     * The list of permissions may change during runtime. This can happen for instance after module deployment or when
     * a user's session gets directed to a new server on fail-over. The permissions for a node are held in a BitSet variable,
     * where the bits point to indexes in the permissions list. This makes it very important that on resolving the permissions from
     * the BitSet we use the very same permissions list, which was used when creating the BitSet.
     *
     * As the list is cached on the client and bcause of the possibility that the list changed on the server, we may detect that the
     * list has to be reloaded.
     *
     * @param async Callback to handle the permissions returned
     */
    void getAvailablePermissions(AsyncCallback<List<String>> async);

    /**
     * get the warning messages to display in the toolbars
     *
     * @return the warning messages
     */
    void getToolbarWarnings(AsyncCallback<String> async);

    /**
     * Finds the path of the displayble node for the specified one.
     *
     * @param nodePath the path of the target node
     * @param async the asynchronous callback instance to handle the result of the call
     */
    void getDisplayableNodePath(String nodePath, AsyncCallback<String> async);
}
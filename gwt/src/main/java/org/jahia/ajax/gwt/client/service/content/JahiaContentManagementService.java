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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
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
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;

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
public interface JahiaContentManagementService extends RemoteService {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JahiaContentManagementServiceAsync ---------------------

    public GWTJahiaPortletOutputBean drawPortletInstanceOutput(String windowID, String entryPointIDStr, String pathInfo, String queryString);

    public List<GWTJahiaSite> getAvailableSites ();

    public GWTManagerConfiguration getManagerConfiguration(String name) throws GWTJahiaServiceException;

    public GWTEditConfiguration getEditConfiguration(String path, String name) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNode> lsLoad(GWTJahiaNode folder, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, boolean checkSubChild, int limit, int offset, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex, boolean showOnlyNodesWithTemplates) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, List<String> selectedNodes, List<String> openPaths, boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getNodes(List<String> path, List<String> fields) throws GWTJahiaServiceException;

    public Map<String,List<? extends ModelData>> getNodesAndTypes(List<String> paths, List<String> fields, List<String> types) throws GWTJahiaServiceException;

    public GWTJahiaNode getTagNode(String tagName, boolean create) throws GWTJahiaServiceException;

    public void saveOpenPathsForRepository(String repositoryType, List<String> paths) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNode> search(GWTJahiaSearchQuery search, int limit, int offset, boolean showOnlyNodesWithTemplates) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> searchSQL(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, boolean sortOnDisplayName) throws GWTJahiaServiceException;

    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException;

    public void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name, boolean onTopOf) throws GWTJahiaServiceException;

    public void mount(String path, String target, String root) throws GWTJahiaServiceException;

    public List<GWTJahiaNode> getMountpoints() throws GWTJahiaServiceException;

    public void storePasswordForProvider(String providerKey, String username, String password) throws GWTJahiaServiceException;

    public Map<String, String> getStoredPasswordsProviders() throws GWTJahiaServiceException;

    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException;

     public void clearAllLocks(String path, boolean processChildNodes) throws GWTJahiaServiceException;

    public void deletePaths(List<String> paths) throws GWTJahiaServiceException;

    public void markForDeletion(List<String> paths, String comment) throws GWTJahiaServiceException;

    public void undeletePaths(List<String> path) throws GWTJahiaServiceException;

    public String getAbsolutePath(String path) throws GWTJahiaServiceException;

    public void checkWriteable(List<String> paths) throws GWTJahiaServiceException;

    public void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut) throws GWTJahiaServiceException;

    public void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName) throws GWTJahiaServiceException;

    public GWTJahiaGetPropertiesResult getProperties(String path, String langCode) throws GWTJahiaServiceException;

    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, Set<String> removedTypes) throws GWTJahiaServiceException;

    void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes) throws GWTJahiaServiceException;

    void saveNode(GWTJahiaNode node, List<GWTJahiaNode> orderedChilden, GWTJahiaNodeACL acl, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes) throws GWTJahiaServiceException;

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties) throws GWTJahiaServiceException;

    public GWTJahiaNode createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin, GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties) throws GWTJahiaServiceException;

    public GWTJahiaNode createFolder(String parentPath, String name) throws GWTJahiaServiceException;

    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz) throws GWTJahiaServiceException;

    public GWTJahiaNode createRSSPortletInstance(String path, String name, String url) throws GWTJahiaServiceException;

    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script) throws GWTJahiaServiceException;

    public void checkExistence(String path) throws GWTJahiaServiceException;

    public String rename(String path, String newName) throws GWTJahiaServiceException;

    public void move(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public void moveAtEnd(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public void moveOnTopOf(String sourcePath, String targetPath) throws GWTJahiaServiceException;

    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeUsage> getUsages(List<String> paths) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNode> getNodesByCategory(GWTJahiaNode category, int offset, int limit) throws GWTJahiaServiceException;

    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException;

    public void unzip(List<String> paths) throws GWTJahiaServiceException;

    public String getExportUrl(String path) throws GWTJahiaServiceException;

    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace) throws GWTJahiaServiceException;

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace) throws GWTJahiaServiceException;

    public void activateVersioning(List<String> path) throws GWTJahiaServiceException;

    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException;

    public PagingLoadResult<GWTJahiaNodeVersion> getVersions(GWTJahiaNode node, int limit, int offset) throws GWTJahiaServiceException;

    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion, boolean allSubTree) throws GWTJahiaServiceException;

    public void restoreNodeByIdentifierAndDate(String identifier, Date versionDate, String versionLabel, boolean allSubTree) throws GWTJahiaServiceException;

    public void uploadedFile(List<String[]> uploadeds) throws GWTJahiaServiceException;

    public GWTRenderResult getRenderedContent(String path, String workspace, String locale, String template, String configuration, Map<String, List<String>> contextParams, boolean editMode, String configName) throws GWTJahiaServiceException;

    public String getNodeURL(String servlet, String path, Date versionDate, String versionLabel, String workspace, String locale) throws GWTJahiaServiceException;

    public void importContent(String parentPath, String fileKey, Boolean asynchronously) throws GWTJahiaServiceException;

    public Map<String,GWTJahiaWorkflowDefinition> getWorkflowDefinitions(List<String> workflowDefinitionIds) throws GWTJahiaServiceException;

    public void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition, List<GWTJahiaNodeProperty> properties, List<String> comments) throws GWTJahiaServiceException;

    public void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def,
                              List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args) throws GWTJahiaServiceException;

    public void abortWorkflow(String processId, String provider) throws GWTJahiaServiceException;
    
    public void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome, List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException;

    /**
     * Publish the specified uuids.
     *
     * @param uuids the list of node uuids to publish, will not auto publish the parents
     */
    public void publish(List<String> uuids, List<GWTJahiaNodeProperty> properties, List<String> comments) throws GWTJahiaServiceException;

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param uuids the list of node uuids to publish, will not auto publish the parents
     */
    public void unpublish(List<String> uuids) throws GWTJahiaServiceException;

    /**
     * Get the publication status information for multiple pathes.
     *
     *
     * @param uuids path to get publication info from
     * @param checkForUnpublication
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     */
    public List<GWTJahiaPublicationInfo> getPublicationInfo(List<String> uuids, boolean allSubTree,
                                                            boolean checkForUnpublication) throws GWTJahiaServiceException;


    public String getHighlighted(String original, String amendment) throws GWTJahiaServiceException;

    /**
     * Retrieves a list of URL mapping objects for current node and locale.
     *
     * @param node   node to retrieve mapping for
     * @param locale current locale
     * @return a list of URL mapping objects for current node and locale
     * @throws GWTJahiaServiceException in case of an error
     */
    public List<GWTJahiaUrlMapping> getUrlMappings(GWTJahiaNode node, String locale) throws GWTJahiaServiceException;

    void deployTemplates(String templatesPath, String sitePath) throws GWTJahiaServiceException;

    GWTJahiaNode createTemplateSet(String key, String baseSet, String siteType) throws GWTJahiaServiceException;

    GWTJahiaNode generateWar(String moduleName) throws GWTJahiaServiceException;

    List<GWTJahiaWorkflowComment> addCommentToWorkflow(GWTJahiaWorkflow task, String comment);

    List<GWTJahiaWorkflowComment> getWorkflowComments(GWTJahiaWorkflow workflow);

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryProcesses(String nodeId,
                                                                         String locale) throws GWTJahiaServiceException;

    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryTasks(String provider, String processId,
                                                                     String locale) throws GWTJahiaServiceException;

    List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryForUser() throws GWTJahiaServiceException;

    public Integer isValidSession() throws GWTJahiaServiceException;

    GWTJahiaCreateEngineInitBean initializeCreateEngine(String typeName, String parentPath, String targetName) throws GWTJahiaServiceException;

    GWTJahiaCreatePortletInitBean initializeCreatePortletEngine(String typeName, String parentPath) throws GWTJahiaServiceException;

    GWTJahiaEditEngineInitBean initializeEditEngine(String nodePath, boolean tryToLockNode) throws GWTJahiaServiceException;

    GWTJahiaEditEngineInitBean initializeEditEngine(List<String> paths, boolean tryToLockNode) throws GWTJahiaServiceException;

    void closeEditEngine(String nodepath) throws GWTJahiaServiceException;

    Set<String> compareAcl(GWTJahiaNodeACL nodeAcl, List<GWTJahiaNode> reference) throws GWTJahiaServiceException;

    public Map<GWTJahiaWorkflowType,List<GWTJahiaWorkflowDefinition>> getWorkflowRules(String path) throws GWTJahiaServiceException;

    void flush(String path) throws GWTJahiaServiceException;

    void flushAll() throws GWTJahiaServiceException;

    Map<String,Object> getPollData(Set<String> keys) throws GWTJahiaServiceException;

    /**
     * Retrieve job list using pagination and sorting if supported. Also can take an optional groupName list for
     * filtering.
     *
     * @param offset     the offset for pagination
     * @param limit      the limit for pagination (the size of the page)
     * @param sortField  the field on which to sort
     * @param sortDir    the direction in which to sort
     * @param groupNames normally this should be passed as a Set, but it seems that GWT has trouble serializing a
     *                   Set, so we use a list instead.
     * @return a Pagination-ready list of job details
     * @throws GWTJahiaServiceException
     */
    PagingLoadResult<GWTJahiaJobDetail> getJobs(int offset, int limit, String sortField, String sortDir, List<String> groupNames) throws GWTJahiaServiceException;

    /**
     * Deletes all completed job details.
     *
     * @return the number of deleted jobs 
     * @throws GWTJahiaServiceException in case of an error
     */
    Integer deleteAllCompletedJobs() throws GWTJahiaServiceException;
    
    /**
     * Deletes a job either already executed or not yet executed. Don't try to call this on a running job as the
     * behavior will not be determined.
     *
     * @param jobName
     * @param groupName
     * @return
     * @throws GWTJahiaServiceException
     */
    Boolean deleteJob(String jobName, String groupName) throws GWTJahiaServiceException;

    /**
     * Retrieves the list of job groups from the scheduler, to be used for example for filtering by group.
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    List<String> getAllJobGroupNames() throws GWTJahiaServiceException;

    /**
     * Retrieves the history of modifications on a content node.
     *
     * @param nodeIdentifier the identifier of the node for which to retrieve the history
     * @param offset         the paging offset
     * @param limit          the limit of entries to retrieve
     * @return a paging list of history entries for the specified node identifier.
     * @throws GWTJahiaServiceException
     */
    PagingLoadResult<GWTJahiaContentHistoryEntry> getContentHistory(String nodeIdentifier, int offset, int limit) throws GWTJahiaServiceException;

    public void cleanReferences(String path) throws GWTJahiaServiceException;
    
    GWTJahiaFieldInitializer getFieldInitializerValues(String typeName, String propertyName, String parentPath, Map<String, List<GWTJahiaNodePropertyValue>> dependentValues) throws GWTJahiaServiceException;

    List<GWTJahiaNode> getPortalNodes(String targetAreaName);
    
	/**
	 * Validates the HTML texts against WCAG rules. This method allows to
	 * validate multiple texts at once to be able to check WCAG rules for all
	 * rich text fields in the engine.
	 * 
	 * @param richTexts
	 *            a map of HTML texts to be validated, keyed by field IDs (
	 *            {@link CKEditorField#getItemId()})
	 * @return the WCAG validation results, keyed by the original field IDs (
	 *         {@link CKEditorField#getItemId()})
	 */
	Map<String, WCAGValidationResult> validateWCAG(Map<String, String> richTexts);

    int getNumberOfTasksForUser() throws GWTJahiaServiceException;

    public GWTJahiaToolbar getGWTToolbars(String toolbarGroup) throws GWTJahiaServiceException;
    
    boolean createRemotePublication(String nodeName, Map<String, String> props, boolean validateConnectionSettings) throws GWTJahiaServiceException;

    String getNodeURLByIdentifier(String servlet, String identifier, Date versionDate, String versionLabel,
                                  String workspace, String locale) throws GWTJahiaServiceException;

    void flushSite(String siteUUID) throws GWTJahiaServiceException;

    GWTJahiaNodeType getNodeType(String names) throws GWTJahiaServiceException;

    List<GWTJahiaNodeType> getNodeTypes(List<String> names) throws GWTJahiaServiceException;

    List<GWTJahiaNodeType> getSubNodeTypes(List<String> names) throws GWTJahiaServiceException;

    /**
     * Returns a list of node types with name and label populated that are the
     * sub-types of the specified base type.
     *
     *
     * @param baseTypes   the node type name to find sub-types
     * @param displayStudioElement
     * @return a list of node types with name and label populated that are the
     *         sub-types of the specified base type
     */
    Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getContentTypes(List<String> baseTypes, boolean includeSubTypes, boolean displayStudioElement) throws GWTJahiaServiceException;

    List<GWTJahiaNode> getContentTypesAsTree(List<String> paths, List<String> nodeTypes, List<String> fields,
                                             boolean includeSubTypes,  boolean includeNonDependentModules) throws GWTJahiaServiceException;

    GWTJahiaNodeType getWFFormForNodeAndNodeType(String formResourceName)
            throws GWTJahiaServiceException;

    public ModelData getVisibilityInformation(String path) throws GWTJahiaServiceException;

    void importContent(String parentPath, String fileKey, Boolean asynchronously, Boolean replaceContent) throws GWTJahiaServiceException;

    // -------------------------- INNER CLASSES --------------------------

    public static class App {
        private static JahiaContentManagementServiceAsync app = null;

        public static synchronized JahiaContentManagementServiceAsync getInstance() {
            if (app == null) {
                String relativeServiceEntryPoint = createEntryPointUrl();
                String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                app = (JahiaContentManagementServiceAsync) GWT.create(JahiaContentManagementService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);

                JahiaGWTParameters.addUpdater(new JahiaGWTParameters.UrlUpdater() {
                    public void updateEntryPointUrl() {
                        String relativeServiceEntryPoint = createEntryPointUrl();
                        String serviceEntryPoint = URL.getAbsoluteURL(relativeServiceEntryPoint);
                        ((ServiceDefTarget) app).setServiceEntryPoint(serviceEntryPoint);
                    }
                });
            }
            return app;
        }

        private static String createEntryPointUrl() {
            return JahiaGWTParameters.getServiceEntryPoint() + "contentManager.gwt?lang=" + JahiaGWTParameters.getLanguage() + "&site=" + JahiaGWTParameters.getSiteUUID() + "&workspace=" + JahiaGWTParameters.getWorkspace();
        }
    }

}

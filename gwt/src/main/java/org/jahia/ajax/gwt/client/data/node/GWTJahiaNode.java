/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.SortInfo;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;
import org.jahia.ajax.gwt.client.util.Collator;

import java.io.Serializable;
import java.util.*;

/**
 * GWT bean that represents a single JCR node.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:38
 */
public class GWTJahiaNode extends BaseTreeModel implements Serializable, Comparable<GWTJahiaNode>, ListLoadConfig {

    public enum WipStatus {
        DISABLED,
        ALL_CONTENT,
        LANGUAGES
    }

    private static final long serialVersionUID = -1918118279356793994L;
    public static final String TAGS = "tags";
    public static final String SITE_LANGUAGES = "siteLanguages";
    public static final String NAME = "name";
    public static final String ESCAPED_NAME = "escapedName";
    public static final String PATH = "path";
    public static final String ICON = "icon";
    public static final String LOCKED = "locked";
    public static final String LOCK_ALLOWS_ADD = "lockAllowsAdd";
    public static final String LOCKABLE = "lockable";
    public static final String PERMISSIONS = "permissions";
    public static final String DELETEABLE = "deleteable";
    public static final String ACL = "hasACL";
    public static final String UUID = "uuid";
    public static final String DISPLAY_NAME = "displayName";
    public static final String FILE = "file";
    public static final String SIZE = "size";
    public static final String NODE_TYPES = "nodeTypes";
    public static final String INHERITED_NODE_TYPES = "inheritedNodeTypes";
    public static final String PROVIDER_KEY = "providerKey";
    public static final String PREVIEW = "preview";
    public static final String PREVIEW_LARGE = "previewLarge";
    public static final String THUMBNAILS = "thumbnails";
    public static final String SITE_UUID = "siteUUID";
    public static final String SITE_TYPE = "siteType";
    public static final String SITE_KEY = "siteKey";
    public static final String CURRENT_VERSION = "currentVersion";
    public static final String VERSIONS = "versions";
    public static final String CHILDREN_INFO = "childrenInfo";
    public static final String COUNT = "count";
    public static final String PUBLICATION_INFO = "publicationInfo";
    public static final String PUBLICATION_INFOS = "publicationInfos";
    public static final String QUICK_PUBLICATION_INFO = "quickPublicationInfo";
    public static final String AVAILABLE_WORKKFLOWS = "j:availableWorkflows";
    public static final String WORKFLOW_INFO = "workflowInfo";
    public static final String WORKFLOW_INFOS = "workflowInfos";
    public static final String PRIMARY_TYPE_LABEL = "primaryTypeLabel";
    public static final String DEFAULT_LANGUAGE = "j:defaultLanguage";
    public static final String VISIBILITY_INFO = "visibilityInfo";
    public static final String IS_VISIBLE = "isVisible";
    public static final String LOCKS_INFO = "locksInfo";
    public static final String SUBNODES_CONSTRAINTS_INFO = "subnodesConstraintsInfo";
    public static final String SITE_MANDATORY_LANGUAGES = "siteMandatoryLanguages";
    public static final String RESOURCE_BUNDLE = "resourceBundle";
    public static final String INCLUDE_CHILDREN = "includeChildren";
    public static final String EDIT_MODE_BLOCKED = "editModeBlocked";
    public static final String WORK_IN_PROGRESS_LANGUAGES = "j:workInProgressLanguages";
    public static final String WORK_IN_PROGRESS_STATUS = "j:workInProgressStatus";

    public static final List<String> DEFAULT_FIELDS =
            Arrays.asList(ICON, TAGS, CHILDREN_INFO, "j:view", "j:width", "j:height", PERMISSIONS, LOCKS_INFO, PUBLICATION_INFO, SUBNODES_CONSTRAINTS_INFO);

    public static final List<String> DEFAULT_REFERENCE_FIELDS =
            Arrays.asList(ICON, COUNT, CHILDREN_INFO, NAME, DISPLAY_NAME);

    public static final String HOMEPAGE_PATH = "homepage-path";

    public static final List<String> DEFAULT_SITE_FIELDS =
            Arrays.asList("j:moduleType", "j:installedModules", "j:templatesSet", "j:dependencies", "j:languages",
                    "j:defaultLanguage", HOMEPAGE_PATH, SITE_LANGUAGES, "j:versionInfo", PERMISSIONS, LOCKS_INFO, "j:resolvedDependencies", "j:serverName");

    public static final List<String> DEFAULT_USER_FIELDS =
            Arrays.asList("j:firstName", "j:lastName");
    public static final List<String> DEFAULT_GROUP_FIELDS =
            Arrays.asList("j:firstName", "j:lastName");

    public static final List<String> DEFAULT_SITEMAP_FIELDS = Arrays.asList("j:versionInfo", EDIT_MODE_BLOCKED);

    public static final List<String> RESERVED_FIELDS =
            Arrays.asList(TAGS, NAME, PATH, ICON, LOCKED, LOCK_ALLOWS_ADD, LOCKABLE, PERMISSIONS, DELETEABLE, UUID, DISPLAY_NAME, FILE,
                    SIZE, NODE_TYPES, INHERITED_NODE_TYPES, PROVIDER_KEY, PREVIEW, THUMBNAILS, SITE_UUID,
                    CURRENT_VERSION, VERSIONS, CHILDREN_INFO, COUNT, AVAILABLE_WORKKFLOWS, DEFAULT_LANGUAGE, HOMEPAGE_PATH,
                    LOCKS_INFO, VISIBILITY_INFO, PUBLICATION_INFO, PUBLICATION_INFOS, QUICK_PUBLICATION_INFO, WORKFLOW_INFO, WORKFLOW_INFOS, PRIMARY_TYPE_LABEL,
                    SITE_LANGUAGES, SUBNODES_CONSTRAINTS_INFO, "j:versionInfo", RESOURCE_BUNDLE, "j:resolvedDependencies", "j:isDynamicMountPoint",
                    "index", "j:view", "j:width", "j:height", "j:password");

    private boolean displayable = false;
    private boolean isShared = false;
    private boolean reference = false;
    private String url;
    private boolean hasChildren = false;
    private boolean versioned = false;
    private SortInfo sortInfo = new SortInfo(DISPLAY_NAME, SortDir.ASC);
    private List<GWTJahiaNodeVersion> versions;
    private String selectedVersion;
    private String languageCode;
    private boolean expandOnLoad = false;
    private boolean selectedOnLoad = false;
    private GWTJahiaNode referencedNode;
    private GWTJahiaWorkflowInfo workflowInfo;
    private GWTBitSet permissions;

    // in case of a folder, it allows to know if the node is selectable or not
    private boolean matchFilters = false;
    private Map<String, GWTJahiaPublicationInfo> publicationInfos;
    private GWTJahiaPublicationInfo quickPublicationInfo;

    private Map<String, GWTJahiaWorkflowInfo> workflowInfos;
    private Map<String, List<GWTJahiaPublicationInfo>> fullPublicationInfos;
    private boolean wcagCompliance;
    private List<String> invalidLanguages;

    protected Set<String> removedChildrenPaths = new HashSet<String>();

    public GWTJahiaNode() {
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setDisplayable(boolean disp) {
        displayable = disp;
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setLockable(Boolean lockable) {
        set(LOCKABLE, lockable);
    }

    public Boolean isLockable() {
        return get(LOCKABLE) != null ? (Boolean) get(LOCKABLE) : false;
    }

    public void setLocked(Boolean locked) {
        set(LOCKED, locked);
    }

    public Boolean isLocked() {
        return get(LOCKED) != null ? (Boolean) get(LOCKED) : false;
    }

    public void setLockAllowsAdd(Boolean locked) {
        set(LOCK_ALLOWS_ADD, locked);
    }

    public Boolean isLockAllowsAdd() {
        return get(LOCK_ALLOWS_ADD) != null ? (Boolean) get(LOCK_ALLOWS_ADD) : false;
    }

    public Map<String, List<String>> getLockInfos() {
        return get("lockInfos");
    }

    public void setLockInfos(Map<String, List<String>> lockInfos) {
        set("lockInfos", lockInfos);
    }

    public Boolean canUnlock() {
        return get("canUnlock");
    }

    public void setCanUnlock(Boolean canUnlock) {
        set("canUnlock", canUnlock);
    }

    public Boolean canLock() {
        return get("canLock");
    }

    public void setCanLock(Boolean canLock) {
        set("canLock", canLock);
    }

    public void setPermissions(GWTBitSet permissions) {
        this.permissions = permissions;
    }

    public GWTBitSet getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        set(NAME, name);
    }

    public String getName() {
        return get(NAME);
    }

    public void setEscapedName(String escapedName) {
        set(ESCAPED_NAME, escapedName);
    }

    public String getEscapedName() {
        String escapedName = get(ESCAPED_NAME);
        return escapedName != null ? escapedName : (String) get(NAME);
    }

    public void setUUID(String uuid) {
        set(UUID, uuid);
    }

    public String getUUID() {
        return get(UUID);
    }

    public void setDisplayName(String name) {
        set(DISPLAY_NAME, name);
    }

    public String getDisplayName() {
        return get(DISPLAY_NAME);
    }

    public void setPath(String path) {
        set(PATH, path);
    }

    public String getPath() {
        return get(PATH);
    }

    public void setTags(String tags) {
        set(TAGS, tags);
    }

    public String getTags() {
        return get(TAGS);
    }

    public void setFile(Boolean file) {
        set(FILE, file);
    }

    public Boolean isFile() {
        return get(FILE);
    }

    public void setSize(Long size) {
        set(SIZE, size);
    }

    public Long getSize() {
        return get(SIZE);
    }

    public void setNodeTypes(List<String> nodeTypes) {
        set(NODE_TYPES, nodeTypes);
    }

    public List<String> getNodeTypes() {
        return get(NODE_TYPES);
    }

    public void setInheritedNodeTypes(List<String> nodeTypes) {
        set(INHERITED_NODE_TYPES, nodeTypes);
    }

    public List<String> getInheritedNodeTypes() {
        return get(INHERITED_NODE_TYPES);
    }

    public void setIcon(String icon) {
        set(ICON, icon);
    }

    public String getIcon() {
        return get(ICON);
    }

    public void setProviderKey(String providerName) {
        set(PROVIDER_KEY, providerName);
    }

    public String getProviderKey() {
        return get(PROVIDER_KEY);
    }

    public void setPreview(String preview) {
        set(PREVIEW, preview);
    }

    public String getPreview() {
        return get(PREVIEW);
    }

    public void setPreviewLarge(String preview) {
        set(PREVIEW_LARGE, preview);
    }

    public String getPreviewLarge() {
        return get(PREVIEW_LARGE);
    }

    @Deprecated
    public void setThumbnailsMap(Map<String, String> preview) {
        set(THUMBNAILS, preview);
    }

    @Deprecated
    public Map<String, String> getThumbnailsMap() {
        return get(THUMBNAILS);
    }

    public void setDescription(String description) {
        set("description", description);
    }

    public String getDescription() {
        return get("description");
    }

    public void setSiteUUID(String siteUUID) {
        set(SITE_UUID, siteUUID);
    }

    public String getSiteUUID() {
        return get(SITE_UUID);
    }

    public void setSiteKey(String siteKey) {
        set(SITE_KEY, siteKey);
    }

    public String getSiteKey() {
        return get(SITE_KEY);
    }

    public void setVisibilityInfo(Map<GWTJahiaNode, ModelData> visible) {
        set(VISIBILITY_INFO, visible);
    }

    public Map<GWTJahiaNode, ModelData> getVisibilityInfo() {
        return get(VISIBILITY_INFO);
    }

    public void setVisible(Boolean visible) {
        set(IS_VISIBLE, visible);
    }

    public Boolean isVisible() {
        return get(IS_VISIBLE);
    }

    public boolean isVersioned() {
        return versioned;
    }

    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    public boolean isMatchFilters() {
        return matchFilters;
    }

    public void setMatchFilters(boolean matchFilters) {
        this.matchFilters = matchFilters;
    }

    @Override
    public int compareTo(GWTJahiaNode o) {
        if (isFile()) {
            if (o.isFile()) {
                return Collator.getInstance().localeCompare(getName(), o.getName());
            } else {
                return -1;
            }
        } else {
            if (o.isFile()) {
                return 1;
            } else {
                return Collator.getInstance().localeCompare(getName(), o.getName());
            }
        }
    }

    @Override
    public SortDir getSortDir() {
        return sortInfo.getSortDir();
    }

    @Override
    public String getSortField() {
        return sortInfo.getSortField();
    }

    @Override
    public SortInfo getSortInfo() {
        return sortInfo;
    }

    @Override
    public void setSortDir(SortDir sortDir) {
        sortInfo.setSortDir(sortDir);
    }

    @Override
    public void setSortField(String s) {
        sortInfo.setSortField(s);
    }

    @Override
    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo;
    }

    public void setVersions(List<GWTJahiaNodeVersion> versions) {
        this.versions = versions;
    }

    public List<GWTJahiaNodeVersion> getVersions() {
        return versions;
    }

    public void setCurrentVersion(String currentVersion) {
        set(CURRENT_VERSION, currentVersion);
    }

    public String getCurrentVersion() {
        return get(CURRENT_VERSION);
    }

    public void setSelectedVersion(String selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public String getSelectedVersion() {
        return selectedVersion;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setIsShared(boolean isShared) {
        this.isShared = isShared;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setReferencedNode(GWTJahiaNode referencedNode) {
        this.referencedNode = referencedNode;
    }

    public GWTJahiaNode getReferencedNode() {
        return referencedNode;
    }

    public void setChildConstraints(String constraints) {
        set("constraints", constraints);
    }

    public String getChildConstraints() {
        return get("constraints");
    }

    public void setExpandOnLoad(boolean expandOnLoad) {
        this.expandOnLoad = expandOnLoad;
    }

    public boolean isExpandOnLoad() {
        return expandOnLoad;
    }

    public void setSelectedOnLoad(boolean selectedOnLoad) {
        this.selectedOnLoad = selectedOnLoad;
    }

    public boolean isSelectedOnLoad() {
        return selectedOnLoad;
    }

    public boolean isPage() {
        return getInheritedNodeTypes().contains("jnt:page") || getNodeTypes().contains("jnt:page");
    }

    public GWTJahiaPublicationInfo getAggregatedPublicationInfo() {
        if (publicationInfos == null) {
            return null;
        }
        return publicationInfos.get(JahiaGWTParameters.getLanguage());
    }

    public void setWorkflowInfo(GWTJahiaWorkflowInfo workflowInfo) {
        this.workflowInfo = workflowInfo;
    }

    public GWTJahiaWorkflowInfo getWorkflowInfo() {
        return workflowInfo;
    }

    public GWTJahiaPublicationInfo getQuickPublicationInfo() {
        return quickPublicationInfo;
    }

    public void setQuickPublicationInfo(GWTJahiaPublicationInfo quickPublicationInfo) {
        this.quickPublicationInfo = quickPublicationInfo;
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GWTJahiaNode that = (GWTJahiaNode) o;

        return getPath() != null ? getPath().equals(that.getPath()) : that.getPath() == null;
    }

    @Override
    public int hashCode() {
        return getPath() != null ? getPath().hashCode() : 0;
    }

    public void setAggregatedPublicationInfos(Map<String, GWTJahiaPublicationInfo> publicationInfos) {
        this.publicationInfos = publicationInfos;
    }

    public Map<String, GWTJahiaPublicationInfo> getAggregatedPublicationInfos() {
        return publicationInfos;
    }

    public void setWorkflowInfos(Map<String, GWTJahiaWorkflowInfo> workflowInfos) {
        this.workflowInfos = workflowInfos;
    }

    public Map<String, GWTJahiaWorkflowInfo> getWorkflowInfos() {
        return workflowInfos;
    }

    public void setFullPublicationInfos(Map<String, List<GWTJahiaPublicationInfo>> fullPublicationInfos) {
        this.fullPublicationInfos = fullPublicationInfos;
    }

    public Map<String, List<GWTJahiaPublicationInfo>> getFullPublicationInfos() {
        return fullPublicationInfos;
    }

    public boolean isReference() {
        return reference;
    }

    public void setReference(boolean reference) {
        this.reference = reference;
    }

    public void setHasAcl(Boolean deleteable) {
        set(ACL, deleteable);
    }

    public Boolean isHasAcl() {
        return get(ACL);
    }

    public boolean isWCAGComplianceCheckEnabled() {
        return wcagCompliance;
    }

    public void setWCAGComplianceCheckEnabled(boolean wcagComplianceCheckEnabled) {
        wcagCompliance = wcagComplianceCheckEnabled;
    }

    public boolean isNodeType(String nodeType) {
        return getNodeTypes().contains(nodeType) || getInheritedNodeTypes().contains(nodeType);
    }

    public boolean isNodeType(Collection<String> nodeTypes) {
        for (String nodeType : nodeTypes) {
            if (isNodeType(nodeType)) {
                return true;
            }
        }
        return false;
    }

    public void setCanMarkForDeletion(boolean canMarkForDeletion) {
        set("canMarkForDeletion", Boolean.valueOf(canMarkForDeletion));
    }

    public boolean canMarkForDeletion() {
        return (Boolean) get("canMarkForDeletion");
    }

    /**
     * Return a list of invalid languages for the current node.
     *
     * @return a list of invalid languages for the current node.
     */
    public List<String> getInvalidLanguages() {
        return invalidLanguages;
    }

    /**
     * Set the list of invalid languages.
     *
     * @param invalidLanguages List of invalid languages for this node.
     */
    public void setInvalidLanguages(List<String> invalidLanguages) {
        this.invalidLanguages = invalidLanguages;
    }

    public List<String> getRemovedChildrenPaths() {
        return new ArrayList<String>(removedChildrenPaths);
    }

    public void clearRemovedChildrenPaths() {
        removedChildrenPaths = new HashSet<String>();
    }

    @Override
    public void remove(int index) {
        GWTJahiaNode child = (GWTJahiaNode) children.get(index);
        if (child.get("uuid") != null) {
            String path = child.getPath();
            removedChildrenPaths.add(path);
        }
        super.remove(index);
    }

    @Override
    public void remove(ModelData child) {
        if (children.contains(child) && child.get("uuid") != null) {
            removedChildrenPaths.add(((GWTJahiaNode) child).getPath());
        }
        super.remove(child);
    }

    @Override
    public void removeAll() {
        for (ModelData child : children) {
            removedChildrenPaths.add(((GWTJahiaNode) child).getPath());
        }
        super.removeAll();
    }

    /**
     * Checks that the current node is in WIP state either for all languages (all content) or for a specified language.
     *
     * @param language the language to check WIP status for
     * @return <code>true</code>if the current node is in WIP state either for all languages (all content) or for the specified language;
     * <code>false</code> otherwise
     */
    public boolean isInWorkInProgress(String language) {

        String wipStatusStr = getWorkInProgressStatus();
        if (wipStatusStr == null) {
            return false;
        }

        WipStatus wipStatus = WipStatus.valueOf(wipStatusStr);
        switch (wipStatus) {
            case DISABLED:
                return false;
            case ALL_CONTENT:
                return true;
            case LANGUAGES:
                List<String> wipLanguages = getWorkInProgressLanguages();
                return (wipLanguages != null && wipLanguages.contains(language));
            default:
                throw new IllegalStateException("Unsupported WIP status: " + wipStatus);
        }
    }

    public List<String> getWorkInProgressLanguages() {
        return get(WORK_IN_PROGRESS_LANGUAGES);
    }

    public String getWorkInProgressStatus() {
        return get(WORK_IN_PROGRESS_STATUS);
    }

    public void setWorkInProgressLanguages(List<String> languages) {
        set(WORK_IN_PROGRESS_LANGUAGES, languages);
    }

    public void setWorkInProgressStatus(String status) {
        set(WORK_IN_PROGRESS_STATUS, status);
    }

    /**
     * Checks if the current node is marked for deletion.
     *
     * @return <code>true</code>if the current node is marked for deletion; <code>false</code> otherwise
     */
    public boolean isMarkedForDeletion() {
        List<String> nodeTypes = getNodeTypes();
        return nodeTypes != null && nodeTypes.contains("jmix:markedForDeletion");
    }

    /**
     * Checks if the current node is marked for deletion as root node.
     *
     * @return <code>true</code>if the current node is marked for deletion as root node; <code>false</code> otherwise
     */
    public boolean isMarkedForDeletionRoot() {
        List<String> nodeTypes = getNodeTypes();
        return nodeTypes != null && nodeTypes.contains("jmix:markedForDeletionRoot");
    }
}

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

package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ListLoadConfig;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.SortInfo;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowInfo;

import java.io.Serializable;
import java.util.*;

/**
 * GWT bean that represents a single JCR node.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:38
 */
public class GWTJahiaNode extends BaseTreeModel implements Serializable, Comparable<GWTJahiaNode>, ListLoadConfig {
    public static final String TAGS = "tags";
    public static final String SITE_LANGUAGES = "siteLanguages";
    public static final String NAME = "name";
    public static final String PATH = "path";
    public static final String ICON = "icon";
    public static final String LOCKED = "locked";
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
    public static final String ACL_CONTEXT = "aclContext";
    public static final String PROVIDER_KEY = "providerKey";
    public static final String PREVIEW = "preview";
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
    public static final String AVAILABLE_WORKKFLOWS = "j:availableWorkflows";
    public static final String WORKFLOW_INFO = "workflowInfo";
    public static final String WORKFLOW_INFOS = "workflowInfos";
    public static final String PRIMARY_TYPE_LABEL = "primaryTypeLabel";
    public static final String DEFAULT_LANGUAGE = "j:defaultLanguage";
    public static final String VISIBILITY_INFO = "visibilityInfo";
    public static final String IS_VISIBLE = "isVisible";

    public static final List<String> DEFAULT_FIELDS =
            Arrays.asList(ICON, TAGS, CHILDREN_INFO, "j:view", "j:width", "j:height", PUBLICATION_INFO);

    public static final List<String> DEFAULT_REFERENCE_FIELDS =
            Arrays.asList(ICON, COUNT, CHILDREN_INFO,NAME,DISPLAY_NAME);

    public static final String HOMEPAGE_PATH = "homepage-path";

    public static final List<String> DEFAULT_SITE_FIELDS =
            Arrays.asList("j:siteType", "j:installedModules", "j:templatesSet", "j:dependencies","j:languages", "j:defaultLanguage", HOMEPAGE_PATH, SITE_LANGUAGES, "j:versionInfo");

    public static final List<String> RESERVED_FIELDS =
            Arrays.asList(TAGS, NAME, PATH, ICON, LOCKED, LOCKABLE, PERMISSIONS, DELETEABLE, UUID, DISPLAY_NAME, FILE,
                    SIZE, NODE_TYPES, INHERITED_NODE_TYPES, ACL_CONTEXT, PROVIDER_KEY, PREVIEW, THUMBNAILS, SITE_UUID,
                    CURRENT_VERSION, VERSIONS, CHILDREN_INFO, COUNT, AVAILABLE_WORKKFLOWS,DEFAULT_LANGUAGE,HOMEPAGE_PATH);

    private boolean displayable = false;
    private boolean isShared = false;
    private boolean reference = false;
    private String url;
    private boolean hasChildren = false;
    private boolean portlet = false;
    private String normalizedName = null;
    private boolean versioned = false;
    private SortInfo sortInfo = new SortInfo(null, Style.SortDir.ASC);
    private List<GWTJahiaNodeVersion> versions;
    private String selectedVersion;
    private String languageCode;
    private boolean expandOnLoad = false;
    private boolean selectedOnLoad = false;
    private Map<String, Boolean> languageLocked = new HashMap<String, Boolean>();
    private GWTJahiaNode referencedNode;
    private GWTJahiaWorkflowInfo workflowInfo;
    private GWTBitSet permissions;

    // in case of a folder, it allows to know if the node is selectable or not
    private boolean matchFilters = false;
    private Map<String, GWTJahiaPublicationInfo> publicationInfos;
    private Map<String, GWTJahiaWorkflowInfo> workflowInfos;
    private Map<String, List<GWTJahiaPublicationInfo>> fullPublicationInfos;
    private boolean wcagCompliance;

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
        return get(LOCKABLE);
    }

    public void setLocked(Boolean locked) {
        set(LOCKED, locked);
    }

    public Boolean isLocked() {
        return get(LOCKED);
    }

//    public void setLanguageLocked(String language, Boolean locked) {
//        languageLocked.put(language, locked);
//    }
//
//    public Map<String, Boolean> getLanguageLocked() {
//        return languageLocked;
//    }
//
//    public Boolean isLanguageLocked(String language) {
//        return languageLocked.containsKey(language) && languageLocked.get(language);
//    }
//
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

    public void setAclContext(String aclContext) {
        set(ACL_CONTEXT, aclContext);
    }

    public String getAclContext() {
        return get(ACL_CONTEXT);
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

    public void setThumbnailsMap(Map<String, String> preview) {
        set(THUMBNAILS, preview);
    }

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

    public void setVisibilityInfo(Map<GWTJahiaNode,ModelData> visible) {
        set(VISIBILITY_INFO, visible);
    }

    public Map<GWTJahiaNode,ModelData> getVisibilityInfo() {
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

    public String getNormalizedName() {
        if (normalizedName == null) {
            return getName();
        } else {
            return normalizedName;
        }
    }

    public void setNormalizedName(String normalizedName) {
        this.normalizedName = normalizedName;
    }

    public int compareTo(GWTJahiaNode o) {
        if (isFile()) {
            if (o.isFile()) {
                return getNormalizedName().compareToIgnoreCase(o.getNormalizedName());
            } else {
                return -1;
            }
        } else {
            if (o.isFile()) {
                return 1;
            } else {
                return getNormalizedName().compareToIgnoreCase(o.getNormalizedName());
            }
        }
    }


    public Style.SortDir getSortDir() {
        return sortInfo.getSortDir();
    }

    public String getSortField() {
        return sortInfo.getSortField();
    }

    public SortInfo getSortInfo() {
        return sortInfo;
    }

    public void setSortDir(Style.SortDir sortDir) {
        sortInfo.setSortDir(sortDir);
    }

    public void setSortField(String s) {
        sortInfo.setSortField(s);
    }

    public void setSortInfo(SortInfo sortInfo) {
        this.sortInfo = sortInfo;
    }

    public boolean isPortlet() {
        return portlet;
    }

    public void setPortlet(boolean portlet) {
        this.portlet = portlet;
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

        if (getPath() != null ? !getPath().equals(that.getPath()) : that.getPath() != null) {
            return false;
        }

        return true;
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
            if(isNodeType(nodeType)) {
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
}

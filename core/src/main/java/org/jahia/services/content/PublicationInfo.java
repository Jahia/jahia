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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Object to return publication status info for a node and if requested 
 * also contains the infos for the referenced nodes or the subnodes. 
 *
 * @author toto
 */
public class PublicationInfo implements Serializable {
    
    private static final long serialVersionUID = -5752968731917175200L;
    
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;
    public static final int DELETED = 11;
    public static final int MARKED_FOR_DELETION = 12;

    private transient Map<String, List<String>> allUuidsCache = new HashMap<String, List<String>>();


    private PublicationInfoNode root;

    public PublicationInfo() {
        super();
    }

    public PublicationInfo(String rootUuid, String path) {
        this(new PublicationInfoNode(rootUuid, path));
    }

    public PublicationInfo(PublicationInfoNode root) {
        this();
        this.root = root;
    }

    public PublicationInfoNode getRoot() {
        return root;
    }

    public void setRoot(PublicationInfoNode root) {
        this.root = root;
    }

    public List<String> getAllUuids() {
        return getAllUuids(true, true, true);
    }

    public List<String> getAllUuids(boolean includeDeleted, boolean includePublished){
        return getAllUuids(includeDeleted, includePublished, true);
    }

    public List<String> getAllUuids(boolean includeDeleted, boolean includePublished, boolean includeWorkInProgress) {
        String cacheKey = getKey(includeDeleted, includePublished);
        List<String> allUuids = allUuidsCache.get(cacheKey);
        if (allUuids != null) {
            return allUuids;
        }
        allUuids = new ArrayList<String>();
        LinkedList<PublicationInfoNode> nodes = new LinkedList<PublicationInfoNode>();
        Set<PublicationInfoNode> processed = new HashSet<PublicationInfoNode>();
        nodes.add(root);
        processed.add(root);
        PublicationInfoNode node = nodes.poll();
        while (node != null) {
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!processed.contains(infoNode)) {
                    nodes.add(infoNode);
                    processed.add(infoNode);
                }
            }
            if ((includeDeleted || node.getStatus() != DELETED) && (includePublished || node.getStatus() != PUBLISHED)
                    && (includeWorkInProgress || !node.isWorkInProgress())) {
                allUuids.add(node.getUuid());
            }
            node = nodes.poll();
        }
        allUuidsCache.put(cacheKey, allUuids);
        return allUuids;
    }
    
    private String getKey(boolean includeDeleted, boolean includePublished) {
        return String.valueOf(includeDeleted) + String.valueOf(includePublished);
    }

    public List<PublicationInfo> getAllReferences() {
        LinkedHashSet<PublicationInfo> uuids = new LinkedHashSet<PublicationInfo>();
        LinkedList<PublicationInfoNode> nodes = new LinkedList<PublicationInfoNode>();
        Set<PublicationInfoNode> processed = new HashSet<PublicationInfoNode>();
        getAllReferences(uuids, nodes, processed);
        return new ArrayList<PublicationInfo>(uuids);
    }

    private void getAllReferences(LinkedHashSet<PublicationInfo> uuids, LinkedList<PublicationInfoNode> nodes, Set<PublicationInfoNode> processedNodes) {
        nodes.add(root);
        processedNodes.add(root);
        
        PublicationInfoNode node = nodes.poll();
        while(node != null) {
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!processedNodes.contains(infoNode)) {
                    nodes.add(infoNode);
                    processedNodes.add(infoNode);
                }
            }
            for (PublicationInfo refInfo : node.getReferences()) {
                if (!processedNodes.contains(refInfo.getRoot())) {
                    refInfo.getAllReferences(uuids, nodes, processedNodes);
                }
            }
            uuids.addAll(node.getReferences());
            node = nodes.poll();
        }
    }

    public void clearInternalAndPublishedReferences(List<String> uuids) {
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            List<PublicationInfo> toRemove = new ArrayList<PublicationInfo>();
            for (PublicationInfo info : node.getReferences()) {
                if (uuids.contains(info.getRoot().getUuid()) || !info.needPublication(null)) {
                    toRemove.add(info);
                }
            }
            node.getReferences().removeAll(toRemove);
        }
    }

    /**
     * Retrieves a set of all various statuses for the tree.
     * 
     * @param language
     *            the language we are checking
     * @return a set of all various statuses for the tree
     */
    public Set<Integer> getTreeStatus(String language) {
        Set<Integer> status = new HashSet<Integer>();
        LinkedList<PublicationInfoNode> nodes = new LinkedList<PublicationInfoNode>();
        Set<PublicationInfoNode> processed = new HashSet<PublicationInfoNode>();
        nodes.add(root);
        processed.add(root);
        PublicationInfoNode node = nodes.poll();
        String translationNodePath = language != null && node.getChildren().size() > 0 ? "/j:translation_" + language : null;
        while (node != null) {
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!processed.contains(infoNode) &&
                        (language == null || !infoNode.getPath().contains("/j:translation_") || infoNode.getPath().contains(translationNodePath) )) {
                    nodes.add(infoNode);
                }
            }
            status.add(node.getStatus());
            
            node = nodes.poll();
        }
        return status;

    }

    public boolean needPublication(String language) {
        Set<Integer> treeStatus = getTreeStatus(language);
        if (!treeStatus.contains(PUBLISHED) || treeStatus.size() != 1) {
            return true;
        }
        for (PublicationInfo info : getAllReferences()) {
            final Set<Integer> subTreeStatus = info.getTreeStatus(language);
            if (!subTreeStatus.contains(PUBLISHED) || subTreeStatus.size() != 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isUnpublicationPossible(String language) {
        Set<Integer> treeStatus = getTreeStatus(language);
        if (treeStatus.contains(PUBLISHED)) {
            return true;
        }
        for (PublicationInfo info : getAllReferences()) {
            final Set<Integer> subTreeStatus = info.getTreeStatus(language);
            if (subTreeStatus.contains(PUBLISHED)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getAllPublishedLanguages() {
        final Set<String> result = new HashSet<String>();
        new Object() {
            public void getAllPublishedLanguages(PublicationInfoNode node) {
                if (node.getStatus() != UNPUBLISHED  && node.getStatus() != NOT_PUBLISHED  && node.getPath().contains("/j:translation_")) {
                    result.add(StringUtils.substringAfterLast(node.getPath(),"/j:translation_"));
                }
                for (PublicationInfoNode childNode : node.getChildren()) {
                    getAllPublishedLanguages(childNode);
                }
            }
        }.getAllPublishedLanguages(root);
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicationInfo that = (PublicationInfo) o;

        return root == that.root || root != null && root.equals(that.root);
    }

    @Override
    public int hashCode() {
        return root != null ? (31 * super.hashCode() + root.hashCode()) : super.hashCode();
    }
    
    @Override
    public String toString() {
        return root != null ? root.toString() : super.toString();
    }
}

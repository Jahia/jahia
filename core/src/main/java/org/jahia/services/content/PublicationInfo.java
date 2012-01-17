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

package org.jahia.services.content;

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
    }

    public PublicationInfo(String rootUuid, String path) {
        this.root = new PublicationInfoNode(rootUuid, path);
    }

    public PublicationInfo(PublicationInfoNode root) {
        this.root = root;
    }

    public PublicationInfoNode getRoot() {
        return root;
    }

    public void setRoot(PublicationInfoNode root) {
        this.root = root;
    }

    public List<String> getAllUuids() {
        return getAllUuids(true, true);
    }

    public List<String> getAllUuids(boolean includeDeleted, boolean includePublished) {
        String cacheKey = getKey(includeDeleted, includePublished);
        List<String> allUuids = allUuidsCache.get(cacheKey);
        if (allUuids != null) {
            return allUuids;
        }
        allUuids = new ArrayList<String>();
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!nodes.contains(infoNode)) {
                    nodes.add(infoNode);
                }
            }
            if ((includeDeleted || node.getStatus() != DELETED) && (includePublished || node.getStatus() != PUBLISHED)) {
                allUuids.add(node.getUuid());
            }
        }
        allUuidsCache.put(cacheKey, allUuids);
        return allUuids;
    }
    
    private String getKey(boolean includeDeleted, boolean includePublished) {
        return String.valueOf(includeDeleted) + String.valueOf(includePublished);
    }

    public List<PublicationInfo> getAllReferences() {
        List<PublicationInfo> uuids = new ArrayList<PublicationInfo>();
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        getAllReferences(uuids, nodes);
        return uuids;
    }

    private void getAllReferences(List<PublicationInfo> uuids, List<PublicationInfoNode> nodes) {
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!nodes.contains(infoNode)) {
                    nodes.add(infoNode);
                }
            }
            for (PublicationInfo refInfo : node.getReferences()) {
                if (!nodes.contains(refInfo.getRoot())) {
                    refInfo.getAllReferences(uuids, nodes);
                }
            }
            uuids.addAll(node.getReferences());
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

    public Set<Integer> getTreeStatus(String language) {
        Set<Integer> status = new HashSet<Integer>();
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!nodes.contains(infoNode) &&
                        (language == null || !infoNode.getPath().contains("/j:translation_") || infoNode.getPath().contains("/j:translation_"+language) )) {
                    nodes.add(infoNode);
                }
            }
            status.add(node.getStatus());
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
}

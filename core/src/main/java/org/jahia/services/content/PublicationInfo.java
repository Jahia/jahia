/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
    public static final int PUBLISHED = 1;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;

    private transient List<String> allUuids;
    private transient List<String> allPublisheableUuids;


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
            allUuids.add(node.getUuid());
        }
        return allUuids;
    }

    public List<String> getAllPublishableUuids() {
        if (allPublisheableUuids != null) {
            return allPublisheableUuids;
        }
        allPublisheableUuids = new ArrayList<String>();
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (infoNode.isCanPublish() && !nodes.contains(infoNode)) {
                    nodes.add(infoNode);
                }
            }
            allPublisheableUuids.add(node.getUuid());
        }
        return allPublisheableUuids;
    }

    public List<PublicationInfo> getAllReferences() {
        List<PublicationInfo> uuids = new ArrayList<PublicationInfo>();
        List<PublicationInfoNode> nodes = new ArrayList<PublicationInfoNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationInfoNode node = nodes.get(i);
            for (PublicationInfoNode infoNode : node.getChildren()) {
                if (!nodes.contains(infoNode)) {
                    nodes.add(infoNode);
                }
            }
            uuids.addAll(node.getReferences());
        }
        return uuids;
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

}

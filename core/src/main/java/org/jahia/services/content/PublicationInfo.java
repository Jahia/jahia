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

import org.apache.commons.collections.map.ListOrderedMap;

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
    public static final int LOCKED = 2;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;
    
    private PublicationNode root;

    public PublicationInfo() {
    }

    public PublicationInfo(String rootUuid, String path) {
        this.root = new PublicationNode(rootUuid, path);
    }

    public PublicationInfo(PublicationNode root) {
        this.root = root;
    }

    public PublicationNode getRoot() {
        return root;
    }

    public List<PublicationNode> getAllChildInfo() {
        List<PublicationNode> nodes = new ArrayList<PublicationNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
        }
        return nodes;
    }

    public List<String> getAllUuids() {
        List<String> uuids = new ArrayList<String>();
        List<PublicationNode> nodes = new ArrayList<PublicationNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
            uuids.add(node.getUuid());
        }
        return uuids;
    }

    public List<PublicationInfo> getAllReferences() {
        List<PublicationInfo> uuids = new ArrayList<PublicationInfo>();
        List<PublicationNode> nodes = new ArrayList<PublicationNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
            uuids.addAll(node.getReferences());
        }
        return uuids;
    }

    public void clearInternalAndPublishedReferences(List<String> uuids) {
        List<PublicationNode> nodes = new ArrayList<PublicationNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
            List<PublicationInfo> toRemove = new ArrayList<PublicationInfo>();
            for (PublicationInfo info : node.getReferences()) {
                if (uuids.contains(info.getRoot().getUuid()) || !info.needPublication()) {
                    toRemove.add(info);
                }
            }
            node.getReferences().removeAll(toRemove);
        }
    }

    public Set<Integer> getTreeStatus() {
        Set<Integer> status = new HashSet<Integer>();
        List<PublicationNode> nodes = new ArrayList<PublicationNode>();
        nodes.add(root);
        for (int i=0; i<nodes.size(); i++) {
            final PublicationNode node = nodes.get(i);
            nodes.addAll(node.getChildren());
            status.add(node.getStatus());
        }
        return status;

    }

    public boolean needPublication() {
        Set<Integer> treeStatus = getTreeStatus();
        return !treeStatus.contains(PUBLISHED) || treeStatus.size() != 1;
    }

    public class PublicationNode implements Serializable {
        private String uuid;
        private String path;
        private int status;
        private boolean canPublish;
        private List<PublicationNode> child = new ArrayList<PublicationNode>();
        private List<PublicationInfo> references = new ArrayList<PublicationInfo>();
        private List<String> pruned;

        public PublicationNode() {
        }

        public PublicationNode(String uuid, String path) {
            this.uuid = uuid;
            this.path = path;
        }

        public String getUuid() {
            return uuid;
        }

        public String getPath() {
            return path;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public boolean isCanPublish() {
            return canPublish;
        }

        public void setCanPublish(boolean canPublish) {
            this.canPublish = canPublish;
        }

        public List<PublicationNode> getChildren() {
            return child;
        }

        public List<PublicationInfo> getReferences() {
            return references;
        }

        public List<String> getPruned() {
            return pruned;
        }

        public PublicationNode addChild(String uuid, String path) {
            final PublicationNode node = new PublicationNode(uuid, path);
            child.add(node);
            return node;
        }

        public PublicationInfo addReference(String uuid, String path) {
            final PublicationInfo ref = new PublicationInfo(uuid, path);
            references.add(ref);
            return ref;
        }
    }
}

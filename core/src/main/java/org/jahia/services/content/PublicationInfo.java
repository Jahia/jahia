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

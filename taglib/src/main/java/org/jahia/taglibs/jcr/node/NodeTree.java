package org.jahia.taglibs.jcr.node;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Represents the node tree structure, retrieved according to the specified
 * filtering criteria, start node, start level and depth.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class NodeTree {

    /**
     * Single tree node with children.
     * 
     * @author Sergiy Shyrkov
     * 
     */
    public static class NodeTreeItem {

        private Set<NodeTree.NodeTreeItem> children = new LinkedHashSet<NodeTree.NodeTreeItem>();

        private boolean firstInLevel;
        private String identifier;
        private boolean inPath;
        private boolean lastInLevel;
        private int level;
        private JCRNodeWrapper node;
        private NodeTree.NodeTreeItem parent;
        private boolean selected;
        private String title;

        /**
         * Initializes an instance of this class.
         * 
         * @param node
         * @throws RepositoryException
         */
        public NodeTreeItem(JCRNodeWrapper node) throws RepositoryException {
            super();
            this.node = node;
            this.identifier = node.getIdentifier();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this
                    || ((obj instanceof NodeTree.NodeTreeItem) && new EqualsBuilder().append(identifier,
                            ((NodeTree.NodeTreeItem) obj).getIdentifier()).isEquals());
        }

        /**
         * @return the children
         */
        public Set<NodeTree.NodeTreeItem> getChildren() {
            return children;
        }

        public int getChildrenCount() {
            return getChildren().size();
        }

        /**
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * @return the level
         */
        public int getLevel() {
            return level;
        }

        /**
         * @return the node
         */
        public JCRNodeWrapper getNode() {
            return node;
        }

        /**
         * @return the parent
         */
        public NodeTree.NodeTreeItem getParent() {
            return parent;
        }

        /**
         * @return the title
         * @throws RepositoryException
         */
        public String getTitle() throws RepositoryException {
            if (title == null) {
                if (node.hasProperty(Constants.JCR_TITLE)) {
                    title = node.getPropertyAsString(Constants.JCR_TITLE);
                } else {
                    title = node.getName();
                }
            }
            return title;
        }

        public boolean hasChildren() {
            return !getChildren().isEmpty();
        }

        @Override
        public int hashCode() {
            return identifier.hashCode();
        }

        /**
         * @return the firstInLevel
         */
        public boolean isFirstInLevel() {
            return firstInLevel;
        }

        /**
         * @return the inPath
         */
        public boolean isInPath() {
            return inPath;
        }

        /**
         * @return the lastInLevel
         */
        public boolean isLastInLevel() {
            return lastInLevel;
        }

        /**
         * @return the selected
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * @param children the children to set
         */
        public void setChildren(Set<NodeTree.NodeTreeItem> children) {
            this.children = children;
        }

        /**
         * @param firstInLevel the firstInLevel to set
         */
        public void setFirstInLevel(boolean firstInLevel) {
            this.firstInLevel = firstInLevel;
        }

        /**
         * @param inPath the inPath to set
         */
        public void setInPath(boolean inPath) {
            this.inPath = inPath;
        }

        /**
         * @param lastInLevel the lastInLevel to set
         */
        public void setLastInLevel(boolean lastInLevel) {
            this.lastInLevel = lastInLevel;
        }

        /**
         * @param level the level to set
         */
        public void setLevel(int level) {
            this.level = level;
        }

        /**
         * @param node the node to set
         */
        public void setNode(JCRNodeWrapper node) {
            this.node = node;
        }

        /**
         * @param parent the parent to set
         */
        public void setParent(NodeTree.NodeTreeItem parent) {
            this.parent = parent;
        }

        /**
         * @param selected the selected to set
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }

    private int depth;
    private Set<NodeTree.NodeTreeItem> root;

    /**
     * @return the depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * @return the root
     */
    public Set<NodeTree.NodeTreeItem> getRoot() {
        return root;
    }

    /**
     * @param depth the depth to set
     */
    public void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(Set<NodeTree.NodeTreeItem> root) {
        this.root = root;
    }
}
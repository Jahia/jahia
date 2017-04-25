package org.jahia.utils;

import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

/**
 * JCR utilities.
 */
public class JcrUtils {

    /**
     * Name of the node intended for storing temporary data.
     */
    public static final String TEMP_NODE_NAME = "tmp";

    private JcrUtils() {
    }

    /**
     * Get node by its parent node, name and type; create the node if it does not exist yet.
     *
     * @param parent Parent node
     * @param name Name of the node
     * @param type Type of the node
     * @return The node
     * @throws IllegalStateException In case the node already exists, but has different type
     */
    public static JCRNodeWrapper getNodeCreateIfNeeded(JCRNodeWrapper parent, String name, String type) throws IllegalStateException {
        try {
            if (parent.hasNode(name)) {
                JCRNodeWrapper node = parent.getNode(name);
                if (!node.getPrimaryNodeTypeName().equals(type)) {
                    throw new IllegalStateException(String.format("Unexpected type of node %s: %s", node.getPath(), node.getPrimaryNodeTypeName()));
                }
                return node;
            }
            parent.addNode(name, type);
            parent.getSession().save();
            return parent.getNode(name);
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
    }

    /**
     * Get the node intended for storing temporary data; create the node if it does not exist yet.
     *
     * @param session JCR session
     * @return The node
     * @throws IllegalStateException In case the node already exists, but has unexpected type
     */
    public static JCRNodeWrapper getTempNodeCreateIfNeeded(JCRSessionWrapper session) throws IllegalStateException {
        JCRNodeWrapper root;
        try {
            root = session.getRootNode();
        } catch (RepositoryException e) {
            throw new JahiaRuntimeException(e);
        }
        return getNodeCreateIfNeeded(root, TEMP_NODE_NAME, Constants.NT_UNSTRUCTURED);
    }
}

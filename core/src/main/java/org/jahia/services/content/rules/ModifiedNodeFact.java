package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRSessionWrapper;

/**
 * Represents a JCR node modification.
 */
public interface ModifiedNodeFact {

    /**
     * @return The workspace where the modification was done
     */
    String getWorkspace() throws RepositoryException;

    /**
     * @return The JCR session than did the modification
     */
    JCRSessionWrapper getSession() throws RepositoryException;

    /**
     * @return The UUID of the modified node
     */
    String getNodeIdentifier() throws RepositoryException;

    /**
     * @return The primary type of the modified node
     */
    String getNodeType() throws RepositoryException;
}

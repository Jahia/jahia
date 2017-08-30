package org.jahia.services.content.rules;

import javax.jcr.RepositoryException;

/**
 * Represents a modification of a property of a JCR node.
 */
public interface ModifiedPropertyFact extends ModifiedNodeFact {

    /**
     * @return Info about the node
     */
    AddedNodeFact getNode();

    /**
     * @return The name of the modified property
     */
    String getName() throws RepositoryException;
}

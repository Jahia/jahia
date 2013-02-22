package org.jahia.services.content;

import javax.jcr.RepositoryException;

/**
 * Interface for provider factories.
 * Used to mount a provider based on a jnt:mountPoint node. The factory mounts a provider in place of a target
 * jnt:mountPoint node
 *
 */
public interface ProviderFactory {

    /**
     * The node type which is supported by this factory
     * @return The node type name
     */
    public String getNodeTypeName();

    /**
     * Mount the provider in place of the mountPoint node passed in parameter. Use properties of
     * the mountPoint node to set parameters in the store provider
     *
     * @param mountPoint The jnt:mountPoint node
     * @return A new provider instance, mounted
     * @throws RepositoryException
     */
    public JCRStoreProvider mountProvider(JCRNodeWrapper mountPoint) throws RepositoryException;

}

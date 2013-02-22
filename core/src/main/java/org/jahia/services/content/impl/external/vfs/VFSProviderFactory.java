package org.jahia.services.content.impl.external.vfs;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.ProviderFactory;
import org.jahia.services.content.impl.external.ExternalContentStoreProvider;

import javax.jcr.RepositoryException;

/**
 * Mount external VFS Data store
 */
public class VFSProviderFactory implements ProviderFactory {

    /**
     * The node type which is supported by this factory
     * @return The node type name
     */
    @Override
    public String getNodeTypeName() {
        return "jnt:vfsMountPoint";
    }

    /**
     * Mount the provider in place of the mountPoint node passed in parameter. Use properties of
     * the mountPoint node to set parameters in the store provider
     *
     * @param mountPoint The jnt:mountPoint node
     * @return A new provider instance, mounted
     * @throws RepositoryException
     */
    @Override
    public JCRStoreProvider mountProvider(JCRNodeWrapper mountPoint) throws RepositoryException {
        ExternalContentStoreProvider provider = (ExternalContentStoreProvider) SpringContextSingleton.getBean("ExternalStoreProviderPrototype");
        provider.setKey(mountPoint.getIdentifier());
        provider.setMountPoint(mountPoint.getPath());

        VFSDataSource dataSource = new VFSDataSource();
        dataSource.setRoot(mountPoint.getProperty("j:root").getString());
        provider.setDataSource(dataSource);
        provider.setDynamicallyMounted(true);
        provider.setSessionFactory(JCRSessionFactory.getInstance());
        try {
            provider.start();
        } catch (JahiaInitializationException e) {
            throw new RepositoryException(e);
        }
        return provider;

    }
}

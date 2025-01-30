package org.jahia.services.importexport;

import org.jahia.exceptions.JahiaException;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface ImportZipContext extends Closeable {
    interface ImportDescriptorCallback<T> {
        T doWithDescriptor(InputStream is, String name, T previousDescriptorResult) throws RepositoryException, IOException, JahiaException;
    }

    /**
     * Gets the list of files in the ZIP file.
     * Likely to include /content/... and /live-content/... files.
     * Will not include root files. (e.g. repository.xml, live-repository.xml, etc.) (use getRootFiles() for that)
     * @return the list of files
     */
    List<String> getLoadedContentFilePaths();

    /**
     * Gets the list of import descriptors in the ZIP file.
     * Likely to include repository.xml, live-repository.xml, etc.
     * Will not include /content/... and /live-content/... files. (use getFiles() for that)
     * @return the list of root files
     */
    List<String> getLoadedImportDescriptorNames();

    /**
     * Gets the ZIP file resource.
     * @return the ZIP file resource
     */
    Resource getArchive();

    /**
     *
     */
    <X> X executeWithImportDescriptors(Collection<String> descriptors, ImportDescriptorCallback<X> callback) throws RepositoryException, IOException;
}

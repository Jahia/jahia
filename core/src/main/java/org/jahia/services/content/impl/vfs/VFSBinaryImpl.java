package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:21:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSBinaryImpl implements Binary {

    FileContent fileContent;
    InputStream inputStream = null;

    public VFSBinaryImpl(InputStream inputStream) {
        // here we should copy the content of the inputstream, but where ??? Keeping it in memory is a bad idea.
        this.inputStream = inputStream;
    }

    public VFSBinaryImpl(FileContent fileContent) {
        this.fileContent = fileContent;
    }

    public InputStream getStream() throws RepositoryException {
        if (fileContent == null) {
            return inputStream;
        }
        try {
            inputStream = fileContent.getInputStream();
        } catch (FileSystemException e) {
            throw new RepositoryException("Error retrieving inputstream to file content", e);
        }
        return inputStream;
    }

    public int read(byte[] b, long position) throws IOException, RepositoryException {
        if (inputStream == null) {
            getStream();
        }
        return inputStream.read(b, (int) position, b.length);
    }

    public long getSize() throws RepositoryException {
        try {
            return fileContent.getSize();
        } catch (FileSystemException e) {
            throw new RepositoryException("Error retrieving file's size", e);
        }
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}

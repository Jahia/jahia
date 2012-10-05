package org.jahia.services.content.impl.external.vfs;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.*;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.impl.external.ExternalData;
import org.jahia.services.content.impl.external.ExternalDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class VFSDataSource implements ExternalDataSource {
    public static final Logger logger = LoggerFactory.getLogger(VFSDataSource.class);
    private String root;
    private String rootPath;
    private FileSystemManager manager;

    public void setRoot(String root) {
        this.root = root;

        try {
            manager = VFS.getManager();
            rootPath = getFile("/").getName().getPath();
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isSupportsUuid() {
        return true;
    }

    public boolean isSupportsSearch() {
        return false;
    }

    public List<String> getSupportedNodeTypes() {
        return Arrays.asList(Constants.JAHIANT_FILE, Constants.JAHIANT_FOLDER, Constants.JCR_CONTENT);
    }

    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        try {
            UUID testUUID = UUID.fromString(identifier);
            throw new ItemNotFoundException("This repository does not support UUID as identifiers");
        } catch (IllegalArgumentException iae) {
            // this is expected, we should not be using UUIDs
        }
        FileObject fileObject = null;
        try {
            fileObject = manager.resolveFile(identifier);
            if (!fileObject.exists()) {
                throw new ItemNotFoundException(identifier);
            }
            return getFile(fileObject);
        } catch (FileSystemException fse) {
            throw new ItemNotFoundException("File system exception while trying to retrieve " + identifier, fse);
        }
    }

    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        try {
            if (path.endsWith("/"+Constants.JCR_CONTENT)) {
                FileContent content = getFile(StringUtils.substringBeforeLast(path,"/"+Constants.JCR_CONTENT)).getContent();
                return getFileContent(content);
            } else {
                FileObject fileObject = getFile(path);
                if (!fileObject.exists()) {
                    throw new PathNotFoundException(path);
                }
                return getFile(fileObject);
            }

        } catch (FileSystemException e) {
            throw new PathNotFoundException("File system exception while trying to retrieve " + path, e);
        }
    }


    public FileObject getFile(String path) throws FileSystemException {
        return manager.resolveFile(root + path);
    }

    public List<String> getChildren(String path) {
        try {
            if (!path.endsWith("/"+Constants.JCR_CONTENT)) {
                FileObject fileObject = getFile(path);
                if (fileObject.getType() == FileType.FILE) {
                    return Arrays.asList(Constants.JCR_CONTENT);
                } else if (fileObject.getType() == FileType.FOLDER) {
                    List<String> children = new ArrayList<String>();
                    for (FileObject object : fileObject.getChildren()) {
                        children.add(object.getName().getBaseName());
                    }
                    return children;
                } else {
                    logger.warn("Found non file or folder entry, maybe an alias. VFS file type=" + fileObject.getType());
                }
            }
        } catch (FileSystemException e) {
            logger.error("Cannot get node children",e);
        }

        return new ArrayList<String>();
    }

    public List<String> search(String basePath, String type, Map<String, String> constraints, String orderBy, int limit) {
        return new ArrayList<String>();
    }

    private ExternalData getFile(FileObject fileObject) throws FileSystemException {
        String identifier =  fileObject.getURL().toString();
        String type;
        FileType fileType = fileObject.getType();
        if (fileType == FileType.FILE) {
            type = Constants.JAHIANT_FILE;
        } else {
            type = Constants.JAHIANT_FOLDER;
        }

        Map<String,String[]> properties = new HashMap<String, String[]>();
        if (fileObject.getContent() != null) {
            long lastModifiedTime = fileObject.getContent().getLastModifiedTime();
            if (lastModifiedTime > 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastModifiedTime);
                properties.put(Constants.JCR_CREATED, new String[] { ISO8601.format(calendar) });
                properties.put(Constants.JCR_LASTMODIFIED, new String[] { ISO8601.format(calendar) });
            }
        }

        String path = fileObject.getName().getPath().substring(rootPath.length());
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return new ExternalData(identifier, path, type, properties);
    }

    private ExternalData getFileContent(final FileContent content) throws FileSystemException {
        Map<String,String[]> properties = new HashMap<String, String[]>();

        String s1 = content.getContentInfo().getContentType();
        if (s1 == null) {
            s1 = JCRContentUtils.getMimeType(content.getFile().getName().getBaseName());
        }
        if (s1 == null) {
            s1 = "application/octet-stream";
        }
        properties.put(Constants.JCR_MIMETYPE, new String[] { s1 });

        Map<String,Binary[]> binaryProperties = new HashMap<String, Binary[]>();
        binaryProperties.put(Constants.JCR_DATA, new Binary[] {new VFSBinaryImpl(content)});

        String path = content.getFile().getName().getPath().substring(rootPath.length());

        ExternalData externalData = new ExternalData(null, path + "/"+Constants.JCR_CONTENT, Constants.NT_RESOURCE, properties);
        externalData.setBinaryProperties(binaryProperties);
        externalData.setMixin(Arrays.asList("mix:mimeType"));
        return externalData;
    }


    class VFSBinaryImpl implements Binary {

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
            try {
                fileContent.close();
            } catch (FileSystemException e) {
                logger.error("Error",e);
            }
        }
    }


}

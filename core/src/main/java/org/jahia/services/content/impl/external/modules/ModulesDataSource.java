package org.jahia.services.content.impl.external.modules;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.jcr.Binary;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jahia.api.Constants;
import org.jahia.services.content.impl.external.ExternalData;
import org.jahia.services.content.impl.external.vfs.VFSDataSource;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.core.io.Resource;

/**
 * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
 *
 * @author david
 * @since 6.7
 */
public class ModulesDataSource extends VFSDataSource {

    private Map<String, String> fileTypeMapping;

    private Map<String, String> folderTypeMapping;

    private List<String> supportedNodeTypes;

    private JahiaTemplateManagerService templateManagerService;

    @Override
    public List<String> getChildren(String path) {
        try {
            if (!path.endsWith("/"+Constants.JCR_CONTENT)) {
                FileObject fileObject = getFile(path);
                if (fileObject.getType() == FileType.FILE) {
                    return Arrays.asList(Constants.JCR_CONTENT);
                } else if (fileObject.getType() == FileType.FOLDER) {
                    List<String> children = new ArrayList<String>();
                    for (FileObject object : fileObject.getChildren()) {
                        if (path.equals("/") && templateManagerService.getTemplatePackageByFileName(object.getName().getBaseName()) != null)  {
                            children.add(object.getName().getBaseName());
                        } else if (!path.equals("/")) {
                            children.add(object.getName().getBaseName());
                        }
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

    @Override
    public String getDataType(FileObject fileObject) throws FileSystemException {
        String type = fileObject.getType().equals(FileType.FOLDER) ? folderTypeMapping
                .get(fileObject.getName().getBaseName()) : fileTypeMapping.get(fileObject.getName()
                .getExtension());
        return type != null ? type : super.getDataType(fileObject);
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        ExternalData data = super.getItemByPath(path);
        if (path.endsWith(".jsp")) {
            Properties properties = new Properties();

            // set Properties
            InputStream is = null;
            try {
                is = getFile(path.substring(0, path.lastIndexOf(".")) + ".properties").getContent().getInputStream();
                properties.load(is);
                Map<String,String[]> dataProperties = new HashMap<String, String[]>();
                for (Iterator<?> iterator = properties.keySet().iterator(); iterator.hasNext();) {
                    String k = (String) iterator.next();
                    String v = properties.getProperty(k);
                    dataProperties.put(k,v.split(","));
                }
                data.getProperties().putAll(dataProperties);
            } catch (FileSystemException e) {
                //no properties files, do nothing
            } catch (IOException e) {
                logger.error("Cannot read property file",e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return data;
    }

    @Override
    public List<String> getSupportedNodeTypes() {
        return supportedNodeTypes;
    }

    @Override
    public void saveItem(ExternalData data) {
        OutputStream outputStream = null;
        if (data.getPath().endsWith(Constants.JCR_CONTENT)) {
            try {
                outputStream = getFile(data.getPath().substring(0, data.getPath().indexOf("/" + Constants.JCR_CONTENT))).getContent().getOutputStream();
                final Binary[] binaries = data.getBinaryProperties().get(Constants.JCR_DATA);
                for (Binary binary : binaries) {
                    final InputStream stream = binary.getStream();
                    byte[] bytes = new byte[(int) binary.getSize()];
                    final int read = stream.read(bytes,0,(int) binary.getSize());
                    outputStream.write(bytes,0, read);
                }
            } catch (FileSystemException e) {
                logger.error(e.getMessage(), e);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        } else if (data.getType().equals(Constants.JAHIANT_VIEWFILE)) {
            // Handle properties
            try {
                ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());
                Properties properties = new Properties();
                for (String property  : data.getProperties().keySet()) {
                    if (type.getDeclaredPropertyDefinitionsAsMap().containsKey(property)) {
                        String[] v = data.getProperties().get(property);
                        StringBuilder propertyValue = new StringBuilder();
                        if (v!= null) {
                            for (String s : v) {
                                if (propertyValue.length() > 0) {
                                    propertyValue.append(",");
                                }
                                propertyValue.append(s);
                            }
                            properties.put(property,propertyValue.toString());
                        }
                    } else {
                        logger.warn("property not found " + property + " for type " + type.getName());
                    }
                }
                outputStream = getFile(data.getPath().substring(0,data.getPath().lastIndexOf(".")) + ".properties").getContent().getOutputStream();
                properties.store(outputStream,data.getPath());
            } catch (FileSystemException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }  catch (NoSuchNodeTypeException e) {
                logger.error("Unable to find type : " + data.getType() + " for node " + data.getPath(),e);
            }


            finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }

    public void setFileTypeMapping(Map<String, String> fileTypeMapping) {
        this.fileTypeMapping = fileTypeMapping;
    }

    public void setFolderTypeMapping(Map<String, String> folderTypeMapping) {
        this.folderTypeMapping = folderTypeMapping;
    }

    public void setRootResource(Resource root) {
        try {
            super.setRoot(root.getFile().getPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void setSupportedNodeTypes(List<String> supportedNodeTypes) {
        this.supportedNodeTypes = supportedNodeTypes;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}

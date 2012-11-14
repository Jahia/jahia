/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.external.modules;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.impl.external.ExternalData;
import org.jahia.services.content.impl.external.vfs.VFSDataSource;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.springframework.core.io.Resource;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
 *
 * @author david
 * @since 6.7
 */
public class ModulesDataSource extends VFSDataSource {

    private static final List<String> JCR_CONTENT_LIST = Arrays.asList(Constants.JCR_CONTENT);

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
                    return JCR_CONTENT_LIST;
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
        int relativeDepth = getFile("/").getName().getRelativeName(fileObject.getName()).split("/").length;
        String type;
        if (fileObject.getType().equals(FileType.FOLDER)) {
            if (relativeDepth == 2) {
                type = Constants.JAHIANT_MODULEVERSIONFOLDER;
            } else {
                type = folderTypeMapping.get(fileObject.getName().getBaseName());
            }
            if (type == null) {
                if (relativeDepth == 3 && fileObject.getName().getBaseName().contains("_")) {
                    type = Constants.JAHIANT_NODETYPEFOLDER;
                } else {
                    FileObject parent = fileObject.getParent();
                    if (relativeDepth == 4 && parent != null && Constants.JAHIANT_NODETYPEFOLDER.equals(getDataType(parent))) {
                        type = Constants.JAHIANT_TEMPLATETYPEFOLDER;
                    }
                }
            }
        } else {
            type = fileTypeMapping.get(fileObject.getName().getExtension());
        }
        if (type != null && Constants.JAHIANT_RESOURCEBUNDLE_FILE.equals(type)) {
            // we've detected a properties file, check if its parent is of type jnt:resourceBundleFolder
            // -> than this one gets the type jnt:resourceBundleFile; otherwise just jnt:file
            FileObject parent = fileObject.getParent();
            type = parent != null
                    && StringUtils.equals(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER,
                            getDataType(parent)) ? type : null;
        }
        return type != null ? type : super.getDataType(fileObject);
    }

    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        ExternalData data = super.getItemByIdentifier(identifier);
        return enhanceData(data.getPath(), data);
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        ExternalData data = super.getItemByPath(path);
        return enhanceData(path, data);
    }

    private ExternalData enhanceData(String path, ExternalData data) {
        if (data.getType().equals("jnt:moduleVersionFolder")) {
            String v = StringUtils.substringAfterLast(data.getPath(), "/");
            String name = StringUtils.substringBeforeLast(data.getPath(), "/");
            name = StringUtils.substringAfterLast(name, "/");
            name = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(name).getName();

            data.getProperties().put("j:title", new String[]{name + " (" + v + ")"});
        } else if (path.endsWith(".jsp")) {
            // set source code
            InputStream is = null;
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                is = getFile(path).getContent().getInputStream();
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                String[] propertyValue = {writer.toString()};
                data.getProperties().put("sourceCode", propertyValue);
                data.getProperties().put("nodeTypeName",new String[] { path.split("/")[3].replace("_",":") } );
            } catch (Exception e) {
                logger.error("Failed to read source code", e);
            } finally {
                IOUtils.closeQuietly(is);
            }

            // set Properties
            Properties properties = new Properties();
            is = null;
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
        } else if (path.endsWith(".properties")) {
            /* these properties should be read only on demand to avoid memory issues
            try {
                Properties properties = new Properties();
                FileObject file = getFile(path.substring(0, path.lastIndexOf(".")) + ".properties");

                for (FileObject subfile : file.getParent().getChildren()) {
                    String baseName = file.getName().getBaseName();
                    baseName = StringUtils.substringBeforeLast(baseName, ".");

                    String filename = subfile.getName().getBaseName();
                    if (filename.startsWith(baseName + "_") && filename.endsWith(".properties")) {
                        filename = StringUtils.substringBeforeLast(filename,".");
                        String langCode = StringUtils.substringAfterLast(filename,"_");

                        InputStream is = subfile.getContent().getInputStream();
                        Properties i18nproperties = new Properties();
                        i18nproperties.load(is);
                        Map<String,String[]> dataProperties = new HashMap<String, String[]>();
                        for (Iterator<?> iterator = i18nproperties.keySet().iterator(); iterator.hasNext();) {
                            String k = (String) iterator.next();
                            String v = i18nproperties.getProperty(k);
                            dataProperties.put(k,new String[] { v });
                        }
                        if (data.getI18nProperties() == null) {
                            data.setI18nProperties(new HashMap<String, Map<String, String[]>>());
                        }
                        data.getI18nProperties().put(langCode, dataProperties);
                    }
                }

                InputStream is = file.getContent().getInputStream();
                properties.load(is);
                Map<String,String[]> dataProperties = new HashMap<String, String[]>();
                for (Iterator<?> iterator = properties.keySet().iterator(); iterator.hasNext();) {
                    String k = (String) iterator.next();
                    String v = properties.getProperty(k);
                    dataProperties.put(k,new String[] { v });
                }
                data.getProperties().putAll(dataProperties);
            } catch (IOException e) {
                logger.error("Cannot read property file",e);
            }
            */
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
            // Handle source code
            try {
                outputStream = getFile(data.getPath()).getContent().getOutputStream();
                byte[] sourceCode = data.getProperties().get("sourceCode")[0].getBytes();
                outputStream.write(sourceCode);
            } catch (Exception e) {
                logger.error("Failed to write source code", e);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        logger.error("Failed to close output stream", e);
                    }
                }
            }

            // Handle properties
            try {
                ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIAMIX_VIEWPROPERTIES);
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

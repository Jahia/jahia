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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.impl.external.ExternalData;
import org.jahia.services.content.impl.external.vfs.VFSDataSource;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.core.io.Resource;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.*;
import java.util.*;

/**
 * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
 *
 * @author david
 * @since 6.7
 */
public class ModulesDataSource extends VFSDataSource {

    private static final List<String> JCR_CONTENT_LIST = Arrays.asList(Constants.JCR_CONTENT);

    private JahiaTemplatesPackage module;

    private Map<String, String> fileTypeMapping;

    private Map<String, String> folderTypeMapping;

    private List<String> supportedNodeTypes;

    private JahiaTemplateManagerService templateManagerService;

    @Override
    public List<String> getChildren(String path) {
        List<String> children = super.getChildren(path);
        CollectionUtils.filter(children, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return !object.toString().startsWith(".");
            }
        });
        return children;
    }

    @Override
    public String getDataType(FileObject fileObject) throws FileSystemException {
        int relativeDepth = getFile("/").getName().getRelativeName(fileObject.getName()).split("/").length;
        String type;
        if (fileObject.getType().equals(FileType.FOLDER)) {
            if (fileObject.getName().equals(getFile("/").getName())) {
                type = Constants.JAHIANT_MODULEVERSIONFOLDER;
            } else {
                type = folderTypeMapping.get(fileObject.getName().getBaseName());
            }
            if (type == null) {
                if (relativeDepth == 1 && fileObject.getName().getBaseName().contains("_")) {
                    type = Constants.JAHIANT_NODETYPEFOLDER;
                } else {
                    FileObject parent = fileObject.getParent();
                    if (relativeDepth == 2 && parent != null && Constants.JAHIANT_NODETYPEFOLDER.equals(getDataType(parent))) {
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
        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());
            if (type.isNodeType("jnt:moduleVersionFolder")) {
                String name = module.getName();
                String v = module.getVersion().toString();
                data.getProperties().put("j:title", new String[]{name + " (" + v + ")"});
            } else if (type.isNodeType("jnt:editableFile")) {
                // set source code
                InputStream is = null;
                try {
                    is = getFile(path).getContent().getInputStream();
                    String[] propertyValue = {IOUtils.toString(is, Charsets.UTF_8)};
                    data.getProperties().put("sourceCode", propertyValue);
                    data.getProperties().put("nodeTypeName",new String[] { path.split("/")[1].replace("_", ":") } );
                } catch (Exception e) {
                    logger.error("Failed to read source code", e);
                } finally {
                    IOUtils.closeQuietly(is);
                }

                // set Properties
                if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
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
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unknown type",e);
        }
        return data;
    }

    @Override
    public List<String> getSupportedNodeTypes() {
        return supportedNodeTypes;
    }

    @Override
    public void saveItem(ExternalData data) {
        super.saveItem(data);

        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());

            OutputStream outputStream = null;
            if (type.isNodeType("jnt:editableFile")) {
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
                if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
                    try {
                        ExtendedNodeType propertiesType = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIAMIX_VIEWPROPERTIES);
                        Properties properties = new Properties();
                        for (String property  : data.getProperties().keySet()) {
                            if (propertiesType.getDeclaredPropertyDefinitionsAsMap().containsKey(property)) {
                                String[] v = data.getProperties().get(property);
                                StringBuilder propertyValue = new StringBuilder();
                                if (v!= null) {
                                    for (String s : v) {
                                        if (propertyValue.length() > 0) {
                                            propertyValue.append(",");
                                        }
                                        propertyValue.append(s);
                                    }
                                    if (propertiesType.getDeclaredPropertyDefinitionsAsMap().get(property).getRequiredType() != PropertyType.BOOLEAN || !propertyValue.toString().equals("false")) {
                                        properties.put(property,propertyValue.toString());
                                    }
                                }
                            }
                        }
                        FileObject file = getFile(data.getPath().substring(0, data.getPath().lastIndexOf(".")) + ".properties");
                        if (!properties.isEmpty()) {
                            outputStream = file.getContent().getOutputStream();
                            properties.store(outputStream, data.getPath());
                        } else {
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    } catch (FileSystemException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }  catch (NoSuchNodeTypeException e) {
                        logger.error("Unable to find type : " + data.getType() + " for node " + data.getPath(),e);
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unknown type",e);
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

    public JahiaTemplatesPackage getModule() {
        return module;
    }

    public void setModule(JahiaTemplatesPackage module) {
        this.module = module;
    }
}

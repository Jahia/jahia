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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
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
import org.jahia.services.content.nodetypes.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.annotation.Nullable;
import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.version.OnParentVersionAction;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
 *
 * @author david
 * @since 6.7
 */
public class ModulesDataSource extends VFSDataSource {

    private static final List<String> JCR_CONTENT_LIST = Arrays.asList(Constants.JCR_CONTENT);
    protected static final String UNSTRUCTURED_PROPERTY = "__prop__";
    protected static final String UNSTRUCTURED_CHILD_NODE = "__node__";

    private Map<String, String> fileTypeMapping;

    private Map<String, String> folderTypeMapping;

    private List<String> supportedNodeTypes;

    private JahiaTemplateManagerService templateManagerService;

    private Map<String, NodeTypeRegistry> nodeTypeRegistryMap = new HashMap<String, NodeTypeRegistry>();

    @Override
    public List<String> getChildren(String path) {
        String[] splitPath = path.split("/");
        try {
            if (path.endsWith(".cnd")) {
                List<String> children = new ArrayList<String>();
                NodeTypeIterator nodeTypes = loadRegistry(path, splitPath[1]).getNodeTypes(splitPath[1]);
                while (nodeTypes.hasNext()) {
                    children.add(nodeTypes.nextNodeType().getName());
                }
                return children;
            } else if (splitPath.length >= 2 && splitPath[splitPath.length - 2].endsWith(".cnd")) {
                List<String> children = new ArrayList<String>();
                String nodeTypeName = splitPath[splitPath.length - 1];
                try {
                    ExtendedNodeType nodeType = loadRegistry(StringUtils.substringBeforeLast(path,"/"), splitPath[1]).getNodeType(nodeTypeName);
                    children.addAll(nodeType.getDeclaredPropertyDefinitionsAsMap().keySet());
                    for (int type : nodeType.getDeclaredUnstructuredPropertyDefinitions().keySet()) {
                        children.add(UNSTRUCTURED_PROPERTY + String.valueOf(type));
                    }
                    children.addAll(nodeType.getDeclaredChildNodeDefinitionsAsMap().keySet());
                    for (String type : nodeType.getDeclaredUnstructuredChildNodeDefinitions().keySet()) {
                        children.add(UNSTRUCTURED_CHILD_NODE + type);
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.error("Failed to get node type " + nodeTypeName, e);
                }
                return children;
            } else if (!path.endsWith("/"+Constants.JCR_CONTENT)) {
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
        String[] splitPath = path.split("/");
        if (splitPath.length >= 2 && splitPath[splitPath.length - 2].endsWith(".cnd")) {
            String nodeTypeName = splitPath[splitPath.length - 1];
            try {
                ExtendedNodeType nodeType = loadRegistry(StringUtils.substringBeforeLast(path,"/"), splitPath[1]).getNodeType(nodeTypeName);
                return getNodeTypeData(path, nodeType);
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        } else if (splitPath.length >= 3 && splitPath[splitPath.length - 3].endsWith(".cnd")) {
            String nodeTypeName = splitPath[splitPath.length - 2];
            String itemDefinitionName = splitPath[splitPath.length - 1];
            try {
                String definitionsPath = StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/");
                ExtendedNodeType nodeType = loadRegistry(definitionsPath, splitPath[1]).getNodeType(nodeTypeName);
                Map<String,ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getPropertyDefinitionsAsMap();
                if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getPropertyDefinitionData(path, propertyDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                    int type = Integer.parseInt(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                    return getPropertyDefinitionData(path, nodeType.getUnstructuredPropertyDefinitions().get(type), true);
                }
                Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getChildNodeDefinitionsAsMap();
                if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getChildNodeDefinitionData(path, childNodeDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                    String type = itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length());
                    return getChildNodeDefinitionData(path, nodeType.getUnstructuredChildNodeDefinitions().get(type), true);
                }
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        }
        ExternalData data = super.getItemByPath(path);
        return enhanceData(path, data);
    }

    private ExternalData getNodeTypeData(String path, ExtendedNodeType nodeType) {
        Map<String, String[]> properties = new HashMap<String, String[]>();
        ExtendedNodeType[] declaredSupertypes = nodeType.getDeclaredSupertypes();
        String supertype = null;
        List<String> mixins = new ArrayList<String>();
        for (ExtendedNodeType declaredSupertype : declaredSupertypes) {
            if (declaredSupertype.isMixin()) {
                mixins.add(declaredSupertype.getName());
            } else if (supertype == null) {
                supertype = declaredSupertype.getName();
            }
        }
        if (supertype != null) {
            properties.put("j:supertype", new String[]{supertype});
        }
        if (!mixins.isEmpty()) {
            properties.put("j:mixins", mixins.toArray(new String[mixins.size()]));
        }

        properties.put("j:isAbstract", new String[]{String.valueOf(nodeType.isAbstract())});
        properties.put("j:isQueryable", new String[]{String.valueOf(nodeType.isQueryable())});
        properties.put("j:hasOrderableChildNodes", new String[]{String.valueOf(nodeType.hasOrderableChildNodes())});
        properties.put("j:itemsType", new String[]{String.valueOf(nodeType.getItemsType())});

        Function<ExtendedNodeType,String> transformName = new Function<ExtendedNodeType,String>() {
            public String apply(@Nullable ExtendedNodeType from) { return from != null ? from.getName() : null; }
        };
        properties.put("j:mixinExtends", Collections2.<ExtendedNodeType,String>transform(nodeType.getMixinExtends(), transformName).toArray(new String[nodeType.getMixinExtends().size()]));
        String primaryItemName = nodeType.getPrimaryItemName();
        if (primaryItemName != null) {
            properties.put("j:primaryItemName", new String[]{primaryItemName});
        }
        ExternalData externalData = new ExternalData(path, path, nodeType.isMixin() ? "jnt:mixinNodeType" : "jnt:primaryNodeType", properties);
        Map<String, Map<String,String[]>> i18nProperties = new HashMap<String, Map<String,String[]>>();

        for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
            HashMap<String, String[]> value = new HashMap<String, String[]>();
            i18nProperties.put(locale.toString(), value);
            value.put("jcr:title", new String[] { nodeType.getLabel(locale) });
            value.put("jcr:description", new String[] { nodeType.getDescription(locale) });
        }

        externalData.setI18nProperties(i18nProperties);
        return externalData;
    }

    private ExternalData getPropertyDefinitionData(String path, ExtendedPropertyDefinition propertyDefinition, boolean unstructured) {
        Map<String, String[]> properties = new HashMap<String, String[]>();
        properties.put("j:autoCreated", new String[]{String.valueOf(propertyDefinition.isAutoCreated())});
        properties.put("j:mandatory", new String[]{String.valueOf(propertyDefinition.isMandatory())});
        properties.put("j:onParentVersion", new String[]{OnParentVersionAction.nameFromValue(propertyDefinition.getOnParentVersion())});
        properties.put("j:protected", new String[]{String.valueOf(propertyDefinition.isProtected())});
        properties.put("j:requiredType", new String[]{PropertyType.nameFromValue(propertyDefinition.getRequiredType())});
        String[] valueConstraints = propertyDefinition.getValueConstraints();
        if (valueConstraints != null) {
            properties.put("j:valueConstraints", valueConstraints);
        }
        Value[] defaultValues = propertyDefinition.getDefaultValues();
        if (defaultValues != null) {
            List<String> defaultValuesAsString = new ArrayList<String>();
            for (Value value: defaultValues) {
                try {
                    defaultValuesAsString.add(value.getString());
                } catch (RepositoryException e) {
                    logger.error("Failed to get default value", e);
                }
            }
            properties.put("j:defaultValues", defaultValuesAsString.toArray(new String[defaultValuesAsString.size()]));
        }
        properties.put("j:multiple", new String[]{String.valueOf(propertyDefinition.isMultiple())});
        String[] availableQueryOperators = propertyDefinition.getAvailableQueryOperators();
        if (availableQueryOperators != null) {
            properties.put("j:availableQueryOperators", availableQueryOperators);
        }
        properties.put("j:isFullTextSearchable", new String[]{String.valueOf(propertyDefinition.isFullTextSearchable())});
        properties.put("j:isQueryOrderable", new String[]{String.valueOf(propertyDefinition.isQueryOrderable())});
        properties.put("j:isFacetable", new String[]{String.valueOf(propertyDefinition.isFacetable())});
        properties.put("j:isHierarchical", new String[]{String.valueOf(propertyDefinition.isHierarchical())});
        properties.put("j:isInternationalized", new String[]{String.valueOf(propertyDefinition.isInternationalized())});
        ExternalData externalData = new ExternalData(path, path,
                unstructured ? "jnt:unstructuredPropertyDefinition" : "jnt:structuredPropertyDefinition", properties);
        return externalData;
    }

    private ExternalData getChildNodeDefinitionData(String path, ExtendedNodeDefinition nodeDefinition, boolean unstructured) {
        Map<String, String[]> properties = new HashMap<String, String[]>();
        properties.put("j:autoCreated", new String[]{String.valueOf(nodeDefinition.isAutoCreated())});
        properties.put("j:mandatory", new String[]{String.valueOf(nodeDefinition.isMandatory())});
        properties.put("j:onParentVersion", new String[]{OnParentVersionAction.nameFromValue(nodeDefinition.getOnParentVersion())});
        properties.put("j:protected", new String[]{String.valueOf(nodeDefinition.isProtected())});
        String[] requiredPrimaryTypeNames = nodeDefinition.getRequiredPrimaryTypeNames();
        if (requiredPrimaryTypeNames != null) {
            properties.put("j:requiredPrimaryTypes", requiredPrimaryTypeNames);
        }
        String defaultPrimaryTypeName = nodeDefinition.getDefaultPrimaryTypeName();
        if (defaultPrimaryTypeName != null) {
            properties.put("j:defaultPrimaryType", new String[]{defaultPrimaryTypeName});
        }
        ExternalData externalData = new ExternalData(path, path,
                unstructured ? "jnt:unstructuredChildNodeDefinition" : "jnt:structuredChildNodeDefinition", properties);
        return externalData;
    }

    private ExternalData enhanceData(String path, ExternalData data) {
        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());
            if (type.isNodeType("jnt:moduleVersionFolder")) {
                String v = StringUtils.substringAfterLast(data.getPath(), "/");
                String name = StringUtils.substringBeforeLast(data.getPath(), "/");
                name = StringUtils.substringAfterLast(name, "/");
                name = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(name).getName();

                data.getProperties().put("j:title", new String[]{name + " (" + v + ")"});
            } else if (type.isNodeType("jnt:editableFile")) {
                // set source code
                InputStream is = null;
                try {
                    is = getFile(path).getContent().getInputStream();
                    String[] propertyValue = {IOUtils.toString(is, Charsets.UTF_8)};
                    data.getProperties().put("sourceCode", propertyValue);
                    data.getProperties().put("nodeTypeName",new String[] { path.split("/")[3].replace("_",":") } );
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

    private synchronized NodeTypeRegistry loadRegistry(String path, String module) {
        if (nodeTypeRegistryMap.containsKey(module)) {
            return nodeTypeRegistryMap.get(module);
        } else {
            NodeTypeRegistry ntr = new NodeTypeRegistry();
            try {
                ntr.initSystemDefinitions();
                JahiaTemplatesPackage templatesPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(module);
                List<JahiaTemplatesPackage> dependencies = new ArrayList<JahiaTemplatesPackage>(templatesPackage.getDependencies());
                Collections.reverse(dependencies);
                for (JahiaTemplatesPackage depend : dependencies) {
                    for (String s : depend.getDefinitionsFiles()) {
                        ntr.addDefinitionsFile(depend.getResource(s), depend.getRootFolder(), null);
                    }
                }
                ntr.addDefinitionsFile(new UrlResource(getFile(path).getURL()),module,null);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            nodeTypeRegistryMap.put(module, ntr);
            return ntr;
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
            super.setRoot("file://"+root.getFile().getPath());
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

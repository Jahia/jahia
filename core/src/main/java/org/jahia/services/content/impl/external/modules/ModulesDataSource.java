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
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.version.OnParentVersionAction;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
                    for (Integer type : nodeType.getDeclaredUnstructuredPropertyDefinitions().keySet()) {
                        children.add(UNSTRUCTURED_PROPERTY + type.toString());
                    }
                    children.addAll(nodeType.getDeclaredChildNodeDefinitionsAsMap().keySet());
                    for (String type : nodeType.getDeclaredUnstructuredChildNodeDefinitions().keySet()) {
                        children.add(UNSTRUCTURED_CHILD_NODE + type);
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.error("Failed to get node type " + nodeTypeName, e);
                }
                return children;
            } else if (splitPath.length >= 3 && splitPath[splitPath.length - 3].endsWith(".cnd")) {

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
                Map<String,ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
                if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getPropertyDefinitionData(path, propertyDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                    Integer type = Integer.valueOf(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                    return getPropertyDefinitionData(path, nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type), true);
                }
                Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getChildNodeDefinitionData(path, childNodeDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                    String type = itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length());
                    return getChildNodeDefinitionData(path, nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type), true);
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
        String itemsType = nodeType.getItemsType();
        if (itemsType != null) {
            properties.put("j:itemsType", new String[]{itemsType});
        }
        List<ExtendedNodeType> mixinExtends = nodeType.getMixinExtends();
        if (mixinExtends != null && !mixinExtends.isEmpty()) {
            Function<ExtendedNodeType,String> transformName = new Function<ExtendedNodeType,String>() {
                public String apply(@Nullable ExtendedNodeType from) { return from != null ? from.getName() : null; }
            };
            properties.put("j:mixinExtends", Collections2.<ExtendedNodeType,String>transform(mixinExtends, transformName).toArray(new String[mixinExtends.size()]));
        }
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
        properties.put("j:selectorType", new String[]{SelectorType.nameFromValue(propertyDefinition.getSelector())});
        Map<String, String> selectorOptions = propertyDefinition.getSelectorOptions();
        List<String> selectorOptionsList = new ArrayList<String>();
        for (String key : selectorOptions.keySet()) {
            String option = key;
            String value = selectorOptions.get(key);
            if (StringUtils.isNotBlank(value)) {
                option += "='" + value + "'";
            }
            selectorOptionsList.add(option);
        }
        properties.put("j:selectorOptions", selectorOptionsList.toArray(new String[selectorOptionsList.size()]));
        String[] valueConstraints = propertyDefinition.getValueConstraints();
        if (valueConstraints != null && valueConstraints.length > 0) {
            properties.put("j:valueConstraints", valueConstraints);
        }
        Value[] defaultValues = propertyDefinition.getDefaultValues();
        if (defaultValues != null && defaultValues.length > 0) {
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
        if (availableQueryOperators != null && availableQueryOperators.length > 0) {
            properties.put("j:availableQueryOperators", availableQueryOperators);
        }
        properties.put("j:isFullTextSearchable", new String[]{String.valueOf(propertyDefinition.isFullTextSearchable())});
        properties.put("j:isQueryOrderable", new String[]{String.valueOf(propertyDefinition.isQueryOrderable())});
        properties.put("j:isFacetable", new String[]{String.valueOf(propertyDefinition.isFacetable())});
        properties.put("j:isHierarchical", new String[]{String.valueOf(propertyDefinition.isHierarchical())});
        properties.put("j:isInternationalized", new String[]{String.valueOf(propertyDefinition.isInternationalized())});
        properties.put("j:isHidden", new String[]{String.valueOf(propertyDefinition.isHidden())});
        properties.put("j:index", new String[]{IndexType.nameFromValue(propertyDefinition.getIndex())});
        properties.put("j:scoreboost", new String[]{String.valueOf(propertyDefinition.getScoreboost())});
        String analyzer = propertyDefinition.getAnalyzer();
        if (analyzer != null) {
            properties.put("j:analyzer", new String[]{analyzer});
        }
        properties.put("j:onConflictAction", new String[]{OnConflictAction.nameFromValue(propertyDefinition.getOnConflict())});
        String itemType = propertyDefinition.getLocalItemType();
        if (itemType != null) {
            properties.put("j:itemType", new String[]{itemType});
        }
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
    public void removeItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");
        if (path.endsWith(".cnd")) {
            nodeTypeRegistryMap.remove(splitPath[1]);
            super.removeItemByPath(path);
        } else if (splitPath.length >= 2 && splitPath[splitPath.length - 2].endsWith(".cnd")) {
            String nodeTypeName = splitPath[splitPath.length - 1];
            try {
                NodeTypeRegistry nodeTypeRegistry = loadRegistry(StringUtils.substringBeforeLast(path, "/"), splitPath[1]);
                nodeTypeRegistry.unregisterNodeType(nodeTypeName);
                writeDefinitionFile(nodeTypeRegistry, StringUtils.substringBeforeLast(path, "/"), splitPath[1]);
            } catch (ConstraintViolationException e) {
                throw new PathNotFoundException("Failed to remove node type " + nodeTypeName, e);
            }
        } else if (splitPath.length >= 3 && splitPath[splitPath.length - 3].endsWith(".cnd")) {
            String nodeTypeName = splitPath[splitPath.length - 2];
            String itemDefinitionName = splitPath[splitPath.length - 1];
            try {
                String definitionsPath = StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/");
                NodeTypeRegistry nodeTypeRegistry = loadRegistry(definitionsPath, splitPath[1]);
                ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
                Map<String,ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
                if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    propertyDefinitionsAsMap.get(itemDefinitionName).remove();
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                    Integer type = Integer.valueOf(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                    nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type).remove();
                }
                Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    childNodeDefinitionsAsMap.get(itemDefinitionName).remove();
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                    String type = itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length());
                    nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type).remove();
                }
                writeDefinitionFile(nodeTypeRegistry, definitionsPath, splitPath[1]);
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        } else {
            super.removeItemByPath(path);
        }
    }

    @Override
    public void move(String oldPath, String newPath) throws PathNotFoundException {
        String[] splitOldPath = oldPath.split("/");
        String[] splitNewPath = newPath.split("/");
        if (oldPath.endsWith(".cnd")) {
            nodeTypeRegistryMap.remove(splitOldPath[1]);
            super.move(oldPath, newPath);
        } else if (splitOldPath.length >= 2 && splitOldPath[splitOldPath.length - 2].endsWith(".cnd")
                && splitNewPath.length >= 2 && splitNewPath[splitNewPath.length - 2].endsWith(".cnd")) { // nodeType rename
            String oldNodeTypeName = splitOldPath[splitOldPath.length - 1];
            String newNodeTypeName = splitNewPath[splitNewPath.length - 1];
            String module = splitOldPath[1];
            try {
                NodeTypeRegistry nodeTypeRegistry = loadRegistry(StringUtils.substringBeforeLast(oldPath, "/"), module);
                ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(oldNodeTypeName);
                nodeTypeRegistry.unregisterNodeType(oldNodeTypeName);
                Name name = new Name(newNodeTypeName, nodeTypeRegistry.getNamespaces());
                nodeType.setName(name);
                nodeTypeRegistry.addNodeType(name, nodeType);
                nodeType.validate();
                writeDefinitionFile(nodeTypeRegistry, StringUtils.substringBeforeLast(oldPath, "/"), module);
            } catch (ConstraintViolationException e) {
                throw new PathNotFoundException("Failed to move node type " + oldNodeTypeName, e);
            } catch (NoSuchNodeTypeException e) {
                nodeTypeRegistryMap.remove(module);
                throw new PathNotFoundException("Failed to move node type " + oldNodeTypeName, e);
            }
        } else if (splitOldPath.length >= 3 && splitOldPath[splitOldPath.length - 3].endsWith(".cnd")) {
        } else {
            super.move(oldPath, newPath);
        }
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

            } else if (type.isNodeType("jnt:nodeType")) {
                saveNodeType(data);
            } else if (type.isNodeType("jnt:propertyDefinition")) {
                savePropertyDefinition(data);
            } else if (type.isNodeType("jnt:childNodeDefinition")) {
                saveChildNodeDefinition(data);
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unknown type",e);
        }
    }

    private void saveNodeType(ExternalData data) {
        String path = data.getPath();
        String[] splitPath = path.split("/");
        String module = splitPath[1];
        String nodeTypeName = splitPath[splitPath.length - 1];
        NodeTypeRegistry nodeTypeRegistry = loadRegistry(StringUtils.substringBeforeLast(path, "/"), module);
        ExtendedNodeType nodeType = null;
        try {
            nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
        } catch (NoSuchNodeTypeException e) {}
        if (nodeType == null) {
            nodeType = new ExtendedNodeType(nodeTypeRegistry, module);
            nodeType.setName(new Name(nodeTypeName, nodeTypeRegistry.getNamespaces()));
        }
        Map<String, String[]> properties = data.getProperties();
        List<String> declaredSupertypes = new ArrayList<String>();
        String[] values = properties.get("j:supertype");
        if (values != null && values.length > 0) {
            declaredSupertypes.add(values[0]);
        }
        values = properties.get("j:mixins");
        if (values != null) {
            for (String mixin : values) {
                declaredSupertypes.add(mixin);
            }
        }
        nodeType.setDeclaredSupertypes(declaredSupertypes.toArray(new String[declaredSupertypes.size()]));
        values = properties.get("j:isAbstract");
        if (values != null && values.length > 0) {
            nodeType.setAbstract(Boolean.parseBoolean(values[0]));
        }
        values = properties.get("j:isQueryable");
        if (values != null && values.length > 0) {
            nodeType.setQueryable(Boolean.parseBoolean(values[0]));
        } else {
            nodeType.setQueryable(true);
        }
        values = properties.get("j:hasOrderableChildNodes");
        if (values != null && values.length > 0) {
            nodeType.setHasOrderableChildNodes(Boolean.parseBoolean(values[0]));
        } else {
            nodeType.setHasOrderableChildNodes(false);
        }
        values = properties.get("j:itemsType");
        if (values != null && values.length > 0) {
            nodeType.setItemsType(values[0]);
        } else {
            nodeType.setItemsType(null);
        }
        values = properties.get("j:mixinExtends");
        if (values != null) {
            nodeType.setMixinExtendNames(new ArrayList(Arrays.asList(values)));
        } else {
            nodeType.setMixinExtendNames(new ArrayList<String>());
        }
        values = properties.get("j:primaryItemName");
        if (values != null && values.length > 0) {
            nodeType.setPrimaryItemName(values[0]);
        } else {
            nodeType.setPrimaryItemName(null);
        }
        if ("jnt:mixinNodeType".equals(data.getType())) {
            nodeType.setMixin(true);
        } else {
            nodeType.setMixin(false);
        }
        nodeTypeRegistry.addNodeType(nodeType.getNameObject(), nodeType);
        try {
            nodeType.validate();
        } catch (NoSuchNodeTypeException e) {
            logger.error("Failed to save child node definition", e);
            nodeTypeRegistryMap.remove(module);
            return;
        }
        writeDefinitionFile(nodeTypeRegistry, StringUtils.substringBeforeLast(path, "/"), splitPath[1]);
    }

    private void savePropertyDefinition(ExternalData data) {
        String path = data.getPath();
        String[] splitPath = path.split("/");
        String module = splitPath[1];
        NodeTypeRegistry nodeTypeRegistry = loadRegistry(StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/"), module);
        String nodeTypeName = splitPath[splitPath.length - 2];
        String lastPathSegment = splitPath[splitPath.length - 1];
        try {
            ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
            boolean unstructured = "%2A".equals(lastPathSegment) || "jnt:unstructuredPropertyDefinition".equals(data.getType());
            ExtendedPropertyDefinition propertyDefinition;
            if (!"%2A".equals(lastPathSegment) && unstructured) {
                Integer key = Integer.valueOf(StringUtils.substringAfter(lastPathSegment, UNSTRUCTURED_PROPERTY));
                propertyDefinition = nodeType.getDeclaredUnstructuredPropertyDefinitions().get(key);
            } else {
                propertyDefinition = nodeType.getDeclaredPropertyDefinitionsAsMap().get(lastPathSegment);
            }
            if (propertyDefinition == null) {
                propertyDefinition = new ExtendedPropertyDefinition(nodeTypeRegistry);
                String qualifiedName;
                if (unstructured) {
                    qualifiedName = "*";
                } else {
                    qualifiedName = lastPathSegment;
                }
                Name name = new Name(qualifiedName, nodeTypeRegistry.getNamespaces());
                propertyDefinition.setName(name);
                propertyDefinition.setDeclaringNodeType(nodeType);
            }
            Map<String, String[]> properties = data.getProperties();
            String[] values = properties.get("j:autoCreated");
            if (values != null && values.length > 0) {
                propertyDefinition.setAutoCreated(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setAutoCreated(false);
            }
            values = properties.get("j:mandatory");
            if (values != null && values.length > 0) {
                propertyDefinition.setMandatory(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setMandatory(false);
            }
            values = properties.get("j:onParentVersion");
            if (values != null && values.length > 0) {
                propertyDefinition.setOnParentVersion(OnParentVersionAction.valueFromName(values[0]));
            } else {
                propertyDefinition.setOnParentVersion(OnParentVersionAction.VERSION);
            }
            values = properties.get("j:protected");
            if (values != null && values.length > 0) {
                propertyDefinition.setProtected(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setProtected(false);
            }
            values = properties.get("j:selectorType");
            int selectorType = 0;
            if (values != null && values.length > 0) {
                selectorType = SelectorType.valueFromName(values[0]);
            }
            propertyDefinition.setSelector(selectorType);
            values = properties.get("j:selectorOptions");
            ConcurrentHashMap<String, String> selectorOptions = new ConcurrentHashMap<String, String>();
            if (values != null) {
                for (String option : values) {
                    String[] keyValue = option.split("=");
                    if (keyValue.length > 1) {
                        selectorOptions.put(keyValue[0].trim(), StringUtils.strip(keyValue[1].trim(), "'"));
                    } else {
                        selectorOptions.put(keyValue[0].trim(), "");
                    }
                }
            }
            propertyDefinition.setSelectorOptions(selectorOptions);
            values = properties.get("j:requiredType");
            int requiredType = 0;
            if (values != null && values.length > 0) {
                requiredType = PropertyType.valueFromName(values[0]);
            }
            propertyDefinition.setRequiredType(requiredType);
            values = properties.get("j:valueConstraints");
            List<Value> valueConstraints = new ArrayList<Value>();
            if (values != null) {
                for (String valueConstraint : values) {
                    valueConstraints.add(getValueFromString(valueConstraint, requiredType, propertyDefinition));
                }
            }
            propertyDefinition.setValueConstraints(valueConstraints.toArray(new Value[valueConstraints.size()]));
            values = properties.get("j:defaultValues");
            List<Value> defaultValues = new ArrayList<Value>();
            if (values != null) {
                for (String defaultValue : values) {
                    defaultValues.add(getValueFromString(defaultValue, requiredType, propertyDefinition));
                }
            }
            propertyDefinition.setDefaultValues(defaultValues.toArray(new Value[defaultValues.size()]));
            values = properties.get("j:multiple");
            if (values != null && values.length > 0) {
                propertyDefinition.setMultiple(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setMultiple(false);
            }
            values = properties.get("j:availableQueryOperators");
            if (values != null) {
                propertyDefinition.setAvailableQueryOperators(values);
            } else {
                propertyDefinition.setAvailableQueryOperators(Lexer.ALL_OPERATORS);
            }
            values = properties.get("j:isFullTextSearchable");
            if (values != null && values.length > 0) {
                propertyDefinition.setFullTextSearchable(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setFullTextSearchable(true);
            }
            values = properties.get("j:isQueryOrderable");
            if (values != null && values.length > 0) {
                propertyDefinition.setQueryOrderable(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setQueryOrderable(true);
            }
            values = properties.get("j:isFacetable");
            if (values != null && values.length > 0) {
                propertyDefinition.setFacetable(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setFacetable(false);
            }
            values = properties.get("j:isHierarchical");
            if (values != null && values.length > 0) {
                propertyDefinition.setHierarchical(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setHierarchical(false);
            }
            values = properties.get("j:isInternationalized");
            if (values != null && values.length > 0) {
                propertyDefinition.setInternationalized(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setInternationalized(false);
            }
            values = properties.get("j:isHidden");
            if (values != null && values.length > 0) {
                propertyDefinition.setHidden(Boolean.parseBoolean(values[0]));
            } else {
                propertyDefinition.setHidden(false);
            }
            values = properties.get("j:index");
            if (values != null && values.length > 0) {
                propertyDefinition.setIndex(IndexType.valueFromName(values[0].toLowerCase()));
            } else {
                propertyDefinition.setIndex(IndexType.TOKENIZED);
            }
            values = properties.get("j:scoreboost");
            if (values != null && values.length > 0) {
                propertyDefinition.setScoreboost(Double.parseDouble(values[0]));
            } else {
                propertyDefinition.setScoreboost(1.);
            }
            values = properties.get("j:analyzer");
            if (values != null && values.length > 0) {
                propertyDefinition.setAnalyzer(values[0]);
            } else {
                propertyDefinition.setAnalyzer(null);
            }
            values = properties.get("j:onConflictAction");
            if (values != null && values.length > 0) {
                propertyDefinition.setOnConflict(OnConflictAction.valueFromName(values[0]));
            } else {
                propertyDefinition.setOnConflict(OnConflictAction.USE_LATEST);
            }
            values = properties.get("j:itemType");
            if (values != null && values.length > 0) {
                propertyDefinition.setItemType(values[0]);
            } else {
                propertyDefinition.setItemType(null);
            }
            nodeType.validate();
            writeDefinitionFile(nodeTypeRegistry,
                    StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/"), splitPath[1]);
        } catch (NoSuchNodeTypeException e) {
            logger.error("Failed to save child node definition", e);
            nodeTypeRegistryMap.remove(module);
        }
    }

    private Value getValueFromString(String value, int requiredType, ExtendedPropertyDefinition propertyDefinition) {
        if (value.contains("(")) {
            String[] params = StringUtils.substringBetween(value, "(", ")").split(",");
            List<String> paramList = new ArrayList<String>();
            for (String param : params) {
                param = param.trim();
                if (!"".equals(param)) {
                    paramList.add(param);
                }
            }
            return new DynamicValueImpl(value, paramList, requiredType, false, propertyDefinition);
        } else {
            return new ValueImpl(value, requiredType, false);
        }
    }

    private void saveChildNodeDefinition(ExternalData data) {
        String path = data.getPath();
        String[] splitPath = path.split("/");
        String module = splitPath[1];
        NodeTypeRegistry nodeTypeRegistry = loadRegistry(StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/"), module);
        String nodeTypeName = splitPath[splitPath.length - 2];
        String lastPathSegment = splitPath[splitPath.length - 1];
        try {
            boolean newUnstructured = "%2A".equals(lastPathSegment);
            ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
            boolean unstructured = "jnt:unstructuredChildNodeDefinition".equals(data.getType());
            ExtendedNodeDefinition childNodeDefinition = null;
            if (!newUnstructured) {
                if (unstructured) {
                    String key = StringUtils.substringAfter(lastPathSegment, UNSTRUCTURED_CHILD_NODE);
                    childNodeDefinition = nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(key);
                } else {
                    childNodeDefinition = nodeType.getDeclaredChildNodeDefinitionsAsMap().get(lastPathSegment);
                }
            }
            if (childNodeDefinition == null) {
                childNodeDefinition = new ExtendedNodeDefinition(nodeTypeRegistry);
                String qualifiedName;
                if (newUnstructured || unstructured) {
                    qualifiedName = "*";
                } else {
                    qualifiedName = lastPathSegment;
                }
                Name name = new Name(qualifiedName, nodeTypeRegistry.getNamespaces());
                childNodeDefinition.setName(name);
                childNodeDefinition.setRequiredPrimaryTypes(data.getProperties().get("j:requiredPrimaryTypes"));
                childNodeDefinition.setDeclaringNodeType(nodeType);
            }
            Map<String, String[]> properties = data.getProperties();
            String[] values = properties.get("j:autoCreated");
            if (values != null && values.length > 0) {
                childNodeDefinition.setAutoCreated(Boolean.parseBoolean(values[0]));
            } else {
                childNodeDefinition.setAutoCreated(false);
            }
            values = properties.get("j:mandatory");
            if (values != null && values.length > 0) {
                childNodeDefinition.setMandatory(Boolean.parseBoolean(values[0]));
            } else {
                childNodeDefinition.setMandatory(false);
            }
            values = properties.get("j:onParentVersion");
            if (values != null && values.length > 0) {
                childNodeDefinition.setOnParentVersion(OnParentVersionAction.valueFromName(values[0]));
            } else {
                childNodeDefinition.setOnParentVersion(OnParentVersionAction.VERSION);
            }
            values = properties.get("j:protected");
            if (values != null && values.length > 0) {
                childNodeDefinition.setProtected(Boolean.parseBoolean(values[0]));
            } else {
                childNodeDefinition.setProtected(false);
            }
            values = properties.get("j:requiredPrimaryTypes");
            childNodeDefinition.setRequiredPrimaryTypes(values);
            values = properties.get("j:defaultPrimaryType");
            if (values != null && values.length > 0) {
                childNodeDefinition.setDefaultPrimaryType(values[0]);
            } else {
                childNodeDefinition.setDefaultPrimaryType(null);
            }
            nodeType.validate();
            writeDefinitionFile(nodeTypeRegistry,
                    StringUtils.join(Arrays.asList(splitPath).subList(0, splitPath.length - 2), "/"), splitPath[1]);
        } catch (NoSuchNodeTypeException e) {
            logger.error("Failed to save child node definition", e);
            nodeTypeRegistryMap.remove(module);
        }
    }

    private void writeDefinitionFile(NodeTypeRegistry nodeTypeRegistry, String path, String module) {
        try {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(new File(rootPath + path)));
                Map<String, String> namespaces = nodeTypeRegistry.getNamespaces();
                namespaces.remove("rep");
                new JahiaCndWriter(nodeTypeRegistry.getNodeTypes(module), namespaces, writer);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        } catch (IOException e) {
            logger.error("Failed to write definition file", e);
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
                logger.error("Failed to load node type registry", e);
            } catch (ParseException e) {
                logger.error("Failed to load node type registry", e);
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

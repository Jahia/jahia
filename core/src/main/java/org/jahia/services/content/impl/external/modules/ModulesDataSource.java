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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRContentUtils;
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
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.version.OnParentVersionAction;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data source provider that is mapped to the /modules filesystem folder with deployed Jahia modules.
 *
 * @author david
 * @since 6.7
 */
public class ModulesDataSource extends VFSDataSource {

    protected static final String UNSTRUCTURED_PROPERTY = "__prop__";
    protected static final String UNSTRUCTURED_CHILD_NODE = "__node__";
    private static final String PROPERTIES_EXTENSION = ".properties";

    private JahiaTemplatesPackage module;

    private Map<String, String> fileTypeMapping;

    private Map<String, String> folderTypeMapping;

    private List<String> supportedNodeTypes;

    private Map<String, NodeTypeRegistry> nodeTypeRegistryMap = new HashMap<String, NodeTypeRegistry>();

    /**
     * Return the children of the specified path.
     * @param path path of which we want to know the children
     * @return
     */
    @Override
    public List<String> getChildren(String path) {
        if (path.endsWith(".cnd") || path.contains(".cnd/")) {
            return getCndChildren(path);
        } else {
            List<String> children = super.getChildren(path);
            CollectionUtils.filter(children, new Predicate() {
                @Override
                public boolean evaluate(Object object) {
                    return !object.toString().startsWith(".");
                }
            });
            return children;
        }
    }

    /**
     * Allows to know the nodetype associated to a filetype.
     * @param fileObject the file object that we want to know the associated nodetype
     * @return the associated nodetype
     * @throws FileSystemException
     */
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
                if (relativeDepth == 1 && isNodeType(fileObject.getName().getBaseName())) {
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
        if ((fileObject.getParent() !=null
                && StringUtils.equals(Constants.JAHIANT_TEMPLATETYPEFOLDER,getDataType(fileObject.getParent()))))  {
            if (StringUtils.endsWith(fileObject.getName().toString(), PROPERTIES_EXTENSION)) {
                type = "jnt:editableFile";
            } else {
                type = Constants.JAHIANT_VIEWFILE;
            }
        }
        if (type == null && fileObject.getType() == FileType.FILE
                && (fileObject.getContent().getContentInfo().getContentType() == null
                // at least remove image binary files from rendering
                || fileObject.getContent().getContentInfo().getContentType() !=null
                        && !StringUtils.contains(fileObject.getContent().getContentInfo().getContentType(),"image"))) {
            type = "jnt:editableFile";
        }
        return type != null ? type : super.getDataType(fileObject);
    }

    /**
     * Test if the name is a known node type in the system
     * @param name
     * @return
     */
    public boolean isNodeType(String name) {
        name = name.replaceFirst("_",":");
        for (Map.Entry<String, NodeTypeRegistry> entry : nodeTypeRegistryMap.entrySet()) {
            try {
                entry.getValue().getNodeType(name);
                return true;
            } catch (NoSuchNodeTypeException e) {

            }
        }
        return false;
    }

    /**
     * Return item by identifier
     * @param identifier
     * @return
     * @throws ItemNotFoundException
     */
    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        ExternalData data = super.getItemByIdentifier(identifier);
        return enhanceData(data.getPath(), data);
    }

    /**
     * Return item by path
     * @param path
     * @return
     * @throws PathNotFoundException
     */
    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        if (path.toLowerCase().contains(".cnd/")) {
            return getCndItemByPath(path);
        }
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
                    java.nio.charset.Charset c = "jnt:resourceBundleFile".equals(data.getType()) ? Charsets.ISO_8859_1:Charsets.UTF_8;
                    String[] propertyValue = {IOUtils.toString(is, c)};
                    data.getProperties().put("sourceCode", propertyValue);
                    data.getProperties().put("nodeTypeName", new String[]{path.split("/")[1].replace("_", ":")});
                } catch (Exception e) {
                    logger.error("Failed to read source code", e);
                } finally {
                    IOUtils.closeQuietly(is);
                }

                // set Properties
                if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
                    Properties properties = new SortedProperties();
                    is = null;
                    try {
                        is = getFile(StringUtils.substringBeforeLast(path,".") + PROPERTIES_EXTENSION).getContent().getInputStream();
                        properties.load(is);
                        Map<String, String[]> dataProperties = new HashMap<String, String[]>();
                        for (Map.Entry<?, ?> property : properties.entrySet()) {
                            dataProperties.put((String) property.getKey(), ((String) property.getValue()).split(","));
                        }
                        data.getProperties().putAll(dataProperties);
                    } catch (FileSystemException e) {
                        //no properties files, do nothing
                    } catch (IOException e) {
                        logger.error("Cannot read property file", e);
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unknown type", e);
        }
        return data;
    }

    /**
     * Return list of supported node types.
     * @return
     */
    @Override
    public List<String> getSupportedNodeTypes() {
        return supportedNodeTypes;
    }

    @Override
    public void removeItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");
        if (path.toLowerCase().endsWith(".cnd")) {
            nodeTypeRegistryMap.remove(path);
            super.removeItemByPath(path);
        } else if (path.toLowerCase().contains(".cnd/")) {
            removeCndItemByPath(path);
        } else {
            super.removeItemByPath(path);
        }
    }

    private void removeCndItemByPath(String path) throws PathNotFoundException {
        String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
        String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitPath = subPath.split("/");
        String nodeTypeName = splitPath[0];
        if (splitPath.length == 1) {
            try {
                NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
                nodeTypeRegistry.unregisterNodeType(nodeTypeName);
                writeDefinitionFile(nodeTypeRegistry, cndPath);
            } catch (ConstraintViolationException e) {
                throw new PathNotFoundException("Failed to remove node type " + nodeTypeName, e);
            }
        } else {
            String itemDefinitionName = splitPath[1];
            try {
                NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
                ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
                Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
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
                writeDefinitionFile(nodeTypeRegistry, cndPath);
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        }
    }

    /**
     * Move a file from one path to another one.
     * If file is of type cnd the definitions will be first remove from the registry.
     * @param oldPath
     * @param newPath
     * @throws PathNotFoundException
     */
    @Override
    public void move(String oldPath, String newPath) throws PathNotFoundException {
        if (oldPath.endsWith(".cnd")) {
            nodeTypeRegistryMap.remove(oldPath);
            super.move(oldPath, newPath);
        } else if (oldPath.toLowerCase().contains(".cnd/") && newPath.toLowerCase().contains(".cnd/")) {
            moveCndItems(oldPath, newPath);
        } else {
            super.move(oldPath, newPath);
        }
    }

    private void moveCndItems(String oldPath, String newPath) throws PathNotFoundException {
        String oldCndPath = oldPath.substring(0, oldPath.toLowerCase().indexOf(".cnd/") + 4);
        String oldSubPath = oldPath.substring(oldPath.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitOldPath = oldSubPath.split("/");

        String newCndPath = newPath.substring(0, newPath.toLowerCase().indexOf(".cnd/") + 4);
        String newSubPath = newPath.substring(newPath.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitNewPath = newSubPath.split("/");

        if ((splitOldPath.length == 1) && (splitNewPath.length == 1)) {
            String oldNodeTypeName = splitOldPath[0];
            String newNodeTypeName = splitNewPath[0];

            try {
                NodeTypeRegistry oldNodeTypeRegistry = loadRegistry(oldCndPath);
                ExtendedNodeType nodeType = oldNodeTypeRegistry.getNodeType(oldNodeTypeName);

                NodeTypeIterator declaredSubtypes = nodeType.getDeclaredSubtypes();
                List<ExtendedNodeType> n = new ArrayList<ExtendedNodeType>();

                while (declaredSubtypes.hasNext()) {
                    n.add((ExtendedNodeType) declaredSubtypes.nextNodeType());
                }

                for (ExtendedNodeType sub : n) {
                    List<String> s = new ArrayList<String>(Arrays.asList(sub.getDeclaredSupertypeNames()));
                    s.remove(oldNodeTypeName);
                    sub.setDeclaredSupertypes(s.toArray(new String[s.size()]));
                    sub.validate();
                }

                oldNodeTypeRegistry.unregisterNodeType(oldNodeTypeName);

                Name name = new Name(newNodeTypeName, oldNodeTypeRegistry.getNamespaces());
                nodeType.setName(name);
                NodeTypeRegistry newNodeTypeRegistry = loadRegistry(newCndPath);
                newNodeTypeRegistry.addNodeType(name, nodeType);
                nodeType.validate();

                for (ExtendedNodeType sub : n) {
                    List<String> s = new ArrayList<String>(Arrays.asList(sub.getDeclaredSupertypeNames()));
                    s.add(newNodeTypeName);
                    sub.setDeclaredSupertypes(s.toArray(new String[s.size()]));
                    sub.validate();
                }

                writeDefinitionFile(oldNodeTypeRegistry, StringUtils.substringBeforeLast(oldPath, "/"));
                if (!oldCndPath.equals(newCndPath)) {
                    writeDefinitionFile(newNodeTypeRegistry, StringUtils.substringBeforeLast(oldPath, "/"));
                }
            } catch (RepositoryException e) {
                nodeTypeRegistryMap.remove(newCndPath);
                nodeTypeRegistryMap.remove(oldCndPath);
                throw new PathNotFoundException("Failed to move node type " + oldNodeTypeName, e);
            }
        }
    }

    /**
     * Order the node type inside a cnd file in alphabetical order
     * @param path
     * @param children
     * @throws PathNotFoundException
     */
    @Override
    public void order(String path, final List<String> children) throws PathNotFoundException {
        // Order only for nodeType
        if (path.toLowerCase().contains(".cnd/")) {
            String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
            String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
            String[] splitPath = subPath.split("/");

            ExternalData data = getItemByPath(path);
            try {
                NodeTypeRegistry ntr = loadRegistry(cndPath);
                if (data.getType().equals("jnt:primaryNodeType") || data.getType().equals("jnt:mixinNodeType")) {
                    ExtendedNodeType type = ntr.getNodeType(splitPath[0]);
                    Comparator<ExtendedItemDefinition> c = new Comparator<ExtendedItemDefinition>() {
                        @Override
                        public int compare(ExtendedItemDefinition o1, ExtendedItemDefinition o2) {
                            String s1;
                            String s2;
                            if (o1.isUnstructured()) {
                                s1 = computeUnstructuredItemName(o1);
                            } else {
                                s1 = o1.getName();
                            }
                            if (o2.isUnstructured()) {
                                s2 = computeUnstructuredItemName(o2);
                            } else {
                                s2 = o2.getName();
                            }
                            if (s1 != null && s2 != null) {
                                int i1 = children.indexOf(s1);
                                int i2 = children.indexOf(s2);
                                if (i1 == i2) {
                                    return 0;
                                }
                                if (i1 > i2) {
                                    return 1;
                                }
                            }
                            return -1;
                        }
                    };
                    type.sortItems(c);
                    writeDefinitionFile(ntr, cndPath);
                }
            } catch (NoSuchNodeTypeException e) {
                logger.error("Cannot order items",e);

            }
        }
    }

    private String computeUnstructuredItemName(ExtendedItemDefinition o1) {
        String s1;
        if (o1.isNode()) {
            String s = "";
            for (ExtendedNodeType e : ((ExtendedNodeDefinition) o1).getRequiredPrimaryTypes()) {
                s += e.getName() + " ";
            }
            s1 = UNSTRUCTURED_CHILD_NODE + s.trim();
        } else {
            if (((ExtendedPropertyDefinition) o1).isMultiple()) {
                int i = 256 + ((ExtendedPropertyDefinition) o1).getRequiredType();
                s1 = UNSTRUCTURED_PROPERTY + i;
            } else {
                s1 = UNSTRUCTURED_PROPERTY + ((ExtendedPropertyDefinition) o1).getRequiredType();
            }
        }
        return s1;
    }

    @Override
    public void saveItem(ExternalData data) {
        super.saveItem(data);

        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(data.getType());

            if (type.isNodeType("jnt:editableFile")) {
                saveEditableFile(data, type);
            } else if (type.isNodeType("jnt:nodeType")) {
                saveNodeType(data);
            } else if (type.isNodeType("jnt:propertyDefinition")) {
                savePropertyDefinition(data);
            } else if (type.isNodeType("jnt:childNodeDefinition")) {
                saveChildNodeDefinition(data);
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unknown type", e);
        }
    }

    private void saveEditableFile(ExternalData data, ExtendedNodeType type) {
        OutputStream outputStream = null;
        // Handle source code
        try {
            outputStream = getFile(data.getPath()).getContent().getOutputStream();
            byte[] sourceCode = data.getProperties().get("sourceCode")[0].getBytes();
            outputStream.write(sourceCode);
        } catch (Exception e) {
            logger.error("Failed to write source code", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        // Handle properties
        if (type.isNodeType(Constants.JAHIAMIX_VIEWPROPERTIES)) {
            saveProperties(data);
        }
    }

    private void saveProperties(ExternalData data) {
        OutputStream outputStream = null;
        try {
            ExtendedNodeType propertiesType = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIAMIX_VIEWPROPERTIES);
            Properties properties = new SortedProperties();
            for (Map.Entry<String, String[]> property : data.getProperties().entrySet()) {
                Map<String, ExtendedPropertyDefinition> propertyDefinitionMap = propertiesType.getDeclaredPropertyDefinitionsAsMap();
                if (propertyDefinitionMap.containsKey(property.getKey())) {
                    String[] v = property.getValue();
                    if (v != null) {
                        String propertyValue = StringUtils.join(v,",");
                        if (propertyDefinitionMap.get(property.getKey()).getRequiredType() != PropertyType.BOOLEAN ||
                            !propertyValue.equals("false")) {
                            properties.put(property, propertyValue);
                        }
                    }
                }
            }
            FileObject file = getFile(StringUtils.substringBeforeLast(data.getPath(),".") + PROPERTIES_EXTENSION);
            if (!properties.isEmpty()) {
                outputStream = file.getContent().getOutputStream();
                properties.store(outputStream, data.getPath());
            } else {
                if (file.exists()) {
                    file.delete();
                }
            }
            ResourceBundle.clearCache();
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchNodeTypeException e) {
            logger.error("Unable to find type : " + data.getType() + " for node " + data.getPath(), e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private void saveNodeType(ExternalData data) {
        String path = data.getPath();
        String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
        String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitPath = subPath.split("/");

        String nodeTypeName = splitPath[0];
        NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
        ExtendedNodeType nodeType = null;
        try {
            nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
        } catch (NoSuchNodeTypeException e) {
        }
        if (nodeType == null) {
            nodeType = new ExtendedNodeType(nodeTypeRegistry, module.getRootFolder());
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
            nodeTypeRegistryMap.remove(StringUtils.substringBeforeLast(path, "/"));
            return;
        }
        writeDefinitionFile(nodeTypeRegistry, StringUtils.substringBeforeLast(path, "/"));

        saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName));
    }

    private void saveCndResourceBundle(ExternalData data, String key) {
        Map<String, String[]> properties;
        String[] values;
//        String rbBasePath = StringUtils.substringAfter(module.getResourceBundleName(), "modules").replaceAll("\\.", "/").replaceAll("___", ".");
        String resourceBundleName = module.getResourceBundleName();
        if (resourceBundleName == null) {
            resourceBundleName = "resources." + module.getRootFolder();
        }
        String rbBasePath = "/resources/" + StringUtils.substringAfterLast(resourceBundleName, ".");
        Map<String, Map<String, String[]>> i18nProperties = data.getI18nProperties();
        if (i18nProperties != null) {
            for (String lang : i18nProperties.keySet()) {
                properties = i18nProperties.get(lang);
                String title = null;
                String description = null;
                values = properties.get("jcr:title");
                if (values != null && values.length > 0) {
                    title = values[0];
                }
                values = properties.get("jcr:description");
                if (values != null && values.length > 0) {
                    description = values[0];
                }

                String rbPath = rbBasePath + "_" + lang + PROPERTIES_EXTENSION;
                InputStream is = null;
                InputStreamReader isr = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    FileObject file = getFile(rbPath);
                    FileContent content = file.getContent();
                    Properties p = new SortedProperties();
                    if (file.exists()) {
                        is = content.getInputStream();
                        isr = new InputStreamReader(is, Charset.forName("ISO-8859-1"));
                        p.load(isr);
                        isr.close();
                        is.close();
                    } else if (StringUtils.isBlank(title) && StringUtils.isBlank(description)) {
                        continue;
                    }
                    if (!StringUtils.isEmpty(title)) {
                        p.setProperty(key, title);
                    }
                    if (!StringUtils.isEmpty(description)) {
                        p.setProperty(key+"_description", description);
                    }
                    os = content.getOutputStream();
                    osw = new OutputStreamWriter(os, Charset.forName("ISO-8859-1"));
                    p.store(osw, rbPath);
                    ResourceBundle.clearCache();
                } catch (FileSystemException e) {
                    logger.error("Failed to save resourceBundle", e);
                } catch (IOException e) {
                    logger.error("Failed to save resourceBundle", e);
                } finally {
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(isr);
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(osw);
                }
            }
        }
    }

    private void savePropertyDefinition(ExternalData data) {
        String path = data.getPath();
        String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
        String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitPath = subPath.split("/");

        NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);
        String nodeTypeName = splitPath[0];
        String lastPathSegment = splitPath[1];
        try {
            ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
            boolean unstructured = "jnt:unstructuredPropertyDefinition".equals(data.getType());
            ExtendedPropertyDefinition propertyDefinition;
            if (unstructured) {
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
                propertyDefinition.setRequiredType(PropertyType.valueFromName(data.getProperties().get("j:requiredType")[0]));
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
            int selectorType = SelectorType.SMALLTEXT;  // Default selector type is smallText
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
            List<String> ops = new ArrayList<String>();
            if (values != null) {
                for (String op : values) {
                    if (op.equals(Lexer.QUEROPS_EQUAL)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO);
                    } else if (op.equals(Lexer.QUEROPS_NOTEQUAL)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO);
                    } else if (op.equals(Lexer.QUEROPS_LESSTHAN)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN);
                    } else if (op.equals(Lexer.QUEROPS_LESSTHANOREQUAL)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO);
                    } else if (op.equals(Lexer.QUEROPS_GREATERTHAN)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN);
                    } else if (op.equals(Lexer.QUEROPS_GREATERTHANOREQUAL)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO);
                    } else if (op.equals(Lexer.QUEROPS_LIKE)) {
                        ops.add(QueryObjectModelConstants.JCR_OPERATOR_LIKE);
                    }
                }
            }
            if (ops.isEmpty()) {
                propertyDefinition.setAvailableQueryOperators(Lexer.ALL_OPERATORS);
            } else {
                propertyDefinition.setAvailableQueryOperators(ops.toArray(new String[ops.size()]));
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
            writeDefinitionFile(nodeTypeRegistry, cndPath);

            saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName) + "." + JCRContentUtils.replaceColon(lastPathSegment));
        } catch (NoSuchNodeTypeException e) {
            logger.error("Failed to save child node definition", e);
            nodeTypeRegistryMap.remove(cndPath);
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
        String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
        String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitPath = subPath.split("/");

        NodeTypeRegistry nodeTypeRegistry = loadRegistry(cndPath);

        String nodeTypeName = splitPath[0];
        String lastPathSegment = splitPath[1];
        try {
            ExtendedNodeType nodeType = nodeTypeRegistry.getNodeType(nodeTypeName);
            boolean unstructured = "jnt:unstructuredChildNodeDefinition".equals(data.getType());
            ExtendedNodeDefinition childNodeDefinition = null;
            if (unstructured) {
                String key = StringUtils.substringAfter(lastPathSegment, UNSTRUCTURED_CHILD_NODE);
                childNodeDefinition = nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(key);
            } else {
                childNodeDefinition = nodeType.getDeclaredChildNodeDefinitionsAsMap().get(lastPathSegment);
            }
            if (childNodeDefinition == null) {
                childNodeDefinition = new ExtendedNodeDefinition(nodeTypeRegistry);
                String qualifiedName;
                if (unstructured) {
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
            writeDefinitionFile(nodeTypeRegistry, cndPath);

            saveCndResourceBundle(data, JCRContentUtils.replaceColon(nodeTypeName) + "." + JCRContentUtils.replaceColon(lastPathSegment));
        } catch (NoSuchNodeTypeException e) {
            logger.error("Failed to save child node definition", e);
            nodeTypeRegistryMap.remove(cndPath);
        }
    }

    private List<String> getCndChildren(String path) {
        if (path.endsWith(".cnd")) {
            List<String> children = new ArrayList<String>();
            NodeTypeIterator nodeTypes = loadRegistry(path).getNodeTypes(module.getRootFolder());
            while (nodeTypes.hasNext()) {
                children.add(nodeTypes.nextNodeType().getName());
            }
            return children;
        } else {
            String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
            String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
            String[] splitPath = subPath.split("/");

            List<String> children = new ArrayList<String>();

            if (splitPath.length == 1) {
                String nodeTypeName = splitPath[0];
                try {
                    ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
                    for (ExtendedItemDefinition itemDefinition : nodeType.getDeclaredItems(true)) {
                        if (itemDefinition.isUnstructured()) {
                            children.add(computeUnstructuredItemName(itemDefinition));
                        } else {
                            children.add(itemDefinition.getName());
                        }
                    }
                } catch (NoSuchNodeTypeException e) {}
            }
            return children;
        }
    }

    private ExternalData getCndItemByPath(String path) throws PathNotFoundException {
        String cndPath = path.substring(0, path.toLowerCase().indexOf(".cnd/") + 4);
        String subPath = path.substring(path.toLowerCase().indexOf(".cnd/") + 5);
        String[] splitPath = subPath.split("/");
        if (splitPath.length == 1) {
            String nodeTypeName = splitPath[0];
            try {
                ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
                return getNodeTypeData(path, nodeType);
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        } else if (splitPath.length == 2) {
            String nodeTypeName = splitPath[0];
            String itemDefinitionName = splitPath[1];
            try {
                ExtendedNodeType nodeType = loadRegistry(cndPath).getNodeType(nodeTypeName);
                Map<String, ExtendedPropertyDefinition> propertyDefinitionsAsMap = nodeType.getDeclaredPropertyDefinitionsAsMap();
                if (propertyDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getPropertyDefinitionData(path, propertyDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_PROPERTY)) {
                    Integer type = Integer.valueOf(itemDefinitionName.substring(UNSTRUCTURED_PROPERTY.length()));
                    if (nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type) != null)  {
                        return getPropertyDefinitionData(path, nodeType.getDeclaredUnstructuredPropertyDefinitions().get(type), true);
                    }
                }
                Map<String, ExtendedNodeDefinition> childNodeDefinitionsAsMap = nodeType.getDeclaredChildNodeDefinitionsAsMap();
                if (childNodeDefinitionsAsMap.containsKey(itemDefinitionName)) {
                    return getChildNodeDefinitionData(path, childNodeDefinitionsAsMap.get(itemDefinitionName), false);
                }
                if (itemDefinitionName.startsWith(UNSTRUCTURED_CHILD_NODE)) {
                    String type = itemDefinitionName.substring(UNSTRUCTURED_CHILD_NODE.length()).trim();
                    if (nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type) != null) {
                        return getChildNodeDefinitionData(path, nodeType.getDeclaredUnstructuredChildNodeDefinitions().get(type), true);
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                throw new PathNotFoundException("Failed to get node type " + nodeTypeName, e);
            }
        }
        throw new PathNotFoundException("Failed to get node " + path);
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
            Function<ExtendedNodeType, String> transformName = new Function<ExtendedNodeType, String>() {
                public String apply(@Nullable ExtendedNodeType from) {
                    return from != null ? from.getName() : null;
                }
            };
            properties.put("j:mixinExtends", Collections2.<ExtendedNodeType, String>transform(mixinExtends, transformName).toArray(new String[mixinExtends.size()]));
        }
        String primaryItemName = nodeType.getPrimaryItemName();
        if (primaryItemName != null) {
            properties.put("j:primaryItemName", new String[]{primaryItemName});
        }
        ExternalData externalData = new ExternalData(path, path, nodeType.isMixin() ? "jnt:mixinNodeType" : "jnt:primaryNodeType", properties);
        Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();

        for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
            HashMap<String, String[]> value = new HashMap<String, String[]>();
            i18nProperties.put(locale.toString(), value);
            value.put("jcr:title", new String[]{nodeType.getLabel(locale)});
            value.put("jcr:description", new String[]{nodeType.getDescription(locale)});
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
            for (Value value : defaultValues) {
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
        List<String> ops = new ArrayList<String>();
        if (availableQueryOperators != null) {
            for (String op : availableQueryOperators) {
                if (QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO.equals(op)) {
                    ops.add(Lexer.QUEROPS_EQUAL);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_NOT_EQUAL_TO.equals(op)) {
                    ops.add(Lexer.QUEROPS_NOTEQUAL);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN.equals(op)) {
                    ops.add(Lexer.QUEROPS_LESSTHAN);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_LESS_THAN_OR_EQUAL_TO.equals(op)) {
                    ops.add(Lexer.QUEROPS_LESSTHANOREQUAL);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN.equals(op)) {
                    ops.add(Lexer.QUEROPS_GREATERTHAN);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_GREATER_THAN_OR_EQUAL_TO.equals(op)) {
                    ops.add(Lexer.QUEROPS_GREATERTHANOREQUAL);
                } else if (QueryObjectModelConstants.JCR_OPERATOR_LIKE.equals(op)) {
                    ops.add(Lexer.QUEROPS_LIKE);
                }
            }
            if (!ops.isEmpty()) {
                properties.put("j:availableQueryOperators", ops.toArray(new String[ops.size()]));
            }
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
                unstructured ? "jnt:unstructuredPropertyDefinition" : "jnt:propertyDefinition", properties);
        Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();

        for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
            HashMap<String, String[]> value = new HashMap<String, String[]>();
            i18nProperties.put(locale.toString(), value);
            value.put("jcr:title", new String[]{propertyDefinition.getLabel(locale)});
//            value.put("jcr:description", new String[]{propertyDefinition.getDescription(locale)});
        }

        externalData.setI18nProperties(i18nProperties);
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
                unstructured ? "jnt:unstructuredChildNodeDefinition" : "jnt:childNodeDefinition", properties);

        Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();

        for (Locale locale : LanguageCodeConverters.getAvailableBundleLocales()) {
            HashMap<String, String[]> value = new HashMap<String, String[]>();
            i18nProperties.put(locale.toString(), value);
            value.put("jcr:title", new String[]{nodeDefinition.getLabel(locale)});
//            value.put("jcr:description", new String[]{nodeDefinition.getDescription(locale)});
        }

        externalData.setI18nProperties(i18nProperties);
        return externalData;
    }


    private synchronized NodeTypeRegistry loadRegistry(String path) {
        if (nodeTypeRegistryMap.containsKey(path)) {
            return nodeTypeRegistryMap.get(path);
        } else {
            NodeTypeRegistry ntr = new NodeTypeRegistry();
            try {
                ntr.initSystemDefinitions();

                List<JahiaTemplatesPackage> dependencies = new ArrayList<JahiaTemplatesPackage>(module.getDependencies());
                Collections.reverse(dependencies);
                for (JahiaTemplatesPackage depend : dependencies) {
                    for (String s : depend.getDefinitionsFiles()) {
                        ntr.addDefinitionsFile(depend.getResource(s), depend.getRootFolder(), null);
                    }
                }
                ntr.addDefinitionsFile(new UrlResource(getFile(path).getURL()), module.getRootFolder(), null);
            } catch (IOException e) {
                logger.error("Failed to load node type registry", e);
            } catch (ParseException e) {
                logger.error("Failed to load node type registry", e);
            }
            nodeTypeRegistryMap.put(path, ntr);
            return ntr;
        }
    }

    private void writeDefinitionFile(NodeTypeRegistry nodeTypeRegistry, String path) {
        try {
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(new FileOutputStream(new File(rootPath + path)));
                Map<String, String> namespaces = nodeTypeRegistry.getNamespaces();
                namespaces.remove("rep");
                if (nodeTypeRegistryMap.containsKey(path)) {
                    nodeTypeRegistryMap.get(path).flushLabels();
                }

                new JahiaCndWriter(nodeTypeRegistry.getNodeTypes(module.getRootFolder()), namespaces, writer);
            } finally {
                IOUtils.closeQuietly(writer);
            }
        } catch (IOException e) {
            logger.error("Failed to write definition file", e);
        }
    }

    /**
     * Inject mapping of file types to node types
     * @param fileTypeMapping
     */
    public void setFileTypeMapping(Map<String, String> fileTypeMapping) {
        this.fileTypeMapping = fileTypeMapping;
    }

    /**
     * Inject mapping of folder name (type) to node types
     * @param folderTypeMapping
     */
    public void setFolderTypeMapping(Map<String, String> folderTypeMapping) {
        this.folderTypeMapping = folderTypeMapping;
    }

    /**
     * Set the root folder of this module source
     * @param root
     */
    public void setRootResource(Resource root) {
        try {
            super.setRoot("file://" + root.getFile().getPath());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Injection of supported node types
     * @param supportedNodeTypes
     */
    public void setSupportedNodeTypes(List<String> supportedNodeTypes) {
        this.supportedNodeTypes = supportedNodeTypes;
    }

    /**
     * Injection on runtime of the template package associated with this module source provider.
     * @param module
     */
    public void setModule(JahiaTemplatesPackage module) {
        this.module = module;

        for (String s : getChildren("/META-INF")) {
            if (s.endsWith(".cnd")) {
                loadRegistry("/META-INF/"+s);
            }
        }

    }

    class SortedProperties extends Properties {
        @Override
        public synchronized Enumeration<Object> keys() {
            return new Vector(new TreeSet(super.keySet())).elements();
        }
    }
}

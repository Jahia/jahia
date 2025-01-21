/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import com.google.common.collect.Sets;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Jahia implementation of the {@link NodeTypeManager}.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 15:08:56
 */
public class NodeTypeRegistry implements NodeTypeManager {
    private static final Logger logger = LoggerFactory.getLogger(NodeTypeRegistry.class);

    private final Map<Name, ExtendedNodeType> nodetypes = new LinkedHashMap<>();

    private final BidiMap namespaces = new DualHashBidiMap();

    @SuppressWarnings("unchecked")
    private final Map<String, List<Resource>> files = new ListOrderedMap();

    // map of mixin and associate extended node types index by mixin
    private final Map<ExtendedNodeType, List<ExtendedNodeType>> extensionsMixin = new HashMap<>();
    // map of extended node types and associate mixin index by extended node type
    private final Map<ExtendedNodeType, Set<ExtendedNodeType>> mixinExtensions = new HashMap<>();
    private final Map<String, Set<ExtendedItemDefinition>> typedItems = new HashMap<>();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final NodeTypeRegistry INSTANCE = new NodeTypeRegistry();
    }
    public static NodeTypeRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Flush all labels for all node types and items
     */
    public void flushLabels() {
        writeLock.lock();
        try {
            for (ExtendedNodeType nodeType : nodetypes.values()) {
                nodeType.clearLabels();
            }
            for (Set<ExtendedItemDefinition> itemSet : typedItems.values()) {
                for (ExtendedItemDefinition item : itemSet) {
                    item.clearLabels();
                    item.getDeclaringNodeType().clearLabels();
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get all system definitions files
     * @return system definitions files
     */
    public static Map<String,File> getSystemDefinitionsFiles() {
        Map<String, File> res = new LinkedHashMap<>();
        String cnddir = SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/nodetypes";
        File f = new File(cnddir);
        File[] files = f.listFiles();
        if (files != null) {
            SortedSet<File> cndfiles = new TreeSet<>(Arrays.asList(files));
            for (File cndfile : cndfiles) {
                res.put("system-" + Patterns.DASH.split(cndfile.getName())[1], cndfile);
            }
        }
        return res;
    }


    @Deprecated(since = "7.2.0.0", forRemoval = true)
    public void addDefinitionsFile(Resource resource, String systemId, ModuleVersion version) throws IOException, ParseException, RepositoryException {
        addDefinitionsFile(Collections.singletonList(resource), systemId);
    }

    public void addDefinitionsFile(Resource resource, String systemId) throws IOException, ParseException, RepositoryException {
        addDefinitionsFile(Collections.singletonList(resource), systemId);
    }

    @Deprecated(since = "7.2.0.0", forRemoval = true)
    public void addDefinitionsFile(File file, String systemId, ModuleVersion version) throws ParseException, IOException, RepositoryException {
        addDefinitionsFile(file == null ? Collections.<Resource>emptyList() : Collections.singletonList(new FileSystemResource(file)), systemId);
    }

    public void addDefinitionsFile(File file, String systemId) throws ParseException, IOException, RepositoryException  {
        addDefinitionsFile(file == null ? Collections.<Resource>emptyList() : Collections.singletonList(new FileSystemResource(file)), systemId);
    }


    public void addDefinitionsFile(List<? extends Resource> resources, String systemId) throws IOException, ParseException, RepositoryException {
        List<ExtendedNodeType> types = new ArrayList<>();
        for (Resource resource : resources) {
            logger.debug("Adding definitions file {} for {}", resource, systemId);
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                JahiaCndReader r = new JahiaCndReader(resourceReader, resource.toString(), systemId, this);
                r.parse();
                types.addAll(r.getNodeTypesList());
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        }

        registerNodeTypes(types);

        if (!files.containsKey(systemId)) {
            files.put(systemId, new ArrayList<Resource>());
        }
        for (Resource resource : resources) {
            if (!files.get(systemId).contains(resource)) {
                files.get(systemId).add(resource);
            }
        }
    }

    /**
     * Register node types into the registry. Replaces existing types
     *
     * Fill supertypes/subtypes lists
     *
     * @throws NoSuchNodeTypeException if one of the supertype/mixin extend cannot be found
     */
    private void registerNodeTypes(List<ExtendedNodeType> nodeTypesList) throws NoSuchNodeTypeException , InvalidNodeTypeDefinitionException{
        writeLock.lock();
        try {
            // Replaces types,
            List<ExtendedNodeType> previousTypes = new ArrayList<>();
            for (ExtendedNodeType nodeType : nodeTypesList) {
                ExtendedNodeType previous = nodetypes.put(nodeType.getNameObject(), nodeType);
                if (previous != null) {
                    previousTypes.add(previous);

                    // check for subTypes coming from other bundles, already registered on previous nodetype
                    NodeTypeIterator subtypes = previous.getDeclaredSubtypes();
                    while (subtypes.hasNext()) {
                        ExtendedNodeType subType = (ExtendedNodeType) subtypes.next();
                        if (!StringUtils.equals(subType.getSystemId(), previous.getSystemId())) {
                            nodeType.addSubType(subType);
                        }
                    }
                }
            }

            for (ExtendedNodeType type : nodeTypesList) {
                try {
                    type.validate();
                    if (!type.getPrefix().equals("nt") && !type.isMixin() && !type.isNodeType(Constants.MIX_REFERENCEABLE)) {
                        int length = type.getDeclaredSupertypeNames().length;
                        String[] newTypes = new String[length + 1];
                        System.arraycopy(type.getDeclaredSupertypeNames(), 0, newTypes, 0, length);
                        newTypes[length] = Constants.MIX_REFERENCEABLE;
                        type.setDeclaredSupertypes(newTypes);
                        type.validate();
                    }
                } catch (NoSuchNodeTypeException e) {
                    logger.error("Cannot validate type", e);
                    handleError(nodeTypesList, previousTypes);
                    throw e;
                }
            }
            for (ExtendedNodeType type : nodeTypesList) {
                try {
                    type.checkConflicts();
                } catch (InvalidNodeTypeDefinitionException e) {
                    logger.error("Cannot validate type", e);
                    handleError(nodeTypesList, previousTypes);
                    throw e;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void handleError(List<ExtendedNodeType> nodeTypesList, List<ExtendedNodeType> previousTypes){
        // Restoring previous state
        for (ExtendedNodeType addedType : nodeTypesList) {
            removeNodeType(addedType.getNameObject());
        }
        for (ExtendedNodeType previousType : previousTypes) {
            nodetypes.put(previousType.getNameObject(), previousType);
        }
    }

    /**
     * Reads the specified CND file resource and parses it to obtain a list of node type definitions.
     *
     * @param resource
     *            a resource, representing a CND file
     * @param systemId
     *            the ID to use to specify the "origin" on the node types from this file
     * @return a list of the node types parsed from the specified resource
     * @throws ParseException
     *             in case of a parsing error
     * @throws IOException
     *             in case of an I/O error when reading the specified resource
     */
    public List<ExtendedNodeType> getDefinitionsFromFile(Resource resource, String systemId) throws ParseException, IOException {
        String ext = resource.getURL().getPath().substring(resource.getURL().getPath().lastIndexOf('.'));
        if (ext.equalsIgnoreCase(".cnd")) {
            Reader resourceReader = null;
            try {
                resourceReader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);
                JahiaCndReader r = new JahiaCndReader(resourceReader, resource.getURL().getPath(), systemId, this);
                r.parse();
                return r.getNodeTypesList();
            } finally {
                IOUtils.closeQuietly(resourceReader);
            }
        }
        return Collections.emptyList();
    }

    public List<String> getSystemIds() {
        return new ArrayList<>(files.keySet());
    }

    public List<Resource> getFiles(String systemId) {
        return files.get(systemId);
    }

    public ExtendedNodeType getNodeType(String name) throws NoSuchNodeTypeException {
        return getNodeType(name, true);
    }

    public ExtendedNodeType getNodeType(String name, boolean throwExceptionIfMissing) throws NoSuchNodeTypeException {
        readLock.lock();
        try {
            ExtendedNodeType res = StringUtils.isNotEmpty(name) ? nodetypes.get(new Name(name, namespaces)) : null;
            if (res == null && throwExceptionIfMissing) {
                throw new NoSuchNodeTypeException("Unknown type : " + name);
            }
            return res;
        } finally {
            readLock.unlock();
        }
    }

    public JahiaNodeTypeIterator getAllNodeTypes() {
        readLock.lock();
        try {
            final Collection<ExtendedNodeType> values = new ArrayList<>(nodetypes.values());
            return new JahiaNodeTypeIterator(values.iterator(), values.size());
        } finally {
            readLock.unlock();
        }
    }

    public JahiaNodeTypeIterator getAllNodeTypes(List<String> systemIds) {
        if (systemIds == null || systemIds.isEmpty()) {
            return getAllNodeTypes();
        } else {
            List<ExtendedNodeType> res = new ArrayList<>();
            readLock.lock();
            try {
                for (ExtendedNodeType nt : nodetypes.values()) {
                    if (systemIds.contains(nt.getSystemId())) {
                        res.add(nt);
                    }
                }
                return new JahiaNodeTypeIterator(res.iterator(), res.size());
            } finally {
                readLock.unlock();
            }
        }

    }

    public JahiaNodeTypeIterator getNodeTypes(String systemId) {
        return getAllNodeTypes(Collections.singletonList(systemId));
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
        return getPrimaryNodeTypes(null);
    }

    public NodeTypeIterator getPrimaryNodeTypes(List<String> systemIds) throws RepositoryException {
        final boolean addAll = systemIds == null || systemIds.isEmpty();
        final List<ExtendedNodeType> res = new ArrayList<>();

        readLock.lock();
        try {
            for (ExtendedNodeType nt : nodetypes.values()) {
                if (!nt.isMixin() && (addAll || systemIds.contains(nt.getSystemId()))) {
                    res.add(nt);
                }
            }
            return new JahiaNodeTypeIterator(res.iterator(), res.size());
        } finally {
            readLock.unlock();
        }
    }

    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
        return getMixinNodeTypes(null);
    }

    public NodeTypeIterator getMixinNodeTypes(List<String> systemIds) throws RepositoryException {
        final boolean addAll = systemIds == null || systemIds.isEmpty();
        final List<ExtendedNodeType> res = new ArrayList<>();

        readLock.lock();
        try {
            for (ExtendedNodeType nt : nodetypes.values()) {
                if (nt.isMixin() && (addAll || systemIds.contains(nt.getSystemId()))) {
                    res.add(nt);
                }
            }
            return new JahiaNodeTypeIterator(res.iterator(), res.size());
        } finally {
            readLock.unlock();
        }
    }

    public void addNodeType(Name name, ExtendedNodeType nodeType) throws NodeTypeExistsException {
        writeLock.lock();
        try {
            ExtendedNodeType previousNodeType = nodetypes.get(name);
            if ((previousNodeType != null) && !Objects.equals(previousNodeType.getSystemId(), nodeType.getSystemId())) {
                String errorMessage = String.format(
                        "Node type '%s' already defined with a different systemId (existing: '%s', provided: '%s' with name: '%s')",
                        name, previousNodeType.getSystemId(), nodeType.getSystemId(), nodeType.getName()
                );
                throw new NodeTypeExistsException(errorMessage);
            }

            nodetypes.put(name, nodeType);

        } finally {
            writeLock.unlock();
        }
    }

    public void addMixinExtension(ExtendedNodeType mixin, ExtendedNodeType baseType) {
        writeLock.lock();
        try {
            Set<ExtendedNodeType> mixins = mixinExtensions.computeIfAbsent(baseType, k -> new HashSet<>());
            mixins.remove(mixin);
            mixins.add(mixin);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Add extends mixin (mixin that extends one or multiple types) on given extended names (nodetypes to be extends)
     * @param mixin the mixin
     * @param mixinExtendNames the types that need to be extended
     * @return The list of extended node types
     * @throws NoSuchNodeTypeException
     */
    public List<ExtendedNodeType> addMixinExtensions(ExtendedNodeType mixin, List<String> mixinExtendNames) throws NoSuchNodeTypeException {
        writeLock.lock();
        try {
            final List<ExtendedNodeType> newMixinExtend = new ArrayList<>();
            for (String name : mixinExtendNames) {
                ExtendedNodeType type = getNodeType(name);
                addMixinExtension(mixin, type);
                newMixinExtend.add(type);
            }

            if (extensionsMixin.containsKey(mixin)) {
                // mixin was already extending some node types, check if we have to remove this extension on node types
                Set<ExtendedNodeType> extendedNodeTypes = new HashSet<>(extensionsMixin.get(mixin));
                extendedNodeTypes.removeAll(newMixinExtend);

                if (!extendedNodeTypes.isEmpty()) {
                    // some node types should not be extended by this mixin anymore
                    for (ExtendedNodeType extendedNodeType : extendedNodeTypes) {
                        mixinExtensions.get(extendedNodeType).remove(mixin);
                    }
                }
            }

            // store the extends nodetypes index by mixin
            extensionsMixin.put(mixin, newMixinExtend);

            return newMixinExtend;

        } finally {
            writeLock.unlock();
        }
    }

    public Map<ExtendedNodeType, Set<ExtendedNodeType>> getMixinExtensions() {
        readLock.lock();
        try {
            return new HashMap<>(mixinExtensions);
        } finally {
            readLock.unlock();
        }
    }

    public void addTypedItem(ExtendedItemDefinition itemDef) {
        final String type = itemDef.getItemType();
        writeLock.lock();
        try {
            Set<ExtendedItemDefinition> definitions = typedItems.computeIfAbsent(type, k -> new HashSet<>());
            definitions.add(itemDef);
        } finally {
            writeLock.unlock();
        }
    }

    public Map<String, Set<ExtendedItemDefinition>> getTypedItems() {
        readLock.lock();
        try {
            return new HashMap<>(typedItems);
        } finally {
            readLock.unlock();
        }
    }

    public void unregisterNodeType(Name name) {
        writeLock.lock();
        try {
            removeNodeType(name);
        } finally {
            writeLock.unlock();
        }
    }

    private void removeNodeType(Name name) {
        ExtendedNodeType ent = nodetypes.remove(name);
        if (ent != null) {
            // Remove type from supertype subtypes list and mixin extends list
            for (ExtendedNodeType type : ent.getDeclaredSupertypes()) {
                if (type != null) {
                    type.removeSubType(ent);
                }
            }
            for (ExtendedNodeType type : ent.getMixinExtends()) {
                if (type != null && mixinExtensions.containsKey(type)) {
                    mixinExtensions.get(type).remove(ent);
                }
            }
        }
    }

    public void unregisterNodeTypes(String systemId) {
        writeLock.lock();
        try {
            for (Name n : new HashSet<>(nodetypes.keySet())) {
                ExtendedNodeType nt = nodetypes.get(n);
                if (systemId.equals(nt.getSystemId())) {
                    unregisterNodeType(n);
                }
            }
        } finally {
            writeLock.unlock();
        }

        files.remove(systemId);
    }

    public static class JahiaNodeTypeIterator implements NodeTypeIterator, Iterable<ExtendedNodeType> {
        private long size;
        private long pos = 0;
        private final Iterator<ExtendedNodeType> iterator;

        JahiaNodeTypeIterator(Iterator<ExtendedNodeType> it, long size) {
            this.iterator = it;
            this.size = size;
        }

        public NodeType nextNodeType() {
            pos += 1;
            return iterator.next();
        }

        public void skip(long l) {
            if ((pos + l + 1) > size) {
                throw new NoSuchElementException("Tried to skip past " + l +
                        " elements, which with current pos (" + pos +
                        ") brings us past total size=" + size);
            }
            for (int i = 0; i < l; i++) {
                next();
            }
        }

        public long getSize() {
            return size;
        }

        public long getPosition() {
            return pos;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public Object next() {
            return nextNodeType();
        }

        @Override
        public void remove() {
            iterator.remove();
            size -= 1;
        }

        /**
         * Returns an iterator over a set of elements of type T.
         *
         * @return an Iterator.
         */
        @Override
        public Iterator<ExtendedNodeType> iterator() {
            return this;
        }
    }

    public boolean hasNodeType(String name) {
        readLock.lock();
        try {
            return nodetypes.get(new Name(name, namespaces)) != null;
        } finally {
            readLock.unlock();
        }
    }

    public NodeTypeTemplate createNodeTypeTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition ntd) throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeDefinitionTemplate createNodeDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeType registerNodeType(NodeTypeDefinition ntd, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] ntds, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void unregisterNodeType(String name) throws ConstraintViolationException {
        final Name n = new Name(name, namespaces);

        writeLock.lock();
        try {
            if (nodetypes.containsKey(n)) {
                for (ExtendedNodeType type : nodetypes.values()) {
                    if (!type.getName().equals(name)) {
                        for (ExtendedNodeType nt : type.getSupertypes()) {
                            if (nt.getName().equals(name)) {
                                throw new ConstraintViolationException(
                                        "Cannot unregister node type " + name + " because " + type.getName() + " extends it.");
                            }
                        }
                        for (ExtendedNodeDefinition ntd : type.getChildNodeDefinitions()) {
                            if (Sets.newHashSet(ntd.getRequiredPrimaryTypeNames()).contains(name)) {
                                throw new ConstraintViolationException(
                                        "Cannot unregister node type " + name + " because a child node definition of " + type.getName() + " requires it.");
                            }
                        }
                        for (ExtendedNodeDefinition ntd : type.getUnstructuredChildNodeDefinitions().values()) {
                            if (Sets.newHashSet(ntd.getRequiredPrimaryTypeNames()).contains(name)) {
                                throw new ConstraintViolationException(
                                        "Cannot unregister node type " + name + " because a child node definition of " + type.getName() + " requires it.");
                            }
                        }
                    }
                }

                removeNodeType(n);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void unregisterNodeTypes(String[] names) throws ConstraintViolationException {
        for (String name : names) {
            unregisterNodeType(name);
        }
    }
}


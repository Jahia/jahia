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

package org.jahia.services.content.nodetypes;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import javax.jcr.nodetype.*;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import java.io.*;
import java.util.*;

/**
 * Jahia implementation of the {@link NodeTypeManager}.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 15:08:56
 */
public class NodeTypeRegistry implements NodeTypeManager {
    public static final String SYSTEM = "system";
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypeRegistry.class);

    private List<ExtendedNodeType> nodeTypesList = new ArrayList<ExtendedNodeType>();
    private Map<Name, ExtendedNodeType> nodetypes = new HashMap<Name, ExtendedNodeType>();

    private Map<String,String> namespaces = new HashMap<String,String>();

    private Map<String,List<File>> files = new ListOrderedMap();

    private Map<ExtendedNodeType,Set<ExtendedNodeType>> mixinExtensions = new HashMap<ExtendedNodeType,Set<ExtendedNodeType>>();
    private Map<String,Set<ExtendedItemDefinition>> typedItems = new HashMap<String,Set<ExtendedItemDefinition>>();

    private static final NodeTypeRegistry instance = new NodeTypeRegistry();

    public static NodeTypeRegistry getInstance() {
        return instance;
    }
    
    public static void flushLabels() {
        for (ExtendedNodeType nodeType : getInstance().nodetypes.values()) {
            nodeType.clearLabels();
        }
        for (Set<ExtendedItemDefinition> itemSet : getInstance().typedItems.values()) {
            for (ExtendedItemDefinition item : itemSet) {
                item.clearLabels();
                item.getDeclaringNodeType().clearLabels();
            }
        }
    }

    public NodeTypeRegistry() {
        try {
            String cnddir = org.jahia.settings.SettingsBean.getInstance().getJahiaEtcDiskPath() + "/repository/nodetypes";
            try {
                File f = new File(cnddir);
                SortedSet<File> cndfiles = new TreeSet<File>(Arrays.asList(f.listFiles()));
                for (File file : cndfiles) {
                    addDefinitionsFile(file, SYSTEM + "-" + file.getName().split("-")[1]);
                }
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void addDefinitionsFile(File file, String systemId) throws ParseException, IOException {
        String ext = file.getName().substring(file.getName().lastIndexOf('.'));
        if (ext.equalsIgnoreCase(".cnd")) {
            FileReader defsReader = null;
            try {
                defsReader = new FileReader(file);
                JahiaCndReader r = new JahiaCndReader(defsReader, file.getPath(), systemId, this);
                r.parse();
            } finally {
                IOUtils.closeQuietly(defsReader);
            }
        } else if (ext.equalsIgnoreCase(".grp")) {
            FileReader defsReader = null;
            try {
                defsReader = new FileReader(file);
                JahiaGroupingFileReader r = new JahiaGroupingFileReader(defsReader, file.getName(),systemId, this);
                r.parse();            
            } finally {
                IOUtils.closeQuietly(defsReader);
            }
        }

        if (!files.containsKey(systemId)) {
            files.put(systemId, new ArrayList<File>());
        }
        files.get(systemId).add(file);
    }

    public List<String> getSystemIds() {
        return new ArrayList<String>(files.keySet());
    }

    public List<File> getFiles(String systemId) {
        return files.get(systemId);
    }

    public ExtendedNodeType getNodeType(String name) throws NoSuchNodeTypeException {
        ExtendedNodeType res = nodetypes.get(new Name(name, namespaces));
        if (res == null) {
            throw new NoSuchNodeTypeException(name);
        }
        return res;
    }

    public NodeTypeIterator getAllNodeTypes() {
        return new JahiaNodeTypeIterator(nodeTypesList.iterator(),nodeTypesList.size());
    }

    public NodeTypeIterator getNodeTypes(String systemId) {
        List<ExtendedNodeType> l = new ArrayList<ExtendedNodeType>();
        for (ExtendedNodeType nt : nodeTypesList) {
            if (nt.getSystemId().equals(systemId)) {
                l.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(l.iterator(),l.size());
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
        List<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = nodetypes.values().iterator(); iterator.hasNext();) {
            ExtendedNodeType nt = iterator.next();
            if (!nt.isMixin()) {
                res.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(res.iterator(), res.size());
    }

    public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
        List<ExtendedNodeType> res = new ArrayList<ExtendedNodeType>();
        for (Iterator<ExtendedNodeType> iterator = nodetypes.values().iterator(); iterator.hasNext();) {
            ExtendedNodeType nt = iterator.next();
            if (nt.isMixin()) {
                res.add(nt);
            }
        }
        return new JahiaNodeTypeIterator(res.iterator(), res.size());
    }

    public void addNodeType(Name name, ExtendedNodeType nodeType) {
        if (nodetypes.containsKey(name)) {
            nodeTypesList.remove(nodetypes.get(name));
        }
        nodeTypesList.add(nodeType);
        nodetypes.put(name, nodeType);
    }

    public void addMixinExtension(ExtendedNodeType mixin, ExtendedNodeType baseType) {
        if (!mixinExtensions.containsKey(baseType)) {
            mixinExtensions.put(baseType, new HashSet<ExtendedNodeType>());
        }
        mixinExtensions.get(baseType).add(mixin);
    }

    public Map<ExtendedNodeType, Set<ExtendedNodeType>> getMixinExtensions() {
        return mixinExtensions;
    }

    public void addTypedItem(ExtendedItemDefinition itemDef) {
        final String type = itemDef.getItemType();
        if (!typedItems.containsKey(type)) {
            typedItems.put(type, new HashSet<ExtendedItemDefinition>());
        }
        typedItems.get(type).add(itemDef);
    }

    public Map<String, Set<ExtendedItemDefinition>> getTypedItems() {
        return typedItems;
    }

    public void unregisterNodeType(Name name) {
        ExtendedNodeType nt = nodetypes.remove(name);
        nodeTypesList.remove(nt);
    }

    public void unregisterNodeTypes(String systemId) {
        for (Name n : new HashSet<Name>(nodetypes.keySet())) {
            ExtendedNodeType nt = nodetypes.get(n);
            if (systemId.equals(nt.getSystemId())) {
                unregisterNodeType(n);
            }
        }
    }

    class JahiaNodeTypeIterator implements NodeTypeIterator {
        private long size;
        private long pos=0;
        private Iterator<ExtendedNodeType> iterator;

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
            for (int i=0; i < l; i++) {
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
            pos += 1;
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
            size -= 1;
        }
    }

    public boolean hasNodeType(String name) {
        return nodetypes.get(new Name(name, namespaces)) != null;
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

    public void unregisterNodeType(String name) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void unregisterNodeTypes(String[] names) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }
}

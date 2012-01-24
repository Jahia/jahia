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
package org.apache.jackrabbit.core.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.data.DataStore;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.id.PropertyId;
import org.apache.jackrabbit.core.state.ChangeLog;
import org.apache.jackrabbit.core.state.ChildNodeEntry;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeReferences;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.PropertyState;
import org.apache.jackrabbit.core.value.InternalValue;
import org.apache.jackrabbit.spi.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for migrating the persistence store.
 * 
 * Optimized version of {@link org.apache.jackrabbit.core.persistence.PersistenceCopier}.
 * 
 * @see org.apache.jackrabbit.core.persistence.PersistenceCopier
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaPersistenceCopier {

    private static final Logger logger = LoggerFactory.getLogger(JahiaPersistenceCopier.class);

    private ChangeLog batchLog = new ChangeLog();

    private int batchSize;

    /**
     * Identifiers of the nodes that have already been copied or that should explicitly not be copied. Used to avoid duplicate copies of
     * shareable nodes and to avoid trying to copy "missing" nodes like the virtual "/jcr:system" node.
     */
    private final Set<NodeId> exclude = new HashSet<NodeId>();

    private int maxBatchSize;

    /**
     * Source persistence manager.
     */
    private final PersistenceManager source;

    /**
     * Target data store, possibly <code>null</code>.
     */
    private final DataStore store;

    /**
     * Target persistence manager.
     */
    private final PersistenceManager target;

    private int totalCount;

    /**
     * Creates a tool for copying content from one persistence manager to another.
     * 
     * @param source
     *            source persistence manager
     * @param target
     *            target persistence manager
     * @param store
     *            target data store
     * @param batchSize
     *            the number of change logs to handle in a batch
     */
    public JahiaPersistenceCopier(PersistenceManager source, PersistenceManager target,
            DataStore store, int batchSize) {
        this.source = source;
        this.target = target;
        this.store = store;
        maxBatchSize = batchSize;
    }

    /**
     * Recursively copies the identified node and all its descendants. Explicitly excluded nodes and nodes that have already been copied are
     * automatically skipped.
     * 
     * @param id
     *            identifier of the node to be copied
     * @throws RepositoryException
     *             if the copy operation fails
     */
    public void copy(NodeId id) throws RepositoryException {
        if (!exclude.contains(id)) {
            try {
                NodeState node = source.load(id);

                for (ChildNodeEntry entry : node.getChildNodeEntries()) {
                    copy(entry.getId());
                }

                copy(node);
                exclude.add(id);
            } catch (ItemStateException e) {
                throw new RepositoryException("Unable to copy " + id, e);
            }
        }
    }

    /**
     * Copies the given node state and all associated property states to the target persistence manager.
     * 
     * @param sourceNode
     *            source node state
     * @throws RepositoryException
     *             if the copy operation fails
     */
    private void copy(NodeState sourceNode) throws RepositoryException {
        try {
            ChangeLog changes = new ChangeLog();

            // Copy the node state
            NodeState targetNode = target.createNew(sourceNode.getNodeId());
            targetNode.setParentId(sourceNode.getParentId());
            targetNode.setNodeTypeName(sourceNode.getNodeTypeName());
            targetNode.setMixinTypeNames(sourceNode.getMixinTypeNames());
            targetNode.setPropertyNames(sourceNode.getPropertyNames());
            targetNode.setChildNodeEntries(sourceNode.getChildNodeEntries());
            if (target.exists(targetNode.getNodeId())) {
                changes.modified(targetNode);
            } else {
                changes.added(targetNode);
            }

            // Copy all associated property states
            for (Name name : sourceNode.getPropertyNames()) {
                PropertyId id = new PropertyId(sourceNode.getNodeId(), name);
                PropertyState sourceState = source.load(id);
                PropertyState targetState = target.createNew(id);
                targetState.setType(sourceState.getType());
                targetState.setMultiValued(sourceState.isMultiValued());
                InternalValue[] values = sourceState.getValues();
                if (sourceState.getType() == PropertyType.BINARY) {
                    for (int i = 0; i < values.length; i++) {
                        InputStream stream = values[i].getStream();
                        try {
                            values[i] = InternalValue.create(stream, store);
                        } finally {
                            stream.close();
                        }
                    }
                }
                targetState.setValues(values);
                if (target.exists(targetState.getPropertyId())) {
                    changes.modified(targetState);
                } else {
                    changes.added(targetState);
                }
            }

            // Copy all node references
            if (source.existsReferencesTo(sourceNode.getNodeId())) {
                changes.modified(source.loadReferencesTo(sourceNode.getNodeId()));
            } else if (target.existsReferencesTo(sourceNode.getNodeId())) {
                NodeReferences references = target.loadReferencesTo(sourceNode.getNodeId());
                references.clearAllReferences();
                changes.modified(references);
            }

            if (batchSize >= maxBatchSize) {
                flush();
            }
            // Merge the copied states
            batchSize++;
            batchLog.merge(changes);
        } catch (IOException e) {
            throw new RepositoryException("Unable to copy binary values of " + sourceNode, e);
        } catch (ItemStateException e) {
            throw new RepositoryException("Unable to copy " + sourceNode, e);
        }
    }

    /**
     * Explicitly exclude the identified node from being copied. Used for excluding virtual nodes like "/jcr:system" from the copy process.
     * 
     * @param id
     *            identifier of the node to be excluded
     */
    public void excludeNode(NodeId id) {
        exclude.add(id);
    }

    /**
     * Should be called at the end of the copying to finalize the writes.
     * 
     * @throws ItemStateException
     */
    public void flush() throws RepositoryException {
        if (!batchLog.hasUpdates()) {
            batchSize = 0;
            return;
        }

        totalCount += batchSize;
        
        logger.info("Persisting batch of {} ({}) entries into the store", batchSize, totalCount);
        
        try {
            target.store(batchLog);
        } catch (ItemStateException e) {
            throw new RepositoryException("Unable to persist batch of changes: " + batchLog, e);
        } finally {
            batchSize = 0;
            batchLog.reset();
        }
    }
}

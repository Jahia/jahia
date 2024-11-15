/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.observation.EventImpl;
import org.apache.jackrabbit.core.persistence.PersistenceManager;
import org.apache.jackrabbit.core.query.QueryHandlerFactory;
import org.apache.jackrabbit.core.query.lucene.FieldSelectors;
import org.apache.jackrabbit.core.query.lucene.JahiaIndexingConfigurationImpl;
import org.apache.jackrabbit.core.query.lucene.JahiaNodeIndexer;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.query.lucene.Util;
import org.apache.jackrabbit.core.query.lucene.hits.AbstractHitCollector;
import org.apache.jackrabbit.core.state.ChildNodeEntry;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.core.state.SharedItemStateManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JahiaSearchManager extends SearchManager {
    private static final String TRANSLATION_LOCALNODENAME_PREFIX = "translation_";

    public static final String INDEX_LOCK_TYPES_SYSTEM_PROPERTY = "jahia.jackrabbit.searchIndex.indexLockTypesProperty";

    private static final Set<String> IGNORED_LOCK_PROPERTIES_DEFAULT = new HashSet<>(
            Arrays.asList("{http://www.jcp.org/jcr/1.0}lockOwner", "{http://www.jcp.org/jcr/1.0}lockIsDeep",
                    "{http://www.jahia.org/jahia/1.0}locktoken"));

    private static final Set<String> IGNORED_LOCK_PROPERTIES_ALL;

    static {
        IGNORED_LOCK_PROPERTIES_ALL = new HashSet<>(IGNORED_LOCK_PROPERTIES_DEFAULT);
        IGNORED_LOCK_PROPERTIES_ALL.add("{http://www.jahia.org/jahia/1.0}lockTypes");
    }

    private static Set<String> ignoredProperties = Boolean.getBoolean(INDEX_LOCK_TYPES_SYSTEM_PROPERTY) ? IGNORED_LOCK_PROPERTIES_DEFAULT
            : IGNORED_LOCK_PROPERTIES_ALL;

    /**
     * Logger instance for this class
     */
    private static final Logger log = LoggerFactory.getLogger(JahiaSearchManager.class);

    /**
     * The shared item state manager instance for the workspace.
     */
    private final SharedItemStateManager itemMgr;

    public JahiaSearchManager(String workspace,
            RepositoryContext repositoryContext, QueryHandlerFactory qhf,
            SharedItemStateManager itemMgr, PersistenceManager pm,
            NodeId rootNodeId, SearchManager parentMgr, NodeId excludedNodeId)
            throws RepositoryException {
        super(workspace, repositoryContext, qhf, itemMgr, pm, rootNodeId, parentMgr,
                excludedNodeId);
        this.itemMgr = itemMgr;
   }

    //---------------< EventListener interface >--------------------------------

    @Override
    public void onEvent(EventIterator events) {
        log.debug("onEvent: indexing started");
        long time = System.currentTimeMillis();

        // nodes that need to be removed from the index.
        final Set<NodeId> removedNodes = new HashSet<>();
        // nodes that need to be added to the index.
        final Map<NodeId, EventImpl> addedNodes = new HashMap<>();
        // property events
        List<EventImpl> propEvents = new ArrayList<>();

        while (events.hasNext()) {
            EventImpl e = (EventImpl) events.nextEvent();
            if (!isExcluded(e)) {
                long type = e.getType();
                if (type == Event.NODE_ADDED) {
                    addedNodes.put(e.getChildId(), e);
                    if (e.isShareableChildNode()) {
                        // simply re-index shareable nodes
                        removedNodes.add(e.getChildId());
                    }
                } else if (type == Event.NODE_REMOVED) {
                    removedNodes.add(e.getChildId());
                    if (e.isShareableChildNode()) {
                        // check if there is a node remaining in the shared set
                        if (itemMgr.hasItemState(e.getChildId())) {
                            addedNodes.put(e.getChildId(), e);
                        }
                    }
                } else {
                    propEvents.add(e);
                }
            }
        }

        // Jahia: node-event based nodes that need to be removed from the index.
        final Set<NodeId> nodeEventRemovedNodes = new HashSet<>(removedNodes);

        // sort out property events
        for (EventImpl e : propEvents) {
            NodeId nodeId = e.getParentId();
            if (e.getType() == Event.PROPERTY_ADDED) {
                if (!addedNodes.containsKey(nodeId)) {
                    // only property added
                    // need to re-index
                    addedNodes.put(nodeId, e);
                    removedNodes.add(nodeId);
                } else {
                    // the node where this prop belongs to is also new
                }
            } else if (e.getType() == Event.PROPERTY_CHANGED) {
                // need to re-index, but set the event only if there is not already another one set for the node,
                // or the property is j:inherit
                if (!addedNodes.containsKey(nodeId)) {
                    addedNodes.put(nodeId, e);
                } else {
                    try {
                        if (e.getPath().endsWith("j:inherit")) {
                            addedNodes.put(nodeId, e);
                        }
                    } catch (RepositoryException ex) {
                        log.warn("Exception retrieving path", ex);
                    }
                }

                removedNodes.add(nodeId);
            } else {
                // property removed event is only generated when node still exists
                if (!addedNodes.containsKey(nodeId)) {
                    addedNodes.put(nodeId, e);
                }
                removedNodes.add(nodeId);
            }
        }
        try {
            addJahiaDependencies(removedNodes, addedNodes, propEvents, nodeEventRemovedNodes);

            boolean addedNodesPresent = addedNodes.size() > 0;
            if (addedNodesPresent || !removedNodes.isEmpty()) {
                Iterator<NodeState> addedStates = addedNodesPresent ? new NodeStateIterator(addedNodes) : Collections.<NodeState>emptyIterator();
                Iterator<NodeId> removedIds = removedNodes.iterator();

                getQueryHandler().updateNodes(removedIds, addedStates);
            }
        } catch (RepositoryException | IOException e) {
            log.error("Error indexing node.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("onEvent: indexing finished in {} ms.", System.currentTimeMillis() - time);
        }
    }

    private void addJahiaDependencies(final Set<NodeId> removedIds,
            final Map<NodeId, EventImpl> addedStates,
            List<EventImpl> propEvents, final Set<NodeId> nodeEventRemovedIds)
            throws RepositoryException, IOException {

        Set<NodeId> hierarchyNodeIds = getReMovedOrRenamedHierarchicalNodes(nodeEventRemovedIds);
        if (!hierarchyNodeIds.isEmpty()) {
            // if a node which is referenced with a hierarchical faceting property is moved/renamed, we need to re-index the nodes
            // referring to it
            final IndexReader reader = ((SearchIndex)getQueryHandler()).getIndexReader();
            final IndexSearcher searcher = new IndexSearcher(reader);
            try {
                int removeSubListStart = 0;
                List<NodeId> removeList = new ArrayList<>(hierarchyNodeIds);
                int removeSubListEnd = Math.min(removeList.size(), BooleanQuery.getMaxClauseCount());
                while (removeSubListStart < removeList.size()) {
                    long timer = System.currentTimeMillis();
                    BooleanQuery query = new BooleanQuery(true);
                    for (final NodeId nodeId : new ArrayList<NodeId>(removeList.subList(removeSubListStart, removeSubListEnd))) {
                        TermQuery termQuery = new TermQuery(new Term(JahiaNodeIndexer.FACET_HIERARCHY, nodeId.toString()));
                        query.add(new BooleanClause(termQuery, BooleanClause.Occur.SHOULD));
                    }
                    searcher.search(query, new AbstractHitCollector() {
                        public void collect(int doc, float score) {
                            try {
                                String uuid = reader.document(doc, FieldSelectors.UUID).get("_:UUID");
                                addIdToBeIndexed(new NodeId(uuid), removedIds, addedStates);
                            } catch (Exception e) {
                                log.warn("Documents referencing moved/renamed hierarchy facet nodes may not be updated", e);
                            }
                        }
                    });
                    if (log.isDebugEnabled()) {
                        log.debug("Facet hierarchy search in {} ms", new Object[]{(System.currentTimeMillis() - timer)});
                    }
                    removeSubListStart += BooleanQuery.getMaxClauseCount();
                    removeSubListEnd = Math.min(removeList.size(), removeSubListEnd + BooleanQuery.getMaxClauseCount());
                }
            } finally {
                searcher.close();
                Util.closeOrRelease(reader);
            }
        }

        // index also translation subnodes, unless only properties are changed, which are excluded from copying down to
        // translation nodes
        if (!addedStates.isEmpty() && !areAllPropertiesCopyExcluded(propEvents)) {
            for (final NodeId node : new HashSet<NodeId>(addedStates.keySet())) {
                if (itemMgr.hasItemState(node)) {
                    try {
                        for (ChildNodeEntry childNodeEntry : ((NodeState) itemMgr.getItemState(node)).getChildNodeEntries()) {
                            if (childNodeEntry.getName().getLocalName().startsWith(TRANSLATION_LOCALNODENAME_PREFIX)) {
                                try {
                                    addIdToBeIndexed(childNodeEntry.getId(), removedIds, addedStates);
                                } catch (ItemStateException e) {
                                    log.warn("Index of translation node may not be updated", e);
                                }
                            }
                        }
                    } catch (ItemStateException e) {
                        log.warn("Index of translation node may not be updated", e);
                    }
                }
            }
        }
    }

    private void addIdToBeIndexed(NodeId id, Set<NodeId> removedIds, Map<NodeId, EventImpl> addedStates) throws ItemStateException {
        if (!removedIds.contains(id)
                && !addedStates.containsKey(id)) {
            removedIds.add(id);
        }
        if (!addedStates.containsKey(id)) {
            addedStates.put(id, null);
        }
    }

    private boolean areAllPropertiesCopyExcluded(List<EventImpl> propEvents) {
        Set<Name> excludes = ((JahiaIndexingConfigurationImpl) ((SearchIndex)getQueryHandler()).getIndexingConfig()).getExcludesFromI18NCopy();
        for (final EventImpl event : propEvents) {
            try {
                if (event == null || !excludes.contains(event.getQPath().getLastElement().getName())) {
                   return false;
                }
            } catch (RepositoryException e) {
                // ignore
            }
        }
        return true;
    }

    private Set<NodeId> getReMovedOrRenamedHierarchicalNodes(final Set<NodeId> nodeEventRemovedIds) {
        if (nodeEventRemovedIds.isEmpty()) {
            return Collections.emptySet();
        }
        Set<NodeId> hierarchicalNodes = new HashSet<>();
        Set<Name> hierarchicalNodeTypes = ((JahiaIndexingConfigurationImpl) ((SearchIndex)getQueryHandler()).getIndexingConfig()).getHierarchicalNodetypes();
        for (final NodeId nodeId : nodeEventRemovedIds) {
            try {
                if (hierarchicalNodeTypes.contains(((NodeState) itemMgr.getItemState(nodeId)).getNodeTypeName())) {
                    hierarchicalNodes.add(nodeId);
                }
            } catch (ItemStateException e) {
                // can't obtain nodetype, so removed node will be checked for facet hierarchy references
                hierarchicalNodes.add(nodeId);
            }
        }
        return hierarchicalNodes;
    }

    public class NodeStateIterator implements Iterator<NodeState> {
        private final Iterator<NodeId> iter;
        private final Map<NodeId, EventImpl> addedNodes;

        public NodeStateIterator(Map<NodeId, EventImpl> addedNodes) {
            this.addedNodes = addedNodes;
            iter = addedNodes.keySet().iterator();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public Event getEvent(NodeId nodeId) {
            return addedNodes.get(nodeId);
        }

        public NodeState next() {
            NodeState item = null;
            NodeId id = iter.next();
            try {
                item = (NodeState) itemMgr.getItemState(id);
            } catch (ItemStateException ise) {
                // check whether this item state change originated from
                // an external event
                EventImpl e = addedNodes.get(id);
                if (e == null || !e.isExternal()) {
                    log.error("Unable to index node {}: does not exist", id);
                } else {
                    log.info("Node no longer available {}, skipped.", id);
                }
            }
            return item;
        }
    }

    @Override
    protected boolean isExcluded(EventImpl event) {
        if (super.isExcluded(event)) {
            return true;
        }

        int type = event.getType();
        if (type == Event.PROPERTY_ADDED || type == Event.PROPERTY_REMOVED || type == Event.PROPERTY_CHANGED) {
            try {
                String name = event.getQPath().getLastElement().getName().toString();
                return ignoredProperties.contains(name);
            } catch (RepositoryException e) {
                log.warn("Error getting JCR path from the event: " + event, e);
            }
        }

        return false;
    }
}

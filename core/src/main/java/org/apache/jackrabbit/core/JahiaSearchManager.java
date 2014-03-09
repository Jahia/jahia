package org.apache.jackrabbit.core;

import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JahiaSearchManager extends SearchManager {
    private static final String TRANSLATION_LOCALNODENAME_PREFIX = "translation_";
    
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

    public void onEvent(EventIterator events) {
        log.debug("onEvent: indexing started");
        long time = System.currentTimeMillis();

        // nodes that need to be removed from the index.
        final Set<NodeId> removedNodes = new HashSet<NodeId>();
        // nodes that need to be added to the index.
        final Map<NodeId, EventImpl> addedNodes = new HashMap<NodeId, EventImpl>();
        // property events
        List<EventImpl> propEvents = new ArrayList<EventImpl>();

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
        final Set<NodeId> nodeEventRemovedNodes = new HashSet<NodeId>(removedNodes);
        
        // sort out property events
        for (EventImpl e : propEvents) {
            NodeId nodeId = e.getParentId();
            if (e.getType() == Event.PROPERTY_ADDED) {
                if (addedNodes.put(nodeId, e) == null) {
                    // only property added
                    // need to re-index
                    removedNodes.add(nodeId);
                } else {
                    // the node where this prop belongs to is also new
                }
            } else if (e.getType() == Event.PROPERTY_CHANGED) {
                // need to re-index
                addedNodes.put(nodeId, e);
                removedNodes.add(nodeId);
            } else {
                // property removed event is only generated when node still exists
                addedNodes.put(nodeId, e);
                removedNodes.add(nodeId);
            }
        }
        try {
            addJahiaDependencies(removedNodes, addedNodes, propEvents, nodeEventRemovedNodes);
        
            if (removedNodes.size() > 0 || addedNodes.size() > 0) {
                Iterator<NodeState> addedStates = new Iterator<NodeState>() {
                    private final Iterator<NodeId> iter = addedNodes.keySet().iterator();

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }

                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    public NodeState next() {
                        NodeState item = null;
                        NodeId id = (NodeId) iter.next();
                        try {
                            item = (NodeState) itemMgr.getItemState(id);
                        } catch (ItemStateException ise) {
                            // check whether this item state change originated from
                            // an external event
                            EventImpl e = addedNodes.get(id);
                            if (e == null || !e.isExternal()) {
                                log.error("Unable to index node " + id + ": does not exist");
                            } else {
                                log.info("Node no longer available " + id + ", skipped.");
                            }
                        }
                        return item;
                    }
                };
                Iterator<NodeId> removedIds = removedNodes.iterator();            
            
                getQueryHandler().updateNodes(removedIds, addedStates);
            }
        } catch (RepositoryException e) {
            log.error("Error indexing node.", e);
        } catch (IOException e) {
            log.error("Error indexing node.", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("onEvent: indexing finished in "
                    + String.valueOf(System.currentTimeMillis() - time)
                    + " ms.");
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
            final Searcher searcher = new IndexSearcher(reader);
            try {
                int removeSubListStart = 0;
                List<NodeId> removeList = new ArrayList<NodeId>(hierarchyNodeIds);
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
        Set<NodeId> hierarchicalNodes = new HashSet<NodeId>();
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
}

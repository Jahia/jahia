package org.jahia.services.query.impl.jackrabbit;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.query.lucene.IndexFormatVersion;
import org.apache.jackrabbit.core.query.lucene.NamespaceMappings;
import org.apache.jackrabbit.core.query.lucene.SearchIndex;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.lucene.document.Document;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

public class JahiaSearchIndex extends SearchIndex {

    @Override
    protected Document createDocument(NodeState node,
            NamespaceMappings nsMappings, IndexFormatVersion indexFormatVersion)
            throws RepositoryException {
        JahiaNodeIndexer indexer = new JahiaNodeIndexer(node, getContext()
                .getItemStateManager(), nsMappings, getTextExtractor(),
                NodeTypeRegistry.getInstance(), getContext().getNamespaceRegistry());
        indexer.setSupportHighlighting(getSupportHighlighting());
        indexer.setIndexingConfiguration(getIndexingConfig());
        indexer.setIndexFormatVersion(indexFormatVersion);
        Document doc = indexer.createDoc();
        mergeAggregatedNodeIndexes(node, doc);
        return doc;
    }

}

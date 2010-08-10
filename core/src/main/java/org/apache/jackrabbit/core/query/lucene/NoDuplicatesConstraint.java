package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.query.lucene.constraint.EvaluationContext;
import org.apache.jackrabbit.spi.Name;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Filters out all duplicate results, including multiple translation of the same node
 */
public class NoDuplicatesConstraint implements Constraint {
    private Set<String> ids = new HashSet<String>();

    public boolean evaluate(ScoreNode[] row, Name[] selectorNames, EvaluationContext context) throws IOException {
        String id = "";
        for (ScoreNode sn : row) {
            sn.getDoc(context.getIndexReader());
            Document doc = context.getIndexReader().document(sn.getDoc(context.getIndexReader()));
            if (doc.getField(JahiaNodeIndexer.TRANSLATED_NODE_PARENT) != null) {
                id += doc.getField(FieldNames.PARENT).stringValue();
            } else {
                id += sn.getNodeId().toString();
            }
        }
        if (ids.contains(id)) {
            return false;
        }
        ids.add(id);
        return true;
    }
}

package org.apache.jackrabbit.core.query.lucene;

import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.query.lucene.constraint.ChildNodeConstraint;
import org.apache.jackrabbit.core.query.lucene.constraint.EvaluationContext;
import org.apache.jackrabbit.core.query.lucene.constraint.QueryConstraint;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.query.qom.ChildNodeImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;

import javax.jcr.RepositoryException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 2, 2010
 * Time: 6:47:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaChildNodeConstraint extends ChildNodeConstraint {

    public JahiaChildNodeConstraint(ChildNodeImpl constraint,
                               SelectorImpl selector) throws RepositoryException {
        super(constraint, selector);
    }

    @Override public boolean evaluate(ScoreNode[] row, Name[] selectorNames, EvaluationContext context)
            throws IOException {
        ScoreNode sn = row[getSelectorIndex(selectorNames)];
        if (sn == null) {
            return false;
        }

        sn.getDoc(context.getIndexReader());
        Document doc = context.getIndexReader().document(sn.getDoc(context.getIndexReader()));
        final String id = getBaseNodeId(context).toString();

        return id.equals(doc.get(FieldNames.PARENT)) ||
                id.equals(doc.get(JahiaNodeIndexer.TRANSLATED_NODE_PARENT));
    }
}

package org.apache.jackrabbit.core.query.lucene;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModelFactory;

import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeImpl;
import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
import org.apache.jackrabbit.core.query.PropertyTypeRegistry;
import org.apache.jackrabbit.core.query.lucene.constraint.AndConstraint;
import org.apache.jackrabbit.core.query.lucene.constraint.Constraint;
import org.apache.jackrabbit.core.query.lucene.constraint.ConstraintBuilder;
import org.apache.jackrabbit.spi.commons.nodetype.PropertyDefinitionImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.jackrabbit.spi.commons.query.qom.OrderingImpl;
import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;

public class JahiaQueryObjectModelImpl extends QueryObjectModelImpl {

    /**
     * The query object model tree.
     */
    private final QueryObjectModelTree qomTree;    
    
    public JahiaQueryObjectModelImpl(SessionImpl session, ItemManager itemMgr, SearchIndex index,
            PropertyTypeRegistry propReg, QueryObjectModelTree qomTree)
            throws InvalidQueryException {
        super(session, itemMgr, index, propReg, qomTree);
        this.qomTree = qomTree;
    }
    
    /**
     * Executes this query and returns a <code>{@link QueryResult}</code>.
     *
     * @param offset the offset in the total result set
     * @param limit  the maximum result size
     * @return a <code>QueryResult</code>
     * @throws RepositoryException if an error occurs
     */
    public QueryResult execute(long offset, long limit)
            throws RepositoryException {

        JahiaLuceneQueryFactoryImpl factory = new JahiaLuceneQueryFactoryImpl(session,
                index.getSortComparatorSource(),
                index.getContext().getHierarchyManager(),
                index.getNamespaceMappings(), index.getTextAnalyzer(),
                index.getSynonymProvider(), index.getIndexFormatVersion());

        MultiColumnQuery query = factory.create(qomTree);

        Constraint nodup = new NoDuplicatesConstraint();
        if (qomTree.getConstraint() != null) {
            Constraint c = JahiaConstraintBuilder.create(qomTree.getConstraint(),
                    getBindVariableValues(), qomTree.getSource().getSelectors(),
                    factory, session.getValueFactory());
            query = new FilterMultiColumnQuery(query, new AndConstraint(nodup, c));
        } else {
            query = new FilterMultiColumnQuery(query, nodup);
        }


        List<ColumnImpl> columns = new ArrayList<ColumnImpl>();
        // expand columns without name
        for (ColumnImpl column : qomTree.getColumns()) {
            if (column.getColumnName() == null) {
                QueryObjectModelFactory qomFactory = getQOMFactory();
                NodeTypeManagerImpl ntMgr = session.getNodeTypeManager();
                SelectorImpl selector = qomTree.getSelector(column.getSelectorQName());
                NodeTypeImpl nt = ntMgr.getNodeType(selector.getNodeTypeQName());
                for (PropertyDefinition pd : nt.getPropertyDefinitions()) {
                    PropertyDefinitionImpl propDef = (PropertyDefinitionImpl) pd;
                    if (!propDef.unwrap().definesResidual() && !propDef.isMultiple()) {
                        String sn = selector.getSelectorName();
                        String pn = propDef.getName();
                        columns.add((ColumnImpl) qomFactory.column(sn, pn, sn + "." + pn));
                    }
                }
            } else {
                columns.add(column);
            }
        }
        OrderingImpl[] orderings = qomTree.getOrderings();
        return new JahiaMultiColumnQueryResult(index, itemMgr,
                session, session.getAccessManager(),
                // TODO: spell suggestion missing
                this, query, null, columns.toArray(new ColumnImpl[columns.size()]),
                orderings, orderings.length == 0 && getRespectDocumentOrder(),
                offset, limit);
    }

    public QueryObjectModelTree getQomTree() {
        return qomTree;
    }    

}

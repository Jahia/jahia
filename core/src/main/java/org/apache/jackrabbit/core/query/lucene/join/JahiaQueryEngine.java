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

package org.apache.jackrabbit.core.query.lucene.join;

import java.io.IOException;
import java.util.*;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.And;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Comparison;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.EquiJoinCondition;
import javax.jcr.query.qom.FullTextSearchScore;
import javax.jcr.query.qom.Join;
import javax.jcr.query.qom.LowerCase;
import javax.jcr.query.qom.Not;
import javax.jcr.query.qom.Or;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.PropertyValue;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.jcr.query.qom.UpperCase;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.commons.query.qom.OperandEvaluator;
import org.apache.jackrabbit.core.query.FacetedQueryResult;
import org.apache.jackrabbit.core.query.JahiaSimpleQueryResult;
import org.apache.jackrabbit.core.query.lucene.FacetRow;
import org.apache.jackrabbit.core.query.lucene.JahiaLuceneQueryFactoryImpl;
import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
import org.apache.jackrabbit.core.query.lucene.sort.DynamicOperandFieldComparatorSource;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Override QueryEngine :
 *  - add facet handling in execute()
 */
public class JahiaQueryEngine extends QueryEngine {
    private static final Logger log = LoggerFactory.getLogger(QueryEngine.class);

    protected final OperandEvaluator evaluator;

    public static boolean nativeSort = Boolean.valueOf(System
            .getProperty(NATIVE_SORT_SYSTEM_PROPERTY, "false"));


    public JahiaQueryEngine(Session session, LuceneQueryFactory lqf, Map<String, Value> variables)
            throws RepositoryException {
        super(session, lqf, variables);

        Locale locale = null;
        if (lqf instanceof JahiaLuceneQueryFactoryImpl) {
            locale = ((JahiaLuceneQueryFactoryImpl)lqf).getLocale();
        }
        this.evaluator = new JahiaOperandEvaluator(valueFactory, variables, locale);
    }

    /**
     * Override QueryEngine.execute()
     */
    @Override
    protected QueryResult execute(
            Column[] columns, Selector selector, Constraint constraint,
            Ordering[] orderings, long offset, long limit, int printIndentation)
            throws RepositoryException {
        Map<String, NodeType> selectorMap = getSelectorNames(selector);
        String[] selectorNames = selectorMap.keySet().toArray(
                new String[selectorMap.size()]);

        Map<String, PropertyValue> columnMap = getColumnMap(columns,
                selectorMap);
        String[] columnNames = columnMap.keySet().toArray(
                new String[columnMap.size()]);

        Sort sort = new Sort();
        if (nativeSort) {
            sort = new Sort(createSortFields(orderings, session));
        }

        // if true it means that the LuceneQueryFactory should just let the
        // QueryEngine take care of sorting and applying offset and limit
        // constraints
        boolean externalSort = !nativeSort && (orderings != null && orderings.length > 0);

        List<Row> rowsList = null;
        try {
            rowsList = lqf.execute(columnMap, selector, constraint, sort, externalSort, offset, limit);
        } catch (IOException e) {
            throw new RepositoryException("Failed to access the query index", e);
        }
        // Added by jahia
        QueryResult result;
        if (rowsList.size() > 0 && rowsList.get(0) instanceof FacetRow) {
            FacetRow facets = (FacetRow) rowsList.remove(0);
            RowIterator rows = new RowIteratorAdapter(rowsList);
            return new FacetedQueryResult(columnNames, selectorNames, rows, facets);
        } else {
            RowIterator rows = new RowIteratorAdapter(rowsList);
            result = new JahiaSimpleQueryResult(columnNames, selectorNames, rows);
            if (nativeSort) {
                return result;
            }
            long timeSort = System.currentTimeMillis();
            QueryResult r = sort(result, orderings, evaluator, offset, limit);
            if (log.isDebugEnabled()) {
                log.debug("{}SQL2 SORT took {} ms.", genString(printIndentation),
                    System.currentTimeMillis() - timeSort);
            }
            if (r != result) {
                return new JahiaSimpleQueryResult(columnNames, selectorNames, r.getRows(), limit > 0 ? result.getRows().getSize() : 0);
            }
            return result;
        }
    }

    @Override
    public SortField[] createSortFields(Ordering[] orderings, Session session)
            throws RepositoryException {

        if (orderings == null || orderings.length == 0) {
            return new SortField[] { SortField.FIELD_SCORE };
        }
        // orderings[] -> (property, ordering)
        Map<String, Ordering> orderByProperties = new HashMap<String, Ordering>();
        for (Ordering o : orderings) {
            final String p = o.toString();
            if (!orderByProperties.containsKey(p)) {
                orderByProperties.put(p, o);
            }
        }
        final DynamicOperandFieldComparatorSource dofcs = new DynamicOperandFieldComparatorSource(
                session, evaluator, orderByProperties);

        List<SortField> sortFields = new ArrayList<SortField>();

        // as it turn out, orderByProperties.keySet() doesn't keep the original
        // insertion order
        for (Ordering o : orderings) {
            final String p = o.toString();
            // order on jcr:score does not use the natural order as
            // implemented in lucene. score ascending in lucene means that
            // higher scores are first. JCR specs that lower score values
            // are first.
            boolean isAsc = QueryObjectModelConstants.JCR_ORDER_ASCENDING
                    .equals(o.getOrder());
            if (o.getOperand() instanceof FullTextSearchScore || JcrConstants.JCR_SCORE.equals(p)) {
                sortFields.add(new SortField(null, SortField.SCORE, isAsc));
            } else {
                // TODO use native sort if available
                sortFields.add(new SortField(p, dofcs, !isAsc));
            }
        }
        return sortFields.toArray(new SortField[sortFields.size()]);
    }

    private static String genString(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }


    @Override
    protected QueryResult execute(Column[] columns, Join join, Constraint constraint, Ordering[] orderings, long offset, long limit, int printIndentation) throws RepositoryException {
        QueryResult result = isEquiJoinWithUuid(join, constraint) ? executeEquiJoin(
                columns, join, constraint, orderings, offset, limit) : super
                .execute(columns, join, constraint, orderings, offset, limit, printIndentation);
        if (!(result instanceof JahiaSimpleQueryResult)) {
            result = new JahiaSimpleQueryResult(result.getColumnNames(), result.getSelectorNames(),
                    result.getRows());
        }
        return result;
    }

    protected boolean isEquiJoinWithUuid(Join join, Constraint constraint) {
        boolean result = false;
        if (join.getJoinCondition() instanceof EquiJoinCondition) {
            EquiJoinCondition condition = (EquiJoinCondition)join.getJoinCondition();
            if (condition.getProperty2Name().equals(Constants.JCR_UUID)) {
                result = true;
            }
        }
        return result;
    }

    protected boolean hasLanguageConstraint(Constraint constraint) {
        boolean languageConstraint = false;
        if (constraint instanceof And) {
            And and = (And) constraint;
            languageConstraint = hasLanguageConstraint(and.getConstraint1());
            languageConstraint = languageConstraint || hasLanguageConstraint(and.getConstraint2());
        } else if (constraint instanceof Or) {
            Or or = (Or) constraint;
            languageConstraint = hasLanguageConstraint(or.getConstraint1());
            languageConstraint = languageConstraint || hasLanguageConstraint(or.getConstraint2());
        } else if (constraint instanceof Not) {
            Not not = (Not) constraint;
            languageConstraint = hasLanguageConstraint(not.getConstraint());
        } else if (constraint instanceof Comparison) {
            Comparison c = (Comparison) constraint;
            languageConstraint = hasLanguageConstraint(c.getOperand1());
        }
        return languageConstraint;
    }

    private boolean hasLanguageConstraint(DynamicOperand operand) {
        boolean languageConstraint = false;
        if (operand instanceof LowerCase) {
            LowerCase lower = (LowerCase) operand;
            return hasLanguageConstraint(lower.getOperand());
        } else if (operand instanceof PropertyValue) {
            PropertyValue value = (PropertyValue) operand;
            return value.getPropertyName().equals(Constants.JCR_LANGUAGE);
        } else if (operand instanceof UpperCase) {
            UpperCase upper = (UpperCase) operand;
            return hasLanguageConstraint(upper.getOperand());
        }
        return languageConstraint;
    }

    protected QueryResult executeEquiJoin(
            Column[] columns, Join join, Constraint constraint,
            Ordering[] orderings, long offset, long limit)
            throws RepositoryException {
        JahiaEquiJoinMerger merger = new JahiaEquiJoinMerger(
                join, getColumnMap(columns, getSelectorNames(join)), evaluator, qomFactory,
                (EquiJoinCondition) join.getJoinCondition());
        ConstraintSplitter splitter = new ConstraintSplitter(
                constraint, qomFactory,
                merger.getLeftSelectors(), merger.getRightSelectors(), join);

        Source left = join.getLeft();
        Constraint leftConstraint = splitter.getConstraintSplitInfo().getLeftConstraint();
        QueryResult leftResult =
                execute(null, left, leftConstraint, null, 0, -1);
        List<Row> leftRows = new ArrayList<Row>();
        for (Row row : JcrUtils.getRows(leftResult)) {
            leftRows.add(row);
        }

        if (hasLanguageConstraint(splitter.getConstraintSplitInfo().getRightConstraint())) {
            merger.setIncludeTranslationNode(true);
        }

        RowIterator rightRows;
        Source right = join.getRight();
        List<Constraint> rightConstraints =
                merger.getRightJoinConstraints(leftRows);
        Comparator<Row> rightCo = new RowPathComparator(
                merger.getRightSelectors());

        if (rightConstraints.size() < 500) {
            Constraint rightConstraint = Constraints.and(
                    qomFactory,
                    Constraints.or(qomFactory, rightConstraints),
                    splitter.getConstraintSplitInfo().getRightConstraint());
            rightRows =
                    execute(null, right, rightConstraint, null, 0, -1).getRows();
        } else {
            List<Row> list = new ArrayList<Row>();
            for (int i = 0; i < rightConstraints.size(); i += 500) {
                Constraint rightConstraint = Constraints.and(
                        qomFactory,
                        Constraints.or(qomFactory, rightConstraints.subList(
                                i, Math.min(i + 500, rightConstraints.size()))),
                        splitter.getConstraintSplitInfo().getRightConstraint());
                QueryResult rigthResult =
                        execute(null, right, rightConstraint, null, 0, -1);
                for (Row row : JcrUtils.getRows(rigthResult)) {
                    list.add(row);
                }
            }
            rightRows = new RowIteratorAdapter(list);
        }

        QueryResult result =
                merger.merge(new RowIteratorAdapter(leftRows), rightRows, null, rightCo);
        return sort(result, orderings, evaluator, offset, limit);
    }

    @Override
    public QueryResult execute(Column[] columns, Source source, Constraint constraint, Ordering[] orderings, long offset, long limit) throws RepositoryException {
        QueryResult result = super.execute(columns, source, constraint, orderings, offset, limit);
        if (!(result instanceof JahiaSimpleQueryResult)) {
            result = new JahiaSimpleQueryResult(result.getColumnNames(), result.getSelectorNames(),
                    result.getRows());
        }
        return result;
    }

    @Override
    protected Map<String, PropertyValue> getColumnMap(String selector, NodeType type)
            throws RepositoryException {
        return super.getColumnMap(selector, NodeTypeRegistry.getInstance().getNodeType(type.getName()));
    }

    @Override
    protected Map<String, PropertyValue> getColumnMap(Column[] columns,
                                                      Map<String, NodeType> selectors) throws RepositoryException {
        Map<String, PropertyValue> map =
                new LinkedHashMap<String, PropertyValue>();
        if (columns != null && columns.length > 0) {
            for (int i = 0; i < columns.length; i++) {
                String name = columns[i].getColumnName();
                if (name != null) {
                    map.put(name, qomFactory.propertyValue(
                            columns[i].getSelectorName(),
                            columns[i].getPropertyName()));
                } else if (!StringUtils.isEmpty(columns[i].getPropertyName())) {
                    map.put(columns[i].getSelectorName() + "." + columns[i].getPropertyName(),
                            qomFactory.propertyValue(columns[i].getSelectorName(),
                                    columns[i].getPropertyName()));
                } else {
                    String selector = columns[i].getSelectorName();
                    map.putAll(getColumnMap(selector, selectors.get(selector)));
                }
            }
        } else {
            for (Map.Entry<String, NodeType> selector : selectors.entrySet()) {
                map.putAll(getColumnMap(
                        selector.getKey(), selector.getValue()));
            }
        }
        return map;
    }


}

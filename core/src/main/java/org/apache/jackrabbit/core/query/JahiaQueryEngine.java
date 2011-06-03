/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.apache.jackrabbit.core.query;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.query.lucene.FacetRow;
import org.apache.jackrabbit.core.query.lucene.LuceneQueryFactory;
import org.apache.jackrabbit.core.query.lucene.join.QueryEngine;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Override QueryEngine :
 *  - add facet handling in execute()
 */
public class JahiaQueryEngine extends QueryEngine {


    public JahiaQueryEngine(Session session, LuceneQueryFactory lqf, Map<String, Value> variables)
            throws RepositoryException {
        super(session, lqf, variables);
    }

    /**
     * Override QueryEngine.execute()
     */
    protected QueryResult execute(
            Column[] columns, Selector selector, Constraint constraint,
            Ordering[] orderings, long offset, long limit)
            throws RepositoryException {
        Map<String, NodeType> selectorMap = getSelectorNames(selector);
        String[] selectorNames =
            selectorMap.keySet().toArray(new String[selectorMap.size()]);

        Map<String, PropertyValue> columnMap =
            getColumnMap(columns, selectorMap);
        String[] columnNames =
            columnMap.keySet().toArray(new String[columnMap.size()]);

        try {
            final List<Row> rowsList = lqf.execute(columnMap, selector, constraint);
            // Added by jahia
            QueryResult result;
            if (rowsList.size() > 0 && rowsList.get(0) instanceof FacetRow) {
                FacetRow facets = (FacetRow) rowsList.remove(0);
                RowIterator rows = new RowIteratorAdapter(rowsList);
                result = new FacetedQueryResult(columnNames, selectorNames, rows, facets);
                QueryResult r = sort(result, orderings, offset, limit);
                if (r != result) {
                    result = new FacetedQueryResult(columnNames, selectorNames, r.getRows(), facets);
                }
            } else {
                RowIterator rows = new RowIteratorAdapter(rowsList);
                result = new JahiaSimpleQueryResult(columnNames, selectorNames, rows);
                QueryResult r = sort(result, orderings, offset, limit);
                if (r != result) {
                    result = new JahiaSimpleQueryResult(columnNames, selectorNames, r.getRows());
                }
            }
            return result;
            // End
        } catch (IOException e) {
            throw new RepositoryException(
                    "Failed to access the query index", e);
        }
    }

    @Override
    protected QueryResult execute(Column[] columns, Join join, Constraint constraint, Ordering[] orderings, long offset, long limit) throws RepositoryException {
        QueryResult result = super.execute(columns, join, constraint, orderings, offset, limit);
        if (!(result instanceof JahiaSimpleQueryResult)) {
            result = new JahiaSimpleQueryResult(result.getColumnNames(), result.getSelectorNames(),
                    result.getRows());
        }
        return result;
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

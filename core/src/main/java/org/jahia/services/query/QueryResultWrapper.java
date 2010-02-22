/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.query;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.value.StringValue;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;

/**
 * Implementation of the JCR {@link QueryResult}.
 * 
 * @author Thomas Draier
 */
class QueryResultWrapper implements QueryResult {
    
    /**
     * Invocation handler to decorate the {@link NodeIterator} instance of the
     * query result in order to wrap each {@link Node} into
     * {@link JCRNodeWrapper}.
     * 
     * @author Sergiy Shyrkov
     */
    private class NodeWrapperInvocationHandler implements InvocationHandler {
        private final NodeIterator underlying;

        NodeWrapperInvocationHandler(NodeIterator underlying) {
            super();
            this.underlying = underlying;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws IllegalArgumentException,
                IllegalAccessException, InvocationTargetException, RepositoryException {
            Object result = method.invoke(underlying, args);
            if (!(result instanceof JCRNodeWrapper)
                    && ("nextNode".equals(method.getName()) || "next".equals(method.getName()))) {
                Node node = (Node) result;
                if (session.getLocale() != null && node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                    try {
                        node = node.getParent();
                    } catch (ItemNotFoundException e) {
                        // keep same node
                    }
                }
                result = provider.getNodeWrapper(node, session);
            }
            return result;
        }
    }
    
    private JCRStoreProvider provider;
    private QueryResult result;
    private JCRSessionWrapper session;
    
    /**
     * A facade for a single query result row.
     * 
     * @author Sergiy Shyrkov
     */
    public class RowDecorator implements Row {
        private Node node;
        private Map<String, Node> nodesBySelector = new HashMap<String, Node>(1);
        private Row row;
        
        /**
         * Initializes an instance of this class.
         * @param decoratedRow the row to be decorated
         */
        RowDecorator(Row decoratedRow) {
            super();
            row = decoratedRow;
        }

        public Node getNode() throws RepositoryException {
            if (node == null) {
                node = wrap(row.getNode());
            }
            return node;
        }

        public Node getNode(String selectorName) throws RepositoryException {
            Node targetNode = nodesBySelector.get(selectorName);
            if (targetNode == null) {
                targetNode = wrap(row.getNode(selectorName));
                nodesBySelector.put(selectorName, targetNode);
            }
            return targetNode;
        }

        private String getDerivedPath(String originalPath) throws RepositoryException {
            originalPath = originalPath.replaceFirst(getProvider().getRelativeRoot(),"");
            String mountPoint = getProvider().getMountPoint();
            return mountPoint.equals("/") ? originalPath : mountPoint + originalPath;
        }

        public String getPath() throws RepositoryException {
            return getDerivedPath(row.getPath());
        }

        public String getPath(String selectorName) throws RepositoryException {
            return getDerivedPath(row.getPath(selectorName));
        }

        public double getScore() throws RepositoryException {
            return row.getScore();
        }

        public double getScore(String selectorName) throws RepositoryException {
            return row.getScore(selectorName);
        }

        public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
            if (columnName.equals(JcrConstants.JCR_PATH)) {
                return new StringValue(getPath());
            }
            Value result = row.getValue(columnName);
            if (result == null && session.getLocale() != null) {
                JCRPropertyWrapper property = !row.getNode().hasProperty(columnName) ? ((JCRNodeWrapper)getNode()).getProperty(columnName) : null;
                if (property != null && !property.isMultiple()) {
                    result = property.getValue();
                }
            }
            
            return result;
        }

        public Value[] getValues() throws RepositoryException {
            return row.getValues();
        }
        
        private Node wrap(Node node) throws RepositoryException {
            return getProvider().getNodeWrapper(node, getSession());
        }
        
        public String getSpellcheck() throws ItemNotFoundException, RepositoryException {
            Value suggestion = row.getValue("rep:spellcheck()");
            return suggestion != null ? suggestion.getString() : null; 
        }
    };


    public QueryResultWrapper(QueryResult result, JCRStoreProvider provider, JCRSessionWrapper session) {
        this.result = result;
        this.provider = provider;
        this.session = session;
    }

    public JCRStoreProvider getProvider() {
        return provider;
    }

    public String[] getColumnNames() throws RepositoryException {
        return result.getColumnNames();
    }

    public RowIterator getRows() throws RepositoryException {
        final RowIterator rowIterator = result.getRows();
        return new RowIteratorAdapter(new RangeIteratorAdapter(rowIterator, rowIterator.getSize())) {
            @Override
            public Row next() {
                return new RowDecorator((Row) super.next());
            }
        };
    }

    public NodeIterator getNodes() throws RepositoryException {
        final NodeIterator ni = result.getNodes();
        return (NodeIterator) Proxy.newProxyInstance(ni.getClass().getClassLoader(),
                new Class[] { NodeIterator.class }, new NodeWrapperInvocationHandler(ni));
    }

    public String[] getSelectorNames() throws RepositoryException {
        return result.getSelectorNames();
    }
    
    JCRSessionWrapper getSession() {
        return session;
    }
}
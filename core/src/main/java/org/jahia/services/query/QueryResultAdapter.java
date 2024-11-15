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
package org.jahia.services.query;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RangeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.query.FacetedQueryResult;
import org.apache.jackrabbit.core.query.JahiaSimpleQueryResult;
import org.apache.jackrabbit.value.StringValue;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transform a QueryResult
 */
public class QueryResultAdapter implements QueryResult {

    private JCRStoreProvider provider;
    private QueryResult result;
    private JCRSessionWrapper session;
    private String sessionLanguage;

    /**
     * A facade for a single query result row.
     *
     * @author Sergiy Shyrkov
     */
    public class RowDecorator implements Row {
        private JCRNodeWrapper node;
        private Map<String, JCRNodeWrapper> nodesBySelector;
        private Row row;

        /**
         * Initializes an instance of this class.
         *
         * @param decoratedRow the row to be decorated
         */
        RowDecorator(Row decoratedRow) {
            super();
            row = decoratedRow;
        }

        /**
         * get inner row decorated
         * @return the original row
         */
        Row getRawRow() {
            return row;
        }

        public JCRNodeWrapper getNode() throws RepositoryException {
            if (node == null) {
                node = wrap(row.getNode());
            }
            return node;
        }

        public JCRNodeWrapper getNode(String selectorName) throws RepositoryException {
            return getNodes().get(selectorName);
        }

        @SuppressWarnings("unchecked")
        public Map<String, JCRNodeWrapper> getNodes() throws RepositoryException {
            if (nodesBySelector == null) {
                nodesBySelector = LazyMap.decorate(new HashMap<String, Node>(), new Transformer() {
                    public Object transform(Object selector) {
                        try {
                            return wrap(row.getNode((String) selector));
                        } catch (RepositoryException e) {
                            throw new UnsupportedOperationException(e);
                        }
                    }
                });
            }

            return nodesBySelector;
        }

        private String getDerivedPath(String originalPath) throws RepositoryException {
            String path = originalPath.replaceFirst(getProvider().getRelativeRoot(), "");
            String mountPoint = getProvider().getMountPoint();
            return mountPoint.equals("/") ? path : mountPoint + path;
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

        public Value getValue(String columnName) throws RepositoryException {
            if (columnName.equals(JcrConstants.JCR_PATH)) {
                return new StringValue(getPath());
            }
            Value valueResult = row.getValue(columnName);
            if (valueResult == null && session.getLocale() != null) {
                JCRPropertyWrapper property = null;
                if (!columnName.startsWith("rep:spellcheck(")
                        && !columnName.startsWith("rep:excerpt(")
                        && !(row.getNode() != null && row.getNode().hasProperty(columnName))) {
                    JCRNodeWrapper valueNode = getNode();
                    try {
                        property = valueNode.getProperty(columnName);
                    } catch (RepositoryException e) {
                        // no match
                    }
                }
                if (property != null && !property.isMultiple()) {
                    valueResult = property.getValue();
                }
            }

            return valueResult;
        }

        public Value[] getValues() throws RepositoryException {
            return row.getValues();
        }

        private JCRNodeWrapper wrap(Node node) throws RepositoryException {
            if (node != null && session.getLocale() != null && node.hasProperty(Constants.JCR_LANGUAGE)) {
                String language = node.getProperty(Constants.JCR_LANGUAGE).getString();
                if (!getSessionLanguage().equals(language)) {
                    session = ServicesRegistry
                            .getInstance()
                            .getJCRStoreService()
                            .getSessionFactory()
                            .getCurrentUserSession(session.getWorkspace().getName(),
                                    LanguageCodeConverters.languageCodeToLocale(language),
                                    session.getFallbackLocale());
                }
            }
            return node != null && !(node instanceof JCRNodeWrapper) ? getProvider()
                    .getNodeWrapper(node, session) : (JCRNodeWrapper) node;
        }

        public String getSpellcheck() throws RepositoryException {
            Value suggestion = row.getValue("rep:spellcheck()");
            return suggestion != null ? suggestion.getString() : null;
        }
    }

    public QueryResultAdapter(QueryResult result, JCRStoreProvider provider,
                              JCRSessionWrapper session) {
        this.result = result;
        this.provider = provider;
        this.session = session;
        this.sessionLanguage = session.getLocale() != null ? session.getLocale().toString() : null;
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

    public JCRNodeIteratorWrapper getNodes() throws RepositoryException {
        final NodeIterator ni;
        if (result.getSelectorNames().length <= 1) {
            ni = result.getNodes();

        } else {
            ni = new NodeIteratorAdapter(result.getRows()) {
                @Override
                public Object next() {
                    Row row = (Row) super.next();
                    try {
                        return row.getNode(result.getSelectorNames()[0]);
                    } catch (RepositoryException e) {
                        throw new UnsupportedOperationException("Unable to access the node in " + row, e);
                    }
                }
            };
        }
        return new NodeIteratorWrapper(ni, session, provider);
    }

    public List<FacetField> getFacetFields() {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getFacetFields()
                : null;
    }

    public List<FacetField> getFacetDates() {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getFacetDates()
                : null;
    }

    public List<RangeFacet> getRangeFacets() {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getRangeFacets()
                : null;
    }

    /**
     * Get the facet field for a name
     *
     * @param name the name of the facet field to get
     * @return the FacetField by name or null if it does not exist
     */
    public FacetField getFacetField(String name) {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getFacetField(name)
                : null;
    }

    public FacetField getFacetDate(String name) {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getFacetDate(name)
                : null;
    }

    public RangeFacet getRangeFacet(String name) {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getRangeFacet(name)
                : null;
    }

    public Map<String, Long> getFacetQuery() {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getFacetQuery()
                : null;
    }

    public List<FacetField> getLimitingFacets() {
        return result instanceof FacetedQueryResult ? ((FacetedQueryResult) result).getLimitingFacets()
                : null;
    }

    public String[] getSelectorNames() throws RepositoryException {
        return result.getSelectorNames();
    }


    JCRSessionWrapper getSession() {
        return session;
    }

    private String getSessionLanguage() {
        return sessionLanguage;
    }

    public long getApproxCount() {
        return result instanceof JahiaSimpleQueryResult ? ((JahiaSimpleQueryResult) result).getApproxCount()
                : 0;
    }

}

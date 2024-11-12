/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

import com.google.common.collect.Sets;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.jackrabbit.commons.predicate.Predicate;
import org.apache.jackrabbit.commons.query.qom.OperandEvaluator;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.query.lucene.join.SelectorRow;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.solr.schema.SchemaField;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.search.facets.JahiaQueryParser;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.jahia.utils.security.AccessManagerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Row;
import javax.jcr.query.qom.*;
import javax.jcr.security.Privilege;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.apache.jackrabbit.core.query.lucene.FieldNames.UUID;
import static org.apache.lucene.search.BooleanClause.Occur.MUST;

/**
 * Override LuceneQueryFactory
 *
 * - optimize descendantNode constraint (use index)
 * - optimize childNode constraint (use index)
 * - handles rep:facet when executing (todo : might handle that in a constraint, as rep:filter ?)
 * - handles rep:filter in fulltext constraint
 */
public class JahiaLuceneQueryFactoryImpl extends LuceneQueryFactory {
    private static Logger logger = LoggerFactory.getLogger(JahiaLuceneQueryFactoryImpl.class);

    private Locale locale;
    private String queryLanguage;

    public static final FieldSelector ONLY_MAIN_NODE_UUID = fieldName -> {
        if (JahiaNodeIndexer.TRANSLATED_NODE_PARENT == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (FieldNames.PARENT == fieldName) {
            return FieldSelectorResult.LOAD;
        } else {
            return FieldSelectorResult.NO_LOAD;
        }
    };

    public static final FieldSelector OPTIMIZATION_FIELDS = fieldName -> {
        if (JahiaNodeIndexer.TRANSLATED_NODE_PARENT == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (FieldNames.PARENT == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (JahiaNodeIndexer.ACL_UUID == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (JahiaNodeIndexer.CHECK_VISIBILITY == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (JahiaNodeIndexer.PUBLISHED == fieldName) {
            return FieldSelectorResult.LOAD;
        } else if (JahiaNodeIndexer.INVALID_LANGUAGES == fieldName) {
            return FieldSelectorResult.LOAD;
        } else {
            return FieldSelectorResult.NO_LOAD;
        }
    };

    static boolean checkIndexedAcl(Map<String, Boolean> checkedAcls, String aclUuid, SessionImpl session) throws RepositoryException {
        if (aclUuid == null) {
            return true;
        }
        boolean canRead = false;

        String[] acls = Patterns.SPACE.split(aclUuid);
        ArrayUtils.reverse(acls);

        HashSet<String> readPermissions = null;
        for (String acl : acls) {
            if (acl.contains("/")) {
                // ACL indexed contains a single user ACE, get the username
                String singleUser = StringUtils.substringAfter(acl, "/");
                acl =  StringUtils.substringBefore(acl, "/");
                if (singleUser.indexOf('/') != -1) {
                    // Granted roles are specified in the indexed entry
                    String roles = StringUtils.substringBeforeLast(singleUser, "/");
                    singleUser = StringUtils.substringAfterLast(singleUser, "/");
                    if (!singleUser.equals(session.getUserID())) {
                        // If user does not match, skip this ACL
                        continue;
                    } else {
                        // If user matches, check if one the roles gives the read permission
                        for (String role : StringUtils.split(roles, '/')) {
                            if (null == readPermissions) {
                                readPermissions = Sets.newHashSet(AccessManagerUtils.getPrivilegeName(Privilege.JCR_READ, session.getWorkspace().getName()));
                            }
                            if (((JahiaAccessManager)session.getAccessControlManager()).matchPermission(readPermissions,role)) {
                                // User and role matches, read is granted
                                return true;
                            }
                        }
                    }
                } else {
                    if (!singleUser.equals(session.getUserID())) {
                        // If user does not match, skip this ACL
                        continue;
                    }
                    // Otherwise, do normal ACL check.
                }
            }
            // Verify first if this acl has already been checked
            Boolean aclChecked = checkedAcls.get(acl);
            if (aclChecked == null) {
                try {
                    canRead = session.getAccessManager().canRead(null, new NodeId(acl));
                    checkedAcls.put(acl, canRead);
                } catch (RepositoryException e) {
                    // ignored
                }
            } else {
                canRead = aclChecked;
            }
            break;
        }
        return canRead;
    }

    public JahiaLuceneQueryFactoryImpl(SessionImpl session, SearchIndex index, Map<String, Value> bindVariables) throws RepositoryException {
        super(session, index, bindVariables);
    }

    /**
     * Override LuceneQueryFactory.execute()
     */
    @Override
    public List<Row> execute(Map<String, PropertyValue> columns, Selector selector, Constraint constraint, Sort sort, boolean externalSort,
            long offsetIn, long limitIn) throws RepositoryException, IOException {

        final IndexReader reader = index.getIndexReader(true);
        final int offset = offsetIn < 0 ? 0 : (int) offsetIn;
        final int limit = limitIn < 0 ? Integer.MAX_VALUE : (int) limitIn;

        QueryHits hits = null;
        try {
            JackrabbitIndexSearcher searcher = new JackrabbitIndexSearcher(session, reader, index.getContext().getItemStateManager());
            searcher.setSimilarity(index.getSimilarity());

            BooleanQuery query = new BooleanQuery();
            query.add(create(selector), MUST);

            QueryPair qp = new QueryPair(query);

            Predicate filter = Predicate.TRUE;
            if (constraint != null) {
                String name = selector.getSelectorName();
                NodeType type = ntManager.getNodeType(selector.getNodeTypeName());
                filter = mapConstraintToQueryAndFilter(qp,
                        constraint, Collections.singletonMap(name, type),
                        searcher, reader);
            }

            // Added by jahia
            final Set<String> foundIds = new HashSet<>();
            final int hasFacets = FacetHandler.hasFacetFunctions(columns, session);
            final CountHandler.CountType countType = CountHandler.hasCountFunction(columns, session);
            final boolean isCount = countType != null;
            final BitSet bitset = (hasFacets & FacetHandler.FACET_COLUMNS) == 0 ? null : new BitSet();
            // End

            List<Row> rowList = externalSort ? new LinkedList<>() : null;
            Map<String, Row> rows = externalSort ? null : new LinkedHashMap<>();

            hits = searcher.evaluate(qp.mainQuery, sort, (long) offset + limit);
            int currentNode = 0;
            int addedNodes = 0;
            int resultCount = 0;
            int hitsSize = 0;

            ScoreNode node = hits.nextScoreNode();
            Map<String, Boolean> checkedAcls = new HashMap<>();

            while (node != null) {
                if (isCount && countType.isApproxCount()) {
                    hitsSize++;
                    if (hitsSize > countType.getApproxCountLimit()) {
                        if (hits.getSize() > 0) {
                            hitsSize = hits.getSize();
                            break;
                        } else {
                            node = hits.nextScoreNode();
                            continue;
                        }
                    }
                }
                IndexedNodeInfo infos = getIndexedNodeInfo(node, reader, isCount && countType.isSkipChecks());
                if (foundIds.add(infos.getMainNodeUuid())) { // <-- Added by jahia
                    if (isCount && countType.isSkipChecks()) {
                        resultCount++;
                    } else {
                        try {
                            boolean canRead = true;
                            if (isAclUuidInIndex()) {
                                canRead = checkIndexedAcl(checkedAcls, infos.getAclUuid());
                            }

                            boolean checkVisibility = "1".equals(infos.getCheckVisibility()) && Constants.LIVE_WORKSPACE.equals(session.getWorkspace().getName());
                            if (canRead && (!Constants.LIVE_WORKSPACE.equals(session.getWorkspace().getName())
                                    || ((infos.getPublished() == null || "true".equals(infos.getPublished()))
                                    && (infos.getCheckInvalidLanguages() == null || getLocale() == null || !infos.getCheckInvalidLanguages().contains(getLocale().toString()))))) {
                                if (filter == Predicate.TRUE) { // <-- Added by jahia
                                    if ((hasFacets & FacetHandler.ONLY_FACET_COLUMNS) == 0) {
                                        Row row = null;

                                        if (checkVisibility || !isAclUuidInIndex()) {
                                            NodeImpl objectNode = getNodeWithAclAndVisibilityCheck(node, checkVisibility);
                                            if (isCount) {
                                                resultCount++;
                                            } else {
                                                row = new LazySelectorRow(
                                                        columns,
                                                        evaluator,
                                                        selector.getSelectorName(),
                                                        objectNode,
                                                        node.getScore());
                                            }
                                        } else {
                                            if (isCount) {
                                                resultCount++;
                                            } else {
                                                row = new LazySelectorRow(columns,
                                                        evaluator,
                                                        selector.getSelectorName(),
                                                        node.getNodeId(),
                                                        node.getScore());
                                            }
                                        }

                                        if (row == null)  {
                                            continue;
                                        }

                                        if (externalSort) {
                                            rowList.add(row);
                                        } else {
                                            // apply limit and offset rules locally
                                            if (currentNode >= offset && currentNode - offset < limit) {
                                                rows.put(node.getNodeId().toString(), row);
                                                addedNodes++;
                                            }
                                            currentNode++;
                                            // end the loop when going over the limit
                                            if (addedNodes == limit) {
                                                break;
                                            }
                                        }
                                    }
                                    if ((hasFacets & FacetHandler.FACET_COLUMNS) == FacetHandler.FACET_COLUMNS) {
                                        //Added by Jahia
                                        //can be added to bitset when ACL checked and not in live mode or no visibility rule to check
                                        if (isAclUuidInIndex() && !checkVisibility) {
                                            bitset.set(infos.getDocNumber());
                                        } else {
                                            //try to load nodeWrapper to check the visibility rules
                                            getNodeWithAclAndVisibilityCheck(node, checkVisibility);
                                            bitset.set(infos.getDocNumber());
                                        }
                                        //!Added by Jahia
                                    }
                                } else {
                                    NodeImpl objectNode = session.getNodeById(node.getNodeId());
                                    if (objectNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                                        objectNode = (NodeImpl) objectNode.getParent();
                                    }
                                    if (isCount) {
                                        resultCount++;
                                    } else {
                                        Row row = new SelectorRow(columns, evaluator, selector.getSelectorName(), objectNode, node.getScore());
                                        if (filter.evaluate(row)) {
                                            if ((hasFacets & FacetHandler.ONLY_FACET_COLUMNS) == 0) {
                                                if (externalSort) {
                                                    rowList.add(row);
                                                } else {
                                                    // apply limit and offset rules locally
                                                    if (currentNode >= offset && currentNode - offset < limit) {
                                                        rows.put(node.getNodeId().toString(), row);
                                                        addedNodes++;
                                                    }
                                                    currentNode++;
                                                    // end the loop when going over the limit
                                                    if (addedNodes == limit) {
                                                        break;
                                                    }
                                                }
                                            }
                                            if ((hasFacets & FacetHandler.FACET_COLUMNS) == FacetHandler.FACET_COLUMNS) {
                                                bitset.set(infos.getDocNumber()); // <-- Added by jahia
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (PathNotFoundException | ItemNotFoundException e) {
                            // skip the node
                        }
                    }
                } else {
                    if (((hasFacets & FacetHandler.ONLY_FACET_COLUMNS) == 0)
                            && !isCount
                            && !externalSort
                            && !infos.getMainNodeUuid().equals(
                            node.getNodeId().toString())
                            && rows.containsKey(infos.getMainNodeUuid())) {
                        // we've got the translation node -> adjusting the position of the original node in the result list
                        rows.put(infos.getMainNodeUuid(), rows.remove(infos.getMainNodeUuid()));
                    }
                }// <-- Added by jahia
                node = hits.nextScoreNode();
            }

            if (rowList == null) {
                rowList = (rows == null) ? new LinkedList<>() : new LinkedList<>(rows.values());
            }

            // Added by jahia
            if ((hasFacets & FacetHandler.FACET_COLUMNS) == FacetHandler.FACET_COLUMNS) {
                OpenBitSet docIdSet = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());

                FacetHandler h = new FacetHandler(columns, selector, docIdSet, index, session, nsMappings);
                h.handleFacets(reader);
                rowList.add(0, h.getFacetsRow());

            } else if (isCount) {
                boolean wasApproxLimitReached = false;
                if (countType.isApproxCount() && hitsSize > countType.getApproxCountLimit()) {
                    resultCount = hitsSize * resultCount / countType.getApproxCountLimit();
                    resultCount = (int) Math.ceil(MathUtils.round(resultCount,
                            resultCount < 1000 ? -1 : (resultCount < 10000 ? -2 : -3), BigDecimal.ROUND_UP));
                    wasApproxLimitReached = true;
                }
                rowList.add(0,CountHandler.createCountRow(resultCount, wasApproxLimitReached));
            }
            // End

            return rowList;
        } finally {
            if(hits != null){
                hits.close();
            }
            Util.closeOrRelease(reader);
        }
    }

    private NodeImpl getNodeWithAclAndVisibilityCheck(ScoreNode node, boolean checkVisibility) throws RepositoryException {
        NodeImpl objectNode = session.getNodeById(node.getNodeId());

        if (objectNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
            objectNode = (NodeImpl) objectNode.getParent();
        }

        if (checkVisibility) {
            String nodePath = objectNode.getPath();
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE);
            if (session.itemExists(nodePath) && !VisibilityService.getInstance().matchesConditions(session.getNode(nodePath))) {
                throw new ItemNotFoundException(node.getNodeId().toString());
            }
        }

        return objectNode;
    }

    private boolean checkIndexedAcl(Map<String, Boolean> checkedAcls, String aclUuid) throws RepositoryException {
        return checkIndexedAcl(checkedAcls, aclUuid, session);
    }

    /**
     * Returns <code>true</code> if ACL-UUID should be resolved and stored in index.
     * This can have a negative effect on performance, when setting rights on a node,
     * which has a large subtree using the same rights, as all these nodes will need
     * to be reindexed. On the other side the advantage is that the queries are faster,
     * as the user rights are resolved faster.
     *
     * @return Returns <code>true</code> if ACL-UUID should be resolved and stored in index.
     */
    public boolean isAclUuidInIndex() {
        return JahiaSearchIndex.isAclUuidInIndex(index);
    }

    /**
     * Get a String array of indexed fields for running quick checks
     * [0] the uuid of the language independent node
     * [1] the acl-id
     * [2] "1" if visibility rule is set for node
     * [3] "true" node is published / "false" node is not published
     */
    private IndexedNodeInfo getIndexedNodeInfo(ScoreNode sn, IndexReader reader, final boolean onlyMainNodeUuid) throws IOException {
        final IndexedNodeInfo info = new IndexedNodeInfo(sn.getDoc(reader));
        final FieldSelector fieldSelector = (onlyMainNodeUuid) ? ONLY_MAIN_NODE_UUID : OPTIMIZATION_FIELDS;
        final Document doc = reader.document(info.getDocNumber(), fieldSelector);

        if (doc.getFieldable(JahiaNodeIndexer.TRANSLATED_NODE_PARENT) != null) {
            info.setMainNodeUuid(doc.getFieldable(FieldNames.PARENT).stringValue());
        } else {
            info.setMainNodeUuid(sn.getNodeId().toString());
        }

        if (!onlyMainNodeUuid) {
            if (isAclUuidInIndex()) {
                Fieldable aclUuidField = doc.getFieldable(JahiaNodeIndexer.ACL_UUID);
                if (aclUuidField != null) {
                    info.setAclUuid(aclUuidField.stringValue());
                }
            }

            Fieldable checkVisibilityField = doc.getFieldable(JahiaNodeIndexer.CHECK_VISIBILITY);
            if (checkVisibilityField != null) {
                info.setCheckVisibility(checkVisibilityField.stringValue());
            }

            Fieldable publishedField = doc.getFieldable(JahiaNodeIndexer.PUBLISHED);
            if (publishedField != null) {
                info.setPublished(publishedField.stringValue());
            }

            Fieldable[] checkInvalidLanguagesField = doc.getFieldables(JahiaNodeIndexer.INVALID_LANGUAGES);
            for (Fieldable field : checkInvalidLanguagesField) {
                info.addInvalidLanguages(field.stringValue());
            }
        }

        return info;
    }

    @Override
    protected Query getNodeIdQuery(String field, String path) throws RepositoryException {
        try {
            if (field.equals(FieldNames.PARENT)) {
                String identifier = session.getNode(path).getIdentifier();
                Query q1 = new JackrabbitTermQuery(new Term(FieldNames.PARENT, identifier));
                Query q2 = new JackrabbitTermQuery(new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, identifier));

                BooleanQuery or = new BooleanQuery();
                or.add(q1, BooleanClause.Occur.SHOULD);
                or.add(q2, BooleanClause.Occur.SHOULD);
                return or;

            } else {
                return super.getNodeIdQuery(field, path);
            }
        } catch (AccessDeniedException | PathNotFoundException e) {
            return new JackrabbitTermQuery(new Term(FieldNames.UUID, "invalid-node-id")); // never matches
        }
    }

    @Override
    protected Query create(Constraint constraint, Map<String, NodeType> selectorMap, JackrabbitIndexSearcher searcher) throws RepositoryException, IOException {
        if (constraint instanceof SameNode) {
            SameNode sn = (SameNode) constraint;
            if (locale != null) {
                String identifier = session.getNode(sn.getPath()).getIdentifier();

                Query q1 = new JackrabbitTermQuery(new Term(FieldNames.UUID, identifier));
                Query q2 = new JackrabbitTermQuery(new Term(FieldNames.PARENT, identifier));
                Query q3 = new JackrabbitTermQuery(new Term(JahiaNodeIndexer.TRANSLATION_LANGUAGE, locale.toString()));

                BooleanQuery and = new BooleanQuery();
                and.add(q2, BooleanClause.Occur.MUST);
                and.add(q3, BooleanClause.Occur.MUST);

                BooleanQuery or = new BooleanQuery();
                or.add(q1, BooleanClause.Occur.SHOULD);
                or.add(and, BooleanClause.Occur.SHOULD);

                return or;

            } else {
                return getNodeIdQuery(UUID, sn.getPath());
            }
        }

        return super.create(constraint, selectorMap, searcher);
    }

    @Override
    protected Query getFullTextSearchQuery(FullTextSearch fts) throws RepositoryException {
        if (!StringUtils.startsWith(fts.getPropertyName(), "rep:filter(")) {
            return super.getFullTextSearchQuery(fts);
        }

        StaticOperand expr = fts.getFullTextSearchExpression();
        if (expr instanceof Literal) {
            String expression = ((Literal) expr).getLiteralValue().getString();
            // check if query is a single range query with mixed inclusive/exclusive endpoints, then
            // directly create range query as the Lucene parser fails with ParseException (LUCENE-996)
            Query qobj = resolveSingleMixedInclusiveExclusiveRangeQuery(expression);

            if (qobj == null) {
                try {
                    QueryParser qp = new JahiaQueryParser(FieldNames.FULLTEXT, new KeywordAnalyzer());
                    qp.setLowercaseExpandedTerms(false);
                    qobj = qp.parse(expression);
                } catch (ParseException e) {
                    throw new RepositoryException(e);
                }
            }

            return qobj;
        }

        throw new RepositoryException("Unknown static operand type: " + expr);
    }

    private Query resolveSingleMixedInclusiveExclusiveRangeQuery(String expression) {
        boolean inclusiveEndRange = expression.endsWith("]");
        boolean exclusiveEndRange = expression.endsWith("}");
        int inclusiveBeginRangeCount = StringUtils.countMatches(expression, "[");
        int exclusiveBeginRangeCount = StringUtils.countMatches(expression, "{");

        if ((inclusiveEndRange && exclusiveBeginRangeCount == 1 && inclusiveBeginRangeCount == 0)
                || (exclusiveEndRange && inclusiveBeginRangeCount == 1 && exclusiveBeginRangeCount == 0)) {
            final String separator = inclusiveEndRange ? ":{" : ":[";
            String fieldName = (inclusiveEndRange || exclusiveEndRange) ? StringUtils.substringBefore(expression, separator) : "";
            if (fieldName.indexOf(' ') == -1) {
                fieldName = fieldName.replace("\\:", ":");

                final String open = inclusiveEndRange ? "{" : "[";
                final String close = inclusiveEndRange ? "]" : "}";
                String rangeExpression = StringUtils.substringBetween(expression, open, close);

                String part1 = StringUtils.substringBefore(rangeExpression, " TO");
                String part2 = StringUtils.substringAfter(rangeExpression, "TO ");

                SchemaField sf = new SchemaField(fieldName, JahiaQueryParser.STRING_TYPE);
                return JahiaQueryParser.STRING_TYPE.getRangeQuery(null, sf,
                        part1.equals("*") ? null : part1,
                        part2.equals("*") ? null : part2,
                        !inclusiveEndRange, inclusiveEndRange);
            }
        }

        return null;
    }

    @Override
    protected Predicate mapConstraintToQueryAndFilter(QueryPair query, Constraint constraint, Map<String, NodeType> selectorMap,
            JackrabbitIndexSearcher searcher, IndexReader reader) throws RepositoryException, IOException {
        try {
            if (constraint instanceof DescendantNode && !((DescendantNode) constraint).getAncestorPath().equals("/")) {
                Node parentNode = session.getNode(((DescendantNode) constraint).getAncestorPath()).getParent();
                Term term = new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, parentNode.getIdentifier());
                query.subQuery.add(new TermQuery(term), BooleanClause.Occur.MUST_NOT);

            } else if (constraint instanceof ChildNode && !((ChildNode) constraint).getParentPath().equals("/")) {
                Node parentNode = session.getNode(((ChildNode) constraint).getParentPath()).getParent();
                Term term = new Term(JahiaNodeIndexer.TRANSLATED_NODE_PARENT, parentNode.getIdentifier());
                query.subQuery.add(new TermQuery(term), BooleanClause.Occur.MUST_NOT);

            } else if (constraint instanceof Or) {
                final BooleanQuery context = new BooleanQuery();
                if (mapOrConstraintWithDescendantNodesOnly(context, constraint)) {
                    query.mainQuery = new DescendantSelfAxisQuery(context, query.subQuery, false);
                    return Predicate.TRUE;
                }
            }
        } catch (AccessDeniedException e) {
            // denied
            // todo : should find another way to test that we are not in a translation sub node
        } catch (PathNotFoundException e) {
            // not found
            Term term = new Term(FieldNames.UUID, "invalid-node-id"); // never matches
            query.subQuery.add(new JackrabbitTermQuery(term), MUST);
        }

        return super.mapConstraintToQueryAndFilter(query,constraint, selectorMap, searcher, reader);
    }

    public boolean mapOrConstraintWithDescendantNodesOnly(BooleanQuery context, Constraint constraint) throws RepositoryException {
        if (constraint instanceof Or) {
            Or or = (Or) constraint;
            return mapOrConstraintWithDescendantNodesOnly(context, or.getConstraint1())
                    && mapOrConstraintWithDescendantNodesOnly(context, or.getConstraint2());

        } else if (constraint instanceof DescendantNode) {
            DescendantNode descendantNode = (DescendantNode) constraint;
            context.add(getNodeIdQuery(UUID, descendantNode.getAncestorPath()), BooleanClause.Occur.SHOULD);
            return true;
        }

        return false;
    }

    public Locale getLocale() {
        // if the query set a specific language, we should probably be using this one
        if (queryLanguage != null) {
            return LanguageCodeConverters.languageCodeToLocale(queryLanguage);
        }

        return locale;
    }

    public void setQueryLanguageAndLocale(String queryLanguage, Locale locale) {
        this.queryLanguage = queryLanguage;
        this.locale = locale;
    }

    @Override
    protected Query getComparisonQuery(DynamicOperand left, int transform, String operator, StaticOperand rigth,
            Map<String, NodeType> selectorMap) throws RepositoryException {

        if (left instanceof PropertyValue) {
            String propertyName = ((PropertyValue) left).getPropertyName();
            if (propertyName.equals("_PARENT")) {
                String valueString = getValueString(evaluator.getValue(rigth), PropertyType.REFERENCE);
                return new JackrabbitTermQuery(new Term(FieldNames.PARENT, valueString));

            } else if (propertyName.equals(Constants.JCR_PRIMARYTYPE) || propertyName.equals(Constants.JCR_MIXINTYPES)) {
                String field = npResolver.getJCRName(session.getQName(propertyName));
                return getPropertyValueQuery(field, operator, evaluator.getValue(rigth), PropertyType.NAME, transform);
            }
        }

        return super.getComparisonQuery(left, transform, operator, rigth, selectorMap);
    }

    @Override
    protected Analyzer getTextAnalyzer() {
        if (locale != null || queryLanguage != null) {
            // if we have a locale or the query specified a language, use it to retrieve a potential language-specific Analyzer
            final AnalyzerRegistry analyzerRegistry = index.getAnalyzerRegistry();
            final String lang = getLocale().toString();
            if (analyzerRegistry.acceptKey(lang)) {
                final Analyzer analyzer = analyzerRegistry.getAnalyzer(lang);
                if (analyzer != null) {
                    return analyzer;
                }
            }
        }

        // if we didn't find a language-specific analyzer, just return the default one
        return super.getTextAnalyzer();
    }

    public NamespaceMappings getNamespaceMappings() {
        return this.nsMappings;
    }

    class IndexedNodeInfo {
        private int docNumber;
        private String mainNodeUuid;
        private String aclUuid;
        private String checkVisibility;
        private String published;
        private List<String> checkInvalidLanguages = null;

        public IndexedNodeInfo(int docNumber) {
            super();
            this.docNumber = docNumber;
        }

        public String getMainNodeUuid() {
            return mainNodeUuid;
        }

        public void setMainNodeUuid(String mainNodeUuid) {
            this.mainNodeUuid = mainNodeUuid;
        }

        public String getAclUuid() {
            return aclUuid;
        }

        public void setAclUuid(String aclUuid) {
            this.aclUuid = aclUuid;
        }

        public String getCheckVisibility() {
            return checkVisibility;
        }

        public void setCheckVisibility(String checkVisibility) {
            this.checkVisibility = checkVisibility;
        }

        public String getPublished() {
            return published;
        }

        public void setPublished(String published) {
            this.published = published;
        }

        public int getDocNumber() {
            return docNumber;
        }

        public List<String> getCheckInvalidLanguages() {
            return checkInvalidLanguages;
        }

        public void addInvalidLanguages(String invalidLanguage) {
            if(checkInvalidLanguages==null){
                checkInvalidLanguages = new ArrayList<>();
            }
            checkInvalidLanguages.add(invalidLanguage);
        }
    }

    class LazySelectorRow extends SelectorRow {
        private Node node;
        private NodeId nodeId;

        LazySelectorRow(Map<String, PropertyValue> columns, OperandEvaluator evaluator, String selector, NodeId nodeId, double score) {
            super(columns, evaluator, selector, null, score);
            this.nodeId = nodeId;
        }

        LazySelectorRow(Map<String, PropertyValue> columns, OperandEvaluator evaluator, String selector, Node node, double score) {
            super(columns, evaluator, selector, node, score);
            this.node = node;
        }

        @Override
        public Node getNode() {
            if (node != null) {
                return node;
            }

            try {
                Node originalNode = session.getNodeById(nodeId);
                if (originalNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                    originalNode = originalNode.getParent();
                }
                if (originalNode != null) {
                    node = originalNode;
                }
            } catch (ItemNotFoundException | PathNotFoundException e) {
                // ignored
            } catch (RepositoryException e) {
                logger.error("Cannot get node " + nodeId, e);
            }

            return node;
        }

        @Override
        public Node getNode(String selectorName) throws RepositoryException {
            super.getNode(selectorName);
            return getNode();
        }
    }

}

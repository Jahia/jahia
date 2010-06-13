package org.apache.jackrabbit.core.query.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.RowIterator;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Selector;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.ItemManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.AccessManager;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.query.qom.ColumnImpl;
import org.apache.jackrabbit.spi.commons.query.qom.OrderingImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SelectorImpl;
import org.apache.jackrabbit.spi.commons.query.qom.SourceImpl;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.DocIdBitSet;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.util.OpenBitSetDISI;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.jahia.services.search.facets.SimpleJahiaJcrFacets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JahiaMultiColumnQueryResult extends QueryResultImpl {

    /**
     * The logger instance for this class
     */
    private static final Logger log = LoggerFactory.getLogger(JahiaMultiColumnQueryResult.class);
    
    private static final String RANGEFROM_INCLUSIVE_PREFIX = ":["; 
    private static final String RANGEFROM_EXCLUSIVE_PREFIX = ":{";

    /**
     * The result nodes including their score. This list is populated on a lazy basis while a client iterates through the results.
     * <p/>
     * The exact type is: <code>List&lt;ScoreNode[]></code>
     */
    private final List<ScoreNode[]> displayedResultNodes = new ArrayList<ScoreNode[]>();

    /**
     * The result nodes including their score. This list is populated on a lazy basis while a client iterates through the results.
     * <p/>
     * The exact type is: <code>List&lt;ScoreNode[]></code>
     */
    private final List<ScoreNode[]> allResultNodes = new ArrayList<ScoreNode[]>();

    // Facet stuff
    boolean facetsResolved = false;
    private Map<String, Long> _facetQuery = null;
    private List<FacetField> _facetFields = null;
    private List<FacetField> _limitingFacets = null;
    private List<FacetField> _facetDates = null;

    /**
     * The number of results that are invalid, either because a node does not exist anymore or because the session does not have access to
     * the node.
     */
    private int invalid = 0;

    /**
     * The offset in the total result set
     */
    private final long offset;

    /**
     * The maximum size of this result if limit >= 0
     */
    private final long limit;

    /**
     * This is the raw number of results that matched the query. This number also includes matches which will not be returned due to access
     * restrictions. This value is set whenever hits are obtained.
     */
    private int numResults = -1;

    /**
     * The selector names associated with the score nodes. The selector names are set when the query is executed via
     * {@link #getResults(long)}.
     */
    private Name[] selectorNames;

    /**
     * The excerpt provider or <code>null</code> if none was created yet.
     */
    private ExcerptProvider excerptProvider;

    /**
     * The query to execute.
     */
    private final MultiColumnQuery query;

    /**
     * The order specifier for each of the order properties.
     */
    protected final Ordering[] orderings;

    /**
     * The name of the facet function without prefix but with left parenthesis.
     */
    private static final String FACET_FUNC_LPAR = "facet(";

    /**
     * The start Name for the rep:facet function: rep:facet(
     */
    private static final Name REP_FACET_LPAR = NameFactoryImpl.getInstance().create(
            Name.NS_REP_URI, FACET_FUNC_LPAR);
    
    private static final String FIELD_SPECIFIC_PREFIX = "f.";

    public JahiaMultiColumnQueryResult(SearchIndex index, ItemManager itemMgr, SessionImpl session,
            AccessManager accessMgr, AbstractQueryImpl queryImpl, MultiColumnQuery query,
            SpellSuggestion spellSuggestion, ColumnImpl[] columns, OrderingImpl[] orderings,
            boolean documentOrder, long offset, long limit) throws RepositoryException {
        super(index, itemMgr, session, accessMgr, queryImpl, spellSuggestion, columns,
                documentOrder, offset, limit);
        this.offset = offset;
        this.limit = limit;
        this.query = query;
        this.orderings = index.createOrderings(orderings);
        // if document order is requested get all results right away
        getResults(docOrder ? Integer.MAX_VALUE : index.getResultFetchSize());
    }

    /**
     * {@inheritDoc}
     */
    protected MultiColumnQueryHits executeQuery(long resultFetchHint) throws IOException {
        return ((JahiaSearchIndex) index).executeQuery(session, query, orderings, resultFetchHint,
                true);
    }

    /**
     * Creates a {@link ScoreNodeIterator} over the query result.
     * 
     * @return a {@link ScoreNodeIterator} over the query result.
     */
    private ScoreNodeIterator getScoreNodes() {
        if (docOrder) {
            return new DocOrderScoreNodeIterator(itemMgr, displayedResultNodes, 0);
        } else {
            return new LazyScoreNodeIteratorImpl();
        }
    }

    /**
     * Collect score nodes from <code>hits</code> into the <code>collector</code> list until the size of <code>collector</code> reaches
     * <code>maxResults</code> or there are not more results.
     * 
     * @param hits
     *            the raw hits.
     * @param collector
     *            where the access checked score nodes are collected.
     * @param maxResults
     *            the maximum number of results in the collector.
     * @throws IOException
     *             if an error occurs while reading from hits.
     * @throws RepositoryException
     *             if an error occurs while checking access rights.
     */
    private void collectScoreNodes(MultiColumnQueryHits hits, List<ScoreNode[]> collector,
            long maxResults) throws IOException, RepositoryException {
        while (collector.size() < maxResults) {
            ScoreNode[] sn = hits.nextScoreNodes();
            if (sn == null) {
                // no more results
                break;
            }
            // check access
            if (isAccessGranted(sn)) {
                collector.add(sn);
            } else {
                invalid++;
            }
        }
    }

    /**
     * Attempts to get <code>size</code> results and puts them into {@link #displayedResultNodes}. If the size of
     * {@link #displayedResultNodes} is less than <code>size</code> then there are no more than <code>resultNodes.size()</code> results for
     * this query.
     * 
     * @param size
     *            the number of results to fetch for the query.
     * @throws RepositoryException
     *             if an error occurs while executing the query.
     */
    protected void getResults(long size) throws RepositoryException {
        if (log.isDebugEnabled()) {
            log.debug("getResults({}) limit={}", size, limit);
        }

        long maxResultSize = size;

        // is there any limit?
        if (limit >= 0) {
            maxResultSize = limit;
        }

        if (displayedResultNodes.size() >= maxResultSize && selectorNames != null) {
            // we already have them all
            return;
        }

        // execute it
        MultiColumnQueryHits result = null;
        try {
            long time = System.currentTimeMillis();
            long r1 = IOCounters.getReads();
            result = executeQuery(maxResultSize);
            long r2 = IOCounters.getReads();
            log.debug("query executed in {} ms ({})", System.currentTimeMillis() - time, r2 - r1);
            // set selector names
            selectorNames = result.getSelectorNames();
            if (displayedResultNodes.isEmpty() && offset > 0) {
                // collect result offset into the allResultNodes list
                collectScoreNodes(result, allResultNodes, offset);
            } else {
                int start = displayedResultNodes.size() + invalid + (int) offset;
                result.skip(start);
            }

            time = System.currentTimeMillis();
            collectScoreNodes(result, displayedResultNodes, maxResultSize);
            // add also the displayed result nodes to the allResultNodes list
            allResultNodes.addAll(displayedResultNodes);
            // get also the rest as it is needed for faceting
            collectScoreNodes(result, allResultNodes, Long.MAX_VALUE);

            long r3 = IOCounters.getReads();
            log.debug("retrieved ScoreNodes in {} ms ({})", System.currentTimeMillis() - time, r3
                    - r2);

            // handle faceting
            if (result instanceof JahiaFilterMultiColumnQueryHits && hasFacetFunctions()
                    && !facetsResolved) {
                handleFacets(((JahiaFilterMultiColumnQueryHits) result)
                        .getReader());
                facetsResolved = true;
            }

            // update numResults
            numResults = result.getSize();
        } catch (IOException e) {
            log.error("Exception while executing query: ", e);
            // todo throw?
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (IOException e) {
                    log.warn("Unable to close query result: " + e);
                }
            }
        }
    }

    private void handleFacets(IndexReader reader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        try {
            String facetFunctionPrefix = session.getJCRName(REP_FACET_LPAR);
            NamedList<Object> parameters = new NamedList<Object>();
            int counter = 0;
            Set<Integer> selectorIndexes = new HashSet<Integer>();
            for (Column column : columns.values()) {
                if (isFacetFunction(column.getColumnName())) {
                    String facetOptions = StringUtils.substring(column.getColumnName(), StringUtils
                            .indexOf(column.getColumnName(), facetFunctionPrefix)
                            + facetFunctionPrefix.length(), StringUtils.lastIndexOf(column
                            .getColumnName(), ")"));
                   
                    String propertyName = null;
                    if (!StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, FacetParams.FACET_FIELD + "=")) ||
                            !StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, "field="))) {
                        propertyName = StringUtils.substring(propertyName, 0, StringUtils.indexOfAny(
                                propertyName, "&)") >= 0 ? StringUtils.indexOfAny(propertyName,
                                "&)") : propertyName.length()) + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter; 
                    } else if (!StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, FacetParams.FACET_DATE + "=")) ||
                            !StringUtils.isEmpty(propertyName = StringUtils.substringAfter(facetOptions, "date="))) {
                        propertyName = StringUtils.substring(propertyName, 0, StringUtils.indexOfAny(
                                propertyName, "&)") >= 0 ? StringUtils.indexOfAny(propertyName,
                                "&)") : propertyName.length()) + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;                        
                    } else if (!StringUtils.contains(facetOptions, FacetParams.FACET_QUERY)) {
                        propertyName = column.getPropertyName() + SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR + counter;
                        parameters.add((facetOptions.indexOf("&date.") >= 0 || facetOptions
                                .indexOf("facet.date.") >= 0) ? FacetParams.FACET_DATE
                                : FacetParams.FACET_FIELD, propertyName);
                    }
                    
                    for (String option : StringUtils.split(facetOptions, "&")) {
                        String key = StringUtils.substringBefore(option, "=");
                        String value = StringUtils.substringAfter(option, "=");
                        int index = 0;
                        if (StringUtils.startsWith(key, FIELD_SPECIFIC_PREFIX)) {
                            index = FIELD_SPECIFIC_PREFIX.length() + StringUtils.substringBetween(key, ".", ".").length() + 1;
                        }
                        int indexOfFacetPrefix = StringUtils.indexOf(key, FacetParams.FACET + ".",
                                index);
                        if (indexOfFacetPrefix == index) {
                            index = FacetParams.FACET.length() + 1;
                        }
                        String facetOption = FacetParams.FACET + "." + StringUtils.substring(key, index);
                        if (facetOption.equals(FacetParams.FACET_QUERY)) {           
                            parameters.add(facetOption, value);                            
                        } else if (facetOption.equals(FacetParams.FACET_FIELD) || facetOption.equals(FacetParams.FACET_DATE)) {
                            parameters.add(facetOption, propertyName);                            
                        } else {
                            parameters.add(FIELD_SPECIFIC_PREFIX + propertyName + "."
                                    + facetOption, value);
                        }                       
                    }
                    if (!StringUtils.contains(facetOptions, FacetParams.FACET_QUERY)) {
                        String nodeTypeParam = FIELD_SPECIFIC_PREFIX + propertyName + "."
                                + FacetParams.FACET + ".nodetype";
                        if (parameters.get(nodeTypeParam) == null) {
                            parameters.add(nodeTypeParam, getNodeTypeFromSelector(column
                                    .getSelectorName(), column.getPropertyName()));
                        }
                    }
                    int i = 0;
                    for (String selectorName : getSelectorNames()) {
                        if (selectorName.equals(column.getSelectorName())) {
                            selectorIndexes.add(i);
                            break;
                        }
                        i++;
                    }
                    counter++;
                }
            }

            SimpleJahiaJcrFacets facets = new SimpleJahiaJcrFacets(
                    ((JahiaQueryObjectModelImpl) queryImpl).getQomTree(), searcher,
                    transformToDocIdSet(allResultNodes, reader, selectorIndexes), SolrParams
                            .toSolrParams(parameters), index, session);
            extractFacetInfo(facets.getFacetCounts());
        } catch (Exception ex) {
            log.warn("Problem creating facets: ", ex);
        } finally {
            try {
                searcher.close();
            } catch (IOException e) {
                log.warn("Unable to close searcher: " + e);
            }
        }
        return;
    }
    
    private String getNodeTypeFromSelector(String selectorName,
            String propertyName) throws RepositoryException {
        Selector foundSelector = null;
        for (SelectorImpl selector : ((SourceImpl) ((JahiaQueryObjectModelImpl) queryImpl).getQomTree().getSource()).getSelectors()) {
            if (StringUtils.isEmpty(selectorName) || selectorName.equals(selector.getSelectorName())) {
                foundSelector = selector;
                break;
            }
        }        
        return foundSelector.getNodeTypeName();
    }
    
    private OpenBitSet transformToDocIdSet(List<ScoreNode[]> scoreNodeArrays, IndexReader reader, Set<Integer> selectorIndexes) {
        OpenBitSet docIds = null;
        try {
            BitSet bitset = new BitSet();
            for (ScoreNode[] scoreNodes : scoreNodeArrays) {
                int i = 0;
                for (ScoreNode node : scoreNodes) {
                    if (node != null && selectorIndexes.contains(i)) {
                        bitset.set(node.getDoc(reader));
                    }
                    i++;
                }
            }
            docIds = new OpenBitSetDISI(new DocIdBitSet(bitset).iterator(), bitset.size());
        } catch (IOException e) {
            log.debug("Can't retrive bitset from hits", e);
        }
        return docIds;
    }

    /**
     * Returns the total number of hits. This is the number of results you will get get if you don't set any limit or offset. Keep in mind
     * that this number may get smaller if nodes are found in the result set which the current session has no permission to access. This
     * method may return <code>-1</code> if the total size is unknown.
     * 
     * @return the total number of hits.
     */
    public int getTotalSize() {
        if (numResults == -1) {
            return -1;
        } else {
            return numResults - invalid;
        }
    }

    /**
     * Checks if access is granted to all <code>nodes</code>.
     * 
     * @param nodes
     *            the nodes to check.
     * @return <code>true</code> if read access is granted to all <code>nodes</code>.
     * @throws RepositoryException
     *             if an error occurs while checking access rights.
     */
    private boolean isAccessGranted(ScoreNode[] nodes) throws RepositoryException {
        for (ScoreNode node : nodes) {
            try {
                // TODO: rather use AccessManager.canRead(Path)
                if (node != null && !accessMgr.isGranted(node.getNodeId(), AccessManager.READ)) {
                    return false;
                }
            } catch (ItemNotFoundException e) {
                // node deleted while query was executed
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public NodeIterator getNodes() throws RepositoryException {
        return new NodeIteratorImpl(itemMgr, getScoreNodes(), 0);
    }

    /**
     * {@inheritDoc}
     */
    public RowIterator getRows() throws RepositoryException {
        if (excerptProvider == null) {
            try {
                excerptProvider = createExcerptProvider();
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
        return new RowIteratorImpl(getScoreNodes(), columns, selectorNames, itemMgr, index
                .getContext().getHierarchyManager(), session, session.getValueFactory(),
                excerptProvider, spellSuggestion);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getSelectorNames() throws RepositoryException {
        String[] names = new String[selectorNames.length];
        for (int i = 0; i < selectorNames.length; i++) {
            names[i] = session.getJCRName(selectorNames[i]);
        }
        return names;
    }

    /**
     * {@inheritDoc}
     */
    protected ExcerptProvider createExcerptProvider() throws IOException {
        // TODO
        return null;
    }

    private final class LazyScoreNodeIteratorImpl implements ScoreNodeIterator {

        private int position = -1;

        private boolean initialized = false;

        private ScoreNode[] next;

        public ScoreNode[] nextScoreNodes() {
            initialize();
            if (next == null) {
                throw new NoSuchElementException();
            }
            ScoreNode[] sn = next;
            fetchNext();
            return sn;
        }

        /**
         * {@inheritDoc}
         */
        public void skip(long skipNum) {
            initialize();
            if (skipNum < 0) {
                throw new IllegalArgumentException("skipNum must not be negative");
            }
            if (skipNum == 0) {
                // do nothing
            } else {
                // attempt to get enough results
                try {
                    getResults(position + invalid + (int) skipNum);
                    if (displayedResultNodes.size() >= position + skipNum) {
                        // skip within already fetched results
                        position += skipNum - 1;
                        fetchNext();
                    } else {
                        // not enough results after getResults()
                        throw new NoSuchElementException();
                    }
                } catch (RepositoryException e) {
                    throw new NoSuchElementException(e.getMessage());
                }
            }
        }

        /**
         * {@inheritDoc}
         * <p/>
         * This value may shrink when the query result encounters non-existing nodes or the session does not have access to a node.
         */
        public long getSize() {
            int total = getTotalSize();
            if (total == -1) {
                return -1;
            }
            long size = total - offset;
            if (limit >= 0 && size > limit) {
                return limit;
            } else {
                return size;
            }
        }

        /**
         * {@inheritDoc}
         */
        public long getPosition() {
            initialize();
            return position;
        }

        /**
         * @throws UnsupportedOperationException
         *             always.
         */
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            initialize();
            return next != null;
        }

        /**
         * {@inheritDoc}
         */
        public Object next() {
            return nextScoreNodes();
        }

        /**
         * Initializes this iterator but only if it is not yet initialized.
         */
        private void initialize() {
            if (!initialized) {
                fetchNext();
                initialized = true;
            }
        }

        /**
         * Fetches the next node to return by this iterator. If this method returns and {@link #next} is <code>null</code> then there is no
         * next node.
         */
        private void fetchNext() {
            next = null;
            int nextPos = position + 1;
            while (next == null) {
                if (nextPos >= displayedResultNodes.size()) {
                    // quick check if there are more results at all
                    // this check is only possible if we have numResults
                    if (numResults != -1 && (nextPos + invalid) >= numResults) {
                        break;
                    }

                    // fetch more results
                    try {
                        int num;
                        if (displayedResultNodes.size() == 0) {
                            num = index.getResultFetchSize();
                        } else {
                            num = displayedResultNodes.size() * 2;
                        }
                        getResults(num);
                    } catch (RepositoryException e) {
                        log.warn("Exception getting more results: " + e);
                    }
                    // check again
                    if (nextPos >= displayedResultNodes.size()) {
                        // no more valid results
                        break;
                    }
                }
                next = displayedResultNodes.get(nextPos);
            }
            position++;
        }
    }

    private boolean hasFacetFunctions() {
        boolean hasFacetRequest = false;
        for (Column column : columns.values()) {
            if (isFacetFunction(column.getColumnName())) {
                hasFacetRequest = true;
                break;
            }
        }
        return hasFacetRequest;
    }

    /**
     * @param name
     *            a String.
     * @return <code>true</code> if <code>name</code> is the rep:facet function, <code>false</code> otherwise.
     */
    private boolean isFacetFunction(String name) {
        try {
            return name.trim().startsWith(session.getJCRName(REP_FACET_LPAR));
        } catch (NamespaceException e) {
            // will never happen
            return false;
        }
    }

    private void extractFacetInfo(NamedList<Object> info) {
        // Parse the queries
        _facetQuery = new HashMap<String, Long>();
        NamedList<Long> fq = (NamedList<Long>) info.get("facet_queries");
        for (Map.Entry<String, Long> entry : fq) {
            _facetQuery.put(entry.getKey(), entry.getValue());
        }

        // Parse the facet info into fields
        // TODO?? The list could be <int> or <long>? If always <long> then we can switch to <Long>
        NamedList<NamedList<Number>> ff = (NamedList<NamedList<Number>>) info.get("facet_fields");
        if (ff != null) {
            _facetFields = new ArrayList<FacetField>(ff.size());
            _limitingFacets = new ArrayList<FacetField>(ff.size());

            long minsize = getTotalSize();
            for (Map.Entry<String, NamedList<Number>> facet : ff) {
                String key = StringUtils.substringBeforeLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                String fieldInIndex = StringUtils.substringAfterLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                FacetField f = new FacetField(key);
                for (Map.Entry<String, Number> entry : facet.getValue()) {
                    f.add(entry.getKey(), entry.getValue().longValue());
                    f.getValues().get(f.getValueCount() - 1).setFilterQuery(
                            ClientUtils.escapeQueryChars(fieldInIndex) + ":"
                                    + ClientUtils.escapeQueryChars(entry.getKey()));
                }

                _facetFields.add(f);
                FacetField nl = f.getLimitingFields(minsize);
                if (nl.getValueCount() > 0) {
                    _limitingFacets.add(nl);
                }
            }
        }

        // Parse date facets
        NamedList<NamedList<Object>> df = (NamedList<NamedList<Object>>) info.get("facet_dates");
        if (df != null) {
            // System.out.println(df);
            _facetDates = new ArrayList<FacetField>(df.size());
            for (Map.Entry<String, NamedList<Object>> facet : df) {
                // System.out.println("Key: " + facet.getKey() + " Value: " + facet.getValue());
                NamedList<Object> values = facet.getValue();
                String gap = (String) values.get("gap");
                Date end = (Date) values.get("end");
                FacetField f = new FacetField(StringUtils.substringBeforeLast(facet.getKey(),
                        SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR), gap, end);

                for (Map.Entry<String, Object> entry : values) {
                    try {
                        String key = StringUtils.substringBeforeLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);
                        String query = StringUtils.substringAfterLast(entry.getKey(),
                                SimpleJahiaJcrFacets.PROPNAME_INDEX_SEPARATOR);                                     
                        f.add(key, Long.parseLong(entry.getValue().toString()));
                        if (!StringUtils.isEmpty(query)) {
                            String rangePrefix = null;
                            if (query.contains(RANGEFROM_EXCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_EXCLUSIVE_PREFIX;
                            } else if (query.contains(RANGEFROM_INCLUSIVE_PREFIX)) {
                                rangePrefix = RANGEFROM_INCLUSIVE_PREFIX;
                            }
                            if (!StringUtils.isEmpty(rangePrefix)) {
                                f.getValues().get(f.getValueCount() - 1).setFilterQuery(
                                        ClientUtils.escapeQueryChars(StringUtils.substringBefore(
                                                query, rangePrefix))
                                                + rangePrefix
                                                + StringUtils.substringAfter(query, rangePrefix));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignore for non-number responses which are already handled above
                    }
                }

                _facetDates.add(f);
            }
        }
    }

    public Map<String, Long> getFacetQuery() {
        return _facetQuery;
    }

    public List<FacetField> getFacetFields() {
        return _facetFields;
    }

    public List<FacetField> getFacetDates() {
        return _facetDates;
    }

    /**
     * get
     * 
     * @param name
     *            the name of the
     * @return the FacetField by name or null if it does not exist
     */
    public FacetField getFacetField(String name) {
        if (_facetFields == null)
            return null;
        for (FacetField f : _facetFields) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    public FacetField getFacetDate(String name) {
        if (_facetDates == null)
            return null;
        for (FacetField f : _facetDates)
            if (f.getName().equals(name))
                return f;
        return null;
    }

    public List<FacetField> getLimitingFacets() {
        return _limitingFacets;
    }
}

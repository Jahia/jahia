package org.jahia.services.content;

import org.jahia.services.query.QueryWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;

/**
 * Extension of the QueryManager interface, with wrapped return types
 */
public interface QueryManagerWrapper extends QueryManager {

    /**
     * Creates a new query by specifying the query <code>statement</code> itself
     * and the <code>language</code> in which the query is stated. The
     * <code>language</code> must be a string from among those returned by
     * QueryManager.getSupportedQueryLanguages().
     *
     * @param statement a <code>String</code>
     * @param language  a <code>String</code>
     * @return a <code>Query</code> object
     * @throws javax.jcr.query.InvalidQueryException if the query statement is syntactically
     *                               invalid or the specified language is not supported.
     * @throws javax.jcr.RepositoryException   if another error occurs.
     */
    public QueryWrapper createQuery(String statement, String language) throws InvalidQueryException, RepositoryException;

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     * @since JCR 2.0
     */
    public QueryObjectModelFactory getQOMFactory();

    /**
     * Retrieves an existing persistent query.
     * <p>
     * Persistent queries are created by first using {@link
     * QueryManager#createQuery} to create a <code>Query</code> object and then
     * calling <code>Query.save</code> to persist the query to a location in the
     * workspace.
     *
     * @param node a persisted query (that is, a node of type
     *             <code>nt:query</code>).
     * @return a <code>Query</code> object.
     * @throws InvalidQueryException If <code>node</code> is not a valid
     *                               persisted query (that is, a node of type <code>nt:query</code>).
     * @throws RepositoryException   if another error occurs
     */
    public QueryWrapper getQuery(Node node) throws InvalidQueryException, RepositoryException;



}

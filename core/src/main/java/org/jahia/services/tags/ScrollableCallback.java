package org.jahia.services.tags;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

/**
 * Callback class for query. To be used with ScrollableQuery's execute method,
 *
 * @author kevan
 */
abstract class ScrollableCallback<T> {
    QueryResult stepResult;

    protected void setStepResult(QueryResult stepResult) {
        this.stepResult = stepResult;
    }

    /**
     * While this method return true, the ScrollableQuery will re execute the query and refresh the stepResult
     *
     * @return true when we need to scroll again the query
     * @throws RepositoryException
     */
    abstract boolean scroll() throws RepositoryException;

    /**
     * When the ScrollableQuery stop to scroll, he call this this method to return the result of the callback
     *
     * @return the callback result
     */
    abstract T getResult();
}

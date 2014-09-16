package org.jahia.services.query;


import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

/**
 * ScrollableQuery allow you to execute a Query with a step iteration
 * It can be useful in some cases when you are looking for some data and just want a sample in it
 * There is a security to avoid infinite looping, it's stop automatically at the end of the data available for the query
 *
 * @author kevan
 */
public class ScrollableQuery {
    private long step = 100;
    private long maxIteration = -1;
    private Query query;

    /**
     * create a ScrollableQuery instance
     *
     * @param step the step size
     * @param query the query to scroll on
     */
    public ScrollableQuery(long step, Query query) {
        this.step = step;
        this.query = query;
    }

    /**
     * create a ScrollableQuery instance, with an iteration limit.
     *
     * @param step the step size
     * @param maxIteration the maximum number of iteration
     * @param query the query to scroll on
     */
    public ScrollableQuery(long step, long maxIteration, Query query) {
        this.step = step;
        this.maxIteration = maxIteration;
        this.query = query;
    }

    /**
     * The callback
     *
     * @param callback the callback that will be used on each iteration
     * @return return the callback result
     * @throws RepositoryException
     */
    public <T> T execute(ScrollableQueryCallback<T> callback) throws RepositoryException {
        query.setLimit(step);
        callback.setStepResult(query.execute());

        long _step = 0;
        long count = 0;
        while (callback.scroll()){
            count ++;
            _step += this.step;

            query.setLimit(step);
            query.setOffset(_step);
            QueryResult queryResult = query.execute();
            callback.setStepResult(queryResult);

            // handle maxIteration
            if(maxIteration == count){
                break;
            }

            // leave if the current result is less than the step size,
            // meaning that there is no more results after the current step
            if(queryResult.getNodes().getSize() < step){
                // call a last time callback.scroll() to process the last contents
                callback.scroll();
                // leave
                break;
            }
        }

        return callback.getResult();
    }
}

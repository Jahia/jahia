package org.jahia.services.tags;


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
    private Query query;

    protected ScrollableQuery(long step, Query query) {
        this.step = step;
        this.query = query;
    }

    /**
     * The callback
     *
     * @param callback the callback that will be used on each iteration
     * @return return the callback result
     * @throws RepositoryException
     */
    public <T> T execute(ScrollableCallback<T> callback) throws RepositoryException {
        query.setLimit(step);
        callback.setStepResult(query.execute());

        long _step = 0;
        while (callback.scroll()){
            _step += this.step;
            query.setLimit(step);
            query.setOffset(_step);
            QueryResult queryResult = query.execute();
            callback.setStepResult(queryResult);

            // leave if the current result is less than the step size,
            // meaning that there is no more results after the current step
            if(queryResult.getNodes().getSize() < step){
                break;
            }
        }

        return callback.getResult();
    }
}

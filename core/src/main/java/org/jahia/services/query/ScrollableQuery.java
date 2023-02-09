/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
     * @throws RepositoryException in case of JCR-related errors
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

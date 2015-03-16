/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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

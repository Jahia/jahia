/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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


import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

/**
 *  Fake query result row, holding count function result
 */
public class CountRow implements Row {

    private long count;
    private boolean approxLimitReached;

    public CountRow(long count, boolean approxLimitReached) {
        this.count = count;
        this.approxLimitReached = approxLimitReached;
    }

    @Override
    public Value[] getValues() throws RepositoryException {
        return (approxLimitReached ? new Value[] { new ValueImpl(count),
                new ValueImpl(approxLimitReached) }
                : new Value[] { new ValueImpl(count) });
    }

    @Override
    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
        return columnName.equals("approxLimitReached") ? new ValueImpl(approxLimitReached) : new ValueImpl(count);
    }

    @Override
    public Node getNode() throws RepositoryException {
        return null;
    }

    @Override
    public Node getNode(String selectorName) throws RepositoryException {
        return null;
    }

    @Override
    public String getPath() throws RepositoryException {
        return null;
    }

    @Override
    public String getPath(String selectorName) throws RepositoryException {
        return null;
    }

    @Override
    public double getScore() throws RepositoryException {
        return 0;
    }

    @Override
    public double getScore(String selectorName) throws RepositoryException {
        return 0;
    }
}

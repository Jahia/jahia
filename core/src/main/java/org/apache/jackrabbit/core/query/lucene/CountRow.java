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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

    public static final String APPROX_LIMIT_REACHED = "approxLimitReached";

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
        return APPROX_LIMIT_REACHED.equals(columnName) ? new ValueImpl(approxLimitReached) : new ValueImpl(count);
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

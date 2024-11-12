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
package org.jahia.services.query;

import java.util.LinkedList;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;

/**
 * Utility class to help building a query object mode in several steps. It is
 * mainly used by the query tag library to build the QOM with our tags.
 *
 * @author Benjamin Papez
 */
public class QOMBuilder {
    private List<Column> columns;
    private Constraint constraint;
    private List<Ordering> orderings;
    private QueryObjectModelFactory qomFactory;
    private Source source;
    private ValueFactory valueFactory;

    public QOMBuilder(QueryObjectModelFactory qomFactory, ValueFactory valueFactory) {
        this.qomFactory = qomFactory;
        this.valueFactory = valueFactory;
        orderings = new LinkedList<Ordering>();
        columns = new LinkedList<Column>();
    }

    /**
     * Add a constraint. If it is the first constraint it becomes the root
     * constraint, otherwise it is added with a logical conjunction (AND).
     *
     * @param c a constraint to add
     * @throws InvalidQueryException if a particular validity test is possible
     *             on this method, the implementation chooses to perform that
     *             test (and not leave it until later, on {@link #createQuery},
     *             and the parameters given fail that test
     * @throws RepositoryException if the operation otherwise fails
     */
    public void andConstraint(Constraint c) throws InvalidQueryException, RepositoryException {
        if (c == null) {
            return;
        }
        constraint = constraint != null ? getQOMFactory().and(constraint, c) : c;
    }

    /**
     * Creates the {@link QueryObjectModel} object from for currently provided
     * data.
     *
     * @return the created query object model (including modifications and
     *         optimizations)
     * @throws InvalidQueryException if a particular validity test is possible
     *             on this method, the implementation chooses to perform that test
     *             and the parameters given fail that test.
     * @throws RepositoryException if the operation otherwise fails
     */
    public QueryObjectModel createQOM() throws InvalidQueryException, RepositoryException {
        return getQOMFactory().createQuery(source, constraint, orderings.toArray(new Ordering[getOrderings().size()]),
                columns.toArray(new Column[getColumns().size()]));
    }

    /**
     * Gets the columns for this query.
     *
     * @return a list of defined {@link Column} objects array of zero or more
     *         columns; non-null
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Gets the constraint for this query.
     *
     * @return the constraint, or null if none
     */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * Gets the orderings for this query.
     *
     * @return a list of defined {@link Ordering} objects or an empty list if
     *         non were defined yet
     */
    public List<Ordering> getOrderings() {
        return orderings;
    }

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     */
    public QueryObjectModelFactory getQOMFactory() {
        return qomFactory;
    }

    /**
     * Returns the node-tuple source for this query.
     *
     * @return the node-tuple source for this query
     */
    public Source getSource() {
        return source;
    }

    /**
     * This method returns a <code>ValueFactory</code> that is used to create <code>Value</code> objects
     * for use when setting repository properties.
     *
     * @return a <code>ValueFactory</code>
     * @throws RepositoryException if an error occurs
     */
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    /**
     * Adds a constraint. If it is the first constraint it becomes the root
     * constraint, otherwise it is added with a logical disjunction (OR).
     *
     * @param c a constraint to add
     * @throws InvalidQueryException if a particular validity test is possible
     *             on this method, the implementation chooses to perform that
     *             test (and not leave it until later, on {@link #createQuery},
     *             and the parameters given fail that test
     * @throws RepositoryException if the operation otherwise fails
     */
    public void orConstraint(Constraint c) throws InvalidQueryException, RepositoryException {
        if (c == null) {
            return;
        }
        constraint = constraint != null ? getQOMFactory().or(constraint, c) : c;
    }

    /**
     * Sets the node-tuple source for this query.
     *
     * @param s the node-tuple source for this query
     */
    public void setSource(Source s) {
        this.source = s;
    }
}

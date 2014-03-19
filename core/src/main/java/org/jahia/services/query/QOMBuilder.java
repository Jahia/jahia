/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

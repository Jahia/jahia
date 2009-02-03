/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.query.qom;

import java.util.Properties;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Column;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Ordering;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Source;
import javax.jcr.version.VersionException;

import org.jahia.exceptions.JahiaException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 09:58:49
 * To change this template use File | Settings | File Templates.
 */
public class QueryObjectModelImpl extends QOMNode implements QueryObjectModel {

    private Constraint constraint;
    private Ordering[] orderings;
    private Properties properties;
    private Source source;
    private QueryObjectModelFactoryImpl queryFactory;
    private QueryExecute queryExecute;

    /**
     *
     * @param queryFactory
     * @param queryExecute
     * @param source
     * @param constraint
     * @param orderings
     */
    public QueryObjectModelImpl( QueryObjectModelFactoryImpl queryFactory, QueryExecute queryExecute,
                                 Source source, Constraint constraint, Ordering[] orderings) {
        this(queryFactory,queryExecute,source,constraint,orderings,new Properties());
    }

    /**
     *
     * @param queryFactory
     * @param queryExecute
     * @param source
     * @param constraint
     * @param orderings
     * @param properties
     */
    public QueryObjectModelImpl( QueryObjectModelFactoryImpl queryFactory, QueryExecute queryExecute,
                                 Source source, Constraint constraint, Ordering[] orderings, Properties properties) {
        this.queryFactory = queryFactory;
        this.queryExecute = queryExecute;
        this.source = source;
        this.constraint = constraint;
        this.orderings = orderings;
        if ( this.orderings == null ){
            this.orderings = new OrderingImpl[]{};
        }
        this.properties = properties;
        if ( this.properties == null ){
            this.properties = new Properties();
        }
    }

    public Source getSource() {
        return source;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public Ordering[] getOrderings() {
        return orderings;
    }

    /**
     * Accepts an <code>interpreter</code> and calls the appropriate visit method
     * depending on the type of this QOM node.
     *
     * @param interpreter the interpreter.
     */
    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        if (interpreter ==null){
            return;
        }
        interpreter.accept(this);

        interpreter.accept((SelectorImpl)this.source);
        if (this.orderings != null){
            for (int i=0; i<this.orderings.length; i++){
                ((OrderingImpl)this.orderings[i]).accept(interpreter);
            }
        }
        if (this.constraint != null){
            ((ConstraintImpl)this.constraint).accept(interpreter);
        }
    }

    /**
     * Gets the columns for this query.
     *
     * @return an array of zero or more columns; non-null
     */
    public Column[] getColumns() {
        //@todo not implemented yet.
        return null;
    }

    /**
     *
     * @return
     * @throws javax.jcr.RepositoryException
     */
    public QueryResult execute() throws javax.jcr.RepositoryException {
        if (queryExecute != null){
            return queryExecute.execute(this);
        }
        return null;
    }

    public String getStatement() {
        //@todo not implemented yet
        return "";
    }

    public String getLanguage(){
        //@todo not implemented yet
        return "";
    }

    public String getPersistentQueryPath() throws javax.jcr.ItemNotFoundException, javax.jcr.RepositoryException {
        //@todo not implemented yet
        return "";
    }

    public void save(java.lang.String s) throws javax.jcr.ItemExistsException, javax.jcr.PathNotFoundException, javax.jcr.version.VersionException, javax.jcr.nodetype.ConstraintViolationException, javax.jcr.lock.LockException, javax.jcr.UnsupportedRepositoryOperationException, javax.jcr.RepositoryException {
        //@todo not implemented yet
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public QueryObjectModelFactory getQueryFactory() {
        return queryFactory;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("constraint=").append(this.constraint);
        buffer.append(" orderings=");
        if ( orderings == null || orderings.length==0 ){
            buffer.append("null");
        } else {
            for (int i=0; i<orderings.length; i++){
                buffer.append(orderings[i].toString());
            }
        }
        buffer.append(" properties=").append(properties);
        return buffer.toString();
    }

    public String getStoredQueryPath() throws ItemNotFoundException,
            RepositoryException {
        throw new UnsupportedOperationException("Method org.jahia.query.qom.QueryObjectModelImpl.getStoredQueryPath() is not supported yet");
    }

    public Node storeAsNode(String arg0) throws ItemExistsException,
            PathNotFoundException, VersionException,
            ConstraintViolationException, LockException,
            UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedOperationException("Method org.jahia.query.qom.QueryObjectModelImpl.storeAsNode(String) is not supported yet");
    }

}

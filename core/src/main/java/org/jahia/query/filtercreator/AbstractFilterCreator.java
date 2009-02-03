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

package org.jahia.query.filtercreator;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Comparison;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.FullTextSearch;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;
import org.jahia.data.containers.ContainerFilterBean;
import org.jahia.data.containers.ContainerFilterInterface;
import org.jahia.data.containers.NumberFormats;
import org.jahia.data.fields.FieldTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.query.qom.JahiaQueryObjectModelConstants;
import org.jahia.services.containers.ContainerQueryContext;

/**
 * Abstract filter creator
 *
 * User: hollis
 * Date: 15 nov. 2007
 * Time: 16:49:35
 */
public abstract class AbstractFilterCreator implements FilterCreator {

    private String name;

    /**
     * Returns the filter creator name.
     * @return
     */
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a concrete Filter for the given TerminalConstraint
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    public ContainerFilterInterface getContainerFilter(Constraint c,
                                                       QueryObjectModel queryModel,
                                                       ContainerQueryContext queryContext,
                                                       ProcessingContext context)
    throws JahiaException {
        if ( c instanceof Comparison){
            return getFilter((Comparison)c,queryModel,queryContext,context);
        } else if ( c instanceof FullTextSearch){
            return getFilter((FullTextSearch)c,queryModel,queryContext,context);
        } else if ( c instanceof ChildNode){
            return getFilter((ChildNode)c,queryModel,queryContext,context);
        } else if ( c instanceof DescendantNode){
            return getFilter((DescendantNode)c,queryModel,queryContext,context);
        }
        return null;
    }

    /**
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public abstract ContainerFilterInterface getFilter(Comparison c, QueryObjectModel queryModel,
                                              ContainerQueryContext queryContext,
                                              ProcessingContext context) throws JahiaException;

    /**
     * Returns a concrete Range Query Filter.
     *
     * @param c1
     * @param c2
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public abstract ContainerFilterInterface getRangeQueryFilter(   Comparison c1,
                                                                    Comparison c2,
                                                                    QueryObjectModel queryModel,
                                                                    ContainerQueryContext queryContext,
                                                                    ProcessingContext context) throws JahiaException;

    /**
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public abstract ContainerFilterInterface getFilter(ChildNode c, QueryObjectModel queryModel,
                                              ContainerQueryContext queryContext,
                                              ProcessingContext context) throws JahiaException;


    /**
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public abstract ContainerFilterInterface getFilter(DescendantNode c, QueryObjectModel queryModel,
                                              ContainerQueryContext queryContext,
                                              ProcessingContext context) throws JahiaException;

    /**
     *
     * @param c
     * @param queryModel
     * @param queryContext
     * @param context
     * @return
     * @throws JahiaException
     */
    public abstract ContainerFilterInterface getFilter(FullTextSearch c, QueryObjectModel queryModel,
                                              ContainerQueryContext queryContext,
                                              ProcessingContext context) throws JahiaException;


    public static String getFilterOperatorFromQueryModelOperator( int queryModelOperator ) {
        if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_EQUAL_TO ){
            return ContainerFilterBean.COMP_EQUAL;
        }else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_NOT_EQUAL_TO ){
              return ContainerFilterBean.COMP_NOT_EQUAL;         
        } else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN ){
            return ContainerFilterBean.COMP_BIGGER;
        } else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_GREATER_THAN_OR_EQUAL_TO ){
            return ContainerFilterBean.COMP_BIGGER_OR_EQUAL;
        } else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN ){
            return ContainerFilterBean.COMP_SMALLER;
        } else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_LESS_THAN_OR_EQUAL_TO ){
            return ContainerFilterBean.COMP_SMALLER_OR_EQUAL;
        } else if ( queryModelOperator == JahiaQueryObjectModelConstants.OPERATOR_LIKE ){
            return ContainerFilterBean.COMP_STARTS_WITH;
        }
        return null;
    }

    public static boolean numberType(int fieldType){
        return ( fieldType == FieldTypes.FLOAT || fieldType == FieldTypes.INTEGER );
    }

    public static String numberFormat(int fieldType){
        if ( fieldType == FieldTypes.FLOAT  ){
            return NumberFormats.FLOAT_FORMAT;
        } else if ( fieldType == FieldTypes.INTEGER ){
            return NumberFormats.LONG_FORMAT;
        }
        return null;
    }

}
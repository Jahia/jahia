/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.query.dasl;

import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.search.CompareOperator;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 14:30:41
 * To change this template use File | Settings | File Templates.
 */
public class SearchBuilderTools {

    /**
     * create an expression with DALSCompareExpression
     * with namespace <code>Constants.DAV</code>
     * and not of date type
     *
     * @param expName
     * @param propLocalName
     * @param operator
     * @param value
     * @return
     */
    public static ExpressionType getExpressionType(String expName,
                                                   String propLocalName,
                                                   CompareOperator operator,
                                                   Object value){
        return getExpressionType(expName,propLocalName,Constants.DAV,operator,value,false);
    }


    public static ExpressionType getExpressionType(String expName,
                                                   String propLocalName,
                                                   String propNamespaceURI,
                                                   CompareOperator operator,
                                                   Object value,
                                                   boolean isDate){

        PropertyName propName = new PropertyName(propNamespaceURI,propLocalName);
        DASLCompareExpression exp = new DASLCompareExpression(operator,propName,value,isDate);
        return new ExpressionType(expName,exp);
    }

}

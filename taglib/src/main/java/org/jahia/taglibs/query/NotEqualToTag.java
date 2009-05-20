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
package org.jahia.taglibs.query;

import org.jahia.query.qom.JahiaQueryObjectModelConstants;

import javax.servlet.jsp.JspException;

/**
 * Tag used to create an NotImpl Equal To ConstraintImpl
 *
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class NotEqualToTag extends ComparisonTag  {

    public NotEqualToTag(){
        super();
        this.setOperator(JahiaQueryObjectModelConstants.OPERATOR_NOT_EQUAL_TO);
    }

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        this.setOperator(JahiaQueryObjectModelConstants.OPERATOR_NOT_EQUAL_TO);
        return eval;
    }

}
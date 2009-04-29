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

import javax.servlet.jsp.JspException;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.log4j.Logger;
import org.jahia.query.qom.FullTextSearchImpl;


/**
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 */
@SuppressWarnings("serial")
public class FullTextSearchTag extends ConstraintTag  {

    private static Logger logger =
        Logger.getLogger(FullTextSearchTag.class);

    private FullTextSearchImpl fullTextSearch;

    private String searchExpression;
    private String propertyName;
    private String isMetadata;

    public int doEndTag() throws JspException {
        int eval = super.doEndTag();
        fullTextSearch = null;
        propertyName = null;
        isMetadata= null;
        return eval;
    }

    public Constraint getConstraint(){
        if ( fullTextSearch != null ){
            return fullTextSearch;
        }
        if ( this.searchExpression == null || this.searchExpression.trim().equals("") ){
            return null;
        }
        try {
            fullTextSearch = (FullTextSearchImpl)this.getQueryFactory()
                    .fullTextSearch(this.propertyName,this.searchExpression);
            fullTextSearch.setMetadata("true".equals(this.isMetadata));
        } catch ( Exception e ){
            logger.warn(e.getMessage(), e);
        }
        return fullTextSearch;
    }

    public String getSearchExpression() {
        return searchExpression;
    }

    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getMetadata() {
        return isMetadata;
    }

    public void setMetadata(String metadata) {
        isMetadata = metadata;
    }

}
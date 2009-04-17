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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
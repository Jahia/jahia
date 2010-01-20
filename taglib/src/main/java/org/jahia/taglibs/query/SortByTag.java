/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.query;

import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Used to specify query sorting parameters.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:33:24
 */
public class SortByTag extends QueryDefinitionDependentTag {

    private static final long serialVersionUID = 7747723525104918964L;

    private static Logger logger = Logger.getLogger(SortByTag.class);

    private String propertyName;

    private String order;

    public int doStartTag() throws JspException {
        QueryDefinitionTag queryModelDefTag = getQueryDefinitionTag();
        try {
            if (queryModelDefTag == null || queryModelDefTag.getQueryFactory()==null) {
                return SKIP_BODY;
            }
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        }
        if (this.propertyName == null || this.propertyName.trim().equals("")){
            return EVAL_BODY_BUFFERED;
        }
        try {
            queryModelDefTag.addOrdering(getSelectorName(),getPropertyName(), getOrder());
        } catch ( Exception t ){
            logger.debug("Error creating ordering clause",t);
            throw new JspException("Error creating Ordering node in SortBy Tag",t);
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        propertyName = null;
        propertyName = null;
        order = null;
        return EVAL_PAGE;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.query;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Defines a column to include in the tabular view of query results.<br>
 * If propertyName is not specified, a column is included for each single-valued
 * non-residual property of the node type specified by the nodeType attribute of
 * the selector selectorName.<br>
 * If propertyName is specified, columnName is required and used to name the
 * column in the tabular results.<br>
 * If propertyName is not specified, columnName must not be specified, and the
 * included columns will be named "selectorName.propertyName".
 * 
 * @author Sergiy Shyrkov
 * 
 * @since 6.5
 */
public class ColumnTag extends QOMBuildingTag {

    private static final long serialVersionUID = -2970406105716054811L;

    private String columnName;

    private String propertyName;

    @Override
    public int doEndTag() throws JspException {
        try {
            try {
                getQOMBuilder().getColumns().add(getQOMFactory().column(getSelectorName(), propertyName, columnName));
            } catch (RepositoryException e) {
                throw new JspTagException(e);
            }
        } finally {
            resetState();
        }
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        columnName = null;
        propertyName = null;
        super.resetState();
    }

    /**
     * Sets the name of the column to include into results.
     * 
     * @param columnName the name of the column to include into results
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Sets the name of the property to include into results.
     * 
     * @param propertyName the name of the property to include into results
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

}

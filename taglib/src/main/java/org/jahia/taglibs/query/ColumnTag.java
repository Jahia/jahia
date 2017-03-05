/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

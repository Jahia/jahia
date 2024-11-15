/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

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
package org.jahia.query.qom;

import org.jahia.exceptions.JahiaException;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Column;

/**
 * Class implementing the JSR-283 QOM <code>Column</code> interface
 * 
 * User: hollis
 * Date: 13 f�vr. 2008
 * Time: 16:55:45
 * To change this template use File | Settings | File Templates.
 */
public class ColumnImpl extends QOMNode implements Column {

    private String selectorName;
    private String propertyName;
    private String columnName;

    /**
     * Gets the name of the selector.
     *
     * @return the selector name; non-null
     */
    public String getSelectorName() {
        return this.selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

    /**
     * Gets the name of the property.
     *
     * @return the property name, or null to include a column for
     *         each single-value non-residual property of the
     *         selector's node type
     */
    public String getPropertyName() {
        return this.propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the column name.
     * <p/>
     *
     * @return the column name; must be null if
     *         <code>getPropertyName</code> is null and non-null
     *         otherwise
     */
    public String getColumnName() {
        return this.columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

}

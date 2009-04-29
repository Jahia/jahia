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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.DescendantNode;

/**
 * <code>DescendantNodeImpl</code>...
 */
public class DescendantNodeImpl
        extends ConstraintImpl
        implements DescendantNode, Constraint {

    /**
     * A selector name.
     */
    private String selectorName;

    /**
     * An absolute path.
     */
    private String path;

    public DescendantNodeImpl(String selectorName, String path) {
        this.selectorName = selectorName;
        this.path = path;
    }

    /**
     * Gets the name of the selector against which to apply this constraint.
     *
     * @return the selector name; non-null
     */
    public String getSelectorName() {
        return selectorName;
    }

    /**
     * Gets the absolute path.
     *
     * @return the path; non-null
     */
    public String getPath() {
        return path;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

}
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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.ChildNode;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 14 f�vr. 2008
 * Time: 15:21:43
 * To change this template use File | Settings | File Templates.
 */
public class ChildNodeImpl extends ConstraintImpl implements ChildNode, Constraint {

    private String selectorName;
    private String path;

    public ChildNodeImpl(String selectorName, String path) {
        this.selectorName = selectorName;
        this.path = path;
    }

    /**
     * Gets the name of the selector against which to apply this constraint.
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
     * Gets the absolute path.
     *
     * @return the path; non-null
     */
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

}

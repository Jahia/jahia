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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Selector;

/**
 * Class implementing the JSR-238 QOM <code>Selector</code> interface
 *
 * User: hollis
 * Date: 13 f�vr. 2008
 * Time: 16:52:31
 * To change this template use File | Settings | File Templates.
 */
public class SelectorImpl extends QOMNode implements Selector {

    private String nodeTypeName;
    private String selectorName;

    public SelectorImpl(String nodeTypeName, String selectorName) {
        this.nodeTypeName = nodeTypeName;
        this.selectorName = selectorName;
    }

    /**
     * Gets the name of the required node type.
     *
     * @return the node type name; non-null
     */
    public String getNodeTypeName() {
        return this.nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    /**
     * Gets the selector name.
     * <p/>
     * A selector's name can be used elsewhere in the query to identify the
     * selector.
     *
     * @return the selector name; non-null
     */
    public String getSelectorName() {
        return this.selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

    public void accept(QueryObjectModelInterpreter interpreter) throws JahiaException {
        interpreter.accept(this);
    }

}

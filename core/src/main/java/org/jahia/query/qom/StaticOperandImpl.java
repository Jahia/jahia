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

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.StaticOperand;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 11:17:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class StaticOperandImpl extends QOMNode implements StaticOperand {

    /**
     * Return a multi valued String array. Values are comma separated.
     * Multi values should be separated by <code>JahiaQueryObjectModelConstants.MULTI_VALUE_SEP</code>
     *
     * @return
     */
    public abstract String[] getStringValues() throws JahiaException;

    /**
     * Return the value as String
     * @return
     */
    public abstract String getValueAsString() throws JahiaException;

    /**
     * Return true if the multi value separated should use AND logic for comparison.
     * If false , an OR logic should be used
     * @return
     */
    public abstract boolean isMultiValueANDLogic();



}

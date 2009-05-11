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

package org.jahia.taglibs;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Base class for tags, exposing any value into the page scope.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class ValueJahiaTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger
            .getLogger(ValueJahiaTag.class);

    /**
     * @deprecated use {@link #var} instead
     */
    private String valueID;

    private String var;

    /**
     * Returns the name of the page scope variable to expose the value under.
     * 
     * @return the name of the page scope variable to expose the value under
     * @deprecated use {@link #getVar()} instead
     */
    protected final String getValueID() {
        return valueID;
    }

    /**
     * Returns the name of the page scope variable to expose the value under.
     * 
     * @return the name of the page scope variable to expose the value under
     */
    public final String getVar() {
        return var;
    }

    @Override
    protected void resetState() {
        super.resetState();
        valueID = null;
        var = null;
    }

    /**
     * Sets the name of the page scope variable to expose the value under.
     * 
     * @param valueID
     *            the name of the page scope variable to expose the value under
     * @deprecated use {@link #setVar(String)} instead
     */
    public final void setValueID(String valueID) {
        if (logger.isDebugEnabled()) {
            logger.debug("The valueID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.",
                    new JspException());
        } else {
            logger.info("The valueID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use var attribute instead.");
        }
        this.valueID = valueID == null || valueID.length() == 0 ? null
                : valueID;
    }

    public final void setVar(String var) {
        this.var = var == null || var.length() == 0 ? null : var;
    }
}

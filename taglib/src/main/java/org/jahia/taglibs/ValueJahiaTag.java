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

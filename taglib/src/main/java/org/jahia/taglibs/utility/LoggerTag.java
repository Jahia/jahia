/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.utility;

import org.apache.log4j.Logger;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Simple tag in order to allow the template developper to output some information without having to wirte scriptlet
 * code in his/her jsp files.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class LoggerTag extends AbstractJahiaTag {

    public static final String LOGGER_NAME = "jsp.jahia.templates.Logger";
    private static final transient Logger logger = Logger.getLogger(LOGGER_NAME);

    private String level;
    private String value;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public int doStartTag() throws JspException {
        final String lowerCaseLevel = level.toLowerCase();

        if (lowerCaseLevel.equals("debug")) {
            logger.debug(value);

        } else if (lowerCaseLevel.equals("info")) {
            logger.info(value);

        } else if (lowerCaseLevel.equals("warn")) {
            logger.warn(value);

        } else if (lowerCaseLevel.equals("error")) {
            logger.error(value);

        } else if (lowerCaseLevel.equals("fatal")) {
            logger.fatal(value);

        } else {
            throw new JspTagException("Unknown value for attribute 'level' : " +
                    level + ". Allowed values are 'debug', 'info', 'warn', 'error' and 'fatal'");
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        level = null;
        value = null;
        return EVAL_PAGE;
    }

}

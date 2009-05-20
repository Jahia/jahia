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

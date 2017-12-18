/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.taglibs.utility;

import org.slf4j.Logger;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * Simple tag in order to allow the template developer to output some information without having to write scriptlet
 * code in his/her JSP files.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class LoggerTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger("jsp.jahia.templates.Logger");

    private String level;
    private String value;

    public void setLevel(String level) {
        this.level = level;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public int doStartTag() throws JspException {
        if (level == null) {
            logger.debug(value);
        } else {
        	level = level.toLowerCase();
        	if ("fatal".equals(level) || "error".equals(level)) {
        		logger.error(value);
        	} else if ("warn".equals(level)) {
        		logger.warn(value);
        	} else if ("info".equals(level)) {
        		logger.info(value);
        	} else if ("trace".equals(level)) {
        		logger.trace(value);
        	} else {
        		logger.debug(value);
        	}
        } 
        
        return SKIP_BODY;
    }

    public int doEndTag() {
        level = null;
        value = null;
        return EVAL_PAGE;
    }

}

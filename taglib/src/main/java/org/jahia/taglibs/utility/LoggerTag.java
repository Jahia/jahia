/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

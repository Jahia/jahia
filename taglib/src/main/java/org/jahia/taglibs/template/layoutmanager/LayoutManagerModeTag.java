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
package org.jahia.taglibs.template.layoutmanager;

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.jahia.taglibs.AbstractJahiaTag;
import org.apache.log4j.Logger;

/**
 * User: ktlili
 * Date: 19 nov. 2008
 * Time: 16:42:42
 */
@SuppressWarnings("serial")
public class LayoutManagerModeTag extends AbstractJahiaTag {
    private static final Logger logger = Logger.getLogger(LayoutManagerModeTag.class);
    private String value;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        try {
            //define area
            if (value != null) {
                out.print("<div mode='" + value + "' style='display:none;'>");
            }


        } catch (IOException e) {
            logger.error(e, e);
        }

        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final JspWriter out = pageContext.getOut();
        try {
            if (value != null) {
                out.print("\n</div>\n");

            }
            value = null;
        } catch (IOException e) {
            logger.error(e, e);
        }

        return SKIP_BODY;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

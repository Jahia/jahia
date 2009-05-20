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
package org.jahia.taglibs.template.include;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

/**
 * @author Thomas Draier
 */
@SuppressWarnings("serial")
public class IncludeContentTag extends TagSupport {

    private static transient final Logger logger = Logger.getLogger(IncludeContentTag.class);

    public int doStartTag() throws JspException {
        String includePage = (String) pageContext.getRequest().getAttribute("includedBody");
        try {
            pageContext.getOut().write(includePage);
        } catch (IOException e) {
            logger.error("Error in IncludeContentTag: " + e.getMessage(), e);
        }
        return Tag.SKIP_BODY;
    }
}

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

import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.ServletException;
import java.util.Map;
import java.io.IOException;

/**
 * @author Thomas Draier
 */
@SuppressWarnings("serial")
public class IncludeExtensionTag extends TagSupport {

    private static transient final Logger logger = Logger.getLogger(IncludeExtensionTag.class);

    private String type;

    public int doStartTag() throws JspException {
        Map<String, String> extensionsPages = (Map<String, String>) pageContext.getRequest().getAttribute("extensionsPages");
        if (extensionsPages.containsKey(type)) {
            try {
                pageContext.include(extensionsPages.get(type));
            } catch (ServletException e) {
                logger.error("Error in IncludeExtensionTag: " + e.getMessage(), e);
            } catch (IOException e) {
                logger.error("Error in IncludeExtensionTag: " + e.getMessage(), e);
            }
        }
        return Tag.SKIP_BODY;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

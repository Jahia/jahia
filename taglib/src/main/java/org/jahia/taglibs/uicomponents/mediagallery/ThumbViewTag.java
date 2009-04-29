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
package org.jahia.taglibs.uicomponents.mediagallery;

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import java.util.Random;

/**
 * a simple iterating body tag to display a list of image files in a webdav path
 */
@SuppressWarnings("serial")
public class ThumbViewTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ThumbViewTag.class);

    private String path;

    public void setPath(String path) {
        this.path = path;
    }

    public int doStartTag() throws JspException {
        final StringBuffer buf = new StringBuffer();
        try {
            if (path.length() > 0 && !path.equals("null")) {
                buf.append("<div ");
                if (cssClassName != null && cssClassName.length() > 0) {
                    buf.append("class=\"");
                    buf.append(cssClassName);
                    buf.append("\" ");
                }

                buf.append(JahiaType.JAHIA_TYPE);
                buf.append("=\"");
                buf.append(JahiaType.MEDIA_GALLERY);
                buf.append("\" ");
                buf.append("id=\"");
                buf.append(new Random().nextInt() + System.currentTimeMillis());
                buf.append("\" ");

                buf.append("path=\"");
                buf.append(path);
                buf.append("\" />");
                buf.append("</div>\n");

                pageContext.getOut().print(buf.toString());
            }
        } catch (final Exception e) {
            logger.error("Error in ThumbViewTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        path = null;
        return EVAL_PAGE;
    }
}


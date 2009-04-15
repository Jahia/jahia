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


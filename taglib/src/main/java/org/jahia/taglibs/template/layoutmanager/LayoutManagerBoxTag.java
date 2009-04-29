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

import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.taglibs.AbstractJahiaTag;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 21 nov. 2007
 * Time: 11:44:40
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class LayoutManagerBoxTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(LayoutManagerBoxTag.class);
    
    private String title;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        try {
            // begin box declaration
            out.print("<div jahiatype=\"" + JahiaType.LAYOUT_MANAGER_BOX + "\"");

            String id = getId();
            logger.debug("id: " + id);
            if (id != null) {
                out.print(" id=\"" + id + "\"");
            }

            logger.debug("css: " + cssClassName);
            if (cssClassName != null) {
                out.print(" class=\"" + cssClassName + "\"");
            }

            String title = getTitle();
            logger.debug("title: " + title);
            if (title != null) {
                out.print(" title=\"" + title + "\"");
            }
            out.print(">");
        } catch (IOException e) {
            logger.error(e, e);
        }

        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final JspWriter out = pageContext.getOut();
        try {
            // end box declaration
            out.print("</div>\n");

        } catch (IOException e) {
            logger.error(e, e);
        }

        return SKIP_BODY;
    }
}

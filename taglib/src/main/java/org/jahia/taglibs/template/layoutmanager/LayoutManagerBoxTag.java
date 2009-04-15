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

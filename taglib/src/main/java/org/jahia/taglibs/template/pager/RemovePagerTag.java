/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.pager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class RemovePagerTag extends TagSupport {
    private String prefix;

    private int pageSize;
    private long totalSize;

    private String id;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        Object value = pageContext.getAttribute("old_begin"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("begin", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_end"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("end", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_pageSize"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("pageSize", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_nbPages"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("nbPages", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_currentPage"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("currentPage", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_paginationActive"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("paginationActive", value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("old_totalSize"+id, PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("totalSize", value, PageContext.REQUEST_SCOPE);
        }
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }
}
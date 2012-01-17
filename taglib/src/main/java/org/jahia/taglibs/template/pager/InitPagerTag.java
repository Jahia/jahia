/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.pager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * 
 */
public class InitPagerTag extends TagSupport {
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
        try {
            Map<String,Object> moduleMap  = (HashMap<String,Object>)  pageContext.getRequest().getAttribute("moduleMap");
            if (moduleMap == null) {
                moduleMap = new HashMap<String,Object>();
            }
            Object value = moduleMap.get("begin");
            if (value != null) {
                moduleMap.put("old_begin"+id, value);
            }
            value = moduleMap.get("end");
            if (value != null) {
                moduleMap.put("old_end"+id, value);
            }
            value = moduleMap.get("pageSize");
            if (value != null) {
                moduleMap.put("old_pageSize"+id, value);
            }
            value = moduleMap.get("nbPages");
            if (value != null) {
                moduleMap.put("old_nbPages"+id, value);
            }
            value = moduleMap.get("currentPage");
            if (value != null) {
                moduleMap.put("old_currentPage"+id, value);
            }
            value = moduleMap.get("paginationActive");
            if (value != null) {
                moduleMap.put("old_paginationActive"+id, value);
            }
            value = moduleMap.get("totalSize");
            if (value != null) {
                moduleMap.put("old_totalSize"+id, value);
            }
            String beginStr = pageContext.getRequest().getParameter("begin"+id);
            String endStr = pageContext.getRequest().getParameter("end"+id);

            if(pageContext.getRequest().getParameter("pagesize"+id)!=null) {
                pageSize = Integer.parseInt(pageContext.getRequest().getParameter("pagesize"+id));
            }

            int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
            int end = endStr == null ? pageSize - 1 : Integer.parseInt(endStr);
            if(totalSize < pageSize) {
                begin = 0;
            }
            long nbPages = totalSize / pageSize;
            if (nbPages * pageSize < totalSize) {
                nbPages++;
            }
            moduleMap.put("begin", begin);
            moduleMap.put("end", end);
            moduleMap.put("pageSize", pageSize);
            moduleMap.put("nbPages", nbPages);
            moduleMap.put("currentPage", begin / pageSize + 1);
            moduleMap.put("paginationActive", true);
            moduleMap.put("totalSize", totalSize);
            pageContext.setAttribute("moduleMap",moduleMap);
            pageContext.setAttribute("begin_"+id,begin,PageContext.REQUEST_SCOPE);
            pageContext.setAttribute("end_"+id,end,PageContext.REQUEST_SCOPE);
        } catch (Exception e) {
            throw new JspException(e);
        }        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }
}

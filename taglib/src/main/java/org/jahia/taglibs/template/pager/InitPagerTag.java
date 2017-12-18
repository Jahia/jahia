/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.taglibs.template.pager;

import org.apache.commons.lang.StringEscapeUtils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import java.util.*;

/**
 * 
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * 
 */
public class InitPagerTag extends TagSupport {
    private static final long serialVersionUID = 3487375821225747403L;

    private int pageSize;
    private long totalSize;
    private boolean sizeNotExact = false;
    private final static int DEFAULT_PAGE_SIZE=10;

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
            @SuppressWarnings("unchecked")
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
            String beginStr = StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("begin"+id));
            String endStr = StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("end"+id));

            if (pageContext.getRequest().getParameter("pagesize"+id) != null) {
                pageSize = Integer.parseInt(StringEscapeUtils.escapeXml(pageContext.getRequest().getParameter("pagesize"+id)));
            }

            //To escape dividing by zero we test if the page size is equal to zero if yes we put it to a default pagesize value
            if(pageSize== 0)
            {
                pageSize = DEFAULT_PAGE_SIZE;
            }

            int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
            int end = endStr == null ? pageSize - 1 : Integer.parseInt(endStr);

            int currentPage = begin / pageSize + 1;

            long nbPages = totalSize / pageSize;
            if (nbPages * pageSize < totalSize) {
                nbPages++;
            }
            if (totalSize == Integer.MAX_VALUE) {
                nbPages = currentPage;// + 1;
            }

            if (totalSize < pageSize) {
                begin = 0;
            } else if (begin > totalSize) {
                begin = (int) ((nbPages-1) * pageSize);
                end = begin + pageSize - 1;
            }
            
            if (currentPage > nbPages) {
                currentPage = (int)nbPages;
            }

            moduleMap.put("begin", begin);
            moduleMap.put("end", end);
            moduleMap.put("pageSize", pageSize);
            moduleMap.put("nbPages", nbPages);
            moduleMap.put("currentPage", currentPage);
            moduleMap.put("paginationActive", true);
            moduleMap.put("totalSize", totalSize);
            moduleMap.put("sizeNotExact", sizeNotExact);            
            moduleMap.put("totalSizeUnknown", totalSize == Integer.MAX_VALUE);
            pageContext.setAttribute("moduleMap",moduleMap);
            pageContext.setAttribute("begin_"+id,begin,PageContext.REQUEST_SCOPE);
            pageContext.setAttribute("end_"+id,end,PageContext.REQUEST_SCOPE);

            moduleMap.put("requestAttributesToCache", Arrays.asList("begin_"+id, "end_"+id));
        } catch (Exception e) {
            throw new JspException(e);
        }        
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSizeNotExact() {
        return sizeNotExact;
    }

    public void setSizeNotExact(boolean sizeNotExact) {
        this.sizeNotExact = sizeNotExact;
    }

    @Override
    public void release() {
        super.release();
        id = null;
        pageSize = 0;
        sizeNotExact = false;
        totalSize = 0;
    }
}

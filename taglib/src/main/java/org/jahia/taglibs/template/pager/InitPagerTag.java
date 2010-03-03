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
        Object value = pageContext.getAttribute("begin", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_begin"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("end", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_end"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("pageSize", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_pageSize"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("nbPages", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_nbPages"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("currentPage", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_currentPage"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("paginationActive", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_paginationActive"+id, value, PageContext.REQUEST_SCOPE);
        }
        value = pageContext.getAttribute("totalSize", PageContext.REQUEST_SCOPE);
        if (value != null) {
            pageContext.setAttribute("old_totalSize"+id, value, PageContext.REQUEST_SCOPE);
        }
        String beginStr = pageContext.getRequest().getParameter("begin");
        String endStr = pageContext.getRequest().getParameter("end");

        int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
        int end = endStr == null ? pageSize - 1 : Integer.parseInt(endStr);

        long nbPages = totalSize / pageSize;
        if (nbPages * pageSize < totalSize) {
            nbPages++;
        }
        pageContext.setAttribute("begin", begin, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("end", end, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("pageSize", pageSize, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("nbPages", nbPages, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("currentPage", begin / pageSize + 1, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("paginationActive", true, PageContext.REQUEST_SCOPE);
        pageContext.setAttribute("totalSize", totalSize, PageContext.REQUEST_SCOPE);
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }
}

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
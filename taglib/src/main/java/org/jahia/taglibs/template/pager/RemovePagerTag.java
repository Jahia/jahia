package org.jahia.taglibs.template.pager;

import javax.jcr.RangeIterator;
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

    private Object items;

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
        pageContext.removeAttribute("begin", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("end", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("pageSize", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("nbPages", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("currentPage", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("paginationActive", PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("totalSize", PageContext.REQUEST_SCOPE);
        return super.doEndTag();
    }

    public void setItems(Object items) {
        this.items = items;
    }
}
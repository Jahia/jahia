package org.jahia.taglibs.template.pager;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

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
    private int totalSize;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public int doStartTag() throws JspException {
        String beginStr = pageContext.getRequest().getParameter("begin");
        String endStr = pageContext.getRequest().getParameter("end");

        int begin = beginStr == null ? 0 : Integer.parseInt(beginStr);
        int end = endStr == null ? pageSize-1 : Integer.parseInt(endStr);

        int nbPages = totalSize / pageSize;
        if (nbPages * pageSize < totalSize) {
            nbPages ++;
        }
        pageContext.setAttribute("begin", begin);
        pageContext.setAttribute("end", end);
        pageContext.setAttribute("pageSize", pageSize);
        pageContext.setAttribute("nbPages", nbPages);
        pageContext.setAttribute("currentPage", begin/pageSize + 1);

        return super.doStartTag();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int doEndTag() throws JspException {
        return super.doEndTag();    //To change body of overridden methods use File | Settings | File Templates.
    }
}

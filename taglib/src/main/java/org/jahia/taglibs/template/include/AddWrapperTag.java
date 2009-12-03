package org.jahia.taglibs.template.include;

import org.jahia.services.render.Resource;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

/**
 * Add a wrapper to the current rendering
 */
public class AddWrapperTag extends TagSupport {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int doStartTag() throws JspException {
        ((Resource)pageContext.getRequest().getAttribute("currentResource")).pushWrapper(name);
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        name = null;
        return super.doEndTag();
    }
}

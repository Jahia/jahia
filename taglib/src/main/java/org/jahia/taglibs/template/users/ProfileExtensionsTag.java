package org.jahia.taglibs.template.users;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.util.Map;
import java.util.HashMap;

/**
 * Get the list of profile extensions
 *
 */
public class ProfileExtensionsTag extends TagSupport {
    private String var;

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        //todo do something better
        Map m = new HashMap();
        m.put("tasklist", "Task list");
        pageContext.setAttribute(var, m.entrySet());
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        var = null;
        return super.doEndTag();
    }
}

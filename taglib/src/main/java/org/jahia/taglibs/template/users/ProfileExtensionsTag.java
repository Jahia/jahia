package org.jahia.taglibs.template.users;

import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.utils.profile.ProfileExtensions;

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
        ProfileExtensions profileExtensions = (ProfileExtensions) SpringContextSingleton.getInstance().getContext().getBean("profileExtensions");
        pageContext.setAttribute(var, profileExtensions.getProfileExtensions().entrySet());
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        var = null;
        return super.doEndTag();
    }
}

package org.jahia.taglibs.utility;

import org.jahia.data.beans.JahiaBean;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 27, 2009
 * Time: 6:19:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResolvePathTag extends TagSupport {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int doStartTag() throws JspException {
        JahiaBean jahiaBean = (JahiaBean) pageContext.getAttribute("jahia");
        try {
            this.pageContext.getOut().write(jahiaBean.getIncludes().getWebPath().get(value));
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY; 
    }
}

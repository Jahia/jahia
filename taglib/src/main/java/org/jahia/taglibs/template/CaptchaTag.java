package org.jahia.taglibs.template;

import org.jahia.services.render.URLGenerator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1/14/11
 * Time: 16:05
 * To change this template use File | Settings | File Templates.
 */
public class CaptchaTag extends TagSupport {

    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();

        try {
            String id = (String) pageContext.getAttribute("currentFormId");

            out.print("<img src=\"");
            out.print(((URLGenerator)pageContext.findAttribute("url")).getContext());
            out.print(((URLGenerator)pageContext.findAttribute("url")).getCaptcha());
            out.print("?token=##formtoken("+id+")##\" />" );

            TokenizedFormTag formTag = (TokenizedFormTag) findAncestorWithClass(this, TokenizedFormTag.class);
            formTag.setHasCaptcha(true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return EVAL_PAGE;
    }
}

package org.jahia.taglibs.template.include;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 27, 2009
 * Time: 6:07:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportTag extends BodyTagSupport {

    /**
     * Default processing of the start tag returning EVAL_BODY_BUFFERED.
     *
     * @return EVAL_BODY_BUFFERED
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.BodyTag#doStartTag
     */
    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        ModuleTag module = (ModuleTag) TagSupport.findAncestorWithClass(this, ModuleTag.class);
        module.setImportString(getBodyContent().getString());
        return EVAL_PAGE;
    }
}

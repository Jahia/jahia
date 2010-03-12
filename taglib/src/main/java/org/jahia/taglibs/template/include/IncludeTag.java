package org.jahia.taglibs.template.include;

import org.jahia.services.content.JCRNodeWrapper;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 11, 2010
 * Time: 5:32:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class IncludeTag extends ModuleTag {

    @Override
    public int doStartTag() throws JspException {
        addParameter("forcedSkin","none");
        setNode((JCRNodeWrapper) pageContext.getAttribute("currentNode", PageContext.REQUEST_SCOPE));
        setEditable(false);
        setForcedTemplate(getTemplate());
        return super.doStartTag();
    }

}

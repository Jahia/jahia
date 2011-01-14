package org.jahia.taglibs.template;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringUtils;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1/13/11
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */

public class TokenizedFormTag extends BodyTagSupport {

    private boolean hasCaptcha = false;

    public void setHasCaptcha(boolean hasCaptcha) {
        this.hasCaptcha = hasCaptcha;
    }

    @Override
    public int doStartTag() throws JspException {
        String id = java.util.UUID.randomUUID().toString();
        pageContext.setAttribute("currentFormId", id);

        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        try {
            String id = (String) pageContext.getAttribute("currentFormId");

            JspWriter out = pageContext.getOut();

            String bodyContent = getBodyContent().getString();
            Source source = new Source(bodyContent);

            OutputDocument outputDocument = new OutputDocument(source);

            TreeMap<String,String> hiddenInputs = new TreeMap<String,String>();
            List<StartTag> formTags = source.getAllStartTags("form");

            StartTag formTag = formTags.get(0);
            String action = formTag.getAttributeValue("action");

            if (!action.startsWith("/") && !action.contains("://")) {
                action = StringUtils.substringBeforeLast(((HttpServletRequest)pageContext.getRequest()).getRequestURI(), "/")+ "/" +action;
            }
            hiddenInputs.put("form-action",action);

            List<StartTag> inputTags = source.getAllStartTags("input");
            for (StartTag inputTag : inputTags) {
                if ("hidden".equals(inputTag.getAttributeValue("type"))) {
                    hiddenInputs.put(inputTag.getAttributeValue("name"), inputTag.getAttributeValue("value"));
                }
            }

            if (hasCaptcha) {
                // Put random number here, will be replaced by the captcha servlet with the expected value
                hiddenInputs.put("captcha",java.util.UUID.randomUUID().toString());
            }

            outputDocument.insert(formTag.getEnd(), "<input type=\"hidden\" name=\"form-token\" value=\"##formtoken(\"" + id + "\")##\"/>");


            Map<String,Map<String,String>> forms = (Map<String, Map<String, String>>) pageContext.getAttribute("form-parameter", PageContext.REQUEST_SCOPE);
            if (forms == null) {
                forms = new HashMap<String, Map<String,String>>();
                pageContext.setAttribute("form-parameter", forms, PageContext.REQUEST_SCOPE);
            }
            forms.put(id, hiddenInputs);

            out.print(outputDocument.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        hasCaptcha = false;

        return super.doEndTag();
    }

    @Override
    public void doInitBody() throws JspException {
        super.doInitBody();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int doAfterBody() throws JspException {
        return super.doAfterBody();    //To change body of overridden methods use File | Settings | File Templates.
    }
}

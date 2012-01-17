/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.Resource;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.util.*;

/**
 * User: toto
 * Date: 1/13/11
 * Time: 15:11
 */

public class TokenizedFormTag extends BodyTagSupport {

    private static final long serialVersionUID = -1427914171244787502L;

    @Override
    public int doStartTag() throws JspException {
        String id = java.util.UUID.randomUUID().toString();

        pageContext.setAttribute("currentFormId", id,PageContext.REQUEST_SCOPE);

        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspException {
        boolean hasCaptcha = false;
        if (pageContext.findAttribute("hasCaptcha") != null) {
            hasCaptcha = (Boolean) pageContext.findAttribute("hasCaptcha");
        }
        try {
            String id = (String) pageContext.findAttribute("currentFormId");
            Resource currentResource = (Resource) pageContext.getAttribute("currentResource",
                                                                           PageContext.REQUEST_SCOPE);
            JspWriter out = pageContext.getOut();

            String bodyContent = getBodyContent().getString();
            Source source = new Source(bodyContent);

            OutputDocument outputDocument = new OutputDocument(source);

            TreeMap<String,List<String>> hiddenInputs = new TreeMap<String,List<String>>();
            List<StartTag> formTags = source.getAllStartTags("form");

            StartTag formTag = formTags.get(0);
            String action = formTag.getAttributeValue("action");

            if (!action.startsWith("/") && !action.contains("://")) {
                action = StringUtils.substringBeforeLast(((HttpServletRequest)pageContext.getRequest()).getRequestURI(), "/")+ "/" +action;
            }
            hiddenInputs.put("form-action",Arrays.asList(StringUtils.substringBeforeLast(action,";")));
            hiddenInputs.put("form-method", Arrays.asList(StringUtils.capitalize(formTag.getAttributeValue("method"))));

            List<StartTag> inputTags = source.getAllStartTags("input");
            for (StartTag inputTag : inputTags) {
                if ("hidden".equals(inputTag.getAttributeValue("type"))) {
                    String name = inputTag.getAttributeValue("name");
                    List<String> strings = hiddenInputs.get(name);
                    String value = inputTag.getAttributeValue("value");
                    if(strings==null) {
                        strings = new LinkedList<String>();
                    }
                    strings.add(value);
                    hiddenInputs.put(name, strings);
                }
            }

            if (hasCaptcha) {
                // Put random number here, will be replaced by the captcha servlet with the expected value
                hiddenInputs.put("jcrCaptcha",Arrays.asList(java.util.UUID.randomUUID().toString()));
            }

            outputDocument.insert(formTag.getEnd(), "<input type=\"hidden\" name=\"form-token\" value=\"##formtoken(" + id + ")##\"/>");


            @SuppressWarnings("unchecked")
            Map<String,Map<String,List<String>>> forms = (Map<String, Map<String, List<String>>>) pageContext.getAttribute("form-parameter", PageContext.REQUEST_SCOPE);
            if (forms == null) {
                forms = new HashMap<String, Map<String, List<String>>>();
                pageContext.setAttribute("form-parameter", forms, PageContext.REQUEST_SCOPE);
            }
            forms.put(id, hiddenInputs);
            currentResource.addFormInputs(id,hiddenInputs);
            out.print(outputDocument.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        pageContext.removeAttribute("hasCaptcha",PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("currentFormId",PageContext.REQUEST_SCOPE);
        return super.doEndTag();
    }

    @Override
    public void doInitBody() throws JspException {
        super.doInitBody();
    }

    @Override
    public int doAfterBody() throws JspException {
        return super.doAfterBody();
    }
}

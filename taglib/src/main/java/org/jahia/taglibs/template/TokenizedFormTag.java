/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template;

import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.noggit.JSONUtil;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.filter.AggregateFilter;
import org.slf4j.Logger;

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
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(TokenizedFormTag.class);

    private static final long serialVersionUID = -1427914171244787502L;

    private boolean disableXSSFiltering = false;
    private boolean allowsMultipleSubmits = false;
    private boolean skipAggregation = false;

    @Override
    public int doStartTag() throws JspException {
        String id = java.util.UUID.randomUUID().toString();
        pageContext.setAttribute("currentFormId", id,PageContext.REQUEST_SCOPE);

        // Skip the aggregation in the body of the tag
        if(!AggregateFilter.skipAggregation(pageContext.getRequest())) {
            skipAggregation = true;
            pageContext.getRequest().setAttribute(AggregateFilter.SKIP_AGGREGATION, true);
        }

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

            RenderContext renderContext = (RenderContext) pageContext.findAttribute("renderContext");
            JspWriter out = pageContext.getOut();

            String bodyContent = getBodyContent().getString();
            Source source = new Source(bodyContent);

            OutputDocument outputDocument = new OutputDocument(source);

            TreeMap<String,List<String>> hiddenInputs = new TreeMap<String,List<String>>();
            List<StartTag> formTags = source.getAllStartTags("form");

            StartTag formTag = formTags.get(0);
            String action = formTag.getAttributeValue("action");

            if (!action.startsWith("/") && !action.contains("://")) {
                String requestURI = ((HttpServletRequest)pageContext.getRequest()).getRequestURI();
                if (requestURI.startsWith("/gwt/")) {
                    requestURI = renderContext.getURLGenerator().buildURL(renderContext.getMainResource().getNode(), renderContext.getMainResourceLocale().toString(), renderContext.getMainResource().getTemplate(), renderContext.getMainResource().getTemplateType());
                }
                action = StringUtils.substringBeforeLast(requestURI, "/")+ "/" +action;
            }
            hiddenInputs.put("form-action", Collections.singletonList(StringEscapeUtils.escapeXml(StringUtils.substringBeforeLast(action, ";"))));
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

            hiddenInputs.put("disableXSSFiltering", Arrays.asList(String.valueOf(disableXSSFiltering)));
            hiddenInputs.put("allowsMultipleSubmits", Arrays.asList(String.valueOf(allowsMultipleSubmits)));
            outputDocument.insert(formTag.getEnd(), "<input type=\"hidden\" name=\"disableXSSFiltering\" value=\"" + disableXSSFiltering + "\"/>");

            outputDocument.insert(formTag.getEnd(),"<jahia:token-form id='"+id+"' forms-data='"+JSONUtil.toJSON(hiddenInputs)+"'/>");

            out.print(outputDocument.toString());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        pageContext.removeAttribute("hasCaptcha",PageContext.REQUEST_SCOPE);
        pageContext.removeAttribute("currentFormId",PageContext.REQUEST_SCOPE);

        // Restore the aggregation for the following fragments
        if (skipAggregation) {
            pageContext.getRequest().removeAttribute(AggregateFilter.SKIP_AGGREGATION);
            skipAggregation = false;
        }
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
    
    @Override
    public void release() {
        resetState();
        super.release();
    }

    protected void resetState() {
        disableXSSFiltering = false;
        allowsMultipleSubmits = false;
        skipAggregation = false;
    }

    public void setDisableXSSFiltering(boolean disableXSSFiltering) {
        this.disableXSSFiltering = disableXSSFiltering;
    }

    public void setAllowsMultipleSubmits(boolean allowsMultipleSubmits) {
        this.allowsMultipleSubmits = allowsMultipleSubmits;
    }
}

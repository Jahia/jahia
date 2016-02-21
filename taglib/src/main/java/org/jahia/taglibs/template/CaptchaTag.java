/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.URLGenerator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Generates an image element with a captcha for validation: <code><img src="captchaUrl" alt="captcha"/></code>
 */
public class CaptchaTag extends TagSupport {

    private static final String DEF_VAR_VALUE = "captchaUrl";

    private static final long serialVersionUID = -1357051391573555914L;

    private boolean display = true;

    private boolean displayReloadLink = true;

    private String var = DEF_VAR_VALUE;

    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();

        try {
            URLGenerator urlGen = (URLGenerator) pageContext.findAttribute("url");
            StringBuilder url = new StringBuilder();
            String formId = (String) pageContext.findAttribute("currentFormId");
            url.append(urlGen.getContext()).append(urlGen.getCaptcha())
                    .append("?token=##formtoken(").append(formId).append(")##");

            if (StringUtils.isNotEmpty(var)) {
                pageContext.setAttribute(var, url);
            }
            if (display) {
                out.append("<img id=\"jahia-captcha-").append(formId)
                        .append("\" alt=\"captcha\" src=\"").append(url).append("\" />");
            }
            if (displayReloadLink) {
                out.append("&nbsp;")
                        .append("<a href=\"#reload-captcha\" onclick=\"var captcha=document.getElementById('jahia-captcha-")
                        .append(formId)
                        .append("'); var captchaUrl=captcha.src; if (captchaUrl.indexOf('&amp;tst=') != -1){"
                                + "captchaUrl=captchaUrl.substring(0,captchaUrl.indexOf('&amp;tst='));}"
                                + "captchaUrl=captchaUrl+'&amp;tst='+new Date().getTime();"
                                + " captcha.src=captchaUrl; return false;\"><img src=\"")
                        .append(urlGen.getContext())
                        .append("/icons/refresh.png\" alt=\"refresh\"/></a>");
            }

            pageContext.setAttribute("hasCaptcha", true, PageContext.REQUEST_SCOPE);
        } catch (IOException e) {
            throw new JspException(e);
        }

        return EVAL_PAGE;
    }

    protected String getVar() {
        return var;
    }

    protected boolean isDisplay() {
        return display;
    }

    protected boolean isDisplayReloadLink() {
        return displayReloadLink;
    }

    @Override
    public void release() {
        resetState();
        super.release();
    }

    protected void resetState() {
        display = true;
        displayReloadLink = true;
        var = DEF_VAR_VALUE;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setDisplayReloadLink(boolean displayReloadLink) {
        this.displayReloadLink = displayReloadLink;
    }

    public void setVar(String var) {
        this.var = var;
    }
}

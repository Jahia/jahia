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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.URLGenerator;

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
                        .append("'); var captchaUrl=captcha.src; if (captchaUrl.indexOf('&tst=') != -1){"
                                + "captchaUrl=captchaUrl.substring(0,captchaUrl.indexOf('&tst='));}"
                                + "captchaUrl=captchaUrl+'&tst='+new Date().getTime();"
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

/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.URLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates an image element with a captcha for validation: <code><img src="captchaUrl" alt="captcha"/></code>
 */
public class CaptchaTag extends TagSupport {

    private static Logger logger = LoggerFactory.getLogger(CaptchaTag.class);

    private static final String DEF_VAR_VALUE = "captchaUrl";

    private static final long serialVersionUID = -1357051391573555914L;

    private boolean display = true;

    private boolean displayReloadLink = true;

    private String var = DEF_VAR_VALUE;

    @Override
    public int doEndTag() throws JspException {
        JspWriter out = pageContext.getOut();

        if (logger.isDebugEnabled()) {
            try {
                out.append("<script>console.warn('This captcha implementation has been removed. We recommend using alternatives such as Google reCaptcha, more details can be found here: https://academy.jahia.com/how-to-use-recaptcha-with-jahia')</script>");
            } catch (IOException e) {
                throw new JspException(e);
            }
        }

        return EVAL_PAGE;
    }

    protected String getVar() {
        return var;
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

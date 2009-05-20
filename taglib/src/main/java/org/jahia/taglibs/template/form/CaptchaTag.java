/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.form;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 1, 2009
 * Time: 6:29:11 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class CaptchaTag extends AbstractJahiaTag {

    @Override
    public int doStartTag() throws JspException {
        ServletRequest request = pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        try {
            JspWriter out = pageContext.getOut();

            ContainerFormTag tag = (ContainerFormTag) findAncestorWithClass(this, ContainerFormTag.class, pageContext.getRequest());

            String s = jData.getProcessingContext().getContextPath();
            if (!s.endsWith("/")) {
                s += "/";
            }
            out.write("<img src=\"" + s +"jcaptcha\"/>");
            tag.setHasCaptcha(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return SKIP_BODY;
    }
}

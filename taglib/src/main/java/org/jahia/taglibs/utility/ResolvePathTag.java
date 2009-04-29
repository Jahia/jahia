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
package org.jahia.taglibs.utility;

import org.jahia.data.beans.JahiaBean;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 27, 2009
 * Time: 6:19:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResolvePathTag extends TagSupport {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int doStartTag() throws JspException {
        JahiaBean jahiaBean = (JahiaBean) pageContext.getRequest().getAttribute("jahia");
        try {
            this.pageContext.getOut().write(jahiaBean.getIncludes().getWebPath().get(value));
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY; 
    }
}

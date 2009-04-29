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
package org.jahia.taglibs.internal.gwt;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * Generates a script element for loading the GWT module.
 *
 * @author Khaled Tlili
 */
@SuppressWarnings("serial")
public class GWTImportTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(GWTInitTag.class);

    private String module;

    public int doStartTag() {
        try {
            pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
            pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, getModule()));
        } catch (IOException e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Override
    public int doEndTag() throws JspException {
        super.doEndTag();
        module = null;
        return EVAL_PAGE;
    }
}

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

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.operations.valves.SkeletonAggregatorValve;
import org.jahia.ajax.gwt.utils.GWTInitializer;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Simple Tag that should be called in the header of the HTML page. It create a javascript object that
 * contains some jahia parameters in order to use the Google Web Toolkit.
 *
 * @author Khaled Tlili
 */
@SuppressWarnings("serial")
public class GWTInitTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(GWTInitTag.class);
    
    private String modules;

    private boolean standalone = false ;

    public void setStandalone(boolean value) {
        standalone = value ;
    }

    /**
     * Create a javascript object that
     * contains some jahia parameters  in order to use the Google Web Toolkit.<br>
     * Usage example inside a JSP:<br>
     * <!--
     * <%@ page language="java" contentType="text/html;charset=UTF-8" %>
     * <html>
     * <head>
     * <ajax:initGWT/>
     * </head>
     * -->
     * ...
     *
     * @return SKIP_BODY
     */
    public int doStartTag() {
        try {
            final JspWriter out = pageContext.getOut();
            out.print("<!-- cache:vars var=\""+ SkeletonAggregatorValve.GWT_VARIABLE+"\" -->");
            out.print(GWTInitializer.getInitString(pageContext, standalone)) ;
            out.print("<!-- /cache:vars -->");

            if (StringUtils.isNotEmpty(modules)) {
                for (String module : StringUtils.split(modules, ',')) {
                    out.append(GWTIncluder.generateGWTImport(pageContext, module.trim()));
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public void setModules(String modules) {
        this.modules = modules;
    }
}

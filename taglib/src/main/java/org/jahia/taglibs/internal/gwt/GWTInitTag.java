/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

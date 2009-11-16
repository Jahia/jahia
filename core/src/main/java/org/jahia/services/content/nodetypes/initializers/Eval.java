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
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.SiteBean;
import org.jahia.data.beans.JahiaBean;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.ExpressionEvaluator;
import java.util.List;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:49:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class Eval implements ValueInitializer {
    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        if (jParams != null && jParams instanceof ParamBean && Jahia.getJahiaServlet()!= null) {
            ParamBean paramBean = (ParamBean) jParams;
            JspFactory jspFactory = JspFactory.getDefaultFactory();
            final PageContext pageContext = jspFactory.getPageContext(Jahia.getJahiaServlet(),
                                                                      paramBean.getRealRequest(), paramBean.getRealResponse(),
                                                                      null, true, 8192, false);
            pageContext.setAttribute("currentUser",jParams.getUser());
            pageContext.setAttribute("currentPage",new PageBean(paramBean.getPage(),paramBean));
            pageContext.setAttribute("currentSite",new SiteBean(paramBean.getSite(),paramBean));
            pageContext.setAttribute("currentJahia",new JahiaBean(paramBean));
            try {
                final ExpressionEvaluator evaluator = pageContext.getExpressionEvaluator();

                if (evaluator != null) {
                    final Object o = evaluator.evaluate("${" + params.get(0) + "}", Object.class,
                                                        pageContext.getVariableResolver(), new FunctionMapper() {
                                public Method resolveFunction(String prefix, String localName) {
                                    return null;
                                }
                            });
                    if (o != null) {
                        String s = o.toString();
                        return new Value[]{new ValueImpl(s, PropertyType.STRING,false)};
                    }
                }
            } catch (ELException e) {
                e.printStackTrace();
            }
        }
        return new Value[0];
    }
}

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
        if (jParams != null && jParams instanceof ParamBean) {
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

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.webflow;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.mvc.view.AbstractMvcView;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;

/**
 * Creates bundle views
 */
public class BundleMvcViewFactory extends AbstractMvcViewFactory {
    private JahiaTemplatesPackage module;

    /**
     * Creates a new servlet-based MVC view factory.
     * @param viewId the id of the view as an expression
     * @param viewResolver the resolver to resolve the View implementation
     * @param expressionParser the expression parser
     * @param conversionService the conversion service
     * @param binderConfiguration the model binding configuration
     */
    public BundleMvcViewFactory(Expression viewId, FlowViewResolver viewResolver, ExpressionParser expressionParser,
                                 ConversionService conversionService, BinderConfiguration binderConfiguration,
                                 MessageCodesResolver messageCodesResolver, JahiaTemplatesPackage module) {
        super(viewId, viewResolver, expressionParser, conversionService, binderConfiguration, messageCodesResolver);
        this.module = module;
    }

    protected AbstractMvcView createMvcView(View view, RequestContext context) {
        if (view instanceof JstlView) {
            ((JstlView)view).setServletContext(JahiaContextLoaderListener.getServletContext());
        }
        if (view instanceof InternalResourceView) {
            ((InternalResourceView) view).setUrl("/modules/" + module.getId()
                    + ((InternalResourceView) view).getUrl());
        }

        return new BundleMvcView(view, context);
    }

}

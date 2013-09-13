/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
            ((InternalResourceView)view).setUrl("/modules/"+module.getRootFolder() + "/" +((InternalResourceView)view).getUrl());
        }

        return new BundleMvcView(view, context);
    }

}

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

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
import org.springframework.webflow.mvc.servlet.ServletMvcView;
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

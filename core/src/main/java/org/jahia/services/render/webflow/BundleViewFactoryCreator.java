package org.jahia.services.render.webflow;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.mvc.builder.MvcViewFactoryCreator;
import org.springframework.webflow.mvc.view.AbstractMvcViewFactory;
import org.springframework.webflow.mvc.view.FlowViewResolver;
import org.springframework.webflow.validation.WebFlowMessageCodesResolver;

/**
 * Creates factory view for bundle views
 */
public class BundleViewFactoryCreator extends MvcViewFactoryCreator implements JahiaModuleAware {
    private JahiaTemplatesPackage module;

    private FlowViewResolver flowViewResolver = new BundleFlowViewResolver();

    private MessageCodesResolver messageCodesResolver = new WebFlowMessageCodesResolver();

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    protected AbstractMvcViewFactory createMvcViewFactory(Expression viewId, ExpressionParser expressionParser,
                                                          ConversionService conversionService,
                                                          BinderConfiguration binderConfiguration) {
        return new BundleMvcViewFactory(viewId, flowViewResolver, expressionParser, conversionService,
                binderConfiguration, null, module);
    }

}

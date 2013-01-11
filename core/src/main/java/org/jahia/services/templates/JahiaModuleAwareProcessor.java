package org.jahia.services.templates;


import org.jahia.data.templates.JahiaTemplatesPackage;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;


public class JahiaModuleAwareProcessor implements BeanPostProcessor {
    private JahiaTemplatesPackage module;

    public JahiaModuleAwareProcessor(JahiaTemplatesPackage module) {
        this.module = module;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (this.module != null && bean instanceof JahiaModuleAware) {
            ((JahiaModuleAware) bean).setJahiaModule(module);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}

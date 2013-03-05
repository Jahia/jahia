package org.jahia.services.render.webflow;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaModuleAware;
import org.springframework.binding.convert.ConversionException;
import org.springframework.binding.convert.ConversionExecutor;
import org.springframework.binding.convert.ConversionExecutorNotFoundException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.service.GenericConversionService;

public class JahiaFlowConversionService extends GenericConversionService implements ConversionService, JahiaModuleAware {
    private JahiaTemplatesPackage module;

    @Override
    public void setJahiaModule(JahiaTemplatesPackage module) {
        this.module = module;
    }

    @Override
    public Class getClassForAlias(String alias) {
        // Get the class from the module classloader
        try {
            Class c = module.getChainedClassLoader().loadClass(alias);
            if (c != null) {
                return c;
            }
        } catch (ClassNotFoundException e) {
        }

        return super.getClassForAlias(alias);
    }
}

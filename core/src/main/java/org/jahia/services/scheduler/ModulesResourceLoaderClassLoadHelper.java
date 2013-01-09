package org.jahia.services.scheduler;


import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModulesResourceLoaderClassLoadHelper extends ResourceLoaderClassLoadHelper {
    private Map<String, String> moduleForClass = new HashMap<String, String>();

    @Override
    public Class loadClass(String name) throws ClassNotFoundException {
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

        if (moduleForClass.containsKey(name)) {
            return templateManagerService.getTemplatePackageByFileName(moduleForClass.get(name)).getClassLoader().loadClass(name);
        }

        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            List<JahiaTemplatesPackage> packageList = templateManagerService.getAvailableTemplatePackages();
            for (JahiaTemplatesPackage aPackage : packageList) {
                try {
                    if (aPackage.getClassLoader() != null) {
                        Class clazz = aPackage.getClassLoader().loadClass(name);
                        if (clazz != null) {
                            moduleForClass.put(name, aPackage.getRootFolder());
                            return clazz;
                        }
                    }
                } catch (ClassNotFoundException e1) {
                    // Continue
                }
            }
        }

        throw new ClassNotFoundException(name);
    }
}

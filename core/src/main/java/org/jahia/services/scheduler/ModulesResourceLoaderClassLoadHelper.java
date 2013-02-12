package org.jahia.services.scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;

public class ModulesResourceLoaderClassLoadHelper extends ResourceLoaderClassLoadHelper {
    private Map<String, String> moduleForClass = new ConcurrentHashMap<String, String>();

    private JahiaTemplateManagerService service;

    private JahiaTemplateManagerService getService() {
        if (service == null) {
            service = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        }

        return service;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        String module = moduleForClass.get(name);
        if (module != null) {
            JahiaTemplatesPackage pkg = getService().getTemplatePackageByFileName(module);
            if (pkg != null && pkg.getClassLoader() != null) {
                return pkg.getClassLoader().loadClass(name);
            } else {
                moduleForClass.remove(name);
            }
        }

        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            List<JahiaTemplatesPackage> packageList = getService().getAvailableTemplatePackages();
            for (JahiaTemplatesPackage aPackage : packageList) {
                try {
                    if (aPackage.getClassLoader() != null) {
                        Class<?> clazz = aPackage.getClassLoader().loadClass(name);
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

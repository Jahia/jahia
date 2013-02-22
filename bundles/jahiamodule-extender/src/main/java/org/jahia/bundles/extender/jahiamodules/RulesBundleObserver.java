package org.jahia.bundles.extender.jahiamodules;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * A bundle observer for Jahia Modules. Used by the BundleWatcher defined in the activator.
 */
public class RulesBundleObserver implements BundleObserver<URL> {

    private static Logger logger = LoggerFactory.getLogger(RulesBundleObserver.class);

    private TemplatePackageRegistry templatePackageRegistry;

    public RulesBundleObserver() {
        super();
        templatePackageRegistry = ((JahiaTemplateManagerService) SpringContextSingleton
                .getBean("JahiaTemplateManagerService")).getTemplatePackageRegistry();
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        if (urls.size() == 0) {
            return;
        }
        String bundleName = BundleUtils.getDisplayName(bundle);
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {

                JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);

                if (url.toString().endsWith(".dsl")) {
                    module.setRulesDescriptorFile(bundleResource.getURL().getPath().substring(1));

                    for (RulesListener listener : RulesListener.getInstances()) {
                        listener.addRulesDescriptor(bundleResource);
                    }
                }

                if (url.toString().endsWith(".drl")) {
                    module.setRulesFile(bundleResource.getURL().getPath().substring(1));

                    for (RulesListener listener : RulesListener.getInstances()) {
                        List<String> filesAccepted = listener.getFilesAccepted();
                        if(filesAccepted.contains(StringUtils.substringAfterLast(url.getPath(), "/"))) {
                            listener.addRules(bundleResource, module);
                        }
                    }
                }

                logger.info("Registered rules from file {} for bundle {}", url, bundleName);
            } catch (IOException e) {
                logger.error("Error registering rules file " + url + " for bundle " + bundle, e);
            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);
        for (RulesListener listener : RulesListener.getInstances()) {
            listener.removeRules(module.getName());
        }
    }
}

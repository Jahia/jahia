package org.jahia.bundles.extender.jahiamodules;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.templates.ModuleVersion;
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
public class CndBundleObserver implements BundleObserver<URL> {

    private static Logger logger = LoggerFactory.getLogger(CndBundleObserver.class);

    private JCRStoreService jcrStoreService = null;
    private TemplatePackageRegistry templatePackageRegistry;

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry templatePackageRegistry) {
        this.templatePackageRegistry = templatePackageRegistry;
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {
                JahiaTemplatesPackage module = templatePackageRegistry.lookupByBundle(bundle);
                module.setDefinitionsFile(bundleResource.getURL().getPath().substring(1));
                NodeTypeRegistry.getInstance().addDefinitionsFile(bundleResource, bundle.getSymbolicName(), module.getVersion());
                jcrStoreService.deployDefinitions(bundle.getSymbolicName());
                logger.info("Registered definitions from file " + url + " for bundle " + bundle);
            } catch (IOException e) {
                logger.error("Error registering node type definition file " + url + " for bundle " + bundle, e);
            } catch (ParseException e) {
                logger.error("Error registering node type definition file " + url + " for bundle " + bundle, e);
            }
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        NodeTypeRegistry.getInstance().unregisterNodeTypes(bundle.getSymbolicName());
    }
}

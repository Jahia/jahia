package org.jahia.bundles.extender.jahiamodules;

import org.jahia.bundles.extender.jahiamodules.render.BundleScriptResolver;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.List;

/**
 * Bundle observer for all scripts (JSP, Velocity, Freemarker, etc...)
 */
public class ScriptBundleObserver implements BundleObserver<URL> {

    private BundleScriptResolver bundleScriptResolver = null;

    public ScriptBundleObserver(BundleScriptResolver bundleScriptResolver) {
        this.bundleScriptResolver = bundleScriptResolver;
    }

    public BundleScriptResolver getBundleScriptResolver() {
        return bundleScriptResolver;
    }

    @Override
    public void addingEntries(Bundle bundle, List<URL> urls) {
        for (URL url : urls) {
            bundleScriptResolver.addBundleScript(bundle, url);
        }
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        for (URL url : urls) {
            bundleScriptResolver.removeBundleScript(bundle, url);
        }
    }
}

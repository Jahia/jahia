package org.jahia.bundles.extender.jahiamodules;

import org.jahia.services.render.scripting.bundle.BundleScriptResolver;
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
        bundleScriptResolver.addBundleScripts(bundle, urls);
    }

    @Override
    public void removingEntries(Bundle bundle, List<URL> urls) {
        bundleScriptResolver.removeBundleScripts(bundle, urls);
    }
}

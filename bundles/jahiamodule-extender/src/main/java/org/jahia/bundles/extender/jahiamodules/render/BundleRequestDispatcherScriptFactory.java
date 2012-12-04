package org.jahia.bundles.extender.jahiamodules.render;

import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptFactory;

/**
 *
 */
public class BundleRequestDispatcherScriptFactory implements ScriptFactory {

    private BundleDispatcherServlet bundleDispatcherServlet = null;

    public BundleRequestDispatcherScriptFactory(BundleDispatcherServlet bundleDispatcherServlet) {
        this.bundleDispatcherServlet = bundleDispatcherServlet;
    }

    @Override
    public Script createScript(View view) {
        return new BundleRequestDispatcherScript((BundleView) view, bundleDispatcherServlet);
    }

    @Override
    public void initView(View view) {
    }
}

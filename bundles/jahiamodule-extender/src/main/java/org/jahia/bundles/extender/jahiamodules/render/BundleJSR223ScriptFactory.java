package org.jahia.bundles.extender.jahiamodules.render;

import org.jahia.services.render.View;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptFactory;

/**
 * Created with IntelliJ IDEA.
 * User: loom
 * Date: 06.11.12
 * Time: 15:24
 * To change this template use File | Settings | File Templates.
 */
public class BundleJSR223ScriptFactory implements ScriptFactory {
    @Override
    public Script createScript(View view) {
        return new BundleJSR223Script((BundleView) view);
    }

    @Override
    public void initView(View view) {
    }
}

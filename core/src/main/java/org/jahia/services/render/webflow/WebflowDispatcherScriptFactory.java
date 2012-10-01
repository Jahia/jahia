package org.jahia.services.render.webflow;

import org.jahia.services.render.View;
import org.jahia.services.render.scripting.RequestDispatcherScript;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptFactory;

public class WebflowDispatcherScriptFactory implements ScriptFactory {
    public Script createScript(View view) {
        return new WebflowDispatcherScript(view);
    }
}

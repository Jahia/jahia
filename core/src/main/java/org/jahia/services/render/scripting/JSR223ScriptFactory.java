package org.jahia.services.render.scripting;

import org.jahia.services.render.View;

public class JSR223ScriptFactory implements ScriptFactory {
    public Script createScript(View view) {
        return new JSR223Script(view);
    }
}

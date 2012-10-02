package org.jahia.services.render.scripting;

import org.jahia.services.render.View;

public class RequestDispatcherScriptFactory implements ScriptFactory {
    public Script createScript(View view) {
        return new RequestDispatcherScript(view);
    }

    public void initView(View view) {
    }
}

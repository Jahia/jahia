package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;

public class EditContentEnginePopupListener implements Listener<ComponentEvent> {
    private final Module module;

    public EditContentEnginePopupListener(Module simpleModule) {
        this.module = simpleModule;
    }

    public void handleEvent(ComponentEvent ce) {
        if (module.getNode().isWriteable()) {
            new EditContentEngine(module.getNode()).show();
        } else {
            Info.display("Rights Restriction", "You do not have rights to edit this content");
        }
    }
}
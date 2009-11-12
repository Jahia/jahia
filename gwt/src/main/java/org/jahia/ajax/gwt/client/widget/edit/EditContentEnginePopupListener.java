package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;

public class EditContentEnginePopupListener implements Listener<ComponentEvent> {
    private final Module module;
    private final EditLinker editLinker;

    public EditContentEnginePopupListener(Module module, EditLinker editLinker) {
        //To change body of created methods use File | Settings | File Templates.
        this.module = module;
        this.editLinker = editLinker;
    }

    public void handleEvent(ComponentEvent ce) {
        if (!module.isSelectable()) {
            return;
        }
        if (module.getNode().isWriteable()) {
            new EditContentEngine(module.getNode(),editLinker).show();
        } else {
            Info.display("Rights Restriction", "You do not have rights to edit this content");
        }
    }
}
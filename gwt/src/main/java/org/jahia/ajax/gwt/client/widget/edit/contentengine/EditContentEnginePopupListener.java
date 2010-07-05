package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

public class EditContentEnginePopupListener implements Listener<ComponentEvent> {
    private final Module module;
    private final EditLinker editLinker;

    public EditContentEnginePopupListener(Module module, EditLinker editLinker) {
        
        this.module = module;
        this.editLinker = editLinker;
    }

    public void handleEvent(ComponentEvent ce) {
        if (!module.isSelectable()) {
            return;
        }
        if (module.getNode().isLocked()) {
            Info.display("Lock", "This module is currently locked");
        } else {
            EngineLoader.showEditEngine(editLinker, module.getNode());
        }
    }
}
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineContainer;
import org.jahia.ajax.gwt.client.widget.contentengine.EnginePanel;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineWindow;
import org.jahia.ajax.gwt.client.widget.trash.TrashboardEngine;

/**
 * Open the trashboard engine
 */
public class ShowTrashboardActionItem extends BaseActionItem{


    @Override
    public void onComponentSelection() {
        EngineContainer container;
        if (linker instanceof ManagerLinker) {
            container = new EngineWindow();
        } else {
            container = new EnginePanel();
        }
        new TrashboardEngine(linker, container);
        container.showEngine();
    }

}

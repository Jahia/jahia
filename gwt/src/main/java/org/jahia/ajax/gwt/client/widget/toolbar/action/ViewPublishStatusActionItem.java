package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:01 PM
* To change this template use File | Settings | File Templates.
*/
public class ViewPublishStatusActionItem extends BaseActionItem implements ModuleSelectionHandler {
    public void onSelection() {
        EditActions.viewPublishedStatus(linker);
    }

    public void handleNewModuleSelection(Module selectedModule) {
    }

    public Component createNewToolItem() {
        return new ToggleButton();
    }
}

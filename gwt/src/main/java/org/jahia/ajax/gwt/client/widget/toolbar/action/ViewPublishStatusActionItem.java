package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:01 PM
* To change this template use File | Settings | File Templates.
*/
public class ViewPublishStatusActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.viewPublishedStatus(linker);
    }

    public void handleNewLinkerSelection() {
    }

    public Component createNewToolItem() {
        return new ToggleButton();
    }
}

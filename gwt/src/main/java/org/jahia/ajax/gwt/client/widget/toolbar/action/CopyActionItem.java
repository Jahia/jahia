package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:46 PM
* To change this template use File | Settings | File Templates.
*/
public class CopyActionItem extends BaseActionItem{
    public void onComponentSelection() {
        ContentActions.copy(linker);
    }

    public void handleNewLinkerSelection() {
        setEnabled(linker.getSelectionContext().isTableSelection());
    }
}

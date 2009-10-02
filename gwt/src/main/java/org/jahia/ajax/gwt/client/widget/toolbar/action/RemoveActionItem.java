package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:36 PM
* To change this template use File | Settings | File Templates.
*/
public class RemoveActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.remove(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isDeleteable() && !lh.mount);
    }
}

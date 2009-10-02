package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:51 PM
*/
public class RenameActionItem extends BaseActionItem   {
    public void onComponentSelection() {
        ContentActions.rename(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isWriteable() && (lh.isSingleFile() || lh.isSingleFolder()));
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:20 PM
*/
public class PasteActionItem extends BaseActionItem {
    public void onComponentSelection() {
        ContentActions.paste(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable() && lh.isPasteAllowed() || lh.isTableSelection() && lh.isWriteable() && lh.isPasteAllowed());
    }
}

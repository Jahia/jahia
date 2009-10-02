package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:55 PM
* To change this template use File | Settings | File Templates.
*/
public class ResizeActionItem extends BaseActionItem   {
    public void onComponentSelection() {
        ContentActions.resizeImage(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isParentWriteable() && lh.isSingleFile() && lh.isImage());
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Item for "resize image" action.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:55 PM
 */
public class ResizeActionItem extends BaseActionItem   {
    public void onComponentSelection() {
        ContentActions.resizeImage(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isParentWriteable() && lh.isSingleFile() && lh.isImage()
                && lh.getSelectedNodes().get(0).get("j:height") != null
                && lh.getSelectedNodes().get(0).get("j:width") != null);
    }
}

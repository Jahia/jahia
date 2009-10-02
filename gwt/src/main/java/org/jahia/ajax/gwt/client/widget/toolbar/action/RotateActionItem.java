package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:18 PM
* To change this template use File | Settings | File Templates.
*/
public class RotateActionItem extends BaseActionItem   {
    public void onComponentSelection() {
        ContentActions.rotateImage(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isParentWriteable() && lh.isSingleFile() && lh.isImage());
    }
}

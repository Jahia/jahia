package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:06 PM
* To change this template use File | Settings | File Templates.
*/
public class ZipActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.zip(linker);
    }

    public void handleNewLinkerSelection(){
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isParentWriteable());
    }
}

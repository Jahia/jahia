package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:48 PM
* To change this template use File | Settings | File Templates.
*/
public class ImportActionItem extends BaseActionItem {
    public void onComponentSelection() {
        ContentActions.importContent(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable());
    }
}

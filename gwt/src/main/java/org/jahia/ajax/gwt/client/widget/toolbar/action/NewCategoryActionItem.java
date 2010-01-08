package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:33 PM
* To change this template use File | Settings | File Templates.
*/
public class NewCategoryActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.createNode(linker, Messages.get("fm_newcategory", "New category"), "jnt:category");
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable() || lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
    }
}

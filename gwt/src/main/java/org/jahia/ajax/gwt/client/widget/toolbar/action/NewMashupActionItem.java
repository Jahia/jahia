package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:25 PM
* To change this template use File | Settings | File Templates.
*/
public class NewMashupActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        ContentActions.showMashupWizard(linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isLeftTreeSelection() && lh.isParentWriteable() || lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
    }
}

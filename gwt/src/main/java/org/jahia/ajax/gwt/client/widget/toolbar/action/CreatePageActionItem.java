package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:53 PM
* To change this template use File | Settings | File Templates.
*/
public class CreatePageActionItem extends BaseActionItem {
    public void onComponentSelection() {
        EditActions.createPage(linker);
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:50 PM
* To change this template use File | Settings | File Templates.
*/
public class RefreshActionItem extends BaseActionItem {
    public void onComponentSelection() {
        linker.refresh();
    }
}

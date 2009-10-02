package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:58 PM
*/
public class MountActionItem extends BaseActionItem {
    public void onComponentSelection() {
        ContentActions.mountFolder(linker);
    }

    public void handleNewLinkerSelection() {
        setEnabled("root".equals(JahiaGWTParameters.getCurrentUser())); // TODO dirty code (to refactor using server side configuration and roles)
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.toolbar.handler.ModuleSelectionHandler;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.edit.Module;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* To change this template use File | Settings | File Templates.
*/
public class PublishActionItem extends BaseActionItem implements ModuleSelectionHandler {
    public void onSelection() {
        EditActions.publish(linker);
    }

    public void handleNewModuleSelection(Module selectedModule) {
        if (selectedModule != null) {
            GWTJahiaPublicationInfo info = selectedModule.getNode().getPublicationInfo();
            setEnabled(info.isCanPublish() && (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.MODIFIED));
        }
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:59:06 PM
*/
public class DeleteActionItem extends BaseActionItem {
    public DeleteActionItem() {
    }

    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {                
            }

            public void onSuccess() {
                EditActions.delete(linker);
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isTableSelection() && lh.isWriteable());
    }
}

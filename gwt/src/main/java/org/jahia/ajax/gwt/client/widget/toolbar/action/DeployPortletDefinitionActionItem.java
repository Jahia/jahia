package org.jahia.ajax.gwt.client.widget.toolbar.action;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:35 PM
* To change this template use File | Settings | File Templates.
*/
public class DeployPortletDefinitionActionItem extends BaseActionItem {
    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                ContentActions.showDeployPortletForm(linker);
            }
        });

    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.isMainSelection() && lh.isParentWriteable() || lh.isTableSelection() && lh.isSingleFolder() && lh.isWriteable());
    }
}

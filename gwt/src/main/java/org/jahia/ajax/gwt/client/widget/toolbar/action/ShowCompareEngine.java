package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 10:12:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShowCompareEngine extends BaseActionItem {
    @Override
    public void onComponentSelection() {
        EditActions.showCompare(linker);
        setEnabled(false);
    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh != null);
    }
}

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.widget.content.ContentViews;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 30, 2009
 * Time: 6:29:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SwitchViewActionItem extends BaseActionItem {
    private String view;

    public void setView(String view) {
        this.view = view;
    }

    public void onSelection() {
        if (linker instanceof ManagerLinker) {
            TopRightComponent trc = ((ManagerLinker)linker).getTopRightObject();
            if (trc instanceof ContentViews) {
                if ("list".equals(view)) {
                    ((ContentViews)trc).switchToListView();
                } else if ("thumbs".equals(view)) {
                    ((ContentViews)trc).switchToThumbView();
                } else if ("detailed".equals(view)) {
                    ((ContentViews)trc).switchToDetailedThumbView();
                }
            }
        }
    }
}

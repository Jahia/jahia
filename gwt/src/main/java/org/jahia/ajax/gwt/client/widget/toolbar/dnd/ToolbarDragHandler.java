/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.dnd;

import com.allen_sauer.gwt.dnd.client.*;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.JahiaToolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 5 mars 2008
 * Time: 18:25:57
 */
public class ToolbarDragHandler implements DragHandler {
    private GWTJahiaPageContext page;

    public ToolbarDragHandler(GWTJahiaPageContext page) {
        this.page = page;
    }

    public void onDragEnd(DragEndEvent event) {
        updateToolbarState(event.getContext(), event.getDropTarget());
    }

    public void onDragStart(DragStartEvent event) {

    }

    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {


    }

    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {

    }

    private void updateToolbarState(DragContext dragContext, Widget nextParent) {
        if (dragContext.draggable instanceof JahiaToolbar) {
            final JahiaToolbar draggableJahiaToolbar = (JahiaToolbar) dragContext.draggable;
            final int previousState = draggableJahiaToolbar.getGwtToolbar().getState().getValue();
            Log.debug("previous state: " + previousState);
            final List toolbarList = new ArrayList();

            if (nextParent instanceof TargetVerticalPanel) {
                // preferences toolbars of the new column
                TargetVerticalPanel targetVerticalPanel = (TargetVerticalPanel) nextParent;
                for (int i = 0; i < targetVerticalPanel.getWidgetCount(); i++) {
                    Widget w = targetVerticalPanel.getWidget(i);
                    if (w instanceof JahiaToolbar) {
                        JahiaToolbar jahiaToolbar = (JahiaToolbar) w;
                        if (targetVerticalPanel.isTop()) {
                            jahiaToolbar.getGwtToolbar().getState().setValue(ToolbarConstants.TOOLBAR_TOP);
                        } else if (targetVerticalPanel.isRigth()) {
                            jahiaToolbar.getGwtToolbar().getState().setValue(ToolbarConstants.TOOLBAR_RIGHT);
                        }
                        jahiaToolbar.getGwtToolbar().getState().setIndex(i);
                        toolbarList.add(jahiaToolbar.getGwtToolbar());
                    } else {
                        Log.debug("updateToolbarState: child widget[" + i + "] is not a JahiaToolbar " + w);
                    }
                }
            } else if (nextParent instanceof TargetHorizontalPanel) {
                Log.debug("updateToolbarState:makeVerticalLayout");
                // preferences toolbars of the new column
                TargetHorizontalPanel targetVerticalPanel = (TargetHorizontalPanel) nextParent;
                for (int i = 0; i < targetVerticalPanel.getWidgetCount(); i++) {
                    Widget w = targetVerticalPanel.getWidget(i);
                    if (w instanceof JahiaToolbar) {
                        JahiaToolbar jahiaToolbar = (JahiaToolbar) w;

                        jahiaToolbar.getGwtToolbar().getState().setValue(ToolbarConstants.TOOLBAR_RIGHT);
                        jahiaToolbar.getGwtToolbar().getState().setIndex(i);
                        toolbarList.add(jahiaToolbar.getGwtToolbar());
                    } else {
                        Log.debug("updateToolbarState: child widget[" + i + "] is not a JahiaToolbar ");
                    }
                }

            } else {
                Log.debug("updateToolbarState:makeBoxLayout");
                if (previousState != ToolbarConstants.TOOLBAR_RIGHT && previousState != ToolbarConstants.TOOLBAR_VERTICAL_BOX) {
                    draggableJahiaToolbar.getGwtToolbar().getState().setValue(ToolbarConstants.TOOLBAR_HORIZONTAL_BOX);
                } else {
                    draggableJahiaToolbar.getGwtToolbar().getState().setValue(ToolbarConstants.TOOLBAR_VERTICAL_BOX);
                }
                int scrollLeft = Window.getScrollLeft();
                int scrollTop = Window.getScrollTop();
                draggableJahiaToolbar.getGwtToolbar().getState().setPagePositionX(draggableJahiaToolbar.getAbsoluteLeft() - scrollLeft);
                draggableJahiaToolbar.getGwtToolbar().getState().setPagePositionY(draggableJahiaToolbar.getAbsoluteTop() - scrollTop);
                draggableJahiaToolbar.makeOnScrollFixed();

                toolbarList.add(draggableJahiaToolbar.getGwtToolbar());
            }

            if (previousState != draggableJahiaToolbar.getGwtToolbar().getState().getValue()) {
                draggableJahiaToolbar.refreshUI();
            }
            Log.debug("Toolbar list size: " + toolbarList.size());
            // save state
            if (toolbarList.size() > 0) {
                saveState(toolbarList);
            } else {
                Log.error("Toolbar list is empty.");
            }
        } else {
            Log.error("dragContext.draggable: " + dragContext.draggable);

        }
    }

    private void saveState(List toolbarList) {
        Log.debug("ToolbarDropController: saveState");
        // update
        ToolbarService.App.getInstance().updateToolbars(page, toolbarList, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Log.debug("toolbar update --> failed", throwable);
            }

            public void onSuccess(Object o) {
                Log.debug("toolbar update --> success");
            }
        });
    }

}

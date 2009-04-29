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

import org.jahia.ajax.gwt.client.widget.toolbar.JahiaToolbar;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.ui.IndexedPanel;


/**
 * User: jahia
 * Date: 6 mars 2008
 * Time: 11:01:45
 */
public class ToolbarIndexedDropController extends IndexedDropController {
    public ToolbarIndexedDropController(IndexedPanel dropTarget) {
        super(dropTarget);
    }

    /* protected void insert(Widget widget, int beforeIndex) {
       if (beforeIndex == dropTarget.getWidgetCount()) {
           beforeIndex--;
       }
       super.insert(widget, beforeIndex);
   } */

    public void onMove(DragContext dragContext) {
        super.onMove(dragContext);
        Log.debug("ToolbarDropController: onmove2");
        if (GXT.isIE6) {
            final JahiaToolbar draggableJahiaToolbar = (JahiaToolbar) dragContext.draggable;
            draggableJahiaToolbar.setWidth("10px");
        }
    }

    public void onPreviewDrop(DragContext dragContext) throws VetoDragException {
        super.onPreviewDrop(dragContext);
        Log.debug("ToolbarDropController: onpreviewmove");
        if (GXT.isIE6) {
            final JahiaToolbar draggableJahiaToolbar = (JahiaToolbar) dragContext.draggable;
            draggableJahiaToolbar.setWidth("100%");
        }
    }
}

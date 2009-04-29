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
package org.jahia.ajax.gwt.client.widget.template;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: jahia
 * Date: 11 janv. 2008
 * Time: 09:29:20
 */
public abstract class AdvancedTable extends FlexTable {
    public abstract void mouseEntersRow(Widget sender, int row);

    public abstract void mouseLeavesRow(Widget sender, int row);

    public AdvancedTable() {
        super();
        sinkEvents(Event.ONMOUSEOVER);
        sinkEvents(Event.ONMOUSEOUT);
    }

    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER: {
                // Find out which cell was entered.
                Element td = getMouseEventTargetCell(event);
                if (td == null) return;
                Element tr = DOM.getParent(td);
                Element body = DOM.getParent(tr);
                int row = DOM.getChildIndex(body, tr);
                // Fire the event
                mouseEntersRow(this, row);
                break;
            }
            case Event.ONMOUSEOUT: {
                // Find out which cell was exited.
                Element td = getMouseEventTargetCell(event);
                if (td == null) return;
                Element tr = DOM.getParent(td);
                Element body = DOM.getParent(tr);
                int row = DOM.getChildIndex(body, tr);
                // Fire the event.
                mouseLeavesRow(this, row);
                break;
            }
        }
        super.onBrowserEvent(event);
    }

    /**
     * Method originally copied from HTMLTable superclass where it was
     * defined private
     * Now implemented differently to only return target cell if it's
     * part of 'this' table
     */
    private Element getMouseEventTargetCell(Event event) {
        Element td = DOM.eventGetTarget(event);
        //locate enclosing td element
        while (!DOM.getAttribute(td, "tagName").equalsIgnoreCase("td")) {
            // If we run out of elements, or run into the table itself,then give up.
            if ((td == null) || DOM.compare(td, getElement()))
                return null;
            td = DOM.getParent(td);
        }
        //test if the td is actually from this table
        Element tr = DOM.getParent(td);
        Element body = DOM.getParent(tr);
        if (DOM.compare(body, this.getBodyElement())) {
            return td;
        }
        //Didn't find appropriate cell
        return null;
    }
}

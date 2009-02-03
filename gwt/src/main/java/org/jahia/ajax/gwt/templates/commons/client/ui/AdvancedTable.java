/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.commons.client.ui;

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

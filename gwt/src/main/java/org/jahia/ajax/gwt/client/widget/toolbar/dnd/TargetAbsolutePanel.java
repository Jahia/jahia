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

package org.jahia.ajax.gwt.client.widget.toolbar.dnd;

import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaState;
import org.jahia.ajax.gwt.client.widget.toolbar.JahiaToolbar;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: jahia
 * Date: 24 avr. 2008
 * Time: 10:02:37
 */
public class TargetAbsolutePanel extends AbsolutePanel implements TargetToolbarsPanel {
    public void addToolbar(JahiaToolbar jahiaToolbar) {
        GWTJahiaState state = jahiaToolbar.getGwtToolbar().getState();
        add(jahiaToolbar, state.getPagePositionX(), state.getPagePositionY());
        if (GXT.isIE6) {
            jahiaToolbar.setAutoWidth(true);
        }
        jahiaToolbar.setParentContainer(this);
        jahiaToolbar.makeOnScrollFixed();

    }


    public boolean isTop() {
        return false;
    }

    public boolean isRigth() {
        return false;
    }

    public void hide() {
        // hide all toolbars
        for (int i = 0; i < getWidgetCount(); i++) {
            Widget w = getWidget(i);
            if (w instanceof JahiaToolbar) {
                w.setVisible(false);
            }
        }
    }

    public void show() {
        // diplay all toolbar
        for (int i = 0; i < getWidgetCount(); i++) {
            Widget w = getWidget(i);
            if (w instanceof JahiaToolbar) {
                // check if toolbar should be displayed
                w.setVisible(true);
            }
        }
    }
}

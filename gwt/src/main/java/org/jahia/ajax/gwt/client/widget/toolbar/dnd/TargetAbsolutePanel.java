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

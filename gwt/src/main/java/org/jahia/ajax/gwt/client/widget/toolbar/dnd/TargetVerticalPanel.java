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


import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.ajax.gwt.client.widget.toolbar.JahiaToolbar;

/**
 * User: jahia
 * Date: 6 mars 2008
 * Time: 10:58:39
 */
public class TargetVerticalPanel extends VerticalPanel implements TargetToolbarsPanel {
    private Label dumpLabel = new Label("");
    private int type;

    public TargetVerticalPanel(int type) {
        this.type = type;
        setSpacing(1);
    }


    public void addToolbar(JahiaToolbar jahiaToolbar) {
        int index = jahiaToolbar.getGwtToolbar().getState().getIndex();
        if (index > 0 && index < getWidgetCount()) {
            insert(jahiaToolbar, index);
        } else {
            add(jahiaToolbar);
        }
        jahiaToolbar.setAutoWidth(false);
        jahiaToolbar.setParentContainer(this);
        setCellVerticalAlignment(jahiaToolbar, VerticalPanel.ALIGN_TOP);
    }


    public boolean isTop() {
        return type == ToolbarConstants.AREA_TOP;
    }

    public boolean isRigth() {
        return type == ToolbarConstants.AREA_RIGHT;
    }


    public void hide() {
        setVisible(false);
    }

    public void show() {
        setVisible(true);
    }
}

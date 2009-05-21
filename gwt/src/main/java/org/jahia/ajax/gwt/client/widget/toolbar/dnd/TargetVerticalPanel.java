/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

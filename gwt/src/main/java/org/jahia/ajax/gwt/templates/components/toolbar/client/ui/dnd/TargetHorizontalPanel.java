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

package org.jahia.ajax.gwt.templates.components.toolbar.client.ui.dnd;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.extjs.gxt.ui.client.GXT;
import org.jahia.ajax.gwt.templates.components.toolbar.client.ui.mygwt.JahiaToolbar;

/**
 * User: jahia
 * Date: 6 mars 2008
 * Time: 11:21:43
 */
public class TargetHorizontalPanel extends HorizontalPanel implements TargetToolbarsPanel {
    private Label dumpLabel = new Label();

    public TargetHorizontalPanel() {
        init();
    }

    private void init() {
        add(dumpLabel);
    }

    public void addToolbar(JahiaToolbar jahiaToolbar) {
        int index = jahiaToolbar.getGwtToolbar().getState().getIndex();
        if (index > 0 && index < getWidgetCount()) {
            insert(jahiaToolbar, index);
        } else {
            add(jahiaToolbar);
        }
        jahiaToolbar.setParentContainer(this);
        if (GXT.isIE6) {
            jahiaToolbar.setAutoWidth(false);
        }
    }

    @Override
    public void add(Widget widget) {
        super.add(widget);
        if (widget instanceof JahiaToolbar) {
            ((JahiaToolbar) widget).setParentContainer(this);
        }
    }

    @Override
    public void insert(Widget widget, int i) {
        super.insert(widget, i);
        if (widget instanceof JahiaToolbar) {
            ((JahiaToolbar) widget).setParentContainer(this);
        }
    }

    public boolean isTop() {
        return false;
    }

    public boolean isRigth() {
        return false;
    }

    public void hide() {
        setVisible(false);
    }

    public void show() {
        setVisible(true);
    }
}

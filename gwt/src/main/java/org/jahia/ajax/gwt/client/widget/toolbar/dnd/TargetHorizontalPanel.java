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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.extjs.gxt.ui.client.GXT;
import org.jahia.ajax.gwt.client.widget.toolbar.JahiaToolbar;

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

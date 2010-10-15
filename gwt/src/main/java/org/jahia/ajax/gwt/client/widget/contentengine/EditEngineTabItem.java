/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.InfoTabItem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EditEngineTabItem implements Serializable {
    protected GWTEngineTab gwtEngineTab;

    protected transient AsyncTabItem tab;
    protected transient NodeHolder engine;

    protected EditEngineTabItem() {
    }

    public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        this.gwtEngineTab = engineTab;
        this.engine = engine;

        tab = new AsyncTabItem(gwtEngineTab.getTitle()) {
            @Override public void setProcessed(boolean processed) {
                EditEngineTabItem.this.setProcessed(processed);
                super.setProcessed(processed);
            }
        };

        tab.setLayout(new FitLayout());
        tab.setStyleName("x-panel-mc");
        tab.setData("item", this);
        return tab;
    }

    /**
     * Create the tab item
     */
    public abstract void init(String language);

    public void setProcessed(boolean processed) {
    }

    public boolean handleMultipleSelection() {
        return false;
    }

    public static void addTabs(final List<GWTEngineTab> tabsConfig, final TabPanel tabs, final NodeHolder nodeHolder) {
        for (GWTEngineTab tabConfig : tabsConfig) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            tabs.add(tabItem.create(tabConfig, nodeHolder));
        }
    }
}

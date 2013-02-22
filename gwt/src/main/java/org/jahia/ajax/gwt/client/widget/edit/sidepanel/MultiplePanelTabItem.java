/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGrid;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;
import java.util.Map;

/**
 * tab item that contains SettingsPanel
 *
 */
public class MultiplePanelTabItem extends SidePanelTabItem {

    private transient  ContentPanel settingsPanels;

    private List<SettingsPanel> settingsPanelList;

    /**
     * create method, called when instance is created
     * remove default treeContainer from tab and put settingsPanel instead
     * @param config  config get from spring config
     * @return
     */

    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        VBoxLayout l = new VBoxLayout();
        l.setVBoxLayoutAlign(VBoxLayout.VBoxLayoutAlign.STRETCH);
        tab.setLayout(l);
        settingsPanels = new ContentPanel();
        if (settingsPanelList.size() > 1) {
            settingsPanels.setLayout(new AccordionLayout());
        } else {
            settingsPanels.setLayout(new FitLayout());
        }
        settingsPanels.setScrollMode(Style.Scroll.NONE);
        settingsPanels.setHeaderVisible(false);
        settingsPanels.setBodyBorder(false);
        settingsPanels.setExpanded(true);
        tab.setLayout(new FitLayout());
        tab.add(settingsPanels);
        return tab;
    }

    public void setSettingsPanelList(List<SettingsPanel> settingsPanelList) {
        this.settingsPanelList = settingsPanelList;
    }

    /**
     * fill settingsTabItem with defined panels
     * @param linker
     */
    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        for (SettingsPanel panel : settingsPanelList) {
            if (PermissionsUtils.isPermitted(panel.getRequiredPermission(), JahiaGWTParameters.getSiteNode())) {
                panel.init();
                settingsPanels.add(panel.getSettingsPanel());
            }
        }

    }

    @Override
    public void doRefresh() {
        for (SettingsPanel panel : settingsPanelList) {
            panel.refresh();
        }
    }

}

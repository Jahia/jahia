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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.content.InfoTabItem;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 6:31:49 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EditEngineTabItem extends AsyncTabItem {
    protected NodeHolder engine;

    protected EditEngineTabItem(NodeHolder engine) {
        this.engine = engine;
        setLayout(new FitLayout());
        setStyleName("x-panel-mc");
    }

    protected EditEngineTabItem(String title, NodeHolder engine) {
        super(title);
        this.engine = engine;
        setLayout(new FitLayout());
        setStyleName("x-panel-mc");
    }

    /**
     * Create the tab item
     */
    public abstract void create(GWTJahiaLanguage locale);


    public boolean handleMultipleSelection() {
        return false;
    }

    public static void addTabs(final List<String> tabsConfig, final TabPanel tabs, final NodeHolder nodeHolder) {
        for (String tab : tabsConfig) {
            if (tab.equals("info")) {
                tabs.add(new InfoTabItem(nodeHolder));
            } else if (tab.equals("listOrderingContent")) {
                tabs.add(new ListOrderingContentTabItem(nodeHolder));
            } else if (tab.equals("content")) {
                tabs.add(new ContentTabItem(nodeHolder));
            } else if (tab.equals("template")) {
                tabs.add(new TemplateOptionsTabItem(nodeHolder));
            } else if (tab.equals("layout")) {
                tabs.add(new LayoutTabItem(nodeHolder));
            } else if (tab.equals("metadata")) {
                tabs.add(new MetadataTabItem(nodeHolder));
            } else if (tab.equals("tags")) {
                tabs.add(new TagsTabItem(nodeHolder));
            } else if (tab.equals("categories")) {
                tabs.add(new CategoriesTabItem(nodeHolder));
            } else if (tab.equals("options")) {
                tabs.add(new OptionsTabItem(nodeHolder));
            } else if (tab.equals("rights")) {
                tabs.add(new RightsTabItem(nodeHolder));
            } else if (tab.equals("usages")) {
                tabs.add(new UsagesTabItem(nodeHolder));
            } else if (tab.equals("workflow")) {
                tabs.add(new WorkflowTabItem(nodeHolder));
            } else if (tab.equals("seo")) {
                tabs.add(new SeoTabItem(nodeHolder));
            } else if (tab.equals("analytics")) {
                tabs.add(new AnalyticsTabItem(nodeHolder));
            } else if (tab.equals("rolePrincipals")) {
                tabs.add(new RolePrincipalsTabItem(nodeHolder));
            } else if (tab.equals("portlets")) {
                tabs.add(new PortletsTabItem(nodeHolder));
            } else if (tab.equals("versioning")) {
                tabs.add(new VersioningTabItem(nodeHolder));
            }
        }
    }
}

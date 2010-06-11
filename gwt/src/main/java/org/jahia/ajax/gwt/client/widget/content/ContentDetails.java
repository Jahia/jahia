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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.widget.edit.contentengine.*;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class ContentDetails extends BottomRightComponent implements NodeHolder {
    private GWTManagerConfiguration config;
    private ContentPanel m_component;
    private TabPanel tabs;
    private List<GWTJahiaNodeType> types;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTJahiaNodeProperty> properties = new HashMap<String, GWTJahiaNodeProperty>();
    private GWTJahiaLanguage language;

    private List<GWTJahiaNode> selectedNodes = null;


    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private final JahiaContentDefinitionServiceAsync cDefService = JahiaContentDefinitionService.App.getInstance();


    public ContentDetails(GWTManagerConfiguration config) {
        super();
        this.config = config;
        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(true);
        m_component.setHeaderVisible(false);
        tabs = new TabPanel();
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        tabs.setAnimScroll(true);
        tabs.setTabScroll(true);

        // properties
        initTabs();

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (selectedNodes != null && selectedNodes.size() > 0) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
    }


    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (String tab : config.getTabs()) {
            if (tab.equals("info")) {
                tabs.add(new InfoTabItem(this));
            } else if (tab.equals("content")) {
                tabs.add(new ContentTabItem(this));
            } else if (tab.equals("template")) {
                tabs.add(new TemplateOptionsTabItem(this));
            } else if (tab.equals("layout")) {
                tabs.add(new LayoutTabItem(this));
            } else if (tab.equals("metadata")) {
                tabs.add(new MetadataTabItem(this));
            } else if (tab.equals("classification")) {
                tabs.add(new ClassificationTabItem(this));
            } else if (tab.equals("option")) {
                tabs.add(new OptionsTabItem(this));
            } else if (tab.equals("rights")) {
                tabs.add(new RightsTabItem(this));
            } else if (tab.equals("usages")) {
                tabs.add(new UsagesTabItem(this));
            } else if (tab.equals("publication")) {
                tabs.add(new PublicationTabItem(this));
            } else if (tab.equals("workflow")) {
                tabs.add(new WorkflowTabItem(this));
            } else if (tab.equals("seo")) {
                tabs.add(new SeoTabItem(this));
            } else if (tab.equals("analytics")) {
                tabs.add(new AnalyticsTabItem(this));
            }
        }
        for (TabItem tabItem : tabs.getItems()) {
            ((EditEngineTabItem)tabItem).setToolbarEnabled(true);
        }
    }


    public Component getComponent() {
        return m_component;
    }


    public void clear() {
        m_component.setHeading("&nbsp;");
        selectedNodes = null;
        for (TabItem item : tabs.getItems()) {
            ((EditEngineTabItem)item).setProcessed(false);
            item.setEnabled(false);
        }
    }

    public void fillData(Object selectedItem) {
        clear();

        properties = null;
        types = null;
        mixin = null;

        if (selectedItem != null) {
            if (selectedItem instanceof GWTJahiaNode) {
                selectedNodes = new ArrayList<GWTJahiaNode>();
                selectedNodes.add((GWTJahiaNode) selectedItem);
            } else {
                selectedNodes = (List<GWTJahiaNode>) selectedItem;
            }
            String heading;
            if (selectedNodes.size() == 0) {
                heading = "&nbsp;";
            } else {
                heading = "";
            }
            for (GWTJahiaNode node : selectedNodes) {
                if (heading.length() + node.getName().length() < 100) {
                    heading += node.getName() + ",";
                } else {
                    heading += "... ";
                    break;
                }
            }
            if (selectedNodes.size() > 0) {
                heading = heading.substring(0, heading.length() - 1);
            }
            m_component.setHeading(heading);

            if (selectedNodes.size() == 1) {
                JahiaContentManagementService.App.getInstance().getProperties(selectedNodes.get(0).getPath(), JahiaGWTParameters.getLanguage(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {

                    public void onSuccess(GWTJahiaGetPropertiesResult result) {
                        GWTJahiaNode node = result.getNode();
                        types = result.getNodeTypes();
                        properties = result.getProperties();
                        language = result.getCurrentLocale();

                        //todo : do this in one pass
                        JahiaContentDefinitionService.App.getInstance().getAvailableMixin(node, new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
                            public void onSuccess(List<GWTJahiaNodeType> result) {
                                mixin = result;
                                fillCurrentTab();
                            }

                        });

                        fillCurrentTab();
                    }
                });
                for (TabItem item : tabs.getItems()) {
                    item.setEnabled(true);
                }
            } else if (selectedNodes.size() > 1) {
                for (TabItem item : tabs.getItems()) {
                    if (((EditEngineTabItem)item).handleMultipleSelection()) {
                        item.setEnabled(true);
                    }
                }
            }


            fillCurrentTab();

        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();

        if (currentTab instanceof EditEngineTabItem) {
            EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTab;
            if (!engineTabItem.isProcessed()) {
                engineTabItem.create(language);
                m_component.layout();
            }
        }
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return types;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public GWTJahiaNode getNode() {
        return selectedNodes.get(0);
    }

    public List<GWTJahiaNode> getNodes() {
        return selectedNodes;
    }

    public GWTJahiaNode getParentNode() {
        return null;
    }

    public boolean isExistingNode() {
        return true;
    }

    public boolean isMultipleSelection() {
        return selectedNodes.size() > 1;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }
}

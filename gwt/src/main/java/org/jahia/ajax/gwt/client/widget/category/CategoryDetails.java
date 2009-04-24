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

package org.jahia.ajax.gwt.client.widget.category;

import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryTitle;
import org.jahia.ajax.gwt.client.service.category.CategoryServiceAsync;
import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author ktlili
 */
public class CategoryDetails extends BottomRightComponent {
    private ContentPanel m_component;
    private AsyncTabItem info;
    private AsyncTabItem properties;
    private AsyncTabItem acl;
    private FlowPanel infoPanel;
    private TabPanel tabs;
    private PropertiesEditor propertiesEditor;
    private AclEditor aclEditor;
    private GWTJahiaCategoryNode selectedCategory = null;
    private final CategoryServiceAsync categoryServiceAsync = CategoryService.App.getInstance();

    public CategoryDetails() {
        super();
        m_component = new ContentPanel(new FitLayout());
        m_component.setBodyBorder(false);
        m_component.setBorders(false);

        tabs = new TabPanel();
        tabs.setEnabled(false);
        info = new AsyncTabItem();
        properties = new AsyncTabItem();
        acl = new AsyncTabItem();
        infoPanel = new FlowPanel();

        tabs.add(info);
        tabs.add(properties);
        tabs.add(acl);

        // info panel
        info.setText(getResource("cat_info"));
        info.add(infoPanel);

        // properties
        propertiesEditor = new PropertiesEditor(this);
        properties.setLayout(new FitLayout());
        properties.setText(getResource("cat_prop"));
        properties.setScrollMode(Style.Scroll.AUTO);

        // acl
        acl.setText(getResource("cat_update_acl"));
        acl.setLayout(new FitLayout());

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (selectedCategory != null) {
                    fillCurrentTab();
                }
            }
        });

        m_component.add(tabs);
    }


    public void clear() {
        m_component.setHeading("&nbsp;");
        infoPanel.clear();
        properties.removeAll();
        acl.removeAll();
        selectedCategory = null;
        info.setProcessed(false);
        properties.setProcessed(false);
        acl.setProcessed(false);
    }

    public void fillData(Object selectedItem) {
        clear();
        if (selectedItem != null) {
            List<GWTJahiaCategoryNode> selectedNodes = (List<GWTJahiaCategoryNode>) selectedItem;
            tabs.setEnabled(true);
            if (selectedNodes.size() == 1) {
                selectedCategory = selectedNodes.get(0);
                if (selectedCategory != null) {
                    m_component.setHeading(selectedCategory.getName());
                    fillCurrentTab();
                }
            }
        }else{
            tabs.setEnabled(false);            
        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (currentTab == info) {
            displayInfo();
        } else if (currentTab == properties) {
            displayProperties();
        } else if (currentTab == acl) {
            displayAcl();
        }

    }

    public Component getComponent() {
        return m_component;
    }

    /**
     * Display info
     */
    public void displayInfo() {
        if (!info.isProcessed()) {
            Grid g = new Grid(1, 2);
            g.setCellSpacing(10);
            FlowPanel flowPanel = new FlowPanel();
            String name = selectedCategory.getName();

            // key
            String key = selectedCategory.getKey();
            if (key != null) {
                flowPanel.add(new HTML("<b>" + getResource("cat_key") + " :</b> " + key));
            }

            // titles
            List<GWTJahiaCategoryTitle> gwtCategoryTitles = selectedCategory.getCategoryTitles();
            if (gwtCategoryTitles != null) {
                for (GWTJahiaCategoryTitle categoryTitle : gwtCategoryTitles) {
                    if (name != null) {
                        flowPanel.add(new HTML("<b>" + getResource("cat_title") + "(" + categoryTitle.getLocale() + ") :</b> " + categoryTitle.getTitleValue()));
                    }
                }
            }

            // path
            String path = selectedCategory.getPath();
            if (path != null) {
                flowPanel.add(new HTML("<b>" + getResource("cat_path") + " :</b> " + path));
            }


            g.setWidget(0, 1, flowPanel);
            infoPanel.add(g);
            info.setProcessed(true);
        }
    }


    /**
     * Display properties
     */
    public void displayProperties() {
        if (!properties.isProcessed()) {
            if (getLinker() != null) {
                getLinker().loading("collecting properties...");
            }

            // load proeprties
            categoryServiceAsync.loadProperties(selectedCategory, new AsyncCallback<GWTJahiaCategoryNode>() {
                public void onFailure(Throwable throwable) {
                    Log.debug("Cannot get properties", throwable);
                }

                public void onSuccess(GWTJahiaCategoryNode gwtJahiaCategoryNode) {
                    updatePropertiesTab(gwtJahiaCategoryNode);
                    properties.setProcessed(true);
                    if (getLinker() != null) {
                        getLinker().loaded();
                    }
                }
            });
        } else {
            Log.debug("is processed");
        }
    }

    public void displayAcl() {
        if (selectedCategory != null) {
            if (!acl.isProcessed()) {
                if (getLinker() != null) {
                    getLinker().loading("collecting acls...");
                }
                categoryServiceAsync.getACL(selectedCategory, new AsyncCallback<GWTJahiaNodeACL>() {


                    public void onSuccess(final GWTJahiaNodeACL gwtJahiaNodeACL) {
                        aclEditor = new AclEditor(gwtJahiaNodeACL, "siteSelector");
                        final TextToolItem saveButton = aclEditor.getSaveButton();

                        // add selection lister on save button
                        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
                            public void componentSelected(ComponentEvent event) {
                                Log.debug("save category ACL" + aclEditor.getAcl().getAce());
                                aclEditor.setSaved();
                                categoryServiceAsync.setACL(selectedCategory, aclEditor.getAcl(), new AsyncCallback() {
                                    public void onSuccess(Object o) {

                                    }

                                    public void onFailure(Throwable throwable) {
                                        Log.error("acl save failed", throwable);
                                    }

                                });

                            }
                        });

                        renderAcl();
                        acl.setProcessed(true);
                        if (getLinker() != null) {
                            getLinker().loaded();
                        }
                    }

                    /*on failure */
                    public void onFailure(Throwable throwable) {
                        Log.debug("Cannot retrieve acl", throwable);
                    }
                });
            }
        }
    }

    // ACL TAB
    private void renderAcl() {
        ContentPanel aclPanel = aclEditor.renderNewAclPanel();
        if (selectedCategory != null && !selectedCategory.isAdmin()) {
            aclPanel.setEnabled(false);
        } else {
            aclPanel.setEnabled(true);
        }
        acl.add(aclPanel);
        acl.layout();
    }

    public void rerenderAcl() {
        if (aclEditor != null) {
            acl.removeAll();
            renderAcl();
        }
    }


    /**
     * update properties tab
     */
    public void updatePropertiesTab(GWTJahiaCategoryNode gwtJahiaCategoryNode) {
        properties.removeAll();
        propertiesEditor.setGwtJahiaCategoryNode(gwtJahiaCategoryNode);
        properties.add(propertiesEditor.createForm());
        properties.layout();
    }

    private String getResource(String key) {
        return Messages.getResource(key);
    }

}


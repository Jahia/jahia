/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.storage.client.Storage;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.tripanel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the linker that allow communication between all the browser components.
 *
 * @author rfelden
 * @version 19 juin 2008 - 10:45:20
 */
public class ManagerLinker implements Linker {

    // Components
    private LeftComponent m_leftComponent;
    private TopRightComponent m_topRightComponent;
    private BottomRightComponent m_bottomRightComponent;
    private TopBar m_topBar;
    private BottomBar m_bottomBar;
    private DNDListener dndListener;
    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private GWTJahiaNode leftPanelSelectionWhenHidden;
    private GWTManagerConfiguration config;
    protected static final String PICKER = "picker";
    protected static final String MANAGER = "manager";
    private boolean displayHiddenTypes = false;
    private boolean displayHiddenProperties = false;

    public ManagerLinker(GWTManagerConfiguration configuration) {
        this.config = configuration;
    }

    /**
     * deprecated please use no args constructor and then registerComponents method
     *
     * @param leftComponent        a left tree browser (can be null)
     * @param topRightComponent    a top table or tree table (cannot be null)
     * @param bottomRightComponent a bottom panel displaying details (can be null)
     * @param topBar               a toolbar for interaction(cannot be null)
     * @param bottomBar            a status bar displaying short info on current events
     */
    public ManagerLinker(LeftComponent leftComponent, TopRightComponent topRightComponent, BottomRightComponent bottomRightComponent, TopBar topBar, BottomBar bottomBar) {
        m_topRightComponent = topRightComponent;
        m_topBar = topBar;
        m_bottomRightComponent = bottomRightComponent;
        m_leftComponent = leftComponent;
        m_bottomBar = bottomBar;
        registerLinker();
    }

    /**
     * @param leftComponent        a left tree browser (can be null)
     * @param topRightComponent    a top table or tree table (cannot be null)
     * @param bottomRightComponent a bottom panel displaying details (can be null)
     * @param topBar               a toolbar for interaction(cannot be null)
     * @param bottomBar            a status bar displaying short info on current events
     */
    public void registerComponents(LeftComponent leftComponent, TopRightComponent topRightComponent, BottomRightComponent bottomRightComponent, TopBar topBar, BottomBar bottomBar) {
        m_topRightComponent = topRightComponent;
        m_topBar = topBar;
        m_bottomRightComponent = bottomRightComponent;
        m_leftComponent = leftComponent;
        m_bottomBar = bottomBar;
        registerLinker();
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        if (config.isEnableDragAndDrop()) {
            dndListener = new DNDListener();
        }
        if (m_bottomBar != null) {
            m_bottomBar.initWithLinker(this);
        }
        if (m_topBar != null) {
            m_topBar.initWithLinker(this);
        }
        if (m_leftComponent != null) {
            m_leftComponent.initWithLinker(this);
        }
        if (m_topRightComponent != null) {
            m_topRightComponent.initWithLinker(this);
        }
        if (m_bottomRightComponent != null) {
            m_bottomRightComponent.initWithLinker(this);
        }
    }

    /**
     * Called when the left tree selection changes.
     * When a tree item is selected, by default, we remove the selection
     */
    public void onTreeItemSelected() {
        if (m_leftComponent!= null && m_leftComponent.getSelectedItem() != null) {
            boolean clearSelection = true;
            if (m_bottomRightComponent != null) {
                if (m_bottomRightComponent.getSelection() != null) {
                    for (GWTJahiaNode n :m_bottomRightComponent.getSelection()) {
                        if (n.getPath().substring(0,n.toString().lastIndexOf("/")).equals(m_leftComponent.getSelectedItem().toString())) {
                            clearSelection = false;
                            break;
                        }
                    }
                }
                if (m_bottomRightComponent.getComponentType().equals(PICKER)) {
                    if (clearSelection && m_leftComponent != null) {
                        m_bottomRightComponent.emptySelection();
                    }
                } else {
                    m_bottomRightComponent.fillData(m_leftComponent.getSelectedItem());
                }
            }
            if (m_topRightComponent != null) {
                if (clearSelection) {
                    m_topRightComponent.clearSelection();
                }
                m_topRightComponent.setContent(m_leftComponent.getSelectedItem());
            }
            handleNewSelection();
        }
    }

    /**
     * Called when the table selection changes.
     */
    public void onTableItemSelected() {
        handleNewSelection();
        if (m_bottomRightComponent != null && m_topRightComponent!= null && m_topRightComponent.getSelection()!=null) {
            m_bottomRightComponent.fillData(m_topRightComponent.getSelection());
        }
    }


    public void handleNewSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        if (m_topBar != null) {
            m_topBar.handleNewSelection();
        }
    }

    public void handleNewSelection(List<GWTJahiaNode> nodes) {
        selectionContext.setSelectedNodes(nodes);
        selectionContext.refresh(LinkerSelectionContext.BOTH);
        if (m_topBar != null) {
            m_topBar.handleNewSelection();
        }
    }

    public void onTableItemDoubleClicked(Object item) {
        if (m_leftComponent != null) {
            m_leftComponent.openAndSelectItem(item);
        }
    }

    public void refreshTable() {
        if (m_topRightComponent != null) {
            m_topRightComponent.refresh(null);
        }
        if (m_bottomRightComponent != null) {
            m_bottomRightComponent.fillData(m_topRightComponent != null?m_topRightComponent.getHiddenSelection():null);
        }
        handleNewSelection();
    }

    public void loading() {
        if (m_bottomBar != null) {
            m_bottomBar.showBusy();
        }
    }

    public void loading(String msg) {
        if (m_bottomBar != null) {
            m_bottomBar.showBusy(msg);
        }
    }

    public void loaded() {
        if (m_bottomBar != null) {
            m_bottomBar.clear();
        }
    }

    ////////////////////////
    // Selections getters //
    ////////////////////////

    public Object getTreeSelection() {
        if (m_leftComponent != null) {
            return m_leftComponent.getSelectedItem();
        } else {
            return leftPanelSelectionWhenHidden;
        }
    }

    public Object getTableSelection() {
        if (m_topRightComponent != null) {
            return m_topRightComponent.getSelection();
        } else {
            return null;
        }
    }

    ////////////////////////
    // Components getters //
    ////////////////////////

    public Component getBottomRightComponent() {
        if (m_bottomRightComponent != null) {
            return m_bottomRightComponent.getComponent();
        } else {
            return null;
        }
    }

    public Component getLeftComponent() {
        if (m_leftComponent != null) {
            return m_leftComponent.getComponent();
        } else {
            return null;
        }
    }

    public Component getTopRightComponent() {
        if (m_topRightComponent != null) {
            return m_topRightComponent.getComponent();
        } else {
            return null;
        }
    }

    public Component getTopBar() {
        if (m_topBar != null) {
            return m_topBar.getComponent();
        } else {
            return null;
        }
    }

    public Component getBottomBar() {
        if (m_bottomBar != null) {
            return m_bottomBar.getComponent();
        } else {
            return null;
        }
    }

    ////////////////////////
    // Specific getters   //
    ////////////////////////

    public BottomRightComponent getBottomRightObject() {
        return m_bottomRightComponent;
    }

    public LeftComponent getLeftObject() {
        return m_leftComponent;
    }

    public TopRightComponent getTopRightObject() {
        return m_topRightComponent;
    }

    public TopBar getTopObject() {
        return m_topBar;
    }

    public BottomBar getBottomObject() {
        return m_bottomBar;
    }

    public DNDListener getDndListener() {
        return dndListener;
    }

    ////////////////////////
    // Misc methods       //
    ////////////////////////


    public void setLeftPanelSelectionWhenHidden(GWTJahiaNode leftPanelSelectionWhenHidden) {
        this.leftPanelSelectionWhenHidden = leftPanelSelectionWhenHidden;
    }

    public void setSelectPathAfterDataUpdate(List<String> paths) {
        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
        for (String path: paths) {
            GWTJahiaNode n = new GWTJahiaNode();
            n.setPath(path);
            n.setName(path.substring(path.lastIndexOf("/")));
            l.add(n);
        }
        m_topRightComponent.selectNodes(l);
        if (m_bottomRightComponent != null && !l.isEmpty()) {
            m_bottomRightComponent.fillData(l.get(0));
        }
    }

    public void refresh(Map<String, Object> data) {
        if (m_leftComponent != null) {
            m_leftComponent.refresh(data);
        }
        refreshTable();
    }

    public void select(Object o) {
        List<GWTJahiaNode> nodes = null;
        if (o != null) {
            if (m_leftComponent != null) {
                m_leftComponent.openAndSelectItem(o);
            }
            if (m_bottomRightComponent != null) {
                m_bottomRightComponent.clear();
            }
            if (m_topRightComponent != null) {
                m_topRightComponent.setContent(o);
            }
            if (o instanceof GWTJahiaNode) {
                nodes = new ArrayList<GWTJahiaNode>();
            } else if (o instanceof List) {
                nodes = ((List<GWTJahiaNode>) o);
            }
            handleNewSelection(nodes);
        } else {
            handleNewSelection();
        }
    }

    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    public void syncSelectionContext(int context) {
        if (getTreeSelection() instanceof GWTJahiaNode) {
            selectionContext.setMainNode((GWTJahiaNode) getTreeSelection());

            // Store the last path used for this config to be able to reopen it
            Storage storage = Storage.getLocalStorageIfSupported();
            if (storage != null) {
                storage.setItem("lastSavedPath_" + getConfig().getName() + "_" + JahiaGWTParameters.getSiteKey(), ((GWTJahiaNode) getTreeSelection()).getPath());
            }
        }
        List<GWTJahiaNode> list = null;
        if ( getTableSelection() != null) {
            list = (List<GWTJahiaNode>) getTableSelection();
        }
        selectionContext.setSelectedNodes(list);
        selectionContext.refresh(context);
    }

    public GWTConfiguration getConfig() {
        return config;
    }

    public void setLocale (GWTJahiaLanguage locale) {
        JahiaGWTParameters.setLanguage(locale);
    }

    public void switchLanguage(GWTJahiaLanguage locale) {
        setLocale(locale);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(Linker.REFRESH_ALL, true);
        refresh(data);
    }

    public boolean isDisplayHiddenTypes() {
        return displayHiddenTypes;
    }

    public void setDisplayHiddenTypes(boolean displayHiddenTypes) {
        this.displayHiddenTypes = displayHiddenTypes;
    }

    public boolean isDisplayHiddenProperties() {
        return displayHiddenProperties;
    }

    public void setDisplayHiddenProperties(boolean displayHiddenProperties) {
        this.displayHiddenProperties = displayHiddenProperties;
    }
}

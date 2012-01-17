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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.Component;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.tripanel.*;
import java.util.ArrayList;
import java.util.List;

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
                    if (clearSelection && m_leftComponent!= null) {
                        m_bottomRightComponent.emptySelection();
                    }
                }   else {
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
            m_topRightComponent.refresh();
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
        if (l.size() > 0) {
            m_bottomRightComponent.fillData(l.get(0));
        }

    }

    public void refresh(int flag) {
        if (m_leftComponent != null) {
            if ((Linker.REFRESH_ALL & flag) != 0) {
                m_leftComponent.refresh(Linker.REFRESH_OPEN_FOLDER);
            } else {
                m_leftComponent.refresh(flag);
            }
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
        refresh(Linker.REFRESH_ALL);
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

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
package org.jahia.ajax.gwt.client.widget.tripanel;

import com.extjs.gxt.ui.client.widget.Component;

/**
 * This is the linker that allow communication between all the browser components.
 *
 * @author rfelden
 * @version 19 juin 2008 - 10:45:20
 */
public class BrowserLinker {

    // Components
    private LeftComponent m_leftComponent;
    private TopRightComponent m_topRightComponent;
    private BottomRightComponent m_bottomRightComponent;
    private TopBar m_topBar;
    private BottomBar m_bottomBar;

    public BrowserLinker() {}

    /**
     * deprecated please use no args constructor and then registerComponents method
     * @param leftComponent a left tree browser (can be null)
     * @param topRightComponent a top table or tree table (cannot be null)
     * @param bottomRightComponent a bottom panel displaying details (can be null)
     * @param topBar a toolbar for interaction(cannot be null)
     * @param bottomBar a status bar displaying short info on current events
     */
    public BrowserLinker(LeftComponent leftComponent, TopRightComponent topRightComponent, BottomRightComponent bottomRightComponent, TopBar topBar, BottomBar bottomBar) {
        m_topRightComponent = topRightComponent;
        m_topBar = topBar;
        m_bottomRightComponent = bottomRightComponent;
        m_leftComponent = leftComponent;
        m_bottomBar = bottomBar;
        registerLinker();
    }

    /**
     *
     * @param leftComponent a left tree browser (can be null)
     * @param topRightComponent a top table or tree table (cannot be null)
     * @param bottomRightComponent a bottom panel displaying details (can be null)
     * @param topBar a toolbar for interaction(cannot be null)
     * @param bottomBar a status bar displaying short info on current events
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
        if (m_bottomBar != null) {
            m_bottomBar.initWithLinker(this) ;
        }
        if (m_topBar != null) {
            m_topBar.initWithLinker(this) ;
        }
        if (m_leftComponent != null) {
            m_leftComponent.initWithLinker(this) ;
        }
        if (m_topRightComponent != null) {
            m_topRightComponent.initWithLinker(this) ;
        }
        if (m_bottomRightComponent != null) {
            m_bottomRightComponent.initWithLinker(this) ;
        }
    }

    /**
     * Called when the left tree selection changes.
     */
    public void onTreeItemSelected() {
        if (m_bottomRightComponent != null) {
            m_bottomRightComponent.clear();
        }
        if (m_topRightComponent != null && m_leftComponent != null) {
            m_topRightComponent.setContent(m_leftComponent.getSelectedItem());
        }
        if (m_topBar != null) {
            m_topBar.handleNewSelection(m_leftComponent != null ? m_leftComponent.getSelectedItem() : null, m_topRightComponent != null ? m_topRightComponent.getSelection() : null);
        }
    }

    /**
     * Called when the table selection changes.
     */
    public void onTableItemSelected() {
        if (m_topBar != null) {
            m_topBar.handleNewSelection(m_leftComponent != null ? m_leftComponent.getSelectedItem() : null, m_topRightComponent != null ? m_topRightComponent.getSelection() : null);
        }
        if (m_bottomRightComponent != null && m_topRightComponent != null) {
            m_bottomRightComponent.fillData(m_topRightComponent.getSelection());
        }
    }

    public void handleNewSelection() {
        if (m_topBar != null) {
            m_topBar.handleNewSelection(m_leftComponent != null ? m_leftComponent.getSelectedItem() : null, m_topRightComponent != null ? m_topRightComponent.getSelection() : null);
        }
    }

    public void onTableItemDoubleClicked(Object item) {
        if (m_leftComponent != null) {
            m_leftComponent.openAndSelectItem(item);
        }
    }

    public void onTreeItemInserted() {
        if (m_leftComponent != null) {
            m_leftComponent.refresh() ;
        }
    }

    public void refreshAll() {
        if (m_leftComponent != null) {
            m_leftComponent.refresh();
        }
        refreshTable();
    }

    public void refreshTable() {
        if (m_topRightComponent != null) {
            m_topRightComponent.refresh();
        }
        if (m_bottomRightComponent != null) {
            m_bottomRightComponent.clear();
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
            return m_leftComponent.getSelectedItem() ;
        } else {
            return null ;
        }
    }

    public Object getTableSelection() {
        if (m_topRightComponent != null) {
            return m_topRightComponent.getSelection() ;
        } else {
            return null ;
        }
    }

    ////////////////////////
    // Components getters //
    ////////////////////////
    public Component getBottomRightComponent() {
        if (m_bottomRightComponent != null) {
            return m_bottomRightComponent.getComponent();
        } else {
            return null ;
        }
    }

    public Component getLeftComponent() {
        if (m_leftComponent != null) {
            return m_leftComponent.getComponent();
        } else {
            return null ;
        }
    }

    public Component getTopRightComponent() {
        if (m_topRightComponent != null) {
            return m_topRightComponent.getComponent();
        } else {
            return null ;
        }
    }

    public Component getTopBar() {
        if (m_topBar != null) {
            return m_topBar.getComponent();
        } else {
            return null ;
        }
    }

    public Component getBottomBar() {
        if (m_bottomBar != null) {
            return m_bottomBar.getComponent();
        } else {
            return null ;
        }
    }

    ////////////////////////
    // Specific getters   //
    ////////////////////////
    public BottomRightComponent getBottomRightObject() {
        return m_bottomRightComponent ;
    }

    public LeftComponent getLeftObject() {
            return m_leftComponent ;
    }

    public TopRightComponent getTopRightObject() {
            return m_topRightComponent ;
    }

    public TopBar getTopObject() {
            return m_topBar ;
    }

    public BottomBar getBottomObject() {
        return m_bottomBar ;
    }

    ////////////////////////
    // Misc methods       //
    ////////////////////////
    public void setSelectPathAfterDataUpdate(String path) {
        m_topRightComponent.setSelectPathAfterDataUpdate(path);
    }
}

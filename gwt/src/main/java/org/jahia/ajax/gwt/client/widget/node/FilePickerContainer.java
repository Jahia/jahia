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
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;

import java.util.List;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:47:12
 */
public class FilePickerContainer extends TopRightComponent {

    private ContentPanel m_component ;
    private FileTreeTable m_treeTable ;
    private ThumbView m_thumbs ;
    private SearchTable m_search ;
    private FilePathBar pathBar ;
    private TabPanel tabs ;
    private TabItem treeTable ;
    private TabItem search ;
    private TabItem thumbs;

    public FilePickerContainer(String rootPath, String startPath, ManagerConfiguration config, String callback, boolean allowThumbs) {
        m_treeTable = new FileTreeTable(rootPath, startPath, config);
        m_search = new SearchTable(config) ;
        m_thumbs = new ThumbView(config) ;
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);
        pathBar = new FilePathBar(startPath, config, callback, allowThumbs) ;

        tabs = new TabPanel() ;
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        treeTable = new TabItem(Messages.getResource("fm_browse")) ;
        treeTable.setBorders(false);
        search = new TabItem(Messages.getResource("fm_search")) ;
        search.setBorders(false);
        thumbs = new TabItem(Messages.getResource("fm_thumbs")) ;
        thumbs.setBorders(false);

        treeTable.setLayout(new FitLayout());
        treeTable.add(m_treeTable.getComponent()) ;
        tabs.add(treeTable) ;

        search.setLayout(new FitLayout());
        search.add(m_search.getComponent()) ;
        tabs.add(search) ;

        thumbs.setLayout(new FitLayout());
        thumbs.add(m_thumbs.getComponent()) ;
        tabs.add(thumbs) ;

        thumbs.addListener(Events.Select, new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                if (m_treeTable.getSelection() != null) {
                    List<GWTJahiaNode> selection = (List<GWTJahiaNode>) m_treeTable.getSelection() ;
                    if (selection.size() > 0) {
                        GWTJahiaNode selectedItem = selection.get(0) ;
                        GWTJahiaNode parent = selectedItem.getParent() ;
                        if (selection.size() == 1) {
                            if (selectedItem.isFile()) {
                                if (parent != null) {
                                    m_thumbs.setContent(parent);
                                } else {
                                    m_thumbs.clearTable();
                                }
                            } else {
                                m_thumbs.setContent(selectedItem);
                            }
                        } else if (parent != null){
                            m_thumbs.setContent(parent);
                        } else {
                            m_thumbs.clearTable();
                        }
                    } else {
                        m_thumbs.clearTable();
                    }
                }
            }
        });

        m_component.add(tabs) ;
        m_component.setTopComponent(pathBar.getComponent());
        m_component.setBottomComponent(new FilterStatusBar(config.getFilters(), config.getMimeTypes(), config.getNodeTypes()));
    }

    public boolean isAllowThumbs() {
        return pathBar.isAllowThumbs() ;
    }

    public void handleNewSelection() {
        pathBar.handleNewSelection(null, getSelection());
    }

    public void initWithLinker(BrowserLinker linker) {
        m_treeTable.initWithLinker(linker);
        m_search.initWithLinker(linker);
        m_thumbs.initWithLinker(linker);
        pathBar.initWithLinker(linker);
    }

    public void initContextMenu() {
        m_treeTable.initContextMenu();
    }

    public void setContent(Object root) {
        // ...
    }

    public void clearTable() {
        m_treeTable.clearTable();
        m_search.clearTable();
        m_thumbs.clearTable();
    }

    public Object getSelection() {
        if (tabs.getSelectedItem() == treeTable) {
            return m_treeTable.getSelection();
        } else if (tabs.getSelectedItem() == search){
            return m_search.getSelection();
        } else if (tabs.getSelectedItem() == thumbs) {
            return m_thumbs.getSelection() ;
        }
        return null ;
    }

    public void refresh() {
        m_treeTable.refresh();
        m_search.refresh();
        m_thumbs.refresh();
    }

    public Component getComponent() {
        return m_component ;
    }

    public void setSelectPathAfterDataUpdate(String path) {
        m_treeTable.setSelectPathAfterDataUpdate(path);
    }

    public void clearSelection() {
        if (tabs.getSelectedItem() == treeTable) {
            m_treeTable.clearSelection();
        } else if (tabs.getSelectedItem() == search){
            m_search.clearSelection();
        } else if (tabs.getSelectedItem() == thumbs) {
            m_thumbs.clearSelection();
        }
    }
}

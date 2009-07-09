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

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.TabPanelEvent;

import java.util.List;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:47:12
 */
public class ContentPickerContainer extends TopRightComponent {

    private ContentPanel m_component ;
    private ContentTreeTable m_treeTable ;
    private ThumbView m_thumbs ;
    private SearchTable m_search ;
    private ContentPathBar pathBar ;
    private TabPanel tabs ;
    private TabItem treeTable ;
    private TabItem search ;
    private TabItem thumbs;

    public ContentPickerContainer(String rootPath, String startPath, ManagerConfiguration config, String callback, boolean allowThumbs) {
        m_treeTable = new ContentTreeTable(rootPath, startPath, config);
        m_search = new SearchTable(config) ;
        m_thumbs = new ThumbView(config) ;
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);
        pathBar = new ContentPathBar(startPath, config, callback, allowThumbs) ;

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

        thumbs.addListener(Events.Select, new SelectionListener<TabPanelEvent>() {
            public void componentSelected(TabPanelEvent componentEvent) {
                if (m_treeTable.getSelection() != null) {
                    List<GWTJahiaNode> selection = (List<GWTJahiaNode>) m_treeTable.getSelection() ;
                    if (selection.size() > 0) {
                        GWTJahiaNode selectedItem = selection.get(0) ;
                        GWTJahiaNode parent = (GWTJahiaNode) selectedItem.getParent() ;
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

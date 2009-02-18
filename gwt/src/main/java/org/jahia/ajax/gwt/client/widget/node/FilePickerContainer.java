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

package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.widget.node.FilePathBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.util.nodes.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * User: rfelden
 * Date: 21 oct. 2008 - 16:47:12
 */
public class FilePickerContainer extends TopRightComponent {

    private ContentPanel m_component ;
    private FileTreeTable m_treeTable ;
    private SearchTable m_search ;
    private FilePathBar pathBar ;
    TabPanel tabs ;
    TabItem treeTable ;
    TabItem search ;
    TabItem create ;

    public FilePickerContainer(String rootPath, String startPath, ManagerConfiguration config, String callback, boolean allowThumbs) {
        m_treeTable = new FileTreeTable(rootPath, startPath, config);
        m_search = new SearchTable(config) ;
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_component.setHeaderVisible(false);
        pathBar = new FilePathBar(startPath, config, callback, allowThumbs) ;

        tabs = new TabPanel() ;
        tabs.setBodyBorder(false);
        tabs.setBorders(false);
        treeTable = new TabItem("Browse") ;
        treeTable.setBorders(false);
        search = new TabItem("Search") ;
        search.setBorders(false);
        create = new TabItem("Create") ;
        create.setBorders(false);

        treeTable.setLayout(new FitLayout());
        treeTable.add(m_treeTable.getComponent()) ;
        tabs.add(treeTable) ;

        search.setLayout(new FitLayout());
        search.add(m_search.getComponent()) ;
        tabs.add(search) ;

        m_component.add(tabs) ;
        m_component.setTopComponent(pathBar.getComponent());
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
    }

    public Object getSelection() {
        if (tabs.getSelectedItem() == treeTable) {
            return m_treeTable.getSelection() ;
        } else {
            return m_search.getSelection() ;
        }
    }

    public void refresh() {
        m_treeTable.refresh();
        m_search.refresh();
    }

    public Component getComponent() {
        return m_component ;
    }

    public void setSelectPathAfterDataUpdate(String path) {
        m_treeTable.setSelectPathAfterDataUpdate(path);
    }

    public void clearSelection() {
        if (tabs.getSelectedItem() == treeTable) {
            m_treeTable.clearSelection(); ;
        } else {
            m_search.clearSelection(); ;
        }
    }
}

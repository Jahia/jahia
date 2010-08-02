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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 août 2009
 */
public class EditLinker implements Linker {

    private GWTEditConfiguration config;
    private GWTJahiaNode sidePanelSelectedNode;
    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private Module selectedModule;
    private EditModeDNDListener dndListener;
    private ActionToolbarLayoutContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;
    private ModuleSelectionListener selectionListener;
    private Widget mainAreaComponent;
    private String locale;

    public EditLinker(MainModule mainModule, SidePanel sidePanel, ActionToolbarLayoutContainer toolbar,
                      GWTEditConfiguration config) {
        this.dndListener = new EditModeDNDListener(this);
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;
        this.config = config;
        registerLinker();
    }


    public ActionToolbarLayoutContainer getToolbar() {
        return toolbar;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public GWTConfiguration getConfig() {
        return config;
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
        JahiaGWTParameters.setLanguage(locale);
    }

    public Module getSelectedModule() {
        return selectedModule;
    }

    public GWTJahiaNode getSidePanelSelectedNode() {
        return sidePanelSelectedNode;
    }

    public void onDisplayGridSelection(GWTJahiaNode node) {
        sidePanelSelectedNode = node;
        handleNewSidePanelSelection();
    }

    public void onBrowseTreeSelection(GWTJahiaNode node) {
        sidePanelSelectedNode = node;
        handleNewSidePanelSelection();
    }

    public void onCreateGridSelection(GWTJahiaNodeType selected) {
        sidePanelSelectedNode = null;
        handleNewSidePanelSelection();
    }

    public ModuleSelectionListener getSelectionListener() {
        return selectionListener;
    }

    public void setSelectionListener(ModuleSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    public void onModuleSelection(Module selection) {
        if (this.selectionListener == null) {
            selectedModule = selection;

            handleNewModuleSelection();
            if (selectedModule != null) {
                selectedModule.setDraggable(true);
            }
        } else {
            selectionListener.onModuleSelection(selection);
        }
    }

    public void refresh(int flag) {
        mainModule.refresh(flag);
        sidePanel.refresh(flag);
//        syncSelectionContext();
//        toolbar.handleNewLinkerSelection();
    }

    public GWTJahiaNode getSelectedNode() {
        if (getSelectedModule() != null) {
            return getSelectedModule().getNode();
        }
        return null;
    }

    public void handleNewModuleSelection() {
        syncSelectionContext();
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewModuleSelection(selectedModule);
        sidePanel.handleNewModuleSelection(selectedModule);
    }

    public void handleNewSidePanelSelection() {
        syncSelectionContext();
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewSidePanelSelection(sidePanelSelectedNode);
        sidePanel.handleNewSidePanelSelection(sidePanelSelectedNode);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        if (mainModule != null) {
            try {
                mainModule.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (sidePanel != null) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (toolbar != null) {
            try {
                toolbar.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
    }


    public void select(Object o) {
        if (o == null || o instanceof Module) {
            onModuleSelection((Module) o);
        }
    }

    public GWTJahiaNode getMainNode() {
        return getMainModule().getNode();
    }

    public List<GWTJahiaNode> getSelectedNodes() {
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        if (getSelectedNode() != null) {
            nodes.add(getSelectedNode());
        }
        return nodes;
    }

    public void loaded() {
        // todo:implements 
    }

    public void loading(String resource) {
        // todo:implements
    }

    public void setSelectPathAfterDataUpdate(String path) {
        // todo:implements
    }

    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    public void syncSelectionContext() {
        selectionContext.setMainNode(getMainNode());
        selectionContext.setSelectedNodes(getSelectedNodes());
        selectionContext.refresh();
    }

    public void replaceMainAreaComponent(Widget w) {
        ContentPanel m;
        if (mainAreaComponent == null) {
            m = (ContentPanel) mainModule.getParent();
            m.remove(mainModule);
        } else {
            m = (ContentPanel) mainAreaComponent.getParent();
            m.remove(mainAreaComponent);
        }
        mainAreaComponent = w;
        m.add(mainAreaComponent, new BorderLayoutData(Style.LayoutRegion.CENTER));
        m.layout();
    }

    public void restoreMainArea() {
        ContentPanel m = (ContentPanel) mainAreaComponent.getParent();
        m.remove(mainAreaComponent);
        mainAreaComponent = null;
        m.add(mainModule, new BorderLayoutData(Style.LayoutRegion.CENTER));
        m.layout();
    }
}

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


import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;


/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 24 août 2009
 */
public class EditLinker {
    private GWTJahiaNode sidePanelSelectedNode;
    private Module selectedModule;
    private Module previouslySelectedModule;
    private EditModeDNDListener dndListener;
    private EditModeToolbarContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;

    private String locale;

    public EditLinker(MainModule mainModule, SidePanel sidePanel, EditModeToolbarContainer toolbar) {
        this.dndListener = new EditModeDNDListener(this);
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;

        registerLinker();
    }


    public EditModeToolbarContainer getToolbar() {
        return toolbar;
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
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

    public void onModuleSelection(Module selection) {
        previouslySelectedModule = selectedModule;
        selectedModule = selection;
        if(previouslySelectedModule!=null) {
            final String path = previouslySelectedModule.getPath();
            final String s = selectedModule.getPath();
            if(!path.equals(s) && path.contains(s)) {
                previouslySelectedModule = null;
                return;
            }
        }
        handleNewModuleSelection();
    }

    public void refresh() {
        mainModule.refresh();
        toolbar.refresh();
    }

    public void handleNewModuleSelection() {
        toolbar.handleNewModuleSelection(selectedModule);
        mainModule.handleNewModuleSelection(selectedModule);
        sidePanel.handleNewModuleSelection(selectedModule);
    }

    public void handleNewSidePanelSelection() {
        toolbar.handleNewSidePanelSelection(sidePanelSelectedNode);
        mainModule.handleNewSidePanelSelection(sidePanelSelectedNode);
        sidePanel.handleNewSidePanelSelection(sidePanelSelectedNode);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        if (mainModule != null) {
            mainModule.initWithLinker(this) ;
        }
        if (sidePanel != null) {
            sidePanel.initWithLinker(this) ;
        }
        if (toolbar != null) {
            toolbar.initWithLinker(this) ;
        }
    }


}

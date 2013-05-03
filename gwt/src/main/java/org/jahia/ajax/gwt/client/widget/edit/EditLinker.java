/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit;


import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.Widget;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaChannel;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanel;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionToolbarLayoutContainer;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;


/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 24 août 2009
 */
public class EditLinker implements Linker {

    private GWTEditConfiguration config;
    private String mainPath;
    private String template;
    private String param;

    private LinkerSelectionContext selectionContext = new LinkerSelectionContext();
    private Module selectedModule;
    private EditModeDNDListener dndListener;
    private ActionToolbarLayoutContainer toolbar;
    private MainModule mainModule;
    private SidePanel sidePanel;
    private ModuleSelectionListener selectionListener;
    private Widget mainAreaComponent;
    private int mainAreaVScrollPosition;
    private String locale;
    private GWTJahiaChannel activeChannel;
    private String activeChannelVariant = null;

    public EditLinker(MainModule mainModule, SidePanel sidePanel, ActionToolbarLayoutContainer toolbar,
                      GWTEditConfiguration config) {
        if (config.isEnableDragAndDrop()) {
            this.dndListener = new EditModeDNDListener(this);
        }
        this.mainModule = mainModule;
        this.sidePanel = sidePanel;
        this.toolbar = toolbar;
        this.config = config;
        registerLinker();
    }

    public SidePanel getSidePanel() {
        return sidePanel;
    }

    public void setSidePanel(SidePanel sidePanel) {
        this.sidePanel = sidePanel;
        if (sidePanel != null) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
    }

    public MainModule getMainModule() {
        return mainModule;
    }

    public GWTEditConfiguration getConfig() {
        return config;
    }

    public void switchConfig(GWTEditConfiguration config, String newPath, boolean updateSidePanel, boolean updateToolbar) {
        this.config = config;

        JahiaGWTParameters.setSiteNode(config.getSiteNode());
        JahiaGWTParameters.setSitesMap(config.getSitesMap());
        JahiaGWTParameters.setChannels(config.getChannels());
        PermissionsUtils.loadPermissions(config.getPermissions());

        mainModule.setConfig(config, newPath);
        if (updateSidePanel) {
            sidePanel.setConfig(config);
        }
        if (updateToolbar) {
            toolbar.setConfig(config);
        }
        EditPanelViewport.setViewportStyleName(config.getName());
        registerLinker(true, updateSidePanel, updateToolbar);
    }

    public EditModeDNDListener getDndListener() {
        return dndListener;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(GWTJahiaLanguage locale) {
        if (locale != null) {
            this.locale = locale.getLanguage();
        } else {
            this.locale = null;
        }
        JahiaGWTParameters.setLanguage(locale);
    }

    public Module getSelectedModule() {
        return selectedModule;
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

    public void onMainSelection(String mainPath, String template, String param) {
        this.mainPath = mainPath;
        this.template = template;
        this.param = param;
    }

    public void refresh(Map<String, Object> data) {
        mainModule.refresh(data);
        if (sidePanel != null) {
            sidePanel.refresh(data);
        }
    }

    public void handleNewModuleSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        toolbar.handleNewLinkerSelection();
        mainModule.handleNewModuleSelection(selectedModule);
        if (sidePanel != null) {
            sidePanel.handleNewModuleSelection(selectedModule);
        }
    }

    public void handleNewMainSelection() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        mainModule.handleNewMainSelection(mainPath,template, param);
        mainModule.handleNewModuleSelection(null);
        toolbar.handleNewLinkerSelection();
        if (sidePanel != null) {
            sidePanel.handleNewMainSelection(mainPath);
        }
    }

    public void handleNewMainNodeLoaded() {
        syncSelectionContext(LinkerSelectionContext.BOTH);
        toolbar.handleNewMainNodeLoaded(mainModule.getNode());
        if (sidePanel != null) {
            sidePanel.handleNewMainNodeLoaded(mainModule.getNode());
        }
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker() {
        registerLinker(true,true,true);
    }

    /**
     * Set up linker (callback for each member).
     */
    protected void registerLinker(boolean doMainModule, boolean doSidePanel, boolean doToolbar) {
        if (mainModule != null && doMainModule) {
            try {
                mainModule.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (sidePanel != null && doSidePanel) {
            try {
                sidePanel.initWithLinker(this);
            } catch (Exception e) {
                Log.error("Error on init linker",e);
            }
        }
        if (toolbar != null && doToolbar) {
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

    public void loaded() {
        mainModule.unmask();
    }

    public void loading(String resource) {
        mainModule.mask(resource, "x-mask-loading");

    }

    public void setSelectPathAfterDataUpdate(List<String> paths) {
        // todo:implements
    }

    public LinkerSelectionContext getSelectionContext() {
        return selectionContext;
    }

    public void syncSelectionContext(int context) {
        selectionContext.setMainNode(getMainModule().getNode());
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        if (mainModule.isCtrlActive()) {
            nodes.addAll(selectionContext.getSelectedNodes());
        }
        if (getSelectedModule() != null && getSelectedModule().getNode() != null && !(getSelectedModule() instanceof MainModule)) {
            nodes.add(getSelectedModule().getNode());
        }
        selectionContext.setSelectedNodes(nodes);
        selectionContext.refresh(context);
    }

    public void replaceMainAreaComponent(Widget w) {
        ContentPanel m;
        if (mainAreaComponent == null) {
            m = (ContentPanel) mainModule.getParent();
            mainAreaVScrollPosition = mainModule.getContainer().getVScrollPosition();
            mainModule.setStyleAttribute("display","none");
        } else {
            m = (ContentPanel) mainAreaComponent.getParent();
            m.remove(mainAreaComponent);
        }
        mainAreaComponent = w;
        m.insert(mainAreaComponent, 0, new BorderLayoutData(Style.LayoutRegion.CENTER));
        m.layout();
    }

    public void restoreMainArea() {
        ContentPanel m = (ContentPanel) mainAreaComponent.getParent();
        m.remove(mainAreaComponent);
        mainAreaComponent = null;
        mainModule.setStyleAttribute("display","block");
        m.layout();
        mainModule.getContainer().setVScrollPosition(mainAreaVScrollPosition);
    }

    public boolean isDisplayHiddenProperties() {
        return false;
    }

    public GWTJahiaChannel getActiveChannel() {
        return activeChannel;
    }

    public void setActiveChannel(GWTJahiaChannel activeChannel) {
        this.activeChannel = activeChannel;
    }

    public String getActiveChannelIdentifier() {
        if (activeChannel != null) {
            return activeChannel.getValue();
        }
        return null;
    }

    public String getActiveChannelVariant() {
        return activeChannelVariant;
    }

    public void setActiveChannelVariant(String activeChannelVariant) {
        this.activeChannelVariant = activeChannelVariant;
    }

    public int getActiveChannelVariantIndex() {
        int result = 0;
        if (getActiveChannel() != null && getActiveChannelVariant() != null) {
            String[] variantValueArray = getActiveChannel().getCapability("variants").split(",");
            for (int i = 0; i < variantValueArray.length; i++) {
                if (variantValueArray[i].equals(getActiveChannelVariant())) {
                    // we found the active variant !
                    result = i;
                    break;
                }
            }
        }
        return result;
    }


}

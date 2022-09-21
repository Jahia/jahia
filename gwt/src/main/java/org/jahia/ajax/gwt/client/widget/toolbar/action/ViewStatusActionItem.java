/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.InfoLayers;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 9 févr. 2010
 */
public abstract class ViewStatusActionItem extends BaseActionItem {
    protected transient InfoLayers infoLayers;
    protected transient Set<InfoLayers.InfoLayer> before;

    @Override
    public MenuItem createMenuItem() {
        return new CheckMenuItem();
    }

    public void onComponentSelection() {
        if (((CheckMenuItem)getMenuItem()).isChecked()) {
            showLayer();
        } else {
            hideLayers(false);
        }
    }

    public void handleNewLinkerSelection() {
    }

    public abstract void viewStatus(List<Module> moduleList);

    protected Listener<ComponentEvent> createRemoveListener() {
        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                hideLayers(false);
            }
        };
        return removeListener;
    }

    private void showLayer() {
        List<Module> modules = ModuleHelper.getModules();
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (!m.getPath().endsWith("*")) {
                list.add(m);
            }
        }

        infoLayers.setMainModule(modules.iterator().next());
        before = new HashSet<InfoLayers.InfoLayer>(infoLayers.getContainers());
        viewStatus(list);
    }

    private void hideLayers(boolean check) {
        Set<InfoLayers.InfoLayer> layers = new HashSet<InfoLayers.InfoLayer>(infoLayers.getContainers());
        layers.removeAll(before);
        ((CheckMenuItem)getMenuItem()).setChecked(check);
        infoLayers.removeAll(layers);
    }

    @Override public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem,linker);
        infoLayers = ((EditLinker) linker).getMainModule().getInfoLayers();
        infoLayers.initWithLinker(linker);
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        super.handleNewMainNodeLoaded(node);
        if (((CheckMenuItem)getMenuItem()).isChecked()) {
            hideLayers(true);
            showLayer();
        }
    }
}

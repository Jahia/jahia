/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.InfoLayers;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.*;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 9 févr. 2010
 */
public abstract class ViewStatusActionItem extends BaseActionItem {
    protected transient InfoLayers infoLayers;
    protected transient ToggleButton button;
    protected transient Set<InfoLayers.InfoLayer> before;

    public void onComponentSelection() {
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

    public void handleNewLinkerSelection() {
    }

    public Component createNewToolItem() {
        button = new ToggleButton();
        return button;
    }

    public abstract void viewStatus(List<Module> moduleList);

    protected Listener<ComponentEvent> createRemoveListener() {
        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Set<InfoLayers.InfoLayer> layers = new HashSet<InfoLayers.InfoLayer>(infoLayers.getContainers());
                layers.removeAll(before);
                infoLayers.removeAll(layers);
                if (button != null) {
                    button.toggle(false);
                }
            }
        };
        return removeListener;
    }

    @Override public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem,linker);
        infoLayers = ((EditLinker) linker).getMainModule().getInfoLayers();
        infoLayers.initWithLinker(linker);
    }
}

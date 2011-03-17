/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 */
public class SiteSwitcherActionItem extends BaseActionItem {
    private static List<SiteSwitcherActionItem> instances = new ArrayList<SiteSwitcherActionItem>();

    private transient ComboBox<GWTJahiaNode> mainComponent;
    private GWTJahiaNode selectedSite;

    private List<String> root = Arrays.asList("/sites/*");

    public void setRoot(List<String> root) {
        this.root = root;
    }

    public SiteSwitcherActionItem() {

    }

    public void setSelectedSite(GWTJahiaNode site) {
        this.selectedSite = site;
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        instances.add(this);
        initMainComponent();
        refreshSitesList(linker);
    }

    public static void refreshAllSitesList(final Linker linker) {
        for (SiteSwitcherActionItem instance : instances) {
            instance.refreshSitesList(linker);
        }
    }

    private void refreshSitesList(final Linker linker) {
        List<GWTJahiaNode> sites = new ArrayList<GWTJahiaNode>(JahiaGWTParameters.getSitesMap().values());
        mainComponent.removeAllListeners();
        mainComponent.getStore().removeAll();
        mainComponent.getStore().add(sites);
        for (GWTJahiaNode site : sites) {
            if (site.getUUID().equals(JahiaGWTParameters.getSiteUUID())) {
                mainComponent.setValue(site);
                break;
            }
        }
        mainComponent.getStore().sort("displayName", Style.SortDir.ASC);

        mainComponent.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                final GWTJahiaNode jahiaNode = event.getSelection().get(0);
                JahiaGWTParameters.setSite(jahiaNode, linker);
                ((EditLinker) linker).getSidePanel().refresh(EditLinker.REFRESH_ALL);
                if (root.get(0).startsWith("/templateSets")) {
                    ((EditLinker) linker).onMainSelection(jahiaNode.getPath(), null, null);
                } else {
                    ((EditLinker) linker).onMainSelection(jahiaNode.getPath() + "/home", null, null);
                }
            }
        });
    }

    /**
     * init main component
     */
    private void initMainComponent() {
        mainComponent = new ComboBox<GWTJahiaNode>();
        mainComponent.setStore(new ListStore<GWTJahiaNode>());
        mainComponent.setDisplayField("displayName");
        mainComponent.setValueField("uuid");
        mainComponent.setTypeAhead(true);
        mainComponent.setTriggerAction(ComboBox.TriggerAction.ALL);
        mainComponent.setForceSelection(true);
        mainComponent.setValue(selectedSite);
        setEnabled(true);
    }


    @Override
    public Component getCustomItem() {
        return mainComponent;
    }


    @Override
    public void setEnabled(boolean enabled) {
        mainComponent.setEnabled(enabled);
    }

}
/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.*;

/**
 * User: toto
 * Date: Feb 4, 2010
 * Time: 4:19:51 PM
 */
public class SiteSwitcherActionItem extends BaseActionItem {
    private static List<SiteSwitcherActionItem> instances = new ArrayList<SiteSwitcherActionItem>();

    private transient LayoutContainer mainComponent;
    private transient ComboBox<GWTJahiaNode> sitesCombo;
    private transient SimpleComboBox<String> modulesCombo;
    private boolean useModuleType = false;
    private String defaultModuleType = "";

    private Map<String, String> modulesTypes;

    private List<String> root = Arrays.asList("/sites/*");

    public void setRoot(List<String> root) {
        this.root = root;
    }

    public SiteSwitcherActionItem() {

    }

    public boolean isUseModuleType() {
        return useModuleType;
    }

    public void setUseModuleType(boolean useModuleType) {
        this.useModuleType = useModuleType;
    }

    public String getDefaultModuleType() {
        return defaultModuleType;
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (isUseModuleType()) {
            mainComponent = new VerticalPanel();
        } else {
            mainComponent = new HorizontalPanel();
        }
        instances.add(this);
        if (useModuleType) {
            createModuleTypeCombo();
            mainComponent.add(modulesCombo);
        }
        createSitesCombo();
        mainComponent.add(sitesCombo);
        if (useModuleType) {
            modulesCombo.setSimpleValue(Messages.get("moduleType." + defaultModuleType + ".label", defaultModuleType));
        }
        refreshSitesList(linker, defaultModuleType);
    }

    private void createModuleTypeCombo() {
        modulesTypes = new HashMap<String, String>();
        modulesCombo = new SimpleComboBox<String>();
        modulesCombo.setTypeAhead(true);
        modulesCombo.getListView().setStyleAttribute("font-size","11px");
        modulesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        modulesCombo.setForceSelection(true);
        modulesCombo.setWidth(200);
        List<GWTJahiaNode> sites = new ArrayList<GWTJahiaNode>(JahiaGWTParameters.getSitesMap().values());
        modulesCombo.removeAllListeners();
        modulesCombo.getStore().removeAll();
        // fill modules type
        Set<String> moduleSet = new LinkedHashSet<String>();
        for (GWTJahiaNode s : sites) {
            String moduleType;
            if (s.getProperties().get("j:siteType") != null && !(moduleType = (String) s.getProperties().get("j:siteType")).equals(Constants.MODULE_TYPE_SYSTEM)) {
                String r = Messages.get("moduleType." + moduleType + ".label", moduleType);
                modulesTypes.put(r, moduleType);
                moduleSet.add(r);
            }
        }
        modulesCombo.add(new ArrayList<String>(moduleSet));
        defaultModuleType = (String) JahiaGWTParameters.getSiteNode().getProperties().get("j:siteType");
        modulesCombo.getStore().sort("value", Style.SortDir.ASC);
        modulesCombo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> e) {
                refreshAllSitesList(linker, modulesTypes.get(e.getSelectedItem().getValue()));
            }
        });
        setEnabled(true);
    }

    public static void refreshAllSitesList(final Linker linker) {
        for (SiteSwitcherActionItem instance : instances) {
            instance.refreshSitesList(linker, instance.getDefaultModuleType());
        }
    }

    public static void refreshAllSitesList(final Linker linker, String moduleType) {
        for (SiteSwitcherActionItem instance : instances) {
            instance.refreshSitesList(linker, moduleType);
        }
    }

    private void refreshSitesList(final Linker linker, String moduleType) {
        List<GWTJahiaNode> sites = new ArrayList<GWTJahiaNode>();
        for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
            if (!n.getName().equals("systemsite")) {
                if (moduleType.equals("") || moduleType.equals(n.getProperties().get("j:siteType"))) {
                    sites.add(n);
                }
            }
        }
        sitesCombo.removeAllListeners();
        sitesCombo.getStore().removeAll();
        sitesCombo.getStore().add(sites);
        Set<String> siteNames = new LinkedHashSet<String>();
        boolean b = true;
        for (GWTJahiaNode site : sites) {
            String displayName = site.getDisplayName();
            if (siteNames.contains(site.getDisplayName())) {
                displayName += " (" + site.getSiteKey() + ")";
            }

            if (site.get("j:versionInfo") != null) {
                displayName += " (" + site.get("j:versionInfo") + ")";
            }

            site.set("switcherDisplayName", displayName);
            siteNames.add(site.getDisplayName());
            if (site.getUUID().equals(JahiaGWTParameters.getSiteUUID())) {
                sitesCombo.setValue(site);
                b = !b;
            }
        }
        sitesCombo.getStore().sort("switcherDisplayName", Style.SortDir.ASC);

        sitesCombo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                final GWTJahiaNode jahiaNode = event.getSelection().get(0);
                if (jahiaNode.get("j:languages") != null &&
                    !((List<String>) jahiaNode.get("j:languages")).contains(JahiaGWTParameters.getLanguage())) {
                    ((EditLinker) linker).setLocale((GWTJahiaLanguage) jahiaNode.get(GWTJahiaNode.DEFAULT_LANGUAGE));
                }
                JahiaGWTParameters.setSite(jahiaNode, linker);
                ((EditLinker) linker).getSidePanel().refresh(EditLinker.REFRESH_ALL);
                if (root.get(0).startsWith("/templateSets")) {
                    ((EditLinker) linker).onMainSelection(jahiaNode.getPath(), null, null);
                } else {
                    ((EditLinker) linker).onMainSelection((String) jahiaNode.get(GWTJahiaNode.HOMEPAGE_PATH), null, null);
                }
            }
        });
        // Change the value to the first one of the list if not set before
        if (b) {
            sitesCombo.setValue(sitesCombo.getStore().getAt(0));
        }
    }

    /**
     * init main component
     */
    private void createSitesCombo() {
        sitesCombo = new ComboBox<GWTJahiaNode>();
        sitesCombo.setStore(new ListStore<GWTJahiaNode>());
        sitesCombo.setDisplayField("switcherDisplayName");
        sitesCombo.setValueField("uuid");
        sitesCombo.setTypeAhead(true);
        sitesCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
        sitesCombo.setForceSelection(true);
        sitesCombo.getListView().setStyleAttribute("font-size","11px");
        if (useModuleType) {
            sitesCombo.setWidth(250);
        } else {
            sitesCombo.setWidth(200);
        }
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
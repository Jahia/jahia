/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

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
    private boolean filterOnAvailableSources = false;

    private List<String> root = Arrays.asList("/sites/*");
    private boolean filterEditModeBlockedSites = false;

    public void setRoot(List<String> root) {
        this.root = root;
    }

    public SiteSwitcherActionItem() {

    }

    public boolean isFilterOnAvailableSources() {
        return filterOnAvailableSources;
    }

    public void setFilterOnAvailableSources(boolean filterOnAvailableSources) {
        this.filterOnAvailableSources = filterOnAvailableSources;
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (isFilterOnAvailableSources()) {
            mainComponent = new VerticalPanel();
        } else {
            mainComponent = new HorizontalPanel();
        }
        instances.add(this);

        createSitesCombo();
        mainComponent.add(sitesCombo);
        refreshSitesList(linker);
    }

    public static void reloadAndRefreshAllSitesList(String sitesLocation, final Linker linker) {
        JahiaContentManagementService.App.getInstance().getRoot(Arrays.asList(sitesLocation), Arrays.asList("jnt:virtualsite"), null, null, GWTJahiaNode.DEFAULT_SITEMAP_FIELDS, null, null, false, false, null, null, true, new BaseAsyncCallback<List<GWTJahiaNode>>() {
            @Override
            public void onSuccess(List<GWTJahiaNode> result) {
                Map<String, GWTJahiaNode> sitesMap = new HashMap<String, GWTJahiaNode>();
                for (GWTJahiaNode aSite : result) {
                    sitesMap.put(aSite.getSiteUUID(), aSite);
                }
                JahiaGWTParameters.setSitesMap(sitesMap);
                refreshAllSitesList(linker);
            }
        });
    }


    public static void refreshAllSitesList(final Linker linker) {
        for (SiteSwitcherActionItem instance : instances) {
            instance.refreshSitesList(linker);
        }
    }

    private void refreshSitesList(final Linker linker) {
        List<GWTJahiaNode> sites = new ArrayList<GWTJahiaNode>();
        for (GWTJahiaNode n : JahiaGWTParameters.getSitesMap().values()) {
            if (!filterEditModeBlockedSites || (n.get(GWTJahiaNode.EDIT_MODE_BLOCKED) != null && !(Boolean)n.get(GWTJahiaNode.EDIT_MODE_BLOCKED))) {
                if (!filterOnAvailableSources || n.get("j:sourcesFolder") != null) {
                    sites.add(n);
                }
            }
        }
        sitesCombo.removeAllListeners();
        sitesCombo.getStore().removeAll();
        sitesCombo.setValue(null);
        sitesCombo.getStore().add(sites);
        Set<String> siteNames = new LinkedHashSet<String>();

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
            }
        }
        sitesCombo.getStore().sort("switcherDisplayName", Style.SortDir.ASC);

        sitesCombo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaNode>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaNode> event) {
                EditLinker editLinker = null;
                if (linker instanceof EditLinker) {
                    editLinker = (EditLinker) linker;
                } else if (linker instanceof SidePanelTabItem.SidePanelLinker) {
                    editLinker = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
                }

                final GWTJahiaNode jahiaNode;

                if (event.getSelection().size() == 0) {
                    GWTJahiaNode node = null;
                    for (GWTJahiaNode currentNode : JahiaGWTParameters.getSitesMap().values()) {
                        if (currentNode.getPath().equals("/sites/systemsite")) {
                            node = currentNode;
                            break;
                        }
                    }
                    jahiaNode = node;
                } else {
                    jahiaNode = event.getSelection().get(0);
                }

                final EditLinker finalEditLinker = editLinker;
                JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(jahiaNode.getPath()), GWTJahiaNode.DEFAULT_SITE_FIELDS,
                        new BaseAsyncCallback<List<GWTJahiaNode>>() {
                            /**
                             * Called when an asynchronous call completes successfully.
                             *
                             * @param result the return value of the remote produced call
                             */
                            @Override
                            public void onSuccess(List<GWTJahiaNode> result) {
                                GWTJahiaNode siteNode = result.get(0);
                                if (finalEditLinker != null && siteNode.get("j:languages") != null && ((List<String>) siteNode.get("j:languages")).size() > 0 &&
                                        !((List<String>) siteNode.get("j:languages")).contains(JahiaGWTParameters.getLanguage()) &&
                                        siteNode.get(GWTJahiaNode.DEFAULT_LANGUAGE) != null) {
                                    finalEditLinker.setLocale((GWTJahiaLanguage) siteNode.get(
                                            GWTJahiaNode.DEFAULT_LANGUAGE));
                                }
                                JahiaGWTParameters.setSiteNode(siteNode);
                                if (finalEditLinker != null && finalEditLinker.getSidePanel() != null) {
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_ALL, true);
                                    finalEditLinker.getSidePanel().refresh(data);
                                }
                                if (root.get(0).startsWith("/modules")) {
                                    if (!siteNode.getPath().equals("/sites/systemsite")) {
                                        MainModule.staticGoTo(siteNode.getPath(), null);
                                    } else if (finalEditLinker != null) {
                                        finalEditLinker.handleNewMainSelection();
                                    }
                                } else if (finalEditLinker != null && !finalEditLinker.getMainModule().getPath().startsWith(siteNode.getPath() + "/")) {
                                    MainModule.staticGoTo((String) siteNode.get(GWTJahiaNode.HOMEPAGE_PATH), null);
                                } else if (finalEditLinker != null) {
                                    finalEditLinker.handleNewMainSelection();
                                    finalEditLinker.handleNewMainNodeLoaded();
                                }
                            }
                        });
            }
        });
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
        sitesCombo.getListView().setStyleAttribute("font-size", "11px");
        sitesCombo.setAllowBlank(true);
//        if (filterOnAvailableSources) {
//            sitesCombo.setWidth(250);
//        } else {
        sitesCombo.setWidth(200);
//        }
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

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        String path = JahiaGWTParameters.getSitesMap().get(node.getSiteUUID()).getPath();
        String s = node.getPath();
        if (s.contains("@/") && s.startsWith("/sites")) {
            path = s.substring(0, s.indexOf('/', 7));
        }
        for (GWTJahiaNode site : sitesCombo.getStore().getModels()) {
            if (site.getPath().equals(path)) {
                sitesCombo.setValue(site);
                return;
            }
        }
        sitesCombo.setValue(null);
    }

    public void setFilterEditModeBlockedSites(boolean filterEditModeBlockedSites) {
        this.filterEditModeBlockedSites = filterEditModeBlockedSites;
    }
}
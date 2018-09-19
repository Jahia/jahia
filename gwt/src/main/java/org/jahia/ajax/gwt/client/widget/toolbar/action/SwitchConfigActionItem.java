/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.List;
import java.util.Set;


/**
 * Toolbar action item for switching between editing modes.
 */
@SuppressWarnings("serial")
public class SwitchConfigActionItem extends NodeTypeAwareBaseActionItem {
    private boolean checkPermissionOnSite;
    private String configurationName;
    private boolean updateSidePanel =true;
    private boolean updateToolbar =true;
    private String enforcedWorkspace = "default";
    private boolean forceRootChange = false;
    private boolean updateOnMainNodeRefresh = false;
    private Set<String> noChecksInMode;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem,linker);
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            if (!MainModule.getInstance().getConfig().getName().equals(configurationName)) {
                getTextToolItem().setEnabled(true);
                getGwtToolbarItem().setSelected(false);
            } else {
                getGwtToolbarItem().setSelected(true);
            }
        }
    }

    public boolean isUpdateSidePanel() {
        return updateSidePanel;
    }

    public void setUpdateSidePanel(boolean updateSidePanel) {
        this.updateSidePanel = updateSidePanel;
    }

    public boolean isUpdateToolbar() {
        return updateToolbar;
    }

    public void setUpdateToolbar(boolean updateToolbar) {
        this.updateToolbar = updateToolbar;
    }

    public void setNoChecksInMode(Set<String> noChecksInMode) {
        this.noChecksInMode = noChecksInMode;
    }

    @Override
    public void handleNewMainNodeLoaded(GWTJahiaNode node) {
        if (!updateOnMainNodeRefresh || (noChecksInMode != null && noChecksInMode.contains(linker.getConfig().getName()))) {
            setEnabled(true);
        } else {
            setEnabled(isNodeTypeAllowed(node) && !((EditLinker) linker).isInSettingsPage() && hasPermission(checkPermissionOnSite ? JahiaGWTParameters.getSiteNode() : node)) ;
        }
    }

    /**
     * Performs switch to the specified edit mode configuration.
     */
    @Override
    public void onComponentSelection() {
        if (!configurationName.equals(MainModule.getInstance().getConfig().getName())) {
            linker.loading(Messages.get("label.loading", "Loading..."));
            final Storage storage = Storage.getSessionStorageIfSupported();

            final boolean useSamePath = linker.getConfig().getSamePathConfigsList().contains(configurationName);
            String path = null;
            if (useSamePath && !forceRootChange && linker.getSelectionContext().getMainNode() != null) {
                path = linker.getSelectionContext().getMainNode().getPath();
            } else if (storage != null) {
                path = storage.getItem(configurationName + "_nodePath");
            }

            JahiaContentManagementService.App.getInstance().getEditConfiguration(path, configurationName, enforcedWorkspace, new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    if (gwtEditConfiguration.getDefaultLocation() == null) {
                        linker.loaded();
                        Window.alert(Messages.getWithArgs("label.gwt.error", "Error: {}", new Object[]{ Messages.get("label.noAvailableSites")} ));
                    } else {
                        String newPath;
                        if (useSamePath) {
                            newPath = null;
                        } else {
                            if (storage != null && storage.getItem(gwtEditConfiguration.getName() + "_path") != null) {
                                newPath = storage.getItem(gwtEditConfiguration.getName() + "_path");
                                GWTJahiaNode siteNode = gwtEditConfiguration.getSiteNode();
                                // set locale to the locale from the url
                                MatchResult m = RegExp.compile(gwtEditConfiguration.getDefaultUrlMapping()+"frame/default/([a-zA-Z_]+)/.*").exec(newPath);
                                if (m != null) {
                                    List<GWTJahiaLanguage> langs = (List<GWTJahiaLanguage>) siteNode.get(GWTJahiaNode.SITE_LANGUAGES);
                                    for (GWTJahiaLanguage lang : langs) {
                                        if (lang.getLanguage().equals(m.getGroup(1))) {
                                            ((EditLinker) linker).setLocale(lang);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                // set locale to the site locale
                                String currentLocale = ((EditLinker) linker).getLocale();
                                GWTJahiaNode siteNode = gwtEditConfiguration.getSiteNode();
                                List<String> languages = siteNode.get("j:languages");
                                if ((languages == null || !languages.contains(currentLocale))) {
                                    ((EditLinker) linker).setLocale((GWTJahiaLanguage) siteNode.get(GWTJahiaNode.DEFAULT_LANGUAGE));
                                }
                                newPath = MainModule.getInstance().getBaseUrl() + gwtEditConfiguration.getDefaultLocation();
                            }
                            newPath = removeWebflowParameter(newPath);
                        }

                        EditLinker editLinker = ((EditLinker) linker);
                        if (replaceSwitchByOpen() && newPath != null) {
                            // replace config in url and remove "frame" suffix
                            newPath = newPath.replaceFirst(editLinker.getConfig().getDefaultUrlMapping(), gwtEditConfiguration.getDefaultUrlMapping())
                                    .replaceFirst("frame/default/", "/default/");

                            Window.Location.assign(newPath);
                        } else {
                            editLinker.switchConfig(gwtEditConfiguration, newPath, updateSidePanel, updateToolbar, enforcedWorkspace);
                        }
                    }
                }

                public void onApplicationFailure(Throwable throwable) {
                    linker.loaded();
                    Window.alert(Messages.getWithArgs("label.gwt.error", "Error: {}", new Object[] {throwable.getMessage()}));
                    Log.error("Error when loading EditConfiguration", throwable);
                }
            });
        }
    }

    private String removeWebflowParameter(String path) {
        if (path == null) {
            return null;
        }
        int index = path.indexOf("webflowexecution");
        String newPath = path;
        if (index > -1) {
            newPath = path.substring(0, index);
            index = path.indexOf("&", index);
            if (index > -1) {
                newPath += path.substring(index + 1);
            }
            if (newPath.endsWith("?") || newPath.endsWith("&")) {
                newPath = newPath.substring(0, newPath.length() - 1);
            }
        }
        return newPath;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    @Override
    public Component createNewToolItem() {
        ToggleButton toggleButton = new ToggleButton();
        toggleButton.setToggleGroup("switchConfig");
        if (getGwtToolbarItem().isSelected()) {
            toggleButton.setAllowDepress(false);
        }
        return toggleButton;
    }

    public void setEnforcedWorkspace(String enforcedWorkspace) {
        this.enforcedWorkspace = enforcedWorkspace;
    }

    public void setForceRootChange(boolean forceRootChange) {
        this.forceRootChange = forceRootChange;
    }

    public void setUpdateOnMainNodeRefresh(boolean updateOnMainNodeRefresh) {
        this.updateOnMainNodeRefresh = updateOnMainNodeRefresh;
    }

    public void setCheckPermissionOnSite(boolean checkPermissionOnSite) {
        this.checkPermissionOnSite = checkPermissionOnSite;
    }

    private static native boolean replaceSwitchByOpen () /*-{
        return !!$wnd.jahiaReplaceSwitchConfigByOpen;
    }-*/;
}

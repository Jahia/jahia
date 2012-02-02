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

package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.*;

import com.extjs.gxt.ui.client.widget.form.Field;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends AbstractContentEngine {

    private String contentPath;

    private Button ok;
    private Map<String, GWTJahiaGetPropertiesResult> langCodeGWTJahiaGetPropertiesResultMap =
            new HashMap<String, GWTJahiaGetPropertiesResult>();

    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker, EngineContainer engineContainer) {
        super(linker.getConfig().getEngineTabs(), linker, node.getPath().substring(0, node.getPath().lastIndexOf('/')));
        contentPath = node.getPath();
        nodeName = node.getName();
        init(engineContainer);
        loadEngine();

        //setTopComponent(toolBar);
    }

    public EditContentEngine(String path, Linker linker, EngineContainer engineContainer) {
        super(linker.getConfig().getEngineTabs(), linker, path.substring(0, path.lastIndexOf('/')));
        contentPath = path;
        init(engineContainer);
        loadEngine();
    }

    public void close() {
        contentService.closeEditEngine(contentPath,new BaseAsyncCallback<Object>() {
            public void onSuccess(Object result) {
            }
        });
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : config) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.getRequiredPermission() == null || PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), node)) {
                if ((tabItem.getHideForTypes().isEmpty() || !node.isNodeType(tabItem.getHideForTypes())) &&
                        (tabItem.getShowForTypes().isEmpty() || node.isNodeType(tabItem.getShowForTypes()))) {
                    tabs.add(tabItem.create(tabConfig, this));
                }
            }
        }
        tabs.setSelection(tabs.getItem(0));
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        ok = new Button(Messages.get("label.save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
		ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent event) {
				save(true);
			}
		});
        ok.setEnabled(false);
        buttonBar.add(ok);

        /* ToDo: activate restore button in the engine

        restore = new Button(Messages.getResource("label.restore"));
        restore.setIconStyle("gwt-icons-restore");
        restore.setEnabled(false);

        if (existingNode) {
            restore.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    propertiesEditor.resetForm();
                }
            });
            addButton(this.restore);
        }*/
        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                close();
            }
        });
        buttonBar.add(cancel);

    }

    /**
     * load node
     */
    private void loadProperties() {
        contentService.getProperties(contentPath, getSelectedLanguage(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                fillCurrentTab();
            }
        });

    }

    /**
     * load node
     */
    private void loadEngine() {

        contentService.initializeEditEngine(contentPath , true, new BaseAsyncCallback<GWTJahiaEditEngineInitBean>() {
            public void onSuccess(GWTJahiaEditEngineInitBean result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                langCodeGWTJahiaGetPropertiesResultMap.put(currentLanguageBean.getLanguage(), result);
                acl = result.getAcl();
                referencesWarnings = result.getReferencesWarnings();
                if(!PermissionsUtils.isPermitted("jcr:modifyProperties", node)) {
                    heading = Messages.getWithArgs("label.edit.engine.heading.read.only","Read {0} ({1})",new String[]{nodeName, nodeTypes.get(0).getLabel()});
                } else {
                    heading = Messages.getWithArgs("label.edit.engine.heading.edit","Edit {0} ({1})",new String[]{nodeName, nodeTypes.get(0).getLabel()});
                }
                container.getPanel().setHeading(heading);
                if (node.isLocked()) {
                    String infos = "";
                    if (node.getLockInfos().containsKey(null) && node.getLockInfos().size() == 1) {
                        for (String s : node.getLockInfos().get(null)) {
                            if(s.startsWith("label.")) {
                                infos = Messages.get(s);
                            } else {
                                infos += s.substring(0,s.indexOf(":")) + " (" + s.substring(s.indexOf(":")+1) + ") ";
                            }
                        }
                    } else {
                        for (Map.Entry<String, List<String>> entry : node.getLockInfos().entrySet()) {
                            if (entry.getKey() != null) {
                                if (infos.length() > 0) {
                                    infos += "; ";
                                }
                                infos += entry.getKey() + " : ";
                                int i = 0;
                                for (String s : entry.getValue()) {
                                    if (i > 0) {
                                        infos += ", ";
                                    }
                                    if (s.startsWith("label.")) {
                                        infos += Messages.get(s);
                                    } else {
                                        infos += s.substring(0, s.indexOf(":")) + " (" + s.substring(s.indexOf(":") + 1) + ") ";
                                    }
                                    i++;
                                }
                            }
                        }
                    }
                    heading = heading + "&nbsp;" + Messages.getWithArgs("label.edit.engine.heading.locked.by","[ locked by {0} ]",new String[]{infos});
                    container.getPanel().setHeading(heading);
                } else if (node.getLockInfos() != null) {
                    heading = heading + "&nbsp;" + Messages.get("label.edit.engine.heading.locked.by.you","[ locked by you ]");
                    container.getPanel().setHeading(heading);                    
                }
                
                setAvailableLanguages(result.getAvailabledLanguages());

                // set selectedNode as processed
                if (getSelectedLanguage() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguage(), result);
                }

                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                fillCurrentTab();

                if (PermissionsUtils.isPermitted("jcr:modifyProperties",node) && !node.isLocked()) {
                    ok.setEnabled(true);
                }
                unmask();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
                Info.display(Messages.get("label.error", "Error"),throwable.getLocalizedMessage());
                EditContentEngine.this.container.closeEngine();
            }

            @Override
            public void onSessionExpired() {
                EditContentEngine.this.container.closeEngine();
            }
        });

    }

    /**
     * on language chnage, reload the node
     * @param previous
     */
    protected void onLanguageChange(GWTJahiaLanguage previous) {
        if (previous != null) {
            final String lang = previous.getLanguage();
            for (TabItem item : tabs.getItems()) {
                if (!changedI18NProperties.containsKey(lang)) {
                    changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                }
                Object itemData = item.getData("item");
                if (itemData instanceof EditEngineTabItem) {
                    ((EditEngineTabItem)itemData).onLanguageChange(getSelectedLanguage());
                }
            }
        }
        GWTJahiaGetPropertiesResult result = langCodeGWTJahiaGetPropertiesResultMap.get(getSelectedLanguage());
        if (result == null) {
            loadProperties();
        } else {
            node = result.getNode();
            nodeTypes = result.getNodeTypes();
            properties = result.getProperties();
            currentLanguageBean = result.getCurrentLocale();
            fillCurrentTab();
        }
    }

    
    protected void setButtonsEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
    }

	@Override
    protected void prepareAndSave(boolean closeAfterSave) {
        // node
        List<GWTJahiaNode> orderedChildrenNodes = null;

        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            // case of contentTabItem
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    Field<String> name = ((ContentTabItem) item).getName();
                    if(!name.isValid()) {
                        com.google.gwt.user.client.Window.alert(Messages.get(
                                "label.error.system.name.mandatory", "System name is mandatory and could not be empty"));
                        unmask();
                        ok.setEnabled(true);
                        return;
                    }
                    nodeName = name.getValue();
                    node.setName(nodeName);
                }
            }

            if (item instanceof ListOrderingContentTabItem) {
                // if the manual ranking was activated update new ranking
                orderedChildrenNodes = ((ListOrderingContentTabItem) item).getNewManualOrderedChildrenList();
            }

            // case of right tab
            item.doSave(node, changedProperties, changedI18NProperties, addedTypes, removedTypes, acl);
        }

        node.getNodeTypes().removeAll(removedTypes);
        node.getNodeTypes().addAll(addedTypes);

        contentService.saveNode(node,
                orderedChildrenNodes, acl, changedI18NProperties, changedProperties,
                removedTypes, new BaseAsyncCallback<Object>() {
                    public void onApplicationFailure(Throwable throwable) {
                        String message = throwable.getMessage();
                        if (message.contains("Invalid link")) {
                            message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                        }
                        com.google.gwt.user.client.Window.alert(Messages.get("label.error.invalidlink", "Properties save failed") + "\n\n"
                                + message);
                        Log.error("failed", throwable);
                        unmask();
                        ok.setEnabled(true);
                    }

                    public void onSuccess(Object o) {
                        Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                        int refresh = Linker.REFRESH_MAIN + Linker.REFRESH_MAIN_IMAGES;
                        EditLinker l = null;
                        if (linker instanceof SidePanelTabItem.SidePanelLinker) {
                            l = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
                        } else if (linker instanceof EditLinker) {
                            l = (EditLinker) linker;
                        }
                        if (l != null && node.equals(l.getMainModule().getNode()) && !node.getName().equals(l.getMainModule().getNode().getName())) {
                            l.getMainModule().handleNewMainSelection(node.getPath().substring(0, node.getPath().lastIndexOf("/")+1)+node.getName(), l.getMainModule().getTemplate(), null);
                            refresh += Linker.REFRESH_PAGES;
                        }
                        if (node.isPage() || node.getNodeTypes().contains("jnt:externalLink")
                            || node.getNodeTypes().contains("jnt:nodeLink")
                            || node.getNodeTypes().contains("jnt:template") || node.getInheritedNodeTypes().contains("jnt:template")
                            || node.getInheritedNodeTypes().contains("jmix:visibleInPagesTree")) {
                            refresh += Linker.REFRESH_PAGES;
                        }
                        EditContentEngine.this.container.closeEngine();
                        linker.refresh(refresh);
                    }
                });
        
    }

    @Override
    public String toString() {
        return node.getPath();
    }
}

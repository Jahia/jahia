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

package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;

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
                if ((tabItem.getHideForTypes().isEmpty() || !tabItem.getHideForTypes().contains(node.getNodeTypes().get(0))) &&
                        (tabItem.getShowForTypes().isEmpty() || tabItem.getShowForTypes().contains(node.getNodeTypes().get(0)))) {
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
                acl = result.getAcl();
                referencesWarnings = result.getReferencesWarnings();
                if(!PermissionsUtils.isPermitted("jcr:modifyProperties", node)) {
                    heading = Messages.getWithArgs("label.edit.engine.heading.read.only","Read {0} ({1})",new String[]{nodeName, nodeTypes.get(0).getLabel()});
                } else {
                    heading = Messages.getWithArgs("label.edit.engine.heading.edit","Edit {0} ({1})",new String[]{nodeName, nodeTypes.get(0).getLabel()});
                }
                container.getPanel().setHeading(heading);
                if (node.isLocked()) {
                    heading = heading + "&nbsp;" + Messages.getWithArgs("label.edit.engine.heading.locked.by","[ locked by {0} ]",new String[]{node.getLockOwner()});
                    container.getPanel().setHeading(heading);
                } else if (node.getLockOwner() != null) {
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
                Info.display("",throwable.getLocalizedMessage());
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
                if (itemData instanceof PropertiesTabItem) {
                    PropertiesTabItem propertiesTabItem = (PropertiesTabItem) itemData;
                    changedI18NProperties.get(lang).addAll(propertiesTabItem.getLanguageProperties(true, lang));
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

        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = propertiesTabItem.getPropertiesEditor();
                if (pe != null) {                        
                    //properties.addAll(pe.getProperties());
                    node.getNodeTypes().removeAll(pe.getRemovedTypes());
                    node.getNodeTypes().addAll(pe.getAddedTypes());
                    node.getNodeTypes().addAll(pe.getExternalMixin());
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    if (getSelectedLanguage() != null) {
                        final String lang = getSelectedLanguage();
                        if (!changedI18NProperties.containsKey(lang)) {
                            changedI18NProperties.put(lang, new ArrayList<GWTJahiaNodeProperty>());
                        }

                        changedI18NProperties.get(lang).addAll(propertiesTabItem.getLanguageProperties(true, lang));
                    }
                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties(false, true, true));
                    }
                } else {
                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties(true, true, true));
                    }
                }

                // case of contentTabItem
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        nodeName = ((ContentTabItem) item).getName().getValue();
                        node.setName(nodeName);
                    }
                }

                if (item instanceof ListOrderingContentTabItem) {

                    // if the manual ranking was activated update new ranking
                    orderedChildrenNodes = ((ListOrderingContentTabItem) item).getNewManualOrderedChildrenList();
                }


            }
            // case of right tab
            else if (item instanceof RolesTabItem) {
                AclEditor acl = ((RolesTabItem) item).getRightsEditor();
                if (acl != null) {
                    newNodeACL = acl.getAcl();
                }
            } else {
                item.doSave(node, changedProperties, changedI18NProperties);
            }
        }
        
        contentService.saveNode(node,
                orderedChildrenNodes, newNodeACL, changedI18NProperties, changedProperties,
                new BaseAsyncCallback<Object>() {
                    public void onApplicationFailure(Throwable throwable) {
                        com.google.gwt.user.client.Window.alert(Messages.get(
                                "saved_prop_failed", "Properties save failed\n\n")
                                + throwable.getLocalizedMessage());
                        Log.error("failed", throwable);
                        unmask();
                        ok.setEnabled(true);
                    }

                    public void onSuccess(Object o) {
                        Info.display("", Messages.get("saved_prop", "Properties saved\n\n"));
                        EditContentEngine.this.container.closeEngine();
                        linker.refresh(Linker.REFRESH_MAIN);
                    }
                });
        
    }
}

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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:55:28 PM
 * 
 */
public class CreateContentEngine extends AbstractContentEngine {

    protected GWTJahiaNodeType type = null;
    protected String targetName = null;
    protected boolean createInParentAndMoveBefore = false;
    private Button ok;
    private Button okAndNew;
    /**
     * Open Edit content engine for a new node creation
     *
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param props                       initial values for properties
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public CreateContentEngine(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore, EngineContainer engineContainer) {
        super(linker.getConfig().getEngineTabs(), linker, createInParentAndMoveBefore ? parent.getPath().substring(0, parent.getPath().lastIndexOf('/')) : parent.getPath());
        this.existingNode = false;
        this.targetNode = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;
        this.setId("JahiaGxtCreateContentEngine");
        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        presetProperties = new HashMap<String, GWTJahiaNodeProperty>(props);
        heading = Messages.get("label.add", "Add") + ": " + type.getLabel();
        init(engineContainer);
        loadEngine();

    }

    public void close() {
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : config) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.getRequiredPermission() == null ||
                PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), targetNode)) {
                if (tabItem.isHandleCreate() &&
                    (tabItem.getHideForTypes().isEmpty() || !tabItem.getHideForTypes().contains(type.getName())) &&
                    (tabItem.getShowForTypes().isEmpty() || tabItem.getShowForTypes().contains(type.getName()))) {
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
        ok.addSelectionListener(new CreateSelectionListener());

        buttonBar.add(ok);

        okAndNew = new Button(Messages.get("properties.saveAndNew.label", "Save and new"));
        okAndNew.setHeight(BUTTON_HEIGHT);
        okAndNew.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());

        okAndNew.addSelectionListener(new CreateAndAddNewSelectionListener());
        buttonBar.add(okAndNew);

        Button cancel = new Button(Messages.get("label.cancel", "Cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                close();
            }
        });
        buttonBar.add(cancel);

        setButtonsEnabled(false);
    }

    /**
     * on language chnage, fill currentAzble
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
        fillCurrentTab();
    }


    /**
     * load mixin
     */
    private void loadEngine() {
        contentService.initializeCreateEngine(nodeTypes.iterator().next().getName(), parentPath, targetName, new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                acl = result.getAcl();
                final List<GWTJahiaLanguage> languages = result.getLanguages();
                setAvailableLanguages(languages);
                setButtonsEnabled(true);

                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                nodeName = result.getDefaultName();

                fillCurrentTab();
                
                unmask();                
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to load avalibale mixin", caught);
            }
        });
    }
    
    protected class CreateSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(true);
        }
    }

    protected class CreateAndAddNewSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            save(false);
        }
    }

    protected void prepareAndSave(final boolean closeAfterSave) {
        String nodeName = targetName;
        GWTJahiaNodeACL newNodeACL = new GWTJahiaNodeACL();
        newNodeACL.setAce(new ArrayList<GWTJahiaNodeACE>());

        final Set<String> addedTypes = new HashSet<String>();
        for (TabItem tab : tabs.getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    nodeName = ((ContentTabItem) item).getName().getValue();
                }
            }

            item.doSave(node, changedProperties, changedI18NProperties, addedTypes, new HashSet<String>(), newNodeACL);
        }
        
		doSave(nodeName, changedProperties, changedI18NProperties, new ArrayList<String>(addedTypes), newNodeACL,
		        closeAfterSave);
    }
    
    protected void doSave(String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        final AsyncCallback<GWTJahiaNode> callback = new BaseAsyncCallback<GWTJahiaNode>() {
            public void onApplicationFailure(Throwable throwable) {
                String message = throwable.getMessage();
                if (message.contains("Invalid link")) {
                    message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                }
                com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n"
                        + message);
                Log.error("failed", throwable);
                unmask();
                setButtonsEnabled(true);
            }

            public void onSuccess(GWTJahiaNode node) {
                if (closeAfterSave) {
					Info.display(
					        Messages.get("label.information", "Information"),
					        Messages.get(
					                "org.jahia.engines.contentmanager.addContentWizard.formCard.success.save",
					                "Content node created successfully:")
					                + " " + node.getName());
                    CreateContentEngine.this.container.closeEngine();

                    if (node.isPage() || node.getNodeTypes().contains("jnt:externalLink")
                            || node.getNodeTypes().contains("jnt:nodeLink")
                            || node.getNodeTypes().contains("jnt:template") || node.getInheritedNodeTypes().contains("jnt:template")
                            || node.getInheritedNodeTypes().contains("jmix:visibleInPagesTree")) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                        linker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                    } else {
                        linker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_FOLDERS);
                    }
                } else {
                    CreateContentEngine.this.tabs.removeAll();
                    CreateContentEngine.this.initTabs();
                    CreateContentEngine.this.tabs.setSelection(tabs.getItem(0));
                    CreateContentEngine.this.layout(true);
                    unmask();
                    setButtonsEnabled(true);
                }
            }
        };
        if (createInParentAndMoveBefore) {
            contentService.createNodeAndMoveBefore(targetNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else {
            contentService.createNode(parentPath, nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        }
    }

    protected void setButtonsEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
        okAndNew.setEnabled(enabled);
    }
}

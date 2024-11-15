/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.TabItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private boolean systemNameReadOnly;

    private int childCount;
    private int listLimit;

    /**
     * Open Edit content engine for a new node creation
     *  @param configuration
     * @param linker                      The linker
     * @param parent                      The parent node where to create the new node - if createInParentAndMoveBefore, the node is sibling
     * @param type                        The selected node type of the new node
     * @param props                       initial values for properties
     * @param targetName                  The name of the new node, or null if automatically defined
     * @param createInParentAndMoveBefore
     */
    public CreateContentEngine(GWTEngineConfiguration configuration, Linker linker, GWTJahiaNode parent, GWTJahiaNodeType type, Map<String, GWTJahiaNodeProperty> props, String targetName, boolean createInParentAndMoveBefore, EngineContainer engineContainer, boolean skipRefreshOnSave, boolean systemNameReadOnly) {
        super(configuration, linker, createInParentAndMoveBefore ? parent.getPath().substring(0, parent.getPath().lastIndexOf('/')) : parent.getPath(), skipRefreshOnSave);
        this.existingNode = false;
        this.systemNameReadOnly = systemNameReadOnly;
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
        addStyleName("create-content-engine");
        loadEngine();

    }

    @Override
    public void close() {
        super.close();
        JahiaGWTParameters.removeEngineLanguage();
        container.closeEngine();
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (GWTEngineTab tabConfig : config.getEngineTabs()) {
            EditEngineTabItem tabItem = tabConfig.getTabItem();
            if (tabConfig.showInEngine() && (tabConfig.getRequiredPermission() == null ||
                    PermissionsUtils.isPermitted(tabConfig.getRequiredPermission(), JahiaGWTParameters.getSiteNode()))) {
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
    @Override
    protected void initFooter() {
        for (ButtonItem buttonItem : config.getCreationButtons()) {
            BoxComponent button = buttonItem.create(this);
            buttons.add(button);
            buttonBar.add(button);
        }
        for (ButtonItem buttonItem : config.getCommonButtons()) {
            buttonBar.add(buttonItem.create(this));
        }

        List<Module> parentModules = ModuleHelper.getModulesByPath() != null ? ModuleHelper.getModulesByPath().get(parentPath) : null;
        if (parentModules != null && parentModules.size() == 1) {
            Module module = parentModules.get(0);
            childCount = module.getChildCount();
            listLimit = module.getListLimit();
        } else {
            childCount = 0;
            listLimit = -1;
        }

        setButtonsEnabled(false);
    }

    /**
     * on language change, fill current tab
     * @param previous
     */
    @Override
    protected void onLanguageChange(GWTJahiaLanguage previous) {
        handleLanguageChange(previous);
        fillCurrentTab();
    }

    /**
     * load mixin
     */
    private void loadEngine() {
        JahiaContentManagementService.App.getInstance().initializeCreateEngine(nodeTypes.iterator().next().getName(), parentPath, targetName, new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {

            @Override
            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                if (closed) {
                    return;
                }
                mixin = result.getMixin();
                choiceListInitializersValues = result.getChoiceListInitializersValues();
                defaultValues = result.getDefaultValues();
                currentLanguageBean = result.getCurrentLocale();
                defaultLanguageCode = result.getDefaultLanguageCode();
                acl = result.getAcl();
                final List<GWTJahiaLanguage> languages = result.getLanguages();
                setAvailableLanguages(languages);
                setButtonsEnabled(true);

                initTabs();

                tabs.addListener(Events.Select, new Listener<ComponentEvent>() {

                    @Override
                    public void handleEvent(ComponentEvent event) {
                        fillCurrentTab();
                    }
                });

                nodeName = result.getDefaultName();

                fillCurrentTab();
                updateWipControls();
                loaded();
            }

            @Override
            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to load avalibale mixin", caught);
            }
        });
    }

    @Override
    public void setButtonsEnabled(final boolean enabled) {
        for (BoxComponent button : buttons) {
            button.setEnabled(enabled);
        }
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public boolean isCreateInParentAndMoveBefore() {
        return createInParentAndMoveBefore;
    }

    public GWTJahiaNodeType getType() {
        return type;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public int getListLimit() {
        return listLimit;
    }

    public boolean isSystemNameReadOnly() {
        return systemNameReadOnly;
    }

    @Override
    protected boolean isNodeOfJmixLastPublishedType() {
        return super.isNodeOfJmixLastPublishedType() || this.getType().getSuperTypes().contains("jmix:lastPublished");
    }
}

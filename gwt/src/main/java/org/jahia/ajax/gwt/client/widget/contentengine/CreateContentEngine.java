/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaCreateEngineInitBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:55:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateContentEngine extends AbstractContentEngine {

    protected GWTJahiaNodeType type = null;
    protected String targetName = null;
    protected boolean createInParentAndMoveBefore = false;
    private Button ok;
    private Button okAndNew;
    private Button cancel;


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
        super(getCreateConfig(type, linker.getConfig()), linker);
        this.existingNode = false;
        this.parentNode = parent;
        this.type = type;
        if (!"*".equals(targetName)) {
            this.targetName = targetName;
        }
        this.createInParentAndMoveBefore = createInParentAndMoveBefore;

        nodeTypes = new ArrayList<GWTJahiaNodeType>(1);
        nodeTypes.add(type);
        properties = new HashMap<String, GWTJahiaNodeProperty>(props);
        heading = "Create " + type.getLabel();
        init(engineContainer);
        loadEngine();

    }

    public void close() {
        container.closeEngine();
    }

    public static GWTEngine getCreateConfig(GWTJahiaNodeType type, GWTConfiguration config) {
        for (GWTEngine engine : config.getCreateEngines()) {
            if (type.getName().equals(engine.getNodeType()) || type.getSuperTypes().contains(engine.getNodeType())) {
                return engine;
            }
        }
        return null;
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

        okAndNew = new Button(Messages.get("properties.saveAndNew.label"));
        okAndNew.setHeight(BUTTON_HEIGHT);
        okAndNew.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());

        okAndNew.addSelectionListener(new CreateAndAddNewSelectionListener());
        buttonBar.add(okAndNew);

        cancel = new Button(Messages.get("label.cancel"));
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
     */
    protected void onLanguageChange() {
        fillCurrentTab();
    }


    /**
     * load mixin
     */
    private void loadEngine() {
        contentService.initializeCreateEngine(nodeTypes.iterator().next().getName(), parentNode.getPath(), new BaseAsyncCallback<GWTJahiaCreateEngineInitBean>() {
            public void onSuccess(GWTJahiaCreateEngineInitBean result) {
                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                defaultLanguageBean = result.getCurrentLocale();

                final List<GWTJahiaLanguage> languages = result.getLanguages();
                setAvailableLanguages(languages);
                setButtonsEnabled(true);

                fillCurrentTab();

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

    protected void save(final boolean closeAfterSave) {
        String nodeName = targetName;
        final List<String> mixin = new ArrayList<String>();
        mask(Messages.get("label.saving","Saving..."), "x-mask-loading");
        setButtonsEnabled(false);
        for (TabItem item : tabs.getItems()) {
            if (item instanceof PropertiesTabItem) {
                PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                PropertiesEditor pe = ((PropertiesTabItem) item).getPropertiesEditor();
                if (pe != null) {
                    // props.addAll(pe.getProperties());
                    mixin.addAll(pe.getAddedTypes());
                    mixin.addAll(pe.getTemplateTypes());
                }

                // handle multilang
                if (propertiesTabItem.isMultiLang()) {
                    // for now only contentTabItem  has multilang. properties
                    changedI18NProperties.putAll(propertiesTabItem.getLangPropertiesMap(false));
                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties(false, true, false));
                    }
                } else {
                    if (pe != null) {
                        changedProperties.addAll(pe.getProperties());
                    }
                }
                if (item instanceof ContentTabItem) {
                    if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                        String nodeNameValue = ((ContentTabItem) item).getName().getValue();
                        nodeName = "Automatically Created (you can type your name here if you want)".equals(nodeNameValue) ? targetName : nodeNameValue;
                    }
                }
            } else if (item instanceof RightsTabItem) {
                AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                if (acl != null) {
                    newNodeACL = acl.getAcl();
                }
            } else if (item instanceof CategoriesTabItem) {
                ((CategoriesTabItem) item).updateProperties(changedProperties, mixin);
            } else if (item instanceof TagsTabItem) {
                ((TagsTabItem) item).updateProperties(changedI18NProperties, mixin);
            }
        }

        doSave(nodeName, changedProperties, changedI18NProperties, mixin, newNodeACL, closeAfterSave);
    }

    protected void doSave(String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        final AsyncCallback<GWTJahiaNode> callback = new BaseAsyncCallback<GWTJahiaNode>() {
            public void onApplicationFailure(Throwable throwable) {
                com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                Log.error("failed", throwable);
            }

            public void onSuccess(GWTJahiaNode node) {
                if (closeAfterSave) {
                    Info.display("", "Node " + node.getName() + " created");
                    CreateContentEngine.this.container.closeEngine();
                } else {
                    CreateContentEngine.this.tabs.removeAll();
                    CreateContentEngine.this.initTabs();
                    CreateContentEngine.this.tabs.setSelection(tabs.getItem(0));
                    CreateContentEngine.this.layout(true);
                    unmask();
                    setButtonsEnabled(true);
                }


                if (node.isPage()) {
                    linker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_PAGES);
                } else {
                    linker.refresh(Linker.REFRESH_MAIN);
                }
            }
        };
        if (createInParentAndMoveBefore) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(parentNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else {
            JahiaContentManagementService.App.getInstance().createNode(parentNode.getPath(), nodeName, type.getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        }
    }

    private void setButtonsEnabled(final boolean enabled) {
        ok.setEnabled(enabled);
        okAndNew.setEnabled(enabled);
    }

}

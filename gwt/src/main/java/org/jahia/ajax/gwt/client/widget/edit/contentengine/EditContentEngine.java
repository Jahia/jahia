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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaEditEngineInitBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends AbstractContentEngine {

    private String contentPath;

    private Button ok;
    private String nodeName;
    private Map<String, GWTJahiaGetPropertiesResult> langCodeGWTJahiaGetPropertiesResultMap =
            new HashMap<String, GWTJahiaGetPropertiesResult>();

    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker, EngineContainer engineContainer) {
        super(getEditConfig(node, (GWTEditConfiguration) linker.getConfig()), linker);
        contentPath = node.getPath();
        nodeName = node.getName();
        heading = "Edit " + nodeName;
        loadEngine();
        init(engineContainer);

        //setTopComponent(toolBar);
    }

    public static GWTEngine getEditConfig(GWTJahiaNode node, GWTEditConfiguration config) {
        for (GWTEngine engine : config.getEditEngines()) {
            if (node.getNodeTypes().contains(engine.getNodeType()) ||
                    node.getInheritedNodeTypes().contains(engine.getNodeType())) {
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
        ok.setEnabled(false);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
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
                contentService.setLock(Arrays.asList(contentPath),false,new BaseAsyncCallback() {
                    public void onSuccess(Object result) {
                        String s = Messages.get("label.unlocked", "Nodes has been unlocked");
                        Info.display(s,s);
                    }
                });
                EditContentEngine.this.container.closeEngine();
            }
        });
        buttonBar.add(cancel);
    }

    /**
     * load node
     */
    private void loadProperties() {
        contentService.getProperties(contentPath, getSelectedLanguageCode(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();
                defaultLanguageBean = result.getCurrentLocale();

                // set selectedNode as processed
                if (getSelectedLanguageCode() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguageCode(), result);
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
                defaultLanguageBean = result.getCurrentLocale();

                if (node.isLocked()) {
                    heading = heading + " [ locked by "+node.getLockOwner() + " ]";
                    container.getPanel().setHeading(heading);
                }
                
                // set selectedNode as processed
                if (getSelectedLanguageCode() != null) {
                    langCodeGWTJahiaGetPropertiesResultMap.put(getSelectedLanguageCode(), result);
                }

                setAvailableLanguages(result.getAvailabledLanguages());

                mixin = result.getMixin();
                initializersValues = result.getInitializersValues();
                fillCurrentTab();
                if (node.isWriteable() && !node.isLocked()) {
                    ok.setEnabled(true);
                }
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
     */
    protected void onLanguageChange() {
        GWTJahiaGetPropertiesResult result = langCodeGWTJahiaGetPropertiesResultMap.get(getSelectedLanguageCode());
        if (result == null) {
            for (TabItem item : tabs.getItems()) {
                if (item instanceof PropertiesTabItem) {
                    PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                    changedI18NProperties.putAll(propertiesTabItem.getLangPropertiesMap(true));
                    break;
                }
            }
            loadProperties();
        } else {
            node = result.getNode();
            nodeTypes = result.getNodeTypes();
            properties = result.getProperties();
            defaultLanguageBean = result.getCurrentLocale();
            for (TabItem item : tabs.getItems()) {
                if (item instanceof PropertiesTabItem) {
                    PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                    changedI18NProperties.putAll(propertiesTabItem.getLangPropertiesMap(true));
                    break;
                }
            }
            fillCurrentTab();
        }
    }


    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public SaveSelectionListener() {
        }

        public void componentSelected(ButtonEvent event) {

            // node
            List<GWTJahiaNode> orderedChildrenNodes = null;

            for (TabItem item : tabs.getItems()) {
                if (item instanceof PropertiesTabItem) {
                    PropertiesTabItem propertiesTabItem = (PropertiesTabItem) item;
                    PropertiesEditor pe = propertiesTabItem.getPropertiesEditor();
                    if (pe != null) {
                        //properties.addAll(pe.getProperties());
                        node.getNodeTypes().removeAll(pe.getRemovedTypes());
                        node.getNodeTypes().addAll(pe.getAddedTypes());
                        node.getNodeTypes().addAll(pe.getTemplateTypes());
                    }

                    // handle multilang
                    if (propertiesTabItem.isMultiLang()) {
                        // for now only contentTabItem  has multilang. properties
                        changedI18NProperties.putAll(propertiesTabItem.getLangPropertiesMap(true));
                        if (pe != null) {
                            changedProperties.addAll(pe.getProperties(false, true, true));
                        }
                    } else {
                        if (pe != null) {
                            changedProperties.addAll(pe.getProperties(true, true, true));
                        }
                    }

                    // case od contentTabItem
                    if (item instanceof ContentTabItem) {
                        if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                            nodeName = ((ContentTabItem) item).getName().getValue();
                        }


                    }

                    // case od contentTabItem
                    if (item instanceof ListOrderingContentTabItem) {

                        // if the manual ranking was activated update new ranking
                        orderedChildrenNodes = ((ListOrderingContentTabItem) item).getNewManualOrderedChildrenList();
                    }


                }
                // case of right tab
                else if (item instanceof RightsTabItem) {
                    AclEditor acl = ((RightsTabItem) item).getRightsEditor();
                    if (acl != null) {
                        newNodeACL = acl.getAcl();
                    }
                }
                // case of classification
                else if (item instanceof CategoriesTabItem) {
                    ((CategoriesTabItem) item).updateProperties(changedProperties, node.getNodeTypes());
                } else if (item instanceof TagsTabItem) {
                        ((TagsTabItem) item).updateProperties(changedProperties, node.getNodeTypes());
                } else if (item instanceof SeoTabItem) {
                    ((SeoTabItem) item).doSave();
                } else if (item instanceof WorkflowTabItem) {
                    ((WorkflowTabItem) item).doSave();
                }
            }

            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().saveNode(node, orderedChildrenNodes, newNodeACL,
                    changedI18NProperties, changedProperties, new BaseAsyncCallback() {
                        public void onApplicationFailure(Throwable throwable) {
                            com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed",
                                    "Properties save failed\n\n") + throwable.getLocalizedMessage());
                            Log.error("failed", throwable);
                        }

                        public void onSuccess(Object o) {
                            Info.display("", Messages.get("saved_prop", "Properties saved\n\n"));
                            EditContentEngine.this.container.closeEngine();
                            linker.refresh(Linker.REFRESH_MAIN);
                        }
                    });
        }

    }

}

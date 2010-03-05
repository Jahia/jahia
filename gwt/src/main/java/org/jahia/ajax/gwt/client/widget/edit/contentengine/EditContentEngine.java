/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Content editing widget.
 *
 * @author Sergiy Shyrkov
 */
public class EditContentEngine extends AbstractContentEngine {

    private String contentPath;

    private Button ok;
    private String nodeName;
    private Map<String, GWTJahiaGetPropertiesResult> langCodeGWTJahiaGetPropertiesResultMap = new HashMap<String, GWTJahiaGetPropertiesResult>();


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public EditContentEngine(GWTJahiaNode node, Linker linker) {
        super(linker);
        contentPath = node.getPath();
        nodeName = node.getName();
        heading = "Edit " + nodeName + " ("+node.getCurrentVersion()+")";
        loadNode(true);
        init();

        //setTopComponent(toolBar);
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        tabs.add(new ContentTabItem(this));
        tabs.add(new LayoutTabItem(this));
        tabs.add(new MetadataTabItem(this));
        tabs.add(new ClassificationTabItem(this));
        tabs.add(new OptionsTabItem(this));
        tabs.add(new RightsTabItem(this));
        tabs.add(new UsagesTabItem(this));
        tabs.add(new PublicationTabItem(this));
        tabs.add(new SeoTabItem(this));
    }

    /**
     * init buttons
     */
    protected void initFooter() {
        ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        /* ToDo: activate restore button in the engine

        restore = new Button(Messages.getResource("fm_restore"));
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
        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                EditContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }

    /**
     * load node
     */
    private void loadNode(final boolean updateAvailableLanguages) {

        contentService.getProperties(contentPath, getSelectedLanguageCode(), new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onFailure(Throwable throwable) {
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

                
                if (updateAvailableLanguages) {
                    setAvailableLanguages(result.getAvailabledLanguages());
                }

                //todo : do this in one pass
                definitionService.getAvailableMixin(node, new AsyncCallback<List<GWTJahiaNodeType>>() {
                    public void onSuccess(List<GWTJahiaNodeType> result) {
                        mixin = result;
                        fillCurrentTab();
                    }

                    public void onFailure(Throwable caught) {

                    }
                });

                fillCurrentTab();
                ok.setEnabled(true);
            }
        });

    }

    /**
     * on language chnage, reload the node
     */
    protected void onLanguageChange() {
        GWTJahiaGetPropertiesResult result = langCodeGWTJahiaGetPropertiesResultMap.get(getSelectedLanguageCode());
        if (result == null) {
            loadNode(false);
        } else {
            node = result.getNode();
            nodeTypes = result.getNodeTypes();
            properties = result.getProperties();
            defaultLanguageBean = result.getCurrentLocale();
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
            final List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(node);

            // general properties
            final List<GWTJahiaNodeProperty> properties = new ArrayList<GWTJahiaNodeProperty>();

            // general properties
            final Map<String, List<GWTJahiaNodeProperty>> langCodeProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();

            // new acl
            GWTJahiaNodeACL newNodeACL = null;

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
                        langCodeProperties.putAll(propertiesTabItem.getLangPropertiesMap(true));
                        if (pe != null) {
                            properties.addAll(pe.getProperties(false, true, true));
                        }
                    } else {
                        if (pe != null) {
                            properties.addAll(pe.getProperties(true,true,true));
                        }
                    }

                    // get node name
                    if (item instanceof ContentTabItem) {
                        if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                            nodeName = ((ContentTabItem) item).getName().getValue();
                        }
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
                else if (item instanceof ClassificationTabItem) {
                    ((ClassificationTabItem) item).updatePropertiesListWithClassificationEditorData(((ClassificationTabItem) item).getClassificationEditor(), properties, node.getNodeTypes());
                } else if (item instanceof SeoTabItem) {
                    ((SeoTabItem) item).doSave();
                }
            }

            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, newNodeACL, langCodeProperties, properties, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "Properties save failed\n\n") + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", Messages.get("saved_prop", "Properties saved\n\n"));
                    EditContentEngine.this.hide();
                    linker.refreshMainComponent();
                }
            });
        }

    }

}

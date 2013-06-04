/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.google.gwt.core.client.Scheduler;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.form.CodeMirrorField;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  Engine Tab Item that contains a Code Editor (CodeMirror)
 */
public class CodeEditorTabItem extends EditEngineTabItem {


    private static final long serialVersionUID = 5207870064789655518L;
    public static final String FONT_SIZE = "font-size";
    public static final String FONT_SIZE_VALUE = "11px";
    private String codePropertyName;
    private String stubType;
    private String codeMirrorMode = "application/x-ejs";

    private transient CodeMirrorField codeField;
    private transient GWTJahiaNodeProperty codeProperty;
    private transient Map<String, List<GWTJahiaValueDisplayBean>> snippets;
    private transient ComboBox<GWTJahiaValueDisplayBean> snippetType;
    private transient ComboBox<GWTJahiaValueDisplayBean> mirrorTemplates;

    public CodeEditorTabItem() {
        setHandleCreate(true);
    }

    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        tab.setLayout(new BorderLayout());
        tab.setScrollMode(Style.Scroll.AUTO);
        final HorizontalPanel horizontalPanel = new HorizontalPanel();
        horizontalPanel.setSpacing(10);
        horizontalPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        tab.add(horizontalPanel, new BorderLayoutData(Style.LayoutRegion.NORTH,40));
//        final HorizontalPanel actionStubs = new HorizontalPanel();
//        actionStubs.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        final HorizontalPanel actions = new HorizontalPanel();
        actions.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
        horizontalPanel.add(actions);
//        horizontalPanel.add(actionStubs);
        if (!tab.isProcessed()) {
            // Add list of properties
            GWTJahiaNodeProperty typeName = engine.getProperties().get("nodeTypeName");
            if (typeName == null) {
                typeName = engine.getPresetProperties().get("nodeTypeName");
            }

            if (engine.getProperties().containsKey(codePropertyName)) {
                codeProperty = engine.getProperties().get(codePropertyName);
            } else {
                codeProperty = new GWTJahiaNodeProperty(codePropertyName, "", GWTJahiaNodePropertyType.STRING);
            }

            Button indentButton = new Button(Messages.get("label.indentAll"));
            indentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent buttonEvent) {
                    if (codeField != null) {
                        codeField.indent();
                    }
                }
            });

            if (stubType != null) {
                snippetType = new ComboBox<GWTJahiaValueDisplayBean>();
                snippetType.setTypeAhead(true);
                snippetType.getListView().setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                snippetType.setTriggerAction(ComboBox.TriggerAction.ALL);
                snippetType.setForceSelection(true);
                snippetType.setWidth(200);
                snippetType.removeAllListeners();
                snippetType.setStore(new ListStore<GWTJahiaValueDisplayBean>());
                snippetType.setAllowBlank(false);
                snippetType.setDisplayField("display");
                snippetType.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                        mirrorTemplates.clear();
                        mirrorTemplates.getStore().removeAll();
                        mirrorTemplates.getStore().add(snippets.get(se.getSelectedItem().getValue()));
                    }
                });

                mirrorTemplates = new ComboBox<GWTJahiaValueDisplayBean>();
                mirrorTemplates.setTypeAhead(true);
                mirrorTemplates.getListView().setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                mirrorTemplates.setTriggerAction(ComboBox.TriggerAction.ALL);
                mirrorTemplates.setForceSelection(true);
                mirrorTemplates.setWidth(300);
                mirrorTemplates.removeAllListeners();
                mirrorTemplates.setStore(new ListStore<GWTJahiaValueDisplayBean>());
                mirrorTemplates.getStore().sort("display", Style.SortDir.ASC);
                mirrorTemplates.setAllowBlank(false);
                mirrorTemplates.setDisplayField("display");


                String path = engine.isExistingNode() ? engine.getNode().getPath() : engine.getTargetNode().getPath();
                String nodeType = typeName != null ? typeName.getValues().get(0).getString() : null;
                JahiaContentManagementService.App.getInstance().initializeCodeEditor(path, !engine.isExistingNode(), nodeType, stubType, new BaseAsyncCallback<RpcMap>() {
                    public void onSuccess(RpcMap result) {
                        if (!result.isEmpty() && result.get("snippets") != null) {
                            snippets = (Map<String, List<GWTJahiaValueDisplayBean>>) result.get("snippets");
                            for (String type : snippets.keySet()) {
                                snippetType.getStore().add(new GWTJahiaValueDisplayBean(type, Messages.get("label.snippetType." + type, type)));
                            }
                            snippetType.setValue(snippetType.getStore().getAt(0));
                            Button button = new Button(Messages.get("label.add"));
                            button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                @Override
                                public void componentSelected(ButtonEvent buttonEvent) {
                                    codeField.insertProperty(mirrorTemplates.getValue().getValue());
                                }
                            });
                            Label label = new Label(Messages.get("label.snippetType", "Snippet Type"));
                            label.setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                            actions.add(label);
                            actions.add(snippetType);
                            label = new Label(Messages.get("label.codeMirrorTemplates","Code Template"));
                            label.setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                            actions.add(label);
                            actions.add(mirrorTemplates);
                            actions.add(button);
                            Button addAllButton = new Button(Messages.get("label.addAll"));
                            addAllButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                @Override
                                public void componentSelected(ButtonEvent ce) {
                                    StringBuilder s = new StringBuilder();
                                    for (GWTJahiaValueDisplayBean value  : mirrorTemplates.getStore().getModels()) {
                                        s.append(value.getValue()).append("\n");
                                    }
                                    codeField.insertProperty(s.toString());
                                }
                            });
                            actions.add(addAllButton);

                        }

                        if (!engine.getProperties().containsKey(codePropertyName)) {
                            Map<String,String> stubs = (Map<String,String>) result.get("stubs");
                            if (stubs.size() == 1) {
                                codeProperty = new GWTJahiaNodeProperty(codePropertyName, stubs.values().iterator().next(), GWTJahiaNodePropertyType.STRING);
                                initEditor(tab);
                            } else if (stubs.size() > 1) {
                                actions.hide();
                                final LayoutContainer w = new LayoutContainer(new CenterLayout());
                                final ComboBox<GWTJahiaValueDisplayBean> stubsCombo = new ComboBox<GWTJahiaValueDisplayBean>();
                                stubsCombo.setWidth(300);
                                stubsCombo.setTypeAhead(true);
                                stubsCombo.getListView().setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                                stubsCombo.setTriggerAction(ComboBox.TriggerAction.ALL);
                                stubsCombo.setForceSelection(true);
                                stubsCombo.setStore(new ListStore<GWTJahiaValueDisplayBean>());
                                stubsCombo.setDisplayField("display");
                                stubsCombo.setEmptyText(Messages.get("label.stub.select"));
                                for (String stub : stubs.keySet()) {
                                    String display;
                                    String viewName = "";
                                    if (stub.contains("/")) {
                                        String s = stub.split("/")[0];
                                        viewName = s.substring(s.indexOf(".") ,s.lastIndexOf("."));
                                        display =  Messages.get("label.stub" + viewName);
                                    } else {
                                        display = Messages.get("label.stub.default");
                                        viewName = "";
                                    }
                                    GWTJahiaValueDisplayBean value = new GWTJahiaValueDisplayBean(stubs.get(stub), display);
                                    value.set("viewName",viewName);
                                    stubsCombo.getStore().add(value);
                                }
                                stubsCombo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                                    @Override
                                    public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                                        w.removeFromParent();
                                        initEditor(tab);
                                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                                            public void execute() {
                                                codeField.insertProperty(stubsCombo.getValue().getValue());
                                            }
                                        });
                                        if (engine instanceof CreateContentEngine) {
                                            ((CreateContentEngine) engine).setTargetName(((CreateContentEngine) engine).getTargetName() + stubsCombo.getValue().get("viewName"));
                                        }
                                        actions.show();
                                        codeField.show();
                                    }
                                });
                                w.add(stubsCombo);
                                tab.add(w, new BorderLayoutData(Style.LayoutRegion.CENTER));
                                tab.layout();
                            }
                        } else {
                            initEditor(tab);
                        }
                    }
                });
            } else {
                initEditor(tab);
            }
            actions.add(indentButton);
            actions.show();
            tab.setProcessed(true);
        }
    }

    private void initEditor(final AsyncTabItem tab) {
        codeField = new CodeMirrorField();
        codeField.setWidth("95%");
        codeField.setHeight("90%");
        codeField.setMode(codeMirrorMode);
        List<GWTJahiaNodePropertyValue> values = codeProperty.getValues();
        if (!values.isEmpty()) {
            codeField.setValue(values.get(0).getString());
        }
        tab.add(codeField, new BorderLayoutData(Style.LayoutRegion.CENTER));
        tab.layout();
        tab.show();
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            codeField = null;
            codeProperty = null;
            snippets = null;
            snippetType = null;
            mirrorTemplates = null;
        }
        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties,
                       Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes,
                       Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (codeField != null) {
            codeProperty.setValue(new GWTJahiaNodePropertyValue(codeField.getValue()));
            changedProperties.add(codeProperty);
        }
    }

    public void setCodePropertyName(String codePropertyName) {
        this.codePropertyName = codePropertyName;
    }

    public void setStubType(String stubType) {
        this.stubType = stubType;
    }

    public void setCodeMirrorMode(String codeMirrorMode) {
        this.codeMirrorMode = codeMirrorMode;
    }

}

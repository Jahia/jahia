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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.form.CodeMirrorField;

import java.util.*;

/**
 *  Engine Tab Item that contains a Code Editor (CodeMirror)
 */
public class CodeEditorTabItem extends EditEngineTabItem {


    private static final long serialVersionUID = 5207870064789655518L;
    public static final String FONT_SIZE = "font-size";
    public static final String FONT_SIZE_VALUE = "11px";
    private String codePropertyName;
    private String stubType;
    private String codeMirrorMode = "htmlmixed";

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
        if (!tab.isProcessed()) {
            // Add list of properties
            GWTJahiaNodeProperty typeName = engine.getProperties().get("nodeTypeName");
            if (typeName == null) {
                typeName = engine.getPresetProperties().get("nodeTypeName");
            }
            if (stubType != null) {
                snippetType = new ComboBox<GWTJahiaValueDisplayBean>();
                snippetType.setTypeAhead(true);
                snippetType.getListView().setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                snippetType.setTriggerAction(ComboBox.TriggerAction.ALL);
                snippetType.setForceSelection(true);
                snippetType.setWidth(200);
                snippetType.removeAllListeners();
                snippetType.setStore(new ListStore<GWTJahiaValueDisplayBean>());
                snippetType.getStore().sort("display", Style.SortDir.ASC);
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
                Button button = new Button(Messages.get("label.add"));
                button.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent buttonEvent) {
                        codeField.insertProperty(mirrorTemplates.getValue().getValue());
                    }
                });
                HorizontalPanel horizontalPanel = new HorizontalPanel();
                horizontalPanel.setSpacing(10);
                horizontalPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);
                Label label = new Label(Messages.get("label.snippetType", "Snippet Type"));
                label.setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                horizontalPanel.add(label);
                horizontalPanel.add(snippetType);
                label = new Label(Messages.get("label.codeMirrorTemplates","Code Template"));
                label.setStyleAttribute(FONT_SIZE, FONT_SIZE_VALUE);
                horizontalPanel.add(label);
                horizontalPanel.add(mirrorTemplates);
                horizontalPanel.add(button);
                tab.add(horizontalPanel, new BorderLayoutData(Style.LayoutRegion.NORTH, 40));

                String path = engine.isExistingNode() ? engine.getNode().getPath() : engine.getTargetNode().getPath();
                String nodeType = typeName != null ? typeName.getValues().get(0).getString() : null;
                JahiaContentManagementService.App.getInstance().initializeCodeEditor(path, !engine.isExistingNode(), nodeType, stubType, new BaseAsyncCallback<RpcMap>() {
                    public void onSuccess(RpcMap result) {
                        snippets = (Map<String, List<GWTJahiaValueDisplayBean>>) result.get("snippets");
                        for (String type : snippets.keySet()) {
                            snippetType.getStore().add(new GWTJahiaValueDisplayBean(type, Messages.get("label.snippetType." + type, type)));
                        }

                        if (!engine.getProperties().containsKey(codePropertyName)) {
                            codeProperty = new GWTJahiaNodeProperty(codePropertyName, (String) result.get("stub"), GWTJahiaNodePropertyType.STRING);
                            initEditor(tab);
                        }
                    }
                });
            }
            //Add code source
            if (engine.getProperties().containsKey(codePropertyName)) {
                codeProperty = engine.getProperties().get(codePropertyName);
                initEditor(tab);
            }
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
        tab.setProcessed(true);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties,
                       Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes,
                       Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        codeProperty.setValue(new GWTJahiaNodePropertyValue(codeField.getValue()));
        changedProperties.add(codeProperty);
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

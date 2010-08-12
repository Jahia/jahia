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
package org.jahia.ajax.gwt.client.widget.form;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows the form for the new workflow rule.
 * User: jahia
 * Date: 27 f�vr. 2009
 * Time: 16:43:07
 */
public abstract class FormQuick extends FormPanel {
    String nodetype;
    String path;

    public FormQuick(String nodetype,String path) {
        this.nodetype = nodetype;
        this.path = path;
        createUI();
    }

    protected void createUI() {
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");
        setLabelWidth(150);
        setFieldWidth(400);
        final Button saveButton = new Button(Messages.get("label.save"));
        addButton(saveButton);        
        JahiaContentDefinitionService.App.getInstance().getNodeType(nodetype, new AsyncCallback<GWTJahiaNodeType>() {
            public void onFailure(Throwable throwable) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
            public void onSuccess(GWTJahiaNodeType gwtJahiaNodeType) {

                final TextField<String> nameField = new TextField<String>();
                final Map<String,GWTJahiaItemDefinition> defs = new HashMap<String,GWTJahiaItemDefinition>();
                nameField.setName("name");
                nameField.setFieldLabel(Messages.get("name", "Name"));
                nameField.setAllowBlank(false);
                nameField.setMaxLength(200);
                add(nameField);
                for(GWTJahiaItemDefinition def : gwtJahiaNodeType.getItems()) {
                    if (def instanceof GWTJahiaPropertyDefinition) {
                        Field field = FormFieldCreator.createField(def,null, null);
                        add(field);
                        defs.put(def.getName(),def);
                    }
                }

                // save properties button
                saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent componentEvent) {

                        final Map<String, List<GWTJahiaNodeProperty>> langCodeProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
                        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
                        for (Field field : getFields()) {
                            if (!field.getName().equals("name")) { 
                                GWTJahiaNodeProperty p = new GWTJahiaNodeProperty();
                                p.setMultiple(((GWTJahiaPropertyDefinition) defs.get(field.getName())).isMultiple());
                                p.setName(field.getName());
                                p.setValues(PropertiesEditor.getPropertyValues(field, defs.get(field.getName())));
                                gwtJahiaNodeProperties.add(p);
                            }
                        }
                        final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                        service.createNode(path, nameField.getValue().toString(), nodetype, null,
                                null, gwtJahiaNodeProperties, langCodeProperties, new BaseAsyncCallback<GWTJahiaNode>() {
                                    public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                                        if (getParent() instanceof Window) {
                                            ((Window) getParent()).close();
                                        }
                                        onWorkflowRuleCreated();
                                    }

                                    public void onApplicationFailure(Throwable throwable) {
                                        Log.error("Unable to create node of type " + nodetype + " at " + path , throwable);
                                        if (getParent() instanceof Window) {
                                            ((Window) getParent()).hide();
                                        }
                                    }
                                });
                    }
                });

                // remove all

                layout();
            }});
    }

    public abstract void onWorkflowRuleCreated();

}
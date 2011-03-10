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


import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.Arrays;
import java.util.Locale;

/**
 * User: toto
 * Date: Jan 6, 2010
 * Time: 8:10:21 PM
 */
public class ContentTabItem extends PropertiesTabItem {
    private int maxLength = 32;

    private transient boolean isNodeNameFieldDisplayed = false;
    private transient Field name;
    private transient CheckBox autoUpdateName;
    private transient TextField<String> nameText;
    private transient FieldSet nameFieldSet;
    private transient Label autoUpdateLabel;
    private boolean nameEditable = true;

    public Field<String> getName() {
        return nameText;
    }

    @Override
    public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setMultiLang(true);
        nameText = null;
        autoUpdateName = null;
        nameFieldSet = null;
        nameEditable = true;
        if (dataType == null) {
            dataType = Arrays.asList(GWTJahiaItemDefinition.CONTENT);
        }
        return super.create(engineTab, engine);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String language) {
        super.init(engine, tab, language);

        if (engine.getMixin() != null) {
            final Field titleField = propertiesEditor.getFieldsMap().get("jcr:title");
            if (titleField != null) {
                ((FieldSet) titleField.getParent()).insert(name, 0);
            } else {
                propertiesEditor.insert(nameFieldSet, 0);
            }
            if (nameEditable) {
                if (engine.getDefaultLanguageCode().equals(this.language)) {
                    boolean autoUpdate = true;

                    if (autoUpdateName != null) {
                        Boolean realValue = autoUpdateName.getData("realValue");
                        if (realValue == null) {
                            if (engine.isExistingNode()) {
                                if (titleField != null && titleField.getValue() != null) {
                                    String generated = generateNodeName((String) titleField.getValue());
                                    autoUpdate = engine.getNodeName().equals(generated);
                                } else {
                                    autoUpdate = false;
                                }
                            }
                        } else {
                            autoUpdate = realValue;
                        }
                        autoUpdateName.setValue(autoUpdate);
                        autoUpdateName.setVisible(true);
                        autoUpdateLabel.setText("&nbsp;" + Messages.get("label.synchronizeName", "Automatically synchronize name with title") + ":");
                    } else {
                        autoUpdate = false;
                        autoUpdateLabel.setText("");
                    }

                    nameText.setEnabled(!autoUpdate);

                } else {
                    if (autoUpdateName != null) {
                        autoUpdateName.setVisible(false);
                    }
                    autoUpdateLabel.setText("&nbsp;" + Messages.get("label.switchToUpdateName", "Switch to default language to update name"));
                    nameText.setEnabled(false);
                }
            } else {
                if (autoUpdateName != null) {
                    autoUpdateName.setVisible(false);
                }
                autoUpdateLabel.setText("");
                nameText.setEnabled(false);
            }
            tab.layout();
        }
    }

    @Override
    public void attachPropertiesEditor(final NodeHolder engine, AsyncTabItem tab) {
        // handle jcr:title property
        if (!engine.isMultipleSelection()) {
            tab.setLayout(new RowLayout());
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);

            final Field titleField = propertiesEditor.getFieldsMap().get("jcr:title");

            if (nameText == null) {
                nameFieldSet = new FieldSet();
                nameFieldSet.setHeading(Messages.get("label.systemName", "System name"));
                nameFieldSet.setLayout(fl);

                nameText = new TextField<String>();
                nameText.setWidth("250");
                nameText.setMaxLength(maxLength);
                nameText.setStyleAttribute("padding-left", "0");
                nameText.setValue(engine.getNodeName());
                nameText.setFireChangeEventOnSetValue(true);
                nameText.addListener(Events.Change, new Listener<FieldEvent>() {
                    public void handleEvent(FieldEvent fe) {
                        nameText.setFireChangeEventOnSetValue(false);
                        nameText.setValue(generateNodeName(nameText.getValue()));
                        nameText.setFireChangeEventOnSetValue(true);
                    }
                });

                tab.setData("NodeName", engine.getNodeName());


                final HBoxLayout hBoxLayout = new HBoxLayout();
                hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
                final LayoutContainer panel = new LayoutContainer(hBoxLayout);

                panel.add(this.nameText, new HBoxLayoutData());

                autoUpdateLabel = new Label("");
                panel.add(autoUpdateLabel, new HBoxLayoutData());

                if (titleField != null) {
                    autoUpdateName = new CheckBox();
                    autoUpdateName.setHideLabel(true);
                    panel.add(autoUpdateName, new HBoxLayoutData());
                }

                name = new AdapterField(panel);
                name.setFieldLabel(Messages.get("label.systemName", "System name"));

                isNodeNameFieldDisplayed = true;

                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                nameFieldSet.add(name, fd);
            }

            if (titleField != null && engine.getDefaultLanguageCode().equals(this.language)) {
                autoUpdateName.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent event) {
                        nameText.setEnabled(!autoUpdateName.getValue());

                        if (autoUpdateName.isEnabled()) {
                            autoUpdateName.setData("realValue", autoUpdateName.getValue());
                        }

                        if (autoUpdateName.getValue()) {
                            if (titleField.getValue() != null) {
                                nameText.setValue((String) titleField.getValue());
                            } else {
                                nameText.setValue(engine.getNodeName());
                            }
                        }
                    }
                });

                titleField.addListener(Events.KeyUp, new Listener<FieldEvent>() {
                    public void handleEvent(FieldEvent fe) {
                        if (autoUpdateName.getValue()) {
                            if (titleField.getValue() != null) {
                                nameText.setValue((String) titleField.getValue());
                            } else {
                                nameText.setValue(engine.getNodeName());
                            }
                        }
                    }
                });
            }


        } else {
            isNodeNameFieldDisplayed = false;
        }

        // attach properties node
        super.attachPropertiesEditor(engine, tab);
    }


    /**
     * Return true if nodeNameField is displayed
     *
     * @return
     */
    public boolean isNodeNameFieldDisplayed() {
        return isNodeNameFieldDisplayed;
    }

    public void setNameEditable(boolean nameEditable) {
        this.nameEditable = nameEditable;
    }


    public String generateNodeName(String text) {
        text = text.replaceAll("[àáâãäå]", "a");
        text = text.replaceAll("æ", "ae");
        text = text.replaceAll("ç", "c");
        text = text.replaceAll("[èéêë]", "e");
        text = text.replaceAll("[ìíîï]", "i");
        text = text.replaceAll("ñ", "n");
        text = text.replaceAll("[òóôõö]", "o");
        text = text.replaceAll("œ", "oe");
        text = text.replaceAll("[ùúûü]", "u");
        text = text.replaceAll("[ýÿ]", "y");
        text = text.replaceAll("[ÀÁÂÃÄÅ]", "A");
        text = text.replaceAll("Æ", "AE");
        text = text.replaceAll("Ç", "C");
        text = text.replaceAll("[ÈÉÊË]", "E");
        text = text.replaceAll("[ÌÍÎÏ]", "I");
        text = text.replaceAll("Ñ", "N");
        text = text.replaceAll("[ÒÓÔÕÖ]", "O");
        text = text.replaceAll("Œ", "OE");
        text = text.replaceAll("[ÙÚÛÜ]", "U");
        text = text.replaceAll("[ÝŸ]", "Y");
        String nodeName = text;

        final char[] chars = nodeName.toCharArray();
        final char[] newChars = new char[chars.length];
        int j = 0;

        for (char aChar : chars) {
            if (Character.isLetterOrDigit(aChar) || aChar == 32 || aChar == '-') {
                newChars[j++] = aChar;
            }
        }
        nodeName = new String(newChars, 0, j).trim().replaceAll(" ", "-").toLowerCase();
        if (nodeName.length() > maxLength) {
            nodeName = nodeName.substring(0, maxLength);
            if (nodeName.endsWith("-") && nodeName.length() > 2) {
                nodeName = nodeName.substring(0, nodeName.length() - 1);
            }
        }

        return nodeName;
    }

}

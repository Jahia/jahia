/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * User: toto
 * Date: Jan 6, 2010
 * Time: 8:10:21 PM
 */
public class ContentTabItem extends PropertiesTabItem {
    protected int maxNameSize = 32;

    private transient boolean isNodeNameFieldDisplayed = false;
    protected transient Field name;
    protected transient CheckBox autoUpdateName;
    protected transient Field<String> nameText;
    protected transient FieldSet nameFieldSet;
    protected transient Label autoUpdateLabel;
    private boolean nameEditable = true;

    private List<String> nameNotEditableForTypes;
    private List<String> invalidLanguagesAvailableForTypes;

    private transient List<CheckBox> invalidLanguagesCheckBoxes;
    private transient FieldSet invalidLanguagesFieldSet;

    private boolean defaultAutoUpdate = true;

    public Field<String> getName() {
        return nameText;
    }

    /**
     * Get the list of checked languages checkboxes.
     * @return the list of checked languages checkboxes if they are available or
     * null if they are not present based on invalidLanguagesAvailableForTypes.
     */
    public List<CheckBox> getCheckedLanguagesCheckBox() {
        List<CheckBox> values = new ArrayList<CheckBox>();
        if (invalidLanguagesCheckBoxes != null) {
            for (CheckBox check : invalidLanguagesCheckBoxes) {
                if (check.getValue()) {
                    values.add(check);
                }
            }
            return values;
        }
        return null;
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
        invalidLanguagesCheckBoxes = null;
        invalidLanguagesFieldSet = null;
        return super.create(engineTab, engine);
    }

    @Override
    public void init(final NodeHolder engine, AsyncTabItem tab, String language) {
        super.init(engine, tab, language);

        if (engine.getMixin() != null && nameFieldSet != null) {
            final Field<?> titleField;
            PropertiesEditor.PropertyAdapterField adapterField = propertiesEditor.getFieldsMap().get("jcr:title");
            if (adapterField != null) {
                ((FieldSet) adapterField.getParent()).insert(name, 0);
                titleField = adapterField.getField();
                FieldSet titleFieldSet = propertiesEditor.getFieldSetsMap().get(adapterField.getDefinition().getDeclaringNodeType());
                propertiesEditor.remove(titleFieldSet);
                propertiesEditor.insert(titleFieldSet, 0);

            } else {
                propertiesEditor.insert(nameFieldSet, 0);
                titleField = null;
            }

            if (nameEditable && !engine.isMultipleSelection()) {
                if (autoUpdateName != null) {
                    autoUpdateName.removeAllListeners();
                }
                if (titleField != null) {
                    List<Listener<? extends BaseEvent>> listeners = new ArrayList<Listener<? extends BaseEvent>>(titleField.getListeners(Events.KeyUp));
                    for (Listener listener : listeners) {
                        titleField.removeListener(Events.KeyUp, listener);
                    }
                }
                isNodeNameFieldDisplayed = true;
                boolean autoUpdate = defaultAutoUpdate;

                boolean nameEditingAllowed = isNameEditableForType(engine);

                if (nameEditingAllowed && autoUpdateName != null) {
                    if (autoUpdate && (engine.isExistingNode() || !JahiaGWTParameters.getLanguage().equals(language))) {
                        if (titleField != null && titleField.getValue() != null) {
                            String generated = generateNodeName((String) titleField.getValue());
                            autoUpdate = nameText.getValue().equals(generated);
                        } else {
                            autoUpdate = false;
                        }
                    }
                    autoUpdateName.setValue(autoUpdate);
                    autoUpdateName.setVisible(true);
                    autoUpdateLabel.setHtml("&nbsp;" + Messages.get("label.synchronizeName", "Automatically synchronize name with title") + ":");
                } else {
                    autoUpdate = false;
                    if (autoUpdateLabel != null) {
                        autoUpdateLabel.setHtml("");
                    }
                    if (autoUpdateName != null) {
                        autoUpdateName.setVisible(false);
                    }
                }

                nameText.setEnabled(nameEditingAllowed && !autoUpdate);

                if (titleField != null && autoUpdateName != null) {
                    autoUpdateName.addListener(Events.Change, new Listener<ComponentEvent>() {
                        public void handleEvent(ComponentEvent event) {
                            nameText.setEnabled(!autoUpdateName.getValue());

                            if (autoUpdateName.isEnabled()) {
                                autoUpdateName.setData("realValue", autoUpdateName.getValue());
                            }

                            if (autoUpdateName.getValue()) {
                                if (titleField.getValue() != null) {
                                    nameText.setValue(generateNodeName((String) titleField.getValue()));
                                } else {
                                    nameText.setValue(engine.getNodeName());
                                }
                            }
                        }
                    });
                    titleField.addListener(Events.KeyUp, new Listener<FieldEvent>() {
                        public void handleEvent(FieldEvent fe) {
                            if (autoUpdateName.getValue()) {
                                if (titleField.getValue() != null && ((String)titleField.getValue()).trim().length()>0) {
                                    nameText.setValue(generateNodeName((String) titleField.getValue()));
                                } else {
                                    nameText.setValue(engine.getNodeName());
                                }
                            }
                        }
                    });
                }
            } else {
                isNodeNameFieldDisplayed = false;
                if (autoUpdateName != null) {
                    autoUpdateName.setVisible(false);
                }
                autoUpdateLabel.setHtml("");
                nameText.setValue("");
                nameText.setEnabled(false);
            }

        }
        if (invalidLanguagesFieldSet != null) {
            propertiesEditor.insert(invalidLanguagesFieldSet, 0);
        }
        tab.layout();
    }

    @Override
    public void attachPropertiesEditor(final NodeHolder engine, AsyncTabItem tab) {
        // handle jcr:title property
        setNameField(engine, tab);

        // attach properties node
        // Add information field
        FieldSet fieldSet = new FieldSet();
        final FormLayout fl = new FormLayout();
        fl.setLabelWidth(0);
        fieldSet.setLayout(fl);
        fieldSet.setHeadingHtml(Messages.get("label.information", "Information"));
        FormData fd = new FormData("98%");
        fd.setMargins(new Margins(0));
        final GWTJahiaNode selectedNode = engine.getNode();

        Grid g = new Grid(1, 2);
        g.setCellSpacing(10);
        FlowPanel flowPanel = new FlowPanel();


        if (selectedNode != null) {
            String preview = selectedNode.getReferencedNode() != null ? selectedNode.getReferencedNode().getPreview() : selectedNode.getPreview();
            if (preview != null) {
                g.setWidget(0, 0, new Image(URL.appendTimestamp(preview)));
            }

            if (JahiaGWTParameters.isDevelopmentMode()) {
                String path = selectedNode.getPath();
                if (path != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.path") + ":</b> " + path));
                }
                String id = selectedNode.getUUID();
                if (id != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.id", "ID") + ":</b> " + id));
                }
                if (selectedNode.isFile() != null && selectedNode.isFile()) {
                    Long s = selectedNode.getSize();
                    if (s != null) {
                        flowPanel.add(new HTML("<b>" + Messages.get("label.size") + ":</b> " +
                                Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                    }
                }
                Date date = selectedNode.get("jcr:lastModified");
                if (date != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.lastModif") + ":</b> " +
                            org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
                }
                if (selectedNode.isLocked() != null && selectedNode.isLocked() && selectedNode.getLockInfos() != null) {
                    StringBuilder infos = new StringBuilder();
                    if (selectedNode.getLockInfos().containsKey(null) && selectedNode.getLockInfos().size() == 1) {
                        for (String s : selectedNode.getLockInfos().get(null)) {
                            infos.append(Formatter.getLockLabel(s));
                        }
                    } else {
                        for (Map.Entry<String, List<String>> entry : selectedNode.getLockInfos().entrySet()) {
                            if (entry.getKey() != null) {
                                if (infos.length() > 0) {
                                    infos.append("; ");
                                }
                                infos.append(entry.getKey()).append(" : ");
                                int i = 0;
                                for (String s : entry.getValue()) {
                                    if (i > 0) {
                                        infos.append(", ");
                                    }
                                    infos.append(Formatter.getLockLabel(s));
                                    i++;
                                }
                            }
                        }
                    }
                    flowPanel.add(new HTML(
                            "<b>" + Messages.get("info.lock.label") + ":</b> " + infos));
                }

                flowPanel.add(new HTML("<b>" + Messages.get("nodes.label", "Types") + ":</b> " + selectedNode.getNodeTypes()));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.jcr.edit.tags.tab", "Tags") + ":</b> " + selectedNode.getTags() != null ? selectedNode.getTags() : ""));
                g.setWidget(0, 1, flowPanel);
            }
            if (preview != null || JahiaGWTParameters.isDevelopmentMode()) {
                fieldSet.add(g, fd);
                propertiesEditor.add(fieldSet);
            }
        }
        //Invalid Languages selection
        List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
        if (engine instanceof AbstractContentEngine) {
            siteLanguages = ((AbstractContentEngine) engine).getLanguageSwitcher().getStore().getModels();
        }
        if (invalidLanguagesFieldSet == null && siteLanguages.size() > 1 && engine.getNodeTypes().get(0).getSuperTypes().contains("jmix:i18n") && isInvalidLanguagesAvailableForType(engine)) {
            final List<String> siteMandatoryLanguages = JahiaGWTParameters.getSiteMandatoryLanguages();
            invalidLanguagesCheckBoxes = new ArrayList<CheckBox>();

            LayoutContainer layoutContainer1 = new LayoutContainer();
            layoutContainer1.setBorders(false);
            layoutContainer1.setLayout(new FillLayout(Style.Orientation.HORIZONTAL) {
                @Override
                protected void setSize(Component c, int width, int height) {
                    // This method stays empty so that we do not rely on GWT to calculate the size of the
                    // checkboxes but let the browser do it.
                }
            });
            layoutContainer1.setWidth("100%");

            for (final GWTJahiaLanguage siteLanguage : siteLanguages) {
                CheckBox checkBox = new CheckBox();
                checkBox.setBoxLabel(siteLanguage.getDisplayName());
                checkBox.setValueAttribute(siteLanguage.getLanguage());
                /*checkBox.addListener(Events.Change, new Listener<ComponentEvent>() {
                    public void handleEvent(ComponentEvent componentEvent) {
                        CheckBox checkBox1 = (CheckBox) componentEvent.getSource();
                        if (engine instanceof AbstractContentEngine) {
                            final ComboBox<GWTJahiaLanguage> languageSwitcher = ((AbstractContentEngine) engine).getLanguageSwitcher();
                            if (languageSwitcher != null) {
                                final ListStore<GWTJahiaLanguage> store = languageSwitcher.getStore();
                                if (store != null) {
                                    GWTJahiaLanguage storeModel = store.findModel("language",
                                            checkBox1.getValueAttribute());
                                    storeModel.setActive(checkBox1.getValue());
                                }
                                languageSwitcher.getView().refresh();
                            }
                        }
                    }
                });*/

                if (siteMandatoryLanguages.contains(siteLanguage.getLanguage())) {
                    checkBox.setValue(true);
                    checkBox.setEnabled(false);
                }

                if (selectedNode == null || !selectedNode.getInvalidLanguages().contains(
                        siteLanguage.getLanguage())) {
                    checkBox.setValue(true);
                }
//                Deactivate interactions between display languages checkboxes and language switcher
                /*else if (engine instanceof AbstractContentEngine) {
                    AbstractContentEngine contentEngine = (AbstractContentEngine) engine;
                    final GWTJahiaLanguage model = contentEngine.getLanguageSwitcher().getStore().findModel(
                            "language", siteLanguage.getLanguage());
                    model.setActive(false);
                }*/
                layoutContainer1.add(checkBox);
                invalidLanguagesCheckBoxes.add(checkBox);

            }
            invalidLanguagesFieldSet = new FieldSet();
            invalidLanguagesFieldSet.setHeadingHtml(Messages.get("label.validLanguages", "Valid display languages"));
            invalidLanguagesFieldSet.setLayout(new FormLayout());
            invalidLanguagesFieldSet.add(layoutContainer1, fd);
        }
        super.attachPropertiesEditor(engine, tab);
    }

    protected void setNameField(NodeHolder engine, AsyncTabItem tab) {
        if (!engine.isMultipleSelection()) {
            tab.setLayout(new RowLayout());
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);

            PropertiesEditor.PropertyAdapterField titleField = propertiesEditor.getFieldsMap().get("jcr:title");

            if (nameText == null) {
                nameFieldSet = new FieldSet();
                nameFieldSet.setHeadingHtml(Messages.get("label.systemName", "System name"));
                nameFieldSet.setLayout(fl);

                nameText = new TextField<String>();
                nameText.setId("JahiaGxtField_systemName");
                nameText.setWidth("250");
                ((TextField)nameText).setMaxLength(maxNameSize);
                ((TextField)nameText).setAllowBlank(false);
                nameText.setStyleAttribute("padding-left", "0");
                nameText.setFireChangeEventOnSetValue(true);

                final HBoxLayout hBoxLayout = new HBoxLayout();
                hBoxLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
                final LayoutContainer panel = new LayoutContainer(hBoxLayout);

                panel.add(this.nameText, new HBoxLayoutData(0, 5, 0, 5));

                autoUpdateLabel = new Label("");
                panel.add(autoUpdateLabel, new HBoxLayoutData(0, 5, 0, 5));
                if (titleField != null) {
                    autoUpdateName = new CheckBox();
                    autoUpdateName.setId("JahiaGxtCheckbox_syncSystemNameWithTitle");
                    autoUpdateName.setWidth(10);
                    panel.add(autoUpdateName, new HBoxLayoutData(0, 5, 5, 5));
                }

                name = new AdapterField(panel);
                name.setFieldLabel(Messages.get("label.systemName", "System name"));

                FormData fd = new FormData("98%");
                fd.setMargins(new Margins(0));
                nameFieldSet.add(name, fd);

                boolean nameWriteable = !engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:write_default", engine.getNode()) && !engine.getNode().isLocked());
                if (!nameWriteable) {
                    nameText.setReadOnly(true);
                    if (autoUpdateName != null) {
                        autoUpdateName.setEnabled(false);
                    }
                }
                tab.setData("NodeName", null);
            }

            String nodeName = (String) tab.getData("NodeName");
            if (nodeName == null || !nodeName.equals(engine.getNodeName())) {
                tab.setData("NodeName", engine.getNodeName());
                if (titleField != null) {
                    List<Listener<? extends BaseEvent>> listeners = new ArrayList<Listener<? extends BaseEvent>>(titleField.getListeners(Events.KeyUp));
                    for (Listener listener : listeners) {
                        titleField.removeListener(Events.KeyUp, listener);
                    }
                }
                nameText.setValue(engine.getNodeName());
                if (autoUpdateName != null) {
                    autoUpdateName.removeAllListeners();
                    autoUpdateName.setData("realValue", null);
                }
            }
        }
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
        if (nodeName.length() > maxNameSize) {
            nodeName = nodeName.substring(0, maxNameSize);
            if (nodeName.endsWith("-") && nodeName.length() > 2) {
                nodeName = nodeName.substring(0, nodeName.length() - 1);
            }
        }

        return nodeName;
    }

    public void setMaxNameSize(int maxNameSize) {
        this.maxNameSize = maxNameSize;
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            isNodeNameFieldDisplayed = false;
            nameText = null;
            nameEditable = true;
        }
        super.setProcessed(processed);
    }

    public void setNameNotEditableForTypes(List<String> nameNotEditableForTypes) {
        this.nameNotEditableForTypes = nameNotEditableForTypes;
    }

    private boolean isNameEditableForType(NodeHolder engine) {
        return nameNotEditableForTypes == null || nameNotEditableForTypes.isEmpty()
                || engine == null || !engine.isExistingNode()
                || !engine.getNode().isNodeType(nameNotEditableForTypes);
    }

    public void setInvalidLanguagesAvailableForTypes(List<String> invalidLanguagesAvailableForTypes) {
        this.invalidLanguagesAvailableForTypes = invalidLanguagesAvailableForTypes;
    }

    private boolean isInvalidLanguagesAvailableForType(NodeHolder engine) {
        if (invalidLanguagesAvailableForTypes != null && !invalidLanguagesAvailableForTypes.isEmpty() && engine != null) {
            for (GWTJahiaNodeType type : engine.getNodeTypes()) {
                if (invalidLanguagesAvailableForTypes.contains(type.getName())) {
                    return true;
                }
                for (String superType : type.getSuperTypes()) {
                    if (invalidLanguagesAvailableForTypes.contains(superType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void doValidate(List<EngineValidation.ValidateResult> validateResult, NodeHolder engine, TabItem tab, String selectedLanguage, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, TabPanel tabs) {
        Field<String> nameField = getName();
        if (nameField != null && !nameField.isValid()) {
            EngineValidation.ValidateResult result = new EngineValidation.ValidateResult();
            result.canIgnore = false;
            result.errorTab = tab;
            result.errorField = nameField;
            validateResult.add(result);
        }
        super.doValidate(validateResult, engine, tab, selectedLanguage, changedI18NProperties, tabs);
    }

    public void setDefaultAutoUpdate(boolean defaultAutoUpdate) {
        this.defaultAutoUpdate = defaultAutoUpdate;
    }
}

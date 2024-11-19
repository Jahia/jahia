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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package com.extjs.gxt.ui.client.widget.form;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ListView;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;

import java.util.List;

/**
 * A multi-select list field.
 *
 * <dl>
 * <dt>Inherited Events:</dt>
 * <dd>Field Focus</dd>
 * <dd>Field Blur</dd>
 * <dd>Field Change</dd>
 * <dd>Field Invalid</dd>
 * <dd>Field Valid</dd>
 * <dd>Field KeyPress</dd>
 * <dd>Field SpecialKey</dd>
 * <dd>BoxComponent Move</dd>
 * <dd>BoxComponent Resize</dd>
 * <dd>Component Enable</dd>
 * <dd>Component Disable</dd>
 * <dd>Component BeforeHide</dd>
 * <dd>Component Hide</dd>
 * <dd>Component BeforeShow</dd>
 * <dd>Component Show</dd>
 * <dd>Component Attach</dd>
 * <dd>Component Detach</dd>
 * <dd>Component BeforeRender</dd>
 * <dd>Component Render</dd>
 * <dd>Component BrowserEvent</dd>
 * <dd>Component BeforeStateRestore</dd>
 * <dd>Component StateRestore</dd>
 * <dd>Component BeforeStateSave</dd>
 * <dd>Component SaveState</dd>
 * </dl>
 *
 * @param <D> the model type
 */
public class ListField<D extends ModelData> extends Field<D> implements SelectionProvider<D> {

    protected ListView<D> listView;
    protected ListStore<D> store;

    private XTemplate template;
    private String listStyle = "x-combo-list";
    private String selectedStyle = "x-combo-selected";
    private String itemSelector;
    private El input;
    private String valueField;

    public ListField() {
        listView = new ListView<D>();
        setSize(200, 100);
        setPropertyEditor(new ListModelPropertyEditor<D>());
    }

    public void addSelectionChangedListener(SelectionChangedListener<D> listener) {
        listView.getSelectionModel().addListener(Events.SelectionChange, listener);
    }

    @Override
    public void disable() {
        super.disable();
        listView.disable();
    }

    @Override
    public void enable() {
        super.enable();
        listView.enable();
    }

    /**
     * Returns the display field.
     *
     * @return the display field
     */
    public String getDisplayField() {
        return getPropertyEditor().getDisplayProperty();
    }

    /**
     * Returns the item selector.
     *
     * @return the item selector
     */
    public String getItemSelector() {
        return itemSelector;
    }

    /**
     * Returns the field's list view.
     *
     * @return the list view
     */
    public ListView<D> getListView() {
        return listView;
    }

    @Override
    public ListModelPropertyEditor<D> getPropertyEditor() {
        return (ListModelPropertyEditor<D>) propertyEditor;
    }

    @Override
    public String getRawValue() {
        return "";
    }

    public List<D> getSelection() {
        return listView.getSelectionModel().getSelectedItems();
    }

    /**
     * Returns the field's store.
     *
     * @return the store
     */
    public ListStore<D> getStore() {
        return store;
    }

    /**
     * Returns the custom template.
     *
     * @return the template
     */
    public XTemplate getTemplate() {
        return template;
    }

    @Override
    public D getValue() {
        List<D> sel = getSelection();
        if (sel.size() > 0) {
            return sel.get(0);
        }
        return null;
    }

    public String getValueField() {
        return valueField;
    }

    public void removeSelectionListener(SelectionChangedListener<D> listener) {
        listView.getSelectionModel().removeListener(Events.SelectionChange, listener);
    }

    /**
     * Sets the display field.
     *
     * @param displayField the display field
     */
    public void setDisplayField(String displayField) {
        getPropertyEditor().setDisplayProperty(displayField);
    }

    /**
     * This setting is required if a custom XTemplate has been specified.
     *
     * @param itemSelector the item selector
     */
    public void setItemSelector(String itemSelector) {
        this.itemSelector = itemSelector;
    }

    @Override
    public void setPropertyEditor(PropertyEditor<D> propertyEditor) {
        assert propertyEditor instanceof ListModelPropertyEditor<?> : "PropertyEditor must be a ModelPropertyEditor instance";
        super.setPropertyEditor(propertyEditor);
    }

    public void setSelection(List<D> selection) {
        if (selection != null && selection.size() > 0) {
            super.setValue(selection.get(0));
            listView.getSelectionModel().setSelection(selection);
        } else {
            super.setValue(null);
            listView.getSelectionModel().deselectAll();
        }
    }

    @Override
    public void setValue(D value) {
        super.setValue(value);
        listView.getSelectionModel().select(value, false);
    }

    /**
     * Sets the list field's list store.
     *
     * @param store the store
     */
    public void setStore(ListStore<D> store) {
        this.store = store;
    }

    /**
     * Sets the field's template used to render the list.
     *
     * @param html the html frament
     */
    public void setTemplate(String html) {
        assertPreRender();
        template = XTemplate.create(html);
    }

    /**
     * Sets the field's template used to render the list.
     *
     * @param template
     */
    public void setTemplate(XTemplate template) {
        assertPreRender();
        this.template = template;
    }

    /**
     * Sets the field's value field.
     *
     * @param valueField the value field
     */
    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(listView);
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(listView);
    }

    @Override
    protected El getInputEl() {
        return input;
    }

    @Override
    protected void onFocus(ComponentEvent ce) {
        super.onFocus(ce);
        listView.focus();
    }

    @Override
    protected void onRender(Element parent, int index) {
        setElement(DOM.createDiv(), parent, index);
        addStyleName("x-form-list");

        input = new El((Element) Document.get().createHiddenInputElement().cast());
        getElement().appendChild(input.dom);
        if (template == null) {
            String html = "<tpl for=\".\"><div class='x-combo-list-item' role='option'>{" + getDisplayField() + "}</div></tpl>";
            template = XTemplate.create(html);
        }
        listView.setBorders(false);
        listView.setTemplate(template);
        listView.addStyleName(listStyle);
        listView.setItemSelector(itemSelector != null ? itemSelector : ".x-combo-list-item");
        listView.setStore(store);

        listView.setSelectStyle(selectedStyle);
        listView.setOverStyle("x-combo-over");

        listView.getSelectionModel().addListener(Events.SelectionChange, new Listener<SelectionChangedEvent<D>>() {
            public void handleEvent(SelectionChangedEvent<D> se) {
                onSelectionChange(se.getSelection());
            }
        });

        listView.render(getElement());
        listView.getAriaSupport().setRole("listbox");
        ComponentHelper.setParent(this, listView);
        disableTextSelection(true);
        sinkEvents(Event.ONCLICK);

        super.onRender(parent, index);
    }

    @Override
    protected void onResize(int width, int height) {
        super.onResize(width, height);
        Size frameWidth = el().getFrameSize();
        width -= frameWidth.width;
        height -= frameWidth.height;
        listView.setSize(width, height);
    }

    protected void onSelectionChange(List<D> sel) {
        String prop = valueField != null ? valueField : listView.getDisplayProperty();
        // Original code:
        // StringBuffer sb = new StringBuffer();
        //    for (D m : sel) {
        //      sb.append(m.get(prop));
        //      sb.append(",");
        //    }
        //    String s = sb.toString();
        //    if (sb.length() > 1) {
        //      s = s.substring(0, s.length() - 1);
        //    }
        // Jahia GWT 2.8 fix:
        String s = "";
        for (D m : sel) {
            s += m.get(prop);
            s += ",";
        }
        if (s.length() > 1) {
            s = s.substring(0, s.length() - 1);
        }
        input.setValue(s);
    }

    @Override
    protected boolean validateValue(String value) {
        return true;
    }

}

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
package org.jahia.ajax.gwt.client.widget.resourcebundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTResourceBundle;
import org.jahia.ajax.gwt.client.data.GWTResourceBundleEntry;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.widget.contentengine.AbstractContentEngine;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;

/**
 * Widget for editing resource bundles.
 * 
 * @author Sergiy Shyrkov
 */
public class ResourceBundleEditor extends LayoutContainer {

    protected class AutoScrollableListView<M extends ModelData> extends ListView<M> {
        protected void autoScrollToSelected() {
            M selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Element elem = getElement(getStore().indexOf(selectedItem));
                if (elem != null) {
                    fly(elem).scrollIntoView(getElement(), false);
                }
            }
        }

        @Override
        protected void onRender(Element target, int index) {
            super.onRender(target, index);
            getSelectionModel().addListener(Events.SelectionChange,
                    new Listener<SelectionChangedEvent<GWTResourceBundleEntry>>() {
                        public void handleEvent(SelectionChangedEvent<GWTResourceBundleEntry> be) {
                            if (be.getSelection().size() > 0) {
                                autoScrollToSelected();
                            }
                        }
                    });
        }
    }

    public static final List<String> FIELDS = Arrays.asList(GWTJahiaNode.RESOURCE_BUNDLE);

    protected Button addButton;

    protected AutoScrollableListView<GWTResourceBundleEntry> bundleView;

    protected NodeHolder engine;

    protected FormPanel form;

    protected FormBinding formBinding;

    protected Set<String> languages = new TreeSet<String>();

    private String name;

    protected TextField<String> searchField;

    protected Map<String, TextArea> valuesPerLanguage = new HashMap<String, TextArea>();

    public ResourceBundleEditor(NodeHolder engine) {
        super();
        this.engine = engine;
    }

    private Button createAddButton() {
        Button btn = new Button(Messages.get("label.add", "Add"),
                new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        String newKey = searchField.getValue();
                        GWTResourceBundleEntry newEntry = null;
                        if (newKey.length() == 0
                                || bundleView.getStore().contains(
                                        (newEntry = new GWTResourceBundleEntry(newKey)))) {
                            return;
                        }
                        bundleView.getStore().add(newEntry);
                        bundleView.getSelectionModel().setSelection(Arrays.asList(newEntry));
                        event.getButton().disable();
                    }
                });
        btn.setIcon(ToolbarIconProvider.getInstance().getIcon("newAction"));
        btn.disable();

        return btn;
    }

    private Menu createContextMenu() {
        Menu contextMenu = new Menu();

        MenuItem newItem = new MenuItem();
        newItem.setText(Messages.get("label.new", "New"));
        newItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                MessageBox box = new MessageBox();
                box.setTitle(Messages.get("label.new", "New"));
                box.setMessage(Messages.get("label.resourceBundle.new",
                        "Please provide a key for the new resource bundle entry"));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getText())) {
                            if (be.getValue() == null || be.getValue().trim().length() == 0) {
                                return;
                            }
                            GWTResourceBundleEntry newEntry = new GWTResourceBundleEntry(be
                                    .getValue());
                            GWTResourceBundleEntry entry = null;
                            if ((entry = bundleView.getStore().findModel(newEntry)) == null) {
                                bundleView.getStore().add(newEntry);
                                entry = newEntry;
                            }
                            bundleView.getSelectionModel().setSelection(Arrays.asList(entry));
                        }
                    }
                });
                box.addListener(Events.Show, new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                                .getSelectedItem();
                        if (selectedItem != null) {
                            be.getMessageBox().getTextBox().setValue(selectedItem.getKey());
                            be.getMessageBox().getTextBox().selectAll();
                        }
                    }
                });
                box.show();
            }
        });
        contextMenu.add(newItem);

        MenuItem renameItem = new MenuItem();
        renameItem.setText(Messages.get("label.rename", "Rename"));
        renameItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();

                MessageBox box = new MessageBox();
                box.setTitle(Messages.get("label.rename", "Rename"));
                box.setMessage(Messages.getWithArgs("label.resourceBundle.rename",
                        "Rename \"{0}\" to", new String[] { selectedItem.getKey() }));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getText())) {
                            if (be.getValue() == null || be.getValue().trim().length() == 0
                                    || be.getValue().equals(selectedItem.getKey())) {
                                return;
                            }
                            bundleView.getStore().remove(selectedItem);
                            selectedItem.setKey(be.getValue());
                            bundleView.getStore().add(selectedItem);
                            bundleView.getSelectionModel()
                                    .setSelection(Arrays.asList(selectedItem));
                        }
                    }
                });
                box.addListener(Events.Show, new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                                .getSelectedItem();
                        if (selectedItem != null) {
                            be.getMessageBox().getTextBox().setValue(selectedItem.getKey());
                            be.getMessageBox().getTextBox().selectAll();
                        }
                    }
                });
                box.show();
            }
        });
        contextMenu.add(renameItem);

        MenuItem deleteItem = new MenuItem();
        deleteItem.setText(Messages.get("label.delete", "Delete"));
        deleteItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();
                MessageBox.confirm(Messages.get("label.remove", "Remove"), Messages.getWithArgs(
                        "message.remove.single.confirm",
                        "Do you really want to remove the selected resource {0}?",
                        new Object[] { selectedItem.getKey() }), new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getText())) {
                            bundleView.getStore().remove(selectedItem);
                        }
                    }
                });
            }
        });
        contextMenu.add(deleteItem);

        MenuItem duplicateItem = new MenuItem();
        duplicateItem.setText(Messages.get("label.duplicate", "Duplicate"));
        duplicateItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();

                MessageBox box = new MessageBox();
                box.setTitle(Messages.get("label.duplicate", "Duplicate"));
                box.setMessage(Messages.getWithArgs("label.resourceBundle.duplicate",
                        "Duplicate \"{0}\" to", new String[] { selectedItem.getKey() }));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getText())) {
                            if (be.getValue() == null || be.getValue().trim().length() == 0
                                    || be.getValue().equals(selectedItem.getKey())) {
                                return;
                            }
                            GWTResourceBundleEntry entry = new GWTResourceBundleEntry(be.getValue());
                            entry.setValues(new BaseModelData(selectedItem.getValues()
                                    .getProperties()));
                            bundleView.getStore().add(entry);
                            bundleView.getSelectionModel().setSelection(Arrays.asList(entry));
                        }
                    }
                });
                box.addListener(Events.Show, new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                                .getSelectedItem();
                        if (selectedItem != null) {
                            be.getMessageBox().getTextBox().setValue(selectedItem.getKey());
                            be.getMessageBox().getTextBox().selectAll();
                        }
                    }
                });
                box.show();
            }
        });
        contextMenu.add(duplicateItem);

        return contextMenu;
    }

    private FormPanel createForm() {
        FormPanel panel = new FormPanel();
        panel.setHeaderVisible(false);
        panel.setLabelAlign(LabelAlign.TOP);
        panel.setButtonAlign(HorizontalAlignment.CENTER);
        panel.setFrame(false);
        panel.setBorders(false);

        return panel;
    }

    private TextField<String> createSearchField() {
        TextField<String> searchField = new TextField<String>();
        searchField.setName("bundleKeySearch");
        searchField.addListener(Events.KeyUp, new KeyListener() {
            public void componentKeyUp(ComponentEvent event) {
                @SuppressWarnings("unchecked")
                String search = ((TextField<String>) event.getComponent()).getValue();
                if (search != null && search.length() > 0) {
                    GWTResourceBundleEntry match = bundleView.getStore().findModel("key", search);
                    addButton.setEnabled(match == null);
                    if (match == null) {
                        for (GWTResourceBundleEntry e : bundleView.getStore().getModels()) {
                            if (e.getKey().startsWith(search)) {
                                match = e;
                                break;
                            }
                        }
                    }
                    if (match != null) {
                        bundleView.getSelectionModel().setSelection(Arrays.asList(match));
                    }
                } else {
                    addButton.disable();
                }
            }
        });

        return searchField;
    }

    private AutoScrollableListView<GWTResourceBundleEntry> createView() {
        final AutoScrollableListView<GWTResourceBundleEntry> view = new AutoScrollableListView<GWTResourceBundleEntry>();
        view.setBorders(false);
        ListStore<GWTResourceBundleEntry> store = new ListStore<GWTResourceBundleEntry>();
        store.setDefaultSort("key", SortDir.ASC);
        store.sort("key", SortDir.ASC);
        view.setStore(store);
        view.setSimpleTemplate("{key}");
        view.setDisplayProperty("key");
        view.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        view.getSelectionModel().addListener(Events.SelectionChange,
                new Listener<SelectionChangedEvent<GWTResourceBundleEntry>>() {
                    public void handleEvent(SelectionChangedEvent<GWTResourceBundleEntry> be) {
                        if (be.getSelection().size() > 0) {
                            if (!valuesPerLanguage.values().iterator().next().isEnabled()) {
                                for (TextArea textArea : valuesPerLanguage.values()) {
                                    textArea.enable();
                                }
                            }
                            formBinding.bind(be.getSelectedItem());
                        } else {
                            formBinding.unbind();
                            for (TextArea textArea : valuesPerLanguage.values()) {
                                textArea.disable();
                            }
                        }
                    }
                });

        view.setContextMenu(createContextMenu());

        return view;
    }

    /**
     * Returns current resource bundle values
     * 
     * @return current resource bundle values
     */
    public GWTResourceBundle getResourceBundle() {
        GWTResourceBundle rb = new GWTResourceBundle(name);
        for (GWTResourceBundleEntry entry : bundleView.getStore().getModels()) {
            rb.getEntryMap().put(entry.getKey(), entry);
        }

        return rb;
    }

    protected void onLoad(GWTResourceBundle rb) {
        this.name = rb.getName();
        languages.clear();
        languages.addAll(rb.getLanguages());

        bundleView.getStore().removeAll();
        bundleView.getStore().add(new ArrayList<GWTResourceBundleEntry>(rb.getEntries()));

        populateForm();
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new BorderLayout());
        if (engine instanceof AbstractContentEngine) {
            ((AbstractContentEngine) engine).loading();
        }
        JahiaContentManagementService.App.getInstance().getNodes(
                Arrays.asList(engine.getNode().getPath()), FIELDS,
                new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (engine instanceof AbstractContentEngine) {
                            ((AbstractContentEngine) engine).loaded();
                        }
                        super.onFailure(caught);
                    }

                    public void onSuccess(List<GWTJahiaNode> result) {
                        try {
                            if (result.size() > 0) {
                                onLoad((GWTResourceBundle) result.get(0).get(
                                        GWTJahiaNode.RESOURCE_BUNDLE));
                            }
                        } finally {
                            if (engine instanceof AbstractContentEngine) {
                                ((AbstractContentEngine) engine).loaded();
                            }
                        }
                    }
                });

        ContentPanel cpLeft = new ContentPanel();
        cpLeft.setScrollMode(Scroll.AUTO);
        cpLeft.setHeaderVisible(false);
        cpLeft.setBorders(true);
        VBoxLayout westLayout = new VBoxLayout(VBoxLayoutAlign.STRETCH);
        westLayout.setPadding(new Padding(5));
        cpLeft.setLayout(westLayout);

        bundleView = createView();
        addButton = createAddButton();
        searchField = createSearchField();

        bundleView.setHeight("92%");
        cpLeft.add(bundleView, new VBoxLayoutData(new Margins(0, 0, 5, 0)));
        cpLeft.add(searchField, new VBoxLayoutData(new Margins(0)));
        cpLeft.add(addButton, new VBoxLayoutData(new Margins(0)));

        // layout and add left panel
        BorderLayoutData ldLeft = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 350);
        ldLeft.setMargins(new Margins(5));
        ldLeft.setSplit(true);
        add(cpLeft, ldLeft);

        // center panel
        ContentPanel cpCenter = new ContentPanel();
        cpCenter.setHeaderVisible(false);
        cpCenter.setLayout(new FitLayout());

        form = createForm();
        form.setHeight("100%");
        form.setBorders(true);
        cpCenter.add(form);

        // layout and add center panel
        BorderLayoutData ldCenter = new BorderLayoutData(LayoutRegion.CENTER);
        ldCenter.setMargins(new Margins(5));
        add(cpCenter, ldCenter);
    }

    private void populateForm() {
        valuesPerLanguage.clear();

        if (formBinding != null) {
            formBinding.unbind();
        }

        form.removeAll();

        formBinding = new FormBinding(form);
        formBinding.setStore(bundleView.getStore());

        FormData formData = new FormData("100%");
        TextArea lang = null;

        for (String languageCode : languages) {
            lang = new TextArea();
            lang.setName(languageCode);
            lang.setFieldLabel(!languageCode.equals(GWTResourceBundle.DEFAULT_LANG) ? languageCode
                    : Messages.get("label.default", "Default"));
            lang.disable();
            formBinding.addFieldBinding(new FieldBinding(lang, "values." + languageCode));

            valuesPerLanguage.put(languageCode, lang);
            form.add(lang, formData);
        }

        form.layout();
    }
}

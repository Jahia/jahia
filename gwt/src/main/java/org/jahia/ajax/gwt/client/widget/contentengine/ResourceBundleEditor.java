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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.GWTResourceBundle;
import org.jahia.ajax.gwt.client.data.GWTResourceBundleEntry;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.ToolbarIconProvider;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import com.extjs.gxt.ui.client.Style;
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
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.TabPanel.TabPosition;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.Element;

/**
 * Widget for editing resource bundles.
 *
 * @author Sergiy Shyrkov
 */
public class ResourceBundleEditor extends LayoutContainer {

    /**
     * Custom {@link ListView} implementation that automatically scrolls into the view the selected list item.
     *
     * @author Sergiy Shyrkov
     *
     * @param <M>
     */
    protected static class AutoScrollableListView<M extends ModelData> extends ListView<M> {
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

    private static final String LANGUAGE_TAB_ID = "jahia-rb-language-tab-";

    private static final String NEW_LANGUAGE_TAB_ID = "jahia-rb-new-language-tab";

    protected Button addButton;

    protected List<GWTJahiaValueDisplayBean> availableLanguages;

    protected AutoScrollableListView<GWTResourceBundleEntry> bundleView;

    protected NodeHolder engine;

    protected boolean writable = true;

    protected FormPanel form;

    protected FormBinding formBinding;

    protected Set<String> languages = new TreeSet<String>();

    private String name;

    private AsyncTabItem newLanguageTab;

    protected TextField<String> searchField;

    private TabPanel tabPanel;

    protected Map<String, TextArea> valuesPerLanguage = new HashMap<String, TextArea>();

    public ResourceBundleEditor(NodeHolder engine) {
        super();
        this.engine = engine;
        writable = (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));
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
        btn.addStyleName("button-add");
        btn.setIcon(ToolbarIconProvider.getInstance().getIcon("newAction"));
        btn.disable();

        return btn;
    }

    private Menu createContextMenu() {
        Menu contextMenu = new Menu();

        MenuItem newItem = new MenuItem();
        newItem.setHtml(Messages.get("label.new", "New"));
        newItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                MessageBox box = new MessageBox();
                box.setTitleHtml(Messages.get("label.new", "New"));
                box.setMessage(Messages.get("label.resourceBundle.new",
                        "Please provide a key for the new resource bundle entry"));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getHtml())) {
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
        renameItem.setHtml(Messages.get("label.rename", "Rename"));
        renameItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();

                MessageBox box = new MessageBox();
                box.setTitleHtml(Messages.get("label.rename", "Rename"));
                box.setMessage(Messages.getWithArgs("label.resourceBundle.rename",
                        "Rename \"{0}\" to", new String[] { selectedItem.getKey() }));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getHtml())) {
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
        deleteItem.setHtml(Messages.get("label.delete", "Delete"));
        deleteItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();
                MessageBox.confirm(Messages.get("label.remove", "Remove"), Messages.getWithArgs(
                        "message.remove.single.confirm",
                        "Do you really want to remove the selected resource {0}?",
                        new Object[] { selectedItem.getKey() }), new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.YES.equalsIgnoreCase(be.getButtonClicked().getItemId())) {
                            bundleView.getStore().remove(selectedItem);
                        }
                    }
                });
            }
        });
        contextMenu.add(deleteItem);

        MenuItem duplicateItem = new MenuItem();
        duplicateItem.setHtml(Messages.get("label.duplicate", "Duplicate"));
        duplicateItem.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                final GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel()
                        .getSelectedItem();

                MessageBox box = new MessageBox();
                box.setTitleHtml(Messages.get("label.duplicate", "Duplicate"));
                box.setMessage(Messages.getWithArgs("label.resourceBundle.duplicate",
                        "Duplicate \"{0}\" to", new String[] { selectedItem.getKey() }));
                box.setType(MessageBoxType.PROMPT);
                box.setButtons(Dialog.OKCANCEL);
                box.setType(MessageBoxType.PROMPT);
                box.addCallback(new Listener<MessageBoxEvent>() {
                    public void handleEvent(MessageBoxEvent be) {
                        if (Dialog.OK.equalsIgnoreCase(be.getButtonClicked().getHtml())) {
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

    private AsyncTabItem createLanguageTab(String lang) {
        AsyncTabItem langTab = new AsyncTabItem(getLanguageDisplayName(lang));
        langTab.setLayout(new CenterLayout());
        langTab.setItemId(LANGUAGE_TAB_ID + lang);
        return langTab;
    }

    private void createLanguageTabs() {
        int i = 1;
        for (String l : languages) {
            if (i == tabPanel.getItemCount() - 1
                    || !tabPanel.getItem(i).getItemId().substring(LANGUAGE_TAB_ID.length())
                            .equals(l)) {
                tabPanel.insert(createLanguageTab(l), i);
            }
            i++;
        }
    }

    private AsyncTabItem createNewLanguageTab() {
        // new language tab
        AsyncTabItem newLangTab = new AsyncTabItem(Messages.get("label.new", "New") + "...");
        newLangTab.setLayout(new CenterLayout());
        newLangTab.setIcon(ToolbarIconProvider.getInstance().getIcon("newAction"));
        newLangTab.setItemId(NEW_LANGUAGE_TAB_ID);

        return newLangTab;
    }

    private TabItem createPropertiesTab() {
        TabItem tab = new TabItem(Messages.get("label.properties", "Properties"));
        tab.setLayout(new BorderLayout());
        tab.setItemId("jahia-rb-properties-tab");


        ContentPanel cpLeft = new ContentPanel();
        cpLeft.setScrollMode(Scroll.AUTO);
        cpLeft.setHeaderVisible(false);
        cpLeft.setBorders(true);
        //[QA-7856] Use the row layout intead of box layout
        RowLayout rowLayout = new RowLayout(Style.Orientation.VERTICAL);
        rowLayout.setAdjustForScroll(true);
        cpLeft.setLayout(rowLayout);

        bundleView = createView();
        addButton = createAddButton();
        searchField = createSearchField();

        // trigger loading of data
        loadData();

        // FIXME: we may not need as the row layout will manage it
        bundleView.setHeight("auto");

        cpLeft.add(bundleView, new RowData(1,0.9, new Margins(0, 0, 4, 0)));

        cpLeft.add(searchField, new RowData(1,0.05));

        cpLeft.add(addButton, new RowData(1,0.05, new Margins(1,0,0,0)));


        // layout and add left panel
        BorderLayoutData ldLeft = new BorderLayoutData(LayoutRegion.WEST, 200, 100, 350);
        ldLeft.setMargins(new Margins(5));
        ldLeft.setSplit(true);
        // add(cpLeft, ldLeft);
        tab.add(cpLeft, ldLeft);

        // center panel
        ContentPanel cpCenter = new ContentPanel();
        cpCenter.setHeaderVisible(false);
        cpCenter.setLayout(new FlowLayout());
        cpCenter.setScrollMode(Scroll.AUTOY);

        form = createForm();
        form.setBorders(true);
        form.setWidth("100%");
        cpCenter.add(form);

        // layout and add center panel
        BorderLayoutData ldCenter = new BorderLayoutData(LayoutRegion.CENTER);
        ldCenter.setMargins(new Margins(5));
        // add(cpCenter, ldCenter);
        tab.add(cpCenter, ldCenter);

        return tab;
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
                    addButton.setEnabled(match == null && writable);
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
                            if (writable && !valuesPerLanguage.values().iterator().next().isEnabled()) {
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
        if (writable) {
            view.setContextMenu(createContextMenu());
        }

        return view;
    }

    protected void fillCurrentTab() {
        TabItem selectedTab = tabPanel.getSelectedItem();
        if (!(selectedTab instanceof AsyncTabItem)) {
            return;
        }
        AsyncTabItem currentTab = (AsyncTabItem) selectedTab;

        if (currentTab.getItemId().startsWith(LANGUAGE_TAB_ID)) {
            // language tab
            populateLanguageTab(currentTab);
        } else if (NEW_LANGUAGE_TAB_ID.equals(currentTab.getItemId())) {
            // new language tab
            populateNewLanguageTab();
        }
    }

    protected String getLanguageDisplayName(String langCode) {
        if (GWTResourceBundle.DEFAULT_LANG.equals(langCode)) {
            return "[" + Messages.get("label.default", "Default") + "]";
        }
        String label = langCode;

        for (GWTJahiaValueDisplayBean langBean : availableLanguages) {
            if (langBean.getValue().equals(langCode)) {
                label = langBean.getDisplay();
                break;
            }
        }

        return label;
    }

    /**
     * Returns current resource bundle values.
     *
     * @return current resource bundle values
     */
    public GWTResourceBundle getResourceBundle() {
        // check for changes in plain view tabs
        TabItem selectedItem = tabPanel.getSelectedItem();
        if (selectedItem != null && selectedItem.getItemId().startsWith(LANGUAGE_TAB_ID)) {
            propagateChanges(selectedItem);
        }

        GWTResourceBundle rb = new GWTResourceBundle(name);
        for (GWTResourceBundleEntry entry : bundleView.getStore().getModels()) {
            rb.getEntryMap().put(entry.getKey(), entry);
        }

        return rb;
    }

    private void loadData() {
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
    }

    protected void onLoad(GWTResourceBundle rb) {
        this.name = rb.getName();
        languages.clear();
        languages.addAll(rb.getLanguages());

        bundleView.getStore().removeAll();
        bundleView.getStore().add(new ArrayList<GWTResourceBundleEntry>(rb.getEntries()));

        availableLanguages = rb.getAvailableLanguages();

        populateForm();
    }

    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FitLayout());

        tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.setTabPosition(TabPosition.BOTTOM);
        tabPanel.setMinTabWidth(100);
        tabPanel.setLayoutData(new FitLayout());

        tabPanel.add(createPropertiesTab());
        newLanguageTab = createNewLanguageTab();
        tabPanel.add(newLanguageTab);
        newLanguageTab.setEnabled(writable);

        tabPanel.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                fillCurrentTab();
            }
        });
        tabPanel.addListener(Events.BeforeSelect, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                TabItem selectedItem = tabPanel.getSelectedItem();
                if (selectedItem != null && selectedItem.getItemId().startsWith(LANGUAGE_TAB_ID)) {
                    propagateChanges(selectedItem);
                }
            }
        });

        add(tabPanel);
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
            lang.setFieldLabel(getLanguageDisplayName(languageCode));
            lang.disable();
            formBinding.addFieldBinding(new FieldBinding(lang, "values." + languageCode));

            valuesPerLanguage.put(languageCode, lang);
            form.add(lang, formData);
        }

        form.layout();

        GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            formBinding.bind(selectedItem);
            if (writable) {
                for (TextArea textArea : valuesPerLanguage.values()) {
                    textArea.enable();
                }
            }
        }

        createLanguageTabs();
    }

    private void populateLanguageTab(AsyncTabItem currentTab) {
        TextArea sourceView = null;
        if (!currentTab.isProcessed()) {
            sourceView = new TextArea();
            sourceView.setWidth("100%");
            sourceView.setHeight("100%");
            currentTab.add(sourceView);
            currentTab.layout();
            currentTab.setProcessed(true);
            sourceView.setEnabled(writable);
        } else {
            sourceView = (TextArea) currentTab.getItem(0);
        }

        String lang = currentTab.getItemId().substring(LANGUAGE_TAB_ID.length());

        StringBuilder b = new StringBuilder();
        for (GWTResourceBundleEntry e : bundleView.getStore().getModels()) {
            String value = e.getValue(lang);
            if (value != null && value.length() > 0) {
                b.append(e.getKey()).append("=").append(value).append("\n");
            }
        }
        sourceView.setValue(b.toString());
    }

    private void populateNewLanguageTab() {
        if (newLanguageTab.isProcessed()) {
            return;
        }

        FormPanel formPanel = new FormPanel();
        formPanel.setHeight(150);
        formPanel.setWidth(500);
        formPanel.setLabelWidth(150);
        formPanel.setFrame(true);
        formPanel.setBorders(true);
        formPanel.setButtonAlign(HorizontalAlignment.CENTER);
        formPanel.setHeadingHtml(Messages.get("label.resourceBundle.addLanguage",
                "Add new language to the resource bundle") + ":");

        final ComboBox<GWTJahiaValueDisplayBean> languageSelector = new ComboBox<GWTJahiaValueDisplayBean>();
        languageSelector.setFieldLabel(Messages.get("label.resourceBundle.addLanguage.choose",
                "Choose"));
        languageSelector.setStore(new ListStore<GWTJahiaValueDisplayBean>());
        languageSelector.getStore().add(
                new GWTJahiaValueDisplayBean(GWTResourceBundle.DEFAULT_LANG, "["
                        + Messages.get("label.default", "Default") + "]"));
        languageSelector.getStore().add(availableLanguages);
        languageSelector.setDisplayField("display");
        languageSelector.setTypeAhead(true);
        languageSelector.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSelector.setForceSelection(true);
        formPanel.add(languageSelector);

        final TextField<String> langField = new TextField<String>();
        langField.setName("newLanguage");
        langField.setFieldLabel(Messages.get("label.resourceBundle.addLanguage.type",
                "or type the locale"));
        formPanel.add(langField);

        languageSelector
                .addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                        if (se == null) {
                            return;
                        }
                        langField.setValue(!GWTResourceBundle.DEFAULT_LANG.equals(se
                                .getSelectedItem().getValue()) ? se.getSelectedItem().getValue()
                                : "");
                    }
                });

        Button btn = new Button(Messages.get("label.add", "Add"),
                new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        String newLang = langField.getValue();
                        if (languages.contains(newLang)) {
                            return;
                        }
                        if (!newLang.matches("[a-z]{2,3}(_[A-Z]{2})?")) {
                            MessageBox.alert(Messages.get("label.error"), Messages.get("label.resourceBundle.addLanguage.invalidLang"), null).getDialog().addStyleName("engine-save-error");
                            return;
                        }
                        languages.add(newLang);
                        for (GWTResourceBundleEntry e : bundleView.getStore().getModels()) {
                            e.setValue(newLang, null);
                        }
                        populateForm();
                        tabPanel.setSelection(tabPanel.getItem(0));
                    }
                });
        btn.addStyleName("button-add");
        btn.setEnabled(writable);

        formPanel.addButton(btn);

        newLanguageTab.add(formPanel);

        newLanguageTab.setProcessed(true);

        newLanguageTab.layout();
    }

    protected void propagateChanges(TabItem tab) {
        String lang = tab.getItemId().substring(LANGUAGE_TAB_ID.length());
        String text = ((TextArea) tab.getItem(0)).getValue();
        String[] lines = text != null && text.length() > 0 ? text.split("\n") : new String[] {};
        Map<String, String> rb = new HashMap<String, String>();
        for (String line : lines) {
            int pos = line.indexOf("=");
            if (pos <= 0 && pos <= line.length() - 2) {
                continue;
            }
            rb.put(line.substring(0, pos), line.substring(pos + 1));
        }
        for (GWTResourceBundleEntry e : bundleView.getStore().getModels()) {
            e.setValue(lang, rb.remove(e.getKey()));
        }

        for (Map.Entry<String, String> rbe : rb.entrySet()) {
            GWTResourceBundleEntry model = new GWTResourceBundleEntry(rbe.getKey());
            for (String l : languages) {
                model.setValue(l, null);
            }
            model.setValue(lang, rbe.getValue());

            bundleView.getStore().add(model);
        }

        GWTResourceBundleEntry selectedItem = bundleView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && formBinding != null) {
            formBinding.unbind();
            formBinding.bind(selectedItem);
        }
    }
}

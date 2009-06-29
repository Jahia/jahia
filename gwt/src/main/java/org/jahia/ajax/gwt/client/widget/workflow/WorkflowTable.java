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
package org.jahia.ajax.gwt.client.widget.workflow;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.binder.TableBinder;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.table.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.util.EngineOpener;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowService;
import org.jahia.ajax.gwt.client.service.workflow.WorkflowServiceAsync;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowManagerState;
import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:31:02
 */
public class WorkflowTable extends TopRightComponent {

    private final WorkflowServiceAsync service = WorkflowService.App.getInstance() ;

    private ContentPanel m_component ;
    private PagingToolBar pagingToolBar ;
    private int depth = 3 ;
    private int pageSize = 20 ;
    private Table m_table ;
    private List<String> languageCodes = null ;
    private Map<TableItem, GWTJahiaWorkflowElement> container = new HashMap<TableItem, GWTJahiaWorkflowElement>() ;
    private TableBinder<GWTJahiaWorkflowElement> binder = null ;
    private ListStore<GWTJahiaWorkflowElement> store ;
    private PagingLoader<PagingLoadConfig> loader ;
    private Map<String, Set<String>> checked;
    private Map<String, Set<String>> disabledChecks;
    private Map<String, String> titleForObjectKey ;
    private Set<String> availableActionsInTable;

    public WorkflowTable() {
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setHeaderVisible(false);
        m_component.setBodyBorder(false);
        m_component.setBorders(false);
        m_table = new Table(emptyColumnModel()) ;
        m_component.add(m_table) ;

        titleForObjectKey = new HashMap<String, String>() ;
        checked = new HashMap<String, Set<String>> ();
        disabledChecks = new HashMap<String, Set<String>> ();
        availableActionsInTable = new HashSet<String>();
        // data proxy
        RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaWorkflowElement>> proxy = new RpcProxy<PagingLoadConfig, PagingLoadResult<GWTJahiaWorkflowElement>>() {
            @Override
            protected void load(PagingLoadConfig pageConfig, AsyncCallback<PagingLoadResult<GWTJahiaWorkflowElement>> listAsyncCallback) {
                final GWTJahiaWorkflowElement selection = (GWTJahiaWorkflowElement) getLinker().getTreeSelection() ;
                if (selection != null) {
                    int offset = pageConfig.getOffset();
                    String sortParameter = pageConfig.getSortInfo().getSortField() ;
                    int depth = getFlattenDepth() ;
                    int pageSize = pagingToolBar.getPageSize() ;
                    boolean isAscending = pageConfig.getSortInfo().getSortDir().equals(Style.SortDir.ASC);
                    service.getPagedFlattenedSubElements(selection, depth, offset, pageSize, sortParameter, isAscending, listAsyncCallback);
                }
            }
        };

        // tree loader
        loader = new BasePagingLoader<PagingLoadConfig, PagingLoadResult<GWTJahiaWorkflowElement>>(proxy) {
            @Override
            protected void onLoadSuccess(PagingLoadConfig pagingLoadConfig, PagingLoadResult<GWTJahiaWorkflowElement> gwtJahiaWorkflowElementPagingLoadResult) {
                availableActionsInTable.clear();
                for (GWTJahiaWorkflowElement wfEl: gwtJahiaWorkflowElementPagingLoadResult.getData()) {
                    for (String lang: languageCodes) {
                        if (wfEl.getAvailableAction().containsKey(lang)) {
                            Set<String> acts = wfEl.getAvailableAction().get(lang);
                            availableActionsInTable.addAll(acts);
                        }
                    }
                }
                super.onLoadSuccess(pagingLoadConfig, gwtJahiaWorkflowElementPagingLoadResult);
                binder.setSelection(binder.getStore().getAt(0));
                ((WorkflowToolbar)getLinker().getTopObject()).setAvailableAction(availableActionsInTable);
                m_component.layout() ;
            }
        };
        loader.setRemoteSort(true);

        pagingToolBar = new PagingToolBar(pageSize);
        PagingToolBar.PagingToolBarMessages msgs = pagingToolBar.getMessages() ;
        msgs.setBeforePageText(Messages.getResource("wf_pagingPage")) ;
        msgs.setAfterPageText(Messages.getResource("wf_pagingOf")) ;
        msgs.setDisplayMsg(Messages.getResource("wf_pagingDisplay"));
        msgs.setEmptyMsg(Messages.getResource("wf_pagingNodata"));
        msgs.setFirstText(Messages.getResource("wf_pagingFirst"));
        msgs.setLastText(Messages.getResource("wf_pagingLast"));
        msgs.setNextText(Messages.getResource("wf_pagingNext"));
        msgs.setPrevText(Messages.getResource("wf_pagingPrevious"));
        msgs.setRefreshText(Messages.getResource("wf_pagingRefresh"));

        final NumberField depthField = new NumberField() ;
        depthField.setAllowDecimals(false);
        depthField.setAllowNegative(false);
        depthField.setAllowBlank(false);
        depthField.setValue(Integer.valueOf(depth));
        depthField.setWidth(25);
        AdapterToolItem depthFieldBox = new AdapterToolItem(depthField) ;
        depthFieldBox.addStyleName("item-field");
        pagingToolBar.add(new LabelToolItem(Messages.getResource("wf_depth"))) ;
        pagingToolBar.add(depthFieldBox) ;
        depthField.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (depthField.getValue() != null) {
                    if (depthField.getValue().intValue() > 5) {
                        Info.display("", "A maximum depth of 5 is allowed...");
                        depthField.setValue(5);
                    }
                    depth = depthField.getValue().intValue() ;
                    getLinker().refreshTable();
                }
            }
        });

        pagingToolBar.add(new SeparatorToolItem()) ;

        final NumberField pageField = new NumberField() ;
        pageField.setAllowDecimals(false);
        pageField.setAllowNegative(false);
        pageField.setAllowBlank(false);
        pageField.setValue(Integer.valueOf(pageSize));
        pageField.setWidth(25);
        AdapterToolItem pageFieldBox = new AdapterToolItem(pageField) ;
        pageFieldBox.addStyleName("item-field");
        pagingToolBar.add(new LabelToolItem(Messages.getResource("wf_itemsPerPage"))) ;
        pagingToolBar.add(pageFieldBox) ;
        pageField.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                if (pageField.getValue() != null) {
                    if (pageField.getValue().intValue() == 0) {
                        Info.display("", "At least 1 item/page...");
                        pageField.setValue(1);
                    }
                    if (pageField.getValue().intValue() > 100) {
                        Info.display("", "No more than 100 items/page...");
                        pageField.setValue(100);
                    }
                    pageSize = pageField.getValue().intValue() ;
                    getLinker().refreshTable();
                }
            }
        });

        pagingToolBar.bind(loader);
        m_component.setBottomComponent(pagingToolBar);

        store = new ListStore<GWTJahiaWorkflowElement>(loader) ;
        m_table.setBulkRender(false);
    }

    private TableColumnModel emptyColumnModel() {
        ArrayList<TableColumn> headerList = new ArrayList<TableColumn>();
        TableColumn col = new TableColumn("object", "") ;
        col.setResizable(false);
        headerList.add(col) ;
        return new TableColumnModel(headerList) ;
    }

    private void initHeaders(final Object root) {
        service.getWorkflowManagerState(new AsyncCallback<GWTJahiaWorkflowManagerState>() {
            public void onFailure(Throwable throwable) {
                Window.alert(Messages.getResource("wf_noLanguages") + "\n\n" + throwable.getLocalizedMessage()) ;
            }

            public void onSuccess(GWTJahiaWorkflowManagerState state) {
                if (state != null) {
                    restorePreviousState(state) ;
                    List<String> codes = state.getAvailableLanguages() ;
                    if (codes != null) {
                        languageCodes = codes ;
                        setHeaders();
                        setContentWithHeadersInitialized(root);
                        setContextMenu();
                    }

                } else {
                    Log.error("workflow state was null") ;
                }
            }
        });
    }

    public void setHeaders() {
        ArrayList<TableColumn> headerList = new ArrayList<TableColumn>();
        TableColumn col = new TableColumn("type", "", 30) ;
        col.setResizable(false);
        col.setRenderer( new WorkflowCellRenderer() );
        headerList.add(col) ;

        col = new TableColumn("validation", "", 30) ;
        col.setResizable(false);
        headerList.add(col) ;

        col = new TableColumn("title", Messages.getResource("wf_title"), 150) ;
        headerList.add(col) ;
        for (String languageCode: languageCodes) {
            col = new TableColumn(languageCode, languageCode, 40) ;
            col.setResizable(false);
            col.setAlignment(Style.HorizontalAlignment.CENTER);
            headerList.add(col) ;
        }

        col = new TableColumn("path", Messages.getResource("wf_path"), 390) ;
        col.setHidden(true);
        headerList.add(col) ;

        if (m_table == null) {
            Log.debug("Table is null");
        } else {
            m_component.remove(m_table) ;
        }

//        loader.load(0,10);

        m_table = new Table((new TableColumnModel(headerList)));
        m_component.add(m_table) ;
        m_table.setBulkRender(false);
        m_table.setHorizontalScroll(true) ;

        m_table.addTableListener(new TableListener() {
            @Override
            public void tableRowDoubleClick(TableEvent event) {
                if (languageCodes != null && languageCodes.size() > 0) {
                    TableItem tableItem = event.item ;
                    for (int i=0; i<languageCodes.size(); i++) {
                        WorkflowCheckbox checkbox = (WorkflowCheckbox)tableItem.getValue(i+3) ;
                        if (checkbox.isEnabled() && !checkbox.isChecked()) {
                            checkbox.setChecked(true, true);
                        }
                    }
                }
            }
        });

        binder = new TableBinder<GWTJahiaWorkflowElement>(m_table, store) ;
        binder.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaWorkflowElement>() {
            public void selectionChanged(SelectionChangedEvent<GWTJahiaWorkflowElement> event) {
                getLinker().onTableItemSelected();
            }
        });
        binder.init() ;
    }

    public void setContextMenu() {
        Menu selectMenu = new Menu() ;
        if (languageCodes.size() > 1) {
            MenuItem all = new MenuItem(Messages.getResource("wf_selectAll")) ;
            all.addSelectionListener(new SelectListener("all"));
            all.setIconStyle("wf-selection-select");
            selectMenu.add(all) ;
        }
        MenuItem none = new MenuItem(Messages.getResource("wf_deselectAll")) ;
        none.addSelectionListener(new SelectListener("none"));
        none.setIconStyle("wf-selection-deselect");
        selectMenu.add(none) ;
        Menu previewMenu = new Menu();
        Menu compareMenu = new Menu();
        for (final String lang: languageCodes) {
            MenuItem langItem = new MenuItem(Messages.getResource("wf_select") + " " + lang) ;
            langItem.setIconStyle("flag_" + lang);
            langItem.addSelectionListener(new SelectListener(lang));
            selectMenu.add(langItem) ;
            MenuItem previewItem = new MenuItem(lang, new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent componentEvent) {
                    openPreviewWindow(lang, false);
                }
            });
            MenuItem compareItem = new MenuItem(lang, new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent componentEvent) {
                    openPreviewWindow(lang, true);
                }
            });
            previewItem.setIconStyle("flag_" + lang);
            compareItem.setIconStyle("flag_" + lang);
            previewMenu.add(previewItem);
            compareMenu.add(compareItem);
        }
        MenuItem preview = new MenuItem(Messages.getResource("wf_preview"));
        preview.setSubMenu(previewMenu);
        preview.setIconStyle("wf-preview");
        MenuItem compare = new MenuItem(Messages.getResource("wf_compare"));
        compare.setSubMenu(compareMenu);
        compare.setIconStyle("wf-compare");
        selectMenu.add(preview);
        selectMenu.add(compare);
        m_table.setContextMenu(selectMenu);
    }
    
    private void openPreviewWindow(String languageCode, boolean compare) {
        Object sel = getLinker().getTableSelection();
        if (sel != null) {
            GWTJahiaWorkflowElement selection = (GWTJahiaWorkflowElement) sel;
            service.getPreviewLink(selection.getObjectKey(), compare, languageCode, new AsyncCallback<String>() {
                public void onFailure(Throwable throwable) {
                    Log.error("An error occured:\n" + throwable);
                }
                public void onSuccess(String s) {
                    if (s != null) {
                        Window.open(s, "_blank", "");
                    }
                }
            });
        }
    }

    public void setContent(Object root) {
        if (languageCodes == null) {
            initHeaders(root);
        } else {
            setContentWithHeadersInitialized(root);
        }
    }

    private void setContentWithHeadersInitialized(Object root) {
        if (root != null) {
            pagingToolBar.setPageSize(getPageSize());
            loader.load(0, 10);
        } else {
            store.removeAll();
        }
    }

    public int getFlattenDepth() {
        return depth ;
    }

    public int getPageSize() {
        return pageSize ;
    }

    public void processItems() {
        for (TableItem item: m_table.getItems()) {
            processItem(item);
        }
    }

    private void processItem(TableItem tableItem) {
        final GWTJahiaWorkflowElement wfEl = (GWTJahiaWorkflowElement) tableItem.getModel();
        boolean isGloballyDisabled = wfEl.getStealLock() != null || wfEl.isValidationBlocker() ;
        String action = ((WorkflowToolbar) getLinker().getTopObject()).getAction();
        boolean rowEnabled = false;
        for (int i=0; i<languageCodes.size(); i++) {
            WorkflowCheckbox checkbox = (WorkflowCheckbox)tableItem.getValue(i+3) ;
            boolean enabled = false;
            String lang = languageCodes.get(i) ;
            if (!isGloballyDisabled && wfEl.getAvailableAction().containsKey(lang)) {
                enabled = wfEl.getAvailableAction().get(lang).contains(action);
            }
            processCheckboxEnablesState(wfEl.getObjectKey(), lang, enabled, checkbox);
            rowEnabled |= enabled;
        }
        tableItem.setEnabled(rowEnabled && !isGloballyDisabled);
    }

    /**
     * Set checkbox state depending on previous selections if any
     * @param key the object key
     * @param lang the language
     * @param enabled checkbox can be enabled
     * @param checkbox the checkbox object (state will be changed)
     */
    private void processCheckboxEnablesState(final String key, final String lang, boolean enabled, final WorkflowCheckbox checkbox) {
        if (isCheckedInBatch(key, lang)) {
            checkbox.setChecked(true, false);
            checkbox.setEnabled(false);
        } else {
            if (!enabled) {
                if (checkbox.isChecked()) {
                    if (checked.containsKey(key)) {
                        if (checked.get(key).contains(lang)) {
                            if (!disabledChecks.containsKey(key)) {
                                disabledChecks.put(key, new HashSet<String>()) ;
                            }
                            disabledChecks.get(key).add(lang) ;
                            checkbox.setChecked(false, true);
                        }
                    }
                }
                checkbox.setEnabled(false);
            } else {
                if (checked.containsKey(key)) {
                    if (checked.get(key).contains(lang)) {
                        checkbox.setChecked(true, false);
                    }
                } else if (disabledChecks.containsKey(key)) {
                    if (disabledChecks.get(key).contains(lang)) {
                        checkbox.setChecked(true, true);
                        disabledChecks.get(key).remove(lang) ;
                    }
                } else {
                    checkbox.setChecked(false, false);
                }
                checkbox.setEnabled(true);
            }
        }
    }

    private boolean isCheckedInBatch(String key, String lang) {
        // let's check all the previous batches for existing selections
        Map<String, Map<String, Set<String>>> oldChecked = ((WorkflowToolbar) getLinker().getTopObject()).getBatch() ;
        for (String action: availableActionsInTable) {
            if (oldChecked.containsKey(action)) {
                if (oldChecked.get(action).containsKey(key)) {
                    if (oldChecked.get(action).get(key).contains(lang)) {
                        return true ;
                    }
                }
            }
        }
        return false ;
    }

    public void clearChecked() {
        checked.clear();
        disabledChecks.clear();
        processItems() ;
    }

    class WorkflowCellRenderer implements CellRenderer<TableItem> {
        public String render(TableItem tableItem, String s, Object o) {
            final GWTJahiaWorkflowElement wfEl = (GWTJahiaWorkflowElement) tableItem.getModel();
            // getting the data
            HTML type = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;") ;
            type.setStyleName("icon-" + wfEl.getObjectType());
            HTML validation = new HTML("&nbsp;&nbsp;&nbsp;&nbsp;") ;

            // begin validation part
            int validationStatus = 1 ;
            Map<String, GWTJahiaNodeOperationResult> validationsPerLanguage = wfEl.getValidation() ;
            if (validationsPerLanguage == null) {
                Log.debug("no validations for " + wfEl.getObjectKey());
            }
            if (wfEl.getStealLock() != null) {
                validationStatus = 3 ;
                validation.addClickListener(new ClickListener() {
                    public void onClick(Widget w) {
                        EngineOpener.openEngine(wfEl.getStealLock());
                    }
                });
            } else if (wfEl.isValidationBlocker()) {
                validationStatus = 0 ;
            } else if (validationsPerLanguage != null && !validationsPerLanguage.isEmpty()) {
                validationStatus = 2 ;
            }
            // disable all languages if blocker error or if locked
            boolean isGloballyDisabled = (validationStatus == 0 || validationStatus == 3) ;
            // end validation part

            validation.setStyleName("validation-" + validationStatus);
            List<WorkflowCheckbox> checkboxes = new ArrayList<WorkflowCheckbox>(languageCodes.size()) ;
            Map<String, String> extendedWorkflowStates = wfEl.getWorkflowStates() ;
            for (String lang : languageCodes) {
                String extendedWorkflowState = extendedWorkflowStates.get(lang) ;
                if (extendedWorkflowState == null) {
                    extendedWorkflowState = "000" ;
                }

                // add the checkbox using given parameters
                checkboxes.add(new WorkflowCheckbox(extendedWorkflowState,
                                                    wfEl.getObjectKey(),
                                                    wfEl.getTitle(),
                                                    lang,
                                                    wfEl.getAvailableAction().get(lang),
                                                    (WorkflowToolbar) getLinker().getTopObject(),
                                                    titleForObjectKey,
                                                    checked
                )) ;
            }

            // formatting the data
            Object[] columns = new Object[languageCodes.size() + 4] ;
            tableItem.setWidget(0, type);
            tableItem.setWidget(1, validation);

            columns[0] = type ;
            columns[1] = validation ;

            String action = ((WorkflowToolbar) getLinker().getTopObject()).getAction();
            boolean rowEnabled = false;
            for (int i=0; i<languageCodes.size(); i++) {
                WorkflowCheckbox checkbox = checkboxes.get(i);
                boolean enabled = false;
                String lang = languageCodes.get(i) ;
                if (!isGloballyDisabled && wfEl.getAvailableAction().containsKey(lang)) {
                    Set<String> acts = wfEl.getAvailableAction().get(lang);
                    enabled = acts.contains(action);
                    //availableActionsInTable.addAll(acts); updated before rendering
                }
                processCheckboxEnablesState(wfEl.getObjectKey(), lang, enabled, checkbox);
                rowEnabled |= enabled;
                tableItem.setWidget(i+3, checkbox) ;
            }
            tableItem.setEnabled(rowEnabled && !isGloballyDisabled);

            return "";
        }
    }

    public void clearTable() {
        container.clear();
        m_table.removeAll();
    }

    public Object getSelection() {
        if (binder == null) {
            return null ;
        }
        List<GWTJahiaWorkflowElement> selection = binder.getSelection() ;
        if (selection != null && selection.size() > 0) {
            return selection.get(0) ;
        } else {
            return null ;
        }
    }

    public void selectItems(String language) {
        if (language == null) {
            return ;
        } else {
            List<TableItem> items = m_table.getItems() ;
            if (language.equals("none")) {
                for (TableItem item: items) {
                    for (int i=0; i<languageCodes.size(); i++) {
                        WorkflowCheckbox checkbox = (WorkflowCheckbox) item.getValue(i+3) ;
                        if (checkbox.isEnabled()) {
                            checkbox.setChecked(false);
                        }
                    }
                }
            } else if (language.equals("all")) {
                for (TableItem item: items) {
                    for (int i=0; i<languageCodes.size(); i++) {
                        WorkflowCheckbox checkbox = (WorkflowCheckbox) item.getValue(i+3) ;
                        if (checkbox.isEnabled()) {
                            checkbox.setChecked(true);
                        }
                    }
                }
            } else {
                int i = languageCodes.indexOf(language) ;
                for (TableItem item: items) {
                    WorkflowCheckbox checkbox = (WorkflowCheckbox) item.getValue(i+3) ;
                    if (checkbox.isEnabled()) {
                        checkbox.setChecked(true);
                    }
                }
            }
        }
    }

    public Map<String, Set<String>> getChecked() {
        return checked;
    }

    public Map<String, Set<String>> getDisabledChecks() {
        return disabledChecks;
    }

    public Map<String, String> getTitleForObjectKey() {
        return titleForObjectKey;
    }

    public void refresh() {
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component ;
    }

    private void restorePreviousState(GWTJahiaWorkflowManagerState state) {
        Map<String, Set<String>> checked = state.getChecked() ;
        Map<String, Set<String>> disabledChecks = state.getDisabledChecks() ;
        Map<String, String> titlesForObjectKey = state.getTitleForObjectKey() ;
        Map<String, Map<String, Set<String>>> batch = state.getBatch() ;
        if (checked != null) {
            this.checked.putAll(checked);
        }
        if (disabledChecks != null) {
            this.disabledChecks.putAll(disabledChecks) ;
        }
        if (titlesForObjectKey != null) {
            titleForObjectKey.putAll(titlesForObjectKey);
        }
        if (batch != null) {
            ((WorkflowToolbar) getLinker().getTopObject()).restoreBatch(batch);
        }
    }

    private class SelectListener<E extends ComponentEvent> extends SelectionListener<E> {

        private String language = null ;

        public SelectListener(String language) {
            super() ;
            this.language = language ;
        }

        public void componentSelected(E event) {
            selectItems(language);
        }
    }

}

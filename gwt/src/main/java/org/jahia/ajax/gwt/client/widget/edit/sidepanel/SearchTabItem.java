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

package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ScrollContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTColumn;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.Collator;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.NodeColumnConfigList;
import org.jahia.ajax.gwt.client.widget.content.ContentPickerField;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.ModuleHelper;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.*;

/**
 * Search tab item for the side panel for performing simple queries in the content repository.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class SearchTabItem extends SidePanelTabItem {
    protected int numberResults = 15;


    protected transient ListStore<GWTJahiaNode> contentStore;
    protected transient DisplayGridDragSource displayGridSource;
    private transient TextField<String> searchField;
    private transient ContentPickerField pagePickerField;
    private transient ComboBox<GWTJahiaLanguage> langPickerField;
    private transient ComboBox<GWTJahiaNodeType> defPicker;
    protected transient PagingLoader<PagingLoadResult<GWTJahiaNode>> loader;
    protected transient Grid<GWTJahiaNode> grid;
    private transient CalendarField startDateField;
    private transient CalendarField endDateField;
    private transient ComboBox<ModelData> timesField;
    private transient RadioGroup dateTypeField;
    private String gxtTabId = "JahiaGxtSearchTab";

    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        tab.setLayout(new FitLayout());
        final FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        searchForm.setPadding(4);
        searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.get("label.search"));
        searchField.setId(gxtTabId + "__searchField");
        searchField.addListener(KeyboardEvents.Enter, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                // grid.mask("Loading", "x-mask-loading");
                contentStore.removeAll();
                loader.load(0,numberResults);
            }
        });
        final Button ok = new Button(Messages.get("label.search"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                //  grid.mask("Loading", "x-mask-loading");
                contentStore.removeAll();
                loader.load(0, numberResults);
            }
        });
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.search());
//        final Button drag = new Button(Messages.get("org.jahia.jcr.edit.drag.label"));
//        new EditModeDragSource(drag) {
//            @Override
//            protected void onDragStart(DNDEvent e) {
//                e.setCancelled(false);
//                e.getStatus().update(searchField.getValue());
//                e.getStatus().setStatus(true);
//                e.setData(searchField);
//                e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.QUERY_SOURCE_TYPE);
//                e.getStatus().setData(EditModeDNDListener.SOURCE_QUERY, getGWTJahiaSearchQuery());
//                super.onDragStart(e);
//            }
//        };

        searchForm.add(searchField);

        // page picker field
        pagePickerField = createPageSelectorField();
        searchForm.add(pagePickerField);

        // lang picker
        langPickerField = createLanguageSelectorField();
        searchForm.add(langPickerField);

        defPicker = createNodeSelector();
        searchForm.add(defPicker);
        Radio radio = new Radio();
        radio.setBoxLabel(Messages.get("label.modification","modification"));
        radio.setValueAttribute("1");
        radio.setValue(true);
        Radio radio2 = new Radio();
        radio2.setBoxLabel(Messages.get("label.creation","creation"));
        radio2.setValueAttribute("2");
        Radio radio3 = new Radio();
        radio3.setBoxLabel(Messages.get("label.publication","publication"));
        radio3.setValueAttribute("3");

        dateTypeField = new RadioGroup();
        dateTypeField.setOrientation(Style.Orientation.VERTICAL);
        dateTypeField.setFieldLabel(Messages.get("label.dateType","According date of"));
        dateTypeField.add(radio);
        dateTypeField.add(radio2);
        dateTypeField.add(radio3);
        searchForm.add(dateTypeField);

        startDateField = new CalendarField("dd.MM.yyyy", false, false, null, false, null) {
            @Override
            protected void onClick(ComponentEvent ce) {
                timesField.clearSelections();
                super.onClick(ce);
            }
        };
        startDateField.setFieldLabel(Messages.get("label.startDate","Start Date"));

        endDateField = new CalendarField("dd.MM.yyyy", false, false, null, false, null) {
            @Override
            protected void onClick(ComponentEvent ce) {
                timesField.clearSelections();
                super.onClick(ce);
            }
        };
        endDateField.setFieldLabel(Messages.get("label.endDate","End Date"));
        searchForm.add(startDateField);
        searchForm.add(endDateField);
        String[] timesValues = {"1day,1","1week,7","2weeks,14","1month,30","3months,90","6months,180","1year,365"};
        ListStore<ModelData> times = new ListStore<ModelData>();
        for (String timesValue : timesValues) {
            String[] value = timesValue.split(",");
            ModelData d = new BaseModelData();
            d.set("key",value[1]);
            d.set("title",Messages.get("label." + value[0], value[0]));
            times.add(d);
        }

        timesField = new ComboBox<ModelData>(){

            @Override
            protected void onClick(ComponentEvent ce) {
                startDateField.clear();
                endDateField.clear();
                this.clear();
                super.onClick(ce);
            }
        };
        timesField.setDisplayField("title");
        timesField.setValueField("key");
        timesField.setStore(times);
        timesField.setFieldLabel(Messages.get("label.timeRange", "Time range"));
        searchForm.add(timesField);
        searchForm.addButton(ok);
//        searchForm.addButton(drag);

        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new RowLayout(Style.Orientation.VERTICAL));
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.addStyleName("x-panel-mc");
        panel.add(searchForm, new RowData(1, -1, new Margins(0)));


        RpcProxy<PagingLoadResult<GWTJahiaNode>> proxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
            @Override
            public void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
                doSearch((PagingLoadConfig) loadConfig, callback);
            }
        };

        // loader
        loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(proxy);
        loader.setRemoteSort(true);
        final PagingToolBar toolBar = new PagingToolBar(numberResults);
        toolBar.bind(loader);
        contentStore = new ListStore<GWTJahiaNode>(loader);

        List<GWTColumn> columnNames = new ArrayList<GWTColumn>();
        columnNames.add(new GWTColumn("icon",Messages.get("label.icon", ""),40));
        columnNames.add(new GWTColumn("displayName",Messages.get("label.name", "Name"),240));
        final NodeColumnConfigList columnConfigList = new NodeColumnConfigList(columnNames);
        columnConfigList.init();

        grid = new Grid<GWTJahiaNode>(contentStore, new ColumnModel(columnConfigList));

        ContentPanel gridPanel = new ContentPanel();
        gridPanel.setLayout(new FitLayout());
        gridPanel.setBottomComponent(toolBar);
        gridPanel.setHeaderVisible(false);
        gridPanel.setFrame(false);
        gridPanel.setBodyBorder(false);
        gridPanel.setBorders(false);
        gridPanel.add(grid);

        panel.add(gridPanel, new RowData(1, 1, new Margins(0, 0, 0, 0)));
        tab.add(panel);
        grid.addListener(Events.OnDoubleClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                final GWTJahiaNode node = (GWTJahiaNode) ((GridEvent) be).getModel();
                if (ModuleHelper.getNodeType(node.getNodeTypes().get(0)) == null) {
                    JahiaContentManagementService.App.getInstance().getNodeType(node.getNodeTypes().get(0), new AsyncCallback<GWTJahiaNodeType>() {
                        public void onFailure(Throwable caught) {
                            // Do nothing
                        }
                        public void onSuccess(GWTJahiaNodeType result) {
                            if (!Boolean.FALSE.equals(result.get("canUseComponentForEdit"))) {
                                EngineLoader.showEditEngine(editLinker, node);
                            }
                        }
                    });
                } else {
                    if (!Boolean.FALSE.equals(ModuleHelper.getNodeType(node.getNodeTypes().get(0)).get("canUseComponentForEdit"))) {
                        EngineLoader.showEditEngine(editLinker, node);
                    }
                }
            }
        });
        grid.setContextMenu(createContextMenu(config.getTableContextMenu(), grid.getSelectionModel()));

        tab.setId(gxtTabId);
        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);
        if (linker.getConfig().isEnableDragAndDrop()) {
            displayGridSource = new DisplayGridDragSource(grid);
            displayGridSource.addDNDListener(editLinker.getDndListener());
        }
    }

    /**
     * Create new page picker field
     *
     * @return
     */
    private ContentPickerField createPageSelectorField() {
        ContentPickerField field = new ContentPickerField(null, null, null, null,
                ManagerConfigurationFactory.PAGEPICKER, false);
        field.setFieldLabel(Messages.get("label.pagePicker", "Pages"));
        field.setId(gxtTabId + "__pageSelector");
        return field;
    }

    /**
     * Create language field
     *
     * @return
     */
    private ComboBox<GWTJahiaLanguage> createLanguageSelectorField() {
        final ComboBox<GWTJahiaLanguage> combo = new ComboBox<GWTJahiaLanguage>();
        combo.setFieldLabel(Messages.get("label.language", "Language"));
        combo.setStore(new ListStore<GWTJahiaLanguage>());
        combo.setDisplayField("displayName");
        combo.setTemplate(getLangSwitchingTemplate());
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        combo.getStore().removeAll();
        combo.getStore().add(JahiaGWTParameters.getSiteLanguages());
        combo.setId(gxtTabId + "__languageSelector");
        return combo;
    }

    /**
     * Create nodeTypes field
     *
     * @return
     */
    private ComboBox<GWTJahiaNodeType> createNodeSelector() {
        // create a definition for j:node
        final ComboBox<GWTJahiaNodeType> combo = new ComboBox<GWTJahiaNodeType>();
        combo.setFieldLabel(Messages.get("nodes.label", "Node type"));
        combo.setStore(new ListStore<GWTJahiaNodeType>());
        combo.setDisplayField("label");
        combo.setValueField("name");
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        combo.getStore().setStoreSorter(new StoreSorter<GWTJahiaNodeType>(new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                if (o1 instanceof String && o2 instanceof String) {
                    String s1 = (String) o1;
                    String s2 = (String) o2;
                    return Collator.getInstance().localeCompare(s1, s2);
                } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                    return ((Comparable) o1).compareTo(o2);
                }
                return 0;
            }
        }));
        JahiaContentManagementService.App.getInstance().getContentTypes(Arrays.asList("jmix:editorialContent", "jnt:portlet"), true, false, new BaseAsyncCallback<Map<GWTJahiaNodeType, List<GWTJahiaNodeType>>>() {
            public void onSuccess(Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> result) {
                for (GWTJahiaNodeType key : result.keySet()) {
                    combo.getStore().add(result.get(key));
                }
                combo.getStore().sort("label", Style.SortDir.ASC);
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to get nodetypes :", caught);
            }
        });
        combo.setId(gxtTabId + "__nodeSelector");
        return combo;
    }


    /**
     * Method used by seach form
     */
    private void doSearch(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> callback) {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = getGWTJahiaSearchQuery();
        int limit = 500;
        int offset = 0;
        if (loadConfig != null) {
            limit = loadConfig.getLimit();
            offset = loadConfig.getOffset();
        }

        Log.debug(searchField.getValue() + "," + pagePickerField.getValue() + "," + langPickerField.getValue());
        JahiaContentManagementService.App.getInstance().search(gwtJahiaSearchQuery, limit, offset, false, callback);

    }

    /**
     * Get the GWTJahiaSearchQuery that corresponds to what is selected in fields
     *
     * @return
     */
    private GWTJahiaSearchQuery getGWTJahiaSearchQuery() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = new GWTJahiaSearchQuery();
        gwtJahiaSearchQuery.setQuery(searchField.getValue());
        gwtJahiaSearchQuery.setInContents(true);
        gwtJahiaSearchQuery.setInTags(true);
        gwtJahiaSearchQuery.setOriginSiteUuid(JahiaGWTParameters.getSiteUUID());
        gwtJahiaSearchQuery.setPages(pagePickerField.getValue().size() >0 ? pagePickerField.getValue(): Arrays.asList(JahiaGWTParameters.getSiteNode()));
        gwtJahiaSearchQuery.setLanguage(langPickerField.getValue());
        if ((endDateField != null && endDateField.getValue() != null) ||
                (startDateField != null && startDateField.getValue() != null) ||
                (timesField != null && timesField.getValue() !=  null)) {
            Date startDate  = startDateField.getValue();
            Date endDate = null;

            if (timesField.getValue() !=  null) {
                gwtJahiaSearchQuery.setTimeInDays((String) timesField.getValue().get("key"));
                endDate = new Date();
            }

            if (endDate == null) {
                endDate = endDateField.getValue();
            }
            switch (Integer.parseInt(dateTypeField.getValue().getValueAttribute())) {
                case 1 :
                    gwtJahiaSearchQuery.setStartEditionDate(startDate);
                    gwtJahiaSearchQuery.setEndEditionDate(endDate);
                    break;
                case 2 :
                    gwtJahiaSearchQuery.setStartCreationDate(startDate);
                    gwtJahiaSearchQuery.setEndCreationDate(endDate);
                    break;
                case 3 :
                    gwtJahiaSearchQuery.setStartPublicationDate(startDate);
                    gwtJahiaSearchQuery.setEndPublicationDate(endDate);
                    break;
            }
        }
        List<String> list = new ArrayList<String>();
        if (defPicker.getValue() != null) {
            list.add(defPicker.getValue().getName());
        } else {
            list.add("jmix:editorialContent");
        }
        gwtJahiaSearchQuery.setNodeTypes(list);
        return gwtJahiaSearchQuery;
    }

    public int getNumberResults() {
        return numberResults;
    }

    public void setNumberResults(int numberResults) {
        this.numberResults = numberResults;
    }

    /**
     * LangSwithcing template
     *
     * @return
     */
    private static native String getLangSwitchingTemplate()  /*-{
        return  [
            '<tpl for=".">',
            '<div class="x-combo-list-item"><img src="{image}"/> {displayName}</div>',
            '</tpl>'
        ].join("");
    }-*/;

}
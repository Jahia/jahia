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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.*;

/**
 * Form for searching repository content.
 * User: ktlili
 * Date: Feb 18, 2010
 * Time: 11:17:49 AM
 */
public class ContentSearchForm extends ContentPanel implements AbstractView.ContentSource {
    private TextField<String> searchField;
    private ContentPickerField pagePickerField;
    private ComboBox<GWTJahiaLanguage> langPickerField;
    private CalendarField startDateField;
    private CalendarField endDateField;
    private ComboBox<ModelData> timesField;
    private SimpleComboBox<String> itemPerPage;
    private RadioGroup dateTypeField;
    private CheckBox inNameField;
    private CheckBox inTagField;
    private CheckBox inContentField;
    private CheckBox inFileField;
    private CheckBox inMetadataField;
    private ManagerLinker linker;
    private GWTManagerConfiguration config;

    public ContentSearchForm(GWTManagerConfiguration config) {
        this.config = config;
        setLayout(new RowLayout(Style.Orientation.VERTICAL));
        setWidth("100%");
        setHeight("100%");

        final FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.get("search.label"));
        searchField.addKeyListener(new KeyListener() {
            public void componentKeyPress(ComponentEvent event) {
                if (event.getKeyCode() == 13) {
                    doSearch();
                }
            }
        });
        final Button ok = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                doSearch();
            }
        });
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.search());
        ok.addStyleName("button-ok");

        final Button save = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                saveSearch();
            }
        });
        save.addStyleName("button-save");
        save.setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        save.setToolTip(Messages.get("saveSearch.label"));

        // main search field
        HorizontalPanel mainField = new HorizontalPanel();
        mainField.setSpacing(2);
        LayoutContainer formLayoutContainer = new LayoutContainer();
        FormLayout flayout = new FormLayout();
        flayout.setLabelWidth(50);
        formLayoutContainer.setLayout(flayout);
        formLayoutContainer.add(searchField);
        mainField.add(formLayoutContainer);
        mainField.add(ok);
        mainField.add(save);
        add(mainField, new RowData(1, -1, new Margins(0)));

        // advanced part
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeadingHtml(Messages.get("label.detailed", "Advanced"));
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(70);

        fieldSet.setLayout(layout);
        fieldSet.setCollapsible(false);

        // page picker field
        pagePickerField = createPageSelectorField();
        fieldSet.add(pagePickerField);
        if (!config.isDisplaySearchInPage()) {
            pagePickerField.hide();
        }

        // lang picker
        langPickerField = createLanguageSelectorField();
        fieldSet.add(langPickerField);

        searchForm.add(fieldSet);

        final CheckBoxGroup scopeCheckGroup = new CheckBoxGroup();
        scopeCheckGroup.setOrientation(Style.Orientation.VERTICAL);
        scopeCheckGroup.setFieldLabel(Messages.get("label_searchScope","Search scope"));
        // scope name field
        inNameField = createNameField();
        scopeCheckGroup.add(inNameField);

        // scope tag field
        inTagField = createTagField();
        scopeCheckGroup.add(inTagField);

        // scope metadata field
        inMetadataField = createMetadataField();
        scopeCheckGroup.add(inMetadataField);
        inMetadataField.hide();

        // scope content field
        inContentField = createContentField();
        scopeCheckGroup.add(inContentField);

        // scope file field
        inFileField = createFileField();
        scopeCheckGroup.add(inFileField);

        fieldSet.add(scopeCheckGroup,new FormData("-20"));

        if (config.isDisplaySearchInDateMeta()) {
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
            fieldSet.add(dateTypeField);

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
            fieldSet.add(startDateField);
            fieldSet.add(endDateField);
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
            timesField.setFieldLabel(Messages.get("label.timeRange","Time range"));
            fieldSet.add(timesField);

            itemPerPage = new SimpleComboBox<String>(){
                protected void onClick(ComponentEvent ce) {
                    this.clear();
                    super.onClick(ce);
                }
            };
            itemPerPage.add(Arrays.asList("20", "50", "100", "500",Messages.get("label.all","All")));

            itemPerPage.setSimpleValue("50");
            itemPerPage.setFieldLabel(Messages.get("label.itemPerPage", "items per page"));
            fieldSet.add(itemPerPage);

        }
        setWidth("100%");
        setFrame(true);
        setCollapsible(false);
        setBodyBorder(false);
        setHeaderVisible(false);
        getHeader().setBorders(false);
        add(searchForm, new RowData(1, 1, new Margins(0, 0, 20, 0)));
    }

    /**
     * init with linker
     *
     * @param linker
     */
    public void initWithLinker(ManagerLinker linker) {
        this.linker = linker;
    }

    /**
     * Create new page picker field
     *
     * @return
     */
    private ContentPickerField createPageSelectorField() {
        ContentPickerField field = new ContentPickerField(null, null, null, null, ManagerConfigurationFactory.PAGEPICKER, false);
        field.setFieldLabel(Messages.get("label.pagePicker", "Pages"));
        return field;
    }

    /**
     * Create a new scope fields group selector field
     *
     * @return
     */
    private CheckBox createNameField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.name", "Name & Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("name");
        field.setValue(true);
        return field;
    }

    /**
     * Create tag field
     *
     * @return
     */
    private CheckBox createTagField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.tags", "Tags"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("tag");
        field.setValue(true);
        if (!config.isDisplaySearchInTag()) {
            field.hide();
        }
        return field;
    }

    /**
     * Create metadataFied
     *
     * @return
     */
    private CheckBox createMetadataField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.metadata", "Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("metadata");
        field.setValue(true);
        field.hide();
        return field;
    }

    /**
     * Create content field
     *
     * @return
     */
    private CheckBox createContentField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label.content", "Content"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("content");
        field.setValue(config.isSearchInContent() ? true : false);
        if (!config.isDisplaySearchInContent()) {
            field.hide();
        }
        return field;
    }

    /**
     * Create file field
     *
     * @return
     */
    private CheckBox createFileField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("fileMenu.label", "File"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("file");
        field.setValue(config.isSearchInFile() ? true : false);
        if (!config.isDisplaySearchInFile()) {
            field.hide();
        }
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
        combo.setAllowBlank(true);
        combo.setStore(new ListStore<GWTJahiaLanguage>());
        combo.setDisplayField("displayName");
        combo.setTemplate(getLangSwitchingTemplate());
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        combo.getStore().removeAll();
        combo.getStore().add(JahiaGWTParameters.getSiteLanguages());

        return combo;
    }


    /**
     * Method used by seach form
     */
    public void doSearch() {
        linker.getTopRightObject().getComponent().mask(Messages.get("label.searching","Searching ..."),"x-mask-loading");
        final GWTJahiaSearchQuery gwtJahiaSearchQuery = getCurrentQuery();
        if (gwtJahiaSearchQuery.getQuery() != null ||
                (endDateField != null && endDateField.getValue() != null) ||
                (startDateField != null && startDateField.getValue() != null) ||
                (timesField != null && timesField.getValue() != null)) {
            Log.debug(searchField.getValue() + "," +
                    pagePickerField.getValue() + "," +
                    langPickerField.getValue() + "," +
                    inNameField.getValue() + "," +
                    inTagField.getValue());
            RpcProxy<PagingLoadResult<GWTJahiaNode>> privateProxy = new RpcProxy<PagingLoadResult<GWTJahiaNode>>() {
                @Override
                protected void load(Object loadConfig, AsyncCallback<PagingLoadResult<GWTJahiaNode>> pagingLoadResultAsyncCallback) {
                    int limit = -1;
                    int offset = -1;
                    if (loadConfig instanceof BasePagingLoadConfig) {
                        BasePagingLoadConfig pConf = (BasePagingLoadConfig) loadConfig;
                        limit = pConf.getLimit();
                        offset = pConf.getOffset();
                    }
                    JahiaContentManagementService.App.getInstance().search(gwtJahiaSearchQuery, limit, offset, config.isShowOnlyNodesWithTemplates(), pagingLoadResultAsyncCallback);
                }
            };
            BasePagingLoader<PagingLoadResult<GWTJahiaNode>> loader = new BasePagingLoader<PagingLoadResult<GWTJahiaNode>>(privateProxy) {
                @Override
                protected void onLoadSuccess(Object loadConfig, PagingLoadResult<GWTJahiaNode> result) {
                    if (result.getData().size() == 0) {
                        com.google.gwt.user.client.Window.alert(Messages.get("label.noResult","No result"));
                    }
                    linker.getTopRightObject().setProcessedContent(result.getData(), ContentSearchForm.this);
                    linker.loaded();
                    linker.getTopRightObject().getComponent().unmask();
                    super.onLoadSuccess(loadConfig, result);
                }

                @Override
                protected void onLoadFailure(Object loadConfig, Throwable t) {
                    Log.debug("error while searching nodes due to:", t);
                    linker.getTopRightObject().setProcessedContent(null, ContentSearchForm.this);
                    linker.loaded();
                    linker.getTopRightObject().getComponent().unmask();
                    super.onLoadFailure(loadConfig, t);
                }
            };
            // Limit max set to 100 000 items
            int limit = ( itemPerPage != null && itemPerPage.getValue() != null) ?
                    Messages.get("label.all","All").equals(itemPerPage.getValue().getValue())?
                            100000 :
                            Integer.parseInt(itemPerPage.getValue().getValue()) :
                    50;

            BasePagingLoadConfig loadConfig = new BasePagingLoadConfig(0,limit);
            loader.load(loadConfig);
            linker.getTopRightObject().getToolBar().setPageSize(limit);
            linker.getTopRightObject().getToolBar().bind(loader);
            linker.getTopRightObject().getToolBar().enable();

        } else {
            com.google.gwt.user.client.Window.alert(Messages.get("label.queryEmpty", "Query empty"));
        }
    }

    public void refreshTable() {
        doSearch();
    }

    /**
     * Get current query
     *
     * @return
     */
    private GWTJahiaSearchQuery getCurrentQuery() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = new GWTJahiaSearchQuery();
        gwtJahiaSearchQuery.setQuery(searchField.getValue());
        gwtJahiaSearchQuery.setPages(pagePickerField.getValue());
        gwtJahiaSearchQuery.setLanguage(langPickerField.getValue());
        gwtJahiaSearchQuery.setInName(inNameField.getValue());
        gwtJahiaSearchQuery.setInTags(inTagField.getValue());
        gwtJahiaSearchQuery.setInContents(inContentField.getValue());
        gwtJahiaSearchQuery.setInFiles(inFileField.getValue());
        gwtJahiaSearchQuery.setInMetadatas(inMetadataField.getValue());
        gwtJahiaSearchQuery.setFilters(config.getFilters());
        gwtJahiaSearchQuery.setNodeTypes(config.getNodeTypes());
        gwtJahiaSearchQuery.setFolderTypes(config.getFolderTypes());
        gwtJahiaSearchQuery.setOriginSiteUuid(JahiaGWTParameters.getSiteUUID());
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

        if (config.isSearchInCurrentSiteOnly()) {
            gwtJahiaSearchQuery.setSites(Arrays.asList(JahiaGWTParameters.getSiteKey()));
        }
        if (config.getSearchBasePath() != null) {
            gwtJahiaSearchQuery.setBasePath(config.getSearchBasePath());
        }
        return gwtJahiaSearchQuery;
    }

    /**
     * Save search
     */
    public void saveSearch() {
        GWTJahiaSearchQuery query = getCurrentQuery();
        if (query != null && query.getQuery().length() > 0) {
            String name = Window.prompt(Messages.get("saveSearchName.label", "Please enter a name for this search"), JCRClientUtils.cleanUpFilename(query.getQuery()));
            if (name != null && name.length() > 0) {
                name = JCRClientUtils.cleanUpFilename(name);
                final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                service.saveSearch(query,null, name, false, new BaseAsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        Log.debug("saved.");
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        if (throwable instanceof ExistingFileException) {
                            Window.alert(Messages.get("fm_inUseSaveSearch", "The entered name is already in use."));
                        } else {
                            Log.error("error", throwable);
                        }
                    }


                });
            }
        }

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

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
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfigurationFactory;
import org.jahia.ajax.gwt.client.widget.tripanel.ManagerLinker;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Feb 18, 2010
 * Time: 11:17:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class ContentSearchForm extends ContentPanel {
    private TextField<String> searchField;
    private ContentPickerField pagePickerField;
    private ComboBox<GWTJahiaLanguage> langPickerField;
    private CheckBox inNameField;
    private CheckBox inTagField;
    private CheckBox inContentField;
    private CheckBox inFileField;
    private CheckBox inMetadataField;
    private ManagerLinker linker;
    private ManagerConfiguration config;

    public ContentSearchForm(ManagerConfiguration config) {
        this.config = config;
        setLayout(new RowLayout(Style.Orientation.VERTICAL));
        setWidth("100%");
        setHeight("100%");

        final FormPanel searchForm = new FormPanel();
        searchForm.setHeaderVisible(false);
        searchForm.setBorders(false);
        searchForm.setBodyBorder(false);
        searchField = new TextField<String>();
        searchField.setFieldLabel(Messages.getResource("fm_search"));

        final Button ok = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                doSearch();
            }
        });
        ok.setIconStyle("gwt-toolbar-icon-savedSearch");

        final Button save = new Button("", new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
                saveSearch();
            }
        });
        save.setIconStyle("gwt-toolbar-icon-saveAsReusableComponent");
        save.setToolTip(Messages.getResource("fm_saveSearch"));

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
        fieldSet.setHeading(Messages.get("label_advanced", "Advanced"));
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
     * Create a new date selector field
     *
     * @return
     */
    private Field createDateSelectorField() {
        return null;
    }

    /**
     * Create new page picker field
     *
     * @return
     */
    private ContentPickerField createPageSelectorField() {
        ContentPickerField field = new ContentPickerField(Messages.get("picker_link_header", "Page picker"), Messages.get("picker_link_selection", "Selected page"), null, "/", "", null, "", ManagerConfigurationFactory.LINKPICKER, false, false);
        field.setFieldLabel(Messages.get("picker_link_header", "Pages"));
        return field;
    }

    /**
     * Create a new scope fields group selector field
     *
     * @return
     */
    private CheckBox createNameField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel(Messages.get("label_name", "Name & Metadata"));
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
        field.setFieldLabel(Messages.get("label_tag", "Tags"));
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
        field.setFieldLabel(Messages.get("label_metadata", "Metadata"));
        field.setBoxLabel(field.getFieldLabel());
        field.setName("metadata");
        field.setValue(true);
        return field;
    }

    /**
     * Create content field
     *
     * @return
     */
    private CheckBox createContentField() {
        CheckBox field = new CheckBox();
        field.setFieldLabel("Content");
        field.setBoxLabel(field.getFieldLabel());
        field.setName("content");
        field.setValue(true);
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
        field.setFieldLabel(Messages.get("label_file", "File"));
        field.setBoxLabel(field.getFieldLabel());        
        field.setName("file");
        field.setValue(true);
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
        combo.setFieldLabel("Language");
        combo.setAllowBlank(true);
        combo.setStore(new ListStore<GWTJahiaLanguage>());
        combo.setDisplayField("displayName");
        combo.setTemplate(getLangSwitchingTemplate());
        combo.setTypeAhead(true);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);
        combo.setForceSelection(true);
        JahiaContentManagementService.App.getInstance().getSiteLanguages(new AsyncCallback<List<GWTJahiaLanguage>>() {
            public void onSuccess(List<GWTJahiaLanguage> gwtJahiaLanguages) {
                combo.getStore().removeAll();
                combo.getStore().add(gwtJahiaLanguages);
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while loading languages");
            }
        });
        return combo;
    }


    /**
     * Method used by seach form
     */
    public void doSearch() {
        GWTJahiaSearchQuery gwtJahiaSearchQuery = getCurrentQuery();

        int limit = 500;
        int offset = 0;
        Log.debug(searchField.getValue() + "," + pagePickerField.getValue() + "," + langPickerField.getValue() + "," + inNameField.getValue() + "," + inTagField.getValue());
        JahiaContentManagementService.App.getInstance().search(gwtJahiaSearchQuery, limit, offset, new AsyncCallback<PagingLoadResult<GWTJahiaNode>>() {
            public void onSuccess(PagingLoadResult<GWTJahiaNode> gwtJahiaNodePagingLoadResult) {
                linker.getTopRightObject().setProcessedContent(gwtJahiaNodePagingLoadResult.getData());
                linker.loaded();
            }

            public void onFailure(Throwable throwable) {
                Log.debug("error while searching nodes due to:", throwable);
                linker.getTopRightObject().setProcessedContent(null);
                linker.loaded();
            }
        });

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
        return gwtJahiaSearchQuery;
    }

    /**
     * Save search
     */
    public void saveSearch() {
        GWTJahiaSearchQuery query = getCurrentQuery();
        if (query != null && query.getQuery().length() > 0) {
            String name = Window.prompt(Messages.getNotEmptyResource("fm_saveSearchName", "Please enter a name for this search"), JCRClientUtils.cleanUpFilename(query.getQuery()));
            if (name != null && name.length() > 0) {
                name = JCRClientUtils.cleanUpFilename(name);
                final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                service.saveSearch(query.getQuery(), name, new AsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode o) {
                        Log.debug("saved.");
                    }

                    public void onFailure(Throwable throwable) {
                        if (throwable instanceof ExistingFileException) {
                            Window.alert(Messages.getNotEmptyResource("fm_inUseSaveSearch", "The entered name is already in use."));
                        } else {
                            Log.error("error", throwable);
                        }
                    }


                });
            } else {
                Window.alert(Messages.getNotEmptyResource("fm_failSaveSearch", "The entered name is invalid."));
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

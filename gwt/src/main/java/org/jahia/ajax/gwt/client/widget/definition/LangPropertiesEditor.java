package org.jahia.ajax.gwt.client.widget.definition;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 2:07:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class LangPropertiesEditor extends LayoutContainer {
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();

    // ui vars
    private PropertiesEditor displayedPropertiesEditor;
    private Map<String, PropertiesEditor> langPropertiesEditorMap;
    private List<String> excludedTypes;

    // current node contex variables
    private String dataType;
    private List<GWTJahiaNodeType> nodeTypes;
    private List<GWTJahiaNodeType> mixin;
    private Map<String, GWTJahiaNodeProperty> properties;
    private GWTJahiaNode node;
    private ComboBox<GWTJahiaLanguage> languageSwitcher;
    private VerticalPanel mainPanel;
    private boolean editable = true;
    private GWTJahiaLanguage displayedLocale = null;

    public LangPropertiesEditor(GWTJahiaNode node, String dataType, boolean editable) {
        this.node = node;
        this.dataType = dataType;
        langPropertiesEditorMap = new HashMap<String, PropertiesEditor>();
        this.editable = editable;

        setScrollMode(Style.Scroll.AUTOY);
        setBorders(false);
        mainPanel = new VerticalPanel();
        mainPanel.setBorders(false);


        // add switching form
        languageSwitcher = createLanguageSwitcher();
        mainPanel.add(languageSwitcher);

        add(mainPanel);

        // update node info
        updateNodeInfo(null);
    }


    /**
     * Get properties editor by langCode
     *
     * @param locale
     * @return
     */
    public PropertiesEditor getPropertiesEditorByLang(String locale) {
        if (locale == null) {
            return null;
        }
        return langPropertiesEditorMap.get(locale);
    }

    /**
     * set properties editor by lang
     *
     * @param locale
     */
    private void setPropertiesEditorByLang(PropertiesEditor displayedPropertiesEditor, String locale) {
        if (langPropertiesEditorMap == null || locale == null) {
            return;
        }
        langPropertiesEditorMap.put(locale, displayedPropertiesEditor);
    }

    /**
     * Display properties
     */
    public void updatePropertiesComponent(String locale) {
        if (mixin != null) {
            List<GWTJahiaNodeProperty> previousNon18nProperties = null;
            boolean addSharedLangLabel = true;
            if (displayedPropertiesEditor != null) {
                displayedPropertiesEditor.setVisible(false);
                previousNon18nProperties = displayedPropertiesEditor.getProperties(false, true, false);

            }


            if (locale == null) {
                locale = displayedLocale.getCountryIsoCode();
            }

            PropertiesEditor langPropertiesEditor = getPropertiesEditorByLang(locale);
            if (langPropertiesEditor == null) {
                if (addSharedLangLabel && node.getNodeTypes().contains("jmix:shareable")) {
                    Label label = new Label("Important : This is a shared node, editing it will modify its value for all its usages");
                    label.setStyleAttribute("color", "rgb(200,80,80)");
                    label.setStyleAttribute("font-size", "14px");
                    add(label);
                }
                addSharedLangLabel = false;


                //create and update properties editor
                langPropertiesEditor = new PropertiesEditor(nodeTypes, mixin, properties, false, true, dataType, null, excludedTypes, editable && node.isWriteable(), true);
                langPropertiesEditor.setBorders(false);
                langPropertiesEditor.setBodyBorder(false);
                setPropertiesEditorByLang(langPropertiesEditor, locale);
                mainPanel.add(langPropertiesEditor);
            } else {
                langPropertiesEditor.setVisible(true);
            }

            // synch non18n properties
            if (previousNon18nProperties != null && !previousNon18nProperties.isEmpty()) {
                Map<String, Field<?>> fieldsMap = langPropertiesEditor.getFieldsMap();
                for (GWTJahiaNodeProperty property : previousNon18nProperties) {
                    FormFieldCreator.fillValue(fieldsMap.get(property.getName()), langPropertiesEditor.getGWTJahiaItemDefinition(property), property);
                }
            }


            // update displayed properties 
            displayedPropertiesEditor = langPropertiesEditor;

            mainPanel.layout();
        } else {
            Log.debug("mixin is not set");
        }
    }


    /**
     * Create language switcher component
     *
     * @return
     */
    private ComboBox<GWTJahiaLanguage> createLanguageSwitcher() {
        ComboBox<GWTJahiaLanguage> languageSwitcher = new ComboBox<GWTJahiaLanguage>();
        languageSwitcher.setStore(new ListStore<GWTJahiaLanguage>());
        languageSwitcher.setDisplayField("displayName");
        languageSwitcher.setVisible(false);
        languageSwitcher.setTemplate(getLangSwitchingTemplate());
        languageSwitcher.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                if (!event.getSelectedItem().getCountryIsoCode().equalsIgnoreCase(displayedLocale.getCountryIsoCode())) {
                    onLanguageSelectionChanged(event.getSelectedItem().getCountryIsoCode());
                }
                displayedLocale = event.getSelectedItem();
            }
        });
        languageSwitcher.setTypeAhead(true);
        languageSwitcher.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSwitcher.setForceSelection(true);
        return languageSwitcher;
    }

    /**
     * On language selection changed
     */
    private void onLanguageSelectionChanged(String locale) {
        updateNodeInfo(locale);
    }

    /**
     * load local node
     *
     * @param locale
     */
    private void updateNodeInfo(final String locale) {
        Log.debug("update node info ,locale code = " + locale);
        contentService.getProperties(node.getPath(), locale, new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onSuccess(GWTJahiaGetPropertiesResult result) {
                node = result.getNode();
                nodeTypes = result.getNodeTypes();
                properties = result.getProperties();

                if (displayedLocale == null) {
                    displayedLocale = result.getCurrentLocale();
                    languageSwitcher.setVisible(true);
                    List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                    selected.add(result.getCurrentLocale());
                    languageSwitcher.getStore().add(result.getAvailabledLanguages());
                    languageSwitcher.setSelection(selected);
                }

                //todo : do this in one pass.
                if (mixin == null) {
                    definitionService.getAvailableMixin(result.getNode(), new AsyncCallback<List<GWTJahiaNodeType>>() {
                        public void onSuccess(List<GWTJahiaNodeType> result) {
                            mixin = result;
                            updatePropertiesComponent(locale);
                        }

                        public void onFailure(Throwable caught) {
                            Log.error("Cannot get available mixin", caught);
                        }
                    });
                } else {
                    updatePropertiesComponent(locale);
                }
            }

            public void onFailure(Throwable throwable) {
                Log.error("Cannot get properties", throwable);
            }
        });

    }


    /**
     * Get lang properties per map
     *
     * @return
     */
    public Map<String, List<GWTJahiaNodeProperty>> getLangPropertiesMap() {
        Map<String, List<GWTJahiaNodeProperty>> mapProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();
        Iterator<String> langCodes = langPropertiesEditorMap.keySet().iterator();
        while (langCodes.hasNext()) {
            String langCode = langCodes.next();
            mapProperties.put(langCode, langPropertiesEditorMap.get(langCode).getProperties(true,false,true));
        }
        return mapProperties;
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

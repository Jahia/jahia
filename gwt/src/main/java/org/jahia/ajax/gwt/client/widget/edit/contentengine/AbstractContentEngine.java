package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:57:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContentEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;

    protected static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    protected static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();
    protected GWTEngine config;
    protected Linker linker = null;
    protected List<GWTJahiaNodeType> nodeTypes;
    protected List<GWTJahiaNodeType> mixin;
    protected Map<String, GWTJahiaNodeProperty> properties;
    protected TabPanel tabs;
    protected boolean existingNode = true;
    protected GWTJahiaNode node;
    protected GWTJahiaNode parentNode;
    protected GWTJahiaLanguage defaultLanguageBean;
    protected ComboBox<GWTJahiaLanguage> languageSwitcher;
    protected ButtonBar buttonBar;
    protected String heading;

    protected AbstractContentEngine(GWTEngine config, Linker linker) {
        this.config = config;
        this.linker = linker;
    }

    protected void init() {
        setLayout(new FillLayout());
        setBodyBorder(false);
        setSize(750, 480);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(StandardIconsProvider.STANDARD_ICONS.engineLogoJahia());
        setHeading(heading);

        // init language switcher
        initLanguageSwitcher();
        // init tabs
        tabs = new TabPanel();

        tabs.setBodyBorder(false);
        tabs.setBorders(true);

        initTabs();

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                fillCurrentTab();
            }
        });

        add(tabs);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);

        // copyrigths
        //Text copyright = new Text(Messages.getResource("fm_copyright"));
        //ButtonBar container = new ButtonBar();
        //container.setAlignment(Style.HorizontalAlignment.CENTER);
        //container.add(copyright);
        //buttonsPanel.add(container);
        setBottomComponent(buttonsPanel);

        setFooter(true);
    }

    /**
     * init language switcher
     */
    private void initLanguageSwitcher() {
        languageSwitcher = new ComboBox<GWTJahiaLanguage>();
        languageSwitcher.setStore(new ListStore<GWTJahiaLanguage>());
        languageSwitcher.setDisplayField("displayName");
        languageSwitcher.setVisible(false);
        languageSwitcher.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaLanguage>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaLanguage> event) {
                onLanguageChange();
            }
        });
        languageSwitcher.setTemplate(getLangSwitchingTemplate());
        languageSwitcher.setTypeAhead(true);
        languageSwitcher.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSwitcher.setForceSelection(true);
        getHeader().addTool(languageSwitcher);
    }

    /**
     * Called when a new language has been selected
     */
    protected void onLanguageChange() {

    }

    /**
     * Set availableLanguages
     *
     * @param languages
     */
    protected void setAvailableLanguages(List<GWTJahiaLanguage> languages) {
        if (languageSwitcher != null && !languageSwitcher.isVisible()) {
            //languageSwitcher.getStore().removeAll();
            if (languages != null && !languages.isEmpty()) {
                languageSwitcher.getStore().add(languages);
                List<GWTJahiaLanguage> selected = new ArrayList<GWTJahiaLanguage>();
                selected.add(defaultLanguageBean);
                languageSwitcher.setSelection(selected);
                if (languages.size() > 1) {
                    languageSwitcher.setVisible(true);
                }
            } else {
                languageSwitcher.setVisible(false);
            }
        } else {
            Log.debug("Language switcher disabled.");
        }
    }

    /**
     * Creates and initializes all window tabs.
     */
    protected void initTabs() {
        for (String tab : config.getTabs()) {
            if (tab.equals("content")) {
                tabs.add(new ContentTabItem(this));
            } else if (tab.equals("listOrderingContent")) {
                tabs.add(new ListOrderingContentTabItem(this));
            } else if (tab.equals("createPage")) {
                tabs.add(new CreatePageTabItem(this));
            } else if (tab.equals("template")) {
                tabs.add(new TemplateOptionsTabItem(this));
            } else if (tab.equals("layout")) {
                tabs.add(new LayoutTabItem(this));
            } else if (tab.equals("metadata")) {
                tabs.add(new MetadataTabItem(this));
            } else if (tab.equals("classification")) {
                tabs.add(new ClassificationTabItem(this));
            } else if (tab.equals("option")) {
                tabs.add(new OptionsTabItem(this));
            } else if (tab.equals("rights")) {
                tabs.add(new RightsTabItem(this));
            } else if (tab.equals("usages")) {
                tabs.add(new UsagesTabItem(this));
            } else if (tab.equals("publication")) {
                tabs.add(new PublicationTabItem(this));
            } else if (tab.equals("workflow")) {
                tabs.add(new WorkflowTabItem(this));
            } else if (tab.equals("seo")) {
                tabs.add(new SeoTabItem(this));
            } else if (tab.equals("analytics")) {
                tabs.add(new AnalyticsTabItem(this));
            }
        }
    }

    /**
     * init footer
     */
    protected abstract void initFooter();

    /**
     * fill current tab
     */
    protected void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();

        if (currentTab instanceof EditEngineTabItem) {
            EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTab;
            if (!engineTabItem.isProcessed()) {
                engineTabItem.create(getSelectedLang());
            }
        }
    }

    public Linker getLinker() {
        return linker;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public GWTJahiaNode getParentNode() {
        return parentNode;
    }

    public boolean isExistingNode() {
        return existingNode;
    }

    /**
     * Get Selected Lang
     *
     * @return
     */
    public GWTJahiaLanguage getSelectedLang() {
        if (languageSwitcher == null || languageSwitcher.getSelection().isEmpty()) {
            Log.debug("language switcher value is null");
            return defaultLanguageBean;
        }
        return languageSwitcher.getSelection().get(0);
    }

    /**
     * Get Selected Language Code
     *
     * @return
     */
    public String getSelectedLanguageCode() {
        if (languageSwitcher == null || languageSwitcher.getSelection().isEmpty()) {
            Log.debug("language switcher value is null");
            return null;
        }
        return getSelectedLang().getLanguage();
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

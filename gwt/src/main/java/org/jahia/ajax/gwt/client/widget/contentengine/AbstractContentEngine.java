/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngine;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:57:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContentEngine extends LayoutContainer implements NodeHolder {
    public static final int BUTTON_HEIGHT = 24;

    protected static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    protected static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();
    protected List<GWTEngineTab> config;
    protected Linker linker = null;
    protected List<GWTJahiaNodeType> nodeTypes;
    protected List<GWTJahiaNodeType> mixin;
    protected Map<String, List<GWTJahiaValueDisplayBean>> initializersValues;
    protected Map<String, GWTJahiaNodeProperty> properties;
    protected TabPanel tabs;
    protected boolean existingNode = true;
    protected GWTJahiaNode node;
    protected GWTJahiaNode targetNode;
    protected GWTJahiaLanguage defaultLanguageBean;
    protected ComboBox<GWTJahiaLanguage> languageSwitcher;
    protected ButtonBar buttonBar;
    protected String heading;
    protected EngineContainer container;
    protected GWTJahiaNodeACL acl;
    protected Map<String,Set<String>> referencesWarnings;
    protected GWTJahiaLanguage language;

    // general properties
    protected final List<GWTJahiaNodeProperty> changedProperties = new ArrayList<GWTJahiaNodeProperty>();

    // general properties
    protected final Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties = new HashMap<String, List<GWTJahiaNodeProperty>>();

    protected GWTJahiaNodeACL newNodeACL;


    protected AbstractContentEngine(List<GWTEngineTab> config, Linker linker) {
        this.config = config;
        this.linker = linker;
    }

    protected void init(EngineContainer container) {
        this.container = container;
        setLayout(new FillLayout());
        container.setEngine(this);
        container.getPanel().setHeading(heading);

        // init language switcher
        initLanguageSwitcher();
        // init tabs
        tabs = new TabPanel();

        tabs.setBodyBorder(false);
        tabs.setBorders(true);

        add(tabs);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);
        container.getPanel().setBottomComponent(buttonsPanel);

        container.getPanel().setFooter(true);
        mask(Messages.get("label.loading","Loading..."), "x-mask-loading");
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
                GWTJahiaLanguage previous = language;
                language = event.getSelectedItem();
                onLanguageChange(previous);
            }
        });
        languageSwitcher.setTemplate(getLangSwitchingTemplate());
        languageSwitcher.setTypeAhead(true);
        languageSwitcher.setTriggerAction(ComboBox.TriggerAction.ALL);
        languageSwitcher.setForceSelection(true);
        container.getPanel().getHeader().addTool(languageSwitcher);
    }

    /**
     * Called when a new language has been selected
     * @param previous
     */
    protected void onLanguageChange(GWTJahiaLanguage previous) {

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
     * init footer
     */
    protected abstract void initFooter();

    /**
     * fill current tab
     */
    protected void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (currentTab != null) {
            Object currentTabItem = currentTab.getData("item");
            if (currentTabItem != null && currentTabItem instanceof EditEngineTabItem) {
                EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTabItem;

                if (!((AsyncTabItem)currentTab).isProcessed()) {
                    engineTabItem.init(getSelectedLanguage());
                }
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

    public Map<String, List<GWTJahiaValueDisplayBean>> getInitializersValues() {
        return initializersValues;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public GWTJahiaNodeACL getAcl() {
        return acl;
    }

    public Map<String, Set<String>> getReferencesWarnings() {
        return referencesWarnings;
    }

    public GWTJahiaNode getTargetNode() {
        return targetNode;
    }

    public boolean isExistingNode() {
        return existingNode;
    }

    public boolean isMultipleSelection() {
        return false;
    }

    public List<GWTJahiaNode> getNodes() {
        return Arrays.asList(node);
    }

    /**
     * Get Selected Lang
     *
     * @return
     */
    public String getSelectedLanguage() {
        if (language != null) {
            return language.getLanguage();
        } else {
            return JahiaGWTParameters.getLanguage();
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

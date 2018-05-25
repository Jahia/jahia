/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.LangPropertiesEditor;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

/**
 * 
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:53:30 PM
 * 
 */
public class TranslateContentEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    
    private GWTJahiaNode node;
    private final GWTJahiaLanguage srcLanguage;
    private final GWTJahiaLanguage destLanguage;
    private Linker linker = null;

    private Button ok;
    private CheckBox wipCheckbox;
    private LangPropertiesEditor sourceLangPropertiesEditor;
    private LangPropertiesEditor targetLangPropertiesEditor;
    protected ButtonBar buttonBar;

    private Set<String> workInProgressByLocale = new HashSet<String>();



    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     * @param srcLanguage
     * @param destLanguage
     */
    public TranslateContentEngine(GWTJahiaNode node, Linker linker, GWTJahiaLanguage srcLanguage,
                                  GWTJahiaLanguage destLanguage) {
        addStyleName("translate-content-engine");
        this.linker = linker;
        this.node = node;
        this.srcLanguage = srcLanguage;
        this.destLanguage = destLanguage;

        init();
    }

    protected void init() {
        setLayout(new FitLayout());
        setBodyBorder(false);
        int windowHeight=com.google.gwt.user.client.Window.getClientHeight()-10;
        int windowWidth=com.google.gwt.user.client.Window.getClientWidth()-10;

        setSize(windowWidth, windowHeight);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setHeadingHtml(Messages.get("label.translate", "Translate") + ": " + node.getName());
        LayoutContainer panel = new LayoutContainer();
        panel.setLayout(new BorderLayout());

        sourceLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), false, srcLanguage, null, new LangPropertiesEditor.CallBack() {
            @Override
            public void execute() {
                if (targetLangPropertiesEditor != null && targetLangPropertiesEditor.isNeedRefresh()) {
                    // target lang properties editor is waiting for the source lang properties editor to be fully loaded
                    // it's now the case, so refresh it.
                    targetLangPropertiesEditor.refresh();
                }
            }
        });
        targetLangPropertiesEditor = new LangPropertiesEditor(node, Arrays.asList(GWTJahiaItemDefinition.CONTENT), true, destLanguage, sourceLangPropertiesEditor, new LangPropertiesEditor.CallBack() {
            @Override
            public void execute() {
                wipCheckbox.setValue(workInProgressByLocale.contains(targetLangPropertiesEditor.getDisplayedLocale().getLanguage()));
            }
        });

        // Fill WIP infos
        if (node.get("j:workInProgressStatus") != null) {
            // set languages
            if (node.get("j:workInProgressLanguages") != null) {
                workInProgressByLocale.addAll((List<String>)node.get("j:workInProgressLanguages"));
            }
        }

        Button copyButton = new Button(Messages.get("label.translate.copy", "Copy to other language"));
        copyButton.addStyleName("button-translatecopy");
        sourceLangPropertiesEditor.getTopBar().add(copyButton);
        copyButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                PropertiesEditor sourcePropertiesEditor = sourceLangPropertiesEditor.getPropertiesEditorByLang(sourceLangPropertiesEditor.getDisplayedLocale().getLanguage());
                List<GWTJahiaNodeProperty> props = sourcePropertiesEditor.getProperties();
                Map<String,PropertiesEditor.PropertyAdapterField> fieldsMap = targetLangPropertiesEditor.getPropertiesEditorByLang(targetLangPropertiesEditor.getDisplayedLocale().getLanguage()).getFieldsMap();
                for (final GWTJahiaNodeProperty prop : props) {
                    if (sourcePropertiesEditor.getGWTJahiaItemDefinition(prop).isInternationalized()) {
                        final Field<?> f = fieldsMap.get(prop.getName()).getField();
                        FormFieldCreator.copyValue(prop, f);
                    }
                }
            }
        });

        panel.add(sourceLangPropertiesEditor, new BorderLayoutData(Style.LayoutRegion.WEST, windowWidth/2));
        panel.add(targetLangPropertiesEditor, new BorderLayoutData(Style.LayoutRegion.EAST, windowWidth/2));

        add(panel);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);

        setBottomComponent(buttonsPanel);
        setFooter(true);
        layout();
    }


    /**
     * init buttons
     */
    protected void initFooter() {
        wipCheckbox = new CheckBox();
        wipCheckbox.setBoxLabel(Messages.get("label.saveAsWIP", "Save as work in progress"));
        wipCheckbox.setToolTip(Messages.get("label.saveAsWIP.information", "If checked, this content will ne be part of publication process"));
        wipCheckbox.addListener(Events.Change, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent be) {
                setWorkInProgress(wipCheckbox.getValue());
            }
        });
        buttonBar.add(wipCheckbox);

        ok = new Button(Messages.get("label.save"));
        ok.addStyleName("button-save");

        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        Button cancel = new Button(Messages.get("label.cancel"));
        cancel.addStyleName("button-cancel");
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                TranslateContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }


    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            // node
            final List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            List<GWTJahiaNodeProperty> sharedProperties = new ArrayList<GWTJahiaNodeProperty>();
            nodes.add(node);

            // WIP
            List<GWTJahiaNodePropertyValue> languages = new LinkedList<GWTJahiaNodePropertyValue>();
            for (String locale : workInProgressByLocale) {
                languages.add(new GWTJahiaNodePropertyValue(locale));
            }
            GWTJahiaNodeProperty wipLocaleProperty = new GWTJahiaNodeProperty();
            wipLocaleProperty.setName("j:workInProgressLanguages");
            wipLocaleProperty.setValues(languages);
            wipLocaleProperty.setMultiple(true);
            sharedProperties.add(wipLocaleProperty);

            Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties = targetLangPropertiesEditor.getLangPropertiesMap();
            for (String language : targetLangPropertiesEditor.getLangPropertiesMap().keySet()) {
                if (!changedI18NProperties.containsKey(language)) {
                    changedI18NProperties.put(language, new ArrayList<GWTJahiaNodeProperty>());
                }
            }
            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, null, changedI18NProperties, sharedProperties, null, new BaseAsyncCallback<Object>() {
                public void onApplicationFailure(Throwable throwable) {
                    String message = throwable.getMessage();
                    if (message.contains("Invalid link")) {
                        message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":")+1);
                    }
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n"
                            + message);
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                    TranslateContentEngine.this.hide();
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    linker.refresh(data);
                }
            });
        }

    }

    public void setWorkInProgress(boolean wip) {
        if (wip) {
            workInProgressByLocale.add(targetLangPropertiesEditor.getDisplayedLocale().getLanguage());
        } else {
            workInProgressByLocale.remove(targetLangPropertiesEditor.getDisplayedLocale().getLanguage());
        }
    }

    @Override
    public void focus() {
        // do nothing to prevent menu to disappear when mouse over
    }

}


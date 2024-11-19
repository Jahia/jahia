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
    private TranslateContentEngineSaveCallback translateContentEngineSaveCallback;

    private Set<String> workInProgressByLocale = new HashSet<String>();

    /**
     * Save callback
     */
    public interface TranslateContentEngineSaveCallback {
        /**
         * Called after successful save
         */
        void onSave();
    }

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
        if (node.getWorkInProgressStatus() != null) {
            // set languages
            if (node.getWorkInProgressLanguages() != null) {
                workInProgressByLocale.addAll(node.getWorkInProgressLanguages());
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
                Map<String, PropertiesEditor.PropertyAdapterField> fieldsMap = targetLangPropertiesEditor.getPropertiesEditorByLang(targetLangPropertiesEditor.getDisplayedLocale().getLanguage()).getFieldsMap();
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

        @Override
        public void componentSelected(ButtonEvent event) {
            List<GWTJahiaNodeProperty> sharedProperties = new ArrayList<GWTJahiaNodeProperty>();

            // WIP
            List<GWTJahiaNodePropertyValue> languages = new LinkedList<GWTJahiaNodePropertyValue>();
            boolean saveWip = false;
            List<String> currentLanguages = node.getWorkInProgressLanguages();
            if (currentLanguages == null) {
                currentLanguages = new ArrayList<String>();
            }
            for (String locale : workInProgressByLocale) {
                saveWip |= !currentLanguages.contains(locale);
                languages.add(new GWTJahiaNodePropertyValue(locale));
            }
            // save Wip only if languages added or removed
            if (saveWip || languages.size() != currentLanguages.size()) {
                GWTJahiaNodeProperty wipLocaleProperty = new GWTJahiaNodeProperty();
                wipLocaleProperty.setName(GWTJahiaNode.WORK_IN_PROGRESS_LANGUAGES);
                wipLocaleProperty.setValues(languages);
                wipLocaleProperty.setMultiple(true);
                sharedProperties.add(wipLocaleProperty);

                String status = node.getWorkInProgressStatus();
                if (status == null) {
                    status = GWTJahiaNode.WipStatus.LANGUAGES.name();
                } else if (languages.size() == 0) {
                    status = GWTJahiaNode.WipStatus.DISABLED.name();
                }
                sharedProperties.add(new GWTJahiaNodeProperty(GWTJahiaNode.WORK_IN_PROGRESS_STATUS, status));
            }

            Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties = targetLangPropertiesEditor.getLangPropertiesMap();
            for (String language : targetLangPropertiesEditor.getLangPropertiesMap().keySet()) {
                if (!changedI18NProperties.containsKey(language)) {
                    changedI18NProperties.put(language, new ArrayList<GWTJahiaNodeProperty>());
                }
            }
            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().saveNode(node, null, changedI18NProperties, sharedProperties, null, new BaseAsyncCallback() {

                @Override
                public void onApplicationFailure(Throwable throwable) {
                    String message = throwable.getMessage();
                    if (message.contains("Invalid link")) {
                        message = Messages.get("label.error.invalidlink", "Invalid link") + " : " + message.substring(message.indexOf(":") + 1);
                    }
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n"
                            + message);
                    Log.error("failed", throwable);
                }

                @Override
                public void onSuccess(Object o) {
                    Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                    TranslateContentEngine.this.hide();

                    if (linker != null) {
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_MAIN, true);
                        linker.refresh(data);
                    }

                    if (translateContentEngineSaveCallback != null) {
                        translateContentEngineSaveCallback.onSave();
                    }
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

    public void setTranslateContentEngineSaveCallback(TranslateContentEngineSaveCallback translateContentEngineSaveCallback) {
        this.translateContentEngineSaveCallback = translateContentEngineSaveCallback;
    }

    @Override
    public void focus() {
        // do nothing to prevent menu to disappear when mouse over
    }
}


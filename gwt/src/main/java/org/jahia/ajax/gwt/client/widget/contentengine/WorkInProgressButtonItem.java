/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Radio;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.HashSet;
import java.util.Set;

/**
 * Button Item to handle work in progress in edit engine
 */
public class WorkInProgressButtonItem implements ButtonItem {

    private static final long serialVersionUID = -648440265273432614L;

    private boolean checkedByDefault = false;

    private transient WorkInProgressButton wipButton;

    private String academyUrl;

    @Override
    public BoxComponent create(final AbstractContentEngine engine) {

        // handle default value
        if (engine instanceof CreateContentEngine && checkedByDefault) {
            if (JahiaGWTParameters.getSiteLanguages().size() == 1) {
                engine.setWipStatus(GWTJahiaNode.WipStatus.LANGUAGES);
                Set<String> langs = new HashSet<String>();
                langs.add(JahiaGWTParameters.getSiteLanguages().get(0).getLanguage());
                engine.setWorkInProgressLanguages(langs);
            } else {
                engine.setWipStatus(GWTJahiaNode.WipStatus.ALL_CONTENT);
            }
        }
        if (engine instanceof EditContentEngine) {
            ((EditContentEngine) engine).setWorkInProgressCheckedByDefault(checkedByDefault);
        }

        if (JahiaGWTParameters.getSiteLanguages().size() > 1) {

            wipButton = new WorkInProgressButton(engine);

            final Window window = new WorkInProgressWindow(engine);

            wipButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    window.show();
                }
            });

            return wipButton;
        } else {

            final CheckBoxWip checkbox = new CheckBoxWip();

            checkbox.addListener(Events.Change, new Listener<ComponentEvent>() {

                @Override
                public void handleEvent(ComponentEvent event) {
                    Set<String> langs = new HashSet<String>();
                    if (checkbox.getValue()) {
                        engine.setWipStatus(GWTJahiaNode.WipStatus.LANGUAGES);
                        langs.add(engine.getSelectedLanguage());
                    } else {
                        engine.setWipStatus(GWTJahiaNode.WipStatus.DISABLED);
                    }
                    engine.setWorkInProgressLanguages(langs);
                    engine.updateWipStyle();
                }
            });

            checkbox.setBoxLabel(Messages.get("label.saveAsWIP", "Save as work in progress"));
            checkbox.setToolTip(Messages.get("label.saveAsWIP.information", "If checked, this content will ne be part of publication process"));

            return checkbox;
        }
    }

    // A dedicated class so that a WIP checkbox object can be distinguished from other checkboxes by its class.
    public class CheckBoxWip extends CheckBox {
    }

    public void setCheckedByDefault(boolean checkedByDefault) {
        this.checkedByDefault = checkedByDefault;
    }

    public void setAcademyUrl(String academyUrl) {
        this.academyUrl = academyUrl;
    }

    /**
     * Window that displays WIP details
     */
    private class WorkInProgressWindow extends Window {

        private final Radio turnOff = new Radio();
        private final Radio allContents = new Radio();
        private final Radio selectedLanguages = new Radio();
        private final CheckBoxGroup languages = new CheckBoxGroup();
        private final Html errorLanguages = new Html();
        private final Html helpPanel = new Html();

        public WorkInProgressWindow(final AbstractContentEngine engine) {

            final VerticalPanel vp = new VerticalPanel();
            vp.setSpacing(10);

            final VerticalPanel vpLanguages = new VerticalPanel();
            final LayoutContainer langContainer = new LayoutContainer();


            setSize(500, 300);
            setPlain(true);
            setModal(true);
            setBlinkModal(true);
            setHeadingText(Messages.get("label.wip.title", "Work in progress"));
            addStyleName("wip-window");

            addWindowListener(new WindowListener() {

                @Override
                public void windowShow(WindowEvent we) {
                    super.windowShow(we);
                    // init form with engine values
                    switch (engine.getWipStatus()) {
                        case DISABLED:
                            turnOff.setValue(true);
                            turnOff.fireEvent(Events.OnClick);
                            break;
                        case LANGUAGES:
                            selectedLanguages.setValue(true);
                            selectedLanguages.fireEvent(Events.OnClick);
                            break;
                        case ALL_CONTENT:
                            allContents.setValue(true);
                            allContents.fireEvent(Events.OnClick);
                            break;
                        default:
                            if (checkedByDefault) {
                                allContents.setValue(true);
                                allContents.fireEvent(Events.OnClick);
                            } else {
                                turnOff.setValue(true);
                                turnOff.fireEvent(Events.OnClick);
                            }
                    }
                    // fill languages
                    Set<String> wipLanguages = engine.getWorkInProgressLanguagesSorted();
                    for (Field<?> field : languages.getAll()) {
                        CheckBox language = (CheckBox) field;
                        language.setValue(wipLanguages.contains(language.getValueAttribute()));
                    }
                }
            });

            // remove errors on click
            vpLanguages.addListener(Events.OnChange, new Listener<BaseEvent>() {

                @Override
                public void handleEvent(BaseEvent be) {
                    errorLanguages.hide();
                    vpLanguages.removeStyleName("error-languages");
                }
            });

            Html title = new Html(Messages.get("label.wip.title.sub", "Select what you would like to mark as Work in Progress"));
            title.addStyleName("wip-panel-subtitle");
            vp.add(title);

            allContents.addListener(Events.OnClick, getOnChangeListener(false, Messages.getWithArgs("label.wip.allcontent.helper", "All Content helper text. <a target=\"_blank\" href=\"{0}\">Find out more at The Academy</a>", new String[] {academyUrl})));
            String titleText = Messages.get("label.wip.allcontent", "All Content ( localised & non-localised )");
            allContents.setBoxLabel(titleText);
            allContents.setTitle(titleText);
            allContents.addStyleName("wip-radio-all-content");
            vp.add(allContents);

            selectedLanguages.setBoxLabel(Messages.get("label.wip.localisedcontent", "Localised Content only"));
            selectedLanguages.addListener(Events.OnClick, getOnChangeListener(true, Messages.getWithArgs("label.wip.localisedcontent.helper", "Localised Content helper text. <a target=\"_blank\" href=\"{0}\">Find out more at The Academy</a>", new String[] {academyUrl})));
            vpLanguages.add(selectedLanguages);
            languages.setFieldLabel("Languages");
            languages.setOrientation(Style.Orientation.VERTICAL);
            for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
                CheckBox languageCheck = new CheckBox();
                languageCheck.setBoxLabel(language.getDisplayName());
                languageCheck.setValueAttribute(language.getLanguage());
                languages.add(languageCheck);
            }
            languages.addStyleName("wip-language-list");
            langContainer.add(languages);
            langContainer.setScrollMode(Style.Scroll.AUTO);
            langContainer.setHeight(100);
            vpLanguages.add(langContainer);
            errorLanguages.addStyleName("error-message");
            errorLanguages.setHtml(Messages.get("label.wip.localisedcontent.error", "At least one language must be selected"));
            errorLanguages.hide();
            vpLanguages.add(errorLanguages);
            vp.add(vpLanguages);
            vpLanguages.addStyleName("wip-radio-localised");

            turnOff.setBoxLabel(Messages.get("label.wip.turnoff", "Turn off Work in Progress"));
            turnOff.addListener(Events.OnClick, getOnChangeListener(false, Messages.getWithArgs("label.wip.turnoff.helper", "Turn off helper text. <a target=\"_blank\" href=\"{0}\">Find out more at The Academy</a>",  new String[] {academyUrl})));
            turnOff.addStyleName("wip-radio-off");
            vp.add(turnOff);

            add(vp);

            final Button cancel = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    hide();
                }
            });
            cancel.addStyleName("button-cancel");
            addButton(cancel);

            Button save = new Button(Messages.get("label.save", "Save"), new SelectionListener<ButtonEvent>() {

                @Override
                public void componentSelected(ButtonEvent ce) {
                    // check for languages
                    if (selectedLanguages.getValue() && languages.getValues().size() < 1) {
                        // error
                        languages.markInvalid(Messages.get("label.wip.localisedcontent.error", "At least one language must be selected"));
                        vpLanguages.addStyleName("error-languages");
                        errorLanguages.show();
                        return;
                    }
                    // save wip window data to the engine

                    if (allContents.getValue()) {
                        engine.setWipStatus(GWTJahiaNode.WipStatus.ALL_CONTENT);
                    } else if (selectedLanguages.getValue()) {
                        engine.setWipStatus(GWTJahiaNode.WipStatus.LANGUAGES);
                    } else if (turnOff.getValue()) {
                        engine.setWipStatus(GWTJahiaNode.WipStatus.DISABLED);
                    } else {
                        throw new IllegalArgumentException("unable to set WIP status");
                    }
                    HashSet<String> langs = new HashSet<String>();
                    for (CheckBox language : languages.getValues()) {
                        if (language.getValue()) {
                            langs.add(language.getValueAttribute());
                        }
                    }
                    engine.setWorkInProgressLanguages(langs);
                    engine.updateWipStyle();
                    wipButton.updateButtonTitle();
                    hide();
                }
            });
            save.addStyleName("button-save");
            addButton(save);

            setFocusWidget(getButtonBar().getItem(0));

            wipButton.updateButtonTitle();
            // add help hover
            helpPanel.addStyleName("help-message");
            add(helpPanel);
        }

        private Listener<BaseEvent> getOnChangeListener(final Boolean enableLanguage, final String help) {

            return new Listener<BaseEvent>() {

                @Override
                public void handleEvent(BaseEvent be) {
                    if (enableLanguage) {
                        languages.enable();
                    } else {
                        languages.disable();
                    }
                    helpPanel.setHtml(help);
                }
            };
        }
    }
}

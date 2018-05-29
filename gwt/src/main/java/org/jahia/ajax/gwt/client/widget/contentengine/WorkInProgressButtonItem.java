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

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Radio;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.HashSet;

/**
 * Button Item to handle work in progress in edit engine
 */
public class WorkInProgressButtonItem implements ButtonItem {

    private static final long serialVersionUID = -648440265273432614L;

    private boolean checkedByDefault = false;

    private transient WorkInProgressButton wipButton;

    @Override
    public BoxComponent create(final AbstractContentEngine engine) {

        wipButton = new WorkInProgressButton(engine);

        final Window window = new WorkInProgressWindow(engine);

        wipButton.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                window.show();
            }
        });

        return wipButton;
    }

    public void setCheckedByDefault(boolean checkedByDefault) {
        this.checkedByDefault = checkedByDefault;
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

        public WorkInProgressWindow(final AbstractContentEngine engine) {

            final VerticalPanel vp = new VerticalPanel();
            vp.setSpacing(10);

            final VerticalPanel vpLanguages = new VerticalPanel();
            vpLanguages.setSpacing(10);

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
                            break;
                        case LANGUAGES:
                            selectedLanguages.setValue(true);
                            break;
                        case ALL_CONTENT:
                            allContents.setValue(true);
                            break;
                        default:
                            if (checkedByDefault) {
                                allContents.setValue(true);
                            } else {
                                turnOff.setValue(true);
                            }
                    }
                    // fill languages
                    for (Field<?> field : languages.getAll()) {
                        CheckBox language = (CheckBox) field;
                        language.setValue(engine.getWorkInProgressLanguages().contains(language.getValueAttribute()));
                    }
                    // set languages
                    if (turnOff.getValue() || allContents.getValue()) {
                        languages.disable();
                    } else {
                        languages.enable();
                    }
                }
            });
            // remove errors on click
            vpLanguages.addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    errorLanguages.hide();
                    languages.markInvalid();
                    vpLanguages.removeStyleName("error-languages");
                }
            });
            Html title = new Html(Messages.get("label.wip.title.sub", "Select what you would like to mark as Work in Progress"));
            vp.add(title);

            allContents.addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    languages.disable();
                }
            });
            allContents.setBoxLabel(Messages.get("label.wip.allcontent", "All Content ( localised & non-localised )"));
            allContents.setToolTip(Messages.get("label.wip.allcontent.helper", "All Content helper text. Find out more at The Academy"));
            vp.add(allContents);

            selectedLanguages.setBoxLabel(Messages.get("label.wip.localisedcontent", "Localised Content only"));
            selectedLanguages.setToolTip(Messages.get("label.wip.localisedcontent.helper", "Localised Content helper text. Find out more at The Academy"));
            selectedLanguages.addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    languages.enable();
                }
            });

            if (JahiaGWTParameters.getSiteLanguages().size() > 1) {
                vp.add(selectedLanguages);
                languages.setFieldLabel("Languages");
                for (GWTJahiaLanguage language : JahiaGWTParameters.getSiteLanguages()) {
                    CheckBox languageCheck = new CheckBox();
                    languageCheck.setBoxLabel(language.getDisplayName());
                    languageCheck.setValueAttribute(language.getLanguage());
                    languages.add(languageCheck);
                }
                vpLanguages.add(languages);
                errorLanguages.addStyleName("error-message");
                errorLanguages.setHtml(Messages.get("label.wip.localisedcontent.error", "At least one language must be selected"));
                errorLanguages.hide();
                vpLanguages.add(errorLanguages);
                vp.add(vpLanguages);
            }
            turnOff.setBoxLabel(Messages.get("label.wip.turnoff", "Turn off Work in Progress"));
            turnOff.setToolTip(Messages.get("label.wip.turnoff.helper", "Turn off helper text. Find out more at The Academy"));
            turnOff.addListener(Events.OnClick, new Listener<BaseEvent>() {
                @Override
                public void handleEvent(BaseEvent be) {
                    languages.disable();
                }
            });
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
                    // Save WIP only if necessary
                    engine.setSaveWIP(true);
                    // check for languages
                    if (selectedLanguages.getValue() && languages.getValues().size() < 1) {
                        // error
                        languages.markInvalid(Messages.get("label.wip.localisedcontent.error", "At least one language must be selected"));
                        errorLanguages.show();
                        return;
                    }
                    // save wip window data to the engine

                    if (allContents.getValue()) {
                        engine.setWipStatus(AbstractContentEngine.WipStatus.ALL_CONTENT);
                    } else if (selectedLanguages.getValue()) {
                        engine.setWipStatus(AbstractContentEngine.WipStatus.LANGUAGES);
                    } else if (turnOff.getValue()) {
                        engine.setWipStatus(AbstractContentEngine.WipStatus.DISABLED);
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
                    wipButton.updateButtonTitle();
                    hide();
                }
            });
            save.addStyleName("button-save");
            addButton(save);

            setFocusWidget(getButtonBar().getItem(0));

            wipButton.updateButtonTitle();
        }
    }
}

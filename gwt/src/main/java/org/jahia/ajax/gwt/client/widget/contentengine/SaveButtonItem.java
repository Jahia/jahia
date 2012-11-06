/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.GWTCompositeConstraintViolationException;
import org.jahia.ajax.gwt.client.service.GWTConstraintViolationException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.form.CKEditorField;

import java.util.HashMap;
import java.util.Map;

public abstract class SaveButtonItem implements ButtonItem {

    public Button create(final AbstractContentEngine engine) {
        Button button = new Button(Messages.get("label.save"));
        button.setHeight(BUTTON_HEIGHT);
        button.setIcon(StandardIconsProvider.STANDARD_ICONS.engineButtonOK());
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                save(engine, true);
            }
        });
        return button;
    }

    protected void save(final AbstractContentEngine engine, final boolean closeAfterSave) {
        engine.mask(Messages.get("label.saving", "Saving..."), "x-mask-loading");
        engine.setButtonsEnabled(false);

        if (validateData(engine)) {
            Map<String, String> textForWCAGValidation = null;
            Map<String, CKEditorField> toValidate = null;

            if (engine.getNode() != null && engine.getNode().isWCAGComplianceCheckEnabled() || engine.getNode() == null && engine.getTargetNode().isWCAGComplianceCheckEnabled()) {
                // validation passes, let's get WCAG texts to validate
                toValidate = getFieldsForWCAGValidation(engine);
                textForWCAGValidation = new HashMap<String, String>(toValidate.size());
                for (Map.Entry<String, CKEditorField> fieldEntry : toValidate.entrySet()) {
                    textForWCAGValidation.put(fieldEntry.getKey(), fieldEntry.getValue().getRawValue());
                }
            }
            if (textForWCAGValidation != null && !textForWCAGValidation.isEmpty()) {
                final Map<String, CKEditorField> fieldsForValidation = toValidate;
                // we have texts to validate against WCAG rules
                JahiaContentManagementService.App.getInstance().validateWCAG(textForWCAGValidation, new BaseAsyncCallback<Map<String, WCAGValidationResult>>() {
                    public void onSuccess(Map<String, WCAGValidationResult> result) {
                        boolean wcagOK = true;
                        for (Map.Entry<String, WCAGValidationResult> wcagEntry : result.entrySet()) {
                            if (!wcagEntry.getValue().isEmpty()) {
                                wcagOK = false;
                                CKEditorField fld = fieldsForValidation.get(wcagEntry.getKey());
                                if (fld != null) {
                                    fld.setWcagValidationResult(wcagEntry.getValue());
                                }
                            }
                        }
                        if (wcagOK) {
                            // WCAG checks are OK
                            prepareAndSave(engine, closeAfterSave);
                        } else {
                            validateData(engine);
                        }
                    }

                    @Override
                    public void onApplicationFailure(Throwable caught) {
                        super.onApplicationFailure(caught);
                        // unable to do WCAG check, skipping
                        prepareAndSave(engine, closeAfterSave);
                    }
                });
            } else {
                prepareAndSave(engine, closeAfterSave);
            }
        }
    }

    protected boolean validateData(AbstractContentEngine engine) {
        EngineValidation e = new EngineValidation(engine.getTabs(), engine.getSelectedLanguage(), engine.getChangedI18NProperties());
        EngineValidation.ValidateResult r = e.validateData();

        if (!r.allValid) {
            MessageBox.alert(Messages.get("label.error", "Error"),
                    Messages.get("failure.invalid.constraint.label",
                            "There are some validation errors!"
                                    + " Click on the information icon next to the"
                                    + " highlighted fields, correct the input and save again."),
                    null);
            handleValidationResult(engine, r);
            engine.unmask();
            engine.setButtonsEnabled(true);
            return false;
        } else {
            return true;
        }
    }

    protected void handleValidationResult(AbstractContentEngine engine, EngineValidation.ValidateResult r) {
        if (r.firstErrorLang != null) {
            for (GWTJahiaLanguage jahiaLanguage : engine.getLanguageSwitcher().getStore().getModels()) {
                if (jahiaLanguage.getLanguage().equals(r.firstErrorLang)) {
                    engine.getLanguageSwitcher().setValue(jahiaLanguage);
                    break;
                }
            }
        }
        if (r.firstErrorTab != null && !engine.getTabs().getSelectedItem().equals(r.firstErrorTab)) {
            engine.getTabs().setSelection(r.firstErrorTab);
        }
        if (r.firstErrorField != null) {
            r.firstErrorField.focus();
        }
        if (r.firstErrorTab != null) {
            r.firstErrorTab.layout();
        }
    }

    protected Map<String, CKEditorField> getFieldsForWCAGValidation(AbstractContentEngine engine) {
        Map<String, CKEditorField> fieldsToValidate = new HashMap<String, CKEditorField>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof PropertiesTabItem) {
                Map<String, PropertiesEditor> langPropertiesEditorMap = ((PropertiesTabItem) item).getLangPropertiesEditorMap();
                for (PropertiesEditor pe : langPropertiesEditorMap.values()) {
                    if (pe != null) {
                        for (PropertiesEditor.PropertyAdapterField adapterField : pe.getFieldsMap().values()) {
                            Field<?> field = adapterField.getField();
                            if ((field instanceof CKEditorField) && field.isEnabled() && !field.isReadOnly() && ((FieldSet)adapterField.getParent()).isExpanded()) {
                                CKEditorField ckfield = (CKEditorField) field;
                                if (ckfield.isIgnoreWcagWarnings()) {
                                    continue;
                                }
                                if (ckfield.getItemId() == null) {
                                    ckfield.setItemId("CKEditorField-" + (fieldsToValidate.size() + 1));
                                }
                                String text = ckfield.getRawValue();
                                if (text != null && text.trim().length() > 0) {
                                    fieldsToValidate.put(field.getItemId(), ckfield);
                                }
                            }
                        }
                    }
                }
            }
        }

        return fieldsToValidate;
    }

    protected abstract void prepareAndSave(final AbstractContentEngine engine, boolean closeAfterSave);

    protected void failSave(final AbstractContentEngine engine, Throwable throwable) {
        try {
            throw throwable;
        } catch (final GWTCompositeConstraintViolationException cve) {
            String nodeLevelMessages = "";
            boolean hasFieldErrors = false;
            for (GWTConstraintViolationException violationException : cve.getErrors()) {
                if (violationException.getPropertyName() == null || violationException.getPropertyName().equals("")) {
                    nodeLevelMessages += "<br>" + violationException.getConstraintMessage();
                } else {
                    hasFieldErrors = true;
                }
            }
            final boolean fHasFieldErrors = hasFieldErrors;
            MessageBox.alert(Messages.get("label.error", "Error"),
                    (hasFieldErrors ? Messages.get("failure.invalid.constraint.label",
                            "There are some validation errors!"
                                    + " Click on the information icon next to the"
                                    + " highlighted fields, correct the input and save again.") : "")
                            + nodeLevelMessages,
                    new Listener<MessageBoxEvent>() {
                        public void handleEvent(MessageBoxEvent be) {
                            if (fHasFieldErrors) {
                                EngineValidation e = new EngineValidation(engine.getTabs(), engine.getSelectedLanguage(), engine.getChangedI18NProperties());
                                EngineValidation.ValidateResult r = e.getValidationFromException(cve.getErrors());
                                handleValidationResult(engine, r);
                            }
                        }
                    });
        } catch (Throwable t) {
            String message = throwable.getMessage();
            com.google.gwt.user.client.Window.alert(Messages.get("failure.properties.save", "Properties save failed") + "\n\n"
                    + message);
            Log.error("failed", throwable);
        }
        engine.unmask();
        engine.setButtonsEnabled(true);
    }

}

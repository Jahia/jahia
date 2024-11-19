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
package org.jahia.ajax.gwt.client.widget.content.wizard;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow.ContentWizardCard;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;

/**
 * Wizard card responsible for displaying a form for entering content data.
 *
 * @author Sergiy Shyrkov
 */
public class ContentNameCard extends ContentWizardCard {

    private TextField<String> nameField;



    /**
     * Initialize instnce of this class
     */
    public ContentNameCard() {
        super(Messages
                .get("org.jahia.engines.contentmanager.addContentWizard.nameCard.title", "Content name"),
                Messages.get("org.jahia.engines.contentmanager.addContentWizard.nameCard.text",
                        "Provide a name for the new content:"));

    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#createUI()
     */
    @Override
    public void createUI() {
        FormPanel simple = new FormPanel();
        simple.setHeaderVisible(false);
        simple.setBorders(false);
        LabelField selectedType = new LabelField();
        selectedType.setFieldLabel(Messages.get("org.jahia.engines.contentmanager.addContentWizard.nameCard.nodeType", "Selected type"));
        selectedType.setValue(getWizardData().getNodeType().getLabel() + " ("
                + getWizardData().getNodeType().getName() + ")");
        simple.add(selectedType);

        nameField = new TextField<String>();
        nameField.setFieldLabel(Messages.get(
                "label.user", "Name"));
        nameField.setAllowBlank(false);
        simple.add(nameField);
        add(simple);
        setUiCreated(true);
    }

    @Override
    public boolean isValid() {
        return nameField != null && nameField.getValue().length() > 0;
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#next()
     */
    @Override
    public void next() {
        getWizardData().setNodeName(nameField.getValue());
    }

    @Override
    public void resetUI() {
        super.resetUI();
        if (nameField != null) {
            remove(nameField.getParent());
        }
    }

}

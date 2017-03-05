/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

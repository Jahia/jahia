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

package org.jahia.ajax.gwt.client.widget.content.wizard;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow.ContentWizardCard;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.allen_sauer.gwt.log.client.Log;

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

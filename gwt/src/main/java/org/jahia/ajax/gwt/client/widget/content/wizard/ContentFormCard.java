/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow.ContentWizardCard;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * Wizard card responsible for displaying a form for entering content data.
 * 
 * @author Sergiy Shyrkov
 */
public class ContentFormCard extends ContentWizardCard {

    private PropertiesEditor formEditor;

    /**
     * Initializes an instance of this class.
     */
    public ContentFormCard() {
        super(Messages.get("add_content_wizard_card_form_title",
                "Content add form"), Messages.get(
                "add_content_wizard_card_form_text", "Fill in field values:"));
        setLayout(new FitLayout());
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#createUI()
     */
    @Override
    public void createUI() {
        JahiaContentDefinitionService.App.getInstance().getNodeType(
                getWizardData().getNodeType().getName(),
                new AsyncCallback<GWTJahiaNodeType>() {
                    public void onFailure(Throwable caught) {
                        Log.error("error", caught);
                    }

                    public void onSuccess(GWTJahiaNodeType result) {
                        List<GWTJahiaNodeType> types = new ArrayList<GWTJahiaNodeType>();
                        types.add(result);
                        Map<String, GWTJahiaNodeProperty> defaultValues = new HashMap<String, GWTJahiaNodeProperty>();

                        formEditor = new PropertiesEditor(types, defaultValues,
                                false, true, null, Arrays.asList(
                                        "mix:createdBy", "mix:lastModified",
                                        "mix:created", "jmix:lastPublished",
                                        "jmix:categorized", "jmix:description",
                                        "jnt:jahiacontent"));
                        if (formEditor != null) {
                            setFormPanel(formEditor);
                            layout();
                        } else {
                            add(new Label(Messages.get(
                                    "add_content_wizard_card_form_error_props",
                                    "Unable to load properties panel")));
                        }
                    }

                });
        setUiCreated(true);
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#next()
     */
    @Override
    public void next() {
        JahiaContentManagementService.App.getInstance().createNode(
                getWizardWindow().getParentNode().getPath(),
                getWizardData().getNodeName(),
                getWizardData().getNodeType().getName(),
                formEditor.getProperties(), null,
                new AsyncCallback<GWTJahiaNode>() {
                    public void onFailure(Throwable caught) {
                        Log.error("Error", caught);
                        MessageBox
                                .alert(
                                        Messages
                                                .get(
                                                        "add_content_wizard_card_form_error_title",
                                                        "Error"),
                                        Messages
                                                .get(
                                                        "add_content_wizard_card_form_error_save",
                                                        "Unable to create new content. Cause: ")
                                                + caught.getMessage(), null);
                    }

                    public void onSuccess(GWTJahiaNode result) {
                        if (getWizardWindow().getLinker() != null) {
                            getWizardWindow().getLinker()
                                    .setSelectPathAfterDataUpdate(
                                            result.getPath());
                            getWizardWindow().getLinker().refreshTable();
                        }
                        MessageBox
                                .info(
                                        Messages
                                                .get(
                                                        "add_content_wizard_card_form_success_title",
                                                        "Info"),
                                        Messages
                                                .get(
                                                        "add_content_wizard_card_form_success_save",
                                                        "Content node created successfully: ")
                                                + getWizardData().getNodeName(),
                                        null);
                        getWizardWindow().hide();
                    }
                });
    }

    @Override
    public void resetUI() {
        super.resetUI();
        if (formEditor != null) {
            formEditor.removeAll();
            remove(formEditor);
        }
    }

}
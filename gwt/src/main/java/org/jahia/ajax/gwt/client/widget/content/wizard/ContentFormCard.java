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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow.ContentWizardCard;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.*;

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
        super(Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.title", "Content add form"),
                Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.text", "Fill in field values:"));
        setLayout(new FitLayout());
    }

    /*
     * (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.widget.wizard.WizardCard#createUI()
     */

    @Override
    public void createUI() {
        JahiaContentManagementService.App.getInstance()
                .getNodeType(getWizardData().getNodeType().getName(), new BaseAsyncCallback<GWTJahiaNodeType>() {
                    public void onSuccess(GWTJahiaNodeType result) {
                        List<GWTJahiaNodeType> types = new ArrayList<GWTJahiaNodeType>();
                        types.add(result);
                        Map<String, GWTJahiaNodeProperty> defaultValues = new HashMap<String, GWTJahiaNodeProperty>();

                        formEditor =
                                new PropertiesEditor(types, defaultValues, Arrays.asList(GWTJahiaItemDefinition.CONTENT));
                        formEditor.renderNewFormPanel();
                        setFormPanel(formEditor);
                        layout();
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
        JahiaContentManagementService.App.getInstance()
                .createNode(getWizardWindow().getParentNode().getPath(), getWizardData().getNodeName(),
                        getWizardData().getNodeType().getName(), null, null, formEditor.getProperties(),null,
                        new BaseAsyncCallback<GWTJahiaNode>() {
                            public void onApplicationFailure(Throwable caught) {
                                Log.error("Error", caught);
                                MessageBox.alert(Messages.get("label.error", "Error"),
                                        Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.error.save",
                                                "Unable to create new content. Cause: ") + caught.getMessage(), null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                if (getWizardWindow().getLinker() != null) {
                                    getWizardWindow().getLinker().setSelectPathAfterDataUpdate(Arrays.asList(result.getPath()));
                                    getWizardWindow().getLinker().refresh(Linker.REFRESH_MAIN);
                                }
                                MessageBox.info(Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.success", "Info"),
                                        Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.success.save",
                                                "Content node created successfully: ") + getWizardData().getNodeName(),
                                        null);
                                getWizardWindow().hide();
                                getWizardWindow().getLinker().loaded();
                                getWizardWindow().getLinker().refresh(Linker.REFRESH_ALL);
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
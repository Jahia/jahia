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
                        null, null, true, new BaseAsyncCallback<GWTJahiaNode>() {
                            public void onApplicationFailure(Throwable caught) {
                                Log.error("Error", caught);
                                MessageBox.alert(Messages.get("label.error", "Error"),
                                        Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.error.save",
                                                "Unable to create new content. Cause: ") + caught.getMessage(), null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                if (getWizardWindow().getLinker() != null) {
                                    getWizardWindow().getLinker().setSelectPathAfterDataUpdate(Arrays.asList(result.getPath()));
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_MAIN, true);
                                    getWizardWindow().getLinker().refresh(data);
                                }
                                MessageBox.info(Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.success", "Info"),
                                        Messages.get("org.jahia.engines.contentmanager.addContentWizard.formCard.success.save",
                                                "Content node created successfully: ") + getWizardData().getNodeName(),
                                        null);
                                getWizardWindow().hide();
                                getWizardWindow().getLinker().loaded();
                                Map<String, Object> data = new HashMap<String, Object>();
                                data.put(Linker.REFRESH_ALL, true);
                                getWizardWindow().getLinker().refresh(data);
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
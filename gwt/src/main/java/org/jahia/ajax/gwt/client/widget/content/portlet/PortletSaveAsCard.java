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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content.portlet;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: ktlili
 * Date: 4 d�c. 2008
 * Time: 12:31:50
 */
public class PortletSaveAsCard extends PortletWizardCard {
    private TextField<String> saveAs = new TextField<String>();

    public PortletSaveAsCard() {
        super(Messages.get("label.finish","Finish"), Messages.get("org.jahia.engines.PortletsManager.wizard.saveas.label",""));
    }

    public void createUI() {
        super.createUI();
        FormPanel simple = new FormPanel();
        simple.setFieldWidth(300);
        simple.setLabelWidth(200);
        saveAs.setFieldLabel(Messages.get("org.jahia.engines.PortletsManager.wizard.saveas.label","Save as"));
        saveAs.setAllowBlank(false);
        saveAs.setMinLength(5);
        try {
            saveAs.setValue(getGwtJahiaNewPortletInstance().getGwtJahiaPortletDefinition().getDisplayName());
        } catch (Exception e) {
            Log.error("Error while setting default values");
        }
        simple.add(saveAs);
        setFormPanel(simple);
    }

    @Override
    public boolean isValid() {
        return saveAs.getValue() != null;
    }

    public void next() {
        // on  finish
        String path = "/shared/portlets";
        if (getParentNode() != null) {
            path = getParentNode().getPath();
        }
        getGwtJahiaNewPortletInstance().setInstanceName(saveAs.getValue());

        // save
        JahiaContentManagementService.App.getInstance().createPortletInstance(path, getGwtJahiaNewPortletInstance(), new BaseAsyncCallback<GWTJahiaNode>() {
            public void onSuccess(GWTJahiaNode result) {
                if (getLinker() != null) {
                    getLinker().setSelectPathAfterDataUpdate(Arrays.asList(result.getPath()));
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    getLinker().refresh(data);
                }
                getPortletWizardWindow().onPortletCreated();
                getPortletWizardWindow().hide();
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Error", caught);
                Window.alert(caught.getMessage());
            }
        });
    }
}

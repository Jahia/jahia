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
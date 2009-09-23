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
package org.jahia.ajax.gwt.client.widget.content.portlet;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * User: ktlili
 * Date: 4 d�c. 2008
 * Time: 12:31:50
 */
public class PortletSaveAsCard extends MashupWizardCard {
    private TextField<String> saveAs = new TextField<String>();

    public PortletSaveAsCard() {
        super(Messages.getNotEmptyResource("mw_finish","Finish"), Messages.getNotEmptyResource("mw_finish_description",""));
    }

    public void createUI() {
        super.createUI();        
        FormPanel simple = new FormPanel();
        simple.setFieldWidth(300);
        simple.setLabelWidth(200);
        saveAs.setFieldLabel(Messages.getNotEmptyResource("mw_save_as","Save as"));
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
        String path = "/content/shared/mashups";
        if (getParentNode() != null) {
            path = getParentNode().getPath();
        }
        getGwtJahiaNewPortletInstance().setInstanceName(saveAs.getValue());

        // save
        JahiaContentManagementService.App.getInstance().createPortletInstance(path, getGwtJahiaNewPortletInstance(), new AsyncCallback<GWTJahiaNode>() {
            public void onSuccess(GWTJahiaNode result) {
                if (getLinker() != null) {
                    getLinker().setSelectPathAfterDataUpdate(result.getPath());
                    getLinker().refreshMainComponent();
                }
                getPortletWizardWindow().onPortletCreated();
                getPortletWizardWindow().hide();
            }

            public void onFailure(Throwable caught) {
                Log.error("Error", caught);
                Window.alert(caught.getMessage());
            }
        });
    }
}
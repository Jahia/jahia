/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.node.portlet;

import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
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
        super(Messages.getNotEmptyResource("mw_finish","Finish"));
        setHtmlText(getText());
    }

    public String getText() {
        return Messages.getNotEmptyResource("mw_finish_description","");
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
        JahiaNodeService.App.getInstance().createPortletInstance(path, getGwtJahiaNewPortletInstance(), new AsyncCallback<GWTJahiaNode>() {
            public void onSuccess(GWTJahiaNode result) {
                if (getLinker() != null) {
                    getLinker().setSelectPathAfterDataUpdate(result.getPath());
                    getLinker().refreshTable();
                }
                hide();
                getPortletWizardWindow().onPortletCreated();
            }

            public void onFailure(Throwable caught) {
                Log.error("Error", caught);
                Window.alert(caught.getMessage());
            }
        });
    }
}

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
package org.jahia.ajax.gwt.client.widget.form;

import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 27 f�vr. 2009
 * Time: 16:20:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class FormQuickRSS extends FormQuickMashup {


    public FormQuickRSS(String folderPath) {
        super(folderPath);
        createUI();
    }

    protected void createUI() {
        setBodyBorder(false);
        setFrame(false);
        setAutoHeight(true);
        setHeaderVisible(false);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setStyleAttribute("padding", "4");


        final TextField nameField = new TextField();
        nameField.setName("name");
        nameField.setFieldLabel(Messages.getNotEmptyResource("name", "Name"));
        nameField.setAllowBlank(false);
        nameField.setMaxLength(200);
        add(nameField);

        final TextField urlField = new TextField();
        urlField.setName("url");
        urlField.setFieldLabel(Messages.getNotEmptyResource("rss_url", "Url"));
        urlField.setAllowBlank(false);
        urlField.setMaxLength(200);
        add(urlField);


        // save properties button
        Button saveButton = new Button(Messages.getResource("fm_save"));
        saveButton.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent componentEvent) {
                JahiaNodeService.App.getInstance().createRSSPortletInstance(getFolderPath(),(String) nameField.getValue(), (String) urlField.getValue(), new AsyncCallback<GWTJahiaNode>() {
                    public void onSuccess(GWTJahiaNode gwtJahiaNode) {
                        onMashupCreated();                        
                        if(getParent() instanceof Window){
                            ((Window)getParent()).close();
                        }
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to create rss portlet", throwable);
                        if(getParent() instanceof Window){
                            ((Window)getParent()).close();
                        }
                    }
                });

            }
        });
        addButton(saveButton);

        layout();
    }
}

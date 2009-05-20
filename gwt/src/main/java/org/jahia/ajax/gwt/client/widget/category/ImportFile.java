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
package org.jahia.ajax.gwt.client.widget.category;

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.WorkInProgress;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * User: ktlili
 * Date: 19 sept. 2008
 * Time: 17:26:41
 */
public class ImportFile extends Window {

    public ImportFile(final BrowserLinker linker, String importUrl) {
        super();
        final FormPanel form = new FormPanel();
        form.setAction(importUrl);
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        setModal(true);

        VerticalPanel panel = new VerticalPanel();
        panel.setSpacing(2);
        Label text = new Label("Select the file to import");
        panel.add(text);

        final FileUpload upload = new FileUpload();
        upload.setName("import");
        panel.add(upload);

        HorizontalPanel bPanel = new HorizontalPanel();
        com.extjs.gxt.ui.client.widget.button.Button cancel = new com.extjs.gxt.ui.client.widget.button.Button("Cancel", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide();
            }
        });
        com.extjs.gxt.ui.client.widget.button.Button submit = new com.extjs.gxt.ui.client.widget.button.Button("OK", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                form.submit();
            }
        });
        bPanel.setSpacing(2);
        bPanel.add(submit);
        bPanel.add(cancel);
        panel.add(bPanel);

        form.setWidget(panel);

        form.addFormHandler(new FormHandler() {
            public void onSubmit(FormSubmitEvent e) {
                if (upload.getFilename() == null || upload.getFilename().equals("")) {
                    com.google.gwt.user.client.Window.alert("Please select a file");
                    e.setCancelled(true);
                } else {
                    WorkInProgress.show();
                }
            }

            public void onSubmitComplete(FormSubmitCompleteEvent event) {
                hide();
                WorkInProgress.hide();
                linker.refreshTable();
            }

        });

        add(form);
        show();
    }

}

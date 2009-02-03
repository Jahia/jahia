/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.categories.client.components;

import org.jahia.ajax.gwt.commons.client.ui.WorkInProgress;
import org.jahia.ajax.gwt.engines.categories.client.CategoriesManagerEntryPoint;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ProgressBar;
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

    public ImportFile(final BrowserLinker linker) {
        super();
        final FormPanel form = new FormPanel();
        form.setAction(CategoriesManagerEntryPoint.getImportActionURL());
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

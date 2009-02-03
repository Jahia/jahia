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

package org.jahia.ajax.gwt.filemanagement.client.ui;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.config.client.JahiaGWTParameters;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.util.Resources;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class FileUploader extends Window {
    private int fieldCount = 0 ;
    private FormPanel form ;

    public FileUploader(final BrowserLinker linker, final GWTJahiaNode location) {
        super() ;
        setHeading(Resources.getResource("fm_uploadFiles"));
        setSize(500, 200);
        setResizable(false);
        
        ButtonBar buttons = new ButtonBar() ;
        final ProgressBar bar = new ProgressBar() ;
        bar.setWidth(200);
        form = new FormPanel() ;
        String entryPoint = JahiaGWTParameters.getServiceEntryPoint() ;
        if (entryPoint == null) {
            entryPoint = "/gwt/" ;
        }
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setAction(entryPoint + "fileupload"); // should do
        form.setEncoding(FormPanel.Encoding.MULTIPART);
        form.setMethod(FormPanel.Method.POST);

        form.setLabelWidth(200);

        setModal(true);

        Hidden dest = new Hidden() ;
        dest.setName("uploadLocation") ;

        CheckBox unzip = new CheckBox() ;
        unzip.setFieldLabel(Resources.getResource("fm_autoUnzip"));
        unzip.setName("unzip");

        String parentPath = location.getPath() ;
        if (location.isFile().booleanValue()) {
            int index = parentPath.lastIndexOf("/") ;
            if (index > 0) {
                parentPath = parentPath.substring(0, index) ;
            }
        }
        dest.setValue(parentPath);

        form.add(dest);
        form.add(unzip);

        ToolBar toolBar = new ToolBar() ;
        TextToolItem add = new TextToolItem(Resources.getResource("fm_addFile")) ;
        add.setIconStyle("fm-addFile");
        add.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                addUploadField();
            }
        }) ;
        toolBar.add(add);
        bar.setVisible(false);
        toolBar.add(new FillToolItem()) ;
        toolBar.add(new AdapterToolItem(bar)) ;

        Button cancel = new Button(Resources.getResource("fm_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Resources.getResource("fm_ok"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                form.submit();
            }
        }) ;

        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        setTopComponent(toolBar);

        final FileUpload upload = new FileUpload() ;
        upload.setWidth("430px");
        DOM.setElementAttribute(upload.getElement(), "size", "53");
        upload.setName("uploadedFile" + fieldCount++);
        upload.addStyleName("fm-bottom-margin");
        form.add(upload) ;

        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.setVisible(true);
                bar.auto() ;
            }
        });
        form.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.reset() ;
                hide();
                linker.setSelectPathAfterDataUpdate(location.getPath()+"/"+upload.getFilename());
                linker.refreshTable();
                String result = formEvent.resultHtml ;
                if (!result.contains("OK")) {
                    com.google.gwt.user.client.Window.alert(new HTML(result).getText());
                }
            }
        });

        add(form);
        setScrollMode(Style.Scroll.AUTO);
        show();
    }

    private void addUploadField() {
        FileUpload upload = new FileUpload() ;
        upload.setWidth("430px");
        DOM.setElementAttribute(upload.getElement(), "size", "53");
        upload.setName("uploadedFile" + fieldCount++);
        upload.addStyleName("fm-bottom-margin");
        form.add(upload) ;
        form.layout() ;
    }

}

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
package org.jahia.ajax.gwt.client.widget.node;

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
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;

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
        setHeading(Messages.getResource("fm_uploadFiles"));
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

        final CheckBox unzip = new CheckBox() ;
        unzip.setFieldLabel(Messages.getResource("fm_autoUnzip"));
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
        TextToolItem add = new TextToolItem(Messages.getResource("fm_addFile")) ;
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

        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Messages.getResource("fm_ok"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                try {
                    form.submit();
                } catch (Exception e) {
                    bar.reset() ;
                    bar.setVisible(false);
                    com.google.gwt.user.client.Window.alert(Messages.getResource("fm_checkUploads")) ;
                }
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
                if (unzip.getValue().booleanValue()) {
                    linker.refreshAll();
                } else {
                    linker.refreshTable();
                }
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

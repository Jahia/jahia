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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class FileUploader extends Window {
    private int fieldCount = 0;
    private FormPanel form;

    public FileUploader(final BrowserLinker linker, final GWTJahiaNode location) {
        super();
        setHeading(Messages.getResource("fm_uploadFiles"));
        setSize(500, 200);
        setResizable(false);

        ButtonBar buttons = new ButtonBar();
        final ProgressBar bar = new ProgressBar();
        bar.setWidth(200);
        form = new FormPanel();
        String entryPoint = JahiaGWTParameters.getServiceEntryPoint();
        if (entryPoint == null) {
            entryPoint = "/gwt/";
        }
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setBodyBorder(false);
        form.setAction(entryPoint + "fileupload"); // should do
        form.setEncoding(FormPanel.Encoding.MULTIPART);
        form.setMethod(FormPanel.Method.POST);

        form.setLabelWidth(200);

        setModal(true);

        // upload location
        Hidden dest = new Hidden();
        dest.setName("uploadLocation");


        // unzip parameter
        final CheckBox unzip = new CheckBox();
        unzip.setFieldLabel(Messages.getResource("fm_autoUnzip"));
        unzip.setName("unzip");

        String parentPath = location.getPath();
        if (location.isFile().booleanValue()) {
            int index = parentPath.lastIndexOf("/");
            if (index > 0) {
                parentPath = parentPath.substring(0, index);
            }
        }
        dest.setValue(parentPath);

        form.add(dest);
        form.add(unzip);

        final ToolBar toolBar = new ToolBar();
        Button add = new Button(Messages.getResource("fm_addFile"));
        add.setIconStyle("fm-addFile");
        add.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                addUploadField();
            }
        });
        toolBar.add(add);
        bar.setVisible(false);
        toolBar.add(new FillToolItem());
        toolBar.add(bar);

        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        final Button submit = new Button(Messages.getResource("fm_ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                try {
                    form.submit();
                } catch (Exception e) {
                    bar.reset();
                    bar.setVisible(false);
                    com.google.gwt.user.client.Window.alert(Messages.getResource("fm_checkUploads"));
                }
            }
        });

        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setTopComponent(buttons);

        setTopComponent(toolBar);

        final FileUpload upload = new FileUpload();
        upload.setWidth("430px");
        DOM.setElementAttribute(upload.getElement(), "size", "53");
        upload.setName("uploadedFile" + fieldCount++);
        upload.addStyleName("fm-bottom-margin");
        form.add(upload);

        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.setVisible(true);
                bar.auto();


            }
        });
        form.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.reset();
                linker.setSelectPathAfterDataUpdate(location.getPath() + "/" + upload.getFilename());

                String result = formEvent.getResultHtml();
           
                toolBar.removeAll();
                removeAll();
                String[] results = result.split("\n");

                final List<Field[]> exists = new ArrayList<Field[]>();

                for (int i = 0; i < results.length; i++) {
                    String s = new HTML(results[i]).getText();
                    if (s.startsWith("OK:")) {

                    } else if (s.startsWith("EXISTS:")) {
                        int i1 = s.indexOf(' ');
                        int i2 = s.indexOf(' ', i1 + 1);
                        int i3 = s.indexOf(' ', i2 + 1);
                        final String key = s.substring(i1 + 1, i2);
                        final String tmp = s.substring(i2 + 1, i3);
                        final String name = s.substring(i3 + 1);

                        addExistingToForm(exists, key, tmp, name);
                    }
                }
                if (!exists.isEmpty()) {
                    submit.removeAllListeners();
                    submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        public void componentSelected(ButtonEvent event) {
                            submit.setEnabled(false);
                            final List<Field[]> list = new ArrayList<Field[]>(exists);
                            final List<Field[]> list2 = new ArrayList<Field[]>(exists);
                            exists.clear();
                            removeAll();
                            for (final Field[] exist : list) {
                                final String tmpName = (String) exist[0].getValue();
                                // selected index correspond to the action: ie. 3=versioning
                                final int operation = ((SimpleComboBox) exist[1]).getSelectedIndex();
                                final String key = exist[1].getName();
                                final String newName = (String) exist[2].getValue();
                                JahiaContentManagementService.App.getInstance().uploadedFile(location.getPath(), tmpName, operation, newName, new AsyncCallback() {
                                    public void onFailure(Throwable caught) {
                                        addExistingToForm(exists, key, tmpName, newName);
                                        end(exist);
                                    }

                                    public void onSuccess(Object result) {
                                        end(exist);
                                    }

                                    private void end(Field[] exist) {
                                        list2.remove(exist);
                                        if (list2.isEmpty()) {
                                            if (exists.isEmpty()) {
                                                endUpload(unzip, linker);
                                            } else {
                                                submit.setEnabled(true);
                                                layout();
                                            }
                                        }
                                    }


                                });
                            }
                        }
                    });

                    layout();
                } else {
                    endUpload(unzip, linker);
                }
            }
        });

        add(form);
        setScrollMode(Style.Scroll.AUTO);
        show();
    }

    private void endUpload(CheckBox unzip, BrowserLinker linker) {
        if (unzip.getValue().booleanValue()) {
            linker.refreshAll();
        } else {
            linker.refreshTable();
        }

        hide();
    }

    private void addExistingToForm(List<Field[]> exists, String key, String tmp, final String name) {
        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel("rename");
        textField.setName(key + "_name");
        textField.setValue(name);

        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName(key + "_tmp");
        hiddenField.setValue(tmp);

        // warning, the index of the option is important. Indeed it corresponds to their value.
        final SimpleComboBox<String> choose = new SimpleComboBox<String>();
        choose.setEditable(false);
        choose.setName(key);
        // 0 = rename
        choose.add(Messages.getResource("fm_rename"));
        // 1= rename-to
        choose.add(Messages.getResource("fm_rename") + " auto");
        // 2 = overwrite
        choose.add(Messages.getResource("fm_confOverwrite"));
        // 4 = add new version
        choose.add(Messages.getNotEmptyResource("fm_add_new_version","Add a new version"));
        choose.setHideLabel(true);
        choose.setValue(choose.getStore().getAt(1));
        choose.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent>() {
            public void handleEvent(SelectionChangedEvent event) {
                if (choose.getValue().getValue().equals("Rename")) {
                    textField.setValue(name);
                    textField.enable();
                } else {
                    textField.setValue(name);
                    textField.disable();
                }
            }
        });

        HorizontalPanel p = new HorizontalPanel();
        final Label w = new Label(Messages.getResource("fm_alreadyExists"));
        w.setStyleName("x-form-field");
        p.add(w);
        p.add(choose);
        p.add(textField);
        add(p);
        exists.add(new Field[]{hiddenField, choose, textField});
    }

    private void addUploadField() {
        FileUpload upload = new FileUpload();
        upload.setWidth("430px");
        DOM.setElementAttribute(upload.getElement(), "size", "53");
        upload.setName("uploadedFile" + fieldCount++);
        upload.addStyleName("fm-bottom-margin");
        form.add(upload);
        form.layout();
    }


}

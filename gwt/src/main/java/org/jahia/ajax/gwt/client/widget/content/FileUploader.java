/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * File upload window.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class FileUploader extends Window {
    private int fieldCount = 0;
    private FormPanel form;
    private List<UploadPanel> uploads = new LinkedList<UploadPanel>();
    
    private class UploadPanel extends HorizontalPanel {

        private Button remove;
        private FileUpload upload;
        
        public UploadPanel() {
            super();
            final UploadPanel panel = this;
            remove = new Button("", new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                    if (uploads.size() > 1) {
                        form.remove(panel);
                        uploads.remove(panel);
                        for (UploadPanel p : uploads) {
                            p.checkVisibility();
                        }
                    }
                }
            });
            remove.setIcon(StandardIconsProvider.STANDARD_ICONS.minusRound());
            remove.setToolTip(Messages.get("label.remove"));
            if (uploads.size() == 0) {
                remove.setVisible(false);
            }
            
            add(remove);
            
            upload = new FileUpload();
            upload.addChangeHandler(new ChangeHandler() {
                private boolean done;
                public void onChange(ChangeEvent event) {
                   if ((!done || uploads.size() == 1) && upload.getFilename() != null && upload.getFilename().length() > 0) {
                       addUploadField();
                       done = true;
                   }
                }
            });
            upload.setWidth("430px");
            DOM.setElementAttribute(upload.getElement(), "size", "53");
            upload.setName("uploadedFile" + fieldCount++);
            upload.addStyleName("fm-bottom-margin");
            
            add(upload);
            
            uploads.add(this);
        }

        public FileUpload getUpload() {
            return upload;
        }
        
        public void checkVisibility() {
            remove.setVisible(uploads.size() > 1);
        }
        
    }

    public FileUploader(final Linker linker, final GWTJahiaNode location) {
        super();
        setHeading(Messages.get("uploadFile.label"));
        setSize(500, 250);
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
        form.setAction(entryPoint + "fileupload");
        form.setEncoding(FormPanel.Encoding.MULTIPART);
        form.setMethod(FormPanel.Method.POST);

        form.setLabelWidth(200);

        setModal(true);

        // upload location
        Hidden dest = new Hidden();
        dest.setName("uploadLocation");


        // unzip parameter
        final CheckBox unzip = new CheckBox();
        unzip.setFieldLabel(Messages.get("autoUnzip.label"));
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

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        final Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                try {
                    form.submit();
                } catch (Exception e) {
                    bar.reset();
                    bar.setVisible(false);
                    com.google.gwt.user.client.Window.alert(Messages.get("checkUploads.label"));
                }
            }
        });

        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        final UploadPanel p = new UploadPanel();
        form.add(p);

        form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.setVisible(true);
                bar.auto();


            }
        });
        form.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                bar.reset();
                String filename = p.getUpload().getFilename();
                int beginIndex = filename.lastIndexOf("/");
                if(beginIndex>0)
                filename = filename.substring(beginIndex);
                beginIndex = filename.lastIndexOf("\\");
                if(beginIndex>0)
                filename = filename.substring(beginIndex+1).replaceAll("\\\\","");
                linker.setSelectPathAfterDataUpdate(Arrays.asList(location.getPath() + "/" + filename));

                String result = formEvent.getResultHtml();
           
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
                            List<String[]> uploadeds = new ArrayList<String[]>();
                            for (final Field[] exist : list) {
                                final String tmpName = (String) exist[0].getValue();
                                // selected index correspond to the action: ie. 3=versioning
                                final int operation = ((SimpleComboBox) exist[1]).getSelectedIndex();
                                final String key = exist[1].getName();
                                final String newName = (String) exist[2].getValue();
                                uploadeds.add(new String[] { location.getPath(), tmpName, Integer.toString(operation), newName });
                            }
                            JahiaContentManagementService.App.getInstance().uploadedFile(uploadeds, new BaseAsyncCallback() {
                                public void onSuccess(Object result) {
                                    endUpload(unzip, linker);
                                }
                            });
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

    private void endUpload(CheckBox unzip, Linker linker) {
        if (unzip.getValue().booleanValue()) {
            linker.refresh(Linker.REFRESH_ALL);
        } else {
            linker.refresh(Linker.REFRESH_MAIN + Linker.REFRESH_FOLDERS);
        }

        hide();
    }

    private void addExistingToForm(List<Field[]> exists, String key, String tmp, final String name) {
        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel("rename");
        textField.setName(key + "_name");
        textField.setValue(name);
        textField.setEnabled(false);

        final HiddenField<String> hiddenField = new HiddenField<String>();
        hiddenField.setName(key + "_tmp");
        hiddenField.setValue(tmp);

        // warning, the index of the option is important. Indeed it corresponds to their value.
        final SimpleComboBox<String> choose = new SimpleComboBox<String>();
        choose.setEditable(false);
        choose.setName(key);
        choose.setTriggerAction(TriggerAction.ALL);
        choose.setForceSelection(true);
        // 0 = rename
        choose.add(Messages.get("label.rename", "Rename"));
        // 1= rename-to
        choose.add(Messages.get("label.rename", "Rename") + " auto");
        // 2 = overwrite
        // choose.add(Messages.get("confirm.overwrite.label", "Overwrite"));
        // 4 = add new version
        choose.add(Messages.get("confirm.addNewVersion.label","Add a new version"));
        choose.setHideLabel(true);
        choose.setValue(choose.getStore().getAt(1));
        choose.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent>() {
            public void handleEvent(SelectionChangedEvent event) {
                if (choose.getValue().getValue().equals(Messages.get("label.rename", "Rename"))) {
                    textField.setValue(name);
                    textField.enable();
                } else {
                    textField.setValue(name);
                    textField.disable();
                }
            }
        });

        HorizontalPanel p = new HorizontalPanel();
        final Label w = new Label(Messages.get("alreadyExists.label"));
        w.setStyleName("x-form-field");
        p.add(w);
        p.add(choose);
        p.add(textField);
        add(p);
        exists.add(new Field[]{hiddenField, choose, textField});
    }

    private void addUploadField() {
        form.add(new UploadPanel());
        for (UploadPanel p : uploads) {
            p.checkVisibility();
        }
        form.layout();
    }


}

/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * File upload window.
 *
 * @author rfelden
 */
public class FileUploader extends Window {
    private enum UploadOption {
        AUTO(1),
        RENAME(0),
        VERSION(2);
        private final int value;

        UploadOption(int value){

            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    private final UploadOption uploadOption;
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
            remove.addStyleName("button-remove");

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
                        if (filenameHasInvalidCharacters(upload.getFilename())) {
                            MessageBox.alert(Messages.get("label.error"), Messages.getWithArgs("failure.upload.invalid.filename", "", new String[]{upload.getFilename()}), null);
                            done = false;
                            upload.getElement().setPropertyString("value", "");
                        } else {
                            addUploadField();
                            done = true;
                        }
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

    public FileUploader(final Linker linker, final GWTJahiaNode location, String defaultUploadOption) {
        super();
        addStyleName("file-uploader");
        if ("rename".equalsIgnoreCase(defaultUploadOption)) {
            uploadOption = UploadOption.RENAME;
        } else if ("version".equalsIgnoreCase(defaultUploadOption)) {
            uploadOption = UploadOption.VERSION;
        } else {
            uploadOption = UploadOption.AUTO;
        }
        setHeadingHtml(Messages.get("uploadFile.label"));
        setSize(500, 500);
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

        form.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent ce) {
                String r = ce.getResultHtml();
                if (r == null) {
                    return;
                }
                if (r.contains("UPLOAD-SIZE-ISSUE:")) {
                    MessageBox.alert(Messages.get("label.error"),
                            new DirectionalTextHelper((new HTML(r.replace("UPLOAD-SIZE-ISSUE:", ""))).getElement(),
                                    true).getTextOrHtml(false), null);
                } else if (r.contains("UPLOAD-ISSUE:")) {
                    unmask();
                    final Dialog dl = new Dialog();
                    dl.setModal(true);
                    dl.setHeadingHtml(Messages.get("label.error"));
                    dl.setHideOnButtonClick(true);
                    dl.setLayout(new FlowLayout());
                    dl.setWidth(300);
                    dl.setScrollMode(Style.Scroll.NONE);
                    dl.add(new HTML(new DirectionalTextHelper((new HTML(r.replace("UPLOAD-ISSUE:", ""))).getElement(),
                            true).getTextOrHtml(false)));
                    dl.setHeight(150);
                    dl.show();
                }
            }
        });
        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });

        cancel.addStyleName("button-cancel");
        final Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                try {
                    mask(Messages.get("message.uploading", "Uploading..."), "x-mask-loading");
                    form.submit();
                } catch (Exception e) {
                    unmask();
                    bar.reset();
                    bar.setVisible(false);
                    com.google.gwt.user.client.Window.alert(Messages.get("checkUploads.label"));
                }
            }
        });
        submit.addStyleName("button-ok");
        buttons.add(cancel);
        buttons.add(submit);
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
                String selectFileAfterDataUpdate = null;
                String result = formEvent.getResultHtml();

                removeAll();
                String[] results = result.split("\n");

                final List<Field[]> exists = new ArrayList<Field[]>();

                for (int i = 0; i < results.length; i++) {
                    String s = new HTML(results[i]).getText();
                    if (s.startsWith("OK:")) {
                        if (selectFileAfterDataUpdate == null) {
                            selectFileAfterDataUpdate = URL.decode(s.substring("OK: ".length()));
                        }
                    } else if (s.startsWith("EXISTS:")) {
                        int i1 = s.indexOf(' ');
                        int i2 = s.indexOf(' ', i1 + 1);
                        int i3 = s.indexOf(' ', i2 + 1);
                        final String key = URL.decode(s.substring(i1 + 1, i2));
                        final String tmp = URL.decode(s.substring(i2 + 1, i3));
                        final String name = URL.decode(s.substring(i3 + 1));

                        addExistingToForm(exists, key, tmp, name, submit);
                    }
                }
                if (!exists.isEmpty()) {
                    submit.removeAllListeners();
                    submit.addSelectionListener(new SelectionListener<ButtonEvent>() {
                        public void componentSelected(ButtonEvent event) {
                            mask(Messages.get("message.uploading", "Uploading..."), "x-mask-loading");
                            submit.setEnabled(false);
                            final List<Field[]> list = new ArrayList<Field[]>(exists);
                            exists.clear();
                            removeAll();
                            List<String[]> uploadeds = new ArrayList<String[]>();
                            for (final Field[] exist : list) {
                                final String tmpName = (String) exist[0].getValue();
                                // selected index correspond to the action: ie. 3=versioning
                                final int operation = ((SimpleComboBox) exist[1]).getSelectedIndex();
                                final String newName = (String) exist[2].getValue();
                                uploadeds.add(new String[] { location.getPath(), tmpName, Integer.toString(operation), newName });
                            }
                            JahiaContentManagementService.App.getInstance().uploadedFile(uploadeds, new BaseAsyncCallback() {
                                public void onSuccess(Object result) {
                                    endUpload(unzip, linker);
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    super.onFailure(caught);
                                    MessageBox.alert(Messages.get("label.error", "error"), caught.getLocalizedMessage(), null);
                                    Map<String, Object> data = new HashMap<String, Object>();
                                    data.put(Linker.REFRESH_ALL, true);
                                    linker.refresh(data);
                                    unmask();
                                    hide();
                                }
                            });
                        }
                    });

                    layout();
                } else {
                    if (selectFileAfterDataUpdate != null) {
                        linker.setSelectPathAfterDataUpdate(Arrays.asList(location.getPath() + "/" + selectFileAfterDataUpdate));
                    }
                    endUpload(unzip, linker);
                }
            }
        });

        add(form);
        setScrollMode(Style.Scroll.AUTO);
        show();
    }

    private void endUpload(CheckBox unzip, Linker linker) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (unzip.getValue().booleanValue()) {
            data.put(Linker.REFRESH_ALL, true);
        } else {
            data.put(Linker.REFRESH_MAIN, true);
            data.put("event", "fileUploaded");
        }
        linker.refresh(data);
        unmask();
        hide();
    }

    private void addExistingToForm(final List<Field[]> exists, String key, String tmp, final String name, final Button submitButton) {
        unmask();
        final TextField<String> textField = new TextField<String>();
        textField.setFieldLabel("rename");
        textField.setName(key + "_name");
        textField.setValue(name);
        textField.setEnabled(uploadOption==UploadOption.RENAME);
        textField.sinkEvents(Events.Change.getEventCode() | Events.OnKeyUp.getEventCode());

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
        final String labelRename = Messages.get("label.rename", "Rename");
        choose.add(labelRename);
        // 1= auto-rename
        choose.add(labelRename + " auto");
        // 2 = overwrite
        // choose.add(Messages.get("confirm.overwrite.label", "Overwrite"));
        // 4 = add new version
        choose.add(Messages.get("confirm.addNewVersion.label","Add a new version"));
        choose.setHideLabel(true);
        choose.setValue(choose.getStore().getAt(uploadOption.getValue()));

        @SuppressWarnings("rawtypes")
        final Listener textListener = new Listener<ComponentEvent>() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleEvent(ComponentEvent be) {
                boolean canEnableSubmit = true;
                for (Field[] fld : exists) {
                    if (((SimpleComboBox<String>)fld[1]).getValue().getValue().equals(labelRename) && !fld[2].isDirty()) {
                        canEnableSubmit = false;
                        break;
                    }
                }
                submitButton.setEnabled(canEnableSubmit);
            }
        };

        choose.addListener(Events.SelectionChange, new Listener<SelectionChangedEvent>() {
            public void handleEvent(SelectionChangedEvent event) {
                if (choose.getValue().getValue().equals(labelRename)) {
                    textField.setValue(name);
                    textField.enable();
                    textField.addListener(Events.Change, textListener);
                    textField.addListener(Events.OnKeyUp, textListener);
                    submitButton.disable();
                } else {
                    textField.setValue(name);
                    textField.disable();
                    textField.removeListener(Events.Change, textListener);
                    textField.removeListener(Events.OnKeyUp, textListener);
                    submitButton.enable();
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

    public static boolean filenameHasInvalidCharacters(String fileName) {
        // fix for browsers that have filename starting by C:\fakepath\
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        } else if (fileName.contains("\\")) {
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
        }
        return fileName.matches(".*[\\\\/:*?\\\"<>|]+.*");
    }


}

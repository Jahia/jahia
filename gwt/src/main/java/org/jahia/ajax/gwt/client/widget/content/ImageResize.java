/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.FormData;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Widget defining a new image dimensions and saving a copy of it.
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:07:20 PM
 */
public class ImageResize extends Window {

    private Linker linker;
    private ComboBox<GWTJahiaValueDisplayBean> predefinedSizesBox;
    private boolean autoName = true;

    public ImageResize(final Linker linker, final GWTJahiaNode n, List<Integer[]> predefinedSizes) {
        super() ;
        addStyleName("image-resize");
        this.linker = linker ;

        final int w = Integer.parseInt((String) n.get("j:width"));
        final int h = Integer.parseInt((String) n.get("j:height"));

        setHeadingHtml(Messages.get("label.resize"));
        setSize(500, 250);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setFieldWidth(350);
        setModal(true);

        FormData layoutData = new FormData(100, 0);
        final NumberField wf = new NumberField();
        final NumberField hf = new NumberField();
        final CheckBox keepRatio = new CheckBox();
        final TextField<String> newname = new TextField<String>();

        if (predefinedSizes != null && !predefinedSizes.isEmpty()) {
            List<GWTJahiaValueDisplayBean> sizes = new ArrayList<GWTJahiaValueDisplayBean>();
            for (Integer[] d : predefinedSizes) {
                    String label = d[0] + " x " + d[1];
                    GWTJahiaValueDisplayBean v = new GWTJahiaValueDisplayBean(label, label);
                    v.set("width", d[0]);
                    v.set("height", d[1]);
                    sizes.add(v);
            }
            if (!sizes.isEmpty()) {
                predefinedSizesBox = new ComboBox<GWTJahiaValueDisplayBean>();
                predefinedSizesBox.setDisplayField("display");
                predefinedSizesBox.setStore(new ListStore<GWTJahiaValueDisplayBean>());
                predefinedSizesBox.setForceSelection(true);
                predefinedSizesBox.setTriggerAction(ComboBox.TriggerAction.ALL);
                predefinedSizesBox.setDeferHeight(true);
                predefinedSizesBox.getStore().add(sizes);
                predefinedSizesBox.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                        GWTJahiaValueDisplayBean v = se.getSelectedItem();
                        wf.setValue((Integer) v.get("width"));
                        if (!keepRatio.getValue()) {
                            hf.setValue((Integer) v.get("height"));
                        } else {
                            hf.setValue(h * wf.getValue().intValue() / w);
                        }
                        if (autoName) setAutoName(newname, wf.getValue().intValue(), hf.getValue().intValue());
                    }
                });
                predefinedSizesBox.setEmptyText(Messages.get("selectPredefinedSize.label", "select a predefined size"));
                predefinedSizesBox.setFieldLabel(Messages.get("label.size","Size"));
                form.add(predefinedSizesBox, new ColumnData(200));
            } else {
            }
        }

        wf.setName("width");
        wf.setValue(new Integer(w));
        wf.setFieldLabel(Messages.get("width.label"));
        form.add(wf, layoutData);

        hf.setName("height");
        hf.setValue(new Integer(h));
        hf.setFieldLabel(Messages.get("height.label"));
        form.add(hf, layoutData);

        keepRatio.setName("ratio");
        keepRatio.setValue(true);
        keepRatio.setFieldLabel(Messages.get("ratio.label"));
        form.add(keepRatio, new FormData(20, 0));

        newname.setName("newname");
        newname.setWidth(350);
        int extIndex = n.getName().lastIndexOf(".") ;
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex) ;
            newname.setValue(n.getName().replaceAll(dotExt+"+$", "-resize" + w + "x" + h + dotExt));
        } else {
            newname.setValue(n.getName() + "-resize" + w + "x" + h);
        }
        newname.setFieldLabel(Messages.get("label.rename"));

        newname.addListener(Events.Change, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent be) {
                autoName = false;
            }
        });

        form.add(newname);

        hf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) wf.setValue(w * hf.getValue().intValue() / h);
                if (autoName) setAutoName(newname, wf.getValue().intValue(), hf.getValue().intValue());
            }
        });
        wf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(h * wf.getValue().intValue() / w);
                if (autoName) setAutoName(newname, wf.getValue().intValue(), hf.getValue().intValue());
            }
        });

        keepRatio.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(h * wf.getValue().intValue() / w);
            }
        });

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        cancel.addStyleName("button-cancel");
        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                resizeImage(n.getPath(), newname.getValue(), wf.getValue().intValue(), hf.getValue().intValue(), false) ;
            }
        }) ;
        submit.addStyleName("button-submit");
        buttons.add(cancel) ;
        buttons.add(submit) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(form);
    }


    protected void setAutoName(TextField<String> newname, int w, int h) {
        String v = newname.getValue();
        int dp = v.lastIndexOf(".");
        newname.setFireChangeEventOnSetValue(false);
        newname.setValue(v.substring(0, v.lastIndexOf("-resize")) + "-resize" + w + "x" + h
                + (dp != -1 ? v.substring(dp, v.length()) : ""));
        newname.setFireChangeEventOnSetValue(true);
    }

    private void resizeImage(final String path, final String targetName, final int width, final int height, final boolean force) {
         JahiaContentManagementService.App.getInstance().resizeImage(path, targetName, width, height, force, new BaseAsyncCallback<Object>() {
             public void onApplicationFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                     if (com.google.gwt.user.client.Window.confirm(Messages.get("alreadyExists.label") + "\n" + Messages.get("confirm.overwrite.label"))) {
                         resizeImage(path, targetName, width, height, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.resize.label") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.get("failure.resize.label"), throwable);
                }
             }

             public void onSuccess(Object result) {
                 hide();
                 Map<String, Object> data = new HashMap<String, Object>();
                 data.put(Linker.REFRESH_MAIN, true);
                 linker.refresh(data);
             }
         });
    }
}

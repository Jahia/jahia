/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.ui.Image;

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
 * Widget to crop image and save it by using the jQuery-based Jcrop library.
 */
public class ImageCrop extends Window {

    private Linker linker;

    private boolean autoName = true;

    private TextField<String> newname;

    private ComboBox<GWTJahiaValueDisplayBean> predefinedSizesBox;

    private NumberField width;

    private NumberField height;

    private NumberField top;

    private NumberField left;
    
    public ImageCrop(final Linker linker, final GWTJahiaNode n, List<Integer[]> predefinedSizes) {
        super();
        setLayout(new FitLayout());
        setSize(712, 550);

        this.linker = linker;
        setHeading(Messages.get("label.crop"));
        setId("JahiaGxtImageCrop");

        FormData formData = new FormData("100%");
        FormPanel form = new FormPanel();
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        form.setLabelWidth(70);

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());
        LayoutContainer lcName = new LayoutContainer();
        lcName.setStyleAttribute("paddingRight", "10px");
        lcName.setLayout(new FormLayout(LabelAlign.LEFT));

        newname = new TextField<String>();
        newname.setName("newname");
        newname.setId("newname");
        newname.setFieldLabel(Messages.get("newName.label", "New name"));
        int extIndex = n.getName().lastIndexOf(".");
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex);
            newname.setValue(n.getName().replaceAll(dotExt, "-crop" + dotExt));
        } else {
            newname.setValue(n.getName() + "-crop");
        }

        newname.addListener(Events.Change, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent be) {
                autoName = false;
            }
        });

        lcName.add(newname, formData);

        LayoutContainer lcWidth = new LayoutContainer();
        lcWidth.setStyleAttribute("paddingRight", "10px");
        FormLayout formLayout = new FormLayout(LabelAlign.RIGHT);
        formLayout.setLabelWidth(40);
        lcWidth.setLayout(formLayout);
        form.add(lcName, new ColumnData(.6));


        width = new NumberField();
        width.setPropertyEditorType(Integer.class);
        height = new NumberField();
        height.setPropertyEditorType(Integer.class);
        
        String imageWidthStr = n.get("j:width");
        String imageHeightStr = n.get("j:height");
        int imageWidth = 0;
        int imageHeight = 0;
        try {
            imageWidth = imageWidthStr != null ? Integer.parseInt(imageWidthStr) : 0;
            imageHeight = imageHeightStr != null ? Integer.parseInt(imageHeightStr) : 0;
        } catch (NumberFormatException e) {
            // ignore
        }

        if (predefinedSizes != null && !predefinedSizes.isEmpty()) {
            List<GWTJahiaValueDisplayBean> sizes = new ArrayList<GWTJahiaValueDisplayBean>();
            for (Integer[] d : predefinedSizes) {
                if ((imageWidth == 0 || imageWidth > d[0]) && (imageHeight == 0 || imageHeight > d[1])) {
                    String label = d[0] + " x " + d[1];
                    GWTJahiaValueDisplayBean v = new GWTJahiaValueDisplayBean(label, label);
                    v.set("width", d[0]);
                    v.set("height", d[1]);
                    sizes.add(v);
                }
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
                        setDimensions((Integer) v.get("width"), (Integer) v.get("height"));
                    }
                });
                predefinedSizesBox.setEmptyText(Messages.get("selectPredefinedSize.label","select a predefined size"));
                main.add(predefinedSizesBox, new ColumnData(.6));
            } else {
            }
        }

        width.setFieldLabel(Messages.get("width.label", "Width"));
        width.setName("width");
        width.setId("width");
        lcWidth.add(width, formData);

        LayoutContainer lcHeight = new LayoutContainer();
        lcHeight.setStyleAttribute("paddingLeft", "10px");
        formLayout = new FormLayout(LabelAlign.RIGHT);
        formLayout.setLabelWidth(40);
        lcHeight.setLayout(formLayout);

        height.setFieldLabel(Messages.get("height.label", "Height"));
        height.setName("height");
        height.setId("height");
        lcHeight.add(height, formData);
        main.add(lcWidth, new ColumnData(.2));
        main.add(lcHeight, new ColumnData(.2));


        form.add(main, new FormData("100%"));

        final Image image = new Image();
        image.addLoadHandler(new LoadHandler() {
            public void onLoad(LoadEvent event) {
                initJcrop();
            }
        });
        // Point the image at a real URL.
        image.getElement().setId("cropbox");
        image.setUrl(n.getUrl());

        form.add(image);

        top = new NumberField();
        top.setPropertyEditorType(Integer.class);
        top.setName("top");
        top.setId("top");
        top.setVisible(false);
        form.add(top);
        left = new NumberField();
        left.setPropertyEditorType(Integer.class);
        left.setName("left");
        left.setId("left");
        left.setVisible(false);
        form.add(left);

        ButtonBar buttons = new ButtonBar();
        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                if (width.getValue().intValue() > 0 && height.getValue().intValue() > 0) {
                    cropImage(n.getPath(), newname.getValue().toString(), top.getValue().intValue(), left.getValue()
                            .intValue(), width.getValue().intValue(), height.getValue().intValue(), false);
                }
            }
        });
        buttons.add(submit);
        buttons.add(cancel);
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent be) {
                if (width.getValue() != null && height.getValue() != null) {
                    setDimensions(width.getValue().intValue(), height.getValue().intValue());
                    if (predefinedSizesBox != null) {
                        predefinedSizesBox.clearSelections();
                    }
                }
            }
        };
        width.addListener(Events.Change, listener);
        height.addListener(Events.Change, listener);

        add(form);

        setModal(true);
        setHeaderVisible(true);
        setAutoHide(false);
    }
    
    public void cropSelectionChanged(int x, int y, int w, int h) {
        left.setValue(x);
        top.setValue(y);
        width.setRawValue(String.valueOf(w));
        height.setRawValue(String.valueOf(h));

        if (autoName) {
            // change "new name" field automatically
            String n = newname.getValue();
            int dp = n.lastIndexOf('.');
            newname.setValue(n.substring(0, n.lastIndexOf("-crop")) + "-crop" + w + "x" + h
                    + (dp != -1 ? n.substring(dp, n.length()) : ""));
        }
        
        GWTJahiaValueDisplayBean v = predefinedSizesBox != null ? predefinedSizesBox.getValue() : null;
        if (v != null && (w != ((Integer) v.get("width")) || h != ((Integer) v.get("height")))) {
            // if the selection is changed -> clear predefined size box selection
            predefinedSizesBox.clearSelections();
        }
    }

    private native void setDimensions(int w, int h) /*-{
        try {
            var jcapi=$wnd.jQuery('#cropbox').data('Jcrop');
            var c=jcapi.tellSelect();
            jcapi.setSelect([c.x, c.y, c.x + w, c.y + h]);
        } catch (e) { }
    }-*/;

    private native void initJcrop() /*-{
        thisic=this;
        $wnd.jQuery('#cropbox').Jcrop({
            boxWidth: 700,
            boxHeight: 400,
            onChange: function(c) {
                thisic.@org.jahia.ajax.gwt.client.widget.content.ImageCrop::cropSelectionChanged(IIII)(Math.round(c.x), Math.round(c.y), Math.round(c.w), Math.round(c.h));
            }
        });
    }-*/;

    private void cropImage(final String path, final String targetName, final int top, final int left, final int width, final int height, final boolean force) {
        JahiaContentManagementService.App.getInstance().cropImage(path, targetName, top, left, width, height, force, new BaseAsyncCallback<Object>() {
            public void onApplicationFailure(Throwable throwable) {
                if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(Messages.get("alreadyExists.label") + "\n" + Messages.get("confirm.overwrite.label"))) {
                        cropImage(path, targetName, top, left, width, height, true);
                    }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.crop.label") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.get("failure.crop.label"), throwable);
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

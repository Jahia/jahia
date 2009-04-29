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

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Events;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.node.ExistingFileException;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:07:20 PM
 */
public class ImageResize extends Window {

    private BrowserLinker m_linker ;

    public ImageResize(final BrowserLinker linker, final GWTJahiaNode n) {
        super() ;

        m_linker = linker ;

        int w = n.getWidth();
        int h = n.getHeight();
        if (w > 800) {
            h = h * 800 / w;
            w = 800;
        }
        if (h > 350) {
            w = w * 350 / h;
            h = 350;
        }

        setHeading(Messages.getResource("fm_resize"));
        setSize(500, 200);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        setModal(true);


        final NumberField wf = new NumberField();
        wf.setName("width");
        wf.setValue(new Integer(n.getWidth()));
        wf.setFieldLabel(Messages.getResource("fm_width"));
        form.add(wf);

        final NumberField hf = new NumberField();
        hf.setName("height");
        hf.setValue(new Integer(n.getHeight()));
        hf.setFieldLabel(Messages.getResource("fm_height"));
        form.add(hf);

        final CheckBox keepRatio = new CheckBox();
        keepRatio.setName("ratio");
        keepRatio.setValue(true);
        keepRatio.setFieldLabel(Messages.getResource("fm_ratio"));
        form.add(keepRatio);

        hf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) wf.setValue(n.getWidth() * hf.getValue().intValue() / n.getHeight());
            }
        });
        wf.addListener(Events.KeyUp, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(n.getHeight() * wf.getValue().intValue() / n.getWidth());
            }
        });

        keepRatio.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (keepRatio.getValue()) hf.setValue(n.getHeight() * wf.getValue().intValue() / n.getWidth());
            }
        });

        final TextField<String> newname = new TextField<String>();
        newname.setName("newname");
        int extIndex = n.getName().lastIndexOf(".") ;
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex) ;
            newname.setValue(n.getName().replaceAll(dotExt, "_resize" + dotExt));
        } else {
            newname.setValue(n.getName() + "_resize");
        }
        newname.setFieldLabel(Messages.getResource("fm_newname"));
        form.add(newname);

        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Messages.getResource("fm_ok"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                resizeImage(n.getPath(), newname.getValue(), wf.getValue().intValue(), hf.getValue().intValue(), false) ;
            }
        }) ;
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        add(form);
    }

    private void resizeImage(final String path, final String targetName, final int width, final int height, final boolean force) {
         JahiaNodeService.App.getInstance().resizeImage(path, targetName, width, height, force, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(throwable.toString() + "\n" + Messages.getResource("fm_confOverwrite"))) {
                         resizeImage(path, targetName, width, height, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.getResource("fm_failResize") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.getResource("fm_failResize"), throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                m_linker.refreshTable();
             }
         });
    }
}

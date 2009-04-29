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

import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.service.node.ExistingFileException;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ImageRotate extends Window {

    private BrowserLinker m_linker ;

    public ImageRotate(final BrowserLinker linker, final GWTJahiaNode n) {
        super() ;

        m_linker = linker ;

        setHeading(Messages.getResource("fm_rotate"));
        setSize(500, 150);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        setModal(true);


        final TextField<String> newname = new TextField<String>();
        newname.setName("newname");
        int extIndex = n.getName().lastIndexOf(".") ;
        if (extIndex > 0) {
            String dotExt = n.getName().substring(extIndex) ;
            newname.setValue(n.getName().replaceAll(dotExt, "_rotate" + dotExt));
        } else {
            newname.setValue(n.getName() + "_rotate");
        }
        newname.setFieldLabel(Messages.getResource("fm_newname"));
        form.add(newname);

        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });
        Button left = new Button(Messages.getResource("fm_rotateLeft"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                rotateImage(n.getPath(), newname.getValue(), false, false) ;
            }
        }) ;
        Button right = new Button(Messages.getResource("fm_rotateRight"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                rotateImage(n.getPath(), newname.getValue(), true, false) ;
            }
        }) ;
        buttons.add(left) ;
        buttons.add(right) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        add(form);
    }

    private void rotateImage(final String path, final String target, final boolean clockwise, boolean force) {
         JahiaNodeService.App.getInstance().rotateImage(path, target, clockwise, force, new AsyncCallback() {
             public void onFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(throwable.toString() + "\n"+ Messages.getResource("fm_confOverwrite"))) {
                         rotateImage(path, target, clockwise, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.getResource("fm_failRotate") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.getResource("fm_failRotate"), throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                m_linker.refreshTable();
             }
         });
    }

}

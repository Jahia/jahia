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
                    if (com.google.gwt.user.client.Window.confirm(Messages.getResource("fm_alreadyExists") + "\n"+ Messages.getResource("fm_confOverwrite"))) {
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

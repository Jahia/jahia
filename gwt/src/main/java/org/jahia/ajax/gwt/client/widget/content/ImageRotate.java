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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ImageRotate extends Window {

    private Linker m_linker ;

    public ImageRotate(final Linker linker, final GWTJahiaNode n) {
        super() ;

        m_linker = linker ;

        setHeading(Messages.get("label.rotate"));
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
        newname.setFieldLabel(Messages.get("label.rename"));
        form.add(newname);

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        Button left = new Button(Messages.get("label.rotateLeft"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                rotateImage(n.getPath(), newname.getValue(), false, false) ;
            }
        }) ;
        Button right = new Button(Messages.get("label.rotateRight"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                rotateImage(n.getPath(), newname.getValue(), true, false) ;
            }
        }) ;
        buttons.add(left) ;
        buttons.add(right) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);

        add(form);
    }

    private void rotateImage(final String path, final String target, final boolean clockwise, boolean force) {
         JahiaContentManagementService.App.getInstance().rotateImage(path, target, clockwise, force, new BaseAsyncCallback() {
             public void onApplicationFailure(Throwable throwable) {
                 if (throwable instanceof ExistingFileException) {
                    if (com.google.gwt.user.client.Window.confirm(Messages.get("alreadyExists.label") + "\n"+ Messages.get("confirm.overwrite.label"))) {
                         rotateImage(path, target, clockwise, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert(Messages.get("failure.rotate.label") + "\n" + throwable.getLocalizedMessage());
                    Log.error(Messages.get("failure.rotate.label"), throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                m_linker.refresh(Linker.REFRESH_MAIN);
             }
         });
    }

}

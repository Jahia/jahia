/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.filemanagement.client.ui;

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
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaNode;
import org.jahia.ajax.gwt.filemanagement.client.util.Resources;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.filemanagement.client.exception.ExistingFileException;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;

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

        setHeading("Image resize");
        setSize(500, 200);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setInsetBorder(false);
        form.setBorders(false);
        setModal(true);


        final NumberField wf = new NumberField();
        wf.setName("width");
        wf.setValue(new Integer(n.getWidth()));
        wf.setFieldLabel(Resources.getResource("fm_width"));
        form.add(wf);

        final NumberField hf = new NumberField();
        hf.setName("height");
        hf.setValue(new Integer(n.getHeight()));
        hf.setFieldLabel(Resources.getResource("fm_height"));
        form.add(hf);

        final CheckBox keepRatio = new CheckBox();
        keepRatio.setName("ratio");
        keepRatio.setValue(true);
        keepRatio.setFieldLabel(Resources.getResource("fm_ratio"));
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
        newname.setFieldLabel(Resources.getResource("fm_newname"));
        form.add(newname);

        Button cancel = new Button("Cancel", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });
        Button submit = new Button("OK", new SelectionListener<ComponentEvent>() {
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
                    if (com.google.gwt.user.client.Window.confirm(throwable.toString() + "\nOverwrite ?")) {
                         resizeImage(path, targetName, width, height, true);
                     }
                } else {
                    com.google.gwt.user.client.Window.alert("cannot resize image \n\n" + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }
             }

             public void onSuccess(Object result) {
                hide();
                m_linker.refreshTable();
             }
         });
    }
}

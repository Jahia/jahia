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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.filemanagement.client.JahiaNodeService;
import org.jahia.ajax.gwt.filemanagement.client.util.Resources;
import org.jahia.ajax.gwt.tripanelbrowser.client.BrowserLinker;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class Mounter extends Window {

    public Mounter(final BrowserLinker linker) {
        super() ;
        setHeading("Mount");
        setSize(500, 180);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setLabelWidth(150);
        form.setFieldWidth(300);
        form.setBodyBorder(false);
        form.setBorders(false);
        form.setHeaderVisible(false);
        setModal(true);

        final TextField<String> f = new TextField<String>();
        f.setName("mountpoint");
        f.setFieldLabel(Resources.getResource("fm_mountpoint"));

        form.add(f);

        final TextField<String> t = new TextField<String>();
        t.setName("root");
        t.setValue("smb://");
        t.setFieldLabel(Resources.getResource("fm_serveraddress"));
        form.add(t);

        final ProgressBar bar = new ProgressBar() ;
        final AdapterField barField = new AdapterField(bar) ;
        barField.setFieldLabel("Progress");
        form.add(barField) ;
        barField.setVisible(false);


        final Button cancel = new Button("Cancel", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });

        final Button submit = new Button("OK") ;
        SelectionListener<ComponentEvent> selectionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                barField.setVisible(true);
                bar.auto() ;
                linker.loading("mounting...");
                submit.setEnabled(false);
                cancel.setEnabled(false);
                JahiaNodeService.App.getInstance().mount("", f.getValue(), t.getValue(), new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error("error", throwable);
                        linker.loaded() ;
                        com.google.gwt.user.client.Window.alert("Cannot mount remote server at " + t.getValue());
                        hide();
                    }

                    public void onSuccess(Object o) {
                        Log.info("suceess");
                        bar.reset() ;
                        linker.loaded() ;
                        hide();
                        linker.refreshAll();
                    }

                });

            }
        };
        submit.addSelectionListener(selectionListener);
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setButtonBar(buttons);

        add(form);
        setScrollMode(Style.Scroll.AUTO);
        show();
    }

}
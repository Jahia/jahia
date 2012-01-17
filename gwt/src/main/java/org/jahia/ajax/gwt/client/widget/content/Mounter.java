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
import com.extjs.gxt.ui.client.widget.ProgressBar;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * 
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class Mounter extends Window {

    public Mounter(final Linker linker) {
        super() ;
        setHeading(Messages.get("label.mount"));
        setSize(500, 250);
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
        f.setFieldLabel(Messages.get("mountpoint.label"));

        form.add(f);

        final TextField<String> t = new TextField<String>();
        t.setName("root");
        t.setValue("smb://");
        t.setFieldLabel(Messages.get("serveraddress.label"));
        form.add(t);

        final Label disclaimer = new Label(Messages.get("mount.disclaimer.label"));
        final AdapterField disclaimerField = new AdapterField(disclaimer);
        disclaimerField.setFieldLabel(Messages.get("mount.disclaimer"));
        form.add(disclaimerField);

        final ProgressBar bar = new ProgressBar() ;
        final AdapterField barField = new AdapterField(bar) ;
        barField.setFieldLabel(Messages.get("statusbar.mounting.label"));
        form.add(barField) ;
        barField.setVisible(false);


        final Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });

        final Button submit = new Button(Messages.get("label.ok")) ;
        SelectionListener<ButtonEvent> selectionListener = new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                barField.setVisible(true);
                bar.auto() ;
                linker.loading(Messages.get("statusbar.mounting.label")) ;
                submit.setEnabled(false);
                cancel.setEnabled(false);
                JahiaContentManagementService.App.getInstance().mount("", f.getValue(), t.getValue(), new BaseAsyncCallback() {
                    public void onApplicationFailure(Throwable throwable) {
                        Log.error(Messages.get("failure.mount.label"), throwable);
                        linker.loaded() ;
                        com.google.gwt.user.client.Window.alert(Messages.get("failure.mount.label") + " " + t.getValue());
                        hide();
                    }

                    public void onSuccess(Object o) {
                        //Log.info("success");
                        bar.reset() ;
                        linker.loaded() ;
                        hide();
                        linker.refresh(Linker.REFRESH_ALL);
                    }

                });

            }
        };
        submit.addSelectionListener(selectionListener);
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setTopComponent(buttons);

        add(form);
        setScrollMode(Style.Scroll.AUTO);
        show();
    }

}
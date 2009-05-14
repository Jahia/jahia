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
import com.google.gwt.user.client.ui.Label;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.node.JahiaNodeService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 17:45:41
 */
public class Mounter extends Window {

    public Mounter(final BrowserLinker linker) {
        super() ;
        setHeading(Messages.getResource("fm_mount"));
        setSize(500, 230);
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
        f.setFieldLabel(Messages.getResource("fm_mountpoint"));

        form.add(f);

        final TextField<String> t = new TextField<String>();
        t.setName("root");
        t.setValue("smb://");
        t.setFieldLabel(Messages.getResource("fm_serveraddress"));
        form.add(t);

        final Label disclaimer = new Label(Messages.getResource("fm_mountDisclaimerLabel"));
        final AdapterField disclaimerField = new AdapterField(disclaimer);
        disclaimerField.setFieldLabel(Messages.getResource("fm_mountDisclaimer"));
        form.add(disclaimerField);

        final ProgressBar bar = new ProgressBar() ;
        final AdapterField barField = new AdapterField(bar) ;
        barField.setFieldLabel(Messages.getResource("fm_mounting"));
        form.add(barField) ;
        barField.setVisible(false);


        final Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                hide() ;
            }
        });

        final Button submit = new Button(Messages.getResource("fm_ok")) ;
        SelectionListener<ComponentEvent> selectionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                barField.setVisible(true);
                bar.auto() ;
                linker.loading(Messages.getResource("fm_mounting")) ;
                submit.setEnabled(false);
                cancel.setEnabled(false);
                JahiaNodeService.App.getInstance().mount("", f.getValue(), t.getValue(), new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error(Messages.getResource("fm_failMount"), throwable);
                        linker.loaded() ;
                        com.google.gwt.user.client.Window.alert(Messages.getResource("fm_failMount") + " " + t.getValue());
                        hide();
                    }

                    public void onSuccess(Object o) {
                        //Log.info("success");
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
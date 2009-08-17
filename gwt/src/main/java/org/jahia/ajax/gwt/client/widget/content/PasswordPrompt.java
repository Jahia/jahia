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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:07:20 PM
 */
public class PasswordPrompt extends Window {

    private BrowserLinker m_linker ;

    public PasswordPrompt(final BrowserLinker linker, String username, final String providerKey, final MenuItem item, final String logoutLabel) {
        super() ;

        m_linker = linker ;

        setHeading(Messages.getResource("fm_password"));
        setSize(500, 200);
        setResizable(false);
        ButtonBar buttons = new ButtonBar() ;
        final FormPanel form = new FormPanel() ;
        form.setFrame(false);
        form.setHeaderVisible(false);
        form.setBorders(false);
        setModal(true);


        final TextField<String> user = new TextField<String>();
        user.setValue(username);
        user.setName("fm_username");
        user.setFieldLabel(Messages.getResource("fm_username"));
        form.add(user);

        final TextField<String> pass = new TextField<String>();
        pass.setName("fm_password");
        pass.setFieldLabel(Messages.getResource("fm_password"));
        pass.setPassword(true);
        form.add(pass);

        Button cancel = new Button(Messages.getResource("fm_cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        Button submit = new Button(Messages.getResource("fm_ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                JahiaContentManagementService.App.getInstance().storePasswordForProvider(providerKey, user.getValue(), pass.getValue(), new AsyncCallback() {
                    public void onSuccess(Object o) {
                        hide();
                        item.setText(logoutLabel);
                        m_linker.refreshAll();
                    }

                    public void onFailure(Throwable throwable) {
                        Log.error(Messages.getResource("fm_fail"), throwable);
                        hide();
                    }
                });
            }
        }) ;
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setTopComponent(buttons);

        add(form);
    }

}
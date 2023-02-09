/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
import com.allen_sauer.gwt.log.client.Log;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:07:20 PM
 */
public class PasswordPrompt extends Window {

    private ManagerLinker m_linker ;

    public PasswordPrompt(final ManagerLinker linker, String username, final String providerKey, final MenuItem item, final String logoutLabel) {
        super() ;
        addStyleName("password-prompt");
        m_linker = linker ;

        setHeadingHtml(Messages.get("label.password"));
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
        user.setName("label.username");
        user.setFieldLabel(Messages.get("label.username"));
        form.add(user);

        final TextField<String> pass = new TextField<String>();
        pass.setName("label.password");
        pass.setFieldLabel(Messages.get("label.password"));
        pass.setPassword(true);
        form.add(pass);

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        cancel.addStyleName("button-cancel");
        Button submit = new Button(Messages.get("label.ok"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                JahiaContentManagementService.App.getInstance().storePasswordForProvider(providerKey, user.getValue(), pass.getValue(), new BaseAsyncCallback() {
                    public void onSuccess(Object o) {
                        hide();
                        item.setText(logoutLabel);
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_ALL, true);
                        m_linker.refresh(data);
                    }

                    public void onApplicationFailure(Throwable throwable) {
                        Log.error(Messages.get("fm_fail"), throwable);
                        hide();
                    }
                });
            }
        }) ;
        submit.addStyleName("button-submit");
        buttons.add(submit) ;
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setTopComponent(buttons);

        add(form);
    }

}

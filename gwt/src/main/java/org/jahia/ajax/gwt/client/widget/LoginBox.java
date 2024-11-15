/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Displays the login form.
 *
 * @author Sergiy Shyrkov
 */
public class LoginBox extends Window {

    private static LoginBox instance;

    public static LoginBox getInstance() {
        if (instance == null) {
            instance = new LoginBox();
        }
        return instance;
    }

    public LoginBox() {

        addStyleName("login-box");
        this.setSize(400, 200);
        this.setModal(true);
        this.setBlinkModal(true);
        this.setHeadingHtml(Messages.get("label.login", "Login"));
        this.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setAction(JahiaGWTParameters.getContextPath() + "/cms/login?doLogin=true&restMode=true&site="+JahiaGWTParameters.getSiteKey());
        form.setMethod(Method.POST);
        form.setLabelWidth(125);

        VerticalPanel vpLabels = new VerticalPanel();
        vpLabels.add(new Label(Messages.get("message.sessionExpired",
                "You have been performed no actions for some time. Due to security concerns we have ended your session. Please log in again.")));

        final Label lbWrongCredentials =
                new Label(Messages.get("message_invalidUsernamePassword", "Invalid username/password"));
        lbWrongCredentials.setVisible(false);
        lbWrongCredentials.setStyleAttribute("color", "red");
        vpLabels.add(lbWrongCredentials);

        form.add(vpLabels);

        TextField<String> username = new TextField<String>();
        username.setName("username");
        username.setFieldLabel(Messages.get("label.username", "Username"));
        username.setAllowBlank(false);
        username.setValue(JahiaGWTParameters.getCurrentUser());
        username.setReadOnly(true);

        username.addKeyListener(new KeyListener() {
            public void componentKeyPress(ComponentEvent event) {
                if (event.getKeyCode() == 13) {
                    lbWrongCredentials.setVisible(false);
                    form.submit();
                }
            }
        });
        form.add(username);

        TextField<String> password = new TextField<String>();
        password.setPassword(true);
        password.setName("password");
        password.setFieldLabel(Messages.get("label.password", "Password"));
        password.setAllowBlank(false);
        password.addKeyListener(new KeyListener() {
            public void componentKeyPress(ComponentEvent event) {
                if (event.getKeyCode() == 13) {
                    lbWrongCredentials.setVisible(false);
                    form.submit();
                }
            }
        });
        form.add(password);


        Button btnSubmit = new Button(Messages.get("label.login", "Login"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                lbWrongCredentials.setVisible(false);
                form.submit();
            }
        });
        btnSubmit.addStyleName("button-submit");

        form.addButton(btnSubmit);


        Button btnCancel = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
                final String portOrigin = com.google.gwt.user.client.Window.Location.getPort();
                String port = (portOrigin == null || portOrigin.equals("80") || portOrigin.equals("")) ? "" : (":" + portOrigin);
                com.google.gwt.user.client.Window.Location.assign(
                        com.google.gwt.user.client.Window.Location.getProtocol() + "//" +
                                com.google.gwt.user.client.Window.Location.getHostName() + port + JahiaGWTParameters.getContextPath());
            }
        });
        btnCancel.addStyleName("button-cancel");
        form.addButton(btnCancel);
        form.setButtonAlign(HorizontalAlignment.CENTER);

        form.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent formEvent) {
                if (!formEvent.getResultHtml().contains("OK")) {
                    // login information was incorrect
                    lbWrongCredentials.setVisible(true);
                } else {
                    if (CommonEntryPoint.getSessionCheckTimer() != null) {
                        CommonEntryPoint.getSessionCheckTimer().run();
                    }
                    hide();
                }
            }
        });

        this.add(form);
    }
}

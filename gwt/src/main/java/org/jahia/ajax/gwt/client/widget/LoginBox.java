/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
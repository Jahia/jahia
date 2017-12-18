/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.messages.Messages;

public class ForgeLoginWindow extends Window {
    public static String username = "";
    public static String password = "";

    public interface Callback {
        void handle(String username, String password);
    }

    private Callback callback;

    private GWTModuleReleaseInfo releaseInfo;

    public ForgeLoginWindow() {
        super();
        addStyleName("forge-login-window");
    }

    @Override
    protected void onRender(Element element, int index) {
        super.onRender(element, index);

        setLayout(new FitLayout());
        setHeadingHtml(Messages.get("label.login", "Login"));
        setModal(true);
        setWidth(500);
        setHeight(150);

        final FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setLabelWidth(150);
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);

        final TextField<String>tfUsername = new TextField<String>();
        tfUsername.setFieldLabel(Messages.get("label.username", "Username"));
        tfUsername.setValue(username);
        formPanel.add(tfUsername);

        final TextField<String> tfPassword = new TextField<String>();
        tfPassword.setFieldLabel(Messages.get("label.password", "Password"));
        tfPassword.setValue(password);
        tfPassword.setPassword(true);
        formPanel.add(tfPassword);

        Button b = new Button(Messages.get("label.login", "Login"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                ForgeLoginWindow.username = tfUsername.getValue();
                ForgeLoginWindow.password = tfPassword.getValue();
                callback.handle(tfUsername.getValue(), tfPassword.getValue());
            }
        });
        b.addStyleName("button-login");

        formPanel.addButton(b);

        final Window w = this;
        b = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
            }
        });
        b.addStyleName("button-cancel");
        formPanel.addButton(b);

        add(formPanel);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}

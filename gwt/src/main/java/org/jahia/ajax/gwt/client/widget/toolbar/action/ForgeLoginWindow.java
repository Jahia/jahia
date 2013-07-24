package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.messages.Messages;

import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    protected void onRender(Element element, int index) {
        super.onRender(element, index);

        String versionInfo = JahiaGWTParameters.getSiteNode().get("j:versionInfo");

        setLayout(new FitLayout());
        setHeading(Messages.get("label.login", "Login"));
        setModal(true);
        setWidth(500);
        setHeight(150);

        final List<Integer> versionNumbers = JahiaGWTParameters.getSiteNode().get("j:versionNumbers");
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
        formPanel.addButton(b);

        final Window w = this;
        b = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
            }
        });
        formPanel.addButton(b);

        add(formPanel);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


}

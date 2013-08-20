package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.messages.Messages;

public abstract class DistributionServerWindow extends Window {

    private GWTModuleReleaseInfo info;

    protected DistributionServerWindow() {
        this.info = new GWTModuleReleaseInfo();
    }

    protected DistributionServerWindow(GWTModuleReleaseInfo info) {
        if (info != null) {
            this.info = info;
        } else {
            this.info = new GWTModuleReleaseInfo();
        }
    }

    protected abstract void callback(GWTModuleReleaseInfo info);

    @Override
    protected void onRender(Element parent, int pos) {
        super.onRender(parent, pos);

        setLayout(new FitLayout());
        setHeading(Messages.get("label.releaseModule.distributionServer", "Distribution server (Maven)"));
        setModal(true);
        setWidth(500);
        setHeight(380);

        VerticalPanel p = new VerticalPanel();
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.notProvided",
                "No target distribution server configured for this module yet.")));
        p.add(new HTML("<br/>"));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.purpose",
                "A target distribution server is a Maven repository,"
                        + " where built module artifacts (module JAR file)"
                        + " are pushed to during module release process.")));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.authentication",
                "If your distribution server requires authentication, please, provide the corresponding"
                        + " <server/> section in your Maven's settings.xml file.")));
        p.add(new HTML("<br/>"));
        p.add(new Label(Messages.get("label.releaseModule.distributionServer.provideNow",
                "Would you like to configure the distribution server now?")));

        final FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setLabelWidth(100);
        formPanel.setFieldWidth(330);
        formPanel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        formPanel.setBorders(false);

        final ComboBox<GWTJahiaValueDisplayBean> combo = new ComboBox<GWTJahiaValueDisplayBean>();
        combo.setFieldLabel("Repository type");
        combo.setValueField("value");
        combo.setDisplayField("display");
        combo.setStore(new ListStore<GWTJahiaValueDisplayBean>());
        combo.getStore().add(new GWTJahiaValueDisplayBean("forge", "Jahia forge"));
        combo.getStore().add(new GWTJahiaValueDisplayBean("maven", "Maven repository"));
        combo.setForceSelection(true);
        combo.setTypeAhead(false);
        combo.setTriggerAction(ComboBox.TriggerAction.ALL);

        formPanel.add(combo);

        final FieldSet forgeFs = new FieldSet();
        final FormLayout forgeFl = new FormLayout();
        forgeFl.setLabelWidth(100);
        forgeFl.setDefaultWidth(330);
        forgeFs.setLayout(forgeFl);

        final TextField<String> tfForgeUrl = new TextField<String>();
        tfForgeUrl.setFieldLabel(Messages.get("label.url", "URL"));
        tfForgeUrl.setAllowBlank(false);
        if (info.getForgeUrl() != null) {
            tfForgeUrl.setValue(info.getForgeUrl());
        }
        forgeFs.add(tfForgeUrl);
        final TextField<String> tfUsername = new TextField<String>();
        final TextField<String> tfPassword = new TextField<String>();
        tfUsername.setFieldLabel(Messages.get("label.username", "Username"));
        tfPassword.setFieldLabel(Messages.get("label.password", "Password"));
        tfPassword.setPassword(true);
        tfUsername.setValue(ForgeLoginWindow.username);
        tfPassword.setValue(ForgeLoginWindow.password);
        forgeFs.add(tfUsername);
        forgeFs.add(tfPassword);

        final FieldSet mavenFs = new FieldSet();
        final FormLayout mavenfl = new FormLayout();
        mavenfl.setLabelWidth(30);
        mavenfl.setDefaultWidth(400);
        mavenFs.setLayout(mavenfl);

        final TextField<String> tfRepoId = new TextField<String>();
        tfRepoId.setFieldLabel(Messages.get("label.id", "ID"));
        tfRepoId.setAllowBlank(false);
        if (info.getRepositoryId() != null) {
            tfRepoId.setValue(info.getRepositoryId());
        }
        mavenFs.add(tfRepoId);

        final TextField<String> tfRepoUrl = new TextField<String>();
        tfRepoUrl.setFieldLabel(Messages.get("label.url", "URL"));
        tfRepoUrl.setAllowBlank(false);
        if (info.getRepositoryUrl() != null) {
            tfRepoUrl.setValue(info.getRepositoryUrl());
        }
        mavenFs.add(tfRepoUrl);

        formPanel.add(mavenFs);

        if (info.getForgeUrl() == null && info.getRepositoryUrl() != null) {
            combo.setValue(combo.getStore().getAt(1));
            forgeFs.hide();
        } else {
            combo.setValue(combo.getStore().getAt(0));
            mavenFs.hide();
        }

        formPanel.add(forgeFs);

        combo.addSelectionChangedListener(new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                if (se.getSelectedItem().getValue().equals("forge")) {
                    mavenFs.hide();
                    forgeFs.show();
                } else {
                    forgeFs.hide();
                    mavenFs.show();
                }
                formPanel.layout();
            }
        });

        final Window w = this;
        formPanel.addButton(new Button(Messages.get("label.save", "Save"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
                if (combo.getValue().getValue().equals("forge")) {
                    info.setForgeUrl(tfForgeUrl.getValue());
                    info.setUsername(tfUsername.getValue());
                    info.setPassword(tfPassword.getValue());
                    ForgeLoginWindow.username = tfUsername.getValue();
                    ForgeLoginWindow.password = tfPassword.getValue();
                } else {
                    info.setForgeUrl(null);
                    info.setRepositoryUrl(tfRepoUrl.getValue());
                    info.setRepositoryId(tfRepoId.getValue());
                }
                callback(info);
            }
        }));
        formPanel.addButton(new Button(Messages.get("label.skip", "Skip"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                w.hide();
                callback(null);
            }
        }));

        p.add(formPanel);

        add(p, new MarginData(5));
    }
}

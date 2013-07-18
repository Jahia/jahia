/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTModuleReleaseInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.Element;

/**
 * Dialog screen for module release process.
 * 
 * @author Sergiy Shyrkov
 */
public class ReleaseModuleWindow extends Window {

    public interface Callback {
        void handle(GWTModuleReleaseInfo releaseInfo);
    }

    private Callback callback;

    private GWTModuleReleaseInfo releaseInfo;

    public ReleaseModuleWindow(GWTModuleReleaseInfo releaseInfo) {
        super();
        this.releaseInfo = releaseInfo != null ? releaseInfo : new GWTModuleReleaseInfo();
    }

    private String generateVersionNumber(List<Integer> orderedVersionNumbers, int index) {
        List<Integer> newOrderedVersionNumbers = new ArrayList<Integer>(orderedVersionNumbers);
        newOrderedVersionNumbers.set(index, orderedVersionNumbers.get(index) + 1);
        for (int i = index + 1; i < newOrderedVersionNumbers.size(); i++) {
            newOrderedVersionNumbers.set(i, Integer.valueOf(0));
        }
        String s = "";
        for (Integer n : newOrderedVersionNumbers) {
            s += ("." + n);
        }
        s = s.substring(1);
        s += "-SNAPSHOT";
        return s;
    }

    @Override
    protected void onRender(Element element, int index) {
        super.onRender(element, index);

        String versionInfo = JahiaGWTParameters.getSiteNode().get("j:versionInfo");

        setLayout(new FitLayout());
        setHeading(Messages.get("label.releaseWar") + "&nbsp;" + versionInfo + "&nbsp;->&nbsp;"
                + versionInfo.replace("-SNAPSHOT", ""));
        setModal(true);
        setWidth(500);
        setHeight(150);

        final List<Integer> versionNumbers = JahiaGWTParameters.getSiteNode().get("j:versionNumbers");
        final FormPanel formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setLabelWidth(150);
        formPanel.setButtonAlign(HorizontalAlignment.CENTER);

        final SimpleComboBox<String> cbNextVersion = new SimpleComboBox<String>();
        cbNextVersion.setFieldLabel(Messages.get("label.nextVersion", "Next iteration version"));
        cbNextVersion.setTriggerAction(ComboBox.TriggerAction.ALL);
        cbNextVersion.setForceSelection(false);
        String minorVersion = generateVersionNumber(versionNumbers, 1);
        cbNextVersion.add(minorVersion);
        cbNextVersion.add(generateVersionNumber(versionNumbers, 0));
        cbNextVersion.setSimpleValue(minorVersion);
        formPanel.add(cbNextVersion);

        FieldSet fsMaven = null;
        if (releaseInfo.getRepositoryUrl() != null) {
            setHeight(200);
            fsMaven = new FieldSet();
            fsMaven.setCheckboxToggle(true);
            GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
            if (siteNode.get("j:sourcesFolder") != null && siteNode.get("j:scmURI") != null) {
                fsMaven.setCollapsible(false);
            }
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(30);
            fl.setDefaultWidth(400);
            fsMaven.setLayout(fl);
            fsMaven.setHeading(Messages.get("label.releaseModule.publishToMaven",
                    "Publish to Maven distribution server"));

            if (releaseInfo.getRepositoryId() != null) {
                LabelField lbRepoId = new LabelField();
                lbRepoId.setValue(releaseInfo.getRepositoryId());
                lbRepoId.setFieldLabel(Messages.get("label.id", "ID") + ":");
                fsMaven.add(lbRepoId);
            }
            LabelField lbRepoUrl = new LabelField();
            lbRepoUrl.setToolTip(releaseInfo.getRepositoryUrl());
            lbRepoUrl.setValue(releaseInfo.getRepositoryUrl());
            lbRepoUrl.setFieldLabel(Messages.get("label.url", "URL") + ":");
            fsMaven.add(lbRepoUrl);

            formPanel.add(fsMaven);
        }

        FieldSet fsCatalog = null;
        TextField<String> tfUsername = null;
        TextField<String> tfPassword = null;
        if (fsMaven != null && releaseInfo.getCatalogUrl() != null) {
            setHeight(340);
            fsCatalog = new FieldSet();
            fsCatalog.setCheckboxToggle(true);
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(80);
            fl.setDefaultWidth(320);
            fsCatalog.setLayout(fl);
            fsCatalog.setHeading(Messages
                    .get("label.releaseModule.publishToModuleCatalog", "Publish to module catalog"));

            LabelField lbCatalogUrl = new LabelField();
            lbCatalogUrl.setToolTip(releaseInfo.getCatalogUrl());
            lbCatalogUrl.setValue(releaseInfo.getCatalogUrl());
            lbCatalogUrl.setFieldLabel(Messages.get("label.url", "URL") + ":");
            fsCatalog.add(lbCatalogUrl);

            tfUsername = new TextField<String>();
            tfUsername.setFieldLabel(Messages.get("label.username", "Username"));
            fsCatalog.add(tfUsername);

            tfPassword = new TextField<String>();
            tfPassword.setFieldLabel(Messages.get("label.password", "Password"));
            tfPassword.setPassword(true);
            fsCatalog.add(tfPassword);

            if (fsMaven.isCollapsible()) {
                final FieldSet finalFsCatalog = fsCatalog;
                Listener<FieldSetEvent> listener = new Listener<FieldSetEvent>() {
                    @Override
                    public void handleEvent(FieldSetEvent be) {
                        if (be.getFieldSet().isExpanded()) {
                            finalFsCatalog.show();
                        } else {
                            finalFsCatalog.hide();
                        }
                    }};
                fsMaven.addListener(Events.Expand, listener);
                fsMaven.addListener(Events.Collapse, listener);
            }

            formPanel.add(fsCatalog);
        }

        final FieldSet finalFsMaven = fsMaven;
        final FieldSet finalFsCatalog = fsCatalog;
        final TextField<String> finalTfUsername = tfUsername;
        final TextField<String> finalTfPassword = tfPassword;

        Button b = new Button(Messages.get("label.release", "Release"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                String v = cbNextVersion.getRawValue();
                if (v == null || v.length() == 0 || !v.endsWith("-SNAPSHOT")) {
                    cbNextVersion.markInvalid(Messages.get("label.snapshotRequired",
                            "Working version number must end with -SNAPSHOT"));
                    return;
                }

                releaseInfo.setNextVersion(cbNextVersion.getRawValue());
                releaseInfo.setPublishToMaven(finalFsMaven != null && finalFsMaven.isExpanded());
                releaseInfo.setPublishToCatalog(finalFsCatalog != null && finalFsCatalog.isVisible()
                        && finalFsCatalog.isExpanded());
                if (releaseInfo.isPublishToCatalog()) {
                    releaseInfo.setCatalogUsername(finalTfUsername.getValue());
                    releaseInfo.setCatalogPassword(finalTfPassword.getValue());
                }

                callback.handle(releaseInfo);
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

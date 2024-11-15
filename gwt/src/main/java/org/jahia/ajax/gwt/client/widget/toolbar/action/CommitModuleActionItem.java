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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Action item to create a new templates set
 */
@SuppressWarnings("serial")
public class CommitModuleActionItem extends BaseActionItem {

    @Override
    public void onComponentSelection() {
        final Window wnd = new Window();
        wnd.addStyleName("commit-modal");
        wnd.setWidth(450);
        wnd.setHeight(200);
        wnd.setModal(true);
        wnd.setBlinkModal(true);
        wnd.setHeadingHtml(Messages.get("label.commitModule", "Commit module"));
        wnd.setLayout(new FitLayout());

        final FormPanel form = new FormPanel();
        form.setHeaderVisible(false);
        form.setFrame(false);
        form.setLabelAlign(LabelAlign.TOP);
        form.setFieldWidth(415);

        final TextArea message = new TextArea();
        message.setName("sources");
        message.setFieldLabel(Messages.get("label.comment", "Comment"));
        message.setHeight(80);

        form.add(message);

        Button btnSubmit = new Button(Messages.get("label.save", "Save"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
                linker.loading(Messages.get("message.moduleSaving", "Saving module..."));
                JahiaContentManagementService.App.getInstance().saveModule(JahiaGWTParameters.getSiteKey(), message.getValue(), new BaseAsyncCallback<Object>() {

                    @Override
                    public void onSuccess(Object result) {
                        linker.loaded();
                        Info.display(Messages.get("label.information", "Information"), Messages.get("message.moduleSaved", "Module saved"));
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event", "commit");
                        linker.refresh(data);
                    }

                    @Override
                    public void onApplicationFailure(Throwable caught) {
                        linker.loaded();
                        MessageBox.alert(Messages.get("label.error", "Error"), caught.getMessage(), null);
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put("event", "commit");
                        linker.refresh(data);
                    }
                });
            }
        });
        btnSubmit.addStyleName("button-submit");

        form.addButton(btnSubmit);

        Button btnCancel = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
            }
        });
        btnCancel.addStyleName("button-cancel");
        form.addButton(btnCancel);
        form.setButtonAlign(Style.HorizontalAlignment.CENTER);

        wnd.add(form);
        wnd.layout();
        wnd.setFocusWidget(message);

        wnd.show();
    }

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        String s = siteNode.get("j:versionInfo");
        if (s != null && s.endsWith("-SNAPSHOT") && siteNode.get("j:sourcesFolder") != null && siteNode.get("j:scmURI") != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

}

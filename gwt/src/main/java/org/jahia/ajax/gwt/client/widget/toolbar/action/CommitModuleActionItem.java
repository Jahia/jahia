/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
        form.addButton(btnSubmit);

        Button btnCancel = new Button(Messages.get("label.cancel", "Cancel"), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent event) {
                wnd.hide();
            }
        });
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

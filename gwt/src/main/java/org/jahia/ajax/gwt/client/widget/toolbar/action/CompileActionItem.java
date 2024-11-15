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
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.user.client.ui.HTML;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Action item to create a new templates set
 */
@SuppressWarnings("serial")
public class CompileActionItem extends BaseActionItem {

    @Override public void onComponentSelection() {
        linker.loading(Messages.get("label.compilingModule", "Compile module..."));
        JahiaContentManagementService.App.getInstance().compileAndDeploy(JahiaGWTParameters.getSiteKey(), new BaseAsyncCallback<Object>() {
            public void onSuccess(Object result) {
                linker.loaded();
                Info.display(Messages.get("label.information", "Information"), Messages.get("message.moduleCompiled", "Module compiled"));
            }

            public void onApplicationFailure(Throwable caught) {
                linker.loaded();
                Info.display(Messages.get("label.error", "Error"), Messages.get("message.moduleCompilationFailed", "Module compilation failed"));
                StringBuilder sb = new StringBuilder();
                for (String line : caught.getMessage().split("\n")) {
                    if (line .startsWith("[ERROR]")) {
                        sb.append("<span style=\"color:red\">");
                    }
                    sb.append(line);
                    sb.append("</br>");
                    if (line .startsWith("[ERROR]")) {
                        sb.append("</span>");
                    }
                }
                final Dialog dl = new Dialog();
                dl.setModal(true);
                dl.setHeadingHtml(Messages.get("label.error", "Error"));
                dl.setHideOnButtonClick(true);
                dl.setLayout(new FlowLayout());
                dl.setWidth(500);
                dl.setScrollMode(Style.Scroll.AUTO);
                dl.add(new HTML(sb.toString()));
                dl.setHeight(500);
                dl.show();
            }
        });
    }

    @Override
    public void handleNewLinkerSelection() {
        GWTJahiaNode siteNode = JahiaGWTParameters.getSiteNode();
        if (siteNode.get("j:sourcesFolder") != null) {
            setEnabled(true);
        } else {
            setEnabled(false);
        }
    }

}

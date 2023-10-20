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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

public class WebdavUrlCopyActionItem extends NodeTypeAwareBaseActionItem {
    public void onComponentSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        JahiaContentManagementService.App.getInstance().getAbsolutePath(lh.getSingleSelection().getPath(), new BaseAsyncCallback<String>() {
            public void onApplicationFailure(Throwable throwable) {
                Log.error(throwable.getMessage(), throwable);
                MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
            }

            public void onSuccess(String s) {
                final Dialog dl = new Dialog();
                dl.setHeadingHtml(Messages.get("label.webdav.url"));
                dl.setModal(true);
                dl.setHideOnButtonClick(true);
                dl.setLayout(new HBoxLayout());
                dl.setWidth(500);
                dl.setScrollMode(Style.Scroll.NONE);
                final TextBox textBox = new TextBox();
                textBox.setValue(s);
                textBox.setWidth("460px");
                textBox.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        textBox.selectAll();
                    }
                });
                dl.add(textBox);
                dl.setHeight(102);
                dl.show();
            }
        });
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        setEnabled(lh.getSingleSelection() != null
                && isNodeTypeAllowed(lh.getSingleSelection()));
    }
}

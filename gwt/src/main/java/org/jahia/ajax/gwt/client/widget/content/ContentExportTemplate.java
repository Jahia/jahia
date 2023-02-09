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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 *
 *
 * User: toto
 * Date: Nov 13, 2008 - 7:31:46 PM
 */
public class ContentExportTemplate extends Window {

    public ContentExportTemplate(final Linker linker, final GWTJahiaNode n) {
        this(linker , n.getPath(), n.getName());
    }

    public ContentExportTemplate(final Linker linker, final String path, final String name) {
        super() ;
        addStyleName("content-export-template");
        setHeadingHtml(Messages.get("label.export"));
        setSize(500, 150);
        setResizable(false);
        setId("JahiaGxtContentExportTemplate");
        ButtonBar buttons = new ButtonBar() ;

        setModal(true);

        JahiaContentManagementService.App.getInstance().getExportUrl(path, new BaseAsyncCallback<String>() {

            public void onSuccess(String result) {
                HTML link = new HTML("<br /><a href=\"" + result + ".xml?cleanup=template&root=/"+ "\" target=\"_new\">" + name + ".xml</a>");
                add(link);
                link = new HTML("<br /><a href=\"" + result + ".zip?cleanup=template&root=/"+ "\"  target=\"_new\">" + name + ".zip</a>");
                add(link);
                layout();
            }

            public void onApplicationFailure(Throwable caught) {
                com.google.gwt.user.client.Window.alert(Messages.get("fm_fail") + "\n" + caught.getLocalizedMessage());
                Log.error(Messages.get("fm_fail"), caught);
            }

        });

        Button cancel = new Button(Messages.get("label.cancel"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide() ;
            }
        });
        cancel.addStyleName("button-cancel");
        buttons.add(cancel) ;
        setButtonAlign(Style.HorizontalAlignment.CENTER);
        setBottomComponent(buttons);
    }

}

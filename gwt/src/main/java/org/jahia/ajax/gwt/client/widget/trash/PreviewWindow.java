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
package org.jahia.ajax.gwt.client.widget.trash;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextArea;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

/**
 * Window that displays information about deleted node and its preview
 */
public class PreviewWindow extends Window {
    private LayoutContainer htmlPreview;

    public PreviewWindow(Linker linker, GWTJahiaNode jahiaNode) {
        addStyleName("preview-window");
        setClosable(true);
        setModal(true);
        setLayout(new BorderLayout());
        setSize(800,600);

        LayoutContainer container = new LayoutContainer(new RowLayout());
        HTML label = new HTML("<b>"+Messages.getWithArgs("label.trashedContentPreview", "{0} on {1} said about \"{2}\"" + " : ", new Object[]{jahiaNode.get("j:deletionUser"), jahiaNode.get("j:deletionDate"), jahiaNode.getDisplayName()})+"</b>");

        container.add(label);
        TextArea textArea = new TextArea();
        textArea.setReadOnly(true);
        textArea.setEnabled(false);
        textArea.setValue(jahiaNode.<String>get("j:deletionMessage"));
        textArea.setWidth("95%");
        container.add(textArea);
        container.add(new Text(Messages.get("label.path", "path") + " : " + jahiaNode.getPath()));
        add(container, new BorderLayoutData(Style.LayoutRegion.NORTH, 100));

        htmlPreview = new LayoutContainer();
        htmlPreview.setStyleAttribute("background-color", "white");
        htmlPreview.setScrollMode(Style.Scroll.AUTO);
        add(htmlPreview, new BorderLayoutData(Style.LayoutRegion.CENTER));

        Button button = new Button(Messages.get("label.close", "Close"));
        button.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                hide();
            }
        });
        addButton(button);
        // Use gwt configuration for pages
        String configuration = jahiaNode.isNodeType("jnt:page")?"gwt":"preview";
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put("noDeleteLayer",Arrays.asList("true"));
        JahiaContentManagementService.App.getInstance().getRenderedContent(
                jahiaNode.getPath(), null, JahiaGWTParameters.getLanguage(),
                "default", configuration, params, true, linker.getConfig().getName(),
                null, null, new BaseAsyncCallback<GWTRenderResult>() {
                    public void onSuccess(GWTRenderResult gwtRenderResult) {
                        htmlPreview.removeAll();
                        htmlPreview.add(new HTML(gwtRenderResult.getResult()));
                        htmlPreview.layout();
                    }
                });
    }


}

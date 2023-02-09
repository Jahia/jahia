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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.NodeUsagesGrid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:44:45 PM
 *
 */
public class UsagesTabItem extends EditEngineTabItem {

    public UsagesTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void init(final NodeHolder engine, AsyncTabItem tab, String locale) {
        tab.setLayout(new RowLayout());

        if (engine.getNode() != null) {
            Grid<GWTJahiaNodeUsage> grid = NodeUsagesGrid.createUsageGrid(Arrays.asList(engine.getNode()), JahiaGWTParameters.getBaseUrl());
            Button button = new Button(Messages.get("label.usages.clean"), new SelectionListener<ButtonEvent>() {
                @Override
                public void componentSelected(ButtonEvent ce) {
                    final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
                    service.cleanReferences(engine.getNode().getPath(), new BaseAsyncCallback<Object>() {

                        @Override
                        public void onSuccess(Object result) {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            engine.getLinker().refresh(data);
                        }
                    });
                }
            });
            button.addStyleName("button-clean");
            RowData layoutData1 = new RowData(200, 30);
            layoutData1.setMargins(new Margins(3));
            tab.add(button, layoutData1);
            tab.add(grid, new RowData(1, 1));
            tab.setProcessed(true);
        }
        tab.layout();
    }
}

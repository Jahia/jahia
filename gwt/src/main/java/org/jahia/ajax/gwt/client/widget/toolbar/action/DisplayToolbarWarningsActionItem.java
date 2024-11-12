/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.poller.Poller;
import org.jahia.ajax.gwt.client.widget.poller.ToolbarWarningEvent;

/**
 * toolbar item that display warnings if available
 * Created by david on 21/05/14.
 */
public class DisplayToolbarWarningsActionItem extends BaseActionItem implements Poller.PollListener<ToolbarWarningEvent> {
    private static final long serialVersionUID = 3328698500846922180L;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        refreshMessages();
        Poller.getInstance().registerListener(this, ToolbarWarningEvent.class);
    }

    @Override
    public Component createNewToolItem() {
        Button b = (Button) super.createNewToolItem();
        b.setText(Messages.get("label.notifications","Notifications"));
        b.setEnabled(false);
        return b;
    }

    private void refreshMessages() {
        JahiaContentManagementService.App.getInstance().getToolbarWarnings(new BaseAsyncCallback<String>() {
            @Override
            public void onSuccess(String toolbarWarnings) {
                Button b = (Button) getTextToolItem();
                if (toolbarWarnings != null &&  toolbarWarnings.length() > 0) {
                    String[] messagesTab = toolbarWarnings.split("\\|\\|");
                    final Menu menu = new Menu();
                    menu.addStyleName("toolbar-warning-messages");
                    b.setMenu(menu);
                    for (String s : messagesTab) {
                        MenuItem m = new MenuItem();
                        m.setText(s);
                        menu.add(m);
                    }
                    b.setEnabled(true);
                    b.show();
                } else {
                    b.setEnabled(false);
                    b.hide();
                }
            }
        });
    }

    @Override
    public void handlePollingResult(ToolbarWarningEvent result) {
        refreshMessages();
    }
}

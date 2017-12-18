/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.security;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.content.ContentManager;

/**
 * User: ktlili
 * Date: Feb 3, 2010
 * Time: 10:58:17 AM
 */
public class RolesManager extends LayoutContainer {

    private String config;
    private String siteKey;

    public RolesManager(final String config, final String siteKey) {
        super();
        this.config = config;
        this.siteKey = siteKey != null && siteKey.length() > 0 ? siteKey : null;
    }


    @Override
    protected void onRender(Element parent, int index) {
        super.onRender(parent, index);
        setLayout(new FillLayout());

        JahiaContentManagementService.App.getInstance().getManagerConfiguration(config, new BaseAsyncCallback<GWTManagerConfiguration>() {
            public void onSuccess(GWTManagerConfiguration config) {
                PermissionsUtils.loadPermissions(config.getPermissions());
                final ContentManager cm = new ContentManager(null, null, null, config, 500);

                TabPanel tabPanel = new TabPanel();
                tabPanel.setBorders(false);
                tabPanel.setSize(600, 500);
                TabItem managerItem = new TabItem(Messages.get("label_rolemanager", "Role manager"));
                managerItem.add(cm);
                tabPanel.add(managerItem);
/*
                TabItem rolePermisionItem = new TabItem(Messages.get("label_rolepermissionmapping", "Role/permission mapping"));
                rolePermisionItem.add(pr);
                tabPanel.add(rolePermisionItem);*/


                add(tabPanel);
                layout();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error(throwable.getMessage(), throwable);
            }
        });

    }
}

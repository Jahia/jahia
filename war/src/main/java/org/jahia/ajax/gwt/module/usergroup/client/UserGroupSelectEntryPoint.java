/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.module.usergroup.client;

import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 8, 2009
 * Time: 4:07:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserGroupSelectEntryPoint extends CommonEntryPoint {

    private static UserGroupSelect userGroupSelect;

    public void afterPermissionsLoad() {
        super.afterPermissionsLoad();
        JahiaGWT.init();
        initJavaScriptApi();
    }

    public static void openUserGroupSelect(final String mode, final String id, final String pattern) {
        int viewMode = UserGroupSelect.VIEW_TABS;
        if ("users".equals(mode)) viewMode = UserGroupSelect.VIEW_USERS;
        if ("groups".equals(mode)) viewMode = UserGroupSelect.VIEW_GROUPS;

        UserGroupSelect ug = new UserGroupSelect(new UserGroupAdder() {
            public void addUsers(List<GWTJahiaUser> users) {
                for (GWTJahiaUser user : users) {
                    UserManagerService.App.getInstance().getFormattedPrincipal(user.getUserKey(), 'u', pattern.split("\\|"), new AsyncCallback<String[]>() {
                        public void onFailure(Throwable throwable) {

                        }

                        public void onSuccess(String[] strings) {
                            add(strings[0], strings[1]);
                        }
                    });
                }
            }

            public void addGroups(List<GWTJahiaGroup> groups) {
                for (GWTJahiaGroup group : groups) {
                    UserManagerService.App.getInstance().getFormattedPrincipal(group.getGroupKey(), 'g', pattern.split("\\|"), new AsyncCallback<String[]>() {
                        public void onFailure(Throwable throwable) {

                        }

                        public void onSuccess(String[] strings) {
                            add(strings[0], strings[1]);
                        }
                    });
                }
            }
        },viewMode,"currentSite");
    }


    private native void initJavaScriptApi() /*-{
        // define a static JS function with a friendly name
        $wnd.openUserGroupSelect = function (mode,id,pattern) { @org.jahia.ajax.gwt.module.usergroup.client.UserGroupSelectEntryPoint::openUserGroupSelect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(mode,id,pattern) };
    }-*/;


    public static native void add(String text, String value) /*-{
    try {
        eval('$wnd.addOptions')(text, value);
    } catch (e) {};
    }-*/;

}

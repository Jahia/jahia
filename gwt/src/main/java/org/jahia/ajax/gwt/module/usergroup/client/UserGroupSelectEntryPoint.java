/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.module.usergroup.client;

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
public class UserGroupSelectEntryPoint {

    private static UserGroupSelect userGroupSelect;

    public void onModuleLoad() {
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

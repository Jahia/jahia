/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.module.edit.client;

import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.WorkInProgress;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupAdder;
import org.jahia.ajax.gwt.client.widget.usergroup.UserGroupSelect;

import java.util.List;

/**
 * Edit mode GWT entry point.
 * User: toto
 * Date: Aug 18, 2009
 * Time: 5:53:34 PM
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void onModuleLoad() {
        super.onModuleLoad();
        WorkInProgress.init();
        exposeFunctions();
        checkSession();
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            final String path = DOM.getElementAttribute(panel.getElement(), "path");
            String config = DOM.getElementAttribute(panel.getElement(), "config");
            String hash = Window.Location.getHash();
            if (!hash.equals("")) {
                config = hash.substring(1, hash.indexOf('|'));
            }
            JahiaContentManagementService.App.getInstance().getEditConfiguration(path, config,"default", new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    PermissionsUtils.loadPermissions(gwtEditConfiguration.getPermissions());
                    DOM.setInnerHTML(panel.getElement(), "");
                    panel.add(EditPanelViewport.createInstance(
                            path,
                            DOM.getElementAttribute(panel.getElement(), "template"), 
                            DOM.getElementAttribute(panel.getElement(), "nodetypes"),
                            DOM.getElementAttribute(panel.getElement(), "locale"), gwtEditConfiguration));
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error when loading EditConfiguration", throwable);
                }
            });
        }
    }
    private native void exposeFunctions() /*-{
        $wnd.openUserGroupSelect = function (mode,id,pattern) { @org.jahia.ajax.gwt.module.edit.client.EditEntryPoint::openUserGroupSelect(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(mode,id,pattern) };
        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        $wnd.jahia.alert = function (title, message) {@org.jahia.ajax.gwt.module.edit.client.EditEntryPoint::alert(Ljava/lang/String;Ljava/lang/String;)(title, message); };

    }-*/;

    /**
     * Alert message
     * @param title
     * @param message
     */
    static void alert(String title, String message) {
        MessageBox.alert(title != null ? title : "Info", message, null);
    }


    /**
     * User/group picker
     * @param mode
     * @param id
     * @param pattern
     */
    public static void openUserGroupSelect(final String mode, final String id, final String pattern) {
        int viewMode = UserGroupSelect.VIEW_TABS;
        if ("users".equals(mode)) viewMode = UserGroupSelect.VIEW_USERS;
        if ("groups".equals(mode)) viewMode = UserGroupSelect.VIEW_GROUPS;

        new UserGroupSelect(new UserGroupAdder() {
            public void addUsers(List<GWTJahiaUser> users) {
                for (GWTJahiaUser user : users) {
                    UserManagerService.App.getInstance().getFormattedPrincipal(user.getUserKey(), 'u', pattern.split("\\|"), new BaseAsyncCallback<String[]>() {
                        public void onSuccess(String[] strings) {
                            add(strings[0], strings[1]);
                        }
                    });
                }
            }

            public void addGroups(List<GWTJahiaGroup> groups) {
                for (GWTJahiaGroup group : groups) {
                    UserManagerService.App.getInstance().getFormattedPrincipal(group.getGroupKey(), 'g', pattern.split("\\|"), new BaseAsyncCallback<String[]>() {
                        public void onSuccess(String[] strings) {
                            add(strings[0], strings[1]);
                        }
                    });
                }
            }
        }, viewMode, "currentSite");
    }

    /**
     * Add option
     * @param text
     * @param value
     */
    public static native void add(String text, String value) /*-{
        try {
            eval('$wnd.addOptions')(text, value);
        } catch (e) {};
    }-*/;
}

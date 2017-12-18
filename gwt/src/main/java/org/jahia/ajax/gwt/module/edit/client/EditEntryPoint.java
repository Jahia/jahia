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
package org.jahia.ajax.gwt.module.edit.client;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.CommonEntryPoint;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.WorkInProgress;
import org.jahia.ajax.gwt.client.widget.edit.EditPanelViewport;

/**
 * Edit mode GWT entry point.
 * @author toto
 */
public class EditEntryPoint extends CommonEntryPoint {
    public void onModuleLoad() {
        super.onModuleLoad();
        WorkInProgress.init();
        exposeFunctions();
        checkSession();
        final RootPanel panel = RootPanel.get("editmode");
        if (panel != null) {
            final Element element = panel.getElement();
            final String path = DOM.getElementAttribute(element, "path");
            String config = DOM.getElementAttribute(element, "config");
            String hash = Window.Location.getHash();
            if (!hash.equals("") && hash.contains("|")) {
                config = hash.substring(1, hash.indexOf('|'));
            }
            JahiaContentManagementService.App.getInstance().getEditConfiguration(path, config,"default", new BaseAsyncCallback<GWTEditConfiguration>() {
                public void onSuccess(GWTEditConfiguration gwtEditConfiguration) {
                    PermissionsUtils.loadPermissions(gwtEditConfiguration.getPermissions());
                    DOM.setInnerHTML(element, "");
                    panel.add(EditPanelViewport.createInstance(
                            path,
                            DOM.getElementAttribute(element, "template"),
                            DOM.getElementAttribute(element, "nodetypes"),
                            DOM.getElementAttribute(element, "locale"), gwtEditConfiguration));
                }

                public void onApplicationFailure(Throwable throwable) {
                    Log.error("Error when loading EditConfiguration", throwable);
                    Window.Location.assign(JahiaGWTParameters.getContextPath() + "/errors/error_404.jsp");
                }
            });
        }
    }
    private native void exposeFunctions() /*-{
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

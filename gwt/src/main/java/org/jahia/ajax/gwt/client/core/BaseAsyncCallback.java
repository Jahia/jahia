/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import org.jahia.ajax.gwt.client.widget.LoginBox;

/**
 * Base AsyncCallback class that handles default errors.
 *
 * @param <T> Type of the return value
 */
public abstract class BaseAsyncCallback<T> implements AsyncCallback<T> {

    public void onFailure(Throwable caught) {
        if (caught instanceof SessionExpirationException ||
                (caught instanceof StatusCodeException && ((StatusCodeException) caught).getStatusCode() == 403)) {
            showLogin();
            onSessionExpired();
        } else {
            onApplicationFailure(caught);
        }
    }

    public void onApplicationFailure(Throwable caught) {
        Log.error("Error", caught);
    }

    public void onSessionExpired() {
    }

    public void showLogin() {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
            }

            public void onSuccess() {
                final String loginUrl = CommonEntryPoint.getLoginUrl();
                if (loginUrl != null && !loginUrl.isEmpty()) {
                    Window.Location.assign(loginUrl + (loginUrl.contains("?") ? "&" : "?") + "redirect=" + URL.encodeQueryString(Window.Location.getHref()));
                } else {
                    if (!LoginBox.getInstance().isVisible()) {
                        LoginBox.getInstance().show();

                    }
                }
            }
        });
    }

}

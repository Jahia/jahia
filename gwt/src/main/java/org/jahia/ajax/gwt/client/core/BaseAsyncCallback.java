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

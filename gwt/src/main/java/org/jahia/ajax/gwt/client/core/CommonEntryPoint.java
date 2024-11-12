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
package org.jahia.ajax.gwt.client.core;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.SessionValidationResult;


/**
 * Common entry point for all our custom entry points.
 * Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 *         Date: Dec 16, 2009
 *         Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {

    private static Timer sessionCheckTimer;

    JahiaContentManagementServiceAsync contentManagementService;

    private static String loginUrl;

    public void onModuleLoad() {
        /*
         * Install an UncaughtExceptionHandler which will produce <code>FATAL</code> log messages
         */
        Log.setUncaughtExceptionHandler();
    }

    protected void checkSession() {
        sessionCheckTimer = new Timer() {
            public void run() {
                getContentManagementService().isValidSession(new BaseAsyncCallback<SessionValidationResult>() {
                    @Override
                    public void onSuccess(SessionValidationResult result) {
                        int val = result.getPollingInterval();
                        loginUrl = result.getLoginUrl();
                        if (val > 0) {
                            schedule(val);
                        } else if (val == 0) {
                            cancel();
                            handleSessionExpired(this);
                        }
                    }
                });
            }
        };
        sessionCheckTimer.run();
    }

    protected JahiaContentManagementServiceAsync getContentManagementService() {
        if (contentManagementService == null) {
            contentManagementService = JahiaContentManagementService.App.getInstance();
        }

        return contentManagementService;
    }

    protected void handleSessionExpired(BaseAsyncCallback<?> callback) {
        callback.showLogin();
    }

    public static Timer getSessionCheckTimer() {
        return sessionCheckTimer;
    }

    static String getLoginUrl() {
        return loginUrl;
    }

    /**
     * Utility function to log a message in the console
     * @param message
     */
    public static native void consoleLog(String message) /*-{
        console.log(message);
    }-*/;

    /**
     * Utility function to send an error to the console
     * @param message
     */
    public static native void consoleError(String message) /*-{
        console.error(message);
    }-*/;

}

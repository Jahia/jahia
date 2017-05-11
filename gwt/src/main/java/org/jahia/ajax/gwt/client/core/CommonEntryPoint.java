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

}
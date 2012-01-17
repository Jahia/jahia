/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.core;

import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;

import com.extjs.gxt.ui.client.widget.Layout;
import com.extjs.gxt.ui.client.widget.layout.AnchorLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Timer;


/**
 * Common entry point for all our custom entry points.
 * Use this class to share behavior between all our entry points.
 *
 * @author Serge Huber.
 * Date: Dec 16, 2009
 * Time: 10:39:39 AM
 */
public class CommonEntryPoint implements EntryPoint {

    private static Timer sessionCheckTimer;

    JahiaContentManagementServiceAsync contentManagementService;
    
    public void onModuleLoad() {
        /* todo The following two lines are a hack to get development mode to work on Mac OS X, should be removed once this
           problem is fixed.
         */
        @SuppressWarnings("unused")
        Layout junk = new AnchorLayout();
        /* End of GWT hack */
    }
    
    protected void checkSession() {
        sessionCheckTimer = new Timer() {
            public void run() {
                try {
                    getContentManagementService().isValidSession(new BaseAsyncCallback<Integer>() {
                        public void onSuccess(Integer val) {
                            if (val > 0) {
                               schedule(val);
                            } else if (val == 0) {
                               cancel();
                               handleSessionExpired(this);
                            }
                        }
                    });
                } catch (GWTJahiaServiceException e) {
                    e.printStackTrace();
                }
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

}
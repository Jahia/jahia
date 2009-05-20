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
package org.jahia.ajax.gwt.client.widget.process;

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

/**
 * User: jahia
 * Date: 29 juil. 2008
 * Time: 16:01:34
 */
public class ProcessdisplayBrowserLinker extends BrowserLinker {
    private GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference;

    public ProcessdisplayBrowserLinker(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference, ProcessJobOptionsComponent leftComponent, ProcessJobGrid topRightComponent, ProcessJobMessagesTab bottomRightComponent, ProcessJobTopBar topBar) {
        super(leftComponent, topRightComponent, bottomRightComponent, topBar, null);
        this.gwtJahiaProcessJobPreference = gwtJahiaProcessJobPreference;
    }


    public void refreshPreferenceAndTable() {
        ProcessDisplayService.App.getInstance().getPreferences(new AsyncCallback<GWTJahiaProcessJobPreference>() {
            public void onFailure(Throwable throwable) {
                Log.error("Enable to load preference", throwable);
                refreshTable();
            }

            public void onSuccess(GWTJahiaProcessJobPreference o) {
                gwtJahiaProcessJobPreference = o;
                refreshTable();
            }
        });
    }

    public GWTJahiaProcessJobPreference getGwtJahiaProcessJobPreference() {
        return gwtJahiaProcessJobPreference;
    }

    public void setGwtJahiaProcessJobPreference(GWTJahiaProcessJobPreference gwtJahiaProcessJobPreference) {
        this.gwtJahiaProcessJobPreference = gwtJahiaProcessJobPreference;
    }
}

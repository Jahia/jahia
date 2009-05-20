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

import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.service.JahiaService;
import org.jahia.ajax.gwt.client.widget.ReportGrid;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 19, 2008
 * Time: 12:06:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class JobReport extends Viewport {
    public JobReport(String name, String groupName) {
        super() ;
        setLayout(new FitLayout());

        JahiaService.App.getInstance().getProcessJob(name, groupName, new AsyncCallback<GWTJahiaProcessJob>() {
            public void onFailure(Throwable caught) {
                Log.error("Error when loading process", caught);
            }

            public void onSuccess(GWTJahiaProcessJob job) {
                add(new ReportGrid(job.getActions(), job.getTitleForObjectKey(), true, job.getLogs(), true));
            }
        });

    }

}

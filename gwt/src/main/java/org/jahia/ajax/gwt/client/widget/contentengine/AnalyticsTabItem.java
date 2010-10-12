/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsQuery;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 11:19:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsTabItem extends EditEngineTabItem {
    private static JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private AnalyticsDataVisualizer dataVisualizer;
    private GWTJahiaAnalyticsQuery lastQuery;

    public AnalyticsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.analytics", "Analytics"), engine);
    }

    public AnalyticsTabItem(String title, AbstractContentEngine engine) {
        super(title, engine);
    }

    @Override
    public void create(final String locale) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
                Window.alert("Code download failed");
            }

            public void onSuccess() {
                if (engine.getNode() == null) {
                    return;
                } else {
                    lastQuery = new GWTJahiaAnalyticsQuery();
                    lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:date");
                    lastQuery.setMetrics("ga:pageviews");
                    lastQuery.setSort("-ga:pageviews");
                    //lastQuery.setFilters("ga:pagePath==" + engine.getNode());
                    display();
                }
                layout();
            }
        });
    }

    /**
     * render chart
     *
     */
    private void display() {
        // init data visulaiser
        if (dataVisualizer == null) {
            dataVisualizer = new AnalyticsDataVisualizer() {
                public void oneDateChanged(Date newStartDate, Date newEndDate) {
                    lastQuery.setStartDate(newStartDate);
                    lastQuery.setEndDate(newEndDate);
                    loadData(lastQuery);
                }
                @Override
                public void onTextButtonSelected() {
                    lastQuery.setDimensions("ga:pagePath,ga:source,ga:country,ga:hostname,ga:networkDomain,ga:browser,ga:browserVersion");
                    lastQuery.setMetrics("ga:visits");
                    lastQuery.setSort("-ga:visits");
                    loadData(lastQuery);
                }
                @Override
                public void oneGeoMapSelected() {
                    lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:country,ga:date");
                    loadData(lastQuery);
                }
                @Override
                public void oneAnnotatedTimeLineSelected() {
                    lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:date");
                    loadData(lastQuery);
                }
            };
            add(dataVisualizer);
        }

        // load date
        loadData(lastQuery);

    }

    /**
     * load data
     */
    private void loadData(GWTJahiaAnalyticsQuery query) {
        // get data
        service.getAnalyticsData(query, new BaseAsyncCallback<List<GWTJahiaAnalyticsData>>() {
            public void onSuccess(List<GWTJahiaAnalyticsData> dataList) {
                dataVisualizer.setDataList(dataList);
                dataVisualizer.refreshUI();
            }

            public void onApplicationFailure(Throwable throwable) {
                Log.error("Error while getting analytics data", throwable);
            }
        });
    }


}

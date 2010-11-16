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

package org.jahia.ajax.gwt.helper;

import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsProfile;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsQuery;
import org.jahia.services.analytics.GoogleAnalyticsService;
import org.jahia.services.content.decorator.JCRSiteNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 11:44:38 AM
 * 
 */
public class AnalyticsHelper {
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    final static Logger logger = org.slf4j.LoggerFactory.getLogger(AnalyticsHelper.class);
    private GoogleAnalyticsService analyticsService = GoogleAnalyticsService.getInstance();

    /**
     * Get analytics data by several parameters
     *
     * @return
     */
    public List<GWTJahiaAnalyticsData> queryData(String gaLogin, String gaPwd, String gaAccount, GWTJahiaAnalyticsQuery gwtQuery) {
        List<GWTJahiaAnalyticsData> results = new ArrayList<GWTJahiaAnalyticsData>();
        try {
        DataQuery query = new DataQuery(new URL("https://www.google.com/analytics/feeds/data"));
        query.setDimensions(gwtQuery.getDimensions());
        if (gwtQuery.getMetrics() != null ) { query.setMetrics(gwtQuery.getMetrics()); }
        if (gwtQuery.getSort() != null) { query.setSort(gwtQuery.getSort()); }
        if (gwtQuery.getFilters() != null) { query.setFilters(gwtQuery.getFilters()); } 
        // default start date = this date -3 ont
        if (query.getStartDate() == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -3);
            query.setStartDate(dateFormatter.format(cal.getTime()));
        }

        // default end-date = today
        if (query.getEndDate() == null) {
            Calendar cal = Calendar.getInstance();
            query.setEndDate(dateFormatter.format(cal.getTime()));
        }
        List<DataEntry> dataEntries = analyticsService.queryData(query ,  gaLogin, gaPwd, gaAccount);
        if (dataEntries != null) {
            for (DataEntry entry : dataEntries) {
                GWTJahiaAnalyticsData gwtAnData = new GWTJahiaAnalyticsData();
                for (Dimension dim : entry.getDimensions()) {
                    gwtAnData.set(dim.getName().substring(3),dim.getValue());    
                }
                for (Metric met : entry.getMetrics()) {
                    gwtAnData.set(met.getName().substring(3),met.getValue());
                }
                results.add(gwtAnData);
            }
        }
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return results;
    }

    /**
     * get list of active profiles
     * @param site
     * @return
     */

    public GWTJahiaAnalyticsProfile getProfile(JCRSiteNode site) {
        return (new GWTJahiaAnalyticsProfile(site.getGoogleAnalyticsProfile().getAccount()));
    }
}


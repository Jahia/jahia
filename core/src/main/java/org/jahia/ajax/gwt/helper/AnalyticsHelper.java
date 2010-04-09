package org.jahia.ajax.gwt.helper;

import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.Dimension;
import com.google.gdata.data.analytics.Metric;
import org.apache.log4j.Logger;
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
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsHelper {
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    final static Logger logger = Logger.getLogger(AnalyticsHelper.class);
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


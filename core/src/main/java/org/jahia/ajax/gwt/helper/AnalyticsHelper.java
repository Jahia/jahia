package org.jahia.ajax.gwt.helper;

import com.google.gdata.data.analytics.DataEntry;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsProfile;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsQuery;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.analytics.GoogleAnalyticsService;
import org.jahia.services.content.decorator.JCRSiteNode;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 11:44:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsHelper {
    final static Logger logger = Logger.getLogger(AnalyticsHelper.class);
    private GoogleAnalyticsService analyticsService = GoogleAnalyticsService.getInstance();

    /**
     * Get analytics data by several parameters
     *
     * @param jahiaProfileName
     * @return
     */
    public List<GWTJahiaAnalyticsData> queryData(String jahiaProfileName, String gaLogin, String gaPwd, String gaAccount, GWTJahiaAnalyticsQuery query) {
        List<GWTJahiaAnalyticsData> results = new ArrayList<GWTJahiaAnalyticsData>();
        List<DataEntry> dataEntries = analyticsService.queryData(query.getStartDate(), query.getEndDate(), query.getDimensions(), jahiaProfileName, gaLogin, gaPwd, gaAccount);
        if (dataEntries != null) {
            for (DataEntry entry : dataEntries) {
                String pageTitle = entry.stringValueOf("ga:pageTitle");
                String pagePath = entry.stringValueOf("ga:pagePath");
                String pageViews = entry.stringValueOf("ga:pageviews");
                String viewCountry = entry.stringValueOf("ga:country");
                String viewDateAsStrg = entry.stringValueOf("ga:date");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");
                ParsePosition pos = new ParsePosition(0);
                Date viewDate = null;
                if (viewDateAsStrg != null) {
                    viewDate = formatter.parse(viewDateAsStrg, pos);
                }


                //  if (query.getNode().getPath().equalsIgnoreCase(pagePath)) {
                results.add(new GWTJahiaAnalyticsData(viewCountry, viewDate, Double.parseDouble(pageViews)));
                //  }
                logger.debug(
                        "\nPage Title = " + entry.stringValueOf("ga:pageTitle") +
                                "\nPage Path  = " + entry.stringValueOf("ga:pagePath") +
                                "\nview Country  = " + entry.stringValueOf("ga:country") +
                                "\nview date  = " + viewDate +
                                "\nPageviews  = " + entry.stringValueOf("ga:pageviews"));
            }
        }
        return results;
    }

    /**
     * get list of active profiles
     * @param site
     * @return
     */
    public List<GWTJahiaAnalyticsProfile> getActiveProfiles(JCRSiteNode site) {
        List<GWTJahiaAnalyticsProfile> list = new ArrayList<GWTJahiaAnalyticsProfile>();
        final Iterator<GoogleAnalyticsProfile> googleAnalyticsProfileIterator = site.getGoogleAnalyticsProfile().iterator();
        while (googleAnalyticsProfileIterator.hasNext()) {
            final GoogleAnalyticsProfile googleAnalyticsProfile = googleAnalyticsProfileIterator.next();
            if (googleAnalyticsProfile.isEnabled()) {
                list.add(new GWTJahiaAnalyticsProfile(googleAnalyticsProfile.getName()));
            }
        }
        return list;
    }
}


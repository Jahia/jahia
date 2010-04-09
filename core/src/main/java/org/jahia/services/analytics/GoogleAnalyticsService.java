/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.analytics;

import com.google.gdata.client.analytics.*;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataEntry;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.AuthenticationException;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 23, 2010
 * Time: 5:09:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAnalyticsService {
    final static Logger logger = Logger.getLogger(GoogleAnalyticsService.class);
    // format must be "2006-04-01"
    private AnalyticsService analyticsService;
    private static GoogleAnalyticsService googleAnalyticsService = new GoogleAnalyticsService();

    /**
     * Get instance
     *
     * @return
     */
    public static GoogleAnalyticsService getInstance() {
        if (googleAnalyticsService == null) {
            googleAnalyticsService = new GoogleAnalyticsService();
        }
        return googleAnalyticsService;
    }

    /**
     * check if the credential are valid
     *
     * @param email
     * @param pwd
     * @return
     */
    public boolean checkCredential(String email, String pwd) {
        return getAnalyticsService(email, pwd) != null;
    }


    /**
     * queryData
     *
     * @return
     */
    public List<DataEntry> queryData(DataQuery query, String login, String pwd, String webPropertyId) {
        final AnalyticsService analyticsService = getAnalyticsService(login, pwd);
        final AccountEntry accountEntry = getAccountEntry(webPropertyId);

        if (accountEntry != null) {
            try {
                query.setIds(accountEntry.getTableId().getValue());
                // Make a request to the API, using DataFeed class as the second parameter.
                DataFeed dataFeed = analyticsService.getFeed(query.getUrl(), DataFeed.class);
                // Output data to the screen.
                logger.debug("----------- Data Feed Results ----------");
                for (DataEntry entry : dataFeed.getEntries()) {
                    logger.debug(
                            "\nPage Title = " + entry.stringValueOf("ga:pageTitle") +
                                    "\nPage Path  = " + entry.stringValueOf("ga:pagePath") +
                                    "\nPage county  = " + entry.stringValueOf("ga:country") +
                                    "\nPageviews  = " + entry.stringValueOf("ga:pageviews"));
                }

                return dataFeed.getEntries();
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        logger.debug("AccountEntry is null for parameter ["+ webPropertyId + "]");
        return null;
    }

    /**
     * get ananlytics service
     *
     * @param login
     * @param pwd
     * @return
     */
    private AnalyticsService getAnalyticsService(String login, String pwd) {
        try {
            if (analyticsService == null) {
                analyticsService = connect(login, pwd);
            }
            logger.debug("Credential parameters are valid");
        } catch (AuthenticationException e) {
            logger.debug("Credential parameters are not valid");
            logger.warn(e, e);
        }
        return analyticsService;
    }

    /**
     * Connect to the Analytics service
     *
     * @param login
     * @param pwd
     * @return
     * @throws AuthenticationException
     */
    private AnalyticsService connect(String login, String pwd) throws AuthenticationException {
        AnalyticsService analyticsService = new AnalyticsService("gaExportAPI_acctSample_v1.0");
        analyticsService.setUserCredentials(login, pwd);
        return analyticsService;
    }

    /**
     * Get analytics service by profil name
     *
     * @return
     */
    private AnalyticsService getAnalyticsService() {
        return analyticsService;
    }

    /**
     * Check if user account is a valid one
     *
     * @param webPropertyId
     * @return
     */
    private AccountEntry getAccountEntry(String webPropertyId) {

        if (webPropertyId == null) {
            logger.debug("webPropertyId (with pattern UA-xxxx-x) is null");
            return null;
        }
        boolean isValid = webPropertyId.startsWith("UA-");
        if (isValid) {
            try {
                URL queryUrl = new URL("https://www.google.com/analytics/feeds/accounts/default?max-results=50");

                // Make request to the API, using AccountFeed class as the second parameter.
                AccountFeed accountFeed = getAnalyticsService().getFeed(queryUrl, AccountFeed.class);
                for (AccountEntry entry : accountFeed.getEntries()) {
                    final String currentWebPropertyId = entry.getProperty("ga:webPropertyId");

                    logger.debug("\nWeb property Id  = " + entry.getProperty("ga:webPropertyId")
                            + "\nAccount Name  = " + entry.getProperty("ga:accountName") +
                            "\nProfile Name  = " + entry.getTitle().getPlainText() +
                            "\nProfile Id    = " + entry.getProperty("ga:profileId") +
                            "\nTable Id      = " + entry.getTableId().getValue());

                    if (webPropertyId.equalsIgnoreCase(currentWebPropertyId)) {
                        return entry;
                    }
                 }
            } catch (Exception e) {
                logger.error(e, e);
            }
        } else {
            logger.debug("Account [" + webPropertyId + "] doesn't begin with 'UA-'.");
        }
        return null;
    }

    /**
     * Get node tracking javascript code
     *
     * @param nodes
     * @param site
     * @return
     */
    public String renderNodeTrackingCode(List<JCRNodeWrapper> nodes, JCRSiteNode site) {
        if (nodes == null || nodes.isEmpty()) {
            logger.debug(" There is no tracking nodes");
            return "";
        }
        StringBuffer result = new StringBuffer();
        result.append("<script type=\"text/javascript\">\n");
        result.append("try{\n");
        boolean initTracker = true;
        logger.error("Number of tracked nodes: " + nodes.size());
        for (JCRNodeWrapper node : nodes) {
            result.append(renderNodeTrackingCode(node, site, initTracker));
            initTracker = false;
        }
        result.append("\n} catch(err) {}\n");
        result.append("\n</script>");
        return result.toString();
    }

    /**
     * Render tracking code
     *
     * @return
     */
    private String renderNodeTrackingCode(JCRNodeWrapper node, JCRSiteNode site, boolean initTracker) {

        StringBuffer result = new StringBuffer();


        // get enabled profiles
            GoogleAnalyticsProfile googleAnalyticsProfile = site.getGoogleAnalyticsProfile();

            if (googleAnalyticsProfile.isEnabled()) {
                String jahiaProfileName = googleAnalyticsProfile.getName();
                String trackerName = jahiaProfileName.replaceAll(" ", "_") + "Tracker";

                // create tracker
                String account = googleAnalyticsProfile.getAccount();
                if (initTracker) {
                    String tracker = "var " + trackerName + " = _gat._getTracker('" + account + "');\n";
                    logger.info(tracker);
                    result.append(tracker);
                }

                // add tracked url
                String trackedUrls = googleAnalyticsProfile.getTypeUrl();
                String url = node.getUrl();
                if (trackedUrls.equals("virtual")) {
                    try {
                        url = "'/Unique_Universal_id/" + node.getUUID();
                    } catch (Exception e) {
                        logger.error("Error in gaTrackingCode", e);
                    }
                }
                String trackPageview = trackerName + "._trackPageview('" + url + "');\n";

                result.append(trackPageview);
            }


        return result.toString();
    }

    /**
     * Render mandatory tracking code
     *
     * @return
     */
    public String renderBaseTrackingCode(String protocol) {
        String src = (protocol != null && protocol.equalsIgnoreCase("https")) ? "https://ssl.google-analytics.com/ga.js" : "http://www.google-analytics.com/ga.js";
        return "<script type='text/javascript' src='" + src + "'></script>\n";
    }

    /**
     * get javascript code that allows using google visualisation api
     *
     * @return
     */
    public String renderBaseVisualisationCode() {
        String gviz =
                "<script type='text/javascript' src='http://www.google.com/jsapi'>\n</script>" +
                        "<script type='text/javascript'>" +
                        "google.load('visualization', '1', {packages:['annotatedtimeline','piechart','geomap']});" +
                        "</script>\n";
        return gviz;
    }


}

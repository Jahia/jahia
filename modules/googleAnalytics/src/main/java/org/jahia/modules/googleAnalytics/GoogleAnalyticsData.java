package org.jahia.modules.googleAnalytics;

import com.google.gdata.client.analytics.AnalyticsService;
import com.google.gdata.client.analytics.DataQuery;
import com.google.gdata.data.analytics.AccountEntry;
import com.google.gdata.data.analytics.AccountFeed;
import com.google.gdata.data.analytics.DataFeed;
import com.google.gdata.util.ServiceException;
import org.jahia.utils.EncryptionUtils;

import java.io.IOException;
import java.net.URL;


/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 2/8/11
 * Time: 4:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleAnalyticsData {
    public static DataFeed getData(String login, String password, String query, String webPropertyID) throws Exception {
        AnalyticsService analyticsService = new AnalyticsService("gaExportAPI_acctSample_v1.0");
        analyticsService.setUserCredentials(login, EncryptionUtils.passwordBaseDecrypt(password));
        // Select account
        AccountFeed af = analyticsService.getFeed(new URL("https://www.google.com/analytics/feeds/accounts/default"),AccountFeed.class);
        // lookup for current Account
        for (AccountEntry ac : af.getEntries()) {
            if (webPropertyID.equals(ac.getProperty("ga:webPropertyId"))) {
                query = query.replace("[tableId]",ac.getTableId().getValue());
                break;
            }
        }
        try {
            return analyticsService.getFeed(new DataQuery(new URL(query)), DataFeed.class);
        } catch (IOException e) {
            System.err.println("Network error trying to retrieve feed: " + e.getMessage());
            return null;
        } catch (ServiceException e) {
            System.err.println("Analytics API responded with an error message: " + e.getMessage());
            return null;
        }
    }
}

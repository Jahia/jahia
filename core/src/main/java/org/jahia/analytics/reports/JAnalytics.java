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
package org.jahia.analytics.reports;

import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


import org.jahia.analytics.util.Utilities;
import org.jahia.analytics.util.XpathUtilities;

/**
 * The <code>JAnalytics</code> class is used to login the the Google Analytics
 * service with an email and a password. After a successful login then any of
 * the reporting methods may be invoked. Typical use will include a login
 * followed by obtaining one or more reports. After all required reports are
 * obtained then the <code>JAnalytics</code> should be allowed to go out of
 * scope and a new <code>JAnalytics</code> may be created if more reports are
 * required in the future.
 *
 * @author Dan Andrews
 */
public class JAnalytics {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JAnalytics.class);
    /**
     * The password constant is used as a parameter name to post to the Google
     * login service.
     */
    public static final String PASSWORD_ATTRIBUTE_NAME = "Passwd";

    /**
     * The email constant is used as a parameter name to post to the Google
     * login service.
     */
    public static final String EMAIL_ATTRIBUTE_NAME = "Email";

    /**
     * The filter mode attribute name.
     */
    public static final String FILTER_MODE_ATTRIBUTE_NAME = "qtyp";

    /**
     * The filter attribute name.
     */
    public static final String FILTER_ATTRIBUTE_NAME = "q";

    /**
     * The end date attribute name.
     */
    public static final String END_DATE_ATTRIBUTE_NAME = "ed";

    /**
     * The start date attribute name.
     */
    public static final String START_DATE_ATTRIBUTE_NAME = "bd";

    /**
     * The report id attribute name.
     */
    public static final String REPORT_ID_ATTRIBUTE_NAME = "rid";

    /**
     * The report type attribute name.
     */
    public static final String REPORT_TYPE_ATTRIBUTE_NAME = "vid";

    /**
     * The date format attribute name.
     */
    public static final String DATE_FORMAT_ATTRIBUTE_NAME = "dateFormat";

    /**
     * The row index attribute name.
     */
    public static final String ROW_INDEX_ATTRIBUTE_NAME = "tst";

    /**
     * The row index attribute name.
     */
    public static final String ROW_COUNT_ATTRIBUTE_NAME = "trows";

    /**
     * The default row count. This is the maximum page size the Google Analytics
     * will return.
     */
    public static final int DEFAULT_ROW_COUNT = 500;

    /**
     * The date format attribute name.
     */
    public static final String DEFAULT_USER_DATE_FORMAT = "dd/MM/yyyy";

    /**
     * Select end tag used to determine the end of the profile list.
     */
    private static final String SELECT = "</select>";

    /**
     * Used to determine the start of the profile list.
     */
    private static final String NAME_PROFILE_LIST = "name=\"profile_list\"";

    /**
     * Used to determine the start of the profile list seems to be now changed
     * to "profile"
     */
    private static final String NAME_PROFILE_LIST2 = "id=\"profile\"";

    /**
     * Used to determine the start of a report id value.
     */
    private static final String OPTION_VALUE = "<option value=";

    /**
     * The date format pattern for the report get request.
     */
    public static final String GOOGLE_DATE_PATTERN = "yyyyMMdd";

    /**
     * The <code>DateFormat</code> for the report get request.
     */
    public static final FastDateFormat GOOGLE_DATE_FORMAT = FastDateFormat.getInstance(GOOGLE_DATE_PATTERN);

    /**
     * The <code>HttpClient</code> for the Google Analytics service.
     */
    private HttpClient client;

    /**
     * A list of report ids.
     */
    private List reportIdList = new ArrayList();

    /**
     * The latest HTML-source fetched through the httpclient
     */
    private String latestFetchedSource;

    /**
     * URL of analytics home-screen
     */
    public static final String URL_ANALYTICS_HOME = "https://www.google.com/analytics/home";

    /**
     * URL of "continue" page, post login
     */
    public static final String URL_ANALYTICS_CONTINUE = "https://www.google.com/analytics/settings/?&et=reset&hl=en-US";

    /**
     * URL of analytics login-screen
     */
    public static final String URL_ANALYTICS_LOGIN = "https://www.google.com/accounts/ServiceLoginBoxAuth";

    /**
     * URL of analytics for export
     */
    public static final String URL_ANALYTICS_REPORTING_EXPORT = "https://www.google.com/analytics/reporting/export";

    /**
     * URL of analytics select account-screen, the slash at the end is important
     */
    public static final String URL_ANALYTICS_SETTINGS_HOME = "https://www.google.com/analytics/reporting/";

    /**
     * URL of webmastertools dashboard-screen
     */
    public static final String URL_WEBMASTER_TOOLS_DASHBOARD = "https://www.google.com/webmasters/tools/dashboard";

    /**
     * URL of webmastertools download querystats
     */
    public static final String URL_WEBMASTER_TOOLS_QUERYSTATSDL = "https://www.google.com/webmasters/tools/querystatsdl";

    public String msg = "";

    /**
     * Constructs a new <code>JAnalytics</code> object.
     */
    public JAnalytics() {
    }

    /**
     * The login method must be invoked prior to invoking any of the reporting
     * methods.
     *
     * @param email    The a valid Google Analytics email account.
     * @param password The corresponding Google Analytics email password.
     * @throws JAnalyticsException
     */
    public void login(String email, String password) throws JAnalyticsException {
        login(email, password, Languages.ENGLISH_US);
    }

    /**
     * This method is currently private and only ENGLISH_US logins are allowed
     * for now.
     *
     * @param email    The a valid Google Analytics email account.
     * @param password The corresponding Google Analytics email password.
     * @throws JAnalyticsException
     */
    private void login(String email, String password, Languages language)
            throws JAnalyticsException {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null.");
        }
        if (language == null) {
            throw new IllegalArgumentException("Language cannot be null.");
        }

        if (client == null) {
            client = new HttpClient();
        }
        client.getHostConfiguration().setHost("www.google.com", 443, "https");
        // client.getHostConfiguration().setProxy("localhost", 8888);

        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        // Prepare login parameters
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        // New login parameters, post 1.01
        list.add(new NameValuePair("continue", URL_ANALYTICS_CONTINUE));
        list.add(new NameValuePair("service", "analytics"));
        list.add(new NameValuePair("nui", "1"));
        list.add(new NameValuePair("hl", "en-US"));
        list.add(new NameValuePair("GA3T", "8Lz5P-NaGZ4"));
        list.add(new NameValuePair(EMAIL_ATTRIBUTE_NAME, email));
        list.add(new NameValuePair("rmShown", "1"));
        list.add(new NameValuePair(PASSWORD_ATTRIBUTE_NAME, password));

        NameValuePair[] nameValuePairs = new NameValuePair[list.size()];
        for (int i = 0; i < list.size(); i++) {
            nameValuePairs[i] = list.get(i);
        }

        PostMethod postMethod = new PostMethod(URL_ANALYTICS_LOGIN);
        postMethod.setRequestBody(nameValuePairs);

        try {
            client.executeMethod(postMethod);
            int statuscode = postMethod.getStatusCode();
            //logger.info("status code "+statuscode);
            if (statuscode == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(postMethod
                                .getResponseBodyAsStream()));
                StringBuffer buf = new StringBuffer();
                for (String line = reader.readLine(); line != null; line = reader
                        .readLine()) {
                }
                postMethod.releaseConnection();

                GetMethod redirect = new GetMethod(URL_ANALYTICS_HOME);

                client.executeMethod(redirect);
                if (statuscode == HttpStatus.SC_OK) {
                    reader = new BufferedReader(new InputStreamReader(redirect
                            .getResponseBodyAsStream()));
                    buf = new StringBuffer();
                    for (String line = reader.readLine(); line != null; line = reader
                            .readLine()) {
                        buf.append(line);
                    }
                    redirect.releaseConnection();
                    latestFetchedSource = buf.toString();
                }
            }
        } catch (IOException e) {
            throw new JAnalyticsException("Login Failure: " + e.getMessage(), e);
        }

    }


    // Customized methods for jahia needs
    //###################################################################################################################
    // 17 decembre 2008
    public InputStream downloadReport(ReportParams rp) {
        InputStream in = null;

        try {
            in = new ByteArrayInputStream(getReportStringJahiaCustom(rp)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return in;
    }

    private String getReportStringJahiaCustom(ReportParams rp) {
        //logger.info("getReportType"+rp.getReportType());
        //logger.info("getUrl "+rp.getUrl());

        // Prepare request parameters
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair("fmt", "1"));
        list.add(new NameValuePair("id", rp.getReportId()));
        list.add(new NameValuePair("pdr", rp.getDateRange()));

        // Customized parameters
        if (!rp.getUrl().equals("site")) list.add(new NameValuePair("d1", rp.getUrl()));
        if (rp.getGeoMapStatType() != -1) list.add(new NameValuePair("midx", "" + rp.getGeoMapStatType()));


        list.add(new NameValuePair("cmp", "average"));
        list.add(new NameValuePair("rpt", rp.getReportType()
                .getReportAttributeValue()));
        list.add(new NameValuePair(ROW_INDEX_ATTRIBUTE_NAME, String
                .valueOf(rp.getRowIndex())));
        list.add(new NameValuePair(ROW_COUNT_ATTRIBUTE_NAME, String
                .valueOf(rp.getRowCount())));
        if (rp.getFilter() != null && rp.getFilterMode() != FilterModes.NONE) {
            list.add(new NameValuePair(FILTER_ATTRIBUTE_NAME, rp.getFilter()));
            list.add(new NameValuePair(FILTER_MODE_ATTRIBUTE_NAME, rp.getFilterMode()
                    .getValueAsString()));
        }

        NameValuePair[] drillDownPairs = rp.getReportType().getDrillDownParams();

        // fill in for request
        NameValuePair[] nameValuePairs = new NameValuePair[list.size() + drillDownPairs.length];
        System.arraycopy(list.toArray(new NameValuePair[0]), 0, nameValuePairs, 0, list.size());

        if (drillDownPairs.length > 0) {
            System.arraycopy(drillDownPairs, 0, nameValuePairs, list.size(), drillDownPairs.length);
        }

        GetMethod getMethod = new GetMethod(URL_ANALYTICS_REPORTING_EXPORT);
        getMethod.setQueryString(nameValuePairs);
        String reportString = null;
        try {
            client.executeMethod(getMethod);

            int statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                reportString = getMethod.getResponseBodyAsString();

                latestFetchedSource = reportString;
            } else {
                throw new JAnalyticsException(
                        "Exception while getting report: http status code is "
                                + statuscode);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JAnalyticsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return reportString;
    }


    // for jahia 12 decembre 2008
    public InputStream getReportInputStreamForTheCurrentSite(String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount, String siteORpage) {
        InputStream in = null;

        try {
            in = new ByteArrayInputStream(getReportStringForTheGivenRange(reportId, reportType, dateRange, filter, filterMode, rowIndex, rowCount, siteORpage)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return in;
    }

    // for jahia 12 decembre 2008
    public InputStream getReportInputStreamForTheCurrentPage(String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount, String siteORpage) {
        InputStream in = null;

        try {
            in = new ByteArrayInputStream(getReportStringForTheGivenRange(reportId, reportType, dateRange, filter, filterMode, rowIndex, rowCount, siteORpage)
                    .getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return in;
    }

    // for jahia 12 decembre 2008
    private String getReportStringForTheGivenRange(String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount, String siteORpage) {
// Prepare request parameters
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair("fmt", "1"));
        list.add(new NameValuePair("id", reportId));
        list.add(new NameValuePair("pdr", dateRange));
        if (!siteORpage.equals("site")) list.add(new NameValuePair("d1", siteORpage));
        list.add(new NameValuePair("cmp", "average"));
        list.add(new NameValuePair("rpt", reportType
                .getReportAttributeValue()));
        list.add(new NameValuePair(ROW_INDEX_ATTRIBUTE_NAME, String
                .valueOf(rowIndex)));
        list.add(new NameValuePair(ROW_COUNT_ATTRIBUTE_NAME, String
                .valueOf(rowCount)));
        if (filter != null && filterMode != FilterModes.NONE) {
            list.add(new NameValuePair(FILTER_ATTRIBUTE_NAME, filter));
            list.add(new NameValuePair(FILTER_MODE_ATTRIBUTE_NAME, filterMode
                    .getValueAsString()));
        }

        NameValuePair[] drillDownPairs = reportType.getDrillDownParams();

        // fill in for request
        NameValuePair[] nameValuePairs = new NameValuePair[list.size() + drillDownPairs.length];
        System.arraycopy(list.toArray(new NameValuePair[0]), 0, nameValuePairs, 0, list.size());

        if (drillDownPairs.length > 0) {
            System.arraycopy(drillDownPairs, 0, nameValuePairs, list.size(), drillDownPairs.length);
        }

        GetMethod getMethod = new GetMethod(URL_ANALYTICS_REPORTING_EXPORT);
        getMethod.setQueryString(nameValuePairs);

        String reportString = null;

        try {
            client.executeMethod(getMethod);

            int statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                reportString = getMethod.getResponseBodyAsString();

                latestFetchedSource = reportString;
            } else {
                throw new JAnalyticsException(
                        "Exception while getting report: http status code is "
                                + statuscode);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JAnalyticsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }

        return reportString;
    }
    //###################################################################################################################

    /**
     * Getting information about analytic-accounts
     *
     * @return a dictionary-structure with the accountIds as keys and the accountNames as values
     */
    public LinkedHashMap<String, String> getAccounts() throws Exception {
        return extractedAccountsFromDom(XpathUtilities.getDomForSource(getLatestFetchedSource()));
    }

    /**
     * Getting information about analytic-profiles for a certain selected account
     *
     * @return a dictionary-structure with the profileIds as keys and the profileNames as values
     */
    public LinkedHashMap<String, String> getProfiles() throws Exception {
        return extractedProfilesFromDom(XpathUtilities.getDomForSource(getLatestFetchedSource()));
    }

    /**
     * Getting all websites registered in the webmastertools-account
     *
     * @return a dictionary-structure with the websiteUrls as keys and the websiteNames as values
     */
    public LinkedHashMap<String, String> getWebsites() throws Exception {
        return extractedWebsitesFromDom(XpathUtilities.getDomForSource(getLatestFetchedSource()));
    }

    private LinkedHashMap<String, String> getLinkedHashMapForXpathsFromDom(String keyXpath, String valueXpath, Document dom) throws Exception {
        ArrayList<Node> keyNodes = XpathUtilities.getNodesForXpathInDOM(keyXpath, dom);
        ArrayList<Node> valueNodes = XpathUtilities.getNodesForXpathInDOM(valueXpath, dom);
        String key, value = null;
        int min = Math.min(keyNodes.size(), valueNodes.size());
        LinkedHashMap<String, String> dict = new LinkedHashMap<String, String>();
        for (int i = 0; i < min; i++) {
            value = XpathUtilities.getContentFromNode(keyNodes.get(i));
            key = XpathUtilities.getContentFromNode(valueNodes.get(i));
            if (!key.equalsIgnoreCase("0") && !key.equalsIgnoreCase("-1") && !key.equalsIgnoreCase("")) {
                dict.put(key, value);
            }
        }
        return dict;
    }

    private LinkedHashMap<String, String> extractedWebsitesFromDom(Document dom) throws Exception {
        // String xpathAccountNodes = "//select[@id='account']/option";
        String xpathWebsiteTitles = "//td/a/text()";
        String xpathWebsiteIds = "//td/a/@href";
        LinkedHashMap<String, String> lmap = getLinkedHashMapForXpathsFromDom(xpathWebsiteTitles, xpathWebsiteIds, dom);
        LinkedHashMap<String, String> cleanedMap = new LinkedHashMap<String, String>();
        // filter out those not needed
        String currentValue = null;
        for (String currentKey : lmap.keySet()) {
            currentValue = lmap.get(currentKey);
            if (currentKey != null && currentKey.startsWith("showaddsitemap?")) { // these are the urls needed
                currentKey = URLDecoder.decode(currentKey, "UTF-8");
                currentKey = currentKey.substring("showaddsitemap?".length(), currentKey.length());
                currentKey = currentKey.substring(currentKey.indexOf("siteUrl=") + "siteUrl=".length(), currentKey.indexOf("&"));
                cleanedMap.put(currentKey, currentValue);
            }
        }
        return cleanedMap;
    }


    private LinkedHashMap<String, String> extractedAccountsFromDom(Document dom) throws Exception {
        // String xpathAccountNodes = "//select[@id='account']/option";
        String xpathAccountTitles = "//select[@id='account']/option/text()";
        String xpathAccountIds = "//select[@id='account']/option/@value";
        return getLinkedHashMapForXpathsFromDom(xpathAccountTitles, xpathAccountIds, dom);
    }

    private LinkedHashMap<String, String> extractedProfilesFromDom(Document dom) throws Exception {
        //String xpathProfileNodes = "//select[@id='profile']/option";
        String xpathProfileTitles = "//select[@id='profile']/option/text()";
        String xpathProfileIds = "//select[@id='profile']/option/@value";
        return getLinkedHashMapForXpathsFromDom(xpathProfileTitles, xpathProfileIds, dom);
    }


    public LinkedHashMap<String, String> getProfilesForAccountWithId(String accountId) throws Exception {

        NameValuePair[] nameValuePairs = new NameValuePair[3];
        nameValuePairs[0] = new NameValuePair("scid", accountId);
        nameValuePairs[1] = new NameValuePair("ns", "100"); // number of profiles shown in list (100 is maximum)

        /** The following parameter may be set dynamicaly in more advanced releases of this API to make paged fetching available */
        nameValuePairs[2] = new NameValuePair("sn", "1"); // start list with number of entry (start with entry 1)

        GetMethod getMethod = new GetMethod(URL_ANALYTICS_SETTINGS_HOME);
        getMethod.setQueryString(nameValuePairs);

        try {
            client.executeMethod(getMethod);
            int statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                latestFetchedSource = getMethod.getResponseBodyAsString();

            } else {
                throw new JAnalyticsException(
                        "Exception while getting profiles for account: http status code is "
                                + statuscode);
            }

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting profiles for account: "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return getProfiles();
    }

    /**
     * Getting CSV-Data for a certain site of the webmastertools-account
     *
     * @param siteUrl the url which comes from the dictionary structure you got via @see {@link #getWebsites()}
     * @return a string which is the CSV of all querystats-data you could get for this site
     */
    public String getWebmasterToolsQueryStatsForSiteUrlAsCSV(String siteUrl) throws Exception {

        NameValuePair[] nameValuePairs = new NameValuePair[2];
        nameValuePairs[0] = new NameValuePair("siteUrl", siteUrl);
        nameValuePairs[1] = new NameValuePair("hl", "en");

        GetMethod getMethod = new GetMethod(URL_WEBMASTER_TOOLS_QUERYSTATSDL);
        getMethod.setFollowRedirects(true);

        getMethod.setQueryString(nameValuePairs);

        int statuscode = 0;
        try {
            client.executeMethod(getMethod);
            statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");


                BufferedReader br = null;
                InputStreamReader isr = null;
                StringBuffer buf = null;

                isr = new InputStreamReader(getMethod.getResponseBodyAsStream(), getMethod.getResponseCharSet());
                br = new BufferedReader(isr);
                buf = new StringBuffer();
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    buf.append(line);
                }
                latestFetchedSource = buf.toString();

            } else {
                throw new JAnalyticsException(
                        "Exception while downloading querystats as CSV: http status code is "
                                + statuscode);
            }

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while downloading querystats as CSV for selected siteUrl: "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return latestFetchedSource;
    }

    /**
     * Extracts the keywords and number of hits which were registered in the querystatistics in a dictionary-like structure
     * which contains the (over all countries) aggregated total of hits for each keyword
     */
    public static LinkedHashMap<String, String> getKeywordHitsFromFetchedQueryStats(String queryStatsAsCSV) {
        return extractedKeywordHitsFromFetchedQueryStats(queryStatsAsCSV);
    }

    private static LinkedHashMap<String, String> extractedKeywordHitsFromFetchedQueryStats(String queryStatsAsCSV) {
        if (queryStatsAsCSV == null) return new LinkedHashMap<String, String>();
        queryStatsAsCSV = queryStatsAsCSV.replaceAll("\"\"", "\"");
        LinkedHashMap<String, String> keywordsMap = new LinkedHashMap<String, String>();
        Pattern regexPattern = Pattern.compile("\\[(\\w|\\,| |%)*\\]");
        Matcher matcher = regexPattern.matcher((CharSequence) queryStatsAsCSV);
        String foundString = null;
        ArrayList<String> listOfMatches = new ArrayList<String>();
        while (matcher.find()) {
            foundString = matcher.group();
            listOfMatches.add(foundString);
        }

        String keyword = null;
        String key, value, existingValue = null;
        int parsedValue = 0;
        for (String currentToken : listOfMatches) {
            keyword = currentToken.substring(1, currentToken.indexOf(","));
            value = currentToken.substring(currentToken.lastIndexOf(",") + 1, currentToken.length() - 1);
            value = value.trim();
            key = keyword.toLowerCase();
            existingValue = (String) keywordsMap.get(key);
            parsedValue = 0;
            if (existingValue != null) {
                int aggregation = Integer.parseInt(existingValue);
                try {
                    parsedValue = Integer.parseInt(value);
                }
                catch (Exception ex) {
                    // do nothing here
                    parsedValue = 0;
                }
                aggregation += parsedValue;
                keywordsMap.put(key, "" + aggregation);
            } else {
                keywordsMap.put(key, value);
            }
        }
        return keywordsMap;
    }

    /**
     * Getting all websites registered in the webmastertools-account
     *
     * @return a dictionary-structure with the websiteUrls as keys and the websiteNames as values
     */
    public LinkedHashMap<String, String> getWebmasterToolsWebsites() throws Exception {
        NameValuePair[] nameValuePairs = new NameValuePair[1];
        nameValuePairs[0] = new NameValuePair("pli", "1");

        GetMethod getMethod = new GetMethod(URL_WEBMASTER_TOOLS_DASHBOARD);
        getMethod.setFollowRedirects(true);

        getMethod.setQueryString(nameValuePairs);

        int statuscode = 0;
        try {
            client.executeMethod(getMethod);
            statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                latestFetchedSource = getMethod.getResponseBodyAsString();

            } else {
                throw new JAnalyticsException(
                        "Exception while getting webmastertools: http status code is "
                                + statuscode);
            }

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting webmastertools for account: "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        // check possible redirect
        Document dom = XpathUtilities.getDomForSource(latestFetchedSource);
        String redirectUrl = XpathUtilities.getContentForXpathInDOM("//meta[@http-equiv='refresh']/@content", dom);

        if (redirectUrl != null && redirectUrl.length() > 0) { // do we need to redirect?
            redirectUrl = redirectUrl.substring(redirectUrl.indexOf("url='") + "url='".length(), redirectUrl.lastIndexOf("'"));

            statuscode = -1;
            getMethod = new GetMethod(redirectUrl);

            try {
                client.executeMethod(getMethod);
                statuscode = getMethod.getStatusCode();
                if (statuscode == HttpStatus.SC_OK) {
                    getMethod.setRequestHeader("Content-Type", "UTF-8");
                    getMethod.setRequestHeader("Content-Length", "5");

                    latestFetchedSource = getMethod.getResponseBodyAsString();

                } else {
                    throw new JAnalyticsException(
                            "Exception while getting webmastertools after following redirect: http status code is "
                                    + statuscode);
                }

            } catch (IOException e) {
                throw new JAnalyticsException("Exception while getting webmastertools after following redirect for account: "
                        + e.getMessage(), e);
            } finally {
                if (getMethod != null) {
                    getMethod.releaseConnection();
                }
            }
        } // end if
        return getWebsites();
    }


    /**
     * Gets the array of report ids.
     *
     * @return The array of report ids.
     */
    public String[] getReportIds() {
        String[] array = null;
        try {
            int i = 0;
            array = new String[getProfiles().size()];
            for (String currentKey : getProfiles().keySet()) {
                array[i] = currentKey;
                i++;
            }
        }
        catch (Exception ex) {

        }
        return array;
    }

    /**
     * Gets the given report as an indented xml string.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @return The report as an indented xml string.
     * @throws JAnalyticsException
     */
    public String getReport(String reportId, ReportTypes reportType,
                            Calendar startTime, Calendar endTime) throws JAnalyticsException {
        return getReport(reportId, reportType, startTime, endTime, null,
                FilterModes.NONE, 0, DEFAULT_ROW_COUNT);
    }/**/

    /**
     * Gets the given report as an indented xml string.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @param filter     A named filter or null if none.
     * @param filterMode The filter mode.
     * @param rowIndex   The zero-based row index in the report dataset to start the
     *                   export at.
     * @param rowCount   The number of dataset rows to return. Google Analytics won't
     *                   return more than 500 rows, setting this higher than 500 will
     *                   still give you 500 rows.
     * @return The report as an indented xml string.
     * @throws JAnalyticsException
     */
    public String getReport(String reportId, ReportTypes reportType,
                            Calendar startTime, Calendar endTime, String filter,
                            FilterModes filterMode, int rowIndex, int rowCount)
            throws JAnalyticsException {
        return getReportString(reportId, reportType, startTime, endTime,
                filter, filterMode, rowIndex, rowCount);
    }  /*  */

    /**
     * Gets the given report as an xml <code>Document</code> object.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @return The xml <code>Document</code> object.
     * @throws JAnalyticsException
     * @deprecated Not much point in returning a Document if we aren't
     *             supporting any useful transformations.
     */
    public Document getReportDocument(String reportId, ReportTypes reportType,
                                      Calendar startTime, Calendar endTime) throws JAnalyticsException {
        return getReportDocument(reportId, reportType, startTime, endTime,
                null, FilterModes.NONE, 0, DEFAULT_ROW_COUNT);
    }

    /*  */
    /**
     * Gets the given report as an xml <code>Document</code> object.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @param filter     A named filter or null if none.
     * @param filterMode The filter mode.
     * @param rowIndex   The zero-based row index in the report dataset to start the
     *                   export at.
     * @param rowCount   The number of dataset rows to return. Google Analytics won't
     *                   return more than 500 rows, setting this higher than 500 will
     *                   still give you 500 rows.
     * @return The xml <code>Document</code> object.
     * @throws JAnalyticsException
     * @deprecated Not much point in returning a Document if we aren't
     *             supporting any useful transformations.
     */
    public Document getReportDocument(String reportId, ReportTypes reportType,
                                      Calendar startTime, Calendar endTime, String filter,
                                      FilterModes filterMode, int rowIndex, int rowCount)
            throws JAnalyticsException {
        Document document = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            document = builder
                    .parse(getReportInputStream(reportId, reportType,
                            startTime, endTime, filter, filterMode, rowIndex,
                            rowCount));

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } catch (SAXException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        }
        return document;
    }
     /**/
    /**
     * Gets the given report as an <code>InputStream</code> object.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @return The <code>InputStream</code> object.
     * @throws JAnalyticsException
     */
    public InputStream getReportInputStream(String reportId,
                                            ReportTypes reportType, Calendar startTime, Calendar endTime)
            throws JAnalyticsException {
        return getReportInputStream(reportId, reportType, startTime, endTime,
                null, FilterModes.NONE, 0, DEFAULT_ROW_COUNT);
    }
     /**/
    /**
     * Gets the given report as an <code>InputStream</code> object.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @param filter     A named filter or null if none.
     * @param filterMode The filter mode.
     * @param rowIndex   The zero-based row index in the report dataset to start the
     *                   export at.
     * @param rowCount   The number of dataset rows to return. Google Analytics won't
     *                   return more than 500 rows, setting this higher than 500 will
     *                   still give you 500 rows.
     * @return The <code>InputStream</code> object.
     * @throws JAnalyticsException
     */
    public String getReportString(String reportId, ReportTypes reportType,
                                  Calendar startTime, Calendar endTime, String filter,
                                  FilterModes filterMode, int rowIndex, int rowCount)
            throws JAnalyticsException {
        startTime = Utilities.trimCalendar(startTime);
        endTime = Utilities.trimCalendar(endTime);


        // Prepare request parameters
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair("fmt", "1"));
        list.add(new NameValuePair("id", reportId));
        list.add(new NameValuePair("pdr", Utilities
                .calendarToString(startTime)
                + "-" + Utilities.calendarToString(endTime)));
        list.add(new NameValuePair("cmp", "average"));
        list.add(new NameValuePair("rpt", reportType
                .getReportAttributeValue()));
        list.add(new NameValuePair(ROW_INDEX_ATTRIBUTE_NAME, String
                .valueOf(rowIndex)));
        list.add(new NameValuePair(ROW_COUNT_ATTRIBUTE_NAME, String
                .valueOf(rowCount)));
        if (filter != null && filterMode != FilterModes.NONE) {
            list.add(new NameValuePair(FILTER_ATTRIBUTE_NAME, filter));
            list.add(new NameValuePair(FILTER_MODE_ATTRIBUTE_NAME, filterMode
                    .getValueAsString()));
        }

        NameValuePair[] drillDownPairs = reportType.getDrillDownParams();

        // fill in for request
        NameValuePair[] nameValuePairs = new NameValuePair[list.size() + drillDownPairs.length];
        System.arraycopy(list.toArray(new NameValuePair[0]), 0, nameValuePairs, 0, list.size());

        if (drillDownPairs.length > 0) {
            System.arraycopy(drillDownPairs, 0, nameValuePairs, list.size(), drillDownPairs.length);
        }

        GetMethod getMethod = new GetMethod(URL_ANALYTICS_REPORTING_EXPORT);
        getMethod.setQueryString(nameValuePairs);

        String reportString = null;
        try {
            client.executeMethod(getMethod);
            int statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                reportString = getMethod.getResponseBodyAsString();

                latestFetchedSource = reportString;
            } else {
                throw new JAnalyticsException(
                        "Exception while getting report: http status code is "
                                + statuscode);
            }

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return reportString;
    }

    /*	 */
    /**
     * Gets the given report as an <code>InputStream</code> object.
     *
     * @param reportId   The report id that must be one of the values returned by the
     *                   <code>getReportIds</code> method.
     * @param reportType The report type which cannot be null.
     * @param startTime  The start time which cannot be null.
     * @param endTime    The end time which cannot be null and must be equal to or
     *                   later than the start time.
     * @param filter     A named filter or null if none.
     * @param filterMode The filter mode.
     * @param rowIndex   The zero-based row index in the report dataset to start the
     *                   export at.
     * @param rowCount   The number of dataset rows to return. Google Analytics won't
     *                   return more than 500 rows, setting this higher than 500 will
     *                   still give you 500 rows.
     * @return The <code>InputStream</code> object.
     * @throws JAnalyticsException
     */
    public InputStream getReportInputStream(String reportId,
                                            ReportTypes reportType, Calendar startTime, Calendar endTime,
                                            String filter, FilterModes filterMode, int rowIndex, int rowCount)
            throws JAnalyticsException {
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(getReportString(reportId, reportType,
                    startTime, endTime, filter, filterMode, rowIndex, rowCount)
                    .getBytes("UTF-8"));

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } finally {
        }
        return in;
    }/**/
    /**
     * Gets the latest fetched source returned from the internal HttpClient.
     *
     * @return the latest fetched source
     */
    public String getLatestFetchedSource() {
        return latestFetchedSource;
    }

    /*NEW*/
    public InputStream getReportInputStream(String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount) throws JAnalyticsException {

        InputStream in = null;
        try {
            in = new ByteArrayInputStream(getReportString(reportId, reportType, dateRange, filter, filterMode, rowIndex, rowCount)
                    .getBytes("UTF-8"));

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } finally {
        }
        return in;
    }

    /*NEW*/
    private String getReportString(String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount) throws JAnalyticsException {

        // Prepare request parameters
        ArrayList<NameValuePair> list = new ArrayList<NameValuePair>();
        list.add(new NameValuePair("fmt", "1"));
        list.add(new NameValuePair("id", reportId));
        list.add(new NameValuePair("pdr", dateRange));
        list.add(new NameValuePair("cmp", "average"));
        list.add(new NameValuePair("rpt", reportType
                .getReportAttributeValue()));
        list.add(new NameValuePair(ROW_INDEX_ATTRIBUTE_NAME, String
                .valueOf(rowIndex)));
        list.add(new NameValuePair(ROW_COUNT_ATTRIBUTE_NAME, String
                .valueOf(rowCount)));
        if (filter != null && filterMode != FilterModes.NONE) {
            list.add(new NameValuePair(FILTER_ATTRIBUTE_NAME, filter));
            list.add(new NameValuePair(FILTER_MODE_ATTRIBUTE_NAME, filterMode
                    .getValueAsString()));
        }

        NameValuePair[] drillDownPairs = reportType.getDrillDownParams();

        // fill in for request
        NameValuePair[] nameValuePairs = new NameValuePair[list.size() + drillDownPairs.length];
        System.arraycopy(list.toArray(new NameValuePair[0]), 0, nameValuePairs, 0, list.size());

        if (drillDownPairs.length > 0) {
            System.arraycopy(drillDownPairs, 0, nameValuePairs, list.size(), drillDownPairs.length);
        }

        GetMethod getMethod = new GetMethod(URL_ANALYTICS_REPORTING_EXPORT);
        getMethod.setQueryString(nameValuePairs);

        String reportString = null;
        try {
            client.executeMethod(getMethod);
            int statuscode = getMethod.getStatusCode();
            if (statuscode == HttpStatus.SC_OK) {
                getMethod.setRequestHeader("Content-Type", "UTF-8");
                getMethod.setRequestHeader("Content-Length", "5");

                reportString = getMethod.getResponseBodyAsString();

                latestFetchedSource = reportString;
            } else {
                throw new JAnalyticsException(
                        "Exception while getting report: http status code is "
                                + statuscode);
            }

        } catch (IOException e) {
            throw new JAnalyticsException("Exception while getting report: "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return reportString;
    }

    public boolean gmailCredentialsAreValids() {
        CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
        Cookie[] logoncookies = cookiespec.match(
                "www.google.com", 443, "/", false,
                client.getState().getCookies());
        if (logoncookies.length == 0) {
            //logger.info(" NO COOKIE");
            return false;
        } else {
            /*for (int i = 0; i < logoncookies.length; i++) {
                logger.info("- " + logoncookies[i].toString());
            }*/
            return true;
        }
    }

}

/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.analytics.data;

import org.jahia.analytics.reports.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.apache.commons.jxpath.JXPathContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 *
 * @author ielghandour
 *         Date: 10 d�c. 2008
 *         Time: 17:12:14
 *         To change this template use File | Settings | File Templates.
 */
public class GAdataCollector {
    HashMap entireRangeDatesPositions = new HashMap();
    HashMap activeValuesPositions = new HashMap();
    HashMap statisticType = new HashMap();
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GAdataCollector.class);

    public GAdataCollector() {
        // to customize the midx parameter of the http request for the geo map report  // ex: midx = 0 for the Visits
        getStatisticTypes();

    }


    public Map<String, String> getData(String gmailAccount, String gmailPassword, String profile, String gaAccount, String dateRange, String statType, String chartType, String siteORpage) {
        Map<String, String> data = new HashMap<String, String>();
        JAnalytics ja = new JAnalytics();    //todo check first if the login is successful
        try {
            //logger.info("getData :: before logging in");
            ja.login(gmailAccount, gmailPassword);
            ////logger.info("getData :: logged in");
            String reportId = getReportIdFromProfile(ja.getProfilesForAccountWithId(gaAccount), profile);

            if (siteORpage.equals("site")) {
                if (chartType.equals("GeoMap")) {
                    //logger.info("download GEO_MAP_REPORT");
                    InputStream is = downloadDataForGeoMap(ja, reportId, ReportTypes.GEO_MAP_REPORT, dateRange, null, FilterModes.MATCH, 0, ja.DEFAULT_ROW_COUNT, siteORpage, (String) statisticType.get(statType));
                    data = parseGeoMapReport(is, statType);
                } else {
                    //logger.info(" getData :: download DASHBOARD_REPORT");
                    InputStream is = downloadDataForSparklineORminitable(ja, reportId, ReportTypes.DASHBOARD_REPORT, dateRange, null, FilterModes.MATCH, 0, ja.DEFAULT_ROW_COUNT, siteORpage);
                    if (!chartType.equals("Pie")) data = parseDashboardReportSparklineCase(is, dateRange, statType);
                    else data = parseDashboardReportMinitableCase(is, statType);
                }
            } else {
                InputStream is = downloadDataForSparklineORminitable(ja, reportId, ReportTypes.CONTENT_DRILLDOWN, dateRange, null, FilterModes.MATCH, 0, ja.DEFAULT_ROW_COUNT, siteORpage.replace(" ", ""));
                data = parseContentDrillDownReport(is, dateRange, statType);
            }

        } catch (JAnalyticsException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //logger.info("getData :: GO OUT OK");
        return data;
    }

    private Map<String, String> parseContentDrillDownReport(InputStream is, String dateRange, String statType) {
        //logger.info("parseContentDrillDownReport Done");
        Map<String, String> data = new HashMap<String, String>();

        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(is);
            Element root = doc.getDocumentElement();
            Element report = (Element) root.getElementsByTagName("Report").item(0);
            entireRangeDatesPositions = getEntireDatesRange(report);
            activeValuesPositions = getActiveDatesRange(entireRangeDatesPositions, dateRange);
            List items = new ArrayList();

            Node scorecard = report.getElementsByTagName("Scorecard").item(0);
            Node itemGroup = ((Element) scorecard).getElementsByTagName("Group").item(0);

            items = JXPathContext.newContext(itemGroup).selectNodes("Item[Name='" + statType + "']");
            Element item = (Element) items.get(0);
            Element sparkline = (Element) (item.getElementsByTagName("Sparkline")).item(0);
            entireRangeDatesPositions = getEntireDatesRange(report);
            activeValuesPositions = getActiveDatesRange(entireRangeDatesPositions, dateRange);
            fill(sparkline, activeValuesPositions, data, statType, "");

        }
        catch (ParserConfigurationException e) {
            data.put("Error", "ParserConfigurationException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (IOException e) {
            data.put("Error", "IOException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (SAXException e) {
            data.put("Error", "Username and password do not match");
            return data;
            //e.printStackTrace();  // todo make it return default data
        }


        return data;  //To change body of created methods use File | Settings | File Templates.
    }

    private Map<String, String> parseDashboardReportSparklineCase(InputStream is, String dateRange, String statType) {
        Map<String, String> data = new HashMap<String, String>();
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(is);
            Element root = doc.getDocumentElement();
            Element report = (Element) root.getElementsByTagName("Report").item(0);
            entireRangeDatesPositions = getEntireDatesRange(report);
            activeValuesPositions = getActiveDatesRange(entireRangeDatesPositions, dateRange);
            List sparks = new ArrayList();
            if (!statType.startsWith("Goal")) {
                ////logger.info("getDataFromInputStream :: look for sparkline");
                sparks = JXPathContext.newContext(report).selectNodes("Sparkline[@id='" + statType + "Sparkline']");
                String total = ((String) ((JXPathContext.newContext(report).getValue("ItemSummary [@id='" + statType + "Summary']/Item/SummaryValue")))).replace(",", "'") + " " + statType;
                Element spp = (Element) sparks.get(0);
                ////logger.info("getDataFromInputStream :: go to fill data");
                fill(spp, activeValuesPositions, data, statType, total);
            } else {
                sparks = JXPathContext.newContext(report).selectNodes("Sparkline[@id='" + statType + "']");

                if (sparks.size() != 0) {
                    String total = (String) JXPathContext.newContext(report).getValue("ItemSummary [@id='GoalSummary_" + statType.charAt(statType.length() - 1) + "']/Item/SummaryValue");
                    Element spp = (Element) sparks.get(0);
                    fill(spp, activeValuesPositions, data, statType, total);
                } else {
                    data.clear();
                    data.put("total", statType + " is off");
                }
            }
        }
        catch (ParserConfigurationException e) {
            data.put("Error", "ParserConfigurationException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (IOException e) {
            data.put("Error", "IOException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (SAXException e) {
            data.put("Error", "Username and password do not match");
            return data;
            //e.printStackTrace();  // todo make it return default data
        }
        /*Set keys = data.keySet();
        Iterator it = keys.iterator();
        logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        logger.info("%%%%%parsed data %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        while(it.hasNext())
        {
            String st = (String)it.next();
            logger.info(" - "+st+" = "+data.get(st));
        } */
        return data;
    }

    private Map<String, String> parseDashboardReportMinitableCase(InputStream is, String statType) {
        Map<String, String> data = new HashMap();
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        //logger.info(" :: getMinitableData");
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(is);
            Element root = doc.getDocumentElement();
            Element report = (Element) root.getElementsByTagName("Report").item(0);
            Element minitable = (Element) (JXPathContext.newContext(report).selectNodes("MiniTable [@id='" + statType + "MiniTable']")).get(0);
            ////logger.info(" :: getMinitableData :: minitable ok");
            NodeList rows_in_mt = minitable.getElementsByTagName("Row");
            //logger.info("getDataFromInputStream :: " + rows_in_mt + " " + statType);
            for (int i = 0; i < rows_in_mt.getLength(); i++) {
                Node one_row = rows_in_mt.item(i);
                String key = (String) JXPathContext.newContext(one_row).getValue("PrimaryKey");
                String value = ((String) JXPathContext.newContext(one_row).getValue("Cell[1]/Content/Value")).replace(",", "");
                data.put(key, value);
                ////logger.info("getDataFromInputStream :: " + key + " = " + value);
            }
            data.put("size", "" + rows_in_mt.getLength());
            data.put("statType", statType);
        }
        catch (ParserConfigurationException e) {
            data.put("Error", "ParserConfigurationException");
            return data;
        } catch (IOException e) {

            data.put("Error", "IOException");
            return data;
        } catch (SAXException e) {
            data.put("Error", "Username and password do not match");
            return data;
        }
        return data;
    }

    private Map<String, String> parseGeoMapReport(InputStream is, String statType) {
        Map<String, String> data = new HashMap();

        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            doc = db.parse(is);
            Element root = doc.getDocumentElement();
            Element report = (Element) root.getElementsByTagName("Report").item(0);


            Element geoMap = (Element) report.getElementsByTagName("GeoMap").item(0);
            NodeList regions = geoMap.getElementsByTagName("Region");
            data.put("size", "" + regions.getLength());
            data.put("statType", statType);
            for (int i = 0; i < regions.getLength(); i++) {
                String country = ((Element) regions.item(i)).getElementsByTagName("Name").item(0).getFirstChild().getNodeValue();
                String value = (((Element) regions.item(i)).getElementsByTagName("Value").item(0).getFirstChild().getNodeValue()).replace(",", "");
                data.put(country, value);
                ////logger.info(country+" : "+value);
            }
        }
        catch (ParserConfigurationException e) {
            data.put("Error", "ParserConfigurationException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (IOException e) {

            data.put("Error", "IOException");
            return data;
            //e.printStackTrace();  // todo make it return default data
        } catch (SAXException e) {

            data.put("Error", "Username and password do not match");
            return data;
            //e.printStackTrace();  // todo make it return default data
        }

        return data;
    }

    private String getReportIdFromProfile(LinkedHashMap profiles, String profile) {
        Set set = profiles.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (profiles.get(key).equals(profile)) return key;
        }
        return null;
    }

    // 17 december 2008
    private InputStream downloadDataForSparklineORminitable(JAnalytics ja, String reportId, ReportTypes reportType, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount, String url) {
        ReportParams rp = new ReportParams();
        rp.setReportId(reportId);
        rp.setReportType(reportType);
        rp.setDateRange(dateRange);
        rp.setFilter(filter);
        rp.setFilterMode(filterMode);
        rp.setRowIndex(rowIndex);
        rp.setRowCount(rowCount);
        rp.setUrl(url);
        return ja.downloadReport(rp);
    }

    private InputStream downloadDataForGeoMap(JAnalytics ja, String reportId, ReportTypes geoMapReport, String dateRange, String filter, FilterModes filterMode, int rowIndex, int rowCount, String url, String statType) {
        ReportParams rp = new ReportParams();
        rp.setReportId(reportId);
        rp.setReportType(geoMapReport);
        rp.setDateRange(dateRange);
        rp.setFilter(filter);
        rp.setFilterMode(filterMode);
        rp.setRowIndex(rowIndex);
        rp.setRowCount(rowCount);
        rp.setUrl(url);
        rp.setGeoMapStatType(Integer.parseInt(statType));

        return ja.downloadReport(rp);  //To change body of created methods use File | Settings | File Templates.
    }

    private HashMap getEntireDatesRange(Element report) {
        Element graph = (Element) report.getElementsByTagName("Graph").item(0);
        Element serie = (Element) graph.getElementsByTagName("Serie").item(0);
        NodeList points = serie.getElementsByTagName("Point");
        HashMap datesPositions = new HashMap();
        for (int i = 0; i < points.getLength(); i++) {
            String date = ((((Element) (points.item(i))).getElementsByTagName("Label")).item(0)).getFirstChild().getNodeValue();
            datesPositions.put(date.split(",")[1] + "," + date.split(",")[2], i + 1);
        }
        return datesPositions;
    }

    //  7 decembre 2008
    private HashMap getActiveDatesRange(HashMap entireDatesRange, String activeDateRange) {
        HashMap activeRange = new HashMap();

        int begin_month = Integer.parseInt((activeDateRange.split("-"))[0].substring(4, 6));
        int end_month = Integer.parseInt((activeDateRange.split("-"))[1].substring(4, 6));

        int begin_day = Integer.parseInt((activeDateRange.split("-"))[0].substring(6, 8));
        int end_day = Integer.parseInt((activeDateRange.split("-"))[1].substring(6, 8));

        int begin_year = Integer.parseInt((activeDateRange.split("-"))[0].substring(0, 4));
        int end_year = Integer.parseInt((activeDateRange.split("-"))[1].substring(0, 4));

        String begin_date = mapMonthToString(begin_month) + " " + begin_day + ", " + begin_year;
        String end_date = mapMonthToString(end_month) + " " + end_day + ", " + end_year;
        int values_begin_at = 0;
        if (entireDatesRange.containsKey(begin_date.trim())) {
            values_begin_at = (Integer) entireDatesRange.get(begin_date.trim());
        }
        int values_end_at = entireDatesRange.size() - 1;
        if (entireDatesRange.containsKey(end_date.trim())) {
            values_end_at = (Integer) entireDatesRange.get(end_date.trim());
        }
        Set set = entireDatesRange.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
            String date = (String) it.next();
            int position = (Integer) entireDatesRange.get(date);
            if ((position >= values_begin_at) && (position <= values_end_at)) activeRange.put(date, position);

        }
        return activeRange;
    }

    private void fill(Element spp, HashMap dateRange, Map<String, String> data, String statType, String total) {
        //logger.info("fill :: ");
        Set date = dateRange.keySet();
        Iterator it = date.iterator();
        data.clear();
        data.put("size", "" + date.size());
        data.put("statType", statType);
        data.put("total", total);
        while (it.hasNext()) {
            String currentDate = (String) it.next();
            int valuePosition = (Integer) entireRangeDatesPositions.get(currentDate);
            double value = Double.parseDouble(((Node) (JXPathContext.newContext(spp).selectNodes("PrimaryValue[" + valuePosition + "]").get(0))).getFirstChild().getNodeValue().replace(",", ""));
            data.put(currentDate, "" + value);
            //logger.info("fill :: " + currentDate + " = " + value);
        }

    }

    private String mapMonthToString(int month) {
        switch (month) {
            case 1:
                return ("January");
            case 2:
                return ("February");
            case 3:
                return ("March");
            case 4:
                return ("April");
            case 5:
                return ("May");
            case 6:
                return ("June");
            case 7:
                return ("July");
            case 8:
                return ("August");
            case 9:
                return ("September");
            case 10:
                return ("October");
            case 11:
                return ("November");
            case 12:
                return ("December");
            default:
                break;
        }

        return null;
    }

    private void getStatisticTypes() {
        statisticType.put("Visits", "0");
        statisticType.put("PagesPerVisit", "1");
        statisticType.put("Pageviews", "10");
        statisticType.put("NewVisits", "12");
        statisticType.put("Bounces", "13");
        /*statisticType.put("Goal1Completions", "14");
        statisticType.put("Goal2Completions", "15");
        statisticType.put("Goal3Completions", "16");
        statisticType.put("GoalConversions", "17");
        statisticType.put("TotalGoalValue", "18");*/

    }


}

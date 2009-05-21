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
package org.jahia.analytics.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.NameValuePair;

/**
 * Report types for Google Analytics xml reports.
 *
 * @author Dan Andrews.
 */
public final class ReportTypes implements Comparable {

    /*
      * These enums are defined as the <code>ReportTypes</code> class so we can
      * compile with JDK 1.4. TODO: I18N "Executive Overview", ...
      */

    /**
     * ****************************
     */
    public static final ReportTypes USER_DEFINED = new ReportTypes(1212,
            "User-defined", "UserDefinedDetailReport");
    public static final int _USER_DEFINED = 1212;


    public static final ReportTypes CONTENT_BY_TITLES_1 = new
            ReportTypes(2206, "Content by Titles 1", "ContentbyTitles1Report");
    public static final int _CONTENT_BY_TITLES_1 = 2206;

    public static final ReportTypes CONTENT_BY_TITLES_2 = new
            ReportTypes(1306, "Content by Titles 2", "ContentByTitles2Report");
    public static final int _CONTENT_BY_TITLES_2 = 1306;

    public static final ReportTypes CONTENT_DRILLDOWN = new ReportTypes(1305,
            "Content-Drilldown", "ContentDrilldownReport");//"ContentDrillDown");
    public static final int _CONTENT_DRILLDOWN = 1305;


    public static final ReportTypes DOMAINS = new ReportTypes(1231,
            "DOMAINS", "DomainsReport");
    public static final int _DOMAINS = 1231;


    public static final ReportTypes CONTENT_BY_TITLE_DETAIL = new ReportTypes(100013,
            "Content by Title Detail", "ContentByTitleDetailReport");
    public static final int _CONTENT_BY_TITLE_DETAIL = 100013;

    public static final ReportTypes CONTENT_DETAIL_REPORT = new ReportTypes(
            100014, "Content Detail", "ContentDetailReport");
    public static final int _CONTENT_DETAIL_REPORT = 100014;

    public static final ReportTypes CONTENT_BY_TITLE_DETAIL_REPORT = new ReportTypes(
            100015, "Content by Title Detail", "ContentByTitleDetailReport");
    public static final int _CONTENT_BY_TITLE_DETAIL_REPORT = 100015;


    /**
     * ****************************
     */

    public static final ReportTypes DASHBOARD_REPORT = new ReportTypes(100000,
            "Dashboard", "DashboardReport");
    public static final int _DASHBOARD_REPORT = 100000;

    public static final ReportTypes GEO_MAP_REPORT = new ReportTypes(100001,
            "GeoMap", "GeoMapReport");
    public static final int _GEO_MAP_REPORT = 100001;

    public static final ReportTypes BROWSERS_REPORT = new ReportTypes(100002,
            "Browsers", "BrowsersReport");
    public static final int _BROWSERS_REPORT = 100002;

    public static final ReportTypes ALL_NAVIGATION = new ReportTypes(100003,
            "All Navigation", "TopContentDetailNavigationReport");
    public static final int _ALL_NAVIGATION = 100003;

    public static final ReportTypes GEO_MAP_DETAIL_CITY_REPORT = new ReportTypes(
            100004, "GeoMapDetailCity", "GeoMapReport", "segkey=city");
    public static final int _GEO_MAP_DETAIL_CITY_REPORT = 100004;

    public static final ReportTypes GEO_MAP_DETAIL_COUNTRY_REPORT = new ReportTypes(
            100005, "GeoMapDetailCountry", "GeoMapReport", "segkey=country");
    public static final int _GEO_MAP_DETAIL_COUNTRY_REPORT = 100005;

    public static final ReportTypes GEO_MAP_DETAIL_CONTINENT_REPORT = new ReportTypes(
            100006, "GeoMapDetailContinent", "GeoMapReport", "segkey=continent");
    public static final int _GEO_MAP_DETAIL_CONTINENT_REPORT = 100006;

    public static final ReportTypes GEO_MAP_DETAIL_SUB_CONTINENT_REPORT = new ReportTypes(
            100007, "GeoMapDetailSubContinent", "GeoMapReport",
            "segkey=sub_continent_region");
    public static final int _GEO_MAP_DETAIL_SUB_CONTINENT_REPORT = 100007;

    public static final ReportTypes PRODUCT_SKUS_REPORT = new ReportTypes(
            100008, "Product SKUs", "SkusReport");
    public static final int _PRODUCT_SKUS_REPORT = 100008;


    public static final ReportTypes TOP_CONTENT_REPORT = new ReportTypes(
            100009, "Top Content", "TopContentReport");
    public static final int _TOP_CONTENT_REPORT = 100009;


    public static final ReportTypes ABSOLUTE_UNIQUE_VISITORS = new ReportTypes(
            100010, "Absolute Unique Visitors", "UniqueVisitorsReport");
    public static final int _ABSOLUTE_UNIQUE_VISITORS = 100010;


    public static final ReportTypes TOP_CONTENT_DETAIL_REPORT = new ReportTypes(
            100011, "Top Content Detail", "TopContentDetailReport");
    public static final int _TOP_CONTENT_DETAIL_REPORT = 100011;


    public static final ReportTypes ALL_SOURCE_MEDIUM_BASE_DETAIL_REPORT = new ReportTypes(
            100012, "All Traffic Sources - Source Medium Detail", "AllSourceMediumBaseDetailReport");
    public static final int _ALL_SOURCE_MEDIUM_BASE_DETAIL_REPORT = 100012;

    /**
     * The value of the enum.
     */
    private int value;

    /**
     * The report name.
     */
    private String reportName;

    /**
     * The report attribute value.
     */
    private String reportAttributeValue;

    /**
     * The optional drilldown param string.
     */
    private String drildownParamString;

    /**
     * An unmodifiable list of report types.
     */
    private static List values;

    static {

        List list = Arrays.asList(new ReportTypes[]{DASHBOARD_REPORT,
                GEO_MAP_REPORT, BROWSERS_REPORT, ALL_NAVIGATION,
                GEO_MAP_DETAIL_CITY_REPORT, GEO_MAP_DETAIL_COUNTRY_REPORT,
                GEO_MAP_DETAIL_CONTINENT_REPORT,
                GEO_MAP_DETAIL_SUB_CONTINENT_REPORT, PRODUCT_SKUS_REPORT,
                TOP_CONTENT_REPORT, ABSOLUTE_UNIQUE_VISITORS,
                TOP_CONTENT_DETAIL_REPORT, ALL_SOURCE_MEDIUM_BASE_DETAIL_REPORT,
                /****************************************************************/
                USER_DEFINED, CONTENT_BY_TITLES_1, CONTENT_BY_TITLES_2, CONTENT_DRILLDOWN, CONTENT_BY_TITLE_DETAIL, CONTENT_DETAIL_REPORT,
                CONTENT_BY_TITLE_DETAIL_REPORT
        });
        Collections.sort(list);
        values = Collections.unmodifiableList(list);
    }

    /**
     * Constructor.
     *
     * @param value The value of the enum.
     */
    private ReportTypes(int value, String reportName,
                        String reportAttributeValue) {
        this(value, reportName, reportAttributeValue, null);
    }

    /**
     * Constructor.
     *
     * @param value The value of the enum.
     */
    private ReportTypes(int value, String reportName,
                        String reportAttributeValue, String drildownParamString) {
        this.value = value;
        this.reportName = reportName;
        this.reportAttributeValue = reportAttributeValue;
        this.drildownParamString = drildownParamString;
    }

    /**
     * Gets the report name.
     *
     * @return The report name.
     */
    public String getReportName() {
        return reportName;
    }

    /**
     * Gets the report type from one of the report type values where the value
     * must be defined in one of the int constants defined in this class.
     *
     * @param value The value of the report type.
     * @return The corresponding <code>ReportTypes</code> object.
     */
    public static ReportTypes fromInt(int value) {
        ReportTypes rv = null;
        switch (value) {
            case _DASHBOARD_REPORT:
                rv = DASHBOARD_REPORT;
                break;
            case _GEO_MAP_REPORT:
                rv = GEO_MAP_REPORT;
                break;
            case _BROWSERS_REPORT:
                rv = BROWSERS_REPORT;
                break;
            case _ALL_NAVIGATION:
                rv = ALL_NAVIGATION;
                break;
            case _GEO_MAP_DETAIL_CITY_REPORT:
                rv = GEO_MAP_DETAIL_CITY_REPORT;
                break;
            case _GEO_MAP_DETAIL_COUNTRY_REPORT:
                rv = GEO_MAP_DETAIL_COUNTRY_REPORT;
                break;
            case _GEO_MAP_DETAIL_CONTINENT_REPORT:
                rv = GEO_MAP_DETAIL_CONTINENT_REPORT;
                break;
            case _GEO_MAP_DETAIL_SUB_CONTINENT_REPORT:
                rv = GEO_MAP_DETAIL_SUB_CONTINENT_REPORT;
                break;
            case _PRODUCT_SKUS_REPORT:
                rv = PRODUCT_SKUS_REPORT;
                break;
            case _TOP_CONTENT_REPORT:
                rv = TOP_CONTENT_REPORT;
                break;
            case _ABSOLUTE_UNIQUE_VISITORS:
                rv = ABSOLUTE_UNIQUE_VISITORS;
                break;
            case _TOP_CONTENT_DETAIL_REPORT:
                rv = TOP_CONTENT_DETAIL_REPORT;
                break;
            case _ALL_SOURCE_MEDIUM_BASE_DETAIL_REPORT:
                rv = ALL_SOURCE_MEDIUM_BASE_DETAIL_REPORT;
                break;
/****************************************/
            case _USER_DEFINED:
                rv = USER_DEFINED;
                break;
            case _CONTENT_BY_TITLES_1:
                rv = CONTENT_BY_TITLES_1;
                break;
            case _CONTENT_BY_TITLES_2:
                rv = CONTENT_BY_TITLES_2;
                break;

            case _CONTENT_DRILLDOWN:
                rv = CONTENT_DRILLDOWN;
                break;
            case _DOMAINS:
                rv = DOMAINS;
                break;
            case _CONTENT_BY_TITLE_DETAIL:
                rv = CONTENT_BY_TITLE_DETAIL;
                break;

            case _CONTENT_DETAIL_REPORT:
                rv = CONTENT_DETAIL_REPORT;
                break;
            case _CONTENT_BY_TITLE_DETAIL_REPORT:
                rv = CONTENT_BY_TITLE_DETAIL_REPORT;
                break;


            default:
                throw new IllegalArgumentException("Invalid report type value: "
                        + value);
        }
        return rv;
    }

    /**
     * Gets the report attribute value for the report query.
     *
     * @return the reportAttributeValue
     */
    public String getReportAttributeValue() {
        return reportAttributeValue;
    }

    /**
     * Gets the optional drilldown params as an array of
     * <code>NameValuePair</code> objects.
     *
     * @return Always returns a non null array, however, the array may be of
     *         length 0 if drilldown are not used for this report type.
     */
    public NameValuePair[] getDrillDownParams() {
        NameValuePair[] rv = null;
        if (drildownParamString == null) {
            rv = new NameValuePair[0];
        } else {
            StringTokenizer tokens = new StringTokenizer(drildownParamString,
                    "&");
            List list = new ArrayList();
            while (tokens.hasMoreTokens()) {
                StringTokenizer pairTokens = new StringTokenizer(tokens
                        .nextToken(), "=");
                if (pairTokens.countTokens() != 2) {
                    throw new IllegalArgumentException(
                            "drildownParamString used to construct report types must be "
                                    + "in the form name1=value2%name2=value2....");
                }
                list.add(new NameValuePair(pairTokens.nextToken(), pairTokens
                        .nextToken()));
            }
            if (list.size() == 0) {
                throw new IllegalArgumentException(
                        "If present drildownParamString used to construct report types must be "
                                + "in the form name1=value2%name2=value2....");
            }
            rv = (NameValuePair[]) list.toArray(new NameValuePair[list.size()]);
        }
        return rv;
    }

    /**
     * Sets the optional drilldown params for an instantiated ReportType object.
     *
     * @param props A <code>Properties</code> object containing name/value pairs
     *              for all drilldown parameters.
     *              <p/>
     *              Example for adding drilldown parameter and using it:
     *              // Create the ReportType
     *              ReportTypes tcd = ReportTypes.fromInt(ReportTypes._TOP_CONTENT_DETAIL_REPORT);
     *              // Configure the drilldown parameter for this report.
     *              Properties queryParams = new Properties();
     *              queryParams.put("d1", "/pageToQuery");
     *              tcd.setDrillDownParams(queryParams);
     *              // Call the report using the configured report type.
     *              String xmlReport = analytics.getReport(reportID, tcd, start, end);
     */
    public void setDrillDownParams(Properties props) {
        // If the array has values, continue
        if (props != null && !props.isEmpty()) {
            StringBuffer result = new StringBuffer();
            // Loop through each array value, and extract the info.
            Enumeration en = props.propertyNames();
            for (; en.hasMoreElements();) {
                // Get property name (key)
                String propName = (String) en.nextElement();
                // append key and value onto master query string
                result.append(propName);
                result.append("=");
                result.append(props.get(propName));
                // If we have more NameValuePairs to process, then add a &
                // at the end.
                if (en.hasMoreElements()) {
                    result.append("&");
                }
            }
            // Set the drildown variable after we're done processing.
            drildownParamString = result.toString();
        }
    }


    /**
     * Sets the optional drilldown params for an instantiated ReportType object.
     *
     * @param params A String containing the full query parameters you need for this report.
     *               For example: d1=xxx&d2=yyyy
     */
    public void setDrillDownParams(String params) {
        if (params != null && params.length() > 0) {
            drildownParamString = params;
        }
    }


    /**
     * Compares this object with the specified object for order using the report
     * name.
     *
     * @param o The other object which must be a <code>ReportTypes</code>
     *          object.
     * @return A negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        ReportTypes other = (ReportTypes) o;
        return reportName.compareTo(other.reportName);
    }

    /**
     * Gets all report types.
     *
     * @return An <code>Iterator</code> of all report types.
     */
    public static Iterator getReportTypes() {
        return values.iterator();
    }

    /**
     * Gets the enum value as a string, for example the Executive Overview will
     * return a value of "2001".
     *
     * @return The value of the enum as a string.
     */
    public String getValueAsString() {
        return Integer.toString(value);
    }

    /**
     * Gets the string representation of the value, for example the Executive
     * Overview will return "Executive Overview".
     *
     * @return The value of the enum as a string.
     */
	public String toString() {
		return reportName;
	}

}

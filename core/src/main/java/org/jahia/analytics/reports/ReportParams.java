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

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 17 d�c. 2008
 * Time: 18:30:59
 * To change this template use File | Settings | File Templates.
 */
public class ReportParams {
    String reportId;
    ReportTypes reportType;
    String dateRange;
    FilterModes filterMode;
    int rowIndex;
    int rowCount;
    String url;                 // param: d1 of the request asking for Drill down report or topcontentDetailReport
    String filter;



    int geoMapStatType = -1;   // param: midx of the request asking for geomap report

 public int getGeoMapStatType() {
        return geoMapStatType;
    }

    public void setGeoMapStatType(int geoMapStatType) {
        this.geoMapStatType = geoMapStatType;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public ReportTypes getReportType() {
        return reportType;
    }

    public void setReportType(ReportTypes reportType) {
        this.reportType = reportType;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public FilterModes getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(FilterModes filterMode) {
        this.filterMode = filterMode;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilter() {

        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public ReportParams() {
    }

}

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

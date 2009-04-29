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
package org.jahia.ajax.gwt.client.data.toolbar.analytics;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 11 d�c. 2008
 * Time: 10:35:38
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaAnalyticsParameter implements Serializable {
    private String gaLogin;
    private String gaPassword;
    private String gaProfile;
    private String jahiaGAprofile;
    private String dateRange;
    private String statType;
    private String siteORpage;


    public String getJahiaGAprofile() {
        return jahiaGAprofile;
    }

    public void setJahiaGAprofile(String jahiaGAProfile) {
        this.jahiaGAprofile = jahiaGAProfile;
    }

    public String getSiteORpage() {
        return siteORpage;
    }

    public void setSiteORpage(String siteORpage) {
        this.siteORpage = siteORpage;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    private String chartType;

    public GWTJahiaAnalyticsParameter() {
    }

    public GWTJahiaAnalyticsParameter(String gaLogin, String gaPassword, String gaProfile, String dateRange, String statType, String chartType, String siteORpage) {
        this.gaLogin = gaLogin;
        this.gaPassword = gaPassword;
        this.gaProfile = gaProfile;
        
        this.dateRange = dateRange;
        this.statType = statType;
        this.chartType = chartType;
        this.siteORpage = siteORpage;
    }

    public String getgaLogin() {
        return gaLogin;
    }

    public void setgaLogin(String gaLogin) {
        this.gaLogin = gaLogin;
    }

    public String getgaPassword() {
        return gaPassword;
    }

    public void setgaPassword(String gaPassword) {
        this.gaPassword = gaPassword;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getgaProfile() {
        return gaProfile;
    }

    public void setgaProfile(String gaProfile) {
        this.gaProfile = gaProfile;
    }

    public String getStatType() {
        return statType;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }
}

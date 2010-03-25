package org.jahia.ajax.gwt.client.data.analytics;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 11:31:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaAnalyticsData extends BaseModelData {
    private String date;
    private String country;
    private double value;

    public GWTJahiaAnalyticsData() {
    }

    public GWTJahiaAnalyticsData(String country,String date, double value) {
        this.country = country;
        this.value = value;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

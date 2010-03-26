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
   
    public GWTJahiaAnalyticsData() {
    }

    public GWTJahiaAnalyticsData(String country, Date date, double value) {
        setCountry(country);
        setDate(date);
        setValue(value);
    }

    public String getCountry() {
        return get("country");
    }

    public void setCountry(String country) {
        set("country", country);
    }

    public Double getValue() {
        return get("value");
    }

    public void setValue(Double value) {
        set("value", value);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }
}

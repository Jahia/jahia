package org.jahia.ajax.gwt.client.data.analytics;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 29, 2010
 * Time: 10:52:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaAnalyticsProfile extends BaseModelData {

    public GWTJahiaAnalyticsProfile() {
    }

    public GWTJahiaAnalyticsProfile(String name) {
        setName(name);
    }


    public String getName() {
        return get("name");
    }

    public void setName(String name) {
         set("name",name);
    }
}

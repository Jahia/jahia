package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Mar 16, 2009
 * Time: 5:16:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeVersion extends BaseModel {
    public GWTJahiaNodeVersion() {
    }

    public GWTJahiaNodeVersion(String version, Date date) {
        setVersionNumber(version);
        setDate(date);
    }


    public String getVersionNumber() {
        return get("versionNumber");
    }

    public void setVersionNumber(String versionNumber) {
        set("versionNumber", versionNumber);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public GWTJahiaNode getNode() {
        return get("node");
    }

    public void setNode(GWTJahiaNode node) {
        set("node", node);
    }


}

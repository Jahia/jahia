package org.jahia.ajax.gwt.client.data.analytics;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 1:36:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaAnalyticsQuery implements Serializable {
    private String dimensions;
    private Date startDate;
    private Date endDate;
    private GWTJahiaNode node;

    public GWTJahiaAnalyticsQuery() {
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
    }
}

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
package org.jahia.ajax.gwt.client.data.versioning;

import java.util.List;
import java.io.Serializable;

import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:50:38
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaVersionComparisonData implements Serializable {

    private String titleAssert;
    private GWTJahiaVersion version1;
    private GWTJahiaVersion version2;

    private String addedDiffLegend;
    private String removedDiffLegend;
    private String changedDiffLegend;

    private String[] versionRowDataHeadLabels;

    private List<GWTJahiaFieldGroup> fieldGroups;

    public GWTJahiaVersionComparisonData() {
    }

    public GWTJahiaVersionComparisonData(String titleAssert, GWTJahiaVersion version1, GWTJahiaVersion version2,
                                 List<GWTJahiaFieldGroup> fieldGroups) {
        this.titleAssert = titleAssert;
        this.version1 = version1;
        this.version2 = version2;
        this.fieldGroups = fieldGroups;
    }

    public String getTitleAssert() {
        return titleAssert;
    }

    public void setTitleAssert(String titleAssert) {
        this.titleAssert = titleAssert;
    }

    public GWTJahiaVersion getVersion1() {
        return version1;
    }

    public void setVersion1(GWTJahiaVersion version1) {
        this.version1 = version1;
    }

    public GWTJahiaVersion getVersion2() {
        return version2;
    }

    public void setVersion2(GWTJahiaVersion version2) {
        this.version2 = version2;
    }

    public List<GWTJahiaFieldGroup> getFieldGroups() {
        return fieldGroups;
    }

    public void setFieldGroups(List<GWTJahiaFieldGroup> fieldGroups) {
        this.fieldGroups = fieldGroups;
    }

    public String[] getVersionRowDataHeadLabels() {
        return versionRowDataHeadLabels;
    }

    public void setVersionRowDataHeadLabels(String[] versionRowDataHeadLabels) {
        this.versionRowDataHeadLabels = versionRowDataHeadLabels;
    }

    public String getAddedDiffLegend() {
        return addedDiffLegend;
    }

    public void setAddedDiffLegend(String addedDiffLegend) {
        this.addedDiffLegend = addedDiffLegend;
    }

    public String getRemovedDiffLegend() {
        return removedDiffLegend;
    }

    public void setRemovedDiffLegend(String removedDiffLegend) {
        this.removedDiffLegend = removedDiffLegend;
    }

    public String getChangedDiffLegend() {
        return changedDiffLegend;
    }

    public void setChangedDiffLegend(String changedDiffLegend) {
        this.changedDiffLegend = changedDiffLegend;
    }

}

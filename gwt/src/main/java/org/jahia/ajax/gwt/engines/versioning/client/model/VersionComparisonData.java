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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.versioning.client.model;

import java.util.List;
import java.io.Serializable;

import org.jahia.ajax.gwt.commons.client.beans.GWTVersion;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:50:38
 * To change this template use File | Settings | File Templates.
 */
public class VersionComparisonData implements Serializable {

    private String titleAssert;
    private GWTVersion version1;
    private GWTVersion version2;

    private String addedDiffLegend;
    private String removedDiffLegend;
    private String changedDiffLegend;

    private String[] versionRowDataHeadLabels;

    private List<FieldGroup> fieldGroups;

    public VersionComparisonData() {
    }

    public VersionComparisonData(String titleAssert, GWTVersion version1, GWTVersion version2,
                                 List<FieldGroup> fieldGroups) {
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

    public GWTVersion getVersion1() {
        return version1;
    }

    public void setVersion1(GWTVersion version1) {
        this.version1 = version1;
    }

    public GWTVersion getVersion2() {
        return version2;
    }

    public void setVersion2(GWTVersion version2) {
        this.version2 = version2;
    }

    public List<FieldGroup> getFieldGroups() {
        return fieldGroups;
    }

    public void setFieldGroups(List<FieldGroup> fieldGroups) {
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

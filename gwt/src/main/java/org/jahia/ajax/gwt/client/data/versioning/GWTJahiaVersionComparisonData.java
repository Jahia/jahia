/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

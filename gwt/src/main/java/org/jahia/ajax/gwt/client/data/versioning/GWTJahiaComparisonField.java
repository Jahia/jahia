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

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:21:41
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaComparisonField implements Serializable {

    private String name;
    private String title;
    private String icon;
    private boolean isBigText;
    private String originalValue;
    private String newValue;
    private String mergedDiffValue;

    public GWTJahiaComparisonField() {
    }

    public GWTJahiaComparisonField(String name, String title, String icon, boolean bigText, String originalValue, String newValue,
                 String mergedDiffValue)
    {
        this.name = name;
        this.title = title;
        this.icon = icon;
        isBigText = bigText;
        this.originalValue = originalValue;
        this.newValue = newValue;
        this.mergedDiffValue = mergedDiffValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isBigText() {
        return isBigText;
    }

    public void setBigText(boolean bigText) {
        isBigText = bigText;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getMergedDiffValue() {
        return mergedDiffValue;
    }

    public void setMergedDiffValue(String mergedDiffValue) {
        this.mergedDiffValue = mergedDiffValue;
    }

}

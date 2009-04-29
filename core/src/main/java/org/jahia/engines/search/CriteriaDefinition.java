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
 package org.jahia.engines.search;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 10:18:38
 * To change this template use File | Settings | File Templates.
 */
public class CriteriaDefinition {

    private String name;
    private String label;
    private String labelKey;
    private boolean isDate;


    public CriteriaDefinition(String name,
                              String label,
                              String labelKey,
                              boolean isDate){
        this.name = name;
        this.label = label;
        this.labelKey = labelKey;
        this.isDate = isDate;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

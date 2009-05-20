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
package org.jahia.testtemplate.sorter;

import java.util.Comparator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplateDef;

public class LocalizedTemplateNameSorter implements Comparator<JahiaTemplateDef> {
    private ResourceBundle res;

    public LocalizedTemplateNameSorter(ResourceBundle res) {
        super();
        this.res = res;
    }

    public int compare(JahiaTemplateDef o1, JahiaTemplateDef o2) {
        String resValue1 = null;
        try {
            resValue1 = res.getString(o1.getDisplayName());
        } catch (MissingResourceException e) {
            // do nothing
        }
        if (resValue1 == null) {
            resValue1 = o1.getDisplayName();
        }
        String resValue2 = null;
        try {
            resValue2 = res.getString(o2.getDisplayName());            
        } catch (MissingResourceException e) {
            // do nothing
        }
        if (resValue2 == null) {
            resValue2 = o2.getDisplayName();
        }        
        return resValue1.compareTo(resValue2);
    }
    

}

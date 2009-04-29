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
package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;

import java.util.Comparator;
import java.util.Locale;
import java.text.Collator;

/**
 * User: jahia
 * Date: 31 janv. 2008
 * Time: 14:25:11
 */
public class VersionComparator implements Comparator {

    private boolean asc;
    private String field;
    private Locale locale;
    public VersionComparator(String field, boolean asc, Locale locale) {
        this.field = field;
        this.asc = asc;
        this.locale = locale;
        if (this.locale == null){
            this.locale = Locale.getDefault();
        }
    }

    public int compare(Object o1, Object o2) {

        GWTJahiaVersion version1 = (GWTJahiaVersion) o1;
        GWTJahiaVersion version2 = (GWTJahiaVersion) o2;
        if (!asc){
            version1 = (GWTJahiaVersion) o2;
            version2 = (GWTJahiaVersion) o1;
        }
        Object value1 = null;
        Object value2 = null;
        Collator collator = Collator.getInstance(this.locale);
        value1 = version1.get(field);
        value2 = version2.get(field);
        if (value1==null){
            if (value2==null){
                return 0;
            }
            return -1;
        } else {
            if (value2==null){
                return 1;
            }
        }
        return collator.compare(value1,value2);
    }
}
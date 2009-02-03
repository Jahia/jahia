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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.ajax.gwt.commons.client.beans.GWTVersion;

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

        GWTVersion version1 = (GWTVersion) o1;
        GWTVersion version2 = (GWTVersion) o2;
        if (!asc){
            version1 = (GWTVersion) o2;
            version2 = (GWTVersion) o1;
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
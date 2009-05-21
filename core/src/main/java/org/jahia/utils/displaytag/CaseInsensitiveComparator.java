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
 package org.jahia.utils.displaytag;


import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 25 avr. 2006
 * Time: 17:03:18
 * To change this template use File | Settings | File Templates.
 */
public class CaseInsensitiveComparator implements Comparator {

    private Collator collator;

    /**
     * Instantiate a default comparator with no collator specified.
     */
    public CaseInsensitiveComparator()
    {
        this.collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY); // ignore case and accents
    }

    /**
     * Instantiate a default comparator with a specified locale.
     * @param locale
     */
    public CaseInsensitiveComparator(Locale locale)
    {
        this.collator = Collator.getInstance(locale);
    }

    /**
     * Instantiate a default comparator with a specified collator.
     * @param collatorToUse collator instance
     */
    public CaseInsensitiveComparator(Collator collatorToUse)
    {
        this.collator = collatorToUse;
    }

    /**
     * Compares two given objects. Not comparable objects are compared using their string representation. String
     * comparisons are done using a Collator.
     * @param object1 first parameter
     * @param object2 second parameter
     * @return the value
     */
    public int compare(Object object1, Object object2)
    {
        int returnValue;
        boolean stringComparison = true;
        if (object1 instanceof String && object2 instanceof String) {
        } else if (object1 instanceof Comparable && object2 instanceof Comparable) {
            stringComparison = false;
        }
        if ( stringComparison ){
            String str1 = object1.toString();
            String str2 = object2.toString();
            returnValue = collator.compare(str1.toLowerCase(), str2.toLowerCase());
            if (returnValue == 0 && !str1.equals(str2)){
                returnValue = collator.compare(str1,str2);
            }
        } else {
            returnValue = ((Comparable) object1).compareTo(object2);
        }
        return returnValue;
    }

}

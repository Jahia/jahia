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

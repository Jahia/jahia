/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

//

package org.jahia.utils.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Title: Holds information from a resource bundle marker tag</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class ResourceBundleMarker {
    
    private static final transient Logger logger = LoggerFactory
            .getLogger(ResourceBundleMarker.class);
    private static Pattern pattern = Pattern.compile("<jahia-resource id=\"(.*)\" key=\"(.*)\" default-value=\"(.*)\"/>");
    /**
     * The unique id that identifies a resource bundle file @see JahiaResourceBundle
     */
    private String resourceBundleID;

    /**
     * The resource key
     */
    private String resourceKey;

    /**
     * The value matching the resource
     */
    private String value;

    /**
     * A default value to use when the resource is not found
     */
    private String defaultValue;

    public ResourceBundleMarker(String resourceBundleID,
                                String resourceKey,
                                String defaultValue) {
        this.resourceBundleID = resourceBundleID;
        this.resourceKey = resourceKey;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the resource bundle identifier.
     *
     * @return the resource bundle identifie
     */
    public String getResourceBundleID() {
        return this.resourceBundleID;
    }

    /**
     * Returns the resource key
     *
     * @return resource resource key
     */
    public String getResourceKey() {
        return this.resourceKey;
    }

    /**
     * Returns the internal value.
     * You must call the setValue(String value, ProcessingContext jParams, Locale locale)
     * once to set the internal value with a value that matches the resource.
     *
     * @return the internal value
     */
    public String getValue() {
        if (this.value == null) {
            return "";
        }
        return this.value;
    }

    /**
     * Retrieves the real value of a given key.
     *
     * @param value          The default value
     * @param resourceBundle The bundleKey or resourceBundle name
     * @param valueKey       Key in the bundle to use
     * @param locale         Locale to use
     * @return The real value of the resource in the given locale
     */
    public static String getValue(final String value,
                                  final String resourceBundle,
                                  final String valueKey,
                                  final Locale locale) {
        String result = value;
        try {
            final ResourceBundle res = ResourceBundle.getBundle(resourceBundle, locale);

            result = res.getString(valueKey);
        } catch (MissingResourceException mre) {
            logger.debug(mre.getMessage(), mre);
        }
        if (result == null || result.length() == 0) return value;
        return result;
    }

    /**
     * Returns the default value
     *
     * @return resource resource key
     */
    public String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Generates a valid resource bundle marker from internal value
     * <p/>
     * <jahia-resource id="MySiteResource" key="product.001" default-value="Crew"/>
     *
     * @param resourceBundleID
     * @param resourceKey
     * @param defaultValue
     * @return
     */
    public String drawMarker() {
        return drawMarker(this.getResourceBundleID(),
                this.getResourceKey(),
                this.getDefaultValue());
    }

    /**
     * Set the internal value.
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Generates a ResourceBundleMarker Bean from a resource bundle marker value String
     *
     * @param value a valid tag :
     *              <jahia-resource id="MySiteResource" key="product.001" default-value="Crew"/>
     * @return a resource bundle marker bean or null on any parsing error.
     */
    public static ResourceBundleMarker parseMarkerValue(String markerStr) {

        if (markerStr == null) {
            return null;
        }

        ResourceBundleMarker marker = null;
        String val = markerStr.trim();
        String resourceBundleID;
        String resourceKey;
        String defaultValue;
        Matcher matcher = pattern.matcher(val);
        if (matcher.matches()) {

            resourceBundleID = matcher.group(1);
            resourceKey = matcher.group(2);
            defaultValue = matcher.group(3);
            marker = new ResourceBundleMarker(resourceBundleID, resourceKey,
                                              defaultValue);
        }

        return marker;
    }

    /**
     * Generates a valid resource bundle marker
     * <p/>
     * <jahia-resource id="MySiteResource" key="product.001" default-value="Crew"/>
     *
     * @param resourceBundleID
     * @param resourceKey
     * @param defaultValue
     * @return
     */
    public static String drawMarker(String resourceBundleID,
                                    String resourceKey, String defaultValue) {

        return new StringBuilder(64).append("<jahia-resource ").append("id=\"")
                .append(resourceBundleID).append("\" key=\"").append(
                        resourceKey).append("\" default-value=\"").append(
                        defaultValue).append("\"/>").toString();
    }

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their value
     *
     * @param Object
     * @param Object
     */
    public int compare(Object c1, Object c2) throws ClassCastException {

        // System.out.println("Comparing: "+o1+" and "+o2);
        if (c1 == null) {
            return 1;
        } else if (c2 == null) {
            return -1;
        }

        String s1;
        String s2;
        if (c1 instanceof ResourceBundleMarker)
            s1 = ((ResourceBundleMarker) c1).getValue();
        else
            s1 = c1.toString();
        if (c2 instanceof ResourceBundleMarker)
            s2 = ((ResourceBundleMarker) c2).getValue();
        else
            s2 = c2.toString();

        // find the first digit.
        int idx1 = getFirstDigitIndex(s1);
        int idx2 = getFirstDigitIndex(s2);

        if ((idx1 == -1) ||
                (idx2 == -1) ||
                (!s1.substring(0, idx1).equals(s2.substring(0, idx2)))
                ) {
            // System.out.println("Shortcutted. ");
            return s1.compareTo(s2);
        }

        // find the last digit
        int edx1 = getLastDigitIndex(s1, idx1);
        int edx2 = getLastDigitIndex(s2, idx2);

        String sub1 = null;
        String sub2 = null;

        if (edx1 == -1) {
            sub1 = s1.substring(idx1);
        } else {
            sub1 = s1.substring(idx1, edx1);
        }

        if (edx2 == -1) {
            sub2 = s2.substring(idx2);
        } else {
            sub2 = s2.substring(idx2, edx2);
        }

        // deal with zeros at start of each number
        int zero1 = countZeroes(sub1);
        int zero2 = countZeroes(sub2);

        sub1 = sub1.substring(zero1);
        sub2 = sub2.substring(zero2);

        // if equal, then recurse with the rest of the string
        // need to deal with zeroes so that 00119 appears after 119
        if (sub1.equals(sub2)) {
            int ret = 0;
            if (zero1 > zero2) {
                ret = 1;
            } else if (zero1 < zero2) {
                ret = -1;
            }
            // System.out.println("EDXs: "+edx1+" & "+edx2);
            if (edx1 == -1) {
                s1 = "";
            } else {
                s1 = s1.substring(edx1);
            }
            if (edx2 == -1) {
                s2 = "";
            } else {
                s2 = s2.substring(edx2);
            }

            int comp = compare(s1, s2);
            if (comp != 0) {
                ret = comp;
            }
            // System.out.println("Dealt with rest of string: "+ret);
            return ret;
        } else {
            // if a numerical string is smaller in length than another
            // then it must be less.
            if (sub1.length() != sub2.length()) {
                // System.out.println("Ahah, different length. ");
                return (sub1.length() < sub2.length()) ? -1 : 1;
            }
        }

        // now we get to do the string based numerical thing :)
        // going to assume that the individual character for the
        // number has the right order. ie) '9' > '0'
        // possibly bad in i18n.
        char[] chr1 = sub1.toCharArray();
        char[] chr2 = sub2.toCharArray();

        int sz = chr1.length;
        for (int i = 0; i < sz; i++) {
            // this should give better speed
            if (chr1[i] != chr2[i]) {
                // System.out.println("Length is different. ");
                return (chr1[i] < chr2[i]) ? -1 : 1;
            }
        }

        // System.out.println("Default. Boo. ");
        return 0;
    }

    private int getFirstDigitIndex(String str) {
        return getFirstDigitIndex(str, 0);
    }

    private int getFirstDigitIndex(String str, int start) {
        return getFirstDigitIndex(str.toCharArray(), start);
    }

    private int getFirstDigitIndex(char[] chrs, int start) {
        int sz = chrs.length;

        for (int i = start; i < sz; i++) {
            if (Character.isDigit(chrs[i])) {
                return i;
            }
        }

        return -1;
    }

    private int getLastDigitIndex(String str, int start) {
        return getLastDigitIndex(str.toCharArray(), start);
    }

    private int getLastDigitIndex(char[] chrs, int start) {
        int sz = chrs.length;

        for (int i = start; i < sz; i++) {
            if (!Character.isDigit(chrs[i])) {
                return i;
            }
        }

        return -1;
    }

    public int countZeroes(String str) {
        int count = 0;

        // assuming str is small...
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '0') {
                count++;
            } else {
                break;
            }
        }

        return count;
    }
}

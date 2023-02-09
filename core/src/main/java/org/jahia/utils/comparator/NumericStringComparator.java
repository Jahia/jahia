/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils.comparator;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.VersionInfo;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryBean;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.util.Comparator;
import java.text.Collator;
import java.io.Serializable;
import java.util.Locale;

/**
 * This class allows sorting of string by their Numeric values also so A1,A10,A2 are sorted like A1,A2,A10
 * or 1B,10B,2B are sorted like 1B,2B,10B.
 * @author cedric.mailleux@jahia.com
 * Date: 7 déc. 2006
 * Time: 17:48:00
 */
public class NumericStringComparator<T> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = 5685606235122904838L;

    private Locale locale;

    public NumericStringComparator(Locale locale) {
        this.locale = locale;
    }

    /**
     * Compare between two objects, sort by their value
     *
     * @param c1
     * @param c2
     */
    public int compare(T c1, T c2) throws ClassCastException {

        // System.out.println("Comparing: "+o1+" and "+o2);
        if (c1 == null) {
            return 1;
        } else if (c2 == null) {
            return -1;
        }

        String s1 = getStringValueForObjectComparison(c1);

        // Second object c2 processing
        String s2 = getStringValueForObjectComparison(c2);

        // find the first digit.
        int idx1 = getFirstDigitIndex(s1);
        int idx2 = getFirstDigitIndex(s2);

        if ((idx1 == -1) ||
                (idx2 == -1) ||
                (!s1.substring(0, idx1).equals(s2.substring(0, idx2)))) {
            // System.out.println("Shortcutted. ");
            return Collator.getInstance(locale).compare(s1, s2);
        }

        // find the last digit
        int edx1 = getLastDigitIndex(s1, idx1);
        int edx2 = getLastDigitIndex(s2, idx2);

        String sub1;
        String sub2;

        sub1 = removeLastDigits(s1, idx1, edx1);

        sub2 = removeLastDigits(s2, idx2, edx2);

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

            int comp = s1.compareTo(s2);
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

    protected int getFirstDigitIndex(String str) {
        return getFirstDigitIndex(str, 0);
    }

    protected int getFirstDigitIndex(String str, int start) {
        return getFirstDigitIndex(str.toCharArray(), start);
    }

    protected int getFirstDigitIndex(char[] chrs, int start) {
        int sz = chrs.length;

        for (int i = start; i < sz; i++) {
            if (Character.isDigit(chrs[i])) {
                return i;
            }
        }

        return -1;
    }

    protected int getLastDigitIndex(String str, int start) {
        return getLastDigitIndex(str.toCharArray(), start);
    }

    protected int getLastDigitIndex(char[] chrs, int start) {
        int sz = chrs.length;

        for (int i = start; i < sz; i++) {
            if (!Character.isDigit(chrs[i])) {
                return i;
            }
        }

        return -1;
    }

    protected int countZeroes(String str) {
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

    // UNUSED
    protected boolean containsOnly(String str, char ch) {
        return containsOnly(str.toCharArray(), ch);
    }

    protected boolean containsOnly(char[] chrs, char ch) {
        int sz = chrs.length;

        for (int i = 0; i < sz; i++) {
            if (chrs[i] != ch) {
                return false;
            }
        }

        return true;
    }

    private String getStringValueForObjectComparison(T c1) {
        String s1;
        if (c1 instanceof ResourceBundleMarker) {
            s1 = ((ResourceBundleMarker) c1).getValue();
        } else if (c1.getClass() == Category.class) {
            final Category cat = (Category) c1;
            s1 = cat.getTitle(JCRSessionFactory.getInstance().getCurrentLocale());
            if (s1 == null || s1.length() == 0) {
                s1 = cat.getKey();
            }
        } else if (c1 instanceof CategoryBean) {
            final CategoryBean cat = (CategoryBean) c1;
            s1 = cat.getKey();
        } else if (c1.getClass() == Version.class) {
            final Version res = (Version) c1;
            try {
                s1 = res.getName();
            } catch (RepositoryException e) {
                s1 = "error";
            }
        } else if (c1.getClass() == VersionInfo.class) {
            final VersionInfo res = (VersionInfo) c1;
            s1 = res.getLabel();
            if(s1==null) {
                try {
                    s1 = res.getVersion().getName();
                } catch (RepositoryException e) {
                    s1 = "error";
                }
            }
        } else
            s1 = c1.toString();
        return s1;
    }

    private String removeLastDigits(String s1, int idx1, int edx1) {
        String sub1;
        if (edx1 == -1) {
            sub1 = s1.substring(idx1);
        } else {
            sub1 = s1.substring(idx1, edx1);
        }
        return sub1;
    }
}

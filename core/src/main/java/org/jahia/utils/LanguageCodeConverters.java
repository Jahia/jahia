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
package org.jahia.utils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.jahia.utils.i18n.JahiaResourceBundle;

/**
 * <p>Title: Utility class to convert between the different type of language
 * codes encodings.</p>
 * <p>Description: This class was design to offer conversion tools between
 * various language code encodings into Strings, Locales and other types of
 * objects. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber.
 * @version 1.0
 */

public class LanguageCodeConverters {

    /**
     * Converts string such as
     *   en_US, fr_CH_unix, fr, en, _GB, fr__UNIX
     * into Locale objects. This method is the reverse operation of the
     * Locale.toString() output. So a good test case for this method could
     * be :
     *   languageCodeToLocale(Locale.toString()).equals(Locale)
     * @param languageCode the encoded string
     * @return the resulting Locale object, or the default language code if
     * no language was found.
     */
    public static Locale languageCodeToLocale(String languageCode) {
        if ( languageCode == null ){
        	return null;
        }
        StringTokenizer codeTokens = new StringTokenizer(languageCode,"_");
        String language = "";
        String country = "";
        String variant = "";

        if (codeTokens.hasMoreTokens()) {
            language = codeTokens.nextToken();
        }
        if (codeTokens.hasMoreTokens()) {
            country = codeTokens.nextToken();
        }
        if (codeTokens.hasMoreTokens()) {
            variant = codeTokens.nextToken();
        }

        return newLocale(language, country, variant);
    }

    /**
     * Converts (alt) string such as
     *   en_US, fr_CH_unix, fr, en, _GB, fr__UNIX
     * into Locale objects. This method is the reverse operation of the
     * Locale.toString() output. So a good test case for this method could
     * be :
     *   languageCodeToLocale(Locale.toString()).equals(Locale)
     * @param code the encoded string
     * @return the resulting Locale object, or the default language code if
     * no language was found.
     */
    public static Locale getLocaleFromCode(String code) {
        if (code == null || code.length() == 0) {
            return Locale.getDefault() ;
        }
        Locale loc ;
        String[] codes = code.split("_") ;
        if (codes.length == 0) {
            return Locale.getDefault() ;
        } else if (codes.length == 1) {
            loc = new Locale(codes[0]) ;
        } else if (codes.length == 2) {
            loc = new Locale(codes[0], codes[1]) ;
        } else if (codes.length == 3) {
            loc = new Locale(codes[0], codes[1], codes[2]) ;
        } else {
            return Locale.getDefault() ;
        }
        return loc ;
    }

    /**
     * Converts language tags encoded (RFC 3066) to Locales. Examples of
     * language tags encoding are :
     *   en-us, en, en-scouse
     * Warning : this is not entirely RFC 3066 because we cannot convert from
     * 3 characters ISO 639 part 2 to ISO 639 part 1 codes. In the case a
     * 3 characters code is found we return null.
     * @param languageCode the encoded string
     * @return a Locale corresponding to the encoding.
     */
    public static Locale languageTagsToLocale(String languageCode) {
        StringTokenizer codeTokens = new StringTokenizer(languageCode,"-");
        String language = "";
        String country = "";
        String variant = "";

        String primaryTag = "";
        String secondSubTag = "";
        String thirdSubTag = "";

        if (codeTokens.hasMoreTokens()) {
            primaryTag = codeTokens.nextToken();
        }
        if (codeTokens.hasMoreTokens()) {
            secondSubTag = codeTokens.nextToken();
        }
        if (codeTokens.hasMoreTokens()) {
            thirdSubTag = codeTokens.nextToken();
        }

        // now we must determine what is a language, what is a dialect or
        // a variant depending on RFC 3066's definitions.

        // 1. let's analyse the primary sub tag to see if we can use it as
        //    a language
        if (primaryTag.length() != 2) {
            // language codes other than 2 characters are not supported for
            // the moment.

            // we could correct or reduce this problem by implementing a
            // conversion table from ISO 639 part 2 (3-char) to ISO 639 part 1
            // (2-char), but for the moment we don't.

            /** @todo implement convertion from 3-char to 2-char for languages */
            return null;
        }
        language = primaryTag;

        // 2. the second tag can be either a country code if it's 2 characters
        //    long, or variant or dialect information.
        if (secondSubTag.length() == 2) {
            country = secondSubTag;
            variant = thirdSubTag;
        } else {
            variant = secondSubTag;
        }

        return newLocale(language, country, variant);
    }

    /**
     * Returns a language tags encoded (RFC 3066) compliant string from
     * a Java Locale object. Note that Locales without any languages will
     * not be accepted by this method and will return a null String since
     * language tags MUST contain a language
     *
     * @param locale the locale we want to be converted.
     *
     * @return a String containing the RFC 3066 encoded language information
     * extracted from the tag. If their is no language found in the locale a
     * NULL string is returned instead !
     */
    public static String localeToLanguageTag(Locale locale) {
        StringBuffer result = new StringBuffer();
        if (!("".equals(locale.getLanguage()))) {
            result.append(locale.getLanguage());
        } else {
            // if there is no language we can't return a valid
            // language tag.
            return null;
        }
        if (!("".equals(locale.getCountry()))) {
            result.append("-");
            result.append(locale.getCountry());
        }
        if (!("".equals(locale.getVariant()))) {
            result.append("-");
            result.append(locale.getVariant());
        }

        return result.toString();
    }

    /**
     * A small helper method that allows the various parameters to be empty
     * strings and always return valid locale objects. It mostly behaves likes
     * the regular Locale constructor, except for the case where the language
     * is empty that makes it return the default system Locale.
     *
     * @param language a 2-character ISO 639 part 1 compliant language code.
     * If the language is empty then the default system locale
     * will be returned.
     * @param country a 2-character ISO 3166 compliant country code
     * @param variant a String indicating the variant of the locale
     * @return a valid locale object using the given parameters, or the default
     * system locale if the language string was empty.
     */
    private static Locale newLocale(String language, String country, String variant) {
        if ("".equals(variant)) {
            if ("".equals(country)) {
                if ("".equals(language)) {
                    return Locale.getDefault();
                } else {
                    return new Locale(language, "");
                }
            } else {
                return new Locale(language, country);
            }
        } else {
            return new Locale(language, country, variant);
        }
    }

    public static List<Locale> getSortedLocaleList(Locale currentLocale) {
        Locale[] availableLocales = Locale.getAvailableLocales();
        List<Locale> sortedLocaleList = new ArrayList<Locale>();
        for (int i=0; i < availableLocales.length; i++) {
          Locale curLocale = availableLocales[i];
          sortedLocaleList.add(curLocale);
        }
        Collections.sort(sortedLocaleList, LanguageCodeConverters.getLocaleDisplayNameComparator(currentLocale));
        return sortedLocaleList;
    }

    /**
     * Comparator implementation that compares locale display names in a certain
     * current locale.
     */
    public static class LocaleDisplayNameComparator implements Comparator<Locale> {

        private Collator collator = Collator.getInstance();
        private Locale currentLocale;

        public LocaleDisplayNameComparator(Locale locale) {
            if (locale != null) {
                this.currentLocale = locale;
                collator = Collator.getInstance(locale);
            }
        }

        public int compare(Locale locale1,
                Locale locale2) {
            return collator.compare(locale1.getDisplayName(currentLocale), locale2.getDisplayName(currentLocale));
        }

        public boolean equals(Object obj) {
            if (obj instanceof LocaleDisplayNameComparator) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static LocaleDisplayNameComparator getLocaleDisplayNameComparator(Locale locale) {
        return new LocaleDisplayNameComparator(locale);
    }

    public static List<Locale> getAvailableBundleLocales(
            String resourceBundleName, Locale defaultLocale) {
        final List<Locale> availableBundleLocales = new LinkedList<Locale>();
        // first let's add the default locale if it exists.
        if (defaultLocale != null
                && ResourceBundle.getBundle(resourceBundleName, defaultLocale) != null) {
            availableBundleLocales.add(defaultLocale);
        }
        for (Locale locale : Locale.getAvailableLocales()) {
            ResourceBundle res = ResourceBundle.getBundle(resourceBundleName,
                    locale);
            if (res != null && res.getLocale().equals(locale)
                    && (defaultLocale == null || !locale.equals(defaultLocale))) {
                availableBundleLocales.add(locale);
            }
        }

        return availableBundleLocales;
    }

    public static List<String> localesToLanguageCodes(List<Locale>locales){
        if (locales == null){
            return null;
        }
        if (locales.isEmpty()){
            return new ArrayList<String>();
        }
        List<String> languageCodes = new ArrayList<String>(locales.size());
        for (Locale locale : locales){
            languageCodes.add(locale.toString());
        }
        return languageCodes;
    }

    public static List<Locale> getAvailableBundleLocales() {
        return getAvailableBundleLocales(
                JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, null);
    }

    public static List<Locale> getAvailableBundleLocalesSorted(
            Locale currentLocale) {
        Map<String, Locale> sortedLocales = new TreeMap<String, Locale>();
        for (Locale locale : getAvailableBundleLocales(
                JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, null)) {
            sortedLocales.put(locale.getDisplayName(currentLocale), locale);
        }
        return new LinkedList<Locale>(sortedLocales.values());
    }
}
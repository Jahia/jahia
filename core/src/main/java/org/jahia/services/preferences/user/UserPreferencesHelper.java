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
package org.jahia.services.preferences.user;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.decorator.JCRUserNode;
import org.slf4j.Logger;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;

/**
 * Helper class for accessing user preferences.
 *
 * @author Sergiy Shyrkov
 */
public final class UserPreferencesHelper {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(UserPreferencesHelper.class);

    /**
     * Initializes an instance of this class.
     */
    private UserPreferencesHelper() {
        super();
    }

    /**
     * Returns <code>true</code> if the user has explicitly disabled e-mail
     * notification in the profile.
     *
     * @param user
     *            the user to check preferences
     * @return <code>true</code> if the user has explicitly disabled e-mail
     *         notification in the profile
     */
    public static boolean areEmailNotificationsDisabled(JahiaUser user) {
        String emailNotificationsDisabled = user
                .getProperty("emailNotificationsDisabled");
        return (emailNotificationsDisabled != null && "true"
                .equals(emailNotificationsDisabled));
    }

    /**
     * Returns <code>true</code> if the user has explicitly disabled e-mail
     * notification in the profile.
     *
     * @param user
     *            the user to check preferences
     * @return <code>true</code> if the user has explicitly disabled e-mail
     *         notification in the profile
     */
    public static boolean areEmailNotificationsDisabled(JCRUserNode user) {
        String emailNotificationsDisabled = user
                .getPropertyAsString("emailNotificationsDisabled");
        return (emailNotificationsDisabled != null && "true"
                .equals(emailNotificationsDisabled));
    }

    /**
     * Returns user's e-mail address or <code>null</code> if it is not provided.
     *
     * @param user
     *            the user to retrieve the e-mail address
     * @return user's e-mail address or <code>null</code> if it is not provided
     */
    public static String getEmailAddress(JCRUserNode user) {
        String email = user != null ? user.getPropertyAsString("j:email") : null;

        return StringUtils.isNotBlank(email) ? email : null;
    }

    /**
     * Returns the first name for the specified user or <code>null</code> if it
     * is not provided or empty.
     *
     * @param user
     *            the user to retrieve the first name
     * @return the first name for the specified user or <code>null</code> if it
     *         is not provided or empty
     */
    public static String getFirstName(JCRUserNode user) {
        String name = user != null ? user.getPropertyAsString("j:firstName") : null;

        return !StringUtils.isBlank(name) ? name : null;
    }

    /**
     * Returns the first + last name of the specified user or <code>null</code>
     * if none is not provided or both are empty.
     *
     * @param user
     *            the user to retrieve the name
     * @return the first + last name of the specified user or <code>null</code>
     *         if none is not provided or both are empty
     */
    public static String getFullName(JCRUserNode user) {
        String name = null;

        if (user != null) {
            String firstName = getFirstName(user);
            String lastName = getLastName(user);
            if (firstName != null && lastName != null) {
                name = firstName + " " + lastName;
            } else if (firstName != null || lastName != null) {
                name = firstName != null ? firstName : lastName;
            }
        }

        return name != null ? name : user.getName();
    }

    /**
     * Returns the last name for the specified user or <code>null</code> if it
     * is not provided or empty.
     *
     * @param user
     *            the user to retrieve the first name
     * @return the last name for the specified user or <code>null</code> if it
     *         is not provided or empty
     */
    public static String getLastName(JCRUserNode user) {
        String name = user != null ? user.getPropertyAsString("j:lastName") : null;

        return !StringUtils.isBlank(name) ? name : null;
    }

    /**
     * Returns the e-mail address with the personal name of the specified user.
     *
     * @param user
     *            the user to retrieve the personal name
     * @return the e-mail address with the personal name of the specified user
     */
    public static String getPersonalizedEmailAddress(JCRUserNode user) {
        return getPersonalizedEmailAddress(getEmailAddress(user), user);
    }

    /**
     * Returns the e-mail address with the personal name of the specified user.
     *
     * @param email
     *            the e-mail address itself
     * @param user
     *            the user to retrieve the personal name
     * @return the e-mail address with the personal name of the specified user
     */
    public static String getPersonalizedEmailAddress(String email,
            JCRUserNode user) {
        if (email == null || email.length() == 0 || email.contains("<")) {
            return email;
        }

        String recipientEmail = email;

        if (user != null) {
            String name = getPersonalName(user);
            try {
                recipientEmail = new InternetAddress(recipientEmail, name,
                        SettingsBean.getInstance()
                                .getCharacterEncoding()).toString();
            } catch (UnsupportedEncodingException e) {
                logger.warn(e.getMessage(), e);
                try {
                    recipientEmail = new InternetAddress(recipientEmail, name)
                            .toString();
                } catch (UnsupportedEncodingException e2) {
                    // ignore
                }
            }
        }

        return recipientEmail;
    }

    /**
     * Returns the full user name (first name + last name) or the username if
     * the full name data is not provided.
     *
     * @param user
     *            the user to retrieve the name
     * @return the full user name (first name + last name) or the username if
     *         the full name data is not provided
     */
    public static String getPersonalName(JCRUserNode user) {
        String name = null;

        if (user != null) {
            name = getFullName(user);
            name = name != null ? name : user.getName();
        }

        return name;
    }

    /**
     * Returns the preferred locale of the specified user or the first one from
     * the list of available locales.
     *
     * @param user
     *            the user to retrieve locale preferences
     * @return the preferred locale of the specified user or the first one from
     *         the list of available locales
     */
    public static Locale getPreferredLocale(JCRUserNode user) {
        return getPreferredLocale(user, (Locale) null);
    }

    /**
     * Returns the preferred locale of the specified user or the first one from
     * the list of available locales.
     *
     * @param user
     *            the user to retrieve locale preferences
     * @param fallback the fallback locale to return if no preferred locale is set for the user
     * @return the preferred locale of the specified user or the first one from
     *         the list of available locales
     */
    public static Locale getPreferredLocale(JCRUserNode user, Locale fallback) {
        //String propValue = getPreference("preferredLanguage", user);
        String propValue = user != null ? user.getPropertyAsString("preferredLanguage") : null;
        Locale locale = propValue != null ? LanguageCodeConverters
                .languageCodeToLocale(propValue) : null;

        return locale != null ? locale : fallback;
    }

    /**
     * Returns the preferred locale of the specified user or the first one from
     * the list of available locales.
     *
     * @param user
     *            the user to retrieve locale preferences
     * @param site
     *            the site
     * @return the preferred locale of the specified user or the first one from
     *         the list of available locales
     */
    public static Locale getPreferredLocale(JCRUserNode user, JahiaSite site) {
        //String propValue = getPreference("preferredLanguage", user);
        String propValue = user != null ? user.getPropertyAsString("preferredLanguage") : null;
        Locale locale = propValue != null ? LanguageCodeConverters
                .languageCodeToLocale(propValue) : null;

        if (null == locale) {
            // property is not set --> get list of site languages
            List<Locale> siteLocales = Collections.emptyList();
            if (site != null) {
                siteLocales = site.getLanguagesAsLocales();
            }

            if (siteLocales == null || siteLocales.size() == 0) {
                return JCRSessionFactory.getInstance().getCurrentLocale()!=null?JCRSessionFactory.getInstance().getCurrentLocale():SettingsBean.getInstance().getDefaultLocale();
            }

            List<Locale> availableBundleLocales = LanguageCodeConverters.getAvailableBundleLocales();
            for (Locale siteLocale : siteLocales) {
                if (availableBundleLocales.contains(siteLocale)) {
                    // this one is available
                    locale = siteLocale;
                    break;
                } else if (StringUtils.isNotEmpty(siteLocale.getCountry())) {

                    Locale languageOnlyLocale = new Locale(siteLocale
                            .getLanguage());
                    if (availableBundleLocales.contains(languageOnlyLocale)) {
                        // get language without the country
                        locale = new Locale(siteLocale.getLanguage());
                        break;
                    }
                }
            }
            if (null == locale) {
                locale = availableBundleLocales.get(0);
            }
        }
        return locale;
    }
}

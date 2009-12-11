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
package org.jahia.services.preferences.user;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
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

    private static Logger logger = Logger.getLogger(UserPreferencesHelper.class);
    
    public static final String DISPLAY_ACL_DIFF_STATE = "acldiff.activated";

    public static final String DISPLAY_INTEGRITY_STATE = "integrity.activated";

    public static final String DISPLAY_TBP_STATE = "timebasepublishing.activated";

    public static final String ENABLE_INLINE_EDITING = "inlineediting.activated";

    private static JahiaPreferencesService getPrefsService() {
        return ServicesRegistry.getInstance().getJahiaPreferencesService();
    }

    public static boolean isDisplayAclDiffState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_ACL_DIFF_STATE, SettingsBean.getInstance().isAclDisp(),
                principal);
    }

    public static boolean isDisplayIntegrityState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_INTEGRITY_STATE,
                SettingsBean.getInstance().isIntegrityDisp(), principal);
    }

    public static boolean isDisplayTbpState(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                DISPLAY_TBP_STATE, SettingsBean.getInstance().isTbpDisp(),
                principal);
    }

    public static boolean isDisplayWorkflowState(Principal principal) {
        return SettingsBean.getInstance().isWflowDisp();
    }

    public static boolean isEnableInlineEditing(Principal principal) {
        return getPrefsService().getGenericPreferenceBooleanValue(
                ENABLE_INLINE_EDITING,
                SettingsBean.getInstance().isInlineEditingActivated(),
                principal);
    }

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
     * Returns user's e-mail address or <code>null</code> if it is not provided.
     * 
     * @param user
     *            the user to retrieve the e-mail address
     * @return user's e-mail address or <code>null</code> if it is not provided
     */
    public static String getEmailAddress(JahiaUser user) {
        String email = user != null ? user.getProperty("email") : null;
    
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
    public static String getFirstName(JahiaUser user) {
        String name = user != null ? user.getProperty("firstname") : null;
    
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
    public static String getFullName(JahiaUser user) {
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
    
        return name != null ? name : user.getUsername();
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
    public static String getLastName(JahiaUser user) {
        String name = user != null ? user.getProperty("lastname") : null;
    
        return !StringUtils.isBlank(name) ? name : null;
    }

    /**
     * Returns the e-mail address with the personal name of the specified user.
     * 
     * @param user
     *            the user to retrieve the personal name
     * @return the e-mail address with the personal name of the specified user
     */
    public static String getPersonalizedEmailAddress(JahiaUser user) {
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
            JahiaUser user) {
        if (email == null || email.contains("<")) {
            return email;
        }
    
        String recipientEmail = email;
    
        if (user != null) {
            String name = getPersonalName(user);
            try {
                recipientEmail = new InternetAddress(recipientEmail, name,
                        SettingsBean.getInstance()
                                .getDefaultResponseBodyEncoding()).toString();
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
    public static String getPersonalName(JahiaUser user) {
        String name = null;
    
        if (user != null) {
            name = getFullName(user);
            name = name != null ? name : user.getUsername();
        }
    
        return name;
    }

    /**
     * Returns the preferred locale of the specified user or the first one from
     * the list of available locales.
     * 
     * @param user
     *            the user to retrieve locale preferences
     * @param siteId
     *            the site ID
     * @return the preferred locale of the specified user or the first one from
     *         the list of available locales
     */
    public static Locale getPreferredLocale(JahiaUser user, int siteId) {
        String propValue = getPreference("preferredLanguage", user);
        Locale locale = propValue != null ? LanguageCodeConverters
                .languageCodeToLocale(propValue) : null;
    
        if (null == locale) {
            JahiaSite site = null;
            if (siteId > 0) {
                try {
                    site = ServicesRegistry.getInstance()
                            .getJahiaSitesService().getSite(siteId);
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }
    
            if (site != null) {
                locale = getPreferredLocale(user, site);
            }
        }
        return locale;
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
    public static Locale getPreferredLocale(JahiaUser user, JahiaSite site) {
        String propValue = getPreference("preferredLanguage", user);
        Locale locale = propValue != null ? LanguageCodeConverters
                .languageCodeToLocale(propValue) : null;
    
        if (null == locale) {
            // property is not set --> get list of site languages
            List<Locale> siteLocales = Collections.emptyList();
            if (site != null) {
                try {
                    siteLocales = site.getLanguageSettingsAsLocales(true);
                } catch (JahiaException e) {
                    logger.warn(
                            "Unable to retrieve language settings for site: "
                                    + site, e);
                }
            }
    
            if (siteLocales == null || siteLocales.size() == 0) {
                return Jahia.getThreadParamBean().getLocale();
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
    
    private static String getPreference(String prefName, JahiaUser user) {
        String prefValue = null;
        if (user != null) {
            GenericJahiaPreference preferenceNode = ((GenericJahiaPreference) getPrefsService()
                    .getGenericPreferenceNode(prefName, user));
            if (preferenceNode != null) {
                try {
                    prefValue = preferenceNode.getPrefValue();
                } catch (Exception e) {
                    logger.debug(
                            "Preference \"" + prefName + "\"not retrieved", e);
                }
            }
        }
        return prefValue;
    }
}

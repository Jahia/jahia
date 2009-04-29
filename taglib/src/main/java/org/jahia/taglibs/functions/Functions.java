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
package org.jahia.taglibs.functions;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.JahiaTools;

import java.util.List;
import java.util.Map;

/**
 * Custom functions, which are exposed into the template scope.
 *
 * @author Sergiy Shyrkov
 */
public class Functions {

    private static final Logger logger =
            Logger.getLogger(Functions.class);

    private static final ServicesRegistry registry = ServicesRegistry.getInstance();
    private static final JahiaPageService service = registry.getJahiaPageService();

    public static String attributes(Map<String, Object> attributes) {
        StringBuilder out = new StringBuilder();

        for (Map.Entry<String, Object> attr : attributes.entrySet()) {
            out.append(attr.getKey()).append("=\"").append(
                    attr.getValue() != null ? StringEscapeUtils.escapeXml(attr
                            .getValue().toString()) : "").append("\" ");
        }

        return out.toString();
    }

    public static Object defaultValue(Object value, Object defaultValue) {
        return (value != null && (!(value instanceof String) || (((String) value)
                .length() > 0))) ? value : defaultValue;
    }

    public static Integer getPidFromUrlKey(final String urlKey, final String siteKey) {
        try {
            final List<PageProperty> pageProperties;
            if (siteKey != null && siteKey.length() > 0) {
                final JahiaSite siteByKey = registry.getJahiaSitesService().getSiteByKey(siteKey);
                final int siteID;
                if (siteByKey != null) {
                    siteID = siteByKey.getID();
                } else {
                    siteID = -1;
                }
                pageProperties = service.getPagePropertiesByValueAndSiteID(urlKey, siteID);
            } else {
                pageProperties = service.getPagePropertiesByValue(urlKey);
            }

            if (pageProperties.size() == 1) {
                final PageProperty pageProperty = pageProperties.get(0);
                if (pageProperty.getName().equals(PageProperty.PAGE_URL_KEY_PROPNAME)) {
                    return pageProperty.getPageID();
                }
            }

        } catch (final JahiaException je) {
            logger.error("JahiaException in doStartTag", je);
        }
        return -1;
    }

    public static String removeHtmlTags(String value) {
        return JahiaTools.removeTags(value);
    }

    public static Boolean memberOf(String groups) {
        boolean result = false;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            final String groupName = aGroupArray.trim();
            if (jParams.getUser().isMemberOfGroup(jParams.getSiteID(), groupName)) {
                return true;
            }
        }

        return result;
    }

    public static Boolean notMemberOf(String groups) {
        boolean result = true;
        final ProcessingContext jParams = Jahia.getThreadParamBean();
        final String[] groupArray = StringUtils.split(groups, ',');
        for (String aGroupArray : groupArray) {
            String groupName = aGroupArray.trim();
            if (jParams.getUser().isMemberOfGroup(jParams.getSiteID(),
                    groupName)) {
                return false;
            }
        }

        return result;
    }

    public static String stringConcatenation(String value, String appendix1, String appendix2) {
        final StringBuffer buff = new StringBuffer();
        if (value != null) {
            buff.append(value);
        }
        if (appendix1 != null) {
            buff.append(appendix1);
        }
        if (appendix2 != null) {
            buff.append(appendix2);
        }
        return buff.toString();
    }


    /**
     * Returns <code>true</code> if the subscriptions entry with the specified
     * data exists.
     *
     * @param objectKey the key of the content object that is the source of events
     * @param eventType the type of an event to be notified about
     * @param username  the user to be notified
     * @param siteId    the ID of the site owning the content object in question
     * @return <code>true</code> if the subscriptions entry with the specified
     *         data exists
     */
    public static boolean isSubscribed(String objectKey, String eventType,
                                       String username, int siteId) {

        return SubscriptionService.getInstance().isSubscribed(objectKey,
                eventType, username, siteId);
    }

    /**
     * Returns <code>true</code> if the subscriptions entry with the specified
     * data does not exist.
     *
     * @param objectKey the key of the content object that is the source of events
     * @param eventType the type of an event to be notified about
     * @param username  the user to be notified
     * @param siteId    the ID of the site owning the content object in question
     * @return <code>true</code> if the subscriptions entry with the specified
     *         data does not exist
     */
    public static boolean isNotSubscribed(String objectKey, String eventType,
                                          String username, int siteId) {

        return !SubscriptionService.getInstance().isSubscribed(objectKey,
                eventType, username, siteId);
    }

    public static String removeDuplicates(String initString, String separator) {
        final String[] fullString = initString.split(separator);
        StringBuilder finalString = new StringBuilder();
        String tmpString = initString;
        for (String s:fullString) {
            if (tmpString.contains(s)) {
                finalString.append(s);
                if (finalString.length() > 0) {
                    finalString.append(separator);
                }
                tmpString = tmpString.replaceAll(s,"");
            }
        }
        return finalString.toString();
    }

    public static int countOccurences(String initString, String searchString) {
        final String[] fullString = ("||||" + initString + "||||").split(searchString);
        return fullString.length - 1;
    }
}

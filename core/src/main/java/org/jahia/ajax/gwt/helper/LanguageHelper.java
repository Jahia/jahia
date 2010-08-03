package org.jahia.ajax.gwt.helper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.bin.Jahia;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * Helper class for language-related actions.
 * User: ktlili
 * Date: Jan 18, 2010
 * Time: 2:16:33 PM
 */
public class LanguageHelper {
    private static Logger logger = Logger.getLogger(LanguageHelper.class);

    /**
     * Get available languages for the current site
     *
     * @param site
     * @param user
     * @param currentLocale
     * @return
     */
    public List<GWTJahiaLanguage> getLanguages(JCRSiteNode site, JahiaUser user, Locale currentLocale) {
        List<GWTJahiaLanguage> items = new ArrayList<GWTJahiaLanguage>();

        try {
            if (site != null && site.getLanguages() != null)  {
                final Set<String> languageSettings = site.getLanguages();
                if (languageSettings != null && languageSettings.size() > 0) {
                    final TreeSet<String> orderedLangs = new TreeSet<String>();
                    orderedLangs.addAll(languageSettings);
                    for (String langCode : orderedLangs) {
                        if (user.isPermitted(new PermissionIdentity(langCode, "languages",  site.getSiteKey()))) {
                            GWTJahiaLanguage item = new GWTJahiaLanguage();
                            item.setLanguage(langCode);
                            item.setDisplayName(getDisplayName(langCode));
                            item.setImage(getLangIcon(Jahia.getContextPath(), langCode));
                            item.setCurrent(langCode.equalsIgnoreCase(currentLocale.toString()));
                            items.add(item);
                        }
                    }
                }
            } else {
                Locale[] locales = Locale.getAvailableLocales();
                final TreeSet<String> orderedLangs = new TreeSet<String>();
                for (Locale locale : locales) {
                    orderedLangs.add(locale.getLanguage());
                }
                for (String langCode : orderedLangs) {
                    GWTJahiaLanguage item = new GWTJahiaLanguage();
                    item.setLanguage(langCode);
                    item.setDisplayName(getDisplayName(langCode));
                    item.setImage(getLangIcon(Jahia.getContextPath(), langCode));
                    item.setCurrent(langCode.equalsIgnoreCase(currentLocale.toString()));
                    items.add(item);
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }

        return items;
    }

    /**
     * Get current lang
     *
     * @return
     * @param locale
     */
    public GWTJahiaLanguage getCurrentLang(Locale locale) {
        String langCode = locale.toString();
        GWTJahiaLanguage item = new GWTJahiaLanguage();
        item.setLanguage(langCode);
        item.setDisplayName(getDisplayName(langCode));
        item.setImage(getLangIcon(Jahia.getContextPath(), langCode));
        return item;
    }

    /**
     * Get icon style depending and the selected language
     *
     * @param locale
     * @return
     */
    public static String getLangIcon(String contextPath, String locale) {
        return contextPath + "/css/images/flags/" + locale + "_on.png";
    }

    /**
     * Get display name
     *
     * @param langCode
     * @return
     */
    public static String getDisplayName(String langCode) {
        if (langCode == null) {
            return "";
        }
        langCode = langCode.replace("-", "_");
        Locale currentLocale = LanguageCodeConverters.getLocaleFromCode(langCode);
        return StringUtils.capitalize(currentLocale.getDisplayName(currentLocale));

    }
}

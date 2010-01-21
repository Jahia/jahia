package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;
import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 18, 2010
 * Time: 2:16:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class LanguageHelper {
    private static Logger logger = Logger.getLogger(LanguageHelper.class);

    /**
     * Get available languages for the current site
     *
     * @param jParams
     * @return
     */
    public List<GWTLanguageSwitcherLocaleBean> getLanguages(ParamBean jParams) {
        List<GWTLanguageSwitcherLocaleBean> items = new ArrayList<GWTLanguageSwitcherLocaleBean>();

        try {
            final JahiaSite currentSite = jParams.getSite();
            final Set<String> languageSettings = currentSite.getLanguages();
            if (languageSettings != null && languageSettings.size() > 0) {
                final TreeSet<String> orderedLangs = new TreeSet<String>();
                orderedLangs.addAll(languageSettings);
                for (String langCode : orderedLangs) {
                    GWTLanguageSwitcherLocaleBean item = new GWTLanguageSwitcherLocaleBean();
                    item.setCountryIsoCode(langCode);
                    item.setDisplayName(getDisplayName(langCode));
                    item.setImage(getLangIcon(LanguageCodeConverters.languageCodeToLocale(langCode)));
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
     * @param jParams
     * @return
     */
    public GWTLanguageSwitcherLocaleBean getCurrentLang(ParamBean jParams) {
        String langCode = jParams.getLocale().toString();
        GWTLanguageSwitcherLocaleBean item = new GWTLanguageSwitcherLocaleBean();
        item.setCountryIsoCode(langCode);
        item.setDisplayName(getDisplayName(langCode));
        item.setImage(getLangIcon(jParams.getLocale()));
        return item;
    }

    /**
     * Get icon style depending and the selected language
     *
     * @param locale
     * @return
     */
    public static String getLangIcon(Locale locale) {
        return "/images/flags/plain/flag_" + locale.getCountry()+".png";
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
        return currentLocale.getDisplayName(currentLocale);

    }
}

package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTLanguageSwitcherLocaleBean;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.comparator.LanguageSettingsComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

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
            final List<SiteLanguageSettings> languageSettings = currentSite.getLanguageSettings(true);
            if (languageSettings != null && languageSettings.size() > 0) {
                final TreeSet<SiteLanguageSettings> orderedLangs = new TreeSet<SiteLanguageSettings>(new LanguageSettingsComparator());
                orderedLangs.addAll(languageSettings);
                for (SiteLanguageSettings lang : orderedLangs) {
                    GWTLanguageSwitcherLocaleBean item = new GWTLanguageSwitcherLocaleBean();
                    item.setCountryIsoCode(lang.getCode());
                    item.setDisplayName(getDisplayName(lang.getCode()));
                    item.setIconStyle(getLangIconStyle(lang.getCode()));
                    items.add(item);
                }
            }
        } catch (JahiaException e) {
            logger.error("JahiaException: Error while creating change site link", e);
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
        item.setIconStyle(getLangIconStyle(langCode));
        return item;
    }

    /**
     * Get icon style depending and the selected language
     *
     * @param langCode
     * @return
     */
    public static String getLangIconStyle(String langCode) {
        return "gwt-toolbar-icon-lang-" + langCode;
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

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
package org.jahia.taglibs.uicomponents.i18n;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.comparator.LanguageCodesComparator;
import org.jahia.utils.comparator.LanguageSettingsComparator;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import java.io.IOException;
import java.util.*;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class LanguageSwitchTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LanguageSwitchTag.class);

    // Attribute names
    public static final String DISPLAY_ATTRIBUTE = "display";
    public static final String LINK_DISPLAY_ATTRIBUTE = "linkDisplay";
    public static final String DISPLAY_STATE_ATTRIBUTE = "displayLanguageState";
    public static final String ON_LANGUAGE_SWITCH = "onLanguageSwitch";
    public static final String REDIRECT_CSS_CLASS_NAME = "redirectCssClassName";
    public static final String CSS_CLASS_NAME = "cssClassName";

    // onLanguageSwitch attribute authorized values
    public static final String STAY_ON_CURRENT_PAGE = "stayOnCurrentPage";
    public static final String GO_TO_HOME_PAGE = "goToHomePage";

    // order attribute authorized values 
    public static final String JAHIA_ADMIN_RANKING = "<jahia_admin_ranking>";
    public static final String CURRENT_LANGUAGES_CODES = "languageCodes";

    // display attribute authorized values 
    public static final String HORIZONTAL = "horizontal";
    public static final String VERTICAL = "vertical";
    public static final String COMBO_BOX = "comboBox";

    // Dispatching files for authorized display values
    public static final String HORIZONTAL_FILE = "common/languagelinks/horizontalDisplay.jsp";
    public static final String VERTICAL_FILE = "common/languagelinks/verticalDisplay.jsp";
    public static final String COMBO_BOX_FILE = "common/languagelinks/comboBoxDisplay.jsp";

    // Default Jahia CSS class name when mode "goToHomePage" is activated
    public static final String REDIRECT_DEFAULT_STYLE = "redirectJahiaStyle";

    // Request attribute name used to pass the custom display beans
    public static final String CUSTOM_DISPLAY_TYPE_BEANS = "customLanguageLinksDisplayTypeBeans";

    public static final Map<String, LanguageLinkDisplayBean> DISPLAY_TYPE_BEANS = new HashMap<String, LanguageLinkDisplayBean>(3);
    private static final LanguageSettingsComparator languageSettingsComparator = new LanguageSettingsComparator();
    private static final LanguageCodesComparator languageCodesComparator = new LanguageCodesComparator();

    static {
        DISPLAY_TYPE_BEANS.put(HORIZONTAL, new LanguageLinkDisplayBean(HORIZONTAL, HORIZONTAL_FILE));
        DISPLAY_TYPE_BEANS.put(VERTICAL, new LanguageLinkDisplayBean(VERTICAL, VERTICAL_FILE));
        DISPLAY_TYPE_BEANS.put(COMBO_BOX, new LanguageLinkDisplayBean(COMBO_BOX, COMBO_BOX_FILE));
    }

    private String display;
    private String linkDisplay;
    private String onLanguageSwitch;
    private String redirectCssClassName;
    private String order;
    private boolean activeLanguagesOnly = true;
    private boolean displayLanguageState = false;

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setLinkDisplay(String linkDisplay) {
        this.linkDisplay = linkDisplay;
    }

    public void setOnLanguageSwitch(String onLanguageSwitch) {
        this.onLanguageSwitch = onLanguageSwitch;
    }

    public void setRedirectCssClassName(String redirectCssClassName) {
        this.redirectCssClassName = redirectCssClassName;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public void setActiveLanguagesOnly(boolean activeLanguagesOnly) {
        this.activeLanguagesOnly = activeLanguagesOnly;
    }

    public void setDisplayLanguageState(boolean displayLanguageState) {
        this.displayLanguageState = displayLanguageState;
    }

    public int doEndTag() throws JspException {
        try {
            final ServletRequest request = pageContext.getRequest();
            LanguageLinkDisplayBean displayBean = null;
            if (DISPLAY_TYPE_BEANS.containsKey(display)) {
                displayBean = DISPLAY_TYPE_BEANS.get(display);
            } else {
                final Map<String, LanguageLinkDisplayBean> customTypes = (Map<String, LanguageLinkDisplayBean>)
                        request.getAttribute(CUSTOM_DISPLAY_TYPE_BEANS);
                if (customTypes != null) {
                    displayBean = customTypes.get(display);
                }
                if (displayBean == null) {
                    throw new JspTagException("Language link display with name '" + display + "' was not found !");
                }
            }

            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final JahiaSite currentSite = jData.getProcessingContext().getSite();
            final List<SiteLanguageSettings> languageSettings = currentSite.getLanguageSettings(activeLanguagesOnly);

            if (languageSettings.size() < 2) {
                return EVAL_PAGE;
            }

            final List<String> currentCodes = new ArrayList<String>(languageSettings.size());
            final JahiaPage currentPage = jData.page();
            if (order == null || order.length() == 0 || JAHIA_ADMIN_RANKING.equals(order)) {
                final TreeSet<SiteLanguageSettings> orderedLangs = new TreeSet<SiteLanguageSettings>(languageSettingsComparator);
                orderedLangs.addAll(languageSettings);
                for (final SiteLanguageSettings settings : orderedLangs) {
                    final String languageCode = settings.getCode();
                     if (jData.gui().isNormalMode() && ! currentPage.hasEntry(ContentPage.ACTIVE_PAGE_INFOS, languageCode)) {
                        // Only add the language in Live mode if the current page has an active verison in live mode for that language
                        continue;
                    }
                    currentCodes.add(languageCode);
                }

            } else {
                final List<String> languageCodes = toListOfTokens(order, ",");
                languageCodesComparator.setPattern(languageCodes);
                final TreeSet<String> orderedLangs = new TreeSet<String>(languageCodesComparator);
                final Set<String> codes = new HashSet<String>(languageSettings.size());
                for (final Object lang : languageSettings) {
                    final SiteLanguageSettings settings = (SiteLanguageSettings) lang;
                    final String languageCode = settings.getCode();

                    if (jData.gui().isNormalMode() && ! currentPage.hasEntry(ContentPage.ACTIVE_PAGE_INFOS, languageCode)) {
                        // Only add the language in Live mode if the current page has an active verison in live mode for that language
                        continue;   
                    }
                    codes.add(languageCode);
                }
                orderedLangs.addAll(codes);
                for (String code : orderedLangs) {
                    currentCodes.add(code);
                }
            }

            request.setAttribute(CSS_CLASS_NAME, cssClassName);
            request.setAttribute(LINK_DISPLAY_ATTRIBUTE, linkDisplay);
            request.setAttribute(ON_LANGUAGE_SWITCH, onLanguageSwitch);
            request.setAttribute(REDIRECT_CSS_CLASS_NAME, redirectCssClassName);
            request.setAttribute(CURRENT_LANGUAGES_CODES, currentCodes);
            request.setAttribute(DISPLAY_STATE_ATTRIBUTE, displayLanguageState);
            pageContext.include(resolveIncludeFullPath(displayBean.getDisplayFile()));

        } catch (final JahiaException je) {
            logger.error("Error while generating the language switching links !", je);
        } catch (final IOException je) {
            logger.error("Error while generating the language switching links !", je);
        } catch (final ServletException je) {
            logger.error("Error while generating the language switching links !", je);
        }
        display = null;
        linkDisplay = null;
        onLanguageSwitch = null;
        redirectCssClassName = null;
        activeLanguagesOnly = true;
        displayLanguageState = false;
        order = null;
        return EVAL_PAGE;
    }

    protected List<String> toListOfTokens(final String value, final String separator) {
        final StringTokenizer tokenizer = new StringTokenizer(value, separator);
        final List<String> result = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken().trim();
            result.add(token);
        }
        return result;
    }
}

/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.uicomponents.i18n;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.services.content.JCRNodeWrapper;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import java.util.Locale;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayLanguageSwitchLinkTag extends ValueJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(DisplayLanguageSwitchLinkTag.class);

    public static final String FLAG = "flag";
    public static final String NAME_CURRENT_LOCALE = "nameCurrentLocale";
    public static final String NAME_IN_LOCALE = "nameInLocale";
    public static final String LETTER = "letter";
    public static final String DOUBLE_LETTER = "doubleLetter";
    public static final String LANGUAGE_CODE = "languageCode";
    public static final String ISOLOCALECOUNTRY_CODE = "isoLocaleCountryCode";

    private String languageCode;
    private String linkKind;
    private String urlVar;
    private boolean display = true;
    private String onLanguageSwitch;
    private String redirectCssClassName;
    private String title;
    private String titleKey;
    private JCRNodeWrapper rootPage;

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setLinkKind(String linkKind) {
        this.linkKind = linkKind;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setRootPage(JCRNodeWrapper rootPage) {
        this.rootPage = rootPage;
    }

    public void setOnLanguageSwitch(String onLanguageSwitch) {
        this.onLanguageSwitch = onLanguageSwitch;
    }

    public void setRedirectCssClassName(String redirectCssClassName) {
        this.redirectCssClassName = redirectCssClassName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitleKey(String titleKey) {
        this.titleKey = titleKey;
    }

    public int doStartTag() {
        try {
            final StringBuilder buff = new StringBuilder();

            final boolean isCurrentBrowsingLanguage = isCurrentBrowsingLanguage(languageCode);
            final boolean isRedirectToHomePageActivated = InitLangBarAttributes.GO_TO_HOME_PAGE.equals(onLanguageSwitch);

            if (!isCurrentBrowsingLanguage) {
                if (isRedirectToHomePageActivated) {
                    if (redirectCssClassName == null || redirectCssClassName.length() == 0) {
                        redirectCssClassName = InitLangBarAttributes.REDIRECT_DEFAULT_STYLE;
                    }
                    buff.append("<div class='");
                    buff.append(redirectCssClassName);
                    buff.append("'>");
                }
                buff.append("<a href='");
                final String link;
                if (onLanguageSwitch == null || onLanguageSwitch.length() == 0
                        || InitLangBarAttributes.STAY_ON_CURRENT_PAGE.equals(onLanguageSwitch)) {
                    link = generateCurrentNodeLangSwitchLink(languageCode) + "##requestParameters##";

                } else if (isRedirectToHomePageActivated) {
                    link = generateNodeLangSwitchLink(rootPage, languageCode) + "##requestParameters##";

                } else {
                    throw new JspTagException("Unknown onLanguageSwitch attribute value " + onLanguageSwitch);
                }

                buff.append(StringEscapeUtils.escapeXml(link));
                if (urlVar != null && urlVar.length() > 0) {
                    pageContext.setAttribute(urlVar, link);
                }
                buff.append("' ");
                buff.append("title='");
                if (isRedirectToHomePageActivated) {
                    titleKey += "." + onLanguageSwitch;
                }
                buff.append(getMessage(titleKey, title));
                buff.append("'>");
            } else {
                buff.append("<span>");
                if (urlVar != null) pageContext.removeAttribute(urlVar, PageContext.PAGE_SCOPE);
            }

            String attributeValue = null;
            if (linkKind == null || linkKind.length() == 0 || LANGUAGE_CODE.equals(linkKind) ||
                    linkKind.startsWith(FLAG)) {
                attributeValue = languageCode;
                buff.append(languageCode);

            } else if (NAME_CURRENT_LOCALE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(getRenderContext().getMainResource().getLocale());
                attributeValue = value;
                buff.append(value);

            } else if (NAME_IN_LOCALE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale);
                attributeValue = value;
                buff.append(value);

            } else if (LETTER.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale).substring(0, 1).toUpperCase();
                attributeValue = value;
                buff.append(value);

            } else if (DOUBLE_LETTER.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale).substring(0, 2).toUpperCase();
                attributeValue = value;
                buff.append(value);

            } else if (ISOLOCALECOUNTRY_CODE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                StringBuilder value = new StringBuilder(locale.getLanguage().toUpperCase());
                if (locale.getCountry() != null && locale.getCountry().length() != 0) {
                    value.append("(").append(locale.getCountry()).append(")");
                }
                attributeValue = value.toString();
                buff.append(value);

            } else {
                throw new JspTagException("Unknown linkKind value '" + linkKind + "'");
            }

            if (getVar() != null) {
                pageContext.setAttribute(getVar(), attributeValue);
            }

            if (!isCurrentBrowsingLanguage) {
                buff.append("</a>");
                if (isRedirectToHomePageActivated) buff.append("</div>");
            } else {
                buff.append("</span>");
            }

            if (display) {
                pageContext.getOut().print(buff.toString());
            }

        } catch (final Exception e) {
            logger.error("Error while getting language switch URL", e);
        }
        return SKIP_BODY;
    }

    public String getIsoLocaleCountryCode() {
        return ISOLOCALECOUNTRY_CODE;
    }

    public String getNameCurrentLocale() {
        return NAME_CURRENT_LOCALE;
    }

    public String getNameInLocale() {
        return NAME_IN_LOCALE;
    }

    public String getLetter() {
        return LETTER;
    }

    public String getDoubleLetter() {
        return DOUBLE_LETTER;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
        onLanguageSwitch = null;
        redirectCssClassName = null;
        linkKind = null;
        languageCode = null;
        urlVar = null;
        display = true;
        title = null;
        titleKey = null;
    }

    protected boolean isCurrentBrowsingLanguage(final String languageCode) {
        final String currentLanguageCode = getRenderContext().getMainResource().getLocale().toString();
        return currentLanguageCode.equals(languageCode);
    }

    public void setUrlVar(String urlVar) {
        this.urlVar = urlVar;
    }
}

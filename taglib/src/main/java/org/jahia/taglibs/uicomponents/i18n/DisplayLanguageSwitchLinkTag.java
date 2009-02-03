/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.uicomponents.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.LanguageCodeConverters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * @author Xavier Lawrence
 */
public class DisplayLanguageSwitchLinkTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayLanguageSwitchLinkTag.class);

    public static final String FLAG = "flag";
    public static final String NAME_CURRENT_LOCALE = "nameCurrentLocale";
    public static final String NAME_IN_LOCALE = "nameInLocale";
    public static final String LETTER = "letter";
    public static final String DOUBLE_LETTER = "doubleLetter";
    public static final String LANGUAGE_CODE = "languageCode";

    private String languageCode;
    private String linkKind;
    private String valueID;
    private String urlValueID;
    private boolean display = true;
    private String onLanguageSwitch;
    private String redirectCssClassName;
    private String title;
    private String titleKey;

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public void setLinkKind(String linkKind) {
        this.linkKind = linkKind;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public void setUrlValueID(String urlValueID) {
        this.urlValueID = urlValueID;
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
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();
            final JspWriter out = pageContext.getOut();
            final StringBuffer buff = new StringBuffer();

            final boolean isCurrentBrowsingLanguage = isCurrentBrowsingLanguage(languageCode, jParams);
            final boolean isRedirectToHomePageActivated = LanguageSwitchTag.GO_TO_HOME_PAGE.equals(onLanguageSwitch);

            if (!isCurrentBrowsingLanguage) {
                if (isRedirectToHomePageActivated) {
                    if (redirectCssClassName == null || redirectCssClassName.length() == 0) {
                        redirectCssClassName = LanguageSwitchTag.REDIRECT_DEFAULT_STYLE;
                    }
                    buff.append("<div class='");
                    buff.append(redirectCssClassName);
                    buff.append("'>");
                }
                buff.append("<a href='");
                try {
                    final String link;
                    if (onLanguageSwitch == null || onLanguageSwitch.length() == 0 ||
                            LanguageSwitchTag.STAY_ON_CURRENT_PAGE.equals(onLanguageSwitch)) {
                        link = jData.gui().drawPageLanguageSwitch(languageCode);

                    } else if (isRedirectToHomePageActivated) {
                        link = jData.gui().drawPageLanguageSwitch(languageCode, jParams.getSite().getHomePageID());

                    } else {
                        throw new JspTagException("Unknown onLanguageSwitch attribute value " + onLanguageSwitch);
                    }

                    buff.append(link);
                    if (urlValueID != null && urlValueID.length() > 0) {
                        pageContext.setAttribute(urlValueID, link);
                    }
                } catch (final JahiaException je) {
                    logger.error("Error while writing the language switch link !", je);
                }
                buff.append("' ");
                buff.append("title='");
                if (isRedirectToHomePageActivated) {
                    titleKey += "." + onLanguageSwitch;
                }
                buff.append(resolveTitle(title, getBundleKey(), titleKey, jParams.getLocale()));
                buff.append("'>");
            } else {
                buff.append("<span>");
                if (urlValueID != null) pageContext.removeAttribute(urlValueID, PageContext.PAGE_SCOPE);
            }

            if (linkKind == null || linkKind.length() == 0 || LANGUAGE_CODE.equals(linkKind) ||
                    FLAG.equals(linkKind)) {
                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, languageCode);
                }
                buff.append(languageCode);

            } else if (NAME_CURRENT_LOCALE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(jParams.getLocale());
                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, value);
                }
                buff.append(value);

            } else if (NAME_IN_LOCALE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale);
                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, value);
                }
                buff.append(value);

            } else if (LETTER.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale).substring(0, 1).toUpperCase();
                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, value);
                }
                buff.append(value);

            } else if (DOUBLE_LETTER.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(locale).substring(0, 2).toUpperCase();
                if (valueID != null && valueID.length() > 0) {
                    pageContext.setAttribute(valueID, value);
                }
                buff.append(value);

            } else {
                throw new JspTagException("Unknown linkKind value '" + linkKind + "'");
            }

            if (!isCurrentBrowsingLanguage) {
                buff.append("</a>");
                if (isRedirectToHomePageActivated) buff.append("</div>");
            } else {
                buff.append("</span>");
            }

            if (display) out.print(buff.toString());

        } catch (final Exception e) {
            logger.error("Error while getting language switch URL", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        onLanguageSwitch = null;
        redirectCssClassName = null;
        linkKind = null;
        languageCode = null;
        valueID = null;
        urlValueID = null;
        display = true;
        title = null;
        titleKey = null;
        resetState();
        return EVAL_PAGE;
    }

    protected boolean isCurrentBrowsingLanguage(final String languageCode,
                                                final ProcessingContext jParams) {
        final String currentLanguageCode = jParams.getLocale().toString();
        return currentLanguageCode.equals(languageCode);
    }
}

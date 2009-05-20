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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.LanguageCodeConverters;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.util.Locale;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayLanguageSwitchLinkTag extends ValueJahiaTag {

    private static final transient Logger logger = Logger.getLogger(DisplayLanguageSwitchLinkTag.class);

    public static final String FLAG = "flag";
    public static final String NAME_CURRENT_LOCALE = "nameCurrentLocale";
    public static final String NAME_IN_LOCALE = "nameInLocale";
    public static final String LETTER = "letter";
    public static final String DOUBLE_LETTER = "doubleLetter";
    public static final String LANGUAGE_CODE = "languageCode";
    public static final String ISOLOCALECOUNTRY_CODE = "isoLocaleCountryCode";

    private String languageCode;
    private String linkKind;
    /** 
     * @deprecated use {@link #urlValueID} instead
     */
    private String urlValueID;
    private String urlVar;
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

    /**
     * @deprecated use {@link #setUrlVar(String)} instead
     */
    public void setUrlValueID(String urlValueID) {
        if (logger.isDebugEnabled()) {
            logger.debug("The urlValueID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use urlVar attribute instead.",
                    new JspException());
        } else {
            logger.info("The urlValueID attribute is deprecated for tag "
                    + StringUtils.substringAfterLast(this.getClass().getName(),
                            ".") + ". Please, use urlVar attribute instead.");
        }
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
            final JahiaData jData = getJahiaData();
            final ProcessingContext jParams = getProcessingContext();
            final StringBuilder buff = new StringBuilder();

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

                    buff.append(StringEscapeUtils.escapeXml(link));
                    if (urlValueID != null && urlValueID.length() > 0) {
                        pageContext.setAttribute(urlValueID, link);
                    }
                    if (urlVar != null && urlVar.length() > 0) {
                        pageContext.setAttribute(urlVar, link);
                    }
                } catch (final JahiaException je) {
                    logger.error("Error while writing the language switch link !", je);
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
                if (urlValueID != null) pageContext.removeAttribute(urlValueID, PageContext.PAGE_SCOPE);
                if (urlVar != null) pageContext.removeAttribute(urlVar, PageContext.PAGE_SCOPE);
            }

            String attributeValue = null; 
            if (linkKind == null || linkKind.length() == 0 || LANGUAGE_CODE.equals(linkKind) ||
                    FLAG.equals(linkKind)) {
                attributeValue = languageCode;
                buff.append(languageCode);

            } else if (NAME_CURRENT_LOCALE.equals(linkKind)) {
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
                final String value = locale.getDisplayName(jParams.getLocale());
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
                if(locale.getCountry()!=null && locale.getCountry().length() != 0) {
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
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), attributeValue);
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
        urlValueID = null;
        urlVar = null;
        display = true;
        title = null;
        titleKey = null;
    }

    protected boolean isCurrentBrowsingLanguage(final String languageCode,
                                                final ProcessingContext jParams) {
        final String currentLanguageCode = jParams.getLocale().toString();
        return currentLanguageCode.equals(languageCode);
    }

    public void setUrlVar(String urlVar) {
        this.urlVar = urlVar;
    }
}

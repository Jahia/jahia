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

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayLanguageFlagTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(DisplayLanguageFlagTag.class);

    private String languageCode;
    private String onLanguageSwitch;
    private String redirectCssClassName;
    private String title;
    private String titleKey;

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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

    public int doStartTag() throws JspTagException {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
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
        }
        buff.append("<span class='flag flag_");
        buff.append(languageCode);
        if (!isCurrentBrowsingLanguage) {
            buff.append("_off");
        } else {
            buff.append("_on");
        }
        buff.append("'>&nbsp;</span>");

        if (!isCurrentBrowsingLanguage) {
            buff.append("</a>");
            if (isRedirectToHomePageActivated) buff.append("</div>");
        }

        try {
            final JspWriter out = pageContext.getOut();
            out.write(buff.toString());
            out.flush();
        } catch (final IOException ie) {
            logger.error("IOException while writing to JSP", ie);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        onLanguageSwitch = null;
        redirectCssClassName = null;
        languageCode = null;
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

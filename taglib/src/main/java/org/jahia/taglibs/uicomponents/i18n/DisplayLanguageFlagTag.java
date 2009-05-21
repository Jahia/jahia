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

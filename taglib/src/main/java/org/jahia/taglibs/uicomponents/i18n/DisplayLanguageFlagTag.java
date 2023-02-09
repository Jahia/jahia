/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.LanguageCodeConverters;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayLanguageFlagTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(DisplayLanguageFlagTag.class);

    private String languageCode;
    private String onLanguageSwitch;
    private String redirectCssClassName;
    private String flagType;
    private String title;
    private String titleKey;
    private JCRNodeWrapper rootPage;


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

    public void setRootPage(JCRNodeWrapper rootPage) {
        this.rootPage = rootPage;
    }

    public int doStartTag() throws JspTagException {
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
            if (onLanguageSwitch == null || onLanguageSwitch.length() == 0 || InitLangBarAttributes.STAY_ON_CURRENT_PAGE.equals(
                    onLanguageSwitch)) {
                link = generateCurrentNodeLangSwitchLink(languageCode) + "##requestParameters##";

            } else if (isRedirectToHomePageActivated) {
                link = generateNodeLangSwitchLink(rootPage, languageCode) + "##requestParameters##";

            } else {
                throw new JspTagException("Unknown onLanguageSwitch attribute value " + onLanguageSwitch);
            }

            buff.append(link);
            buff.append("' ");
            buff.append("title='");
            if (isRedirectToHomePageActivated) {
                titleKey += "." + onLanguageSwitch;
            }
            buff.append(getMessage(titleKey, title));
            buff.append("'>");
        }
        buff.append("<span class='flag ");
        Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
        if (locale.getCountry() != null && locale.getCountry().length() > 0) {
            buff.append("flag_").append(languageCode);
            if (flagType != null) {
                if (flagType.equals("plain")) {
                    buff.append("_plain");
                } else if (flagType.equals("shadow")) {
                    buff.append("_shadow");
                }
            }
        } else {
            buff.append("flag_").append(languageCode);
            if (flagType != null) {
                if (flagType.equals("plain")) {
                    buff.append("_plain");
                } else if (flagType.equals("shadow")) {
                    buff.append("_shadow");
                }
            }

            if (!isCurrentBrowsingLanguage) {
                buff.append("_off");
            } else {
                buff.append("_on");
            }
        }
        buff.append("'>&nbsp;</span>");

        if (!isCurrentBrowsingLanguage) {
            buff.append("</a>");
            if (isRedirectToHomePageActivated) {
                buff.append("</div>");
            }
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

    protected boolean isCurrentBrowsingLanguage(final String languageCode) {
        final String currentLanguageCode = getRenderContext().getMainResource().getLocale().toString();
        return currentLanguageCode.equals(languageCode);
    }

    public String getFlagType() {
        return flagType;
    }

    public void setFlagType(String flagType) {
        this.flagType = flagType;
    }
}

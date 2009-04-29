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
package org.jahia.data.viewhelper.sitemap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;

public class LimitedTemplatesFilter extends PagesFilter {
    /** logging */
    private static final transient Logger logger = Logger.getLogger(LimitedTemplatesFilter.class);

    Set<String> allowedTemplates = new HashSet<String>();

    public void setFieldDefaultValue(String aFieldDefaultValue) {
        super.setFieldDefaultValue(aFieldDefaultValue);

        if (aFieldDefaultValue.indexOf("[") != -1) {
            int p1 = aFieldDefaultValue.indexOf("[");
            int p2 = aFieldDefaultValue.lastIndexOf("]");
            String tList = aFieldDefaultValue.substring(p1, p2 + 1);
            aFieldDefaultValue = StringUtils.replace(aFieldDefaultValue, tList,
                    "");

            tList = tList.substring(1, tList.length() - 1);
            allowedTemplates = getStringToSet(tList);
            logger.debug("tlist: " + allowedTemplates);
        }
    }

    /**
     * utility method
     * 
     * @param s
     *            the long string
     * @return a Set of string
     */
    private Set<String> getStringToSet(final String s) {
        final Set<String> vlist = new HashSet<String>();
        final StringTokenizer tok = new StringTokenizer(s, ",");
        while (tok.hasMoreTokens()) {
            final String v = tok.nextToken().trim();
            vlist.add(v);
        }
        return vlist;
    }

    public boolean filterForDisplay(ContentPage contentPage, ProcessingContext jParams) {
        return false;
    }

    public boolean filterForSelection(ContentPage contentPage, ProcessingContext jParams) {
        return allowedTemplates.size() == 0
                || allowedTemplates.contains(getPageTemplateName(contentPage,
                        jParams)) ? false : true;
    }

    public boolean filterForRelease(ContentPage contentPage, ProcessingContext jParams) {
        return false;
    }

    public List<ContentPage> filterChildren(ContentPage contentPage, List<ContentPage> childPages,
            ProcessingContext jParams) {
        return childPages;
    }

    public List<ContentPage> getStartPages(ContentPage startPage, JahiaUser user,
            int pageInfosFlag, List<Locale> locales, ProcessingContext jParams) {
        List<ContentPage> startPages = new ArrayList<ContentPage>();

        startPages.add(startPage);

        return startPages;
    }
}

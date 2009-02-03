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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

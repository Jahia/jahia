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
package org.jahia.data.viewhelper.sitemap;

import java.util.List;
import java.util.Locale;

import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.usermanager.JahiaUser;

/**
 * @author pap
 */
public abstract class PagesFilter {
    private String fieldDefaultValue;

    public abstract boolean filterForDisplay(ContentPage contentPage,
            ProcessingContext jParams);

    public abstract boolean filterForSelection(ContentPage contentPage,
            ProcessingContext jParams);

    public abstract boolean filterForRelease(ContentPage contentPage,
            ProcessingContext jParams);

    public abstract List<ContentPage> filterChildren(ContentPage contentPage,
            List<ContentPage> childPages, ProcessingContext jParams);

    protected static String getPageTemplateName(ContentPage aContentPage,
            ProcessingContext aJParams) {
        if (aContentPage == null)
            return null;

        JahiaPageDefinition pageTemplate = aContentPage
            .getPageTemplate(aJParams);
        if (pageTemplate == null)
            return null;

        return pageTemplate.getName();
    }

    protected static String getPageTemplateName(JahiaPage aJahiaPage,
            ProcessingContext aJParams) {
        if (aJahiaPage == null)
            return null;

        JahiaPageDefinition pageTemplate = aJahiaPage.getPageTemplate();
        if (pageTemplate == null)
            return null;

        return pageTemplate.getName();
    }

    public String getFieldDefaultValue() {
        return fieldDefaultValue;
    }

    public void setFieldDefaultValue(String aFieldDefaultValue) {
        this.fieldDefaultValue = aFieldDefaultValue;
    }
    
    public boolean isSearchTabRequired() {
        return false;
    }    

    public boolean isDirectPagesOnly() {
        return true;
    }       
    
    public abstract List<ContentPage> getStartPages(
      ContentPage startPage,    
      JahiaUser user,
      int pageInfosFlag, 
      List<Locale> locales,
      ProcessingContext jParams);    
}

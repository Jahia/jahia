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

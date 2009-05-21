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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPageBaseService;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.utils.LanguageCodeConverters;

/**
 * <p>Title: Informations site map page</p>
 * <p>Description: Node information of each Jahia page contained in the
 * site map.
 * Private class, should be used with the site map view helper.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
final class PageSiteMap implements Serializable {

    public PageSiteMap(int pageID, int pageLevel, boolean hasChild,
                       int parentPageID, boolean isLastSister, Map titles,
                       int currentLevel, int defaultMaxLevel) {
        this.pageLevel = pageLevel;
        this.pageID = pageID;
        this.objectKey = new ContentPageKey(pageID);
        this.hasChild = hasChild;
        this.parentPageID = parentPageID;
        this.isLastSister = isLastSister;
        this.titles = titles;
        this.expanded = currentLevel < defaultMaxLevel && hasChild;
        this.displayable = currentLevel <= defaultMaxLevel;
        this.showInformation = false;
        this.showWarnings = false;
        this.showErrors = false;
        this.showEvents = false;
    }

    public PageSiteMap(ObjectKey objectKey, int pageLevel, boolean hasChild,
                       int parentPageID, boolean isLastSister, Map titles,
                       int currentLevel, int defaultMaxLevel) {
        this.pageLevel = pageLevel;
        if (objectKey.getType() .equals(ContentPageKey.PAGE_TYPE)) {
            this.pageID = Integer.parseInt(objectKey.getIDInType());
        } else {
            this.pageID = 0;
        }
        this.objectKey = objectKey;
        this.hasChild = hasChild;
        this.parentPageID = parentPageID;
        this.isLastSister = isLastSister;
        this.titles = titles;
        this.expanded = currentLevel < defaultMaxLevel && hasChild;
        this.displayable = currentLevel <= defaultMaxLevel;
        this.showInformation = false;
        this.showWarnings = false;
        this.showErrors = false;
        this.showEvents = false;
    }

    public PageSiteMap(ObjectKey objectKey,
                       Map titles) {
        if (objectKey.getType() .equals(ContentPageKey.PAGE_TYPE)) {
            this.pageID = Integer.parseInt(objectKey.getIDInType());
        } else {
            this.pageID = 0;
        }
        this.objectKey = objectKey;
        this.titles = titles;
        displayable = true;
    }

    public int getPageID() {
        return pageID;
    }

    public ObjectKey getObjectKey() {
        return objectKey;
    }

    public int getPageLevel() {
        return pageLevel;
    }

    public boolean hasChild() {
        return hasChild;
    }

    public int getParentPageID() {
        return parentPageID;
    }

    public boolean isLastSister() {
        return isLastSister;
    }

    public String getPageTitle(String languageCode) {
        return getAPageTitleAnyway(languageCode);
    }

    public boolean isDisplayable() {
        return displayable;
    }

    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setShowInformation(boolean showInformation) {
        this.showInformation = showInformation;
    }

    public boolean isShowInformation() {
        return showInformation;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public boolean isShowWarnings() {
        return showWarnings;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public boolean isShowErrors() {
        return showErrors;
    }

    public void setShowEvents(boolean showEvents) {
        this.showEvents = showEvents;
    }

    public boolean isShowEvents() {
        return showEvents;
    }

    private String getAPageTitleAnyway(String languageCode) {
        String pageTitle1 = ((String)this.titles.get(languageCode));
        if (pageTitle1 == null) {
            Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);    
            if (objectKey.getType().equals(ContentPageKey.PAGE_TYPE)) {
                try {
                    ContentPage contentPage = JahiaPageBaseService.getInstance().
                            lookupContentPage(pageID, false);
                    if (contentPage != null && contentPage.isStagedEntryMarkedForDeletion(languageCode)) {
                        Map titles = contentPage.getTitles(ContentPage.ACTIVATED_PAGE_TITLES);
                        pageTitle1 = (String)titles.get(languageCode);
                    } else {
                        String msgFormat = 
                            JahiaResourceBundle.getMessageResource("org.jahia.engines.workflow.pageNotApplicable",
                             locale);          
                        Object[] arguments = {new Integer(this.pageID), 
                                LanguageCodeConverters.languageCodeToLocale(languageCode).getDisplayName(locale)};
                        pageTitle1 = MessageFormat.format(msgFormat, arguments);
                        // pageTitle1 = "Page title (" + this.pageID + ") N/A in ";
                        // pageTitle1 += LanguageCodeConverters.languageCodeToLocale(languageCode).getDisplayName();
                    }
                } catch (JahiaException je) {
                    logger.debug("Cannot recover the page with ID " + pageID);
                    return JahiaResourceBundle.getMessageResource("org.jahia.engines.workflow.pageNotExisting",
                            locale);
                }
            } else {
                try {
                    ContentObject object = ContentObject.getContentObjectInstance(objectKey);
                    ContentDefinition def = ContentDefinition.getContentDefinitionInstance(object.getDefinitionKey(null));
                    pageTitle1 = def.getName();
                    if (objectKey.getType().equals(ContentContainerKey.CONTAINER_TYPE)) {
                        pageTitle1 += " (Container " + objectKey.getIDInType() + ")";
                        try {
                            List l = object.getChilds(null,null,JahiaContainerStructure.JAHIA_FIELD);
                            for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                                ContentObject contentObject = (ContentObject) iterator.next();
                                if (contentObject instanceof ContentSmallTextField) {
                                    pageTitle1 = ((ContentSmallTextField)contentObject).getValue((ContentObjectEntryState) contentObject.getActiveAndStagingEntryStates().last()) + " (Container " + objectKey.getIDInType() + ")";
                                    break;
                                }
                            }
                        } catch (JahiaException e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else if (objectKey.getType().equals(ContentContainerListKey.CONTAINERLIST_TYPE)) {
                        pageTitle1 += " (List " + objectKey.getIDInType() + ")";
                    } else if (objectKey.getType().equals(ContentFieldKey.FIELD_TYPE)) {
                        pageTitle1 += " (Field " + objectKey.getIDInType() + ")";
                    }
                } catch (ClassNotFoundException e) {
                    pageTitle1 = objectKey.toString();
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return pageTitle1;
    }

    // Page status attributes
    private  int pageID;
    private  ObjectKey objectKey;
    private  int pageLevel;
    private  boolean hasChild;
    private  int parentPageID;
    private  boolean isLastSister;
    private  Map titles;
    // Display parameters
    private boolean expanded;
    private boolean displayable;
    private boolean showInformation;
    private boolean showWarnings;
    private boolean showErrors;
    private boolean showEvents;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(PageSiteMap.class);
}

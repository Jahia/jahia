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
 package org.jahia.services.pages;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

import java.util.*;

/**
 * Class JahiaPageBaseService
 *
 * @author Eric Vassalli
 * @author Khue
 * @author Fulco Houkes
 * @version 1.0
 */
public class JahiaPageBaseService extends JahiaPageService {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaPageBaseService.class);

    private static JahiaPageBaseService instance;

    public static final String CONTENT_PAGE_CACHE = "ContentPageCache";

    protected JahiaPageBaseService() {
    }

    public static synchronized JahiaPageBaseService getInstance()
            throws JahiaException {
        if (instance == null) {
            instance = new JahiaPageBaseService();
        }
        return instance;
    }

    public List<JahiaPageContentRights> getPageIDsWithAclIDs(Set<Integer> aclIDs) throws JahiaException {
        return null;
    }

    public int getPageFieldID(int pageID)
            throws JahiaException {
        return 0;
    }

    public List<JahiaPage> getPageChilds (int pageID, int loadFlag,
                                 EntryLoadRequest loadRequest)
            throws JahiaException {
        return null;
    }

    public List<JahiaPage> getPageChilds(int pageID, int loadFlag, ProcessingContext jParam)
            throws JahiaException {
        return null;
    }

    public List<JahiaPage> getPageChilds(int pageID, int loadFlag, JahiaUser user)
            throws JahiaException {
        return null;
    }

    public List<JahiaPage> getPageChilds (int pageID, int loadFlag, JahiaUser user,
                                 EntryLoadRequest loadRequest)
            throws JahiaException {
        return null;

    }

    public List<ContentPage> getDirectContentPageChilds(int pageID, JahiaUser user,
                                             int pageInfosFlag,
                                             String languageCode)
            throws JahiaException {
        return null;
    }

    public List<ContentPage> getContentPageChilds (int pageID,
                                        JahiaUser user,
                                        int pageInfosFlag,
                                        String languageCode,
                                        int versionId,
                                        boolean directPageOnly)
        throws JahiaException {
        return null;
    }

    public List<ContentPage> getContentPageChilds (int pageID, JahiaUser user,
                                      int pageInfosFlag,
                                      String languageCode,
                                      boolean directPageOnly)
            throws JahiaException {
        return null;
    }

    public List<ContentPage> getContentPagePath(int pageID, EntryLoadRequest loadRequest,
                                     String opMode, JahiaUser user)
            throws JahiaException {
        return null;
    }

    public List<ContentPage> getContentPagePath(int pageID, EntryLoadRequest loadRequest,
                                     String opMode, JahiaUser user, int command)
            throws JahiaException {
        return null;
    }


    public synchronized void start()
        throws JahiaInitializationException {
    }

    public JahiaPage lookupPage(int pageID, ProcessingContext jParam)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return null;
    }

    public JahiaPage lookupPage(int pageID, EntryLoadRequest loadRequest)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return null;
    }

    public ContentPage lookupContentPage(int pageID,
                                         boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return null;
    }

    public ContentPage lookupContentPage (int pageID,
                                          boolean withTemplates,
                                          boolean forceLoadFromDB)
        throws JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException {
        return null;
    }

    public ContentPage lookupContentPage(int pageID,
                                         EntryLoadRequest loadRequest,
                                         boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return null;
    }

    public ContentPage lookupContentPage(int pageID,
            EntryLoadRequest loadRequest, boolean withTemplates,
            boolean forceLoadFromDB) throws JahiaException,
            JahiaPageNotFoundException, JahiaTemplateNotFoundException {
        return null;
    }

    public JahiaPage lookupPage(int pageID, EntryLoadRequest loadRequest,
                                String operationMode, JahiaUser user,
                                boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return null;
    }

    public List<ContentPage> findPagesByPropertyNameAndValue(String name, String value) throws JahiaException {
        return null;
    }

    public List<Object[]> getPagePropertiesByName(String name) {
        return null;
    }

    public synchronized void stop() {
    }

    public int getRealActiveNbPages()
            throws JahiaException {
        return 0;
    }

    public int getRealActiveNbPages(int siteID)
            throws JahiaException {
        return 0;
    }

    public void invalidatePageCache(int pageID) {        
    }

    /**
     * Return the map containing the page's properties
     *
     * @param pageID
     * @return map
     */
    public Map<String, PageProperty> getPageProperties(int pageID)
            throws JahiaException {
        return null;
    }

    public List<PageProperty> getPagePropertiesByValue (String propertyValue)
        throws JahiaException {
        return null;
    }

    public List<PageProperty> getPagePropertiesByValueAndSiteID (String propertyValue, int siteID)
        throws JahiaException {
        return null;
    }

    public List<PageProperty> getPagePropertiesByNameValueSiteIDAndParentID (String propertyName, String propertyValue, int siteID, int parentPageID)
        throws JahiaException {
        return null;
    }

    public List<Integer> sortPages(List<Integer> pageIDs, EntryLoadRequest loadRequest,
                            JahiaUser user, String operationMode)
    throws JahiaException {
        return null;
    }

    public int getParentPageFieldId(int pageId,EntryLoadRequest loadRequest) {
        return 0;
    }

    public void loadPage(int pageID, ProcessingContext jParams) throws JahiaException {        
    }
}

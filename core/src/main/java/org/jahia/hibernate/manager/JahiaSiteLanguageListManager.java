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

/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.FastArrayList;
import org.apache.log4j.Logger;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.dao.JahiaSiteLanguageListDAO;
import org.jahia.hibernate.model.JahiaSiteLangList;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.SiteLanguageSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 avr. 2005
 * Time: 18:08:20
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSiteLanguageListManager {
// ------------------------------ FIELDS ------------------------------

    private static final String CACHE_NAME = "JahiaSiteLanguageListManager";
    private static final String CACHE_KEY_PREFIX = "JahiaSiteLanguage_";

    private JahiaSiteDAO siteDAO = null;
    private JahiaSiteLanguageListDAO dao = null;

    private CacheService cacheService = null;
    private Cache<String, List<SiteLanguageSettings>> siteLanguageCache = null;
    private Logger log = Logger.getLogger(getClass());

// --------------------- GETTER / SETTER METHODS ---------------------

    public List<String> getAllSitesLanguages() {
        return dao.getAllSitesLanguages();
    }

    public void setJahiaSiteDAO(JahiaSiteDAO siteDAO) {
        this.siteDAO = siteDAO;
    }

    public void setJahiaSiteLanguageListDAO(JahiaSiteLanguageListDAO dao) {
        this.dao = dao;
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

// -------------------------- OTHER METHODS --------------------------

    public void addSiteLanguageSettings(SiteLanguageSettings curSetting) {
        JahiaSiteLangList langList = new JahiaSiteLangList();
        langList.setActivated(Boolean.valueOf(curSetting.isActivated()));
        langList.setCode(curSetting.getCode());
        langList.setMandatory(Boolean.valueOf(curSetting.isMandatory()));
        langList.setRank(Integer.valueOf(curSetting.getRank()));
        langList.setSite(siteDAO.findById(new Integer(curSetting.getSiteID())));
        dao.save(langList);
        curSetting.setID(langList.getId().intValue());
        flushCache(curSetting.getSiteID());
    }

    public void updateSiteLanguageSettings(SiteLanguageSettings curSetting) {
        JahiaSiteLangList langList = new JahiaSiteLangList();
        langList.setActivated(Boolean.valueOf(curSetting.isActivated()));
        langList.setCode(curSetting.getCode());
        langList.setId(Integer.valueOf(curSetting.getID()));
        langList.setMandatory(Boolean.valueOf(curSetting.isMandatory()));
        langList.setRank(new Integer(curSetting.getRank()));
        langList.setSite(siteDAO.findById(new Integer(curSetting.getSiteID())));
        dao.update(langList);
        flushCache(curSetting.getSiteID());
    }

    public List<SiteLanguageSettings> getSiteLanguages(int siteId) {
        return getSiteLanguages(siteId,false);
    }

    /**
     *
     * @param siteId
     * @param forceLoadFromDB if true, bypass cache
     * @return
     */
    public List<SiteLanguageSettings> getSiteLanguages(int siteId, boolean forceLoadFromDB) {

        List<SiteLanguageSettings> retList = Collections.emptyList();
        try {
            if(siteLanguageCache == null) {
                siteLanguageCache = cacheService.createCacheInstance(CACHE_NAME);
            }
            if (!forceLoadFromDB) {
                List<SiteLanguageSettings> langsList = (List<SiteLanguageSettings>) siteLanguageCache
                        .get(new StringBuffer(CACHE_KEY_PREFIX).append(siteId).toString());
                if (langsList != null) {
                    return langsList;
                }
            }

            List<JahiaSiteLangList> settings = dao.getSiteLanguages(new Integer(siteId));
            List<SiteLanguageSettings> tempList = new FastArrayList(settings.size());
            for (JahiaSiteLangList langList : settings) {
                SiteLanguageSettings languageSettings = new SiteLanguageSettings(langList.getId().intValue(),
                                                                                 langList.getSite().getId().intValue(),
                                                                                 langList.getCode(),
                                                                                 langList.getActivated().booleanValue(),
                                                                                 langList.getRank().intValue(),
                                                                                 langList.getMandatory().booleanValue(),
                                                                                 true);
                tempList.add(languageSettings);
            }
            ((FastArrayList)tempList).setFast(true);
            retList = tempList;
            siteLanguageCache.put(new StringBuffer(CACHE_KEY_PREFIX).append(siteId).toString(),retList);
        } catch ( Exception t ){
            log.debug("Error loading site language settings");
        }
        return retList;
    }

    public void removeSiteLanguageSettings(int id) {
        dao.delete(new Integer(id));
        flushCache();
    }

    private void flushCache(int siteId) {
        if(siteLanguageCache != null) {
            siteLanguageCache.remove(CACHE_KEY_PREFIX+siteId);
        }
    }

    private void flushCache() {
        if(siteLanguageCache != null) {
            siteLanguageCache.flush(true);
        }
    }

}


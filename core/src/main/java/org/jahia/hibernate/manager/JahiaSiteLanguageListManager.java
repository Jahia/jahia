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


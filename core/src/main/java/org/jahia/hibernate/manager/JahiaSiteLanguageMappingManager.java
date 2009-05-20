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

import java.util.List;

import org.apache.commons.collections.FastArrayList;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.dao.JahiaSiteLanguageMappingDAO;
import org.jahia.hibernate.model.JahiaSiteLangMap;
import org.jahia.services.sites.SiteLanguageMapping;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 20 avr. 2005
 * Time: 17:23:57
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSiteLanguageMappingManager {
    private JahiaSiteLanguageMappingDAO dao;
    private JahiaSiteDAO siteDAO;
    public void setJahiaSiteLanguageMappingDAO(JahiaSiteLanguageMappingDAO dao) {
        this.dao = dao;
    }

    public void setJahiaSiteDAO(JahiaSiteDAO siteDAO) {
        this.siteDAO = siteDAO;
    }

    public List<SiteLanguageMapping> getSiteLanguageMappings(int id) {
        List<JahiaSiteLangMap> mappings = dao.getSiteLanguageMappings(new Integer(id));
        FastArrayList retList = new FastArrayList(mappings.size());
        for (JahiaSiteLangMap map : mappings) {
            SiteLanguageMapping mapping = new SiteLanguageMapping(map.getId().intValue(),map.getSite().getId().intValue(),
                                                                  map.getFromLanguageCode(), map.getToLanguageCode());
            retList.add(mapping);
        }
        retList.setFast(true);
        return retList;
    }

    public void addSiteLanguageMapping(SiteLanguageMapping newMapping) {
        JahiaSiteLangMap jahiaSiteLangMap = new JahiaSiteLangMap();
        jahiaSiteLangMap.setFromLanguageCode(newMapping.getFromLanguageCode());
        jahiaSiteLangMap.setSite(siteDAO.findById(new Integer(newMapping.getSiteID())));
        jahiaSiteLangMap.setToLanguageCode(newMapping.getToLanguageCode());
        dao.save(jahiaSiteLangMap);
        newMapping.setId(jahiaSiteLangMap.getId().intValue());

    }

    public void removeSiteLanguageMapping(int id) {
        dao.delete(new Integer(id));
    }

    public void updateSiteLanguageMapping(SiteLanguageMapping curMapping) {
        JahiaSiteLangMap jahiaSiteLangMap = new JahiaSiteLangMap();
        jahiaSiteLangMap.setFromLanguageCode(curMapping.getFromLanguageCode());
        jahiaSiteLangMap.setId(new Integer(curMapping.getId()));
        jahiaSiteLangMap.setSite(siteDAO.findById(new Integer(curMapping.getSiteID())));
        jahiaSiteLangMap.setToLanguageCode(curMapping.getToLanguageCode());
        dao.update(jahiaSiteLangMap);
        curMapping.setId(jahiaSiteLangMap.getId().intValue());
    }
}

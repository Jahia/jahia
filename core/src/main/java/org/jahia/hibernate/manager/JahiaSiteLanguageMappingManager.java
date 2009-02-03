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

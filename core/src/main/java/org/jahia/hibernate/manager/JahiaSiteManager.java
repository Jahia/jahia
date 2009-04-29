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
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.dao.JahiaSitePropertyDAO;
import org.jahia.hibernate.model.JahiaSiteProp;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.cache.CacheService;
import org.jahia.services.cache.Cache;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 déc. 2004
 * Time: 18:03:01
 * To change this template use File | Settings | File Templates.
 */
public class JahiaSiteManager {
    private Log log = LogFactory.getLog(JahiaSiteManager.class);
    private JahiaSiteDAO dao = null;
    private JahiaSitePropertyDAO sitePropertyDAO = null;
    private CacheService cacheService;

    public void setJahiaSiteDAO(JahiaSiteDAO dao) {
        this.dao = dao;
    }

    public void setSitePropertyDAO(JahiaSitePropertyDAO sitePropertyDao) {
        this.sitePropertyDAO = sitePropertyDao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void saveJahiaSite(JahiaSite site) {
        org.jahia.hibernate.model.JahiaSite jahiaSite = convertServiceJahiaSiteToModelJahiaSite(site);
        this.dao.save(jahiaSite);
        site.setID(jahiaSite.getId().intValue());
    }

    public void updateJahiaSite(JahiaSite site) {
        org.jahia.hibernate.model.JahiaSite jahiaSite = convertServiceJahiaSiteToModelJahiaSite(site);
        this.dao.update(jahiaSite);
        site.setID(jahiaSite.getId().intValue());
    }

    public void remove(int id) {
        this.dao.remove(new Integer(id));
    }

    public JahiaSite getSiteByKey(String key) {
        try {
            org.jahia.hibernate.model.JahiaSite jahiaSite = this.dao.findByKey(key);
            return convertModelJahiaSiteToServiceJahiaSite(jahiaSite);
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public JahiaSite getSiteByName(String key) {
        try {
            org.jahia.hibernate.model.JahiaSite jahiaSite = this.dao.findByServername(key);
            return convertModelJahiaSiteToServiceJahiaSite(jahiaSite);
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public JahiaSite getSiteById(int id) {
        try {
            org.jahia.hibernate.model.JahiaSite jahiaSite = this.dao.findById(new Integer(id));
            return convertModelJahiaSiteToServiceJahiaSite(jahiaSite);
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public org.jahia.hibernate.model.JahiaSite getModelSiteById(int id) {
        try {
            org.jahia.hibernate.model.JahiaSite jahiaSite = this.dao.findById(new Integer(id));
            return jahiaSite;
        } catch (ObjectRetrievalFailureException e) {
            return null;
        }
    }

    public List<JahiaSite> getSites() {
        List<org.jahia.hibernate.model.JahiaSite> list = this.dao.getSites();
        List<JahiaSite> retList = new ArrayList<JahiaSite>(list.size());
        for (org.jahia.hibernate.model.JahiaSite jahiaSite : list) {
            JahiaSite convertedSite = null;
            Cache<Comparable<?>, JahiaSite> cache = cacheService.getCache(JahiaSitesBaseService.SITE_CACHE_BYID);
            if (cache != null) {
                convertedSite = cache.get(jahiaSite.getId());
            }
            if (convertedSite == null) {
                convertedSite = convertModelJahiaSiteToServiceJahiaSite(jahiaSite);
                if (cache != null) {
                    cache.put(jahiaSite.getId(), convertedSite);
                }
            }
            retList.add(convertedSite);
        }
        return retList;
    }

    /**
     * @param jahiaSite
     *
     * @return Returns a service jahia site
     *
     * @todo When implemented change the acl loading by a manager loading
     */
    private JahiaSite convertModelJahiaSiteToServiceJahiaSite(org.jahia.hibernate.model.JahiaSite jahiaSite) {
        final int id = jahiaSite.getId().intValue();
        final boolean active = integerToBoolean(jahiaSite.getActive());
        final int tplId = jahiaSite.getDefaulttemplateid().intValue();
        final String descr = jahiaSite.getDescr();
        final int pageId = jahiaSite.getDefaultpageid().intValue();
        final String servername = jahiaSite.getServername();

        JahiaBaseACL acl = null;
        try {
            acl = new JahiaBaseACL();
            acl.load(jahiaSite.getRights().intValue());

        } catch (JahiaException je) {
            log.debug("Error while retrieving site ACL : ", je);
            acl = null;
        }

        Properties props = new Properties();

        Iterator<JahiaSiteProp> iterator = sitePropertyDAO.getSitePropById(jahiaSite.getId()).iterator();

        while ( iterator.hasNext() ){
            JahiaSiteProp jahiaSiteProp = iterator.next();
            props.put(jahiaSiteProp.getComp_id().getName(),jahiaSiteProp.getValue() == null ? "" : jahiaSiteProp.getValue());
        }

        JahiaSite site = new JahiaSite(id, jahiaSite.getTitle(), servername, jahiaSite.getKey(), active, pageId, descr,
                                       acl, props);
        final boolean tplMode = integerToBoolean(jahiaSite.getTplDeploymode());
        site.setTemplatesAutoDeployMode(tplMode);
        final boolean webMode = integerToBoolean(jahiaSite.getWebappsDeploymode());
        site.setWebAppsAutoDeployMode(webMode);
        site.setDefaultTemplateID(tplId);
        return site;
    }

    private final static org.jahia.hibernate.model.JahiaSite convertServiceJahiaSiteToModelJahiaSite(JahiaSite site) {
        if (site == null) {
            return null;
        }
        org.jahia.hibernate.model.JahiaSite jahiaSite = new org.jahia.hibernate.model.JahiaSite();
        jahiaSite.setActive(booleanToInteger(site.isActive()));
        jahiaSite.setDefaultpageid(new Integer(site.getHomePageID()));
        jahiaSite.setDefaulttemplateid(new Integer(site.getDefaultTemplateID()));
        jahiaSite.setTitle(site.getTitle());
        jahiaSite.setKey(site.getSiteKey());
        jahiaSite.setDescr(site.getDescr());
        jahiaSite.setServername(site.getServerName());
        jahiaSite.setTplDeploymode(booleanToInteger(site.getTemplatesAutoDeployMode()));
        jahiaSite.setWebappsDeploymode(booleanToInteger(site.getWebAppsAutoDeployMode()));
        jahiaSite.setRights(new Integer(site.getAclID()));
        final int id = site.getID();
        if (id > 0) {
            jahiaSite.setId(new Integer(id));
        }
        return jahiaSite;
    }

    public static Integer booleanToInteger(boolean bool) {
        return new Integer(bool ? 1 : 0);
    }

    public static boolean integerToBoolean(Integer integer) {
        int i = integer.intValue();
        return i == 1;
    }

    public int getNbSites() {
        return dao.getNbSites();
    }

    public JahiaSite getDefaultSite() {
        org.jahia.hibernate.model.JahiaSite defaultSite = dao.getDefaultSite();
        if(defaultSite==null) return null;
        return convertModelJahiaSiteToServiceJahiaSite(defaultSite);
    }

    public void setDefaultSite(JahiaSite site) {
        dao.setDefaultSite(convertServiceJahiaSiteToModelJahiaSite(site));
    }

    public List<Integer> findSiteIdByPropertyNameAndValue(String name, String value) {
        return sitePropertyDAO.findSiteIdByPropertyNameAndValue(name, value);
    }
}

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
 package org.jahia.hibernate.manager;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.dao.JahiaAclDAO;
import org.jahia.hibernate.dao.JahiaSavedSearchDAO;
import org.jahia.hibernate.dao.JahiaSiteDAO;
import org.jahia.hibernate.dao.JahiaUserDAO;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * JahiaSearch Persistence Manager
 *
 */
public class JahiaSavedSearchManager {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger (JahiaSavedSearchManager.class);

    private JahiaAclDAO aclDao = null;
    private JahiaSavedSearchDAO savedSearchDao = null;
    private JahiaSiteDAO siteDao = null;
    private JahiaUserDAO userDao = null;


    public JahiaAclDAO getAclDao() {
        return aclDao;
    }

    public void setAclDao(JahiaAclDAO aclDao) {
        this.aclDao = aclDao;
    }

    public JahiaSavedSearchDAO getSearchDao() {
        return savedSearchDao;
    }

    public void setSearchDao(JahiaSavedSearchDAO savedSearchDao) {
        this.savedSearchDao = savedSearchDao;
    }

    public JahiaSiteDAO getSiteDao() {
        return siteDao;
    }

    public void setSiteDao(JahiaSiteDAO siteDao) {
        this.siteDao = siteDao;
    }

    public JahiaUserDAO getUserDao() {
        return userDao;
    }

    public void setUserDao(JahiaUserDAO userDao) {
        this.userDao = userDao;
    }

    public JahiaSavedSearch getSavedSearch( String title ) throws JahiaException {
         org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch hibSavedSearch =
                 this.savedSearchDao.getSearch(title);
        return getJahiaSavedSearchFromHibSavedSearch(hibSavedSearch);
    }

    public void addSearch(  JahiaSavedSearch savedSearch ) throws JahiaException {
        savedSearch.setId(null);
        saveSearch(savedSearch);
    }

    public void saveSearch(  JahiaSavedSearch savedSearch ) throws JahiaException {

        try {
            org.jahia.hibernate.model.JahiaSite site = siteDao.findById(new Integer(savedSearch.getSideId()));
            org.jahia.hibernate.model.JahiaAcl acl = aclDao.findAclById(new Integer(savedSearch.getAcl().getID()));
            org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch hibSavedSearch =
                    new org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch(
                            savedSearch.getId(),
                            savedSearch.getTitle(),
                            savedSearch.getDescr(),
                            savedSearch.getSearch(),
                            savedSearch.getCreationDate(),
                            savedSearch.getOwnerKey(),
                            savedSearch.getSearchViewHandlerClass(), site, acl );
            hibSavedSearch = savedSearchDao.save(hibSavedSearch);
            savedSearch.setId(hibSavedSearch.getId());
        } catch (Exception t) {
            throw new JahiaException(   "Error saving JahiaSavedSearch ",
                                        "Error saving JahiaSavedSearch ",
                                        JahiaException.DATA_ERROR,
                                        JahiaException.ERROR_SEVERITY,t);
        }
    }

    /**
     * Returns all saved searchs
     *
     * @return
     */
    public List<JahiaSavedSearch> getSearches() {
        return getJahiaSavedSearchFromListOfHibSavedSearch(savedSearchDao.getSearches());
    }

    /**
     * Returns all saved searchs for the user.
     * 
     * @param owner
     *            current user key
     * @return all saved searchs for the user
     */
    public List<JahiaSavedSearch> getSearches(String ownerKey) {
        return getJahiaSavedSearchFromListOfHibSavedSearch(savedSearchDao
                .getSearches(ownerKey));
    }

    /**
     * Delete a saved search
     *
     * @param searchId
     */
    public void deleteSearch(Integer searchId){
        savedSearchDao.deleteSearch(searchId);
    }

    /**
     *
     * @param hibSavedSearchs
     * @return
     */
    protected List<JahiaSavedSearch> getJahiaSavedSearchFromListOfHibSavedSearch (
            List hibSavedSearchs) {
        if ( hibSavedSearchs == null ){
            return null;
        }
        List savedSearchs = new ArrayList();
        Iterator iterator = hibSavedSearchs.iterator();
        JahiaSavedSearch savedSearch = null;
        while ( iterator.hasNext() ){
            savedSearch = getJahiaSavedSearchFromHibSavedSearch(
                    (org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch)iterator.next());
            if ( savedSearch != null ){
                savedSearchs.add(savedSearch);
            }
        }
        return savedSearchs;
    }

    /**
     *
     * @param hibSavedSearch
     * @return
     */
    protected JahiaSavedSearch getJahiaSavedSearchFromHibSavedSearch (
            org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearch hibSavedSearch) {
        if ( hibSavedSearch == null ){
            return null;
        }
        int siteId = 0;
        try {
            siteId = hibSavedSearch.getJahiaSite().getId().intValue();
        } catch ( Exception t ){
            logger.debug("Error accessing saved search site",t);
        }
        JahiaBaseACL acl = null;
        try {
            acl = hibSavedSearch.getJahiaAcl().getACL();
        } catch ( Exception t ){
            logger.debug("Error accessing saved search acl",t);
        }

        JahiaSavedSearch search = new JahiaSavedSearch(hibSavedSearch.getId(),
                hibSavedSearch.getTitle(),
                hibSavedSearch.getDescr(),
                hibSavedSearch.getSearch(),
                hibSavedSearch.getCreationDate(),
                hibSavedSearch.getOwnerKey(),
                hibSavedSearch.getSearchViewHandlerClass(), siteId, acl);
        return search;
    }
}

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
package org.jahia.hibernate.manager;

import java.io.Writer;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.dao.JahiaSavedSearchViewDAO;
import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearchView;
import org.jahia.hibernate.model.jahiasavedsearch.JahiaSavedSearchViewPK;
import org.jahia.services.search.savedsearch.JahiaSavedSearchViewSettings;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * JahiaSearchView Persistence Manager
 */
public class JahiaSavedSearchViewManager {

    private static final XStream SERIALIZER;

    static {
        SERIALIZER = new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new CompactWriter(out, xmlFriendlyReplacer());
            }
        });
        SERIALIZER.alias("view-settings", JahiaSavedSearchViewSettings.class);
        SERIALIZER.alias("field", JahiaSavedSearchViewSettings.ViewField.class);
    }

    private JahiaSavedSearchViewDAO savedSearchViewDao = null;

    private void checkKey(JahiaSavedSearchViewPK key) {
        if (key.getSearchMode() == null) {
            key.setSearchMode(new Integer(0));
        }
        if (key.getSavedSearchId() == null) {
            key.setSavedSearchId(new Integer(0));
        }
        if (key.getContextId() == null) {
            key.setContextId("");
        }
        if (key.getUserKey() == null) {
            key.setUserKey("");
        }
    }

    /**
     * @param key
     */
    public void deleteSearch(
            org.jahia.services.search.savedsearch.JahiaSavedSearchView view) {
        this.savedSearchViewDao.deleteView(toModelObjectKey(view));
    }

    private JahiaSavedSearchViewSettings deserialize(String settings) {
        return (JahiaSavedSearchViewSettings) SERIALIZER.fromXML(settings);
    }

    public org.jahia.services.search.savedsearch.JahiaSavedSearchView getView(
            Integer searchMode, Integer savedSearchId, String contextId,
            String userKey, boolean exactKeyOnly) throws JahiaException {
        return getView(JahiaSavedSearchViewPK.getInstance(searchMode,
                savedSearchId, contextId, userKey), exactKeyOnly);
    }

    private org.jahia.services.search.savedsearch.JahiaSavedSearchView getView(
            JahiaSavedSearchViewPK viewKey, boolean exactKeyOnly)
            throws JahiaException {
        checkKey(viewKey);
        JahiaSavedSearchView view = this.savedSearchViewDao.getView(viewKey);
        if (exactKeyOnly || view != null) {
            return toServiceObject(view);
        }
        String userKey = viewKey.getUserKey();
        if (StringUtils.isNotEmpty(userKey)) {
            JahiaSavedSearchViewPK theKey = new JahiaSavedSearchViewPK(viewKey
                    .getSearchMode(), viewKey.getSavedSearchId(), viewKey
                    .getContextId(), viewKey.getUserKey());

            theKey.setUserKey("");
            view = this.savedSearchViewDao.getView(theKey);
        }
        // do not search the view in another context of for another user
        // have no idea, why it could be usefull

        // if (view == null) {
        // theKey.setUserKey(userKey);
        // theKey.setContextId("");
        // checkKey(theKey);
        // view = this.savedSearchViewDao.getView(theKey);
        // }
        // if (view == null && StringUtils.isNotEmpty(userKey)) {
        // theKey.setUserKey("");
        // view = this.savedSearchViewDao.getView(theKey);
        // }

        return toServiceObject(view);
    }

    /**
     * Returns all views
     * 
     * @return all views
     */
    public List getViews() {
        return this.savedSearchViewDao.getViews();
    }

    public void saveView(
            org.jahia.services.search.savedsearch.JahiaSavedSearchView view)
            throws JahiaException {
        JahiaSavedSearchView model = toModelObject(view);
        checkKey(model.getComp_id());
        this.savedSearchViewDao.save(model);
    }

    private String serialize(JahiaSavedSearchViewSettings settings) {
        return SERIALIZER.toXML(settings);
    }

    public void setSearchViewDao(JahiaSavedSearchViewDAO savedSearchViewDao) {
        this.savedSearchViewDao = savedSearchViewDao;
    }

    private JahiaSavedSearchView toModelObject(
            org.jahia.services.search.savedsearch.JahiaSavedSearchView serviceObj) {
        if (serviceObj == null) {
            return null;
        }
        JahiaSavedSearchView modelObj = new JahiaSavedSearchView(
                toModelObjectKey(serviceObj), serialize(serviceObj
                        .getSettings()));
        modelObj.setViewName(serviceObj.getName());

        return modelObj;
    }

    private JahiaSavedSearchViewPK toModelObjectKey(
            org.jahia.services.search.savedsearch.JahiaSavedSearchView serviceObj) {
        return JahiaSavedSearchViewPK.getInstance(serviceObj.getSearchMode(),
                serviceObj.getSavedSearchId(), serviceObj.getContextId(),
                serviceObj.getUserKey());
    }

    private org.jahia.services.search.savedsearch.JahiaSavedSearchView toServiceObject(
            JahiaSavedSearchView modelObj) {
        if (modelObj == null) {
            return null;
        }
        org.jahia.services.search.savedsearch.JahiaSavedSearchView serviceObj = new org.jahia.services.search.savedsearch.JahiaSavedSearchView(
                modelObj.getComp_id().getSearchMode(), modelObj.getComp_id()
                        .getSavedSearchId(), modelObj.getComp_id()
                        .getContextId(), modelObj.getComp_id().getUserKey());
        serviceObj.setName(modelObj.getViewName());
        serviceObj.setSettings(deserialize(modelObj.getSetting()));

        return serviceObj;
    }

}

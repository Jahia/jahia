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

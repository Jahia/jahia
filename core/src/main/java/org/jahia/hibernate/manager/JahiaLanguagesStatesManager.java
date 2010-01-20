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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.content.ContentObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.dao.JahiaLanguagesStatesDAO;
import org.jahia.hibernate.model.JahiaLanguagesStates;
import org.jahia.hibernate.model.JahiaLanguagesStatesPK;
import org.jahia.registries.ServicesRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:19:06
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLanguagesStatesManager {
    private JahiaLanguagesStatesDAO dao = null;

    public void setJahiaLanguagesStatesDAO(final JahiaLanguagesStatesDAO dao) {
        this.dao = dao;
    }

    public Map getLanguagesStates(final String objectKey) throws JahiaException {
        return getLanguagesStates(objectKey, 0, false);
    }

    public Map<String, Integer> getLanguagesStates(final String objectKey, int siteID,
            boolean activeLanguagesOnly) throws JahiaException {
        final List l = dao.findByObjectKey(objectKey);
        final Map<String, Integer> results = new HashMap<String, Integer>(l.size());
        List<String> activeLocales = null;
        if (activeLanguagesOnly) {
            List<Locale> locales = ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteID).getLanguagesAsLocales();
            activeLocales = new LinkedList<String>();
            for (Locale locale : locales) {
                activeLocales.add(locale.toString());
            }
        }
        final Iterator iterator = l.iterator();
        while (iterator.hasNext()) {
            final JahiaLanguagesStates jahiaLanguagesStates = (JahiaLanguagesStates) iterator
                    .next();
            if (!activeLanguagesOnly
                    || ContentObject.SHARED_LANGUAGE
                            .equals(jahiaLanguagesStates.getComp_id()
                                    .getLanguageCode())
                    || activeLocales.contains(jahiaLanguagesStates.getComp_id()
                            .getLanguageCode())) {
                results.put(
                        jahiaLanguagesStates.getComp_id().getLanguageCode(),
                        jahiaLanguagesStates.getWorkflowState());
            }
        }
        return results;
    }

    public void clearEntries(final String objectKey) {
        dao.clearEntries(objectKey);
    }

    public void updateLanguagesState(final String objectKey,
                                      final String languageCode,
                                      final int value,
                                      final int siteID) {
        try {
            final Integer workflowState = new Integer(value);
            final JahiaLanguagesStates ls = dao.findByPK(objectKey,
                    languageCode);
            if (!ls.getWorkflowState().equals(workflowState)) {
                ls.setWorkflowState(workflowState);
                ls.setSiteID(new Integer(siteID));
                dao.update(ls);
            }
        } catch (Exception e) {
            final JahiaLanguagesStates ls = new JahiaLanguagesStates(
                    new JahiaLanguagesStatesPK(objectKey, languageCode),
                    new Integer(value), new Integer(siteID));
            dao.save(ls);
        }
    }

    public void updateLanguagesStates(final String objectKey,
            final Map languageStates, final int siteID) {
        clearEntries(objectKey);
        if (!languageStates.isEmpty()) {
            Integer siteIdObj = new Integer(siteID);
            for (Iterator iterator = languageStates.entrySet().iterator(); iterator
                    .hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                JahiaLanguagesStates ls = new JahiaLanguagesStates(
                        new JahiaLanguagesStatesPK(objectKey, (String) entry
                                .getKey()), (Integer) entry.getValue(),
                        siteIdObj);
                dao.saveOrUpdate(ls);
            }
        }
    }

    public Map<String,List<String>> getAllStagingObject(final int siteID) {
        final Map<String,List<String>> m = new HashMap<String,List<String>>();
        final List l = dao.findAllStagingObjects(siteID);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            final JahiaLanguagesStates jls = (JahiaLanguagesStates) iterator.next();
            final String key = jls.getComp_id().getObjectkey();
            final String lang = jls.getComp_id().getLanguageCode();
            List<String> langs = m.get(key);
            if (langs == null) {
                langs = new ArrayList<String>();
                m.put(key, langs);
            }
            langs.add(lang);
        }
        return m;
    }

    // TODO use this to provide a new view
    public Map<String,List<String>> getAllStagingAndWaitingObject(final int siteID) {
        final Map<String,List<String>> m = new HashMap<String,List<String>>();
        final List l = dao.findAllStagingAndWaitingObjects(siteID);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            final JahiaLanguagesStates jls = (JahiaLanguagesStates) iterator.next();
            final String key = jls.getComp_id().getObjectkey();
            final String lang = jls.getComp_id().getLanguageCode();
            List<String> langs = m.get(key);
            if (langs == null) {
                langs = new ArrayList<String>();
                m.put(key, langs);
            }
            langs.add(lang);
        }
        return m;
    }

    public Map<String,List<String>> getAllWaitingObject(final int siteID) {
        final Map<String,List<String>> m = new HashMap<String,List<String>>();
        final List l = dao.findAllWaitingObjects(siteID);
        for (Iterator iterator = l.iterator(); iterator.hasNext();) {
            final JahiaLanguagesStates jls = (JahiaLanguagesStates) iterator.next();
            final String key = jls.getComp_id().getObjectkey();
            final String lang = jls.getComp_id().getLanguageCode();
            List<String> langs = m.get(key);
            if (langs == null) {
                langs = new ArrayList<String>();
                m.put(key, langs);
            }
            langs.add(lang);
        }
        return m;
    }
}
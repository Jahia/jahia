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
import java.util.Iterator;
import java.util.ArrayList;

import org.jahia.hibernate.dao.JahiaFieldXRefDAO;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.hibernate.model.JahiaFieldXRefPK;
import org.jahia.services.cache.CacheService;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:19:06
 * To change this template use File | Settings | File Templates.
 */
public class JahiaFieldXRefManager {
    private JahiaFieldXRefDAO jahiaFieldXRefDAO = null;
    public static final String FILE = "file:";
    public static final String PAGE = "page:";
    public static final String IMPORTED_PAGE = "importedpage:";

    private CacheService cacheService = null;

    public void setJahiaFieldXRefDAO(JahiaFieldXRefDAO jahiaFieldXRefDAO) {
        this.jahiaFieldXRefDAO = jahiaFieldXRefDAO;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void createFieldReference(int fieldId, int siteId, String language, int workflow, String target) {
        JahiaFieldXRef xref = new JahiaFieldXRef(new JahiaFieldXRefPK(fieldId, language, workflow, target),siteId);
        jahiaFieldXRefDAO.save(xref);
    }

    public void deleteFieldReference(JahiaFieldXRef xref) {
        if (xref != null) {
            jahiaFieldXRefDAO.delete(xref);
        }
    }

    public void activateField(int fieldId, int siteId, String language) {
        List<String> l = getFieldReferences(fieldId, language, 2);
        jahiaFieldXRefDAO.deleteForField(fieldId,language);
        for (Iterator<String> iterator = l.iterator(); iterator.hasNext();) {
            createFieldReference(fieldId, siteId, language, 1, (String) iterator.next());
        }
    }

    public void changeTarget(String oldTarget, String newTarget, int siteId) {
        List<JahiaFieldXRef> old = getReferencesForTarget(oldTarget);
        for (Iterator<JahiaFieldXRef> iterator = old.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = (JahiaFieldXRef) iterator.next();
            createFieldReference(jahiaFieldXRef.getComp_id().getFieldId(), siteId, jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getWorkflow(), newTarget);
            jahiaFieldXRefDAO.delete(jahiaFieldXRef);
        }
    }

    public void changeTargetWithWildcard(String oldTarget, String newTarget, int siteId) {
        List<JahiaFieldXRef> old = getReferencesForTargetWithWildcard(oldTarget);
        for (Iterator<JahiaFieldXRef> iterator = old.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = iterator.next();
            String thisTarget = newTarget+jahiaFieldXRef.getComp_id().getTarget().substring(oldTarget.length());
            createFieldReference(jahiaFieldXRef.getComp_id().getFieldId(), siteId, jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getWorkflow(), thisTarget);
            jahiaFieldXRefDAO.delete(jahiaFieldXRef);
        }
    }

    public void deleteReferencesForField(int fieldId) {
        jahiaFieldXRefDAO.deleteForField(fieldId);
    }

    public void deleteReferencesForField(int fieldId, String language) {
        jahiaFieldXRefDAO.deleteForField(fieldId,language);
    }

    public void deleteReferencesForField(int fieldId, String language, int workflow) {
        jahiaFieldXRefDAO.deleteForField(fieldId,language, workflow);
    }

    public List<String> getFieldReferences(int fieldId, String language, int workflow) {
        List<String> res = new ArrayList<String>();
        List<JahiaFieldXRef> l = jahiaFieldXRefDAO.getFieldReferences(fieldId, language, workflow);
        for (JahiaFieldXRef jahiaFieldXRef : l) {
            res.add(jahiaFieldXRef.getComp_id().getTarget());
        }
        return res;
    }


    public List<JahiaFieldXRef> getReferencesForTarget(String target) {
        return jahiaFieldXRefDAO.getReferencesForTarget(target);
    }

    public List<JahiaFieldXRef> getReferencesForTargetWithWildcard(String target) {
        return jahiaFieldXRefDAO.getReferencesForTargetWithWildcard(target);
    }

    public int countUsages(String target, int workflow) {
        List<JahiaFieldXRef> l = jahiaFieldXRefDAO.getReferencesForTarget(target, workflow);
        return l.size();
    }


}
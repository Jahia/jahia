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
import java.util.Iterator;
import java.util.ArrayList;

import org.jahia.hibernate.dao.JahiaFieldXRefDAO;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.hibernate.model.JahiaFieldXRefPK;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 21 avr. 2005
 * Time: 10:19:06
 */
public class JahiaFieldXRefManager {
    private JahiaFieldXRefDAO jahiaFieldXRefDAO = null;
    public static final String FILE = "file:";
    public static final String PAGE = "page:";
    public static final String IMPORTED_PAGE = "importedpage:";

    public void setJahiaFieldXRefDAO(JahiaFieldXRefDAO jahiaFieldXRefDAO) {
        this.jahiaFieldXRefDAO = jahiaFieldXRefDAO;
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

    public int countUsages(String target, int workflow) {
        List<JahiaFieldXRef> l = jahiaFieldXRefDAO.getReferencesForTarget(target, workflow);
        return l.size();
    }


}
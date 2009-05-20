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

import org.jahia.hibernate.dao.JahiaBigTextDataDAO;
import org.jahia.hibernate.model.JahiaBigTextData;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 juin 2005
 * Time: 18:10:35
 * To change this template use File | Settings | File Templates.
 */
public class JahiaBigTextDataManager {
    private JahiaBigTextDataDAO dao = null;

    public void setJahiaBigTextDataDAO(JahiaBigTextDataDAO dao) {
        this.dao = dao;
    }

    public String load(String fileName) {
        JahiaBigTextData data = dao.load(fileName);
        if (data != null)
            return data.getRawValues();
        return null;
    }

    public void save(String fileName, String fieldValue) {
        dao.save(new JahiaBigTextData(fileName, fieldValue));        
    }

    public boolean rename(String oldFileName, String newFileName) {
        if(oldFileName.equals(newFileName)) {
            return true;
        }
        boolean b = copy(oldFileName, newFileName);
        if(b) {
            dao.delete(oldFileName);
        }
        return b;
    }

    public boolean delete(String oldFileName) {
        boolean b = false;
        if(dao.doesBigTextAlreadyExist(oldFileName)) {
            dao.delete(oldFileName);
            b = true;
        }
        return b;
    }
    public boolean copy(String oldFileName, String newFileName) {
        if(oldFileName.equals(newFileName)) {
            return false;
        }
        JahiaBigTextData data = dao.load(oldFileName);
        boolean b = false;
        if (data != null) {
            if (dao.doesBigTextAlreadyExist(newFileName)) {
                JahiaBigTextData data2 = dao.load(newFileName);
                data2.setRawValues(data.getRawValues());
                dao.save(data2);
            }
            else
                dao.save(new JahiaBigTextData(newFileName, data.getRawValues()));
            b = true;
        }
        return b;
    }
}

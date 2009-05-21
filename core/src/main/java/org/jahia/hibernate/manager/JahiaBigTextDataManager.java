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

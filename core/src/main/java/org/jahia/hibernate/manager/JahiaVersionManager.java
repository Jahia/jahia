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

import java.util.Date;
import java.util.List;

import org.jahia.bin.Jahia;
import org.jahia.hibernate.dao.JahiaVersionDAO;
import org.jahia.hibernate.model.JahiaVersion;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 août 2007
 * Time: 17:36:50
 * To change this template use File | Settings | File Templates.
 */
public class JahiaVersionManager {
    private JahiaVersionDAO jahiaVersionDao;

    public void setJahiaVersionDao(JahiaVersionDAO jahiaVersionDao) {
        this.jahiaVersionDao = jahiaVersionDao;
    }

    public boolean isCurrentBuildDefined() {
        List<JahiaVersion> l = jahiaVersionDao.findByBuildNumber(Jahia.getBuildNumber());
        return !l.isEmpty();                     
    }

    public List<JahiaVersion> getAllVersion() {
        return jahiaVersionDao.findAll();
    }

    public void createVersion() {
        JahiaVersion v = new JahiaVersion(new Integer(Jahia.getBuildNumber()), org.jahia.settings.SettingsBean.getInstance().getPropertiesFile().getProperty("release"),new Date());
        jahiaVersionDao.save(v);
    }

    public void createOldVersion(int number, String release) {
        JahiaVersion v = new JahiaVersion(new Integer(number), release, null);
        jahiaVersionDao.save(v);
    }
    
    public <E> List<E> executeSqlStmt(String statement) {
        return jahiaVersionDao.executeSqlStmt(statement);
    }    
}

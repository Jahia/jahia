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
import java.util.Properties;

import org.jahia.hibernate.dao.JahiaServerPropertiesDAO;
import org.jahia.hibernate.model.jahiaserver.JahiaServerProp;
import org.jahia.hibernate.model.jahiaserver.JahiaServerPropPK;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 17 mars 2005
 * Time: 14:58:59
 * To change this template use File | Settings | File Templates.
 */
public class JahiaServerPropertiesManager {

    private JahiaServerPropertiesDAO dao = null;

    public void setJahiaServerPropertiesDAO(JahiaServerPropertiesDAO dao) {
        this.dao = dao;
    }

    public void removeProperty(String serverId, String propName) {
        dao.delete(serverId, propName);
    }

    public Properties getProperties(String serverId) {
        Properties properties = new Properties();
        List list = dao.getServerProperties(serverId);
        JahiaServerProp jahiaServerProp = null;
        for (int i = 0; i < list.size(); i++) {
            jahiaServerProp = (JahiaServerProp) list.get(i);
            properties.setProperty(jahiaServerProp.getComp_id().getPropName(),
                    jahiaServerProp.getValue());
        }
        return properties;
    }

    public String getPropertyValue(String serverId, String propName) {
        JahiaServerProp prop = dao.getJahiaServerProp(serverId, propName);
        if ( prop != null ){
            return prop.getValue();
        }
        return null;
    }

    public void save(String serverId, String propName, String propValue) {
        removeProperty(serverId, propName);
        dao.save(new JahiaServerProp(new JahiaServerPropPK(serverId, propName), propValue));
    }

}

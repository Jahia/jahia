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

package org.jahia.services.pagesusers;

import org.jahia.services.JahiaService;
import org.jahia.services.cache.CacheService;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaPagesUsersPropManager;

import java.util.Map;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 22 nov. 2007
 * Time: 13:59:46
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPageUserPropService extends JahiaService {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPageUserPropService.class);
    private JahiaPagesUsersPropManager pagesUsersPropManager;
    private CacheService cacheService;
    private static JahiaPageUserPropService instance;
    public static synchronized JahiaPageUserPropService getInstance() {
        if (instance == null) {
            instance = new JahiaPageUserPropService();
        }
        return instance;
    }


    public CacheService getCacheService() {
        return cacheService;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public JahiaPagesUsersPropManager getPagesUsersPropManager() {
        return pagesUsersPropManager;
    }

    public Map getPageUserProperties(int pageId, String principalKey,String principalType,String propType) {
        return pagesUsersPropManager.getPageUsersProperties(pageId,principalKey,principalType,propType);
    }

    public PageUserProperty getPageUserProperty(int pageId, String principalKey,String principalType,String propType,String propName) {
        return pagesUsersPropManager.getPageUserProperty(pageId,principalKey,principalType,propType,propName);
    }

    public void savePageUserProperty(int pageId, String principalKey,String principalType,String propType,String name, String value) {
        PageUserProperty pageUserProp = new PageUserProperty(pageId,principalKey,principalType,propType,name);
        pageUserProp.setValue(value);
        pagesUsersPropManager.savePageUserProperty(pageUserProp);
    }

    public void deletePageUserPropertiesByPropType(String propType) {
        pagesUsersPropManager.deletePageUserPropertiesByPropType(propType);
    }

    public void setPagesUsersPropManager(JahiaPagesUsersPropManager pagesUsersPropManager) {
        this.pagesUsersPropManager = pagesUsersPropManager;
    }

    public synchronized void start() throws JahiaInitializationException {
        logger.debug("** Initializing the PageUserProperty Service ...");

    }

    public synchronized void stop() {
        logger.debug("** Stop the PageUserProperty Service ...");

    }
}

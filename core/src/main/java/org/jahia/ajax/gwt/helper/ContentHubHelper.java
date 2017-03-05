/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.helper;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:47:08 PM
 */
public class ContentHubHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ContentHubHelper.class);
    private JCRSessionFactory sessionFactory;
    private JahiaUserManagerService userManagerService;

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    public Map<String, String> getStoredPasswordsProviders(JahiaUser user) {
        Map<String, String> results = new HashMap<String, String>();
        results.put(null, user.getUsername());
        JCRUserNode userNode = userManagerService.lookupUserByPath(user.getLocalPath());
        if (userNode != null) {
            for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
                if ("storedPasswords".equals(provider.getAuthenticationType())) {
                    results.put(provider.getKey(), userNode.getPropertyAsString("storedUsername_" + provider.getKey()));
                }
            }
        }
        return results;
    }

    public void storePasswordForProvider(JCRUserNode user, String providerKey, String username, String password) {
        try {
            if (username == null) {
                user.getProperty("storedUsername_" + providerKey).remove();
            } else {
                user.setProperty("storedUsername_" + providerKey, username);
            }
            if (password == null) {
                user.getProperty("storedPassword_" + providerKey).remove();
            } else {
                user.setProperty("storedPassword_" + providerKey, password);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }
}

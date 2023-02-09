/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

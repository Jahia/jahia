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
package org.jahia.services.uicomponents.bean.contentmanager;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Evaluates the need to display provider key column in the content manager,
 * depending on the presence of the mount points
 * 
 * @author Sergiy Shyrkov
 */
public class ProviderKeyColumnVisibility extends Visibility {

    private JCRSessionFactory sessionFactory;

    private boolean alsoCountDynamicProviders;

    @Override
    public boolean getRealValue(JCRNodeWrapper contextNode, JahiaUser jahiaUser, Locale locale, HttpServletRequest request) {
        boolean visible = false;
        List<JCRStoreProvider> providers = sessionFactory.getProviderList();
        if (providers.size() > 1) {
            if (alsoCountDynamicProviders) {
                visible = true;
            } else {
                int count = 0;
                for (JCRStoreProvider jcrStoreProvider : providers) {
                    if (!jcrStoreProvider.isDynamicallyMounted()) {
                        count++;
                        if (count > 1) {
                            visible = true;
                            break;
                        }
                    }
                }
            }
        }

        return visible;
    }

    public void setJcrSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @param alsoCountDynamicProviders the alsoCountDynamicProviders to set
     */
    public void setAlsoCountDynamicProviders(boolean alsoCountDynamicProviders) {
        this.alsoCountDynamicProviders = alsoCountDynamicProviders;
    }

}
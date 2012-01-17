/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
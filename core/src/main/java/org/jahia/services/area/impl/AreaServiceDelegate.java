/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.area.impl;

import org.jahia.services.DelegateService;
import org.jahia.services.area.AreaService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;

import javax.jcr.RepositoryException;

/**
 * Delegate class that is dispatching the calls to an appropriate service implementation of the {@link AreaService}.
 *
 * @author Kevan
 */
public class AreaServiceDelegate extends DelegateService<AreaService> implements AreaService {

    private static volatile AreaServiceDelegate instance;

    public static AreaServiceDelegate getInstance() {
        if (instance == null) {
            synchronized (AreaServiceDelegate.class) {
                if (instance == null) {
                    instance = new AreaServiceDelegate();
                }
            }
        }
        return instance;
    }

    @Override
    public JCRNodeWrapper getOrCreateAreaNode(String areaPath, String areaType, JCRSessionWrapper session) throws RepositoryException {
        return lookupService(AreaService.class).getOrCreateAreaNode(areaPath, areaType, session);
    }
}

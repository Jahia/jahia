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
package org.jahia.services.importexport.validation;


import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ImportExportBaseService;
import org.xml.sax.Attributes;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Import validator that checks if providers needed for this import are available
 */
public class ProviderAvailabilityValidator implements ImportValidator {

    private JCRSessionFactory jcrSessionFactory;

    private JCRSessionWrapper currentUserSession;

    private Set<String> visitedPaths = new TreeSet<String>();

    private Set<String> neededStaticProviders = new LinkedHashSet<String>();

    private Set<String> neededDynamicProviders = new LinkedHashSet<String>();

    private Set<String> neededMountPoint = new LinkedHashSet<String>();

    @Override
    public ValidationResult getResult() {
        try {
            Map<String, JCRStoreProvider> providers = jcrSessionFactory.getProviders();
            Set<String> unavailableProviders = new LinkedHashSet<String>();
            for (String p : neededStaticProviders) {
                if (providers.containsKey(p) && providers.get(p).isAvailable()) {
                    continue;
                }

                unavailableProviders.add(p);
            }
            for (String p : neededDynamicProviders) {
                if (visitedPaths.contains(p)) {
                    continue;
                }
                if (getCurrentUserSession().nodeExists(p)) {
                    String identifier = getCurrentUserSession().getNode(p).getIdentifier();
                    if (providers.containsKey(identifier) && providers.get(identifier).isAvailable()) {
                        continue;
                    }
                }

                unavailableProviders.add(p);
            }
            for (String type : neededMountPoint) {
                if (JCRStoreService.getInstance().getProviderFactories().get(type) == null) {
                    unavailableProviders.add(type);
                }
            }
            return new ProviderAvailabilityValidatorResult(unavailableProviders);
        } catch (RepositoryException e) {
            return new ValidationResult.FailedValidationResult(e);
        }
    }

    @Override
    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {
        String path = StringUtils.removeStart(currentPath, "/content");
        if (StringUtils.isNotBlank(path)) {
            visitedPaths.add(path);
        }
        String type = atts.getValue("jcr:primaryType");
        try {
            if (type != null && NodeTypeRegistry.getInstance().getNodeType(type).isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                neededMountPoint.add(type);
            }
        } catch (NoSuchNodeTypeException e) {
            // Ignore
        }
        if (atts.getIndex(ImportExportBaseService.STATIC_MOUNT_POINT_ATTR) > -1) {
            neededStaticProviders.add(atts.getValue(ImportExportBaseService.STATIC_MOUNT_POINT_ATTR));
        }
        if (atts.getIndex(ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR) > -1) {
            neededDynamicProviders.add(atts.getValue(ImportExportBaseService.DYNAMIC_MOUNT_POINT_ATTR));
        }
    }

    private JCRSessionWrapper getCurrentUserSession() throws RepositoryException {
        if (currentUserSession == null) {
            currentUserSession = jcrSessionFactory.getCurrentUserSession();
        }
        return currentUserSession;
    }

    public void setJcrSessionFactory(JCRSessionFactory jcrSessionFactory) {
        this.jcrSessionFactory = jcrSessionFactory;
    }
}

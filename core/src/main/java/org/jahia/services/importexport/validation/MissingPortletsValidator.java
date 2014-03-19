/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.jahia.api.Constants;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.xml.sax.Attributes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Set;

/**
 * Validator to check for missing portlets before executing an import.
 *
 */
public class MissingPortletsValidator implements ImportValidator {

    private Set<String> missingPortlets = new HashSet<String>();
    private Set<String> unresolvedInstances = new HashSet<String>();
    private Set<String> unresolvedDefinitions = new HashSet<String>();
    private Set<String> importedPortletInstancePaths = new HashSet<String>();
    private Set<String> importedPortletDefinitionPaths = new HashSet<String>();
    private String currentSitePath = null;

    public ValidationResult getResult() {
        // we will now try again to resolve references to instances that might have been imported by this file.
        if (unresolvedDefinitions.size() > 0) {
            for (String unresolvedDefinition : unresolvedDefinitions) {
                if (!importedPortletDefinitionPaths.contains(unresolvedDefinition)) {
                    missingPortlets.add(unresolvedDefinition);
                }
            }
        }
        if (unresolvedInstances.size() > 0) {
            for (String unresolvedInstance : unresolvedInstances) {
                if (!importedPortletInstancePaths.contains(unresolvedInstance)) {
                    missingPortlets.add(unresolvedInstance);
                }
            }
        }
        return new MissingPortletsValidationResult(missingPortlets);
    }

    public String resolveRefPath(String rootPath, String currentSitePath, String refPath) {
        if (refPath == null) {
            return null;
        }
        if ("#/".equals(refPath)) {
            return rootPath;
        } else if (refPath.startsWith("#/")) {
            return rootPath + refPath.substring(2);
        } else if (refPath.startsWith("$currentSite")) {
            return currentSitePath + refPath;
        } else {
            return refPath;
        }
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {
        if (appServiceNotAvailable()) return;
        String rootPath = "/";
        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);
        if (pt != null) {
            if (Constants.JAHIANT_VIRTUALSITE.equals(pt)) {
                currentSitePath = currentPath;
                if (currentSitePath.startsWith("/content")) {
                    currentSitePath = currentSitePath.substring("/content".length());
                }
            } else if ("jnt:portletDefinition".equals(pt)) {
                // portlet definitions are also exported when sites are exported.
                importedPortletDefinitionPaths.add(currentPath);
                String context = atts.getValue("j:context");
                try {
                    ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationByContext(context);
                } catch (Exception e) {
                    missingPortlets.add(currentPath);
                }
            } else if (Constants.JAHIANT_PORTLET.equals(pt)) {
                // here we check for missing portlet definitions
                importedPortletInstancePaths.add(currentPath);
                final String applicationRef = resolveRefPath(rootPath, currentSitePath, atts.getValue("j:applicationRef"));
                String application = atts.getValue("j:application");
                if (applicationRef == null) {
                    missingPortlets.add("Missing ref for portlet" + application);
                    return;
                }
                try {
                    JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            session.getNode(applicationRef);
                            return null;
                        }
                    });
                } catch (RepositoryException e) {
                    // we add the unresolved instance to a set since we will try resolving it again after
                    // the whole file has been parsed.
                    unresolvedDefinitions.add(applicationRef);
                }
            } else if ("jnt:portletReference".equals(pt)) {
                // here we check for missing portlet instances
                final String nodeRef = resolveRefPath(rootPath, currentSitePath, atts.getValue("j:node"));
                if (importedPortletInstancePaths.contains(nodeRef)) {
                    // normal case, we are referencing a portlet instance that's being imported.
                } else {
                    try {
                        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                session.getNode(nodeRef);
                                return null;
                            }
                        });
                    } catch (RepositoryException e) {
                        // we add the unresolved instance to a set since we will try resolving it again after
                        // the whole file has been parsed.
                        unresolvedInstances.add(nodeRef);
                    }
                }
            }
        }
    }

    private boolean appServiceNotAvailable() {
        if (ServicesRegistry.getInstance() == null) {
            return true;
        }
        if (ServicesRegistry.getInstance().getApplicationsManagerService() == null) {
            return true;
        }
        return false;
    }

    private void addMissingPortlet(String applicationRef, String application) {
        if (application != null) {
            missingPortlets.add(application);
        } else {
            missingPortlets.add("portlet " + applicationRef);
        }
    }
}

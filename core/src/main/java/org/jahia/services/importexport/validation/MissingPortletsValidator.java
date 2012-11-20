package org.jahia.services.importexport.validation;

import org.jahia.api.Constants;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
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
                    ApplicationBean appBean = ServicesRegistry.getInstance().getApplicationsManagerService().getApplicationByContext(context);
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
                            Node portletInstanceNode = session.getNode(applicationRef);
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
                                Node portletInstanceNode = session.getNode(nodeRef);
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

package org.jahia.services.importexport;

import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;

/**
 * Wrapper on some patch(es) to be processed at the end of the import of a website exported in a legacy version.
 */
public interface PostImportPatcher {

    /**
     * This method is called at the end of each site import.
     *
     * @param session The JCR session used to perform the import. Do not save it in the patch(es).
     * @param site The just imported website.
     */
    public void executePatches(JCRSessionWrapper session, JahiaSite site);
}

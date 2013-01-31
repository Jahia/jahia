package org.jahia.bundles.url.jahiawar.internal;

import java.util.List;

/**
 * Handler configuration
 */
public interface Configuration {

    /**
     * Returns true if the certificate should be checked on SSL connection, false otherwise.
     *
     * @return true if the certificate should be checked
     */
    Boolean getCertificateCheck();

    List<String> getImportedPackages();

}

package org.jahia.bundles.url.jahiawar;

/**
 * User: loom
 * Date: 27.11.12
 * Time: 12:24
 */
public interface ServiceConstants {

    /**
     * Service PID used for configuration.
     */
    static final String PID = "org.jahia.bundles.url.jahiawar";
    /**
     * The war protocol name.
     */
    public static final String PROTOCOL = "jahiawar";
    /**
     * Certificate check configuration property name.
     */
    static final String PROPERTY_CERTIFICATE_CHECK = PID + ".certificateCheck";

    static final String PROPERTY_IMPORTED_PACKAGED = PID + ".importedPackages";
}

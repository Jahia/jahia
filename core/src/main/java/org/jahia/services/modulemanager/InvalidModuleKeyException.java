package org.jahia.services.modulemanager;

/**
 * Indicates wrong bundle key format, e.g. missing version.
 */
public class InvalidModuleKeyException extends ModuleManagementInvalidArgumentException {

    private static final long serialVersionUID = 742485188649290803L;

    private String bundleKey;

    /**
     * Create an instance of the exception.
     *
     * @param bundleKey Bundle key that caused the exception
     */
    public InvalidModuleKeyException(String bundleKey) {
        super("Invalid module key: " + bundleKey);
        this.bundleKey = bundleKey;
    }

    /**
     * @return Bundle key that caused the exception
     */
    public String getBundleKey() {
        return bundleKey;
    }
}

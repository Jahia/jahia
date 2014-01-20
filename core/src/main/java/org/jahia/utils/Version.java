package org.jahia.utils;

/**
 * Stub class for compatibility, do not use this class anymore
 * @deprecated Use org.jahia.commons.Version from library jahia-commons instead of this
 */
public class Version extends org.jahia.commons.Version {
    public Version(String versionString) throws NumberFormatException {
        super(versionString);
    }
}

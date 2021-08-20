package org.jahia.bundles.config;

import java.util.Arrays;
import java.util.Collection;

/**
 * Supported configuration files formats
 */
public enum Format {
    CFG(".cfg", Arrays.asList(".cfg",".config")), YAML(".yaml", Arrays.asList(".yml",".yaml"));

    private String defaultExtension;
    private Collection<String> supportedExtensions;

    Format(String defaultExtension, Collection<String> supportedExtensions) {
        this.defaultExtension = defaultExtension;
        this.supportedExtensions = supportedExtensions;
    }

    public String getDefaultExtension() {
        return defaultExtension;
    }

    public Collection<String> getSupportedExtensions() {
        return supportedExtensions;
    }
}

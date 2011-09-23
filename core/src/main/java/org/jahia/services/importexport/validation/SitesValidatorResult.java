package org.jahia.services.importexport.validation;


import java.util.Map;
import java.util.Properties;

/**
 * Validator that gets the list of all sites and sites properties from the xml import file
 */
public class SitesValidatorResult implements ValidationResult {
    private Map<String, Properties> sitesProperties;

    public SitesValidatorResult(Map<String, Properties> sitesProperties) {
        this.sitesProperties = sitesProperties;
    }

    public boolean isSuccessful() {
        return true;
    }

    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith;
    }

    public Map<String, Properties> getSitesProperties() {
        return sitesProperties;
    }
}

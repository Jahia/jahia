package org.jahia.services.importexport.validation;

import org.jahia.api.Constants;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Validator that gets the list of all sites and sites properties from the xml import file.
 */
public class SitesValidator implements ImportValidator {

    private Map<String, Properties> sitesProperties;

    public SitesValidator() {
        sitesProperties = new HashMap<String, Properties>();
    }

    public ValidationResult getResult() {
        return new SitesValidatorResult(sitesProperties);
    }

    public void validate(String decodedLocalName, String decodedQName, String currentPath, Attributes atts) {

        String pt = atts.getValue(Constants.JCR_PRIMARYTYPE);

        if (pt != null && pt.equals(Constants.JAHIANT_VIRTUALSITE)) {
            Properties properties = new Properties();
            sitesProperties.put(decodedQName, properties);
        }
    }

    public Map<String, Properties> getSitesProperties() {
        return sitesProperties;
    }
}

package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Constraints validator result object
 * @author Serge Huber
 */
public class ConstraintsValidatorResult implements ValidationResult, Serializable {

    Map<String, Set<String>> missingMandatoryProperties = new TreeMap<String, Set<String>>();
    Map<String, Set<String>> missingMandatoryI18NProperties = new TreeMap<String, Set<String>>();

    public ConstraintsValidatorResult(Map<String, Set<String>> missingMandatoryProperties, Map<String,Set<String>> missingMandatoryI18NProperties) {
        this.missingMandatoryProperties = missingMandatoryProperties;
        this.missingMandatoryI18NProperties = missingMandatoryI18NProperties;
    }

    public ConstraintsValidatorResult(ConstraintsValidatorResult result1, ConstraintsValidatorResult result2) {
        missingMandatoryProperties.putAll(result1.missingMandatoryProperties);
        missingMandatoryI18NProperties.putAll(result1.missingMandatoryI18NProperties);
        for (Map.Entry<String,Set<String>> result2MissingPropertiesEntry : result2.missingMandatoryProperties.entrySet()) {
            if (missingMandatoryProperties.containsKey(result2MissingPropertiesEntry.getKey())) {
                missingMandatoryProperties.get(result2MissingPropertiesEntry.getKey()).addAll(result2MissingPropertiesEntry.getValue());
            } else {
                missingMandatoryProperties.put(result2MissingPropertiesEntry.getKey(), result2MissingPropertiesEntry.getValue());
            }
        }
        for (Map.Entry<String,Set<String>> result2MissingI18NPropertiesEntry : result2.missingMandatoryI18NProperties.entrySet()) {
            if (missingMandatoryI18NProperties.containsKey(result2MissingI18NPropertiesEntry.getKey())) {
                missingMandatoryI18NProperties.get(result2MissingI18NPropertiesEntry.getKey()).addAll(result2MissingI18NPropertiesEntry.getValue());
            } else {
                missingMandatoryI18NProperties.put(result2MissingI18NPropertiesEntry.getKey(), result2MissingI18NPropertiesEntry.getValue());
            }
        }
    }

    @Override
    public boolean isSuccessful() {
        return missingMandatoryProperties.isEmpty() && missingMandatoryI18NProperties.isEmpty();
    }

    @Override
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof ConstraintsValidatorResult) ? this
                : new ConstraintsValidatorResult(this,
                (ConstraintsValidatorResult) toBeMergedWith);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", missingMandatoryProperties=").append(missingMandatoryProperties);
            out.append(", missingMandatoryI18NProperties=").append(missingMandatoryI18NProperties);
        }
        out.append("]");

        return out.toString();
    }

}

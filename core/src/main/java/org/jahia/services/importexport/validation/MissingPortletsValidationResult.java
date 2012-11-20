package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This class stores missing portlet validator results.
 */
public class MissingPortletsValidationResult implements ValidationResult {

    private Set<String> missingPortlets = new HashSet<String>();

    public MissingPortletsValidationResult(Set<String> missingPortlets) {
        this.missingPortlets = missingPortlets;
    }

    protected MissingPortletsValidationResult(MissingPortletsValidationResult missingPortlets1, MissingPortletsValidationResult missingPortlets2) {
        this.missingPortlets.addAll(missingPortlets1.getMissingPortlets());
        this.missingPortlets.addAll(missingPortlets2.getMissingPortlets());
    }

    public boolean isSuccessful() {
        return missingPortlets.size() == 0;
    }

    public Set<String> getMissingPortlets() {
        return missingPortlets;
    }

    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof MissingPortletsValidationResult) ? this
                : new MissingPortletsValidationResult(this,
                        (MissingPortletsValidationResult) toBeMergedWith);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", missingPortlets=").append(missingPortlets);
        }
        out.append("]");

        return out.toString();
    }

}

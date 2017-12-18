/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport.validation;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
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
    Map<String, String> otherConstraintViolations = new HashMap<>();

    public ConstraintsValidatorResult(Map<String, Set<String>> missingMandatoryProperties, Map<String, Set<String>> missingMandatoryI18NProperties, Map<String,
            String> missingConstraint) {
        this.missingMandatoryProperties = missingMandatoryProperties;
        this.missingMandatoryI18NProperties = missingMandatoryI18NProperties;
        this.otherConstraintViolations = missingConstraint;
    }

    public ConstraintsValidatorResult(ConstraintsValidatorResult result1, ConstraintsValidatorResult result2) {
        missingMandatoryProperties.putAll(result1.missingMandatoryProperties);
        missingMandatoryI18NProperties.putAll(result1.missingMandatoryI18NProperties);
        otherConstraintViolations.putAll(result1.otherConstraintViolations);
        for (Map.Entry<String, Set<String>> result2MissingPropertiesEntry : result2.missingMandatoryProperties.entrySet()) {
            if (missingMandatoryProperties.containsKey(result2MissingPropertiesEntry.getKey())) {
                missingMandatoryProperties.get(result2MissingPropertiesEntry.getKey()).addAll(result2MissingPropertiesEntry.getValue());
            } else {
                missingMandatoryProperties.put(result2MissingPropertiesEntry.getKey(), result2MissingPropertiesEntry.getValue());
            }
        }
        for (Map.Entry<String, Set<String>> result2MissingI18NPropertiesEntry : result2.missingMandatoryI18NProperties.entrySet()) {
            if (missingMandatoryI18NProperties.containsKey(result2MissingI18NPropertiesEntry.getKey())) {
                missingMandatoryI18NProperties.get(result2MissingI18NPropertiesEntry.getKey()).addAll(result2MissingI18NPropertiesEntry.getValue());
            } else {
                missingMandatoryI18NProperties.put(result2MissingI18NPropertiesEntry.getKey(), result2MissingI18NPropertiesEntry.getValue());
            }
        }
        for (Map.Entry<String, String> otherConstraintViolationEntry : result2.otherConstraintViolations.entrySet()) {
            otherConstraintViolations.put(otherConstraintViolationEntry.getKey(), otherConstraintViolationEntry.getValue());
        }
    }

    public Map<String, Set<String>> getMissingMandatoryProperties() {
        return missingMandatoryProperties;
    }

    public Map<String, Set<String>> getMissingMandatoryI18NProperties() {
        return missingMandatoryI18NProperties;
    }

    public Map<String, String> getOtherConstraintViolations() {
        return otherConstraintViolations;
    }

    public int getLength() {
        return otherConstraintViolations.size() + missingMandatoryI18NProperties.size() + missingMandatoryProperties.size();
    }

    @Override
    public boolean isSuccessful() {
        return missingMandatoryProperties.isEmpty() && missingMandatoryI18NProperties.isEmpty() && otherConstraintViolations.isEmpty();
    }

    @Override
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof ConstraintsValidatorResult) ? this
                : new ConstraintsValidatorResult(this,
                (ConstraintsValidatorResult) toBeMergedWith);
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        if (!isSuccessful()) {
            out.append(", missingMandatoryProperties=").append(missingMandatoryProperties);
            out.append(", missingMandatoryI18NProperties=").append(missingMandatoryI18NProperties);
            out.append(", otherConstraintViolations=").append(otherConstraintViolations);
        }
        out.append("]");

        return out.toString();
    }
}

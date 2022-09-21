/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import java.io.Serializable;

/**
 * Implementors represent results of the validation checks for imported content items.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public interface ValidationResult {
    /**
     * Returns <code>true</code> if the current validation result is successful.
     * 
     * @return <code>true</code> if the current validation result is successful
     */
    boolean isSuccessful();

    /**
     * Merges the results with the provided and returns a new instance of the {@link ValidationResult} object having "merged" results.
     * 
     * @param toBeMergedWith
     *            a {@link ValidationResult} to merge with
     * @return the results with the provided and returns a new instance of the {@link ValidationResult} object having "merged" results
     */
    ValidationResult merge(ValidationResult toBeMergedWith);

    /**
     * Returns <code>true</code> if the current validation blocks the import.
     *
     * @return <code>true</code> if the current validation result is blocking.
     */
    boolean isBlocking();

    class FailedValidationResult implements ValidationResult, Serializable {
        private final Exception exception;

        public FailedValidationResult(Exception exception) {
            this.exception = exception;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

        @Override
        public ValidationResult merge(ValidationResult toBeMergedWith) {
            return toBeMergedWith;
        }

        @Override
        public boolean isBlocking() {
            return true;
        }

        @Override
        public String toString() {
            final String localizedMessage = exception.getLocalizedMessage();
            final Throwable cause = exception.getCause();
            return "Validation failed because of a " + exception.getClass().getSimpleName()
                    + (localizedMessage == null ? " " : " with message '" + localizedMessage + "'")
                    + (cause == null ? "" : " caused by '" + cause.getLocalizedMessage() + "'");
        }
    }
}

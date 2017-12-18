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

/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.utils.i18n.Messages;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class ProviderAvailabilityValidatorResult implements ValidationResult, Serializable {

    private final Set<String> unavailableProviders;

    public ProviderAvailabilityValidatorResult(Set<String> unavailableProviders) {
        this.unavailableProviders = unavailableProviders;
    }

    public ProviderAvailabilityValidatorResult(ProviderAvailabilityValidatorResult result, ProviderAvailabilityValidatorResult toBeMergedWith) {
        this.unavailableProviders = new LinkedHashSet<String>(result.getUnavailableProviders());
        this.unavailableProviders.addAll(toBeMergedWith.getUnavailableProviders());
    }

    @Override
    public boolean isSuccessful() {
        return unavailableProviders.isEmpty();
    }

    @Override
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith == null || toBeMergedWith.isSuccessful()
                || !(toBeMergedWith instanceof ProviderAvailabilityValidatorResult) ? this
                : new ProviderAvailabilityValidatorResult(this,
                (ProviderAvailabilityValidatorResult) toBeMergedWith);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public Set<String> getFormatedMessages(Locale locale) {
        return Collections.singleton(Messages.getInternalWithArguments("failure.import.unavailableProviders", locale, getUnavailableProviders().size()) +
                getUnavailableProviders());
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(128);
        out.append("[").append(StringUtils.substringAfterLast(getClass().getName(), "."))
                .append("=").append(isSuccessful() ? "successful" : "failure");
        out.append(", unavailableProviders=").append(unavailableProviders);
        out.append("]");

        return out.toString();
    }

    public Set<String> getUnavailableProviders() {
        return unavailableProviders;
    }
}

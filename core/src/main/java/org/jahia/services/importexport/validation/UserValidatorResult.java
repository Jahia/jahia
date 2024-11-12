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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * User validation result
 */
public class UserValidatorResult implements ValidationResult, Serializable {

    private static final long serialVersionUID = 2598819850670436530L;

    private Set<String> duplicateUsers = new TreeSet<String>();

    public UserValidatorResult(Set<String> duplicateUsers) {
        this.duplicateUsers = duplicateUsers;
    }

    public Set<String> getDuplicateUsers() {
        return duplicateUsers;
    }

    @Override
    public boolean isSuccessful() {
        return duplicateUsers.isEmpty();
    }

    @Override
    public ValidationResult merge(ValidationResult toBeMergedWith) {
        return toBeMergedWith;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    public String getMessageKey() {
        return "failure.import.conflictingUser";
    }

    public List<Object> getMessageParams() {
        String res = duplicateUsers.size() > 10 ? StringUtils.join(duplicateUsers.toArray(new String[duplicateUsers.size()]), ",", 0, 10) + " ..." : StringUtils.join(duplicateUsers, ",");
        return Arrays.asList( (Object)duplicateUsers.size(), res);
    }
}

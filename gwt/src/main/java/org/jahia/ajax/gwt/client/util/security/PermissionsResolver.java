/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.util.security;

import java.util.Iterator;
import java.util.List;

/**
 * Provides different strategies to resolve permissions from a list.
 *
 * @author cmoitrier
 */
public enum PermissionsResolver {

    /** All given permissions must match with the {@link Matcher} */
    MATCH_ALL {
        @Override
        public boolean resolve(List<String> permissions, Matcher matcher) {
            if (permissions.isEmpty()) {
                return true;
            }

            boolean result = true;
            for (Iterator<String> it = permissions.iterator(); it.hasNext() && result == true; ) {
                String permission = it.next();
                result = matcher.matches(permission);
            }
            return result;
        }
    },

    /** At least one of the given permissions must match with the {@link Matcher} */
    MATCH_ANY {
        @Override
        public boolean resolve(List<String> permissions, Matcher matcher) {
            if (permissions.isEmpty()) {
                return true;
            }

            for (Iterator<String> it = permissions.iterator(); it.hasNext(); ) {
                String permission = it.next();
                if (matcher.matches(permission)) {
                    return true;
                }
            }
            return false;
        }

    };

    public abstract boolean resolve(List<String> permissions, Matcher matcher);

    public interface Matcher {
        boolean matches(String permission);
    }

}

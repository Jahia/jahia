/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
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

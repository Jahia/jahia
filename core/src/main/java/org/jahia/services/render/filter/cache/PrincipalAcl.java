/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Contains all ACLs on the platform for a specific user or group.
 */
public class PrincipalAcl implements Serializable {

    private static final long serialVersionUID = -9194400174386788272L;

    private Map<String, Map<String, SortedSet<String>>> grantAceByPath = new ConcurrentHashMap<String, Map<String, SortedSet<String>>>();
    private Set<String> allPaths = new HashSet<>();

    public PrincipalAcl(Map<String,Set<String>> mapGranted, Map<String,Set<String>> mapDenied) {
        for (Map.Entry<String, Set<String>> entry : mapGranted.entrySet()) {
            grantAceByPath.put(entry.getKey(), new HashMap<String, SortedSet<String>>());
            for (String role : entry.getValue()) {
                grantAceByPath.get(entry.getKey()).put(role, null);
            }
        }

        for (Map.Entry<String, Set<String>> entry : mapDenied.entrySet()) {
            String path = entry.getKey();
            String grantedPath = path;
            while (grantedPath.length() > 0) {
                grantedPath = StringUtils.substringBeforeLast(grantedPath, "/");
                if (grantAceByPath.containsKey(grantedPath)) {
                    Map<String, SortedSet<String>> rolesMap = grantAceByPath.get(grantedPath);
                    Collection<String> intersection = CollectionUtils.intersection(rolesMap.keySet(), entry.getValue());
                    for (String s : intersection) {
                        if (rolesMap.get(s) == null) {
                            rolesMap.put(s, new TreeSet<String>());
                        }
                        rolesMap.get(s).add(path);
                    }
                }
            }
        }
        allPaths.addAll(mapGranted.keySet());
        allPaths.addAll(mapDenied.keySet());
    }

    /**
     * Get all paths where an ACE is defined for this principal, matching the specified pattern. Fill the set passed
     * in parameter with the results.
     * @param pattern The pattern to match
     * @param results The set where to put the results
     */
    public void fillMatchingPaths(Pattern pattern, Set<String> results) {
        for (String path : allPaths) {
            if (pattern.matcher(path).matches()) {
                results.add(path);
            }
        }
    }

    /**
     * Fill the map passed as parameter with the roles for the corresponding node path, defined for this principal.
     * @param nodePath The path to look for
     * @param rolesForPath The final map where to put the results, with all roles per paths
     */
    public void fillRolesForPath(String nodePath, Map<String, Set<String>> rolesForPath) {
        nodePath += "/";

        for (Map.Entry<String, Map<String, SortedSet<String>>> entry : grantAceByPath.entrySet()) {
            String grantPath = entry.getKey() + "/";
            if (nodePath.startsWith(grantPath)) {
                fillRolesForPath(nodePath, rolesForPath, entry.getValue());
            }
        }
    }

    private void fillRolesForPath(String nodePath, Map<String, Set<String>> rolesForPath, Map<String, SortedSet<String>> rolesAndDeniedPaths) {
        if (!rolesForPath.containsKey(nodePath)) {
            rolesForPath.put(nodePath, new TreeSet<String>());
        }
        fillValidRoles(nodePath, rolesAndDeniedPaths, rolesForPath.get(nodePath));
    }

    private void fillValidRoles(String nodePath, Map<String, SortedSet<String>>  roles, Set<String> validRoles) {
        for (Map.Entry<String, SortedSet<String>> entry : roles.entrySet()) {
            if (entry.getValue() == null) {
                // No path denied
                validRoles.add(entry.getKey());
            } else if (entry.getValue().contains(nodePath)) {
                // Current node path is explicitely denied
            } else {
                // Look for a possible denied parent path
                SortedSet<String> s = entry.getValue().headSet(nodePath);
                if (s.isEmpty() || !nodePath.startsWith(s.last())) {
                    validRoles.add(entry.getKey());
                }
            }
        }
    }

}

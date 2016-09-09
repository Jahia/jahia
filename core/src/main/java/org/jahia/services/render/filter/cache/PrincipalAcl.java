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
     * Get all paths where an ACE is defined for thsi principal. Fill the set passed
     * in parameter with the results.
     * @param results The set where to put the results
     */
    public void fillAllPaths(Set<String> results) {
        results.addAll(allPaths);
    }

    /**
     * Get all paths where an ACE is defined for thsi principal, matching the specified pattern. Fill the set passed
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

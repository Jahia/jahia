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
package org.jahia.bundles.securityfilter.core;

import org.apache.commons.io.IOUtils;
import org.jahia.api.settings.SettingsBean;
import org.jahia.bundles.securityfilter.legacy.PermissionsConfig;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.securityfilter.PermissionService;
import org.jahia.services.securityfilter.ScopeDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionServiceImpl implements PermissionService, ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private AuthorizationConfig authorizationConfig;
    private PermissionsConfig permissionsConfig;
    private ThreadLocal<Set<ScopeDefinition>> currentScopesLocal = new ThreadLocal<>();
    private BundleContext context;
    private SettingsBean settingsBean = org.jahia.settings.SettingsBean.getInstance();

    private boolean legacyMode = false;
    private boolean migrationReporting = false;

    public Collection<ScopeDefinition> getCurrentScopes() {
        return currentScopesLocal.get() != null ? Collections.unmodifiableSet(currentScopesLocal.get()) : null;
    }

    public void setCurrentScopes(Collection<ScopeDefinition> scopes) {
        currentScopesLocal.set(new HashSet<>(scopes));
    }

    public Collection<ScopeDefinition> getAvailableScopes() {
        return Collections.unmodifiableSet(new HashSet<>(authorizationConfig.getScopes()));
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        Map<String, String> m = ConfigUtil.getMap(properties);
        String profile = m.get("security.profile");
        if (profile != null) {
            deployProfileConfig(profile);
        } else {
            removeProfile();
        }
        legacyMode = Boolean.parseBoolean(m.get("security.legacyMode"));
        migrationReporting = Boolean.parseBoolean(m.get("security.migrationReporting"));
    }

    private void deployProfileConfig(String profile) {
        URL url = context.getBundle().getResource("META-INF/configuration-profiles/profile-" + profile + "." + "yml");
        if (url != null) {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", "org.jahia.bundles.api.authorization-default." + "yml");
            try (InputStream input = url.openStream()) {
                List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8);
                lines.add(0, "# Do not edit - Configuration file provided by module, any change will be lost");
                try (Writer w = new FileWriter(path.toFile())) {
                    IOUtils.writeLines(lines, null, w);
                }
                logger.info("Copied configuration file of module {} into {}", url, path);
            } catch (IOException e) {
                logger.error("unable to copy configuration", e);
            }
        } else {
            logger.error("Invalid security-filter profile : {}", profile);
            removeProfile();
        }
    }

    private void removeProfile() {
        try {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", "org.jahia.bundles.api.authorization-default.yml");
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            logger.error("unable to remove configuration", e);
        }
    }

    public void addScopes(Collection<String> scopes, HttpServletRequest request) {
        if (currentScopesLocal.get() == null) {
            currentScopesLocal.set(new HashSet<>());
        }
        currentScopesLocal.get().addAll(authorizationConfig.getScopes().stream()
                .filter(scope -> scopes.contains(scope.getScopeName()))
                .filter(scope -> scope.isValid(request))
                .collect(Collectors.toSet()));
    }

    public void initScopes(HttpServletRequest request) {
        Set<String> scopeNames = authorizationConfig.getScopes().stream()
                .filter(scope -> scope.shouldAutoApply(request))
                .filter(scope -> scope.isValid(request))
                .map(ScopeDefinitionImpl::getScopeName)
                .collect(Collectors.toSet());
        logger.debug("Auto apply following scopes : {}", scopeNames);
        addScopes(scopeNames, request);
    }

    public void resetScopes() {
        currentScopesLocal.remove();
    }

    @Override
    public boolean hasPermission(String apiToCheck) {
        if (apiToCheck == null) {
            throw new IllegalArgumentException("Must pass an api name");
        }

        return hasPermission(Collections.singletonMap("api", apiToCheck));
    }

    @Override
    public boolean hasPermission(String apiToCheck, Node node) {
        if (apiToCheck == null) {
            throw new IllegalArgumentException("Must pass an api name");
        }

        Map<String, Object> query = new HashMap<>();
        query.put("api", apiToCheck);
        query.put("node", node);

        return hasPermission(query);
    }

    @Override
    public boolean hasPermission(Map<String, Object> query) {
        if (query == null) {
            throw new IllegalArgumentException("Must pass a valid api query");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("============ Start query check {} ============ ", query);
        }

        Collection<ScopeDefinition> currentScopes = getCurrentScopes();

        if (currentScopes == null) {
            // initScope has not been called, bypass security check
            return true;
        }

        boolean hasPermission = false;
        boolean hasLegacyPermission = false;

        if (!legacyMode || migrationReporting) {
            hasPermission = hasPermission(query, currentScopes);
            if (logger.isDebugEnabled()) {
                logger.debug("== Permission check result : {}", debugResult(hasPermission));
            }
        }

        if (legacyMode || migrationReporting) {
            try {
                hasLegacyPermission = permissionsConfig.hasPermission((String) query.get("api"), (JCRNodeWrapper) query.get("node"));
                logger.debug("Checking legacy api permission {} : {}", query, debugResult(hasLegacyPermission));
            } catch (RepositoryException e) {
                logger.error("Error when checking legacy permission", e);
            }
        }

        if (migrationReporting && (hasLegacyPermission != hasPermission) && logger.isWarnEnabled()) {
            logger.warn("Permission check for {} : legacy mode is {}, standard mode is {}. Active scopes are {}", query, debugResult(hasLegacyPermission), debugResult(hasPermission), currentScopes.stream().map(ScopeDefinition::getScopeName).collect(Collectors.toList()));
        }
        if (logger.isTraceEnabled()) {
            logger.trace("============ End query check {} ============", query);
            // add new line for reading
            logger.trace("");
        }

        if (!legacyMode) {
            return hasPermission;
        } else {
            return hasLegacyPermission;
        }
    }

    private boolean hasPermission(Map<String, Object> query, Collection<ScopeDefinition> currentScopes) {

        for (ScopeDefinitionImpl scope : authorizationConfig.getScopes()) {
            logger.trace("== Check Scope : [{}]", scope.getScopeName());
            boolean sameScope = currentScopes.stream().anyMatch(scopeDefinition -> scopeDefinition.getScopeName().equals(scope.getScopeName()));
            if (sameScope && scope.isGrantAccess(query)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("=> Scope [{}] : GRANTED", scope.getScopeName());
                }
                return true;
            }
            if (logger.isTraceEnabled()) {
                if (!currentScopes.contains(scope)) {
                    logger.trace("Scope not in current scopes [{}]", currentScopes.stream().map(ScopeDefinition::getScopeName).collect(Collectors.joining(",")));
                }
                logger.trace("=> Scope [{}] : DENIED", scope.getScopeName());
            }
        }
        return false;
    }

    private String debugResult(boolean value) {
        return value ? "GRANTED" : "DENIED";
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public void setPermissionsConfig(PermissionsConfig permissionsConfig) {
        this.permissionsConfig = permissionsConfig;
    }

    public void setAuthorizationConfig(AuthorizationConfig authorizationConfig) {
        this.authorizationConfig = authorizationConfig;
    }
}

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
package org.jahia.bundles.securityfilter.core;

import org.apache.commons.io.IOUtils;
import org.jahia.api.settings.SettingsBean;
import org.jahia.bin.Jahia;
import org.jahia.bundles.securityfilter.legacy.PermissionsConfig;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.securityfilter.PermissionService;
import org.jahia.services.securityfilter.ScopeDefinition;
import org.jahia.utils.DeprecationUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Component(service = { PermissionService.class, ManagedService.class }, immediate = true, property = {
        Constants.SERVICE_PID + "=org.jahia.bundles.api.security", Constants.SERVICE_DESCRIPTION + "=Security filter: core service",
        Constants.SERVICE_VENDOR + "=" + Jahia.VENDOR_NAME })
public class PermissionServiceImpl implements PermissionService, ManagedService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private AuthorizationConfig authorizationConfig;
    private PermissionsConfig permissionsConfig;
    private final ThreadLocal<Set<ScopeDefinition>> currentScopesLocal = new ThreadLocal<>();
    private BundleContext context;
    private SettingsBean settingsBean;

    private boolean legacyMode = false;
    private boolean migrationReporting = false;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setPermissionsConfig(PermissionsConfig permissionsConfig) {
        this.permissionsConfig = permissionsConfig;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setAuthorizationConfig(AuthorizationConfig authorizationConfig) {
        this.authorizationConfig = authorizationConfig;
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public Collection<ScopeDefinition> getCurrentScopes() {
        return currentScopesLocal.get() != null ? Collections.unmodifiableSet(currentScopesLocal.get()) : null;
    }

    public void setCurrentScopes(Collection<ScopeDefinition> scopes) {
        currentScopesLocal.set(new HashSet<>(scopes));
    }

    public Collection<ScopeDefinition> getAvailableScopes() {
        return Collections.unmodifiableSet(new HashSet<>(authorizationConfig.getScopes()));
    }

    static final String PROFILE_OFF = "off";
    static final String PROFILE_DEFAULT = "default";
    static final String AUTHORIZATION_DEFAULT_FILE = "org.jahia.bundles.api.authorization-default.yml";
    static final String MANAGED_FILE_HEADER =
            "# This file is managed by Jahia (security-filter bundle) and should not be modified directly.\n"
                    + "# To change the active security profile, update the 'security.profile' property in\n"
                    + "# org.jahia.bundles.api.security.cfg (karaf/etc/).\n";

    @Override
    public void updated(Dictionary<String, ?> properties) {
        Map<String, String> m = ConfigUtil.getMap(properties);
        String profile = m.get("security.profile");
        if (profile == null) {
            // fallback to 'default' if not specified
            logger.warn("No security.profile property found, falling back to '{}' profile", PROFILE_DEFAULT);
            profile = PROFILE_DEFAULT;
        }
        if ("compat".equals(profile)) {
            DeprecationUtils.onDeprecatedFeatureUsage("Security 'compat' profile", "8.2.4.0", true,
                    "The 'compat' ('compatibility') is strongly discouraged, please use 'default' instead.");
        }
        if (PROFILE_OFF.equals(profile)) {
            disableProfile();
        } else {
            deployProfileConfig(profile);
        }
        legacyMode = Boolean.parseBoolean(m.get("security.legacyMode"));
        migrationReporting = Boolean.parseBoolean(m.get("security.migrationReporting"));
    }

    private void deployProfileConfig(String profile) {
        URL url = context.getBundle().getResource("META-INF/configuration-profiles/profile-" + profile + ".yml");
        if (url != null) {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", AUTHORIZATION_DEFAULT_FILE);
            try (InputStream input = url.openStream()) {
                writeAuthorizationFile(path, IOUtils.toString(input, StandardCharsets.UTF_8));
                logger.info("Deployed security profile '{}' into {}", profile, path);
            } catch (IOException e) {
                logger.error("Unable to deploy security profile '{}'", profile, e);
            }
        } else {
            logger.error("Invalid security-filter profile: '{}'. Falling back to '{}' profile.", profile, PROFILE_DEFAULT);
            if (!PROFILE_DEFAULT.equals(profile)) {
                deployProfileConfig(PROFILE_DEFAULT);
            }
        }
    }

    private void disableProfile() {
        logger.warn(
                "Security profile is set to '{}': no Jahia-provided security profile is active, the file {} will be emptied. Make sure you have provided authorization rules via other supported configuration mechanisms.",
                PROFILE_OFF, AUTHORIZATION_DEFAULT_FILE);
        Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", AUTHORIZATION_DEFAULT_FILE);
        String content = "# security.profile=off - No Jahia built-in profile is active.\n"
                + "# Provide your own authorization configuration via other supported Jahia configuration mechanisms.\n"
                + "{}\n"; // required to get a valid empty YAML file
        try {
            writeAuthorizationFile(path, content);
            logger.info("Emptied security authorization config at {} (profile=off)", path);
        } catch (IOException e) {
            logger.error("Unable to write disabled-profile placeholder to {}", path, e);
        }
    }

    /**
     * Writes {@code content} to {@code target} via a temporary file in the same directory,
     * then atomically replaces the target.
     * <p>
     * The {@link #MANAGED_FILE_HEADER} is always prepended.
     * <p>
     * The atomic move avoids a race condition where fileinstall could observe the target file
     * mid-write: writing directly would truncate it first, and if fileinstall reads it before
     * writing is complete, it would see an empty or incomplete file and fail to parse it.
     */
    private void writeAuthorizationFile(Path target, String content) throws IOException {
        Path tmp = Files.createTempFile(target.getParent(), ".authorization-default-", ".yml.tmp");
        try (Writer w = Files.newBufferedWriter(tmp)) {
            w.write(MANAGED_FILE_HEADER);
            w.write(content);
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    public void addScopes(Collection<String> scopes, HttpServletRequest request) {
        if (currentScopesLocal.get() == null) {
            currentScopesLocal.set(new HashSet<>());
        }
        currentScopesLocal.get().addAll(authorizationConfig.getScopes().stream().filter(scope -> scopes.contains(scope.getScopeName()))
                .filter(scope -> scope.isValid(request)).collect(Collectors.toSet()));
    }

    public void initScopes(HttpServletRequest request) {
        Set<String> scopeNames = authorizationConfig.getScopes().stream().filter(scope -> scope.shouldAutoApply(request))
                .filter(scope -> scope.isValid(request)).map(ScopeDefinitionImpl::getScopeName).collect(Collectors.toSet());
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
            logger.warn("Permission check for {} : legacy mode is {}, standard mode is {}. Active scopes are {}", query,
                    debugResult(hasLegacyPermission), debugResult(hasPermission),
                    currentScopes.stream().map(ScopeDefinition::getScopeName).collect(Collectors.toList()));
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
            boolean sameScope = currentScopes.stream()
                    .anyMatch(scopeDefinition -> scopeDefinition.getScopeName().equals(scope.getScopeName()));
            if (sameScope && scope.isGrantAccess(query)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("=> Scope [{}] : GRANTED", scope.getScopeName());
                }
                return true;
            }
            if (logger.isTraceEnabled()) {
                if (!currentScopes.contains(scope)) {
                    logger.trace("Scope not in current scopes [{}]",
                            currentScopes.stream().map(ScopeDefinition::getScopeName).collect(Collectors.joining(",")));
                }
                logger.trace("=> Scope [{}] : DENIED", scope.getScopeName());
            }
        }
        return false;
    }

    private String debugResult(boolean value) {
        return value ? "GRANTED" : "DENIED";
    }
}

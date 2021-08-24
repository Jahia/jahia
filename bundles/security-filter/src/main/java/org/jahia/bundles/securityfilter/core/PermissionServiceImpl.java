package org.jahia.bundles.securityfilter.core;

import org.apache.commons.io.IOUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.jahia.api.settings.SettingsBean;
import org.jahia.services.securityfilter.PermissionService;
import org.jahia.services.securityfilter.ScopeDefinition;
import org.jahia.bundles.securityfilter.legacy.PermissionsConfig;
import org.jahia.services.content.JCRNodeWrapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
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
    private boolean supportYaml = false;

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
            removeProfile("cfg");
            removeProfile("yml");
        }
        legacyMode = Boolean.parseBoolean(m.get("security.legacyMode"));
        migrationReporting = Boolean.parseBoolean(m.get("security.migrationReporting"));
    }

    private void deployProfileConfig(String profile) {
        String ext = supportYaml ? "yml" : "cfg";
        URL url = context.getBundle().getResource("META-INF/configuration-profiles/profile-" + profile + "." + ext);
        if (url != null) {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", "org.jahia.modules.api.authorization-default." + ext);
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
            removeProfile(supportYaml ? "cfg" : "yml");
        } else {
            logger.error("Invalid security-filter profile : {}", profile);
            removeProfile("cfg");
            removeProfile("yml");
        }
    }

    private void removeProfile(final String ext) {
        try {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", "org.jahia.modules.api.authorization-default." + ext);
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

        Collection<ScopeDefinition> currentScopes = getCurrentScopes();

        if (currentScopes == null) {
            // initScope has not been called, bypass security check
            return true;
        }

        boolean hasPermission = false;
        boolean hasLegacyPermission = false;

        if (!legacyMode || migrationReporting) {
            hasPermission = authorizationConfig.getScopes().stream()
                    .filter(currentScopes::contains)
                    .anyMatch(p -> p.isGrantAccess(query));
            logger.debug("Checking api permission {} with scopes {} : {}", query, currentScopes.stream().map(ScopeDefinition::getScopeName).collect(Collectors.toList()), debugResult(hasPermission));
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

        if (!legacyMode) {
            return hasPermission;
        } else {
            return hasLegacyPermission;
        }
    }

    private String debugResult(boolean value) {
        return value ? "GRANTED" : "DENIED";
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public void setArtifactInstaller(Collection<ArtifactInstaller> installers) {
         for (ArtifactInstaller installer : installers) {
            supportYaml |= installer.canHandle(new File("config.yml"));
        }
    }

    public void setPermissionsConfig(PermissionsConfig permissionsConfig) {
        this.permissionsConfig = permissionsConfig;
    }

    public void setAuthorizationConfig(AuthorizationConfig authorizationConfig) {
        this.authorizationConfig = authorizationConfig;
    }
}

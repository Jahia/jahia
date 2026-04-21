package org.jahia.bundles.securityfilter.core.grant;

import org.jahia.services.modulemanager.util.PropertiesValues;

/**
 * A factory for building {@link Grant} instances from parsed configuration values.
 *
 * <p>Each implementation is responsible for a specific top-level key in a grant block
 * (e.g., {@code "api"} or {@code "node"}). The key is exposed via {@link #getKey()} so
 * that callers can validate grant blocks against the set of known keys without relying on
 * hardcoded strings.
 *
 */
public interface GrantBuilder {

    /**
     * Returns the top-level configuration key this builder handles (e.g., {@code "api"} or {@code "node"}).
     *
     * @return the configuration key
     */
    String getKey();

    /**
     * Builds a {@link Grant} from the given grant configuration values.
     *
     * @param grantValues the parsed configuration values for a single grant entry
     * @return a {@link Grant} if this builder's key is present in {@code grantValues}, or {@code null} otherwise
     * @throws IllegalArgumentException if the block for this builder's key contains unrecognized sub-keys
     */
    Grant build(PropertiesValues grantValues) throws IllegalArgumentException;
}


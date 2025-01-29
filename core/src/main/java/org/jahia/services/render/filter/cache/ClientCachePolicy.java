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
 *     Copyright (C) 2002-2025 Jahia Solutions Group. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Jerome Blanchard
 */
public class ClientCachePolicy implements Serializable {

    public static final String CLIENT_CACHE_FRAGMENT_POLICY_PROPERTY_NAME = "clientCacheFragmentPolicy";

    public static final ClientCachePolicy PRIVATE = new ClientCachePolicy(Level.PRIVATE);
    public static final ClientCachePolicy PUBLIC = new ClientCachePolicy(Level.PUBLIC);
    public static final ClientCachePolicy DEFAULT = PUBLIC;

    private static final long serialVersionUID = 1L;
    private final Level level;
    private final int ttl;

    public ClientCachePolicy(Level level, int ttl) {
        this.level = level;
        this.ttl = ttl;
    }

    public ClientCachePolicy(Level policy) {
        this(policy, 0);
    }

    public Level getLevel() {
        return level;
    }

    public int getTtl() {
        return ttl;
    }

    /**
     * A policy is considered stronger (in terms of time the content is keep in cache) if the cache policy level index
     * is higher than the current one or if the ttl is higher than the current ttl (only relevant if level is custom)
     *
     * The strongest policy is PRIVATE (there is no caching) and the weakest is IMMUTABLE (the content is cached forever)
     *
     * @param policy, the policy to compare with
     * @return true if the current policy is stronger than the one passed in parameter, false otherwise
     */
    public boolean isStronger(ClientCachePolicy policy) {
        return (this.getLevel().getIndex() < policy.getLevel().getIndex()) ||
                (this.getLevel().getIndex() == policy.getLevel().getIndex() && policy.getTtl() > ttl);
    }

    public static ClientCachePolicy strongest(ClientCachePolicy a, ClientCachePolicy b) {
        return a.isStronger(b) ? a : b;
    }

    public static ClientCachePolicy strongest(List<ClientCachePolicy> policies) {
        return policies.stream().reduce(ClientCachePolicy::strongest).orElse(null);
    }

    public enum Level {
        PRIVATE("private", 0),
        CUSTOM("custom", 10),
        PUBLIC("public", 20),
        IMMUTABLE("immutable", 50);

        private final String value;
        private final int index;

        Level(String value, int index) {
            this.value = value;
            this.index = index;
        }

        public String getValue() {
            return value;
        }

        public int getIndex() {
            return index;
        }

        @Override public String toString() {
            return "Level{" + "value='" + value + '\'' + ", index=" + index + '}';
        }

    }

    @Override public String toString() {
        return "ClientCachePolicy{" + "level=" + level + ", ttl=" + ttl + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ClientCachePolicy that = (ClientCachePolicy) o;
        return ttl == that.ttl && level == that.level;
    }

    @Override public int hashCode() {
        return Objects.hash(level, ttl);
    }
}

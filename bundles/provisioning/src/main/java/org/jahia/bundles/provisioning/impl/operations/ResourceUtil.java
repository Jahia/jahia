package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.provisioning.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.util.Map;

/**
 * Utility to get a resource
 */
public final class ResourceUtil {
    private ResourceUtil() {
    }

    /**
     * Get resource
     * @param key key
     * @param executionContext executioncontext
     * @return resource
     * @throws IOException exception
     */
    public static Resource getResource(String key, ExecutionContext executionContext) throws IOException {
        if (key.contains("://")) {
            return new UrlResource(key);
        } else {
            Map<String, Resource> resources = (Map<String, Resource>) executionContext.getContext().get("resources");
            if (resources != null) {
                return resources.get(key);
            }
        }
        throw new IOException("Resource not found");
    }
}


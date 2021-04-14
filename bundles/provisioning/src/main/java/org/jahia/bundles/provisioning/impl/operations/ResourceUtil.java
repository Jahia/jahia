package org.jahia.bundles.provisioning.impl.operations;

import org.jahia.services.provisioning.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility to get a resource
 */
public final class ResourceUtil {
    private ResourceUtil() {
    }

    private static final Pattern PATTERN = Pattern.compile("^[a-z][a-z0-9+.-]+:.*");
    /**
     * Get resource
     * @param key key
     * @param executionContext executioncontext
     * @return resource
     * @throws IOException exception
     */
    public static Resource getResource(String key, ExecutionContext executionContext) throws IOException {
        if (PATTERN.matcher(key).matches()) {
            return new UrlResource(key);
        } else {
            Map<String, Resource> resources = (Map<String, Resource>) executionContext.getContext().get("resources");
            if (resources != null) {
                return resources.get(key);
            }
        }
        throw new IOException(MessageFormat.format("Resource not found, {0}", key));
    }
}


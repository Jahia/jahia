package org.jahia.bundles.extender.jahiamodules;

import org.osgi.framework.Bundle;
import org.springframework.core.io.UrlResource;

import java.net.URL;

/**
 * An implementation of a Spring resource that can resolve files inside bundles
 */
public class BundleResource extends UrlResource {

    private Bundle bundle;

    public BundleResource(URL url, Bundle bundle) {
        super(url);
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }
}

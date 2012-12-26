package org.jahia.bundles.extender.jahiamodules;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.osgi.framework.Bundle;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A specialized JahiaTemplatesPackage that uses OSGi bundles
 */
public class JahiaBundleTemplatesPackage extends JahiaTemplatesPackage {

    private Bundle bundle = null;
    private String rootFolderPath = null;

    private ClassLoader classLoader;

    public JahiaBundleTemplatesPackage(Bundle bundle) {
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public Resource getResource(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        URL entryURL = bundle.getEntry(relativePath);
        if (entryURL != null) {
            return new BundleResource(entryURL, bundle);
        } else {
            return null;
        }
    }

    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public void setRootFolderPath(String rootFolderPath) {
        this.rootFolderPath = rootFolderPath;
    }

    @Override
    public Resource[] getResources(String relativePath) {
        Enumeration<URL> resourceEnum = bundle.findEntries(relativePath, null, false);
        List<Resource> resources = new ArrayList<Resource>();
        if (resourceEnum == null) {
            return new Resource[0];
        } else {
            while (resourceEnum.hasMoreElements()) {
                resources.add(new BundleResource(resourceEnum.nextElement(), bundle));
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}

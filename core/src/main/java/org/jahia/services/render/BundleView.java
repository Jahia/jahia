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
package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.render.scripting.bundle.BundleSourceResourceResolver;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementation of a view, located in an OSGi bundle.
 */
public class BundleView extends BaseView {

    private String resource;
    private BundleSourceResourceResolver bundleSourceResourceResolver;

    /**
     * Initializes an instance of this class.
     * 
     * @param resource
     *            the resource path in the underlying OSGi bundle
     * @param key
     *            the key of the view
     * @param module
     *            corresponding {@link JahiaTemplatesPackage} instance
     * @param displayName
     *            the display name for this view
     */
    public BundleView(String resource, String key, JahiaTemplatesPackage module, String displayName) {
        super(resource, key, module, displayName);
        this.resource = resource;
    }

    @Override
    public String getPath() {
        return getModule().getRootFolderPath() + resource;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && getBundle().getBundleId() == ((BundleView) o).getBundle().getBundleId();
    }

    public Bundle getBundle() {
        return getModule().getBundle();
    }

    public BundleSourceResourceResolver getBundleSourceResourceResolver() {
        if (bundleSourceResourceResolver == null) {
            bundleSourceResourceResolver = new BundleSourceResourceResolver(getBundle());
        }
        return bundleSourceResourceResolver;
    }

    /**
     * Returns an input stream for the view resource or null if the resource does not exist.
     * 
     * @return an input stream for the view resource or null if the resource does not exist
     * @throws IOException
     *             in case of an I/O errors
     */
    public InputStream getInputStream() throws IOException {
        return getInputStream(resource);
    }

    @Override
    protected InputStream getInputStream(String resource) throws IOException {
        URL url = getResource(resource);
        return url != null ? url.openStream() : null;
    }

    public String getResource() {
        return resource;
    }

    @Override
    protected URL getResource(String resource) {
        if (getBundleSourceResourceResolver().hasSourceURLs()) {
            URL resourceURL = getBundleSourceResourceResolver().getResource(resource);
            if (resourceURL != null) {
                return resourceURL;
            }
        }
        return getBundle().getResource(resource);
    }

    @Override
    protected String getResourcePath(String resource) {
        URL url = getResource(resource);
        return url != null ? getModule().getRootFolderPath() + resource : null;
    }



    @Override
    public int hashCode() {
        long bundleId = getBundle().getBundleId();
        return 31 * super.hashCode() + ((int) (bundleId ^ (bundleId >>> 32)));
    }
}

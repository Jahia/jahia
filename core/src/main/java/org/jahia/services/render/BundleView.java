/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.osgi.framework.Bundle;

/**
 * Implementation of a view, located in an OSGi bundle.
 */
public class BundleView extends BaseView {

    private String resource;

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

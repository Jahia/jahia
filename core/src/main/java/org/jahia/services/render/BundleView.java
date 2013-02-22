/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
        super(module.getRootFolderPath() + resource, key, module, displayName);
        this.resource = resource;
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

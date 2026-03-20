/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.io;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Resource backed by a URL inside an OSGi bundle.
 * Extends UrlIOResource with bundle-aware lastModified and equality semantics.
 */
public class BundleIOResource extends UrlIOResource {

    private final Bundle bundle;

    public BundleIOResource(URL url, Bundle bundle) {
        super(url);
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public long lastModified() throws IOException {
        String lastModified = bundle.getHeaders().get("Bnd-LastModified");
        if (lastModified != null) {
            try {
                return Long.parseLong(lastModified);
            } catch (NumberFormatException e) {
                // Ignore header, fall back
            }
        }
        return bundle.getLastModified();
    }

    @Override
    public boolean exists() {
        // Felix may open an InputStream internally when checking contentLength on bundle URLs,
        // so we skip contentLength and directly attempt to open and close the stream.
        // If the stream opens successfully, the resource exists.
        // This avoids resource leaks from unreleased internal streams.
        try {
            URLConnection con = getURL().openConnection();
            con.setUseCaches(false);
            con.getInputStream().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        // override the default implementation to consider the bundle, same url could be present in different bundles
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BundleIOResource that = (BundleIOResource) o;
        return bundle.getSymbolicName().equals(that.bundle.getSymbolicName())
                && getURL().getPath().equals(that.getURL().getPath());
    }

    @Override
    public int hashCode() {
        // override the default implementation to consider the bundle, same url could be present in different bundles
        int result = bundle.getSymbolicName().hashCode();
        result = 31 * result + getURL().getPath().hashCode();
        return result;
    }

    @Override
    public String getDescription() {
        return "bundle [" + bundle.getSymbolicName() + "] resource [" + getURL() + "]";
    }
}

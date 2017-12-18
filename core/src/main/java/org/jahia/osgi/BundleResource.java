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
package org.jahia.osgi;

import org.osgi.framework.Bundle;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

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

    @Override
    public long lastModified() throws IOException {
        String lastModifed = bundle.getHeaders().get("Bnd-LastModified");
        if (lastModifed != null) {
            try {
                return Long.parseLong(lastModifed);
            } catch (NumberFormatException e) {
                // Ignore header
            }
        }
        return bundle.getLastModified();
    }

    @Override
    public boolean exists() {
        try {
            URL url = getURL();
            URLConnection con = url.openConnection();
            try {
                return con.getContentLength() >= 0;
            } finally {
                con.getInputStream().close();
            }
        } catch (IOException e) {
            return false;
        }
    }

}

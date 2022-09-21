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

/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.mvn;

import org.apache.felix.utils.collections.MapToDictionary;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.MavenResolvers;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.url.AbstractURLStreamHandlerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Maven URL stream handler that clean up the #runtime added by JDK 11 to allow a correct parsing by pax.
 */
public class MavenURLStreamHandler extends AbstractURLStreamHandlerService {

    private final ConfigurationAdmin configurationAdmin;

    public MavenURLStreamHandler(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;

    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        Dictionary<String, String> props = getMavenConfig();
        // java11 adds #runtime as a ref in the URL which breaks the artifact version parser in pax-aether-url
        String cleanedURL = url.toExternalForm().replace("#runtime", "");
        return new URLConnection(new URL(cleanedURL)) {
            @Override
            public void connect() throws IOException {
                // nothing to be done
            }

            @Override
            public InputStream getInputStream() throws IOException {
                try (MavenResolver resolver = MavenResolvers.createMavenResolver(props, ServiceConstants.PID)) {
                    File resolve = resolver.resolve(url.toExternalForm());
                    return new FileInputStream(resolve);
                }
            }
        };
    }

    private Dictionary<String, String> getMavenConfig() throws IOException {
        Map<String, String> props = new HashMap<>();
        if (configurationAdmin != null) {
            Configuration config = configurationAdmin.getConfiguration("org.ops4j.pax.url.mvn", null);
            if (config != null) {
                Dictionary<String, Object> cfg = config.getProperties();
                if (cfg != null) {
                    for (Enumeration<String> e = cfg.keys(); e.hasMoreElements(); ) {
                        String key = e.nextElement();
                        Object val = cfg.get(key);
                        if (key != null) {
                            props.put(key, val.toString());
                        }
                    }
                }
            }
        }
        return new MapToDictionary(props);
    }
}

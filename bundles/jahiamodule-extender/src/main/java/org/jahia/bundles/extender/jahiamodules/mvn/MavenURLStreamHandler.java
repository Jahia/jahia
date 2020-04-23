/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.mvn;

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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Maven URL stream handler that clean up the #runtime added by JDK 11 to allow a correct parsing by pax.
 */
public class MavenURLStreamHandler extends AbstractURLStreamHandlerService {

    private final ConfigurationAdmin configurationAdmin;

    public MavenURLStreamHandler(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;

    }

    @Override public URLConnection openConnection(URL url) throws IOException {
        Dictionary<String, String> props = getMavenConfig();
        MavenResolver resolver = MavenResolvers.createMavenResolver(props, ServiceConstants.PID);
        // java11 adds #runtime as a ref in the URL which breaks the artifact version parser in pax-aether-url
        String cleanedURL = url.toExternalForm().replace("#runtime", "");
        return new URLConnection(new URL(cleanedURL)) {
            @Override public void connect() throws IOException {
                // nothing to be done
            }

            @Override public InputStream getInputStream() throws IOException {
                File resolve = resolver.resolve(url.toExternalForm());
                return new FileInputStream(resolve);
            }
        };
    }

    private Dictionary<String, String> getMavenConfig() throws IOException {
        Hashtable<String, String> props = new Hashtable<>();
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
        return props;
    }
}

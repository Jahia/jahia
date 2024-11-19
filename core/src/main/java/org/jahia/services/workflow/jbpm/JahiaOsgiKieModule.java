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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm;

import org.drools.compiler.kie.builder.impl.AbstractKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;

/**
 * An re-implementation of the org.drools.osgi.compiler.OsgiKieModule to make it possible to pass the bundle
 * reference directly, instead of it being resolved by the URL.
 */
public class JahiaOsgiKieModule extends AbstractKieModule {

    private final Bundle bundle;
    private final int bundleUrlPrefixLength;

    private Set<String> fileNames;

    public JahiaOsgiKieModule(ReleaseId releaseId, KieModuleModel kModuleModel, Bundle bundle, int bundleUrlPrefixLength) {
        super(releaseId, kModuleModel);
        this.bundle = bundle;
        this.bundleUrlPrefixLength = bundleUrlPrefixLength;
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAvailable(String pResourceName) {
        return fileNames.contains(pResourceName);
    }

    @Override
    public byte[] getBytes(String pResourceName) {
        URL url = bundle.getEntry(pResourceName);
        return url == null ? null : readUrlAsBytes(url);
    }

    @Override
    public Collection<String> getFileNames() {
        if (fileNames != null) {
            return fileNames;
        }

        fileNames = new HashSet<>();
        Enumeration<URL> e = bundle.findEntries("", "*", true);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            String urlString = url.toString();
            if (urlString.endsWith("/")) {
                continue;
            }
            fileNames.add(urlString.substring(bundleUrlPrefixLength));
        }
        return fileNames;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    private static byte[] readUrlAsBytes(URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            return readBytesFromInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String readUrlAsString(URL url) {
        return new String(readUrlAsBytes(url));
    }

    private static String getPomProperties(Bundle bundle) {
        Enumeration<URL> e = bundle.findEntries("META-INF/maven", "pom.properties", true);
        if (!e.hasMoreElements()) {
            throw new RuntimeException("Cannot find pom.properties file in bundle " + bundle);
        }
        return readUrlAsString(e.nextElement());
    }

    public static JahiaOsgiKieModule create(URL url, Bundle bundle) {
        KieModuleModel kieProject = KieModuleModelImpl.fromXML(url);
        String urlString = url.toString();

        String pomProperties = getPomProperties(bundle);
        ReleaseId releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        return new JahiaOsgiKieModule(releaseId, kieProject, bundle, urlString.indexOf("META-INF"));
    }

    @Override
    public String toString() {
        return "JahiaOsgiKieModule{" +
                "bundle=" + bundle + "," +
                "releaseId=" + getReleaseId() +
                '}';
    }
}

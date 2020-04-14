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
package org.jahia.bundles.extender.jahiamodules.jsp;

import org.apache.jasper.compiler.JarScannerFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.util.scan.JarFactory;
import org.ops4j.pax.web.jsp.TldScanner;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.xml.sax.SAXException;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class BundleTldScanner extends TldScanner {
    private final ServletContext context;

    public BundleTldScanner(ServletContext context, boolean validate, boolean blockExternal) {
        super(context, true, validate, blockExternal);
        this.context = context;
    }

    private static Object invokeMethod(Object object, String... methods) throws ReflectiveOperationException {
        for (String method : methods) {
            object = object.getClass().getMethod(method).invoke(object);
        }
        return object;
    }

    /**
     * Scan content of a JAR file
     * @param jar
     * @throws IOException
     */
    public void scanJar(Jar jar) throws IOException {
        URL jarFileUrl = jar.getJarFileURL();
        jar.nextEntry();
        for (String entryName = jar.getEntryName();
             entryName != null;
             jar.nextEntry(), entryName = jar.getEntryName()) {
            if (!(entryName.startsWith("META-INF/") &&
                    entryName.endsWith(".tld"))) {
                continue;
            }
            TldResourcePath tldResourcePath = new TldResourcePath(jarFileUrl, null, entryName);
            try {
                parseTld(tldResourcePath);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Scan for TLDs in JARs in /WEB-INF/lib.
     *
     * @throws IOException
     */
    @Override
    public void scanJars() throws IOException {
        JarScannerFactory.getJarScanner(context).scan(JarScanType.TLD, context, new JarScannerCallback() {
            @Override
            public void scan(Jar jar, String s, boolean b) throws IOException {
                scanJar(jar);
            }

            @Override
            public void scan(File file, String s, boolean b) throws IOException {
                // Ignore
            }

            @Override
            public void scanWebInfClasses() throws IOException {
                // Ignore
            }
        });
    }

    /**
     * Scan content of a Bundle
     * @param bundle
     * @throws IOException
     */
    public void scanBundle(Bundle bundle) throws IOException {
        if (bundle.getBundleId() > 0) {
            try {
                // Build file:// url to bundle jar
                URL bundleUrl = (URL) invokeMethod(bundle.adapt(BundleRevision.class), "getContent", "getFile", "toURI", "toURL");

                // Look for nested JARs inside bundle
                Enumeration<URL> jars = bundle.findEntries("", "*.jar", false);
                if (jars != null) {
                    while (jars.hasMoreElements()) {
                        URL url = jars.nextElement();
                        scanJar(JarFactory.newInstance(new URL("jar:" + bundleUrl.toString() + "!" + url.getFile())));
                    }
                }

                // Look for TLDs inside bundle
                Enumeration<URL> tlds = bundle.findEntries("META-INF", "*.tld", true);
                if (tlds != null) {
                    while (tlds.hasMoreElements()) {
                        URL url = tlds.nextElement();
                        TldResourcePath backed = new TldResourcePath(bundleUrl, null, url.getFile().substring(1));
                        parseTld(backed);
                    }
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}

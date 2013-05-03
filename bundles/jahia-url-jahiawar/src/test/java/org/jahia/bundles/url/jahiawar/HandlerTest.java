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

package org.jahia.bundles.url.jahiawar;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Unit test for {@link Handler}.
 */
public class HandlerTest {

    /**
     * Protocol handler can be used.
     *
     * @throws java.io.IOException - Unexpected
     */
    @Test
    public void use()
            throws IOException, URISyntaxException {
        // System.setProperty( "java.protocol.handler.pkgs", "org.jahia.bundles.url.jahiawar" );
        URL jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war", new Handler());
        JarInputStream jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        JarEntry jarEntry = null;
        // copy the attributes to sort them
        Map<String, String> mainAttributes = new TreeMap<String, String>();
        for (Map.Entry<Object, Object> attribute : jarInputStream.getManifest().getMainAttributes().entrySet()) {
            mainAttributes.put(attribute.getKey().toString(), attribute.getValue().toString());
        }
        dumpManifestEntries(mainAttributes);
        Assert.assertEquals("Bundle-ClassPath header is not valid", ".,forum-1.3.jar", mainAttributes.get("Bundle-ClassPath"));
        Assert.assertEquals("Bundle-Version header is not valid", "1.3", mainAttributes.get("Bundle-Version"));

        dumpJarEntries(jarInputStream);
//        String[] importPackages = mainAttributes.get("Import-Package").split(",");
//        List<String> importPackageList = Arrays.asList(importPackages);
//        Assert.assertTrue("'default' module missing from Import-Package header", importPackageList.contains("default"));

//        String[] exportPackages = mainAttributes.get("Export-Package").split(",");
//        List<String> exportPackageList = Arrays.asList(exportPackages);
//        Assert.assertTrue("'JahiaForum' module missing from Export-Package header", exportPackageList.contains("JahiaForum"));

        // now let's try with another module
        jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/translateworkflow/1.2/translateworkflow-1.2.war", new Handler());
        jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        // copy the attributes to sort them
        mainAttributes.clear();
        for (Map.Entry<Object, Object> attribute : jarInputStream.getManifest().getMainAttributes().entrySet()) {
            mainAttributes.put(attribute.getKey().toString(), attribute.getValue().toString());
        }
        dumpManifestEntries(mainAttributes);
        dumpJarEntries(jarInputStream);

        URI firstModuleURI = new URI("jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war");
        String modulePath = firstModuleURI.getPath();

    }

    private void dumpJarEntries(JarInputStream jarInputStream) throws IOException {
        JarEntry jarEntry;
        System.out.println("JAR contents:");
        System.out.println("-------------");
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            System.out.println(jarEntry.getName());
            if (jarEntry.getName().endsWith(".jar")) {
                JarInputStream embeddedJar = new JarInputStream(jarInputStream);
                JarEntry embeddedJarEntry = null;
                while ((embeddedJarEntry = embeddedJar.getNextJarEntry()) != null) {
                    System.out.println("    " + embeddedJarEntry.getName());
                }
            }
        }
    }

    private void dumpManifestEntries(Map<String, String> mainAttributes) {
        System.out.println("MANIFEST.MF:");
        System.out.println("------------");
        for (Map.Entry<String, String> attribute : mainAttributes.entrySet()) {
            System.out.println(attribute.getKey() + ": " + attribute.getValue());
        }
    }

}
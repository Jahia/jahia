package org.jahia.bundles.url.jahiawar;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
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
            throws IOException {
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
        String[] importPackages = mainAttributes.get("Import-Package").split(",");
        List<String> importPackageList = Arrays.asList(importPackages);
        Assert.assertTrue("'default' module missing from Import-Package header", importPackageList.contains("default"));

        String[] exportPackages = mainAttributes.get("Export-Package").split(",");
        List<String> exportPackageList = Arrays.asList(exportPackages);
        Assert.assertTrue("'JahiaForum' module missing from Export-Package header", exportPackageList.contains("JahiaForum"));

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
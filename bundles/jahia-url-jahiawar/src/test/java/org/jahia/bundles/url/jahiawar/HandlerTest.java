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

import org.jahia.utils.osgi.BundleUtils;
import org.jahia.utils.osgi.ManifestValueClause;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.jar.Attributes;
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
        System.setProperty("org.jahia.bundles.url.jahiawar.importedPackages", "");
        System.setProperty("org.jahia.bundles.url.jahiawar.excludedImportPackages", "templates-wise=org.jahia.modules.docspace.rules");
        System.setProperty("org.jahia.bundles.url.jahiawar.excludedExportPackages", "templates-wise=org.jahia.modules.social");

        URL jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war", new Handler());
        System.out.println("Processing URL " + jahiaWarURL + "...");
        JarInputStream jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        Attributes mainAttributes = jarInputStream.getManifest().getMainAttributes();
        dumpManifest(jarInputStream);
        dumpJarEntries(jarInputStream);

        Assert.assertEquals("Bundle-SymbolicName", "forum", mainAttributes.getValue("Bundle-SymbolicName"));
        Assert.assertEquals("Bundle-Name", "Jahia Forum", mainAttributes.getValue("Bundle-Name"));
        Assert.assertEquals("Bundle-ClassPath header is not valid", ".,forum-1.3.jar", mainAttributes.getValue("Bundle-ClassPath"));
        Assert.assertEquals("Bundle-Version header is not valid", "1.3", mainAttributes.getValue("Bundle-Version"));

        List<ManifestValueClause> importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from rules DRL file
                "org.jahia.services.content",
                "org.jahia.services.content.rules",
                "org.jahia.services.render",
                "org.jahia.services.search",
                "org.apache.commons.lang.time",
                "org.slf4j",
                // check for imports coming from JSP files
                "org.jahia.services.render.scripting"
        });

        List<ManifestValueClause> exportPackageHeaderClauses = BundleUtils.getHeaderClauses("Export-Package", mainAttributes.getValue("Export-Package"));
        assertPackagesPresent("Missing expected package {0} in Export-Package header clauses", exportPackageHeaderClauses, new String[]{
                "org.jahia.modules.forum.actions"
        });

        URI firstModuleURI = new URI("jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/forum/1.3/forum-1.3.war");
        String modulePath = firstModuleURI.getPath();

        // now let's try with another module
        jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/translateworkflow/1.2/translateworkflow-1.2.war", new Handler());
        System.out.println("");
        System.out.println("Processing URL " + jahiaWarURL + "...");
        jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        mainAttributes = jarInputStream.getManifest().getMainAttributes();
        dumpManifest(jarInputStream);
        dumpJarEntries(jarInputStream);

        importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from jPDL workflow definition file
                "org.jahia.services.workflow.jbpm"
        });

        exportPackageHeaderClauses = BundleUtils.getHeaderClauses("Export-Package", mainAttributes.getValue("Export-Package"));
        assertPackagesPresent("Missing expected package {0} in Export-Package header clauses", exportPackageHeaderClauses, new String[]{
                "org.jahia.modules.translation.initializers"
        });


        jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/ldap/1.3/ldap-1.3.war", new Handler());
        System.out.println("");
        System.out.println("Processing URL " + jahiaWarURL + "...");
        jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        mainAttributes = jarInputStream.getManifest().getMainAttributes();
        dumpManifest(jarInputStream);
        dumpJarEntries(jarInputStream);

        exportPackageHeaderClauses = BundleUtils.getHeaderClauses("Export-Package", mainAttributes.getValue("Export-Package"));
        assertPackagesPresent("Missing expected package {0} in Export-Package header clauses", exportPackageHeaderClauses, new String[]{
                "org.jahia.params.valves",
                "org.jahia.services.usermanager"
        });

        jahiaWarURL = new URL(null, "jahiawar:https://devtools.jahia.com/nexus/content/groups/public/org/jahia/modules/social/1.5/social-1.5.war", new Handler());
        System.out.println("");
        System.out.println("Processing URL " + jahiaWarURL + "...");
        jarInputStream = new JarInputStream(jahiaWarURL.openStream());
        mainAttributes = jarInputStream.getManifest().getMainAttributes();
        dumpManifest(jarInputStream);
        dumpJarEntries(jarInputStream);

        importPackageHeaderClauses = BundleUtils.getHeaderClauses("Import-Package", mainAttributes.getValue("Import-Package"));
        assertPackagesPresent("Missing expected package {0} in Import-Package header clause", importPackageHeaderClauses, new String[]{
                // check for imports coming from Spring module application context file.
                "org.jahia.services.content.rules"
        });

        exportPackageHeaderClauses = BundleUtils.getHeaderClauses("Export-Package", mainAttributes.getValue("Export-Package"));
        assertPackagesPresent("Missing expected package {0} in Export-Package header clauses", exportPackageHeaderClauses, new String[]{
                "org.jahia.modules.social",
                "org.jahia.modules.social.choicelist",
                "org.jahia.modules.social.taglib",
        });
    }

    private void dumpManifest(JarInputStream jarInputStream) throws IOException {
        System.out.println("MANIFEST.MF:");
        System.out.println("------------");
        StringWriter stringWriter = new StringWriter();
        BundleUtils.dumpManifestHeaders(jarInputStream, new PrintWriter(stringWriter));
        System.out.println(stringWriter.toString());
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

    private boolean clauseListContainsPackage(List<ManifestValueClause> manifestValueClauses, String packageName) {
        for (ManifestValueClause manifestValueClause : manifestValueClauses) {
            if (manifestValueClause.getPaths().contains(packageName)) {
                return true;
            }
        }
        return false;
    }

    private boolean clauseListContainsPackageAndVersion(List<ManifestValueClause> manifestValueClauses, String packageName, String version) {
        for (ManifestValueClause manifestValueClause : manifestValueClauses) {
            if (manifestValueClause.getPaths().contains(packageName)) {
                if (manifestValueClause.getAttributes().get("version").equals(version)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void assertPackagesPresent(String message, List<ManifestValueClause> manifestValueClauses, String[] packageNames) {
        for (String packageName : packageNames) {
            String assertionMessage = MessageFormat.format(message, packageName);
            Assert.assertTrue(assertionMessage, clauseListContainsPackage(manifestValueClauses, packageName));
        }
    }
}
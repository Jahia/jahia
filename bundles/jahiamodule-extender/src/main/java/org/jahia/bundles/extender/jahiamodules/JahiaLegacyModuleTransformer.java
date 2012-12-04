package org.jahia.bundles.extender.jahiamodules;

import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * A Felix FileInstall ArtifactTransformer implementation that transforms Jahia's legacy WAR module
 * packaging into an OSGi compliant bundle.
 */
public class JahiaLegacyModuleTransformer implements ArtifactUrlTransformer {

    @Override
    public boolean canHandle(File artifact) {
        if (artifact == null) {
            return false;
        }
        if (artifact.getName().endsWith(".war")) {
            // we must now check the Manifest to see if it has Jahia-specific entries.
            try {
                JarFile jar = new JarFile(artifact);
                Attributes mainAttributes = jar.getManifest().getMainAttributes();
                if (mainAttributes.getValue("module-type") != null) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public URL transform(URL artifact) throws Exception {
        return new URL("jahiawar", null, artifact.toExternalForm());
    }
}

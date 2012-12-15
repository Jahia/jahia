package org.jahia.bundles.extender.jahiamodules;

import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.ops4j.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * A Felix FileInstall ArtifactTransformer implementation that transforms Jahia's legacy WAR module
 * packaging into an OSGi compliant bundle.
 */
public class JahiaLegacyModuleTransformer /* implements ArtifactUrlTransformer */ implements ArtifactTransformer {

    private static Logger logger = LoggerFactory.getLogger(Activator.class);

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

    /*
    @Override
    public URL transform(URL artifact) throws Exception {
        return new URL("jahiawar", null, artifact.toExternalForm());
    }
    */

    public File transform(File artifact, File tmpDir) {
   		try {
               URL war = new URL("jahiawar:" + artifact.toURL().toString());
               File outFile = new File(tmpDir, artifact.getName());
               StreamUtils.copyStream(war.openStream(), new FileOutputStream(outFile), true);
               return outFile;

           } catch (Exception e) {
   			logger.error("Failed to transform the WAR artifact into an OSGi bundle");
   			return null;
   		}
   	}

}

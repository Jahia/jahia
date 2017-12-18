/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import org.apache.felix.fileinstall.ArtifactTransformer;
import org.apache.tika.io.IOUtils;
import org.ops4j.io.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A Felix FileInstall ArtifactTransformer implementation that transforms Jahia's legacy WAR module
 * packaging into an OSGi compliant bundle.
 */
class JahiaLegacyModuleTransformer /* implements ArtifactUrlTransformer */ implements ArtifactTransformer {

    private static Logger logger = LoggerFactory.getLogger(JahiaLegacyModuleTransformer.class);

    @Override
    public boolean canHandle(File artifact) {
        if (artifact == null) {
            return false;
        }
        if (artifact.getName().endsWith(".war")) {
            // we must now check the Manifest to see if it has Jahia-specific entries.
            JarFile jar = null;
            try {
                jar = new JarFile(artifact);
                Manifest mf = jar.getManifest();
                if (mf != null) {
                    Attributes mainAttributes = mf.getMainAttributes();
                    if (mainAttributes.getValue("module-type") != null
                            || mainAttributes.getValue("root-folder") != null) {
                        return true;
                    }
                }
            } catch (IOException e) {
                return false;
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }
        }
        return false;
    }

    @Override
    public File transform(File artifact, File tmpDir) {
        InputStream is = null;
        FileOutputStream os = null;
        try {
            URL war = new URL("jahiawar:" + artifact.toURL().toString());
            File outFile = new File(tmpDir, artifact.getName());
            is = war.openStream();
            os = new FileOutputStream(outFile);
            StreamUtils.copyStream(is, os, false);
            return outFile;

        } catch (Exception e) {
            logger.error("Failed to transform the WAR artifact into an OSGi bundle", e);
            return null;
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

}

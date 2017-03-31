/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.transform;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.felix.fileinstall.ArtifactUrlTransformer;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;

/**
 * Bundle URL transformer that allows to use special DX handler.
 */
public class ModuleUrlTransformer implements ArtifactUrlTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ModuleUrlTransformer.class);

    @Override
    public boolean canHandle(File file) {

        if (file == null || !file.getName().endsWith(".jar")) {
            // we are not dealing with non-JAR files -> return
            return false;
        }

        try (JarFile jar = new JarFile(file)) {
            Manifest mf = jar.getManifest();
            if (mf != null) {
                Attributes attrs = mf.getMainAttributes();
                // it should be our module
                if (attrs.getValue(Constants.ATTR_NAME_JAHIA_MODULE_TYPE) != null) {
                    return true;
                } else {
                    String host = attrs.getValue(Constants.ATTR_NAME_FRAGMENT_HOST);
                    if (host != null) {
                        // it is a fragment bundle, check its host
                        Bundle hostBundle = BundleUtils.getBundleBySymbolicName(host, null);
                        // is its host an our module?
                        return hostBundle != null && hostBundle.getHeaders().get(Constants.ATTR_JAHIA_MODULE_TYPE) != null || ServicesRegistry.getInstance().getJahiaTemplateManagerService().getKnownFragmentHosts().contains(host);
                    }
                }
                return false;
            }
        } catch (IOException e) {
            // ignore
        }

        return false;
    }

    @Override
    public URL transform(URL artifact) throws Exception {
        if (Constants.URL_PROTOCOL_DX.equals(artifact.getProtocol())) {
            // no need to transform it
            return artifact;
        }

        URL transformed = null;
        long startTime = System.currentTimeMillis();
        logger.debug("Transforming artifact {}", artifact);
        try {
            PersistentBundle persistedBundle = ModuleUtils.persist(new UrlResource(artifact));
            transformed = new URL(persistedBundle.getLocation());
            if (logger.isDebugEnabled()) {
                logger.debug("Artifact {} has been successfully transformed into {} in {} ms",
                        new Object[] { artifact, transformed, System.currentTimeMillis() - startTime });
            }
        } catch (Exception e) {
            String msg = "Unable to transform artifact " + artifact + ". Cause: " + e.getMessage();
            logger.error(msg, e);
            throw new ModuleManagementException(msg, e);
        }

        return transformed != null ? transformed : artifact;
    }
}

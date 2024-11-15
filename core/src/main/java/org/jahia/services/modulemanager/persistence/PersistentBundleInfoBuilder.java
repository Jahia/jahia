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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.persistence;

import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_NAME;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_VERSION;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_GROUP_ID;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_IMPL_TITLE;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_IMPL_VERSION;
import static org.jahia.services.modulemanager.Constants.ATTR_NAME_BUNDLE_SYMBOLIC_NAME;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Responsible for parsing module bundle info from the provided resource.
 *
 * @author Ahmed Chaabni
 * @author Sergiy Shyrkov
 */
public final class PersistentBundleInfoBuilder {

    private static final Logger logger = LoggerFactory.getLogger(PersistentBundleInfoBuilder.class);

    /**
     * Parses the supplied resource and builds the information for the bundle.
     *
     * @param resource The bundle resource
     * @return The information about the supplied bundle
     * @throws IOException In case of resource read errors
     */
    public static PersistentBundle build(Resource resource) throws IOException {
        return build(resource, true, true);
    }

    /**
     * Parses the supplied resource and builds the information for the bundle.
     *
     * @param resource The bundle resource
     * @param calculateChecksum should we calculate the resource checksum?
     * @param checkTransformationRequired should we check if the module dependency capability headers has to be added
     * @return The information about the supplied bundle
     * @throws IOException In case of resource read errors
     */
    public static PersistentBundle build(Resource resource, boolean calculateChecksum, boolean checkTransformationRequired) throws IOException {

        // populate data from manifest
        String groupId = null;
        String symbolicName = null;
        String version = null;
        String displayName = null;
        try (JarInputStream jarIs = new JarInputStream(resource.getInputStream())) {
            Manifest mf = jarIs.getManifest();
            if (mf != null) {
                Attributes attrs = mf.getMainAttributes();
                groupId = attrs.getValue(ATTR_NAME_GROUP_ID);
                symbolicName = attrs.getValue(ATTR_NAME_BUNDLE_SYMBOLIC_NAME);
                version = StringUtils.defaultIfBlank(attrs.getValue(ATTR_NAME_BUNDLE_VERSION), attrs.getValue(ATTR_NAME_IMPL_VERSION));
                displayName = StringUtils.defaultIfBlank(attrs.getValue(ATTR_NAME_IMPL_TITLE), attrs.getValue(ATTR_NAME_BUNDLE_NAME));
            }
        }

        if (StringUtils.isBlank(symbolicName) || StringUtils.isBlank(version)) {
            // not a valid JAR or bundle information is missing -> we stop here
            logger.warn("Not a valid JAR or bundle information is missing for resource " + resource);
            return null;
        }

        PersistentBundle bundleInfo = new PersistentBundle(groupId, symbolicName, version);
        bundleInfo.setDisplayName(displayName);
        if (calculateChecksum) {
            bundleInfo.setChecksum(FileUtils.calculateDigest(resource.getInputStream()));
        }
        if (checkTransformationRequired) {
            bundleInfo.setTransformationRequired(isTransformationRequired(resource));
        }
        bundleInfo.setResource(resource);
        return bundleInfo;
    }

    private static boolean isTransformationRequired(Resource resource) throws IOException {
        try (JarInputStream is = new JarInputStream(new BufferedInputStream(resource.getInputStream()), false)) {
            return ModuleUtils.requiresTransformation(is.getManifest().getMainAttributes());
        }
    }

    private PersistentBundleInfoBuilder() {
        super();
    }
}

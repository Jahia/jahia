/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.provisioning.impl.operations;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeatureState;
import org.apache.karaf.features.FeaturesService;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Install/uninstall feature operation
 */
@Component(service = Operation.class, property = "type=installFeature")
public class InstallUninstallFeature implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(InstallUninstallFeature.class);
    public static final String INSTALL_FEATURE = "installFeature";
    public static final String UNINSTALL_FEATURE = "uninstallFeature";

    private FeaturesService featuresService;

    @Reference
    protected void setFeaturesService(FeaturesService featuresService) {
        this.featuresService = featuresService;
    }

    @Override
    public boolean canHandle(Map<String, Object> entry) {
        return entry.get(INSTALL_FEATURE) instanceof String || entry.get(UNINSTALL_FEATURE) instanceof String;
    }

    @Override
    public void perform(Map<String, Object> entry, ExecutionContext executionContext) {
        boolean installFeature = entry.containsKey(INSTALL_FEATURE);
        String featureId = (String) (installFeature ? entry.get(INSTALL_FEATURE) : entry.get(UNINSTALL_FEATURE));
        FeatureState featureState;
        Feature feature = null;
        try {
            feature = featuresService.getFeature(featureId);
        } catch (Exception e) {
            logger.error("Error retrieving feature {}. Ensure its repository is known to the OSGi platform.", featureId, e);
        }
        if (feature == null) {
            logger.error("Unable to locate feature {}. Ensure its repository is known to the OSGi platform.", featureId);
            return;
        }

        // get exact feature ID
        featureId = feature.getId();
        featureState = featuresService.getState(featureId);

        logger.debug("The state of the feature {} is {}", featureId, featureState);

        try {
            if (installFeature) {
                if (FeatureState.Started != featureState) {
                    logger.info("Installing feature {}", featureId);
                    featuresService.installFeature(featureId);
                }
            } else {
                if (FeatureState.Uninstalled != featureState) {
                    logger.info("Uninstalling feature {}", featureId);
                    featuresService.uninstallFeature(featureId);
                }
            }
        } catch (Exception e) {
            logger.error("Unable to install {} feature. Cause: {}", featureId, e.getMessage(), e);
        }
    }
}

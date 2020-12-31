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

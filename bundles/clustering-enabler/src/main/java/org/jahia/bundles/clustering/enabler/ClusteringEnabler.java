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
package org.jahia.bundles.clustering.enabler;

import java.util.Collections;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeatureState;
import org.apache.karaf.features.FeaturesService;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.FrameworkService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Activator for the DX clustering feature that checks if the cluster is activated or not and either starts dx-clustering feature or
 * uninstalls it. The check is done when we get an event about the full startup of the OSGi container.
 *
 * @author Sergiy Shyrkov
 */
@Component(name = "org.jahia.bundles.clustering.enabler", service = EventHandler.class, property = {
        Constants.SERVICE_PID + "=org.jahia.bundles.clustering.enabler",
        Constants.SERVICE_DESCRIPTION + "=DX clustring feature enabler",
        Constants.SERVICE_VENDOR + "=" + Jahia.VENDOR_NAME,
        EventConstants.EVENT_TOPIC + "=" + FrameworkService.EVENT_TOPIC_LIFECYCLE,
        EventConstants.EVENT_FILTER + "=(type=" + FrameworkService.EVENT_TYPE_OSGI_STARTED + ")" }, immediate = true)
public class ClusteringEnabler implements EventHandler {

    private static final String FEATURE_NAME = "dx-clustering";

    private static final Logger logger = LoggerFactory.getLogger(ClusteringEnabler.class);

    private FeaturesService featuresService;

    @Reference(service = FeaturesService.class)
    protected void bindFeaturesService(FeaturesService featuresService) {
        this.featuresService = featuresService;
    }

    @Override
    public void handleEvent(Event event) {
        long startTime = System.currentTimeMillis();
        logger.info("Clustering feature enabler is checking for the state of the feature " + FEATURE_NAME);

        if (featuresService == null) {
            throw new JahiaRuntimeException("Unable to lookup FeaturesService instance");
        }

        FeatureState clusteringState = FeatureState.Uninstalled;
        String featureId = FEATURE_NAME;
        Feature feature = null;
        try {
            feature = featuresService.getFeature(featureId);
        } catch (Exception e) {
            throw new JahiaRuntimeException("Error retrieving feature " + FEATURE_NAME
                    + ". Ensure its repository is known to the OSGi platform.", e);
        }
        if (feature == null) {
            throw new JahiaRuntimeException("Unable to locate feature " + FEATURE_NAME
                    + ". Ensure its repository is known to the OSGi platform.");
        }

        // get exact feature ID
        featureId = feature.getId();
        clusteringState = featuresService.getState(featureId);

        logger.info("The state of the feature {} is {}", featureId, clusteringState);

        boolean clusterActivated = SettingsBean.getInstance().isClusterActivated();

        try {
            if (clusterActivated) {
                if (FeatureState.Started != clusteringState) {
                    logger.info("Installing feature {}", featureId);
                    featuresService.installFeature(featureId);
                    FrameworkService.sendEvent(FrameworkService.EVENT_TOPIC_LIFECYCLE,
                            Collections.singletonMap("type", FrameworkService.EVENT_TYPE_CLUSTERING_FEATURE_INSTALLED),
                            true);
                }
            } else {
                if (FeatureState.Uninstalled != clusteringState) {
                    logger.info("Uninstalling feature {}", featureId);
                    featuresService.uninstallFeature(featureId);
                }
            }
        } catch (Exception e) {
            String msg = "Unable to " + (clusterActivated ? "enable" : "disable") + " clustering feature. Cause: "
                    + e.getMessage();
            logger.error(msg, e);
            // for now we do not fail the startup of the DX
            // throw new JahiaInitializationException(msg, e);
        }

        logger.info("Clustering feature enabler finished in {} ms", System.currentTimeMillis() - startTime);
    }
}

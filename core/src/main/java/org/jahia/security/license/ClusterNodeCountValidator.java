/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.security.license;

import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.ResourceMessage;

/**
 * User: Serge Huber
 * Date: 30 juin 2006
 * Time: 18:39:10
 * Copyright (C) Jahia Inc.
 */
public class ClusterNodeCountValidator extends AbstractValidator {

    public ClusterNodeCountValidator (String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals(String value) {
        int maxNodes = Integer.parseInt(value);

        // Check if the number of users is not exceeding the fixed limit
            int nbItems = ServicesRegistry.getInstance().getClusterService().getNbNodes();

            if (nbItems > maxNodes) {
                errorMessage = new ResourceMessage("org.jahia.security.license.ClusterNodeCountValidator.invalidClusterNodeCount.label", new Integer(nbItems), new Integer(maxNodes));
                return false;
            }
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean assertInRange(String fromValue, String toValue) {
        int minNodes = Integer.parseInt(fromValue);
        int maxNodes = Integer.parseInt(toValue);

        // Check if the number of users is not exceeding the fixed limit
            int nbItems = ServicesRegistry.getInstance().getClusterService().getNbNodes();

            if ((nbItems > maxNodes) || (nbItems < minNodes)) {
                errorMessage = new ResourceMessage("org.jahia.security.license.ClusterNodeCountValidator.clusterNodeCountNotInRange.label", new Integer(nbItems), new Integer(minNodes), new Integer(maxNodes));
                return false;
            }
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

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
package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.events.JahiaEvent;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.registries.ServicesRegistry;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 27 févr. 2006
 * Time: 15:11:59
 * To change this template use File | Settings | File Templates.
 */
public class JCRSiteListener extends JahiaEventListener {
    
    private static final transient Logger logger = Logger
            .getLogger(JCRSiteListener.class);
    
    public void siteAdded(JahiaEvent je) {
        try {
            ServicesRegistry.getInstance().getJCRStoreService().deployNewSite((JahiaSite) je.getObject(), je.getProcessingContext().getUser());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void siteDeleted(JahiaEvent je) {
    }

    @Override
    public void userAdded(JahiaEvent je) {
        try {
            ServicesRegistry.getInstance().getJCRStoreService().deployNewUser(((JahiaUser) je.getObject()).getUsername());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}

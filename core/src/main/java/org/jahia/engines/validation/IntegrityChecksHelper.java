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
package org.jahia.engines.validation;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Helper class for integrity checks.
 * 
 * @author Sergiy Shyrkov
 */
public final class IntegrityChecksHelper {

    public static boolean isAllowedToBypassLinkIntegrityChecks(JahiaUser user,
            JahiaSite site) {
        return !site.isURLIntegrityCheckEnabled()
                || user.isAdminMember(site.getID())
                || ServicesRegistry.getInstance().getJahiaACLManagerService()
                        .getSiteActionPermission("integrity.LinkIntegrity",
                                user, JahiaBaseACL.READ_RIGHTS, site.getID()) <= 0;
    }

    public static boolean isAllowedToBypassWaiChecks(JahiaUser user,
            JahiaSite site) {
        return !site.isWAIComplianceCheckEnabled()
                || user.isAdminMember(site.getID())
                || ServicesRegistry.getInstance().getJahiaACLManagerService()
                        .getSiteActionPermission("integrity.WaiCompliance",
                                user, JahiaBaseACL.READ_RIGHTS, site.getID()) <= 0;
    }

    /**
     * Initializes an instance of this class.
     */
    private IntegrityChecksHelper() {
        super();
    }
}

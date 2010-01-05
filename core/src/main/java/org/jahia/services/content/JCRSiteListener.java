/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.events.JahiaEvent;
import org.jahia.services.sites.JahiaSite;
import org.jahia.registries.ServicesRegistry;

import javax.jcr.RepositoryException;
import java.util.List;

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
//        try {
//            ServicesRegistry.getInstance().getJCRStoreService().deployNewSite((JahiaSite) je.getObject(), je.getProcessingContext().getUser());
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
    }

    public void siteDeleted(final JahiaEvent je) {
//        try {
//            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
//                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
//                    JCRNodeWrapper sites = session.getNode("/sites");
//                    if (!sites.isCheckedOut()) {
//                        sites.checkout();
//                    }
//                    JCRNodeWrapper site = sites.getNode(((JahiaSite) je.getObject()).getSiteKey());
//                    site.remove();
//                    session.save();
//                    return null;
//                }
//            });
//            // Now let's delete the live workspace site.
//            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
//                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
//                    JCRNodeWrapper sites = session.getNode("/sites");
//                    if (!sites.isCheckedOut()) {
//                        sites.checkout();
//                    }
//                    JCRNodeWrapper site = sites.getNode(((JahiaSite) je.getObject()).getSiteKey());
//                    site.remove();
//                    session.save();
//                    return null;
//                }
//            }, null, Constants.LIVE_WORKSPACE);
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
    }
}

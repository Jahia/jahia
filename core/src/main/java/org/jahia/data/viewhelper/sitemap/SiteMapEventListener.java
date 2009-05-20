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

package org.jahia.data.viewhelper.sitemap;

import java.util.Iterator;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.sitemap.JahiaSiteMapService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * <p>Title: Jahia View Helper</p>
 * <p>Description:
 * Listen if a Jahia page was added or if the user logout from CMS. In these
 * cases the appropriate actions are made in particular the invalidation of the
 * view helper.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 *
 * @author MAP
 * @version 1.0
 */
public class SiteMapEventListener extends JahiaEventListener {

    /**
     * logging
     */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SiteMapEventListener.class);

    private JahiaSiteMapService jahiaSiteMapService;

    public JahiaSiteMapService getJahiaSiteMapService() {
        return jahiaSiteMapService;
    }

    public void setJahiaSiteMapService(JahiaSiteMapService jahiaSiteMapService) {
        this.jahiaSiteMapService = jahiaSiteMapService;
    }

    /**
     * Reset engines that implement the site map view helper if a page was added.
     * Called when a page is added to Jahia CMS.
     *
     * @param je The Jahia event in question
     */
    public void pageAdded(JahiaEvent je) {
        jahiaSiteMapService.resetSiteMap();
    }

    /**
     * Reset engines that implement the site map view helper if a page was
     * removed.
     * Called when a field is removed to Jahia CMS.
     *
     * @param je The Jahia event in question
     */
    public void fieldDeleted(JahiaEvent je) {
        final JahiaField theField = (JahiaField) je.getObject();
        if (theField.getType() == FieldTypes.PAGE) {
            jahiaSiteMapService.resetSiteMap();
        }
    }

    public void fieldUpdated(JahiaEvent je) {
        fieldDeleted(je);
    }

    /**
     * Remove the user from the site map created in the engine.
     * Called when a user logout.
     *
     * @param je The Jahia event in question. C'est fou ce qu'on peut se marrer
     *           a repeter les memes aneries.
     */
    public void userLoggedOut(JahiaEvent je) {
        final JahiaUser theUser = (JahiaUser) je.getObject();
        jahiaSiteMapService.removeUserSiteMap(theUser.getUserKey());
    }

    public void containerListPropertiesSet(JahiaEvent je) {
        try {
            final JahiaContainerList theList = (JahiaContainerList) je.getObject();
            if (theList.size() > 0) {
                final JahiaContainer ctn = theList.getContainer(0);
                final Iterator fields = ctn.getFields();
                while (fields.hasNext()) {
                    final JahiaField f = (JahiaField) fields.next();
                    if (f.getType() == FieldTypes.PAGE) {
                        jahiaSiteMapService.resetSiteMap();
                        return;
                    }
                }
            }
        } catch (final JahiaException e) {
            logger.error(e, e);
        }
    }
}

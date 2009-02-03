/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

//

/*
 * ----- BEGIN LICENSE BLOCK -----
 * Version: JCSL 1.0
 *
 * The contents of this file are subject to the Jahia Community Source License
 * 1.0 or later (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.jahia.org/license
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the rights, obligations and limitations governing use of the contents
 * of the file. The Original and Upgraded Code is the Jahia CMS and Portal
 * Server. The developer of the Original and Upgraded Code is JAHIA Ltd. JAHIA
 * Ltd. owns the copyrights in the portions it created. All Rights Reserved.
 *
 * The Shared Modifications are Jahia View Helper.
 *
 * The Developer of the Shared Modifications is Jahia Solution S�rl.
 * Portions created by the Initial Developer are Copyright (C) 2002 by the
 * Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Oct 17 2002 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
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

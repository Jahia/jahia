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
 package org.jahia.views.engines.timebasedpublishing.actions;

import java.util.Properties;

import org.apache.struts.actions.DispatchAction;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.pages.ContentPage;


/**
 *
 * <p>Title: Time Based Publishing Dispatch Action</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Jahia</p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class TimeBasedPublishingAction extends DispatchAction {

    /**
     * Generate a valid url for this Struts Action
     *
     * @param contentPage
     * @param jParams
     * @param strutsAction
     * @param properties
     * @param params
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    public static String composeActionURL(ContentPage contentPage,
                                          ProcessingContext jParams,
                                          String strutsAction,
                                          Properties properties,
                                          String params)
    throws JahiaException {
        // Prepare this struts action Mapping url
        if ( properties == null ){
            properties = new Properties();
        }
        properties.put("pageid",String.valueOf(contentPage.getID()));
        String url = jParams.composeStrutsUrl(strutsAction,properties,params);
        return url;
    }

}

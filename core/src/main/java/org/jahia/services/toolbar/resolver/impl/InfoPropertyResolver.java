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
package org.jahia.services.toolbar.resolver.impl;

import java.text.SimpleDateFormat;
import java.util.Date;


import org.jahia.data.JahiaData;
import org.jahia.gui.GuiBean;
import org.jahia.security.license.LicenseManager;
import org.jahia.services.toolbar.resolver.PropertyResolver;
import org.jahia.utils.i18n.JahiaResourceBundle;



/**
 * User: jahia
 * Date: 18 avr. 2008
 * Time: 11:10:32
 */
public class InfoPropertyResolver implements PropertyResolver {
    public static final String ORG_JAHIA_TOOLBAR_ITEM_CONNECTED_USER = "user";
    public static final String ORG_JAHIA_TOOLBAR_ITEM_LICENSE_INFO = "license";

    public String getValue(JahiaData jData,String input) {
        if (jData == null) {
            return null;
        } else {
            if (input.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_CONNECTED_USER)) {
                GuiBean  guiBean = jData.getGui();
                if(guiBean != null){
                    return guiBean.drawUsername();
                }else{
                    return "";
                }
            } else if(input.startsWith(ORG_JAHIA_TOOLBAR_ITEM_LICENSE_INFO)) {
            	try{
            		  
            	    long warningdays = 10;
            	    try{
            	      if(input.indexOf("_") > 0)
            	      {
            	      	warningdays = Long.parseLong(input.substring(input.indexOf("_") + 1, input.length()));
            	      }
            	    }catch(NumberFormatException ex)
            	    {}
            	    long now = System.currentTimeMillis();
            	    long expirationTime = LicenseManager.getInstance().getJahiaExpirationDate();
            	    if (expirationTime > 0 && (now >= expirationTime || (expirationTime - now) <= 1000L * 60 * 60 * 24 * warningdays)) {
            	        return JahiaResourceBundle.getJahiaInternalResource(
                                "jahia.toolbar.license.expire", jData
                                        .getProcessingContext().getLocale())
                                + " "
                                + new SimpleDateFormat(
                                        JahiaResourceBundle
                                                .getJahiaInternalResource(
                                                        "jahia.toolbar.license.dateformat",
                                                        jData
                                                                .getProcessingContext()
                                                                .getLocale(),
                                                        "dd-MMM-yyyy"))
                                        .format(new Date(expirationTime));
            	    }
              } catch(Exception ex) {
                  // do nothing
              }
            }
        }
        return "";
    }

}

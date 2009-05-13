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
            	        return JahiaResourceBundle.getMessageResource(
                                "jahia.toolbar.license.expire", jData
                                        .getProcessingContext().getLocale())
                                + " "
                                + new SimpleDateFormat(
                                        JahiaResourceBundle
                                                .getMessageResource(
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

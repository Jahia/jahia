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

package org.jahia.services.toolbar.resolver.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import org.jahia.data.JahiaData;
import org.jahia.data.constants.JahiaConstants;
import org.jahia.gui.GuiBean;
import org.jahia.security.license.CommonDaysLeftValidator;
import org.jahia.security.license.LicenseConstants;
import org.jahia.security.license.LicenseManager;
import org.jahia.security.license.LicensePackage;
import org.jahia.security.license.Limit;
import org.jahia.services.toolbar.resolver.PropertyResolver;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.ResourceBundleMarker;



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
            } else if(input.equalsIgnoreCase(ORG_JAHIA_TOOLBAR_ITEM_LICENSE_INFO)) {
            	try{
              LicensePackage licensePackage = LicenseManager.getInstance().
              getLicensePackage(LicenseConstants.JAHIA_PRODUCT_NAME);

              Limit daysLeftLimit = licensePackage.
                      getLicense("org.jahia.actions.server.admin.sites.ManageSites").
                      getLimit("maxUsageDays");
              if (daysLeftLimit != null) {
              	CommonDaysLeftValidator daysLeftValidator = (CommonDaysLeftValidator)
                  	daysLeftLimit.getValidator();
              	int maxDays = Integer.parseInt(daysLeftLimit.getValueStr());
              	long expirationTime = daysLeftValidator.
                  	getCommonInstallDate().getTime() +
                  	1000L * 60L * 60L * 24L * maxDays;
              	Date expirationDate = new Date(expirationTime);            	
              	SimpleDateFormat sf = new SimpleDateFormat("dd-MMM-yyyy");
              	//JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", getLocale());
            	  return JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", jData.getProcessingContext().getLocale()) + " "  + sf.format(expirationDate);
              }
              else
              {
                Limit dateLimit = licensePackage.
                getLicense("org.jahia.actions.server.admin.sites.ManageSites").
                getLimit("date");
            	  if(dateLimit != null)
            	  {
            	  	String enddate = dateLimit.getValueStr();
            	  	int year = Integer.parseInt(enddate.substring(0, 4));
            	  	int month = Integer.parseInt(enddate.substring(5, 7));
            	  	int day = Integer.parseInt(enddate.substring(8, 10));
            	  	Calendar cal = new GregorianCalendar(year, month-1, day);
            	  	Calendar cal1 = new GregorianCalendar(year, month-1, day);
            	  	cal1.add(Calendar.DATE, -10);
            	  	if(cal1.getTime().before(new Date(System.currentTimeMillis())))
            	  	{
            	  		SimpleDateFormat sf1 = new SimpleDateFormat("dd-MMM-yyyy");
            	  		return JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", jData.getProcessingContext().getLocale()) + " " + sf1.format(cal.getTime());
            	  	}
            	  }
              }
              }catch(Exception ex)
              {}
            }
        }
        return "";
    }

}

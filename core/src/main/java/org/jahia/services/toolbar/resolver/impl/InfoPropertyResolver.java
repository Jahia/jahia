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

              Limit daysLeftLimit = licensePackage.getLicense(LicenseConstants.CORE_COMPONENT).
                      getLimit("maxUsageDays");
              if (daysLeftLimit != null) {
              	CommonDaysLeftValidator daysLeftValidator = (CommonDaysLeftValidator)
                  	daysLeftLimit.getValidator();
              	int maxDays = Integer.parseInt(daysLeftLimit.getValueStr());
              	long expirationTime = daysLeftValidator.
                  	getCommonInstallDate().getTime() +
                  	1000L * 60L * 60L * 24L * maxDays;
              	Date expirationDate = new Date(expirationTime);     
        	  		String format = JahiaResourceBundle.getMessageResource("jahia.toolbar.license.dateformat", jData.getProcessingContext().getLocale());
        	  		if(format == null || format.length() < 5)
        	  			format = "dd-MMM-yyyy";
        	  		SimpleDateFormat sf = new SimpleDateFormat(format);              	
              	//JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", getLocale());
            	  return JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", jData.getProcessingContext().getLocale()) + " "  + sf.format(expirationDate);
              }
              else
              {
                Limit dateLimit = licensePackage.getLicense(LicenseConstants.CORE_COMPONENT).
                			getLimit("date");
            	  if(dateLimit != null)
            	  {
            	  	String enddate = dateLimit.getValueStr();
            	  	int year = Integer.parseInt(enddate.substring(6, 10));
            	  	int month = Integer.parseInt(enddate.substring(3, 5));
            	  	int day = Integer.parseInt(enddate.substring(0, 2));
            	  	Calendar cal = new GregorianCalendar(year, month-1, day);
            	  	Calendar cal1 = new GregorianCalendar(year, month-1, day);
            	  	cal1.add(Calendar.DATE, -10);
            	  	if(cal1.getTime().before(new Date(System.currentTimeMillis())))
            	  	{
            	  		String format = JahiaResourceBundle.getMessageResource("jahia.toolbar.license.dateformat", jData.getProcessingContext().getLocale());
            	  		if(format == null || format.length() < 5)
            	  			format = "dd-MMM-yyyy";
            	  		SimpleDateFormat sf1 = new SimpleDateFormat(format);
            	  		return JahiaResourceBundle.getMessageResource("jahia.toolbar.license.expire", jData.getProcessingContext().getLocale()) + "&nbsp;<b>" + sf1.format(cal.getTime()) + "</b>";
            	  	}
            	  }
              }
              }catch(Exception ex)
              {
              }
            }
        }
        return "";
    }

}

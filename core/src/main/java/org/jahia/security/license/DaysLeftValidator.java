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

 package org.jahia.security.license;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jahia.resourcebundle.ResourceMessage;
import org.jahia.settings.SettingsBean;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class DaysLeftValidator extends AbstractValidator {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(DaysLeftValidator.class);

    static private final String DAYSLEFT_FILE = "jahia.png";

    private SettingsBean settingsBean;
    private Map componentDaysLeft = new HashMap();
    private Date componentInstallDate = null;
    private boolean fileLoaded = false;

    public DaysLeftValidator (String name, String value, License license) {
        super(name, value, license);
    }

    public boolean assertEquals (String value) {
        checkSettings();
        int totalAllowedDays = Integer.parseInt(value);
        Date nowDate = new Date();
        long nowDateLong = nowDate.getTime();
        long installDateLong = componentInstallDate.getTime();
        long maxDateLong = installDateLong + 1000L*60L*60L*24L*totalAllowedDays;
        if (nowDateLong > maxDateLong) {
            errorMessage = new ResourceMessage("org.jahia.security.license.DaysLeftValidator.daysLeftExpired.label", new Integer(totalAllowedDays));
            return false;
        } else {
            return true;
        }
    }

    public boolean assertInRange (String fromValue, String toValue) {
        checkSettings();
        int minAllowedDays = Integer.parseInt(fromValue);
        int maxAllowedDays = Integer.parseInt(toValue);
        Date nowDate = new Date();
        long nowDateLong = nowDate.getTime();
        long installDateLong = componentInstallDate.getTime();
        long minDateLong = installDateLong + 1000L*60L*60L*24L*minAllowedDays;
        long maxDateLong = installDateLong + 1000L*60L*60L*24L*maxAllowedDays;
        if ((nowDateLong > maxDateLong) || (nowDateLong < minDateLong)) {
            errorMessage = new ResourceMessage("org.jahia.security.license.DaysLeftValidator.daysLeftNotInRange.label", new Integer(minAllowedDays), new Integer(maxAllowedDays));
            return false;
        } else {
            return true;
        }
    }

    private void checkSettings() {
        if (settingsBean == null) {
            settingsBean = org.jahia.settings.SettingsBean.getInstance();
        }
        if (fileLoaded) {
            return;
        }
        String fullFilePath = settingsBean.getJahiaEtcDiskPath() +
            File.separator + "config" +
            File.separator + DAYSLEFT_FILE;
        File daysLeftFile = new File(fullFilePath);
        if (daysLeftFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(daysLeftFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                try {
                    while (true) {
                        String componentName = (String) objectIn.readObject();
                        long installDateLong = objectIn.readLong();
                        componentDaysLeft.put(componentName, new Date(installDateLong));
                    }
                } catch (EOFException eofe) {
                    // we are going to assume this error is due to reaching the end of
                    // the file.
                }
                fileIn.close();
            } catch (IOException ioe) {
                logger.error("Error while loading days left file [" +
                             fullFilePath + "]", ioe);
            } catch (ClassNotFoundException cnfe) {
                logger.error("Error while loading days left file [" +
                             fullFilePath + "]", cnfe);
            }
        }
        // do we have an entry for our component name ? If not let's add one
        // with the current date.
        if (!componentDaysLeft.containsKey(license.getComponentName())) {
            componentDaysLeft.put(license.getComponentName(), new Date());
            try {
                // now let's update the data in the file if needed
                FileOutputStream fileOut = new FileOutputStream(daysLeftFile);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                Iterator componentNameIter = componentDaysLeft.keySet().iterator();
                while (componentNameIter.hasNext()) {
                    String curComponentName = (String) componentNameIter.next();
                    Date daysLeftDate = (Date) componentDaysLeft.get(curComponentName);
                    objectOut.writeObject(curComponentName);
                    objectOut.writeLong(daysLeftDate.getTime());
                }
                objectOut.flush();
                objectOut.close();
            } catch (IOException ioe) {
                logger.error("Error while updating days left file [" +
                             fullFilePath + "]", ioe);
            }
        }

        componentInstallDate = (Date) componentDaysLeft.get(license.getComponentName());

        fileLoaded=true;
    }

    public Date getComponentInstallDate() {
        return componentInstallDate;
    }

}
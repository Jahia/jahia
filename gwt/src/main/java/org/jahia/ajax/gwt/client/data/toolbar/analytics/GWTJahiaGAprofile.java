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
package org.jahia.ajax.gwt.client.data.toolbar.analytics;
import java.io.Serializable;
/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 21 janv. 2009
 * Time: 09:52:23
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaGAprofile implements Serializable{

    String jahiaGAprofile = "";
    String gaUserAccount = "";
    String gaProfile = "";
    String gaLogin = "";
    String trackedUrls = "";
    Boolean trackingEnabled = false;

    public GWTJahiaGAprofile(){
        
    }

    public String getJahiaGAprofile() {
        return jahiaGAprofile;
    }

    public void setJahiaGAprofile(String jahiaGAprofile) {
        this.jahiaGAprofile = jahiaGAprofile;
    }

    public String getGaUserAccount() {
        return gaUserAccount;
    }

    public void setGaUserAccount(String gaUserAccount) {
        this.gaUserAccount = gaUserAccount;
    }

    public String getGaProfile() {
        return gaProfile;
    }

    public void setGaProfile(String gaProfile) {
        this.gaProfile = gaProfile;
    }

    public String getGaLogin() {
        return gaLogin;
    }

    public void setGaLogin(String gaLogin) {
        this.gaLogin = gaLogin;
    }

    public String getTrackedUrls() {
        return trackedUrls;
    }

    public void setTrackedUrls(String trackedUrls) {
        this.trackedUrls = trackedUrls;
    }

    public Boolean getTrackingEnabled() {
        return trackingEnabled;
    }

    public void setTrackingEnabled(Boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
    public void dispatchSetter(String property,String value)
    {          /*
       if(property.endsWith(value)){
           setGaProfile(value);
       }else */if(property.endsWith("gaUserAccount")){
           setGaUserAccount(value);
       }else if(property.endsWith("gaProfile")){
           setGaProfile(value);
       }else if(property.endsWith("gaLogin")){
           setGaLogin(value);
       }else if(property.endsWith("trackedUrls")){
           setTrackedUrls(value);
       }else if(property.endsWith("trackingEnabled")){
           setTrackingEnabled(Boolean.valueOf(value));
       }
    }
}

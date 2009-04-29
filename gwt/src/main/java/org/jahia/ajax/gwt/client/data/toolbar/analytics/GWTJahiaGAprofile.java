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

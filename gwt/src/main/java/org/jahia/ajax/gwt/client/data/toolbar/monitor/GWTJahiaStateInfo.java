/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.toolbar.monitor;

import java.io.Serializable;

/**
 * User: jahia
 * Date: 28 avr. 2008
 * Time: 18:52:45
 */
public class GWTJahiaStateInfo implements Serializable {
    private String iconStyle;
    private String textStyle;
    private String text;
    private String alertMessage;
    private String refreshMessage;
    private int displayTime = 15000 ;
    private long lastViewTime;
    private boolean needRefresh;
    private boolean currentUserJobEnded;

    private GWTJahiaProcessJobInfo gwtProcessJobInfo;

    //flag that allows to definy what has to be computed
    private boolean checkProcessInfo = true;

    public GWTJahiaStateInfo() {

    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getRefreshMessage() {
        return refreshMessage;
    }

    public void setRefreshMessage(String refreshMessage) {
        this.refreshMessage = refreshMessage;
    }

    public GWTJahiaProcessJobInfo getGwtProcessJobInfo() {
        return gwtProcessJobInfo;
    }

    public void setGwtProcessJobInfo(GWTJahiaProcessJobInfo gwtProcessJobInfo) {
        this.gwtProcessJobInfo = gwtProcessJobInfo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIconStyle() {
        return iconStyle;
    }

    public void setIconStyle(String iconStyle) {
        this.iconStyle = iconStyle;
    }

    public String getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(String textStyle) {
        this.textStyle = textStyle;
    }

    public long getLastViewTime() {
        return lastViewTime;
    }

    public void setLastViewTime(long lastViewTime) {
        this.lastViewTime = lastViewTime;
    }

    public boolean isCheckProcessInfo() {
        return checkProcessInfo;
    }

    public void setCheckProcessInfo(boolean checkProcessInfo) {
        this.checkProcessInfo = checkProcessInfo;
    }

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }

    public boolean isCurrentUserJobEnded() {
        return currentUserJobEnded;
    }

    public void setCurrentUserJobEnded(boolean currentUserJobEnded) {
        this.currentUserJobEnded = currentUserJobEnded;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(int displayTime) {
        this.displayTime = displayTime;
    }
}

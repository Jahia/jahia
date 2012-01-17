/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

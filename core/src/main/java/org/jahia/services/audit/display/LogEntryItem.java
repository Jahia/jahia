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

package org.jahia.services.audit.display;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.services.audit.LogsBasedQueryConstant;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 d�c. 2007
 * Time: 15:35:34
 * To change this template use File | Settings | File Templates.
 */
public class LogEntryItem {

    private int objectID;
    private int objectType;
    private long logTime;
    private Date logTimeAsDate;
    private String username;
    private ObjectKey objectKey;

    public LogEntryItem(int objectID, int objectType, long logTime, String username) {
        this.objectID = objectID;
        this.objectType = objectType;
        this.logTime = logTime;
        this.username = username;
    }

    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public int getObjectType() {
        return objectType;
    }

    public void setObjectType(int objectType) {
        this.objectType = objectType;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
        this.logTimeAsDate = null;
    }

    public Date getLogTimeAsDate(){
        if ( this.logTimeAsDate == null ){
            if ( this.logTime>0 ){
                this.logTimeAsDate = new Date(this.logTime);
            }
        }
        return this.logTimeAsDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ObjectKey getObjectKey() {
        if ( objectKey == null ){
            String contentType = null;
            if ( this.objectType == LogsBasedQueryConstant.CONTAINER_TYPE){
                contentType = ContentContainerKey.CONTAINER_TYPE;
            } else if ( this.objectType == LogsBasedQueryConstant.PAGE_TYPE){
                contentType = ContentPageKey.PAGE_TYPE;
            }
            if ( contentType != null ){
                try {
                    objectKey = ObjectKey.getInstance(contentType + "_" + objectID);
                } catch ( Exception t ){
                }
            }
        }
        return objectKey;
    }

    public void setObjectKey(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

}

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

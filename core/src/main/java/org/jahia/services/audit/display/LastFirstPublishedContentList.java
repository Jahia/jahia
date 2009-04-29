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

import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 d�c. 2007
 * Time: 15:49:54
 * To change this template use File | Settings | File Templates.
 */
public class LastFirstPublishedContentList<E extends LogEntryItem> extends LogsResultList<E> {

    private static final long serialVersionUID = -6180381612868046104L;
    
    private int count;
    private Set<ObjectKey> objectKeys;

    public void buildList(List<Object[]> logs, ProcessingContext context,
            int timeBasedPublishingLoadFlag, boolean checkACL)
            throws JahiaException {

        this.clear();
        objectKeys = new HashSet<ObjectKey>();
        if ( logs == null || logs.isEmpty() ){
            return;
        }

        Iterator<Object[]> iterator = logs.iterator();
        E log = null;
        Object[] row = null;
        count = 0;
        while ( iterator.hasNext() && (this.getMaxSize() == -1 || count <this.getMaxSize()) ){
            row = (Object[])iterator.next();
            log = getLog(row, context, timeBasedPublishingLoadFlag, checkACL);
            addLog(log,context);
        }
    }

    public E getLog(Object[] rowData, ProcessingContext context, int timeBasedPublishingLoadFlag
            , boolean checkACL ){
        int objectID;
        int objectType;
        String username;
        long time;
        objectID = ((Integer)rowData[0]).intValue();
        objectType = ((Integer)rowData[1]).intValue();
        username = (String)rowData[2];
        time = ((Long)rowData[3]).longValue();
        try {
            if (!this.checkAccess(objectID,objectType,context,timeBasedPublishingLoadFlag,checkACL)){
                return null;
            }
        } catch ( Exception t ){
            return null;
        }
        return (E)new LogEntryItem(objectID,objectType,time,username);
    }

    public void addLog(E log, ProcessingContext context){
        if ( log != null ){
            if ( !this.getUniqueContentObject() ){
                this.add(log);
                count++;
            } else {
                ObjectKey objKey = log.getObjectKey();
                if (!objectKeys.contains(objKey)){
                    objectKeys.add(objKey);
                    this.add(log);
                    count++;
                }
            }
        }
    }
}
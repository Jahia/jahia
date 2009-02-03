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
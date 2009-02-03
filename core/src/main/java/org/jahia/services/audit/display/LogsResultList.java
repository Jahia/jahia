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

import java.util.ArrayList;
import java.util.List;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.TimeBasedPublishingState;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.audit.LogsBasedQueryConstant;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 6 d�c. 2007
 * Time: 15:49:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class LogsResultList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 1881004221824195450L;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(LogsResultList.class);

    private int maxSize = Integer.MAX_VALUE;
    private boolean uniqueContentObject;

    /**
     *
     * @param rawLogsResultSet
     * @param context
     * @param timeBasedPublishingLoadFlag
     * @param checkACL
     * @throws JahiaException
     */
    public abstract void buildList(List<Object[]> rawLogsResultSet,
                                   ProcessingContext context,
                                   int timeBasedPublishingLoadFlag, boolean checkACL) throws JahiaException;

    public void setMaxSize(int maxSize){
        this.maxSize = maxSize;
    }

    public int getMaxSize(){
        return maxSize;
    }

    public boolean getUniqueContentObject() {
        return uniqueContentObject;
    }

    /**
     * if true, only the first entrylog for each ContentObject will be returned
     * It is used to filter multiple log entries with same content object but with differents users
     *
     * @param uniqueContentObject
     */
    public void setUniqueContentObject(boolean uniqueContentObject) {
        this.uniqueContentObject = uniqueContentObject;
    }

    protected boolean checkAccess(int objectID, int objectType, ProcessingContext context,
                                  int timeBasedPublishingLoadFlag, boolean checkACL) throws JahiaException {

        if (!checkACL && ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.ALL_STATES_LOAD_FLAG)!=0)){
            return true;
        }
        ContentObject contentObject = null;
        if (checkACL){
            try {
                contentObject = ContentObject.getContentObjectInstance(
                        ObjectKey.getInstance(getObjectType(objectType)+"_"+objectID));
                if (!contentObject.checkReadAccess(context.getUser())){
                    return false;
                }
            } catch (Exception t){
                logger.debug("Exception occured while checking right access",t);
                return false;
            }
        }
        if ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.ALL_STATES_LOAD_FLAG)!=0){
            return true;
        } else {
            if (contentObject == null){
                try {
                    contentObject = ContentObject.getContentObjectInstance(
                            ObjectKey.getInstance(getObjectType(objectType)+"_"+objectID));
                    if (!contentObject.checkReadAccess(context.getUser())){
                        return false;
                    }
                } catch (Exception t){
                    logger.debug("Exception occured while checking right access",t);
                    return false;
                }
            }

            final JahiaObjectManager jahiaObjectManager = ServicesRegistry.getInstance()
                    .getTimeBasedPublishingService().getJahiaObjectMgr();
            final JahiaObjectDelegate jahiaObjectDelegate =
                    jahiaObjectManager.getJahiaObjectDelegate(contentObject.getObjectKey());
            if ( jahiaObjectDelegate == null ){
                return false;
            }
            if ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.IS_VALID_STATE_LOAD_FLAG)!=0){
                return contentObject.isAvailable();
            } else if ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.EXPIRED_OR_NOT_VALID_STATE_LOAD_FLAG)!=0){
                return !(contentObject.isAvailable());
            } else if ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.EXPIRED_STATE_LOAD_FLAG)!=0){
                return (jahiaObjectDelegate.getTimeBPState().intValue()==TimeBasedPublishingState.EXPIRED_STATE);
            } else if ((timeBasedPublishingLoadFlag & TimeBasedPublishingState.NOT_VALID_STATE_LOAD_FLAG) == 1){
                return (jahiaObjectDelegate.getTimeBPState().intValue()==TimeBasedPublishingState.IS_VALID_STATE);
            }
        }
        return false;
    }

    protected String getObjectType(int objectType){
        String objectTypeStr = null;
        if ( objectType == LogsBasedQueryConstant.CONTAINER_TYPE ){
            objectTypeStr = ContentContainerKey.CONTAINER_TYPE;
        } else if ( objectType == LogsBasedQueryConstant.PAGE_TYPE ){
            objectTypeStr = ContentPageKey.PAGE_TYPE;
        } else if ( objectType == LogsBasedQueryConstant.CONTAINER_LIST_TYPE ){
            objectTypeStr = ContentContainerListKey.CONTAINERLIST_TYPE;
        } else if ( objectType == LogsBasedQueryConstant.FIELD_TYPE ){
            objectTypeStr = ContentFieldKey.FIELD_TYPE;
        }
        return objectTypeStr;
    }
}

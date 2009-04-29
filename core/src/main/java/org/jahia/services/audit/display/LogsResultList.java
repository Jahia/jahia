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

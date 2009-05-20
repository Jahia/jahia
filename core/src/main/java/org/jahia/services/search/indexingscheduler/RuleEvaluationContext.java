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
package org.jahia.services.search.indexingscheduler;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Holds some common variable used for rule evaluation
 *
 * User: hollis
 * Date: 23 ao�t 2007
 * Time: 15:36:31
 * To change this template use File | Settings | File Templates.
 */
public class RuleEvaluationContext {

    private ObjectKey objectKey;
    private ContentObject contentObject;
    private String actionPerformed;
    private boolean manualActionPerformed;
    private ProcessingContext context;
    private JahiaUser user;
    private boolean actionPerformedEndReadched;

    /**
     *
     * @param objectKey
     * @param contentObject
     * @param context
     */
    public RuleEvaluationContext(ObjectKey objectKey, ContentObject contentObject,
                                 ProcessingContext context) {
        this.objectKey = objectKey;
        this.contentObject = contentObject;
        this.context = context;
        if ( this.context != null ){
            this.user = this.context.getUser();
        }
    }

    /**
     *
     * @param objectKey
     * @param contentObject
     * @param context
     * @param user
     */
    public RuleEvaluationContext(ObjectKey objectKey, ContentObject contentObject,
                                 ProcessingContext context, JahiaUser user) {
        this(objectKey,contentObject,context);
        if ( user != null ){
            this.user = user;
        }
    }

    /**
     *
     * @param objectKey
     * @param contentObject
     * @param context
     * @param user
     * @param actionPerformed
     * @param actionPerformedReached
     * @param manualActionPerformed
     */
    public RuleEvaluationContext(ObjectKey objectKey, ContentObject contentObject,
            ProcessingContext context,
            JahiaUser user,
            String actionPerformed, boolean actionPerformedReached, boolean manualActionPerformed) {
        this(objectKey,contentObject,context,user);
        this.actionPerformed = actionPerformed;
        this.actionPerformedEndReadched = actionPerformedReached;
        this.manualActionPerformed = manualActionPerformed;
    }

    public ObjectKey getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    public ContentObject getContentObject() {
        return contentObject;
    }

    public void setContentObject(ContentObject contentObject) {
        this.contentObject = contentObject;
    }

    public String getActionPerformed() {
        return actionPerformed;
    }

    public void setActionPerformed(String actionPerformed) {
        this.actionPerformed = actionPerformed;
    }

    public boolean isActionPerformedEndReadched() {
        return actionPerformedEndReadched;
    }

    public void setActionPerformedEndReadched(boolean actionPerformedEndReadched) {
        this.actionPerformedEndReadched = actionPerformedEndReadched;
    }

    public boolean isManualActionPerformed() {
        return manualActionPerformed;
    }

    public void setManualActionPerformed(boolean manualActionPerformed) {
        this.manualActionPerformed = manualActionPerformed;
    }

    public JahiaUser getUser() {
        return user;
    }

    public void setUser(JahiaUser user) {
        this.user = user;
    }

    public ProcessingContext getContext() {
        return context;
    }

    public void setContext(ProcessingContext context) {
        this.context = context;
    }

}

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

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
package org.jahia.ajax.gwt.client.data.actionmenu;

import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;

import java.io.Serializable;

/**
 * Global wrapper for various states to retrieve with an unique AJAX call.
 * Attributes can be null, meaning no state.
 *
 * @author rfelden
 * @version 29 f�vr. 2008 - 12:46:07
 */
public class GWTJahiaGlobalState implements Serializable {

    private GWTJahiaAclDiffState aclDiffState ;
    private GWTJahiaTimebasedPublishingState timebasedPublishingState ;
    private GWTJahiaWorkflowState workflowState ;
    private GWTJahiaIntegrityState integrityState;

    public GWTJahiaGlobalState() {}

    public GWTJahiaGlobalState(GWTJahiaAclDiffState acl, GWTJahiaTimebasedPublishingState tbp, GWTJahiaWorkflowState wf) {
        aclDiffState = acl ;
        timebasedPublishingState = tbp ;
        workflowState = wf ;
    }

    public GWTJahiaAclDiffState getAclDiffState() {
        return aclDiffState;
    }

    public void setAclDiffState(GWTJahiaAclDiffState aclDiffState) {
        this.aclDiffState = aclDiffState;
    }

    public GWTJahiaTimebasedPublishingState getTimebasedPublishingState() {
        return timebasedPublishingState;
    }

    public void setTimebasedPublishingState(GWTJahiaTimebasedPublishingState timebasedPublishingState) {
        this.timebasedPublishingState = timebasedPublishingState;
    }

    public GWTJahiaWorkflowState getWorkflowState() {
        return workflowState;
    }

    public void setWorkflowState(GWTJahiaWorkflowState workflowState) {
        this.workflowState = workflowState;
    }

    public GWTJahiaIntegrityState getIntegrityState() {
        return integrityState;
    }

    public void setIntegrityState(GWTJahiaIntegrityState integrityState) {
        this.integrityState = integrityState;
    }

    // methods to check existence of various states
    public boolean hasAclDiffState() {
        return aclDiffState != null ;
    }

    public boolean hasTimebasedPublishingState() {
        return timebasedPublishingState != null ;
    }

    public boolean hasWorkflowState() {
        return workflowState != null ;
    }

    public boolean hasIntegrityState() {
        return integrityState != null;
    }
}

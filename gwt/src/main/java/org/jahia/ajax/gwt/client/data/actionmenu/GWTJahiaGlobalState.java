/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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

    public GWTJahiaGlobalState() {
        super();
    }

    public GWTJahiaGlobalState(GWTJahiaAclDiffState acl, GWTJahiaTimebasedPublishingState tbp, GWTJahiaWorkflowState wf) {
        this();
        aclDiffState = acl ;
        timebasedPublishingState = tbp ;
        workflowState = wf ;
    }

    public GWTJahiaGlobalState(GWTJahiaAclDiffState acl, GWTJahiaTimebasedPublishingState tbp, GWTJahiaWorkflowState wf, GWTJahiaIntegrityState integrityState) {
        this(acl, tbp, wf);
        this.integrityState = integrityState;
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

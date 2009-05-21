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
package org.jahia.services.version;

import org.jahia.content.ObjectKey;
import org.jahia.content.ContentContainerKey;

public class ContainerRevisionEntrySet extends RevisionEntrySet {

    private RevisionEntry overallRevision = null;

    public static final int CONTAINER_UPDATE = 1;
    public static final int FIELD_UPDATE = 2;
    public static final int METADATA_UPDATE = 4;
    public static final int FIELD_AND_METADATA_UPDATE = FIELD_UPDATE | METADATA_UPDATE;
    public static final int ALL_UPDATE = CONTAINER_UPDATE | FIELD_AND_METADATA_UPDATE;

    private int updateType = ALL_UPDATE;

    public ContainerRevisionEntrySet(){
        super();
    }

    /**
     *
     * @param versionID
     * @param objectKey
     */
    public ContainerRevisionEntrySet(int versionID, ObjectKey objectKey){
        super(versionID,objectKey);
    }

    public void addRevision(RevisionEntry revisionEntry){
        // should we change the overall workflowState;
        if ( revisionEntry == null ){
            return;
        }

        int revWorkflowState = revisionEntry.getWorkflowState();
        if ( revisionEntry.getObjectKey().getType()
             .equals(ContentContainerKey.CONTAINER_TYPE) ){
            if ( this.updateType == ContainerRevisionEntrySet.FIELD_AND_METADATA_UPDATE ){
               this.updateType = ContainerRevisionEntrySet.ALL_UPDATE;
            } else if ( this.updateType != ContainerRevisionEntrySet.FIELD_AND_METADATA_UPDATE ){
                this.updateType = ContainerRevisionEntrySet.CONTAINER_UPDATE;
            }
        } else {
            if ( this.updateType == ContainerRevisionEntrySet.CONTAINER_UPDATE ){
               this.updateType = ContainerRevisionEntrySet.ALL_UPDATE;
            } else if ( this.updateType != ContainerRevisionEntrySet.ALL_UPDATE ){
                this.updateType = ContainerRevisionEntrySet.CONTAINER_UPDATE;
            }
        }
        if ( this.overallRevision == null ){
            this.overallRevision = revisionEntry;
        } else if ( revWorkflowState ==
             ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
            if (this.overallRevision.getWorkflowState() == ContentObjectEntryState.WORKFLOW_STATE_VERSIONED){
                this.overallRevision = revisionEntry;
            }
        } else if ( revWorkflowState !=
                    ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
            this.overallRevision = revisionEntry;
        } else if ( this.getWorkflowState() ==
                    ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
            this.overallRevision = revisionEntry;
        }
        super.addRevision(revisionEntry);
    }

    public int getWorkflowState(){
        if ( this.overallRevision == null ){
            return ContentObjectEntryState.WORKFLOW_STATE_VERSIONED;
        }
        return this.overallRevision.getWorkflowState();
    }

    public int getRevisionType(){
        return this.updateType;
    }

    /**
     * @todo implement multilanguage with res bundle
     *
     * @param languageCode
     * @return
     */
    public String getDescription(String languageCode){
        if ( this.updateType == ContainerRevisionEntrySet.CONTAINER_UPDATE ){
            return "Container Attributes Update";
        } else if ( this.updateType == ContainerRevisionEntrySet.FIELD_AND_METADATA_UPDATE ) {
            return "Field and Metadata Update";
        } else if ( this.updateType == ContainerRevisionEntrySet.ALL_UPDATE ) {
            return "Container Attributes, Contents or Metadata Update";
        }
        return "";
    }
}

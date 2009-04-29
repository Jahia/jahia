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

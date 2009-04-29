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

import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;

public class PageRevisionEntrySet extends RevisionEntrySet {

    private RevisionEntry overallRevision = null;
    private RevisionEntry notPageOverallRevision = null;

    public static final int PAGE_ATTRIBUTES_UPDATE = 1;
    public static final int PAGE_CONTENT_UPDATE = 2;
    public static final int PAGE_ATTRIBUTE_AND_CONTENT_UPDATE = 3;
    public static final int PAGE_DELETED = 4;

    private int updateType = 0;
    private boolean pageDeleted = false;
    private boolean onlyStaging = true;

    public PageRevisionEntrySet(){
        super();
    }

    /**
     *
     * @param versionID
     * @param objectKey
     */
    public PageRevisionEntrySet(int versionID, ObjectKey objectKey){
        super(versionID,objectKey);
    }

    public void addRevision(RevisionEntry revisionEntry){
        // should we change the overall workflowState;
        if ( revisionEntry == null ){
            return;
        }

        int revWorkflowState = revisionEntry.getWorkflowState();
        if ( revisionEntry.getObjectKey().getType()
             .equals(ContentPageKey.PAGE_TYPE) ){
            if ( this.overallRevision == null ){
                this.overallRevision = revisionEntry;
            } else if ( revWorkflowState ==
                 ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                this.overallRevision = revisionEntry;
            } else if ( revWorkflowState !=
                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                this.overallRevision = revisionEntry;
            } else if ( this.getWorkflowState() ==
                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                this.overallRevision = revisionEntry;
            }
            if ( this.updateType == PageRevisionEntrySet.PAGE_CONTENT_UPDATE ){
               this.updateType = PageRevisionEntrySet.PAGE_ATTRIBUTE_AND_CONTENT_UPDATE;
            } else if ( this.updateType != PageRevisionEntrySet.PAGE_ATTRIBUTE_AND_CONTENT_UPDATE ){
                this.updateType = PageRevisionEntrySet.PAGE_ATTRIBUTES_UPDATE;
            }
            if ( revisionEntry.getWorkflowState()
                 == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                this.pageDeleted = true;
            }

        } else {
            if ( this.updateType == PageRevisionEntrySet.PAGE_ATTRIBUTES_UPDATE ){
               this.updateType = PageRevisionEntrySet.PAGE_ATTRIBUTE_AND_CONTENT_UPDATE;
            } else if ( this.updateType != PageRevisionEntrySet.PAGE_ATTRIBUTE_AND_CONTENT_UPDATE ){
                this.updateType = PageRevisionEntrySet.PAGE_CONTENT_UPDATE;
            }
            if ( this.notPageOverallRevision == null ){
                this.notPageOverallRevision = revisionEntry;
            } else if ( revWorkflowState ==
                 ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                this.notPageOverallRevision = revisionEntry;
            } else if ( revWorkflowState !=
                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                this.notPageOverallRevision = revisionEntry;
            } else if ( this.getWorkflowState() ==
                        ContentObjectEntryState.WORKFLOW_STATE_ACTIVE ){
                this.notPageOverallRevision = revisionEntry;
            }
            if ( revisionEntry.getWorkflowState()
                 == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED ){
                this.pageDeleted = true;
            }
        }
        this.onlyStaging = (this.onlyStaging &&
                revisionEntry.getWorkflowState()>ContentObjectEntryState.WORKFLOW_STATE_ACTIVE);
        super.addRevision(revisionEntry);
    }

    public int getWorkflowState(){
        if (this.getForcedWorkflowState() != null){
            return this.getForcedWorkflowState().intValue();
        }
        if ( this.overallRevision == null ){
            if (this.getRevisions() != null && !this.getRevisions().isEmpty() && this.onlyStaging){
                return ContentObjectEntryState.WORKFLOW_STATE_START_STAGING;
            } else if (this.notPageOverallRevision != null){
                return this.notPageOverallRevision.getWorkflowState();
            }
            return ContentObjectEntryState.WORKFLOW_STATE_VERSIONED;
        }
        return this.overallRevision.getWorkflowState();
    }

    public int getRevisionType(){
        if ( this.pageDeleted ){
            return PageRevisionEntrySet.PAGE_DELETED;
        }
        return this.updateType;
    }

    /**
     * @todo implement multilanguage with res bundle
     *
     * @param languageCode
     * @return
     */
    public String getDescription(String languageCode){
        if ( this.pageDeleted ){
            return "Page Delete";
        } else if ( this.updateType == PageRevisionEntrySet.PAGE_ATTRIBUTES_UPDATE ){
            return "Page Attributes Update";
        } else if ( this.updateType == PageRevisionEntrySet.PAGE_CONTENT_UPDATE ) {
            return "Page Contents Update";
        } else if ( this.updateType == PageRevisionEntrySet.PAGE_ATTRIBUTE_AND_CONTENT_UPDATE ) {
            return "Page Attributes and Contents Update";
        }
        return "";
    }
}

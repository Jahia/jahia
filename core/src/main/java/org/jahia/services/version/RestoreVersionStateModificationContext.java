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

import java.util.Set;

import org.jahia.content.ObjectKey;

/**
 *
 * <p>Title: Extended StateModificationContext, hold the entryState of the version to restore </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class RestoreVersionStateModificationContext extends StateModificationContext {

    private ContentObjectEntryState entryState;

    /**
     * if true, the restore version is an undelete indeed. So this is the last version that is restored
     */
    private boolean undelete = false;

    private int containerPageChildId = -1;

    /**
     * Activation context constructor
     * @param startObject specifies the object at which the state modification
     * was triggered, giving us a "start point" that we can insert in the
     * tree path.
     * @param languageCodes contains a Set of String object that contain the
     * language codes for which we want to perform an activation of the content
     * objects.
     * @param entryState, the entrystate to restore.
     */
    public RestoreVersionStateModificationContext(ObjectKey startObject,
                                                  Set languageCodes,
                                                  ContentObjectEntryState entryState) {
        this(startObject,languageCodes,entryState,false);
    }

    /**
     * Utility constructor to construct a context that has the descendingInSubPages
     * boolean directly initialized to a value.
     * @param startObject specifies the object at which the state modification
     * was triggered, giving us a "start point" that we can insert in the
     * tree path.
     * @param languageCodes contains a Set of String object that contain the
     * language codes for which we want to perform an activation of the content
     * objects.
     * @param descendingInSubPages true specifies that we want the activation
     * to be recursive and descend in sub pages. This is useful notably to
     * delete whole subsets of pages.
     * @param entryState, the entrystate to restore.
     */
    public RestoreVersionStateModificationContext(ObjectKey startObject,
                                    Set languageCodes,
                                    boolean descendingInSubPages,
                                    ContentObjectEntryState entryState) {
        this(startObject, languageCodes,descendingInSubPages,entryState,false);
    }

    /**
     * Activation context constructor
     * @param startObject specifies the object at which the state modification
     * was triggered, giving us a "start point" that we can insert in the
     * tree path.
     * @param languageCodes contains a Set of String object that contain the
     * language codes for which we want to perform an activation of the content
     * objects.
     * @param entryState, the entrystate to restore.
     * @param undelete, if true, this is the last version that is restored
     *
     */
    public RestoreVersionStateModificationContext(ObjectKey startObject,
                                                  Set languageCodes,
                                                  ContentObjectEntryState entryState,
                                                  boolean undelete) {
        super(startObject,languageCodes);
        this.entryState = entryState;
        this.undelete = undelete;
    }

    /**
     * Utility constructor to construct a context that has the descendingInSubPages
     * boolean directly initialized to a value.
     * @param startObject specifies the object at which the state modification
     * was triggered, giving us a "start point" that we can insert in the
     * tree path.
     * @param languageCodes contains a Set of String object that contain the
     * language codes for which we want to perform an activation of the content
     * objects.
     * @param descendingInSubPages true specifies that we want the activation
     * to be recursive and descend in sub pages. This is useful notably to
     * delete whole subsets of pages.
     * @param entryState, the entrystate to restore.
     * @param undelete, if true, this is the last version that is restored
     */
    public RestoreVersionStateModificationContext(ObjectKey startObject,
                                    Set languageCodes,
                                    boolean descendingInSubPages,
                                    ContentObjectEntryState entryState,
                                    boolean undelete) {
        super(startObject, languageCodes,descendingInSubPages);
        this.entryState = entryState;
        this.undelete = undelete;
    }

    public ContentObjectEntryState getEntryState(){
        return this.entryState;
    }

    /**
     * return -1 if null
     * @return int
     */
    public int getVersionId(){
        if ( this.getEntryState() == null ){
            return -1;
        }
        return this.getEntryState().getVersionID();
    }

    public boolean isUndelete() {
        return undelete;
    }

    public void setUndelete(boolean undelete) {
        this.undelete = undelete;
    }

    /**
     * if the value is different than -1, this means this context is created by the page of this id to restore it's parent container
     * @return
     */
    public int getContainerPageChildId() {
        return containerPageChildId;
    }

    public void setContainerPageChildId(int containerPageChildId) {
        this.containerPageChildId = containerPageChildId;
    }

}


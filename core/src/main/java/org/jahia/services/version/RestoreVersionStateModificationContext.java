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


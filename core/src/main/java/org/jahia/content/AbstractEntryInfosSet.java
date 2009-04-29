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
package org.jahia.content;

import java.util.SortedSet;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 * <p>Title: Content Entry Info Set for pageContent </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class AbstractEntryInfosSet {

    /**
     * The sorted set of ContentObjectEntryStateInfo
     */
    private SortedSet entryStates;

    /** lower revision date limit **/
    private long fromRevisionDate;

    /** upper revision date limit **/
    private long toRevisionDate;

    /** the number max of last revisions to display **/
    private int nbMaxOfRevisions = 10;

    public void setFromRevisionDate(long date){
        this.fromRevisionDate = date;
    }

    public long getFromRevisionDate(){
        return this.fromRevisionDate;
    }

    public void setToRevisionDate(long date){
        this.toRevisionDate = date;
    }

    public long getToRevisionDate(){
        return this.toRevisionDate;
    }

    public int getNbMaxOfRevisions(){
        return this.nbMaxOfRevisions;
    }

    public void setNbMaxOfRevisions(int value){
        this.nbMaxOfRevisions = value;
    }

    public SortedSet getEntryStates(){
        return this.entryStates;
    }

    public abstract void loadEntryStates(ContentObject contentObject,
                                         EntryLoadRequest loadRequest,
                                         JahiaUser user, int operation );
}


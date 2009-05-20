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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.usermanager.JahiaUser;

public class ContentObjectRevisions {

    /**
     *
     * @param rootObject
     * @param user
     * @param loadRequest
     * @param operationMode
     */
    public ContentObjectRevisions(JahiaUser user,
                                  EntryLoadRequest loadRequest,
                                  String operationMode){
    }

    /**
     *
     * @param contentObject
     * @param user
     * @param loadRequest
     * @param operationMode
     * @param withChildsContent
     * @param pageLevel
     * @param containerListLevel
     * @return
     * @throws JahiaException
     */
    public static SortedSet<RevisionEntry> getRevisions(ContentObject contentObject,
                                     JahiaUser user,
                                     EntryLoadRequest loadRequest,
                                     String operationMode,
                                     boolean withChildsContent,
                                     int pageLevel,
                                     int containerListLevel)
    throws JahiaException {

        if ( contentObject == null ){
            return null;
        }

        SortedSet<RevisionEntry> revisions = new TreeSet<RevisionEntry>();
        SortedSet<ContentObjectEntryState> entryStates =
                contentObject.getEntryStates();

        for ( ContentObjectEntryState entryState : entryStates){
            try {
                RevisionEntry revisionEntry =
                    new RevisionEntry(entryState,
                    ObjectKey.getInstance(contentObject.getObjectKey().getType()
                    + "_" + contentObject.getObjectKey().getIDInType()));
                revisions.add(revisionEntry);
            } catch ( Exception t ){
                throw new JahiaException("Exception creating revision entry ",
                                         "Exception creating revision entry ",
                                         JahiaException.DATA_ERROR,
                                         JahiaException.DATA_ERROR,t);
            }
        }
        if ( !withChildsContent ){
            return revisions;
        }

        List<? extends ContentObject> childs =
                contentObject.getChilds(user, loadRequest);
        for( ContentObject childObject : childs ){
            SortedSet<RevisionEntry> childRevisions = null;
            if ( childObject instanceof ContentPageField ){
                childRevisions =
                    getRevisions(childObject,user,loadRequest,
                            operationMode,
                            (pageLevel == -1 || pageLevel>0),
                            pageLevel-1, containerListLevel);
            } else if ( childObject instanceof ContentContainerList ){
                childRevisions =
                    getRevisions(childObject,user,loadRequest,
                            operationMode,
                            (containerListLevel == -1 || containerListLevel>0),
                            pageLevel, containerListLevel-1);
            } else {
                childRevisions =
                    getRevisions(childObject,user,loadRequest,
                            operationMode,
                            withChildsContent,
                            pageLevel, containerListLevel);
            }
            if ( childRevisions != null ){
                revisions.addAll(childRevisions);
            }
        }
        return revisions;
    }
}

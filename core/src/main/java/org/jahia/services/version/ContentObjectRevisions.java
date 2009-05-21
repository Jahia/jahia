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

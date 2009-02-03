/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.content;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaLinkManager;
import org.jahia.hibernate.manager.SpringContextSingleton;

import java.util.*;


/**
 * This class is the main object cross reference class. Basically it uses a
 * Map to group object reference this way :
 *
 * objectref -> set of objectrefs
 *
 * This way we can easily determine for a given object where it is referenced.
 * ObjectRefs may be of any kind that's a subclass of the ObjectKey class.
 * @author Serge Huber
 */

public class CrossReferenceManager {

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (CrossReferenceManager.class);

    // the Object Link cache name.
    public static final String OBJECT_LINK_CACHE = "ObjectLinkCache";
    // the preloaded object link by reference object.
    public static final String PRELOADED_OBJECT_LINKS_BY_REF_OBJECT_CACHE = "PreloadedObjectLinksByRefObjectCache";

    /** the unique instance of this class */
    private static CrossReferenceManager instance;

    public static final String REFERENCE_TYPE = "reference";
    private JahiaLinkManager linkManager;

    /** Default constructor, creates a new <code>CrossReferenceManager</code> instance.
     */
    protected CrossReferenceManager () {

        logger.debug ("Initializing...");
        linkManager = (JahiaLinkManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaLinkManager.class.getName());
    }


    /**
     * <p>Returns the unique instance of this service.</p>
     * <p><strong>Note</strong>: this is a synchronized access method so it might affect performance
     * if a lot of threads must access it simultaneously.</p>
     *
     * @return the class instance.
     */
    public static synchronized CrossReferenceManager getInstance () {
        if (instance == null) {
            instance = new CrossReferenceManager ();
        }
        return instance;
    }


    /**
     * Retrieve a set of object references, basically all the objects that
     * reference the object passed in parameter
     * @param objectKey an ObjectKey type object that identifies the object for
     * which to retrieve the set of references
     * @return a set of ObjectKey objects that are the objects that reference
     * the object passed in parameter
     * @throws JahiaException in case there was a problem retrieving the
     * references from the database
     */
    public Set<ObjectKey> getObjectXRefs (ObjectKey objectKey)
            throws JahiaException {
        logger.debug ("Retrieving xrefs for object " + objectKey);

        // let's try to load it from the database
        List<ObjectLink> leftLinks = linkManager.findByTypeAndRightObjectKey(REFERENCE_TYPE, objectKey);
        Set<ObjectKey> set = new HashSet<ObjectKey>();
        for (ObjectLink curLink : leftLinks) {
            set.add (curLink.getLeftObjectKey ());
        }
        return set;
    }

    /**
     * Tests if an xref exists
     * @param leftObjectKey
     * @param rightObjectKey
     * @return true if the reference exists, false otherwise
     */
    public boolean isXRefExisting (ObjectKey leftObjectKey, ObjectKey rightObjectKey) {
        List<ObjectLink> objectLinks = linkManager.findByTypeAndLeftAndRightObjectKeys(REFERENCE_TYPE, leftObjectKey, rightObjectKey);
        if (objectLinks.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Performs a reverse lookup of objects, by specifying the reference
     * destination and returning all the sources that are in pointed to from
     * that destination.
     *
     * Note : This method iterates through all the sources to find the
     * destination target so it is NOT optimal and has some overhead. Maybe a
     * further optimization would be to build bidirectional references, but
     * this would complicate handling of "dirty" references.
     *
     * @param objectXRef the destination reference object key that specifies
     * for which object we want to lookup source object.
     * @return a set of ObjectKey objects that are the objects that are
     * references by the passed reference object key.
     * @throws JahiaException in case there was a problem retrieving the
     * references from the database
     */
    public Set<ObjectKey> getReverseObjectXRefs(ObjectKey objectXRef)
            throws JahiaException {
        Set<ObjectKey> objectSourceKeys = new HashSet<ObjectKey>();

        // this could be because the keys were not yet loaded from the
        // database, let's make sure we do so if necessary.
        List<ObjectLink> rightLinks = linkManager.findByTypeAndLeftObjectKey(
                REFERENCE_TYPE, objectXRef);
        Set<ObjectKey> resultSet = new HashSet<ObjectKey>();

        for (ObjectLink curLink : rightLinks) {
            resultSet.add(curLink.getRightObjectKey());
            // getObjectXRefs (curLink.getRightObjectKey ());
        }
        objectSourceKeys = resultSet;

        return objectSourceKeys;
    }

    /**
     * Adds a cross reference between the two objects passed in parameter. The
     * first object key is used as the key in the lookup table.
     * @param objectKey the key for the object to add a cross reference to
     * @param objectXRef another key for the object that references the first
     * object
     * @throws JahiaException in case there were problems storing the references
     * in the database
     */
    public void setObjectXRef (ObjectKey objectKey, ObjectKey objectXRef)
            throws JahiaException
    {
        logger.debug ("Setting xref from object " + objectXRef +
                " to " + objectKey + " ...");
        if ( objectKey != null && objectXRef != null
             && objectKey.equals(objectXRef) ){
            // cannot reference an object to itself
            logger.debug ("Setting xref cannot set reference for the object " + objectXRef +
                    " to itself !!!");
            return;
        }

//        if (!isXRefExisting(objectXRef, objectKey)) {
            ObjectLink.createLink (objectXRef, objectKey, REFERENCE_TYPE,
                    new HashMap<String, String>());
//        }

    }


    /**
     * Removes all cross references for an object.
     * @param objectKey the key for the object whose references are to be
     * deleted.
     */
    public void removeObjectXRefs (ObjectKey objectKey)
            throws JahiaException {
        logger.debug ("Removing xrefs for object " + objectKey.getKey ());
        // now let's remove it from the database, making sure we only use the
        // links that existed for the cross reference manager and no other
        // type of link.
        for (ObjectKey curXRefKey : getObjectXRefs (objectKey)) {
            List<ObjectLink> objectLinks = linkManager.findByTypeAndLeftAndRightObjectKeys (REFERENCE_TYPE, curXRefKey, objectKey);
            ListIterator<ObjectLink> objectLinkIter = objectLinks.listIterator ();
            while (objectLinkIter.hasNext ()) {
                ObjectLink curLink = (ObjectLink)objectLinkIter.next ();
                curLink.remove ();
            }

        }
    }

    public void removeAllObjectXRefs (ObjectKey objectKey)
            throws JahiaException {
        logger.debug ("Removing xrefs for object " + objectKey.getKey ());
        // Now remove all links to or from this objects
        List<ObjectLink> objectLinks = linkManager.findByLeftObjectKey(objectKey);
        ListIterator<ObjectLink> objectLinkIter = objectLinks.listIterator();
        while (objectLinkIter.hasNext()) {
            ObjectLink curLink = objectLinkIter.next();
            curLink.remove();
        }
        objectLinks = linkManager.findByRightObjectKey(objectKey);
        objectLinkIter = objectLinks.listIterator();
        while (objectLinkIter.hasNext()) {
            ObjectLink curLink = objectLinkIter.next();
            curLink.remove();
        }
    }
    /**
     * Remove an xref for a given object, leaving all the other xrefs intact
     * @param objectKey the destination object that is the target of the
     * reference
     * @param objectXRef the object source of the reference
     * @throws JahiaException thrown in case there was a problem communicating
     * with the database
     */
    public void removeObjectXRef (ObjectKey objectKey, ObjectKey objectXRef)
            throws JahiaException {
        logger.debug ("Removing xref " + objectXRef.getKey () + " for object " +
                objectKey.getKey ());
        Set<ObjectKey> curXRefSet = getObjectXRefs (objectKey); // loads from database if necessary
        if (curXRefSet != null) {
            if (curXRefSet.contains (objectXRef)) {
                // ok we found the xref, let's remove it from memory
                curXRefSet.remove (objectXRef);

                // now let's remove it from the database.
                List<ObjectLink> objectLinks = linkManager.findByTypeAndLeftAndRightObjectKeys (
                                REFERENCE_TYPE, objectXRef, objectKey);

                ListIterator<ObjectLink> objectLinkIter = objectLinks.listIterator ();
                while (objectLinkIter.hasNext ()) {
                    ObjectLink curLink = objectLinkIter.next ();
                    curLink.remove ();
                }
            }
        }

    }

    public String toString () {
        StringBuffer refTableBuffer = new StringBuffer ();

        refTableBuffer.append ("CrossReferenceManager internal state\n");

        return refTableBuffer.toString ();
    }
}
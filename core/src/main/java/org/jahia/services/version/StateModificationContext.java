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

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Collection;

import org.jahia.content.ObjectKey;
import org.jahia.content.ContentObjectKey;

/**
 * <p>Title: Contains the "context" of an activation operation</p>
 * <p>Description: This object is passed to the whole hierarchy of content
 * objects during an activation process and can be used both as a configuration
 * bean and as a storage mechanism. Notably it is used to detect activation
 * "looping" when we have link objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Solutions Sarl</p>
 * @author Serge Huber
 * @version 1.0
 */

public class StateModificationContext {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(StateModificationContext.class);

    // contains the set of language codes that select the languages for which
    // we are activating the content objects.
    private Set<String> languageCodes;

    // specifies whether we do a recursive activation by following all links
    // to sub pages.
    private boolean descendingInSubPages = false;

    // contains the current path in the content tree. Used to detect looping
    // when following links.
    private Stack currentTreePath = new Stack();

    // specifies whether the operations should be done for all languages or
    // just some specific ones. This is notably used when performing marking
    // for deletion operations because we might break content tree nodes.
    private Stack allLanguagesStack = new Stack();

    private Set allModifiedObjects = new HashSet();

    /**
     * Making the default constructor private because we want to enforce the
     * creation of this object only with valid language codes.
     */
    private StateModificationContext() {
        allLanguagesStack.push(Boolean.FALSE);
    }

    /**
     * Activation context constructor
     * @param startObject specifies the object at which the state modification
     * was triggered, giving us a "start point" that we can insert in the
     * tree path.
     * @param languageCodes contains a Set of String object that contain the
     * language codes for which we want to perform an activation of the content
     * objects.
     */
    public StateModificationContext(final ObjectKey startObject, final Set languageCodes) {
        this();
        currentTreePath.push(startObject);
        allModifiedObjects.add(startObject);

        if (languageCodes != null) {
            this.languageCodes = languageCodes;
        } else {
            logger.debug("ActivationContext constructor passed null languageCode set !!!");
            this.languageCodes = new HashSet();
        }
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
     */
    public StateModificationContext(final ObjectKey startObject,
                                    final Set languageCodes,
                                    final boolean descendingInSubPages) {
        this(startObject, languageCodes);
        this.descendingInSubPages = descendingInSubPages;
    }

    /**
     * If true it means that the activation will descend in sub pages to activate
     * them also. Otherwise the activation will not activate fields and containers
     * that contain subpages that have no active version.
     * @return see the description for the meaning of the values returned.
     */
    public boolean isDescendingInSubPages() {
        return descendingInSubPages;
    }

    /**
     * If true it means that the activation will descend in sub pages to activate
     * them also. Otherwise the activation will not activate fields and containers
     * that contain subpages that have no active version.
     *
     * @param descendingInSubPages see the description for the meaning of the
     * values returned.
     */
    public void setDescendingInSubPages(final boolean descendingInSubPages) {
        this.descendingInSubPages = descendingInSubPages;
    }

    /**
     * Returns the current path in the content object tree of validation. This
     * can be used to detect loops in the validation process by testing the
     * position of a content object in the stack.
     * @return a Stack containing ObjectKey object that contain
     * identifier to Jahia objects.
     */
    public java.util.Stack getCurrentTreePath() {
        return currentTreePath;
    }

    /**
     * Add an object to the current content object path tree.
     * @param objectID the activation object ID to be added to the stack.
     */
    public void pushObjectID(final ObjectKey objectID) {
        currentTreePath.push(objectID);
    }

    /**
     * Remove an object from the current content object path tree represented
     * by a stack. If we try to remove the start object it is returned but NOT
     * removed from the stack.
     * @return the object removed from the stack.
     */
    public ObjectKey popObjectID() {
        if (currentTreePath.size() > 1) {
            return (ObjectKey) currentTreePath.pop();
        } else {
            return (ObjectKey) currentTreePath.firstElement();
        }
    }

    public ObjectKey getStartObject() {
        return (ObjectKey) currentTreePath.firstElement();
    }

    /**
     * Tests for the presence of an object ID in the current tree path. This
     * is used to detect looping in the activation process.
     * @param objectID the object ID to check for presence in the current
     * content object path tree stack.
     * @return true if the object ID was found in the path, false otherwise.
     */
    public boolean isObjectIDInPath(final ObjectKey objectID) {
        return currentTreePath.contains(objectID);
    }

    public Set<String> getLanguageCodes() {
        return languageCodes;
    }

    public String toString() {
        final StringBuffer result = new StringBuffer();
        result.append("stateModifContext=[");
        result.append("descendingInSubPages=");
        result.append(isDescendingInSubPages());
        result.append(",currentTreePath=");
        result.append(currentTreePath.toString());
        result.append(",languageCodes=");
        result.append(languageCodes.toString());
        result.append("]");
        return result.toString();
    }

    public boolean isAllLanguages() {
        final Boolean firstElement = (Boolean) allLanguagesStack.peek();
        return firstElement.booleanValue();
    }

    /**
     * Specifies whether all languages should be targeted by this operation.
     * This is more significant than the language code set attached to this
     * object.
     * @param allLanguages set to true if all objects should be affected from
     * here on.
     */
    public void pushAllLanguages(final boolean allLanguages) {
        if (allLanguages) {
            logger.debug("Warning : all languages for content object will be "+
                         "deleted from here down !");
        }
        this.allLanguagesStack.push(Boolean.valueOf(allLanguages));
    }

    public boolean popAllLanguages() {
        final Boolean removedElement = (Boolean) allLanguagesStack.pop();
        return removedElement.booleanValue();
    }

    public void addModifiedObject(ContentObjectKey k) {
        allModifiedObjects.add(k);
    }

    public void addModifiedObjects(Collection c) {
        allModifiedObjects.addAll(c);
    }

    public boolean isModifiedObject(ContentObjectKey k) {
        return allModifiedObjects.contains(k);
    }
}
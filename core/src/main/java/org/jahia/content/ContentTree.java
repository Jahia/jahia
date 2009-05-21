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
 package org.jahia.content;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 * <p>Title: Class to traverse Jahia's Content Tree.</p>
 * <p>Description: This class define a default implementation for traversing
 * Jahia's Content Tree.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class ContentTree {

    private ContentObject rootContentObject;

    // Stack of contentTree status for handling iteration through the tree
    private Stack<ContentTreeStatusInterface> contentTreeStatusStack = new Stack<ContentTreeStatusInterface>();

    /**
     * The root content object of the three
     *
     * @param rootContentObject
     */
    public ContentTree(ContentObject rootContentObject) {

        this.rootContentObject = rootContentObject;
    }

    /**
     * Returns the root content object
     *
     * @return
     */
    public ContentObject getRootContentObject(){
        return this.rootContentObject;
    }

    /**
     * Iterate through the entire Tree
     *
     * @param Visitor
     */
    public void iterate(ContentTreeVisitorInterface visitor)
    throws JahiaException {
        this.contentTreeStatusStack = new Stack<ContentTreeStatusInterface>();
        iterate(this.rootContentObject,visitor,0);
    }

    /**
     * The default implementation for gettings the childs of a given
     * Content object
     *
     * @param contentObject
     * @param user
     * @param loadRequest
     * @param operationMode
     * @return
     * @throws JahiaException
     */
    public List<? extends ContentObject> getContentChilds(ContentObject contentObject,
                                      JahiaUser user,
                                      EntryLoadRequest loadRequest,
                                      String operationMode)
    throws JahiaException {
        List<? extends ContentObject> childs
                = contentObject.getChilds(user, loadRequest);
        return childs;
    }

    /**
     * Returns the contentTreeStatusStack
     *
     * @return
     */
    public Stack<ContentTreeStatusInterface> getContentTreeStatusStack(){
        return this.contentTreeStatusStack;
    }

    /**
     * Iterate through the entire Tree
     *
     * @param contentObject
     * @param visitor
     * @param currentPageLevel
     * @throws JahiaException
     */
    private void iterate(ContentObject contentObject,
                         ContentTreeVisitorInterface visitor,
                         int currentPageLevel)
    throws JahiaException {

        int descendingPageLevel = visitor.getDescendingPageLevel();

        ContentTreeStatusInterface contentTreeStatus =
                visitor.getContentTreeStatus(contentObject,currentPageLevel);

        this.contentTreeStatusStack.push(contentTreeStatus);

        if ( descendingPageLevel != -1
             && currentPageLevel>descendingPageLevel ){

            // process last ContentPageField for which we don't want to go further
            visitor.processLastContentPageField(contentObject, currentPageLevel);
            // stop iterate further
        } else {
        // processing root object
        visitor.processContentObjectBeforeChilds(contentObject,
                currentPageLevel);

        // processing childs object
        if ( visitor.withChildsContent()
             && contentTreeStatus.continueWithChilds() ){
            List<? extends ContentObject> childs = visitor.getChilds(contentObject,
                                                  currentPageLevel);
            Iterator<? extends ContentObject> childsIterator = childs.iterator();
            ContentObject childContentObject;
                while (childsIterator.hasNext()
                       && contentTreeStatus.continueWithChilds()) {
                childContentObject = (ContentObject)childsIterator.next();
                if ( childContentObject instanceof ContentPageField ){
                    iterate(childContentObject, visitor,
                            currentPageLevel + 1);
                }
                    else {
                        iterate(childContentObject, visitor, currentPageLevel);
                }
            }
        }
        if ( contentTreeStatus.continueAfterChilds() ){
            visitor.processContentObjectAfterChilds(contentObject,
                    currentPageLevel);
            } else {
                visitor.stopProcessContentObjectAfterChilds(contentObject,
                    currentPageLevel);
            }
        }
        // remove the status
        this.contentTreeStatusStack.pop();
    }
}
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
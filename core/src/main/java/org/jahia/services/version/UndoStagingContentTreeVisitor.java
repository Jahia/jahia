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

 package org.jahia.services.version;

import org.jahia.content.AbstractContentTreeVisitor;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentTree;
import org.jahia.content.ContentTreeStatus;
import org.jahia.content.ContentTreeStatusInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;

import java.util.*;


/**
 *
 * <p>Title: Undo Staging Content Tree Visitor </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class UndoStagingContentTreeVisitor extends AbstractContentTreeVisitor {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(UndoStagingContentTreeVisitor.class);

    protected ContentTree contentTree;
    private ProcessingContext jParams = null;

    // The id of the start page ( which is not the root content object, but the
    // page for which, we can apply undo staging on referred parent page fields.
    // We need this information to handle page move issue.
    private int startPageId = -1;

    /**
     *
     * @param rootContentObject ContentObject
     * @param user JahiaUser
     * @param loadRequest EntryLoadRequest
     * @param operationMode String
     * @param jParams ProcessingContext
     */
    public UndoStagingContentTreeVisitor( ContentObject rootContentObject,
                                          JahiaUser user,
                                          EntryLoadRequest loadRequest,
                                          String operationMode,
                                          ProcessingContext jParams){
        super(user,loadRequest,operationMode);
        contentTree = new ContentTree(rootContentObject);
        this.setDescendingPageLevel(0); // never descending in pages childs.
        this.jParams = jParams;
        if ( rootContentObject instanceof ContentPage ){
            this.startPageId = rootContentObject.getID();
        }
    }

    /**
     *
     * @param rootContentObject ContentObject
     * @param user JahiaUser
     * @param loadRequest EntryLoadRequest
     * @param operationMode String
     * @param jParams ProcessingContext
     * @param startPageId int, precise the page for which we allow undo page move
     */
    public UndoStagingContentTreeVisitor( ContentObject rootContentObject,
                                          JahiaUser user,
                                          EntryLoadRequest loadRequest,
                                          String operationMode,
                                          ProcessingContext jParams,
                                          int startPageId){
        super(user,loadRequest,operationMode);
        contentTree = new ContentTree(rootContentObject);
        this.setDescendingPageLevel(0); // never descending in pages childs.
        this.jParams = jParams;
        this.startPageId = startPageId;
    }

    /**
     * Start undo staging
     *
     */
    public void undoStaging()
    throws JahiaException{
        getContentTree().iterate(this);
    }

    /**
     * Return the internal ContentTree used to traverse the Content Tree
     *
     * @return
     */
    public ContentTree getContentTree(){
        return this.contentTree;
    }

    /**
     * Returns a ContentTreeStatus implementation for a given ContentObject
     *
     * @param contentObject
     * @return
     */
    public ContentTreeStatusInterface getContentTreeStatus(ContentObject contentObject,
            int currentPageLevel)
    throws JahiaException{
        ContentTreeStatus contentTreeStatus = new ContentTreeStatus();
        return contentTreeStatus;
    }

    /**
     * process the current content object when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectBeforeChilds(ContentObject contentObject,
                                                 int currentPageLevel)
    throws JahiaException{
    }

    /**
     * Called after processing the current object's childs when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectAfterChilds(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException{

        if ( contentObject instanceof ContentPage ){
            ContentPage page = (ContentPage)contentObject;
            // we must undo all staging page field linked with this page
            // ( case x of moved page ).
            contentObject.undoStaging(this.jParams);

            List<Integer> ids = ServicesRegistry.getInstance().getJahiaPageService()
                .getStagingAndActivePageFieldIDs(page.getID());
            ContentObject pageField = null;
            for ( Integer id : ids){
                pageField = ContentPageField.getField(id.intValue());
                // undoStagingDeletedContainer(pageField);
                // undo the parent container too
                try {
                    ContentObject parent = pageField
                        .getParent(getUser(), getEntryLoadRequest(),
                                   getOperationMode());
                    if (parent != null && parent instanceof ContentContainer) {
                        this.undoPageMove(page.getID(),parent);
                    } else {
                        this.undoPageMove(page.getID(),pageField);
                    }
                } catch ( Exception t ){
                    logger.debug(t);
                }
            }
        } else {
            contentObject.undoStaging(this.jParams);
        }
    }

    /**
     * Process the Last Content page Field on which, we reached the page level limit when
     * descending in subtree.
     *
     * @param contentObject the last content page field on which we reach the page level limit.
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processLastContentPageField(
            ContentObject contentObject,
            int currentPageLevel)
    throws JahiaException{

        ContentPage page = null;
        EntryLoadRequest loadRequest = (EntryLoadRequest)EntryLoadRequest.STAGED.clone();
        loadRequest.setWithMarkedForDeletion(true);
        String value = ((ContentPageField)contentObject).getValue(this.jParams,loadRequest);
        int pageId = -1;
        if ( value != null ){
            try {
                pageId = Integer.parseInt(value);
                if ( pageId == -1 ){
                    // no page attached with this page field
                    contentObject.undoStaging(this.jParams);
                    return;
                } else {
                    page = ContentPage.getPage(pageId);
                }
            } catch ( Exception t ){
            }
        }

        if ( page == null ){
            // if the page doesn't exist, we just undo this field
            contentObject.undoStaging(this.jParams);
            return;
        }

            // we handle differently direct and link page
        if ( page.getPageType(jParams.getEntryLoadRequest())
             == ContentPage.TYPE_DIRECT ){

            if ( page.getID() == this.getStartPageId() ){
                contentObject.undoStaging(this.jParams);
            } else {
                int versionId = ServicesRegistry.getInstance()
                    .getJahiaVersionService().getCurrentVersionID();
                if ( !contentObject.hasArchiveEntryState(versionId) ){
                    // this page field exists only in staging,
                    // we avoid undo staging if the start page is not the one pointed by this page field
                    ContentPageField contentPageField = (ContentPageField)
                        contentObject;
                    if (contentPageField.getContainerID() > 0) {
                        // we don't allow the parent container to undo staging
                        ContentTreeStatusInterface contentTreeStatus =
                            (ContentTreeStatusInterface)
                            this.contentTree.getContentTreeStatusStack().pop();

                        ContentTreeStatusInterface parentStatus =
                            (ContentTreeStatusInterface)
                            this.contentTree.getContentTreeStatusStack().peek();
                        parentStatus.setContinueAfterChilds(false);

                        this.contentTree.getContentTreeStatusStack()
                            .push(contentTreeStatus);
                    }
                } else {
                    // There is an issue with container containing more than one direct page
                    // If one of these page field reach this point, set the parentStatus to not continue
                    // after child will never undo the container !!
                    // We can do so only if the startPageId does not have the same parent container as this
                    // page
                    if (this.getStartPageId() > 0) {
                        int fieldId = ServicesRegistry.getInstance()
                            .getJahiaPageService().getPageFieldID(this.
                            getStartPageId());
                        if ( fieldId != -1 ){
                            ContentField contentField = ContentField.getField(
                                fieldId);
                            if ( contentField != null ){
                                try {
                                    ContentObject parent = contentField
                                        .getParent(getUser(), getEntryLoadRequest(),
                                                   getOperationMode());
                                    if (parent != null && parent instanceof ContentContainer) {
                                        ContentObject objectParent =
                                            contentObject.getParent(getUser(),
                                                                    getEntryLoadRequest(),
                                                                    getOperationMode());
                                        if ( objectParent != null
                                             && parent.getObjectKey().equals(objectParent.getObjectKey()) ){
                                            // We allows the container to undo staging
                                            return;
                                        }
                                    }
                                } catch ( Exception t ){
                                    logger.debug(t);
                                }

                            }
                        }

                    }
                    // in all other case, we don't allow the parent container to undo staging
                    ContentTreeStatusInterface contentTreeStatus =
                        (ContentTreeStatusInterface)
                            this.contentTree.getContentTreeStatusStack().pop();

                    ContentTreeStatusInterface parentStatus =
                        (ContentTreeStatusInterface)
                        this.contentTree.getContentTreeStatusStack().peek();
                        parentStatus.setContinueAfterChilds(false);

                    this.contentTree.getContentTreeStatusStack()
                        .push(contentTreeStatus);
                    }
                }
            } else {
                // not a direct page, so we can undo the page field and the page link
                // too.
                contentObject.undoStaging(jParams);
                page.undoStaging(jParams);
            }
        }

    /**
     * Returns an array list of childs for a given ContentObject
     *
     * @param contentObject
     * @param currentPageLevel
     * @return
     */
    public List<? extends ContentObject> getChilds(ContentObject contentObject,
                               int currentPageLevel)
    throws JahiaException {

        return super.getChilds(contentObject,currentPageLevel);
    }

    private int getStartPageId(){
        return this.startPageId;
    }

    /**
     * Perform undo of a page move
     *
     * @param pageId int
     * @param rootContentObject ContentObject
     */
    private void undoPageMove(int pageId, ContentObject rootContentObject)
    throws JahiaException {
        if ( rootContentObject == null ){
            return;
        }
        UndoStagingContentTreeVisitor undoSV =
            new UndoStagingContentTreeVisitor(rootContentObject,
            getUser(), getEntryLoadRequest(), getOperationMode(),
            jParams, pageId);
        undoSV.undoStaging();
    }

}

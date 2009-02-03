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

import java.util.*;

import org.jahia.content.AbstractContentTreeVisitor;
import org.jahia.content.ContentDefinition;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentTree;
import org.jahia.content.ContentTreeStatus;
import org.jahia.content.ContentTreeStatusInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.fields.ContentField;
import org.jahia.content.JahiaObject;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.JahiaField;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.apache.commons.collections.map.LRUMap;

/**
 *
 * <p>Title: Concrete visitor for handling Content object revisions </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class ContentTreeRevisionsVisitor extends AbstractContentTreeVisitor {

    public static final int CONTENT_REVISION_ENTRY = 1;
    public static final int METADATA_REVISION_ENTRY = 2;
    public static final int CONTENT_AND_METADATA_REVISION_ENTRY = CONTENT_REVISION_ENTRY | METADATA_REVISION_ENTRY;

    static private TimeZone tz = TimeZone.getTimeZone("UTC");

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentTreeRevisionsVisitor.class);

    protected int revisionEntryType = CONTENT_REVISION_ENTRY;

    protected ContentTree contentTree;
    protected List revisionsList;
    protected Map revisionsStates;

    /** lower revision date limit **/
    protected long fromRevisionDate;

    /** upper revision date limit **/
    protected long toRevisionDate;

    /** ignore or not revisions of content currently deleted **/
    protected boolean withDeletedContent;

    /** ignore or not revisions with active status **/
    protected boolean withActiveRevisions = true;

    /** ignore or not revisions with deleted status **/
    protected boolean withDeletedRevisions = true;

    /** ignore or not staging revisions **/
    protected boolean withStagingRevisions = false;

    protected boolean hasAttributeChange = true;

    /**
     * By default return revisions in all languages
     */
    private boolean applyLanguageFiltering = false;

    /**
     * By default do not check if for a a given revision, the parent Page is available or deleted
     * If true, skip revisions at which the parent page is deleted or not available
     */
    private boolean skipNotAvailablePageRevisions = false;

    private LRUMap notAvailablePageRevisionsCache = new LRUMap(50);

    protected String lastSortLanguageCode = "";
    protected int lastSortAttribute = -1;
    protected int lastSortOrder = -1;

    /**
     *
     * @param rootContentObject
     * @param user
     * @param loadRequest
     * @param operationMode
     */
    public ContentTreeRevisionsVisitor(ContentObject rootContentObject,
                                       JahiaUser user,
                                       EntryLoadRequest loadRequest,
                                       String operationMode){
        super(user,loadRequest,operationMode);
        this.revisionsList = new ArrayList();
        this.revisionsStates = new HashMap();
        this.contentTree = new ContentTree(rootContentObject);
    }

    /**
     * Set the internal user used to check rights access
     * @param user
     */
    public void setUser(JahiaUser user){
        if ( !getUser().getUserKey().equals(user.getUserKey()) ){
            this.hasAttributeChange = true;
        }
        super.setUser(user);
    }

    /**
     * Set the EntryLoadRequest used to retrieve's Content Object Childs.
     * @param loadRequest
     */
    public void setEntryLoadRequest(EntryLoadRequest loadRequest){
        if ( !getEntryLoadRequest().toString().equals(loadRequest.toString()) ){
            this.hasAttributeChange = true;
        }
        super.setEntryLoadRequest(loadRequest);
    }

    /**
     * Set the internal OperationMode used to retrieve's Content Object Childs.
     * @param operationMode
     */
    public void setOperationMode(String operationMode){
        if ( !getOperationMode().equals(operationMode) ){
            this.hasAttributeChange = true;
        }
        super.setOperationMode(operationMode);
    }

    /**
     * Set the descending page level.
     */
    public void setDescendingPageLevel(int level){
        if ( getDescendingPageLevel() != level ){
            this.hasAttributeChange = true;
        }
        super.setDescendingPageLevel(level);
    }

    /**
     * Return the internal ContentTree used to traverse the Content Tree
     * @return
     */
    public ContentTree getContentTree(){
        return this.contentTree;
    }

    /**
     * Return the internal list of revisions
     * @return
     */
    public List getRevisions(){
        return this.revisionsList;
    }

    /**
     * The type of revision entry <code>CONTENT_REVISION_ENTRY</code>, or <code>METADATA_REVISION_ENTRY</code> or
     * <code>CONTENT_AND_METADATA_REVISION_ENTRY</code>
     * @return
     */
    public int getRevisionEntryType() {
        return revisionEntryType;
    }

    public void setRevisionEntryType(int revisionEntryType) {
        this.revisionEntryType = revisionEntryType;
    }

    /**
     * process the current content object when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public abstract void processContentObjectBeforeChilds(ContentObject contentObject,
                                                          int currentPageLevel)
    throws JahiaException;

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
     * Returns an array list of childs for a given ContentObject
     *
     * @param contentObject
     * @param currentPageLevel
     * @return
     */
    public List<ContentObject> getChilds(ContentObject contentObject,
                               int currentPageLevel)
    throws JahiaException{

//        List results = new ArrayList();
        /*
        if ( contentObject instanceof ContentPageField ){
            if ( this.getDescendingPageLevel() >=0
                 && this.getDescendingPageLevel()>=currentPageLevel ){
                return results;
            }
            return results;
        }*/
        return super.getChilds(contentObject,currentPageLevel);
    }

    /**
     * Reload the revisions
     *
     * @param ignoreAttributeChange if true, ignore the state returned by hasChange
     */
    public void loadRevisions(boolean ignoreAttributeChange)
    throws JahiaException{
        if ( ignoreAttributeChange || hasAttributeChange() ){
            // have to reload the content root object
            ContentObject rootObject = this.getContentTree()
                                     .getRootContentObject();
            try {
                rootObject = (ContentObject) JahiaObject.getInstance(rootObject.getObjectKey());
                this.contentTree = new ContentTree(rootObject);
                this.revisionsList = new ArrayList();
                getContentTree().iterate(this);

                // loading stagings
                if ( this.isWithStagingRevisions() ){
                    EntryLoadRequest versionedLoadRequest = this.getEntryLoadRequest();
                    List locales = new ArrayList();
                    Iterator it = versionedLoadRequest.getLocales().iterator();
                    Locale loc = null;
                    while ( it.hasNext() ){
                        loc = (Locale)it.next();
                        locales.add(loc.clone());
                    }
                    EntryLoadRequest stagedEntryLoadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,
                            locales);
                    this.setEntryLoadRequest(stagedEntryLoadRequest);
                    getContentTree().iterate(this);
                    this.setEntryLoadRequest(versionedLoadRequest);
                }

            } catch ( Exception t ){
                logger.debug("Exception when trying to reload Revisions", t);
            }

            // filter out all revisions at which the rootObject is deleted
            List result = new ArrayList();
            List revisions = this.getRevisions();
            int size = revisions.size();
            RevisionEntrySet rs = null;
            rootObject = this.getContentTree().getRootContentObject();
            boolean staging = false;
            RevisionEntrySet stagingEntrySet = null;
            int maxVersion = 0;

            RevisionEntrySet liveRevisionEntrySet = null;
            for ( int i=0; i<size; i++ ){
                rs = (RevisionEntrySet)revisions.get(i);
                staging = rs.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE;
                if ( staging ){
                    stagingEntrySet = rs;
                } else {
                    if ( maxVersion < rs.getVersionID() ){
                        maxVersion = rs.getVersionID();
                    }
                }
                if ( staging || !rootObject.isDeletedOrDoesNotExist(rs.getVersionID()) ){
                    if (liveRevisionEntrySet == null && rs.getWorkflowState()== EntryLoadRequest.ACTIVE_WORKFLOW_STATE){
                        liveRevisionEntrySet = rs;
                    } else if (rs.getWorkflowState()== EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                            && liveRevisionEntrySet.getVersionID()<rs.getVersionID()){
                        liveRevisionEntrySet.setForcedWorkflowState(new Integer(EntryLoadRequest.VERSIONED_WORKFLOW_STATE)); 
                        liveRevisionEntrySet = rs;
                    }
                    result.add(rs);
                }
            }
            if (liveRevisionEntrySet != null){
                liveRevisionEntrySet.setForcedWorkflowState(new Integer(EntryLoadRequest.ACTIVE_WORKFLOW_STATE));
            }
            if ( size > 1 && stagingEntrySet != null && stagingEntrySet.getVersionID()<=maxVersion ){
                // inconsistent staged version id !
                stagingEntrySet.setVersionID(ServicesRegistry.getInstance().getJahiaVersionService().getCurrentVersionID());
            }
            this.revisionsList = result;

            this.sortRevisions( Locale.getDefault().toString(),
                                RevisionEntrySetComparator.SORT_BY_DATE,
                                RevisionEntrySetComparator.ASC_ORDER, true);

            size = revisionsList.size();
            for ( int i=0; i<size; i++ ){
                rs = (RevisionEntrySet)revisionsList.get(i);
                rs.setVersionNumber(i+1);
            }
        }
        this.hasAttributeChange = false;
    }

    /**
     * Sort the revisions
     *
     * @param languageCode
     * @param sortAttribute
     * @param sortOrder
     * @param ignoreSortAttributeChange
     * @throws JahiaException
     */
    public void sortRevisions(String languageCode,
                              int sortAttribute,
                              int sortOrder,
                              boolean ignoreSortAttributeChange)
    throws JahiaException{

        if ( ignoreSortAttributeChange ||
             !lastSortLanguageCode.equals(languageCode) ||
             lastSortAttribute != sortAttribute ||
             lastSortOrder != sortOrder ){
            RevisionEntrySetComparator comparator =
                    new RevisionEntrySetComparator(languageCode, sortAttribute, sortOrder);
            this.sort(comparator);
        }
    }

    /**
     * Returns true if one of the attributes used to retrieve revisions has changed
     * @return
     */
    public boolean hasAttributeChange(){
        return this.hasAttributeChange;
    }

    public void setFromRevisionDate(long date){
        if ( date != this.fromRevisionDate){
            this.hasAttributeChange = true;
        }
        this.fromRevisionDate = date;
    }

    public long getFromRevisionDate(){
        return this.fromRevisionDate;
    }

    public long getToRevisionDate(){
        return this.toRevisionDate;
    }

    public void setToRevisionDate(long date){
        if ( date != this.toRevisionDate){
            this.hasAttributeChange = true;
        }
        this.toRevisionDate = date;
    }

    public boolean isWithDeletedContent(){
        return this.withDeletedContent;
    }

    public void setWithDeletedContent(boolean value){
        if ( value != this.withDeletedContent){
            this.hasAttributeChange = true;
        }
        this.withDeletedContent = value;
    }

    public boolean isWithActiveRevisions(){
        return this.withActiveRevisions;
    }

    public void setWithActiveRevisions(boolean value){
        if ( value != this.withActiveRevisions){
            this.hasAttributeChange = true;
        }
        this.withActiveRevisions = value;
    }

    public boolean isWithDeletedRevisions(){
        return this.withDeletedContent;
    }

    public void setWithDeletedRevisions(boolean value){
        if ( value != this.withDeletedRevisions){
            this.hasAttributeChange = true;
        }
        this.withDeletedRevisions = value;
    }

    public boolean isWithStagingRevisions() {
        return withStagingRevisions;
    }

    public void setWithStagingRevisions(boolean withStagingRevisions) {
        this.withStagingRevisions = withStagingRevisions;
    }

    public boolean isApplyLanguageFiltering() {
        return applyLanguageFiltering;
    }

    public void setApplyLanguageFiltering(boolean applyLanguageFiltering) {
        this.applyLanguageFiltering = applyLanguageFiltering;
    }

    public boolean isSkipNotAvailablePageRevisions() {
        return skipNotAvailablePageRevisions;
    }

    public void setSkipNotAvailablePageRevisions(boolean skipNotAvailablePageRevisions) {
        this.skipNotAvailablePageRevisions = skipNotAvailablePageRevisions;
    }

    /**
     * Returns true if the entry state is inside the date range
     *
     * @param entryState
     * @return
     */
    protected boolean inDateRange(ContentObjectEntryState entryState){
        long entryStateDate = entryState.getVersionID()*1000L;
        return (this.fromRevisionDate==0 || this.fromRevisionDate <=entryStateDate)
                 && (this.toRevisionDate==0 || this.toRevisionDate>=entryStateDate);
    }

    /**
     *
     * @param rev
     * @return
     */
    protected void setRevisionTitle(RevisionEntrySet rev) {

       String title = "";
       try {
           ContentObject contentObject
               = (ContentObject) JahiaObject.getInstance(rev.getObjectKey());
           String langCode = "";
           Locale locale =  this.getEntryLoadRequest().getFirstLocale(true);
           if (locale != null ){
               langCode = locale.toString();
           }
           ContentObjectEntryState entryState =
                   //new ContentObjectEntryState(rev.getWorkflowState(),
                   new ContentObjectEntryState(ContentObjectEntryState.WORKFLOW_STATE_START_STAGING,
                   rev.getVersionID(),langCode);
           EntryLoadRequest loadRequest = new EntryLoadRequest(entryState);
           ContentDefinition definition =
                   ContentDefinition.getContentDefinitionInstance(
                   contentObject.getDefinitionKey(loadRequest));
           if ( definition != null ){
               title = definition.getTitle(contentObject,entryState);
           } else if ( contentObject instanceof ContentPage ){
               title = ((ContentPage)contentObject).getTitle(loadRequest);
           }
       } catch ( Exception t ){
           logger.debug("Exception retrieving content object title",t);
       }
       if ( title == null ){
           title = rev.getObjectKey().toString();
       }
       rev.setProperty(RevisionEntrySet.REVISION_TITLE,title);
    }

    /**
     *
     * @param revisionEntry
     * @param revisionEntrySet
     * @param revisions
     */
    protected void addRevision(RevisionEntry revisionEntry,
                               RevisionEntrySet revisionEntrySet,
                               List revisions){

        if ( revisionEntry.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
            ContentObject contentObject = null;
            try {
                contentObject = ContentObject.getContentObjectInstance(revisionEntrySet.getObjectKey());
                String value = "";
                JahiaField f = contentObject.getMetadataAsJahiaField(CoreMetadataConstant.LAST_CONTRIBUTOR,
                        Jahia.getThreadParamBean());
                if ( f != null ) {
                    value = f.getValue();
                }
                /*
                ContentField metadata = contentObject.getMetadata(CoreMetadataConstant.LAST_CONTRIBUTOR);
                if ( metadata != null ){
                    value = metadata.getValue(revisionEntry.getEntryState());
                }*/
                if ( value == null ){
                    value = "";
                }

                revisionEntrySet.setProperty(CoreMetadataConstant.LAST_CONTRIBUTOR,value);

                ContentField metadata = contentObject.getMetadata(CoreMetadataConstant.LAST_PUBLISHER);
                if ( metadata != null ) {
                    value = metadata.getValue(revisionEntry.getEntryState());
                }
                /*
                ContentField metadata = contentObject.getMetadata(CoreMetadataConstant.LAST_CONTRIBUTOR);
                if ( metadata != null ){
                    value = metadata.getValue(revisionEntry.getEntryState());
                }*/
                if ( value == null ){
                    value = "";
                }
                revisionEntrySet.setProperty(CoreMetadataConstant.LAST_PUBLISHER,value);
                f = contentObject.getMetadataAsJahiaField(CoreMetadataConstant.LAST_MODIFICATION_DATE,Jahia.getThreadParamBean());
                Calendar cal = Calendar.getInstance(tz);
                cal.setTime(new Date(System.currentTimeMillis()));
                value = String.valueOf(cal.getTimeInMillis());
                if ( f != null ){
                    try {
                        cal.setTimeInMillis(Long.parseLong(f.getObject().toString()));
                        value = String.valueOf(cal.getTimeInMillis());
                    } catch ( Exception t){
                    }
                }
                Long dateLong = new Long(cal.getTimeInMillis()/1000);
                revisionEntrySet.setVersionID(dateLong.intValue());
            } catch ( Exception t ){
                logger.debug("Exception adding metadatas information from content object :"
                        + revisionEntry.getObjectKey(),t);
            }
            RevisionEntrySet resItem = null;
            for ( int i=0; i<revisionsList.size(); i++ ){
                resItem = (RevisionEntrySet)revisionsList.get(i);
                if ( resItem.getWorkflowState()>EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                    if ( resItem.getVersionID()<revisionEntrySet.getVersionID() ){
                        resItem.setVersionID(revisionEntrySet.getVersionID());
                        resItem.setProperty(CoreMetadataConstant.LAST_CONTRIBUTOR,
                            revisionEntrySet.getLastContributor());
                    }
                    return;
                }
            }
        }

        int revIndex = revisions.indexOf(revisionEntrySet);
        RevisionEntrySet res = null;
        if ( revIndex == -1 ){
            revIndex = revisionsList.indexOf(revisionEntrySet);
            if ( revIndex != -1 ){
                res = (RevisionEntrySet)revisionsList.get(revIndex);
            }
        } else {
            res = (RevisionEntrySet)revisions.get(revIndex);
        }
        if ( res != null ){
            res.addRevision(revisionEntry);
        } else {
            revisionEntrySet.addRevision(revisionEntry);
            revisions.add(revisionEntrySet);
            if ( revisionEntrySet.getWorkflowState()<EntryLoadRequest.STAGING_WORKFLOW_STATE ){
                ContentObject contentObject = null;
                try {
                    contentObject = ContentObject.getContentObjectInstance(revisionEntrySet.getObjectKey());
                    String value = null;
                    ContentField metadata = contentObject.getMetadata(CoreMetadataConstant.LAST_CONTRIBUTOR);
                    EntryLoadRequest loadRequest = new EntryLoadRequest(revisionEntry.getEntryState());
                    if ( metadata != null ){
                        value = metadata.getValue(Jahia.getThreadParamBean(),loadRequest);
                    }
                    if ( value == null ){
                        value = JahiaResourceBundle.getEngineResource( "org.jahia.engines.version.author.unknown",
                        Jahia.getThreadParamBean(),Jahia.getThreadParamBean().getLocale(),"Unknown");
                    }
                    revisionEntrySet.setProperty(CoreMetadataConstant.LAST_CONTRIBUTOR,value);
                } catch ( Exception t ){
                    logger.debug("Exception adding metadatas information from content object :"
                            + revisionEntry.getObjectKey(),t);
                }
            }
        }
    }

    /**
     *
     * @param contentPage
     * @param versionId
     * @return
     * @throws JahiaException
     */
    protected boolean isPageDeletedOrDoesNotExist(ContentPage contentPage, int versionId) throws JahiaException {
        Boolean result = (Boolean)notAvailablePageRevisionsCache.get(new Integer(contentPage.getID()));
        if (result == null){
            result = new Boolean(contentPage.isDeletedOrDoesNotExist(versionId));
            notAvailablePageRevisionsCache.put(new Integer(contentPage.getID()),result);
        }
        return result.booleanValue();
    }


    public void sort(Comparator comparator){
        Object[] array = this.getRevisions().toArray();
        Arrays.sort(array,comparator);
        this.revisionsList = new ArrayList(Arrays.asList(array));
    }
}

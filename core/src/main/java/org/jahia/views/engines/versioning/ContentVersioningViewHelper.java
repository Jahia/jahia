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
package org.jahia.views.engines.versioning;

import org.jahia.content.ContentObject;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: Helper for Pages Versioning Engine </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class ContentVersioningViewHelper implements Serializable {

    public static final int UNDO_STAGING_OPERATION = 1;
    public static final int RESTORE_ARCHIVE_CONTENT_OPERATION = 2;
    public static final int UNDELETE_OPERATION = 3;

    /**
     * Restore content only
     */
    public static final int RESTORE_CONTENT = 1;

    /**
     * Restore metadata only
     */
    public static final int RESTORE_METADATA = 2;

    /**
     * Restore content and metadata
     */
    public static final int RESTORE_CONTENT_AND_METADATA = RESTORE_CONTENT | RESTORE_METADATA;


    /**
     * The operation type
     */
    private int operationType = ContentVersioningViewHelper.RESTORE_ARCHIVE_CONTENT_OPERATION;

    /**
     * if either Content and Metadata should be restored or only Content.
     */
    private int restoreMode = RESTORE_CONTENT;

    /**
     * The selected container *
     */
    private ContentObject contentObject;

    /**
     * free restore date
     */
    private CalendarHandler restoreDateCalendar;

    /**
     * lower revision date limit *
     */
    private CalendarHandler fromRevisionDateCalendar;

    /**
     * upper revision date limit *
     */
    private CalendarHandler toRevisionDateCalendar;

    /**
     * The selected revisionEntrySet *
     */
    private RevisionEntrySet revisionEntrySet;

    private int sortOrder = RevisionsComparator.DESC_ORDER;

    private int sortAttribute = RevisionsComparator.SORT_BY_DATE;

    private List languagesToRestore;

    /**
     * nb max of revisions to display *
     */
    private int nbMaxOfRevisions = -1;

    /**
     * the type of revisions *
     */
    private int typeOfRevisions = 0;

    /**
     * content revisions, metadata revisions or both revisions
     */
    private int contentOrMetadataRevisions = ContentTreeRevisionsVisitor.CONTENT_REVISION_ENTRY;

    /**
     * If true, ignore range
     */
    private boolean displayAllRevisions = true;

    private List languagesSettings;

    private ContainerCompareBean containerCompareBean;

    /**
     * The Revisions Handler
     */
    private ContentTreeRevisionsVisitor contentTreeRevisionsVisitor;

    /**
     *
     */
    private boolean exactRestore;

    /**
     * @param contentObject
     * @param restoreDateCalendarHandler
     */
    protected ContentVersioningViewHelper ( ContentObject contentObject,
                                            CalendarHandler restoreDateCalendarHandler)
    throws JahiaException {
        this.contentObject = contentObject;
        this.restoreDateCalendar = restoreDateCalendarHandler;
        this.languagesToRestore = new ArrayList();
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(contentObject.getSiteID());
        this.languagesSettings = site.getLanguageSettingsAsLocales(true);
    }

    /**
     * @param contentObject
     * @param restoreDateCalendarHandler
     */
    public static ContentVersioningViewHelper getInstance( ContentObject contentObject,
                                                           CalendarHandler restoreDateCalendarHandler)
    throws JahiaException {

        ContentVersioningViewHelper viewHelper = null;
        if ( contentObject instanceof ContentPage ){
            viewHelper = new PagesVersioningViewHelper((ContentPage)contentObject,restoreDateCalendarHandler);
        } else if ( contentObject instanceof ContentContainer ) {
            viewHelper = new ContainerVersioningViewHelper(contentObject,restoreDateCalendarHandler);
        } else if ( contentObject instanceof ContentContainerList ) {
            viewHelper = new ContainerListVersioningViewHelper(contentObject,restoreDateCalendarHandler);
        } else {
            viewHelper = new ContentVersioningViewHelper(contentObject,restoreDateCalendarHandler);
        }
        return viewHelper;
    }

    /**
     * Return the internal ContentTreeRevisionsVisitor,
     * Create it if not exist
     */
    public ContentTreeRevisionsVisitor getContentTreeRevisionsVisitor(
            final JahiaUser user,
            final EntryLoadRequest loadRequest,
            final String operationMode) {

        if (this.contentTreeRevisionsVisitor == null) {
            if ( contentObject instanceof ContentPage ){
                this.contentTreeRevisionsVisitor =
                    new PageRevisionsCompositor(contentObject, user, loadRequest, operationMode);
            } else if ( contentObject instanceof ContentContainer ){
                this.contentTreeRevisionsVisitor =
                    new ContainerRevisionsCompositor(contentObject, user, loadRequest, operationMode);
            } else {
                //@todo with container list, content page
            }
        } else {
            this.contentTreeRevisionsVisitor.setUser(user);
            this.contentTreeRevisionsVisitor.setEntryLoadRequest(loadRequest);
            this.contentTreeRevisionsVisitor.setOperationMode(operationMode);
        }
        return this.contentTreeRevisionsVisitor;
    }

    /**
     * Return the content tree revisions visitor, can be null if not created !
     *
     */
    public ContentTreeRevisionsVisitor getContentTreeRevisionsVisitor() {
        return this.contentTreeRevisionsVisitor;
    }

    /**
     * If it's a Restore operation, Undo operation or Undelete operation
     * 
     * @return
     */
    public int getOperationType() {
        return this.operationType;
    }

    public void setOperationType(final int type) {
        this.operationType = type;
    }

    /**
     * If either Content and Metadata should be restored or only Content
     *
     * @return
     */
    public int getRestoreMode() {
        return restoreMode;
    }

    public void setRestoreMode(int restoreMode) {
        this.restoreMode = restoreMode;
    }

    public void setRevisionEntrySet(final RevisionEntrySet revisionEntrySet) {
        this.revisionEntrySet = revisionEntrySet;
    }

    public RevisionEntrySet getRevisionEntrySet() {
        return this.revisionEntrySet;
    }

    public int getInvertSortOrder() {
        if (this.getSortOrder() == RevisionsComparator.ASC_ORDER) {
            return RevisionsComparator.DESC_ORDER;
        }
        return RevisionsComparator.ASC_ORDER;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(final int value) {
        this.sortOrder = value;
    }

    public int getSortAttribute() {
        return this.sortAttribute;
    }

    public void setSortAttribute(final int value) {
        this.sortAttribute = value;
    }

    public ContentObject getContentObject() {
        return contentObject;
    }

    public void setContentObject(ContentObject contentObject) {
        this.contentObject = contentObject;
    }

    public CalendarHandler getRestoreDateCalendar() {
        return restoreDateCalendar;
    }

    public void setRestoreDateCalendar(CalendarHandler restoreDateCalendar) {
        this.restoreDateCalendar = restoreDateCalendar;
    }

    public CalendarHandler getFromRevisionDateCalendar() {
        return fromRevisionDateCalendar;
    }

    public void setFromRevisionDateCalendar(CalendarHandler fromRevisionDateCalendar) {
        this.fromRevisionDateCalendar = fromRevisionDateCalendar;
    }

    public CalendarHandler getToRevisionDateCalendar() {
        return toRevisionDateCalendar;
    }

    public void setToRevisionDateCalendar(CalendarHandler toRevisionDateCalendar) {
        this.toRevisionDateCalendar = toRevisionDateCalendar;
    }

    public Integer getNbMaxOfRevisions() {
        return new Integer(this.nbMaxOfRevisions);
    }

    public String getNbMaxOfRevisionsAsStr() {
        return String.valueOf(this.nbMaxOfRevisions);
    }

    public void setNbMaxOfRevisions(final int value) {
        this.nbMaxOfRevisions = value;
    }

    public Integer getTypeOfRevisions() {
        return new Integer(this.typeOfRevisions);
    }

    public String getTypeOfRevisionsAsStr() {
        return String.valueOf(this.typeOfRevisions);
    }

    public void setTypeOfRevisions(final int value) {
        this.typeOfRevisions = value;
    }

    public int getContentOrMetadataRevisions() {
        return contentOrMetadataRevisions;
    }

    public String getContentOrMetadataRevisionsAsString() {
        return String.valueOf(contentOrMetadataRevisions);
    }

    public void setContentOrMetadataRevisions(int contentOrMetadataRevisions) {
        this.contentOrMetadataRevisions = contentOrMetadataRevisions;
    }

    public boolean isDisplayAllRevisions() {
        return displayAllRevisions;
    }

    public void setDisplayAllRevisions(boolean displayAllRevisions) {
        this.displayAllRevisions = displayAllRevisions;
    }

    public boolean isRestoringPage(){
        return ( this.contentObject instanceof ContentPage );
    }

    public boolean isRestoringContainer(){
        return ( this.contentObject instanceof ContentContainer );
    }

    public boolean isRestoringContainerList(){
        return ( this.contentObject instanceof ContentContainerList );
    }

    public List getLanguagesToRestore() {
        return languagesToRestore;
    }

    public void setLanguagesToRestore(List languagesToRestore) {
        this.languagesToRestore = languagesToRestore;
    }

    public List getLanguagesSettings() {
        return languagesSettings;
    }

    public void setLanguagesSettings(List languagesSettings) {
        this.languagesSettings = languagesSettings;
    }

    public ContainerCompareBean getContainerCompareBean() {
        return containerCompareBean;
    }

    public void setContainerCompareBean(ContainerCompareBean containerCompareBean) {
        this.containerCompareBean = containerCompareBean;
    }

    /**
     * If true, content that does not exist at the restore date will be deleted
     * If false, only content that have versioned entries at the restore date will be restored
     * 
     * @return
     */
    public boolean isExactRestore() {
        return exactRestore;
    }

    public void setExactRestore(boolean exactRestore) {
        this.exactRestore = exactRestore;
    }

}

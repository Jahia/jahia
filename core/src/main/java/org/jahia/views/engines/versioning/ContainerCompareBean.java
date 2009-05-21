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
package org.jahia.views.engines.versioning;

import org.apache.commons.lang.StringEscapeUtils;
import org.jahia.content.ContentMetadataFacade;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.ContainerEditView;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.*;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 12 sept. 2006
 * Time: 11:53:15
 * To change this template use File | Settings | File Templates.
 */
public class ContainerCompareBean {

    public static final int DISPLAY_OLD_VALUE = 1;
    public static final int DISPLAY_NEW_VALUE = 2;
    public static final int DISPLAY_MERGED_DIFF_VALUE = 3;

    private List fields;
    private List metadatas;
    private RevisionEntrySet oldRevision;
    private RevisionEntrySet newRevision;
    private int displayMode = DISPLAY_MERGED_DIFF_VALUE;

    /**
     *
     */
    public ContainerCompareBean(){
        fields = new ArrayList();
        metadatas = new ArrayList();
    }

    /**
     *
     * @param displayMode
     * @param fields
     * @param metadatas
     * @param newRevision
     * @param oldRevision
     */
    public ContainerCompareBean(int displayMode,
                                List fields,
                                List metadatas,
                                RevisionEntrySet newRevision,
                                RevisionEntrySet oldRevision) {
        this.displayMode = displayMode;
        this.fields = fields;
        this.metadatas = metadatas;
        this.newRevision = newRevision;
        this.oldRevision = oldRevision;
    }

    public int getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
    }

    public List getFields() {
        return fields;
    }

    public void setFields(List fields) {
        this.fields = fields;
    }

    public List getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(List metadatas) {
        this.metadatas = metadatas;
    }

    public RevisionEntrySet getNewRevision() {
        return newRevision;
    }

    public void setNewRevision(RevisionEntrySet newRevision) {
        this.newRevision = newRevision;
    }

    public RevisionEntrySet getOldRevision() {
        return oldRevision;
    }

    public void setOldRevision(RevisionEntrySet oldRevision) {
        this.oldRevision = oldRevision;
    }

    public boolean isCompareMode() {
        return (this.displayMode == DISPLAY_MERGED_DIFF_VALUE);
    }

    public RevisionEntrySet getActiveRevision() {
        if ( this.displayMode == DISPLAY_OLD_VALUE ){
            return this.oldRevision;
        } else {
            return this.newRevision;
        }
    }

    /**
     *
     * @param contentContainer
     * @param context
     * @param version1
     * @param version2
     * @param displayMode
     * @param withDifferenceOnly
     * @param languageCode
     * @param revisions
     * @return
     * @throws JahiaException
     */
    public static ContainerCompareBean getInstance (
                ContentContainer contentContainer,
                ProcessingContext context,
                String version1,
                String version2,
                int displayMode,
                boolean withDifferenceOnly,
                String languageCode,
                List revisions) throws JahiaException {

        ContainerCompareBean containerCompareBean = new ContainerCompareBean();
        containerCompareBean.setDisplayMode(displayMode);

        RevisionEntrySet revSet = null;
        RevisionEntrySet revSet1 = null;
        RevisionEntrySet revSet2 = null;
        RevisionEntrySet liveRevSet = null;
        RevisionEntrySet stagedRevSet = null;

        String version1WfState = getToken(2, version1);
        String version2WfState = getToken(2, version2);

        if ( revisions == null ){
            revisions = getContentRevisions(contentContainer,
                    context,context.getUser(),
                    context.getOperationMode(),0,0,0,0,0,
                    RevisionEntrySetComparator.SORT_BY_DATE,RevisionEntrySetComparator.DESC_ORDER,null);
        }

        Iterator revIt = revisions.iterator();
        while ( revIt.hasNext() ){
            revSet = (RevisionEntrySet)revIt.next();
            if ( revSet.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                liveRevSet = revSet;
            } else if ( revSet.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
                stagedRevSet = revSet;
            }
            if ( revSet.toString().equals(version1) ||
                (revSet.getWorkflowState()> EntryLoadRequest.ACTIVE_WORKFLOW_STATE && "2".equals(version1WfState)) ){
                revSet1 = revSet;
            }
            if ( revSet.toString().equals(version2) ||
                (revSet.getWorkflowState()> EntryLoadRequest.ACTIVE_WORKFLOW_STATE && "2".equals(version2WfState)) ){
                revSet2 = revSet;
            }
            if ( revSet1 != null && revSet2 != null ){
                break;
            }
        }
        if ( version2 == null ){
            if ( stagedRevSet != null ){
                revSet2 = stagedRevSet;
            } else {
                revSet2 = liveRevSet;
            }
        }
        if ( revSet1 != null && revSet2 != null ){
            if ( revSet1.getVersionID()>revSet2.getVersionID() ){
                RevisionEntrySet tempRevSet = revSet1;
                revSet1 = revSet2;
                revSet2 = tempRevSet;
            }
        }

        containerCompareBean.setOldRevision(revSet1);
        containerCompareBean.setNewRevision(revSet2);

        List locales = new ArrayList();
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        EntryLoadRequest entryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,locales);
        JahiaContainer jahiaContainer = contentContainer.getJahiaContainer(context,entryLoadRequest);
        JahiaContainer theContainer = null;
        if ( jahiaContainer == null ){
            entryLoadRequest =
                    new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                    ServicesRegistry.getInstance().getJahiaVersionService().getCurrentVersionID(),
                    locales);
            entryLoadRequest.setCompareMode(true);
            entryLoadRequest.setWithDeleted(true);
            entryLoadRequest.setWithMarkedForDeletion(true);
            theContainer = contentContainer.getJahiaContainer(context,entryLoadRequest);
        } else {
            JahiaContentContainerFacade jahiaContentContainerFacade
                    = new JahiaContentContainerFacade(jahiaContainer,
                    context.getPage(),
                    LoadFlags.ALL,
                    context,
                    locales,
                    false, true, false);

            theContainer = jahiaContentContainerFacade.getContainer(entryLoadRequest,true);
        }

        // create the edit view
        Map ctnListFieldAcls = JahiaEngineTools.getCtnListFieldAclMap(theContainer, context);
        Set visibleFields = JahiaEngineTools.getCtnListVisibleFields(theContainer, context.getUser(), ctnListFieldAcls);
        ContainerEditView editView = ContainerEditView.getInstance(theContainer, context, visibleFields);

        List fieldVersionCompares = new ArrayList();

        JahiaField jahiaField = null;
        JahiaFieldVersionCompare fieldVersionCompare = null;
        String fieldName = null;
        for ( int i=0; i<editView.getFields().size(); i++ ){
            fieldName = (String)editView.getFields().get(i);
            jahiaField = theContainer.getFieldByName(fieldName);
            if ( jahiaField!= null ){
                fieldVersionCompare = JahiaFieldVersionCompare
                        .getInstance(ContentField.getField(jahiaField.getID()),revSet1,revSet2,languageCode);
                if ( fieldVersionCompare != null ){
                    fieldVersionCompare.setDisplayMode(displayMode);
                    if ( displayMode == DISPLAY_MERGED_DIFF_VALUE ){
                        if ( !withDifferenceOnly || fieldVersionCompare.hasDifference() ){
                            fieldVersionCompares.add(fieldVersionCompare);
                        }
                    } else {
                        fieldVersionCompares.add(fieldVersionCompare);
                    }
                }
            }
        }
        containerCompareBean.setFields(fieldVersionCompares);

        // create metadata diff views
        if (context != null) {
            // ensure to create metadata for this content object
            ContentMetadataFacade metadataFacade = new
                    ContentMetadataFacade(contentContainer.getObjectKey(),LoadFlags.ALL,context,false,false,false);
            theContainer = metadataFacade.getContainer(entryLoadRequest,true);

            // create the edit view
            ctnListFieldAcls = JahiaEngineTools.getCtnListFieldAclMap(theContainer, context);
            visibleFields = JahiaEngineTools.getCtnListVisibleFields(theContainer, context.getUser(), ctnListFieldAcls);
            editView = ContainerEditView.getInstance(theContainer, context, visibleFields);

            fieldVersionCompares = new ArrayList();

            jahiaField = null;
            fieldVersionCompare = null;
            fieldName = null;
            for ( int i=0; i<editView.getFields().size(); i++ ){
                fieldName = (String)editView.getFields().get(i);
                jahiaField = theContainer.getFieldByName(fieldName);
                if ( jahiaField!= null &&
                        !CoreMetadataConstant.notRestorableMetadatas.contains(jahiaField.getDefinition().getName())){
                    fieldVersionCompare = JahiaFieldVersionCompare
                            .getInstance(ContentField.getField(jahiaField.getID()),revSet1,revSet2,languageCode);
                    if ( fieldVersionCompare != null ){
                        fieldVersionCompare.setDisplayMode(displayMode);
                        if ( displayMode == DISPLAY_MERGED_DIFF_VALUE ){
                            if ( !withDifferenceOnly || fieldVersionCompare.hasDifference() ){
                                fieldVersionCompares.add(fieldVersionCompare);
                            }
                        } else {
                            fieldVersionCompares.add(fieldVersionCompare);
                        }
                    }
                }
            }
            containerCompareBean.setMetadatas(fieldVersionCompares);
        }


        return containerCompareBean;
    }

    /**
     *
     * @param contentObject
     * @param context
     * @param user
     * @param operationMode
     * @param toDate
     * @param fromDate
     * @param pageLevel
     * @param clientTimeZoneDiff
     * @param clientTimeDiff
     * @param revisionsVisitor
     * @return
     * @throws JahiaException
     */
    public static List getContentRevisions( ContentObject contentObject,
                                            ProcessingContext context,
                                            JahiaUser user,
                                            String operationMode,
                                            long toDate, long fromDate,
                                            int pageLevel,
                                            int clientTimeZoneDiff,
                                            int clientTimeDiff,
                                            int sortAttribute,
                                            int sortOrder,
                                            ContentTreeRevisionsVisitor revisionsVisitor)
    throws JahiaException {

        if ( toDate == 0 ){
            toDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
        toDate += clientTimeZoneDiff + clientTimeDiff;

        EntryLoadRequest loadRequest =
            new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                    (int) (toDate / 1000),
                    context.getEntryLoadRequest().getLocales());

        // prepare Revisions List
        if (revisionsVisitor == null) {
            if ( contentObject instanceof ContentPage ){
                revisionsVisitor =
                    new PageRevisionsCompositor(contentObject, user, loadRequest, operationMode);
            } else if ( contentObject instanceof ContentContainer ){
                revisionsVisitor =
                    new ContainerRevisionsCompositor(contentObject, user, loadRequest, operationMode);
            } else {
                //@todo with container list, content page
            }
        } else {
            revisionsVisitor.setUser(user);
            revisionsVisitor.setEntryLoadRequest(loadRequest);
            revisionsVisitor.setOperationMode(operationMode);
        }

        revisionsVisitor.setWithDeletedContent(true);
        if ( contentObject instanceof ContentPage ){
            if (pageLevel > 0) {
                pageLevel -= 1;
            }
            revisionsVisitor.setDescendingPageLevel(pageLevel);
        } else if ( contentObject instanceof ContentContainer ){
            revisionsVisitor.setWithStagingRevisions(true);
        }
        if ( fromDate > 0 ){
            fromDate += clientTimeZoneDiff + clientTimeDiff;
        }
        revisionsVisitor.setFromRevisionDate(fromDate);
        revisionsVisitor.setToRevisionDate(toDate);

        revisionsVisitor.loadRevisions(false);
        revisionsVisitor.sortRevisions(context.getLocale().toString(),sortAttribute, sortOrder,false);

        List revisions = revisionsVisitor.getRevisions();
        List filteredRevisions = new ArrayList();
        int size = revisions.size();
        // remove deleted revisions, we don't want to restore at deleted revision
        RevisionEntrySet revEntrySet;
        if ( (contentObject instanceof ContentPage || contentObject instanceof ContentContainer )) {
            for (int i = 0; i < size; i++) {
                revEntrySet = (RevisionEntrySet) revisions.get(i);
                if ((revEntrySet.getWorkflowState() !=
                        EntryLoadRequest.DELETED_WORKFLOW_STATE)) {
                    filteredRevisions.add(revEntrySet);
                }
            }
        } else {
            filteredRevisions = revisions;
        }
        if ( filteredRevisions == null ){
            filteredRevisions = new ArrayList();
        }
        return filteredRevisions;
    }

    public String toJSON(ProcessingContext context) throws JahiaException {


        StringBuffer datas = new StringBuffer("{");
        datas.append(getJSONTabView(this.getFields().iterator(),"fieldDiffs",context));
        datas.append(",");
        datas.append(getJSONTabView(this.getMetadatas().iterator(),"metadataDiffs",context));
        datas.append("}");
        return datas.toString();
    }

    private String getJSONTabView(Iterator fieldsIterator, String tabName, ProcessingContext context)
    throws JahiaException {

        final int pageDefID = context.getPage().getPageTemplateID();

        StringBuffer datas = new StringBuffer("\"").append(tabName).append("\":[");
        JahiaFieldVersionCompare fvc = null;
        ContentField contentField = null;
        JahiaFieldDefinition def = null;
        while ( fieldsIterator.hasNext() ){
          fvc = (JahiaFieldVersionCompare)fieldsIterator.next();
          fvc.getCompareHandler().highLightDiff();
          contentField = ContentField.getField(fvc.getFieldId());
          def = (JahiaFieldDefinition)JahiaFieldDefinition.getChildInstance(String.valueOf(contentField.getFieldDefID()));
          datas.append("{\"fieldId\":").append("\"").append(fvc.getFieldId()).append("\",");
          datas.append("\"title\":").append("\"").append(def.getTitle(
                  LanguageCodeConverters.languageCodeToLocale(fvc.getLanguageCode()))).append("\",");
          datas.append("\"type\":").append("\"").append(contentField.getType()).append("\",");
          datas.append("\"icon\":").append("\"").append(FieldTypes.getIconClassName(contentField.getType(), false))
                  .append("\",");
          datas.append("\"displayMode\":").append("\"").append(fvc.getDisplayMode()).append("\",");
          datas.append("\"mergedDiffValue\":").append("'")
                  .append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getMergedDiffText())).append("',");
          datas.append("\"oldValue\":").append("'")
                  .append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getOldText())).append("',");
          datas.append("\"newValue\":").append("'")
                  .append(StringEscapeUtils.escapeJavaScript(fvc.getCompareHandler().getNewText())).append("'}");
          if ( fieldsIterator.hasNext() ){
            datas.append(",");
          }
        }
        datas.append("]");
        return datas.toString();
    }

    static protected String getToken(int index, String revisionEntrySetKey){
        try {
            String[] tokens = JahiaTools.getTokens(revisionEntrySetKey,"_");
            return tokens[index];
        } catch ( Exception t){
        }
        return null;
    }
}

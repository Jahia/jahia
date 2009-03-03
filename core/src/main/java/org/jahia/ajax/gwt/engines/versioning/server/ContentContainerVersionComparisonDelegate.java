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

package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionComparisonData;
import org.jahia.ajax.gwt.engines.versioning.server.VersionComparisonUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaField;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaFieldGroup;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.ContentTreeRevisionsVisitor;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.RevisionEntrySet;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.ContainerCompareBean;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;
import org.jahia.views.engines.versioning.JahiaFieldVersionCompare;
import org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 30 avr. 2008
 * Time: 11:50:44
 * To change this template use File | Settings | File Templates.
 */
public class ContentContainerVersionComparisonDelegate implements VersionComparisonDelegate {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentContainerVersionComparisonDelegate.class);

    public GWTJahiaVersionComparisonData getVersionComparisonData(VersionComparisonContext context)
    throws JahiaException {

        final HttpServletRequest request = context.getJParams().getRequest();
        GWTJahiaVersionComparisonData data = null;
        try {

            // engine view Helper
            ContentVersioningViewHelper engineViewHelper = null;

            // try to retrieve engine data from session
            engineViewHelper =
                    (ContentVersioningViewHelper) request.getSession()
                            .getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

            final ContentContainer contentObject;

            if (engineViewHelper == null) {
                // Prepage a new engine view helper
                String objectKey = context.getVersionableUUID();
                contentObject = (ContentContainer)ContentObject
                        .getContentObjectInstance(ObjectKey.getInstance(objectKey));
                CalendarHandler cal = this.getCalHandler("restoreDateCalendar", 0,
                        context.getJParams());
                engineViewHelper = ContentVersioningViewHelper.getInstance(contentObject, cal);
                cal = this.getCalHandler("fromRevisionDateCalendar", 0,
                        context.getJParams());
                engineViewHelper.setFromRevisionDateCalendar(cal);
                cal = this.getCalHandler("toRevisionDateCalendar", 0,
                        context.getJParams());
                engineViewHelper.setToRevisionDateCalendar(cal);

                // store engine data in session
                request.getSession().setAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER,
                        engineViewHelper);
            } else {
                contentObject = (ContentContainer)engineViewHelper.getContentObject();
            }

            // check permission
            logger.info("Logged in User :" + context.getUser().getUsername());
            if (!contentObject.checkWriteAccess(context.getUser())) {
                if (!contentObject.checkReadAccess(context.getUser())) {
                    throw new JahiaForbiddenAccessException();
                }
            }
            List revisions = (List) request.getSession()
                            .getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER + ".revisions");

            if (revisions == null ){
                revisions = loadRevisions(engineViewHelper,context);
                request.getSession().setAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER + ".revisions",revisions);
            }
            Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
            String languageCode = context.getJParams().getLocale().toString();
            EngineLanguageHelper elh = null;
            if (engineMap != null) {
                elh = (EngineLanguageHelper)
                        engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                if (elh != null) {
                    languageCode = elh.getCurrentLanguageCode();
                }
            }
            ContainerCompareBean containerCompareBean = getContainerCompareBean(engineViewHelper, context, languageCode);
            data = getVersionComparisonDataFrom(containerCompareBean,context, languageCode);

        } catch (Exception e) {
            throw new JahiaException("Exception occured initializing engine's objects",
                    "Exception occured initializing engine's objects",
                    JahiaException.ENGINE_ERROR,
                    JahiaException.ENGINE_ERROR, e);
        }
        return data;
    }

    /**
     *
     * @param versViewHelper
     * @param context
     * @return
     * @throws JahiaException
     */
    protected List loadRevisions(ContentVersioningViewHelper versViewHelper, VersionComparisonContext context)
            throws JahiaException {

        long toDate = versViewHelper.getToRevisionDateCalendar().getDateLong().longValue();
        if (toDate == 0) {
            toDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
        toDate += versViewHelper.getToRevisionDateCalendar().getTimeZoneOffSet().longValue()
                + versViewHelper.getToRevisionDateCalendar().getServerClientTimeDiff().longValue();

        EntryLoadRequest loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                        (int) (toDate / 1000),
                        context.getJParams().getEntryLoadRequest().getLocales());

        // prepare Revisions List
        ContentTreeRevisionsVisitor revisionsVisitor =
                versViewHelper.getContentTreeRevisionsVisitor(
                        context.getUser(),loadRequest, ProcessingContext.EDIT);

        revisionsVisitor.setWithDeletedContent(true);
        if (versViewHelper.isRestoringPage()) {
            int pageLevel = ((PagesVersioningViewHelper) versViewHelper).getPageLevel().intValue();
            if (pageLevel > 0) {
                pageLevel -= 1;
            }
            revisionsVisitor.setDescendingPageLevel(pageLevel);
        } else if (versViewHelper.isRestoringContainer()) {
            revisionsVisitor.setWithStagingRevisions(true);
        }
        long fromDate = versViewHelper.getFromRevisionDateCalendar().getDateLong().longValue();
        if (fromDate > 0) {
            fromDate += versViewHelper.getFromRevisionDateCalendar().getTimeZoneOffSet().longValue()
                    + versViewHelper.getFromRevisionDateCalendar().getServerClientTimeDiff().longValue();
        }
        revisionsVisitor.setFromRevisionDate(fromDate);
        revisionsVisitor.setToRevisionDate(toDate);

        revisionsVisitor.loadRevisions(false);
        revisionsVisitor.sortRevisions(
                context.getJParams().getLocale().toString(),
                versViewHelper.getSortAttribute(),
                versViewHelper.getSortOrder(), false);

        List revisions = revisionsVisitor.getRevisions();
        List filteredRevisions = new ArrayList();
        int size = revisions.size();
        // remove deleted revisions, we don't want to restore at deleted revision
        RevisionEntrySet revEntrySet;
        if ((versViewHelper.isRestoringPage() || versViewHelper.isRestoringContainer())) {
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
        if (filteredRevisions == null) {
            filteredRevisions = new ArrayList();
        }
        return filteredRevisions;
    }

    protected CalendarHandler getCalHandler(String calIdentifier,
                                            long initialDate,
                                            ProcessingContext jParams) {
        Long date = new Long(initialDate);
        CalendarHandler calHandler =
                new CalendarHandler(jParams.settings().getJahiaEnginesHttpPath(),
                        calIdentifier,
                        CalendarHandler.DEFAULT_DATE_FORMAT,
                        date,
                        jParams.getLocale(),
                        new Long(0));
        return calHandler;
    }

    /**
     *
     * @param versViewHelper
     * @param context
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    protected ContainerCompareBean getContainerCompareBean( ContentVersioningViewHelper versViewHelper,
                                                         VersionComparisonContext context,
                                                         String languageCode) throws JahiaException {
        List revisionsList  = versViewHelper.getContentTreeRevisionsVisitor().getRevisions();
        return ContainerCompareBean
                .getInstance((ContentContainer)versViewHelper.getContentObject(),
                        context.getJParams(),
                        context.getVersion1(),context.getVersion2(),
                        ContainerCompareBean.DISPLAY_MERGED_DIFF_VALUE,
                        true,languageCode,revisionsList);
    }

    protected GWTJahiaVersionComparisonData getVersionComparisonDataFrom(ContainerCompareBean containerCompareBean,
                                                                 VersionComparisonContext context, String languageCode){

        Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);

        if (containerCompareBean == null){
            return null;
        } else {
            GWTJahiaVersion version1 = getVersionFromRevisionEntrySet(containerCompareBean.getOldRevision(),context
                    ,languageCode);
            GWTJahiaVersion version2 = getVersionFromRevisionEntrySet(containerCompareBean.getNewRevision(),context
                    ,languageCode);
            List fieldGroups = new ArrayList();
            addFieldGroups(fieldGroups,containerCompareBean.getFields(),"Fields",context,languageCode);
            addFieldGroups(fieldGroups,containerCompareBean.getMetadatas(),"Metadatas",context,languageCode);

            String titleAssert = VersionComparisonUtils.getTitleAssert(version1.getReadableName(),
                version2.getReadableName(),context.getVersionableUUID(),context.getJParams(),locale);

            String addedDiffLegend = VersionComparisonUtils.getAddedDiffLegendAssert(context.getJParams(),locale);

            String removedDiffLegend = VersionComparisonUtils.getRemovedDiffLegendAssert(context.getJParams(),locale);

            String changedDiffLegend = VersionComparisonUtils.getChangedDiffLegendAssert(context.getJParams(),locale);

            GWTJahiaVersionComparisonData data = new GWTJahiaVersionComparisonData(titleAssert,version1,version2,fieldGroups);
            data.setAddedDiffLegend(addedDiffLegend);
            data.setRemovedDiffLegend(removedDiffLegend);
            data.setChangedDiffLegend(changedDiffLegend);

            List versionRowDataHeadLabels = new ArrayList();
            versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.version",
                    context.getJParams().getLocale(),"Version"));
            versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.workflowStatus",
                    context.getJParams().getLocale(),"Workflow Status"));
            versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.author",
                    context.getJParams().getLocale(),"Author"));
            versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.dateOfRevision",
                    context.getJParams().getLocale(),"Date of revision"));
            versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.language",
                    context.getJParams().getLocale(),"Language"));
            data.setVersionRowDataHeadLabels((String[])versionRowDataHeadLabels.toArray(new String[]{}));
            return data;
        }
    }

    protected void addFieldGroups(List fieldGroups, List fields, String groupName, VersionComparisonContext context,
                                  String languageCode){
        if (fields !=null){
            JahiaFieldVersionCompare fieldForComparison = null;
            GWTJahiaFieldGroup fieldGroup = new GWTJahiaFieldGroup();
            fieldGroup.setGroupName(groupName);
            List compareFields = new ArrayList();
            ContentField contentField = null;
            JahiaFieldDefinition fieldDef = null;
            String fieldTitle = null;
            int fieldType = 0;
            int pageDefId = 0;
            String icon = "";
            for (Iterator it = fields.iterator(); it.hasNext();){
                fieldForComparison = (JahiaFieldVersionCompare)it.next();
                String originalValue = "";
                String newValue = "";
                String mergedDiffValue = "";
                if (fieldForComparison.getCompareHandler() !=null){
                    fieldForComparison.getCompareHandler().highLightDiff();
                    originalValue = fieldForComparison.getCompareHandler().getOldText();
                    newValue = fieldForComparison.getCompareHandler().getNewText();
                    mergedDiffValue = fieldForComparison.getCompareHandler().getMergedDiffText();
                }
                try {
                    contentField = ContentField.getField(fieldForComparison.getFieldId());
                    fieldDef = (JahiaFieldDefinition)JahiaFieldDefinition.getChildInstance(
                            String.valueOf(contentField.getFieldDefID()));
                } catch ( Exception t ){
                    logger.debug("Error retrieving ContentField for Versioning Comparison",t);
                    continue;
                }
                fieldTitle =  fieldDef.getName();
                fieldType = contentField.getType();
                icon = FieldTypes.getIconClassName(contentField.getType(), false);
                if ( context.getJParams().getPage() != null ){
                    pageDefId = context.getJParams().getPage().getPageTemplateID();
                    fieldTitle = fieldDef.getTitle(
                            LanguageCodeConverters.languageCodeToLocale(languageCode));
                }
                GWTJahiaField f = new GWTJahiaField(fieldType,String.valueOf(fieldForComparison.getFieldId()),fieldTitle,
                        icon,fieldType==FieldTypes.BIGTEXT,originalValue,newValue,mergedDiffValue);
                compareFields.add(f);
            }
            fieldGroup.setFields(compareFields);
            fieldGroups.add(fieldGroup);
        }
    }

    protected GWTJahiaVersion getVersionFromRevisionEntrySet(RevisionEntrySet rev, VersionComparisonContext context,
                                                     String languageCode){
        Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
        GWTJahiaVersion version = new GWTJahiaVersion();
        version.setVersionableUUID(context.getVersionableUUID());
        version.setDate(rev.getVersionID()*1000L);
        version.setLang(languageCode);
        version.setName(String.valueOf(rev.getVersionID()));
        String readableName = RevisionEntrySet.getVersionNumber(rev, context.getJParams(), locale);
        if (readableName == null){
            readableName = String.valueOf(rev.getVersionNumber());
        }
        version.setReadableName(readableName);
        List<String> versionRowData = new ArrayList<String>();
        version.set(GWTJahiaVersion.VERSION_LABEL,version.getReadableName());
        versionRowData.add((String)version.get(GWTJahiaVersion.VERSION_LABEL));
        version.set(GWTJahiaVersion.WORKFLOW_STATE,RevisionEntrySet.getWorkflowState(rev, context.getJParams(), locale));
        versionRowData.add((String)version.get(GWTJahiaVersion.WORKFLOW_STATE));
        version.set(GWTJahiaVersion.AUTHOR,rev.getLastContributor());
        versionRowData.add((String)version.get(GWTJahiaVersion.AUTHOR));
        version.set(GWTJahiaVersion.DATE,RevisionEntrySet.getVersionDate(rev,locale));
        versionRowData.add((String)version.get(GWTJahiaVersion.DATE));
        version.set(GWTJahiaVersion.LANG,languageCode);
        versionRowData.add(languageCode);
        version.setVersionRowData(versionRowData.toArray(new String[]{}));
        return version;
    }
}

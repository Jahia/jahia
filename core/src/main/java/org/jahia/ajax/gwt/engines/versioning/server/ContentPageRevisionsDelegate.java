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
package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionsData;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.ContentTreeRevisionsVisitor;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.RevisionEntrySet;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.views.engines.JahiaEngineViewHelper;
import org.jahia.views.engines.versioning.ContentVersioningViewHelper;
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
public class ContentPageRevisionsDelegate {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentPageRevisionsDelegate.class);

    /**
     * returns revision entries with or without staging entry
     *
     * @param versionableUUID
     * @param langCode
     * @param withStagingRevision
     * @param withDeletedRevision
     * @param applyLanguageFiltering
     * @param skipNotAvailablePageRevisions
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public GWTJahiaVersionsData getRevisions(String versionableUUID, String langCode, boolean withStagingRevision,
                                     boolean withDeletedRevision, boolean applyLanguageFiltering,
                                     boolean skipNotAvailablePageRevisions, ParamBean jParams)
    throws JahiaException {

        final HttpServletRequest request = jParams.getRequest();
        GWTJahiaVersionsData data;

        try {

            // Prepage a new engine view helper
            String objectKey = versionableUUID;

            // engine view Helper
            ContentVersioningViewHelper engineViewHelper = null;

            // try to retrieve engine data from session
            engineViewHelper =
                    (ContentVersioningViewHelper) request.getSession()
                            .getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER + "_" + objectKey);

            final ContentPage contentObject;

            if (engineViewHelper == null) {
                contentObject = (ContentPage)ContentObject
                        .getContentObjectInstance(ObjectKey.getInstance(objectKey));
                CalendarHandler cal = this.getCalHandler("restoreDateCalendar", 0,
                        jParams);
                engineViewHelper = ContentVersioningViewHelper.getInstance(contentObject, cal);
               cal = this.getCalHandler("fromRevisionDateCalendar", 0,
                        jParams);
                engineViewHelper.setFromRevisionDateCalendar(cal);
                cal = this.getCalHandler("toRevisionDateCalendar", 0,
                        jParams);
                engineViewHelper.setToRevisionDateCalendar(cal);

                // store engine data in session
                request.getSession().setAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER  + "_" + objectKey,
                        engineViewHelper);
            } else {
                contentObject = (ContentPage)engineViewHelper.getContentObject();
            }

            // check permission
            logger.info("Logged in User :" + jParams.getUser().getUsername());
            if (!contentObject.checkWriteAccess(jParams.getUser())) {
                if (!contentObject.checkReadAccess(jParams.getUser())) {
                    throw new JahiaForbiddenAccessException();
                }
            }
            List<RevisionEntrySet> revisions = loadRevisions(engineViewHelper, langCode, withStagingRevision,
                    withDeletedRevision, applyLanguageFiltering, skipNotAvailablePageRevisions, jParams);
            data = getRevisionsData(revisions, jParams, langCode);

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
     * @param langCode
     * @param withStagingRevision
     * @param withDeletedRevision
     * @param jParams
     * @return
     * @throws JahiaException
     */
    protected List<RevisionEntrySet> loadRevisions(ContentVersioningViewHelper versViewHelper, String langCode,
                                                   boolean withStagingRevision, boolean withDeletedRevision,
                                                   boolean applyLanguageFiltering, boolean skipNotAvailablePageRevisions,
                                                   ParamBean jParams)
    throws JahiaException {

        long toDate = versViewHelper.getToRevisionDateCalendar().getDateLong().longValue();
        if (toDate == 0) {
            toDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        }
        toDate += versViewHelper.getToRevisionDateCalendar().getTimeZoneOffSet().longValue()
                + versViewHelper.getToRevisionDateCalendar().getServerClientTimeDiff().longValue();

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(langCode));

        EntryLoadRequest loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,(int) (toDate / 1000),locales);

        // prepare Revisions List
        ContentTreeRevisionsVisitor revisionsVisitor =
                versViewHelper.getContentTreeRevisionsVisitor(
                        jParams.getUser(),loadRequest, ProcessingContext.EDIT);
        revisionsVisitor.setApplyLanguageFiltering(applyLanguageFiltering);
        revisionsVisitor.setSkipNotAvailablePageRevisions(skipNotAvailablePageRevisions);
        revisionsVisitor.setWithStagingRevisions(withStagingRevision);
        revisionsVisitor.setWithDeletedContent(withDeletedRevision);
        if (versViewHelper.isRestoringPage()) {
            int pageLevel = ((PagesVersioningViewHelper) versViewHelper).getPageLevel().intValue();
            if (pageLevel > 0) {
                pageLevel -= 1;
            }
            revisionsVisitor.setDescendingPageLevel(pageLevel);
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
                jParams.getLocale().toString(),
                versViewHelper.getSortAttribute(),
                versViewHelper.getSortOrder(), false);

        return revisionsVisitor.getRevisions();
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

    protected GWTJahiaVersionsData getRevisionsData(List<RevisionEntrySet> revisions, ParamBean jParams, String languageCode){

        GWTJahiaVersionsData data = new GWTJahiaVersionsData();
        List<String> versionRowDataHeadLabels = new ArrayList<String>();
        versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.version",
                jParams.getLocale(),"Version"));
        versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.workflowStatus",
                jParams.getLocale(),"Workflow Status"));
        versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.author",
                jParams.getLocale(),"Author"));
        versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.dateOfRevision",
                jParams.getLocale(),"Date of revision"));
        versionRowDataHeadLabels.add(JahiaResourceBundle.getJahiaInternalResource( "org.jahia.engines.version.language",
                jParams.getLocale(),"Language"));
        data.setVersionRowDataHeadLabels(versionRowDataHeadLabels.toArray(new String[versionRowDataHeadLabels.size()]));

        if (revisions == null){
            return data;
        }
        List<GWTJahiaVersion> gwtRevisionEntrySets = new ArrayList<GWTJahiaVersion>();
        try {
            Iterator<RevisionEntrySet> iterator = revisions.iterator();
            RevisionEntrySet entrySet;
            while (iterator.hasNext()){
                entrySet = iterator.next();
                gwtRevisionEntrySets.add(getGWTRevisionEntryFromRevisionEntrySet(entrySet,jParams,languageCode));
            }
        } catch ( Throwable t ){
            logger.debug(t);
        }
        data.setVersions(gwtRevisionEntrySets);
        
        return data;
    }

    protected GWTJahiaVersion getGWTRevisionEntryFromRevisionEntrySet(RevisionEntrySet rev, ParamBean jParams,
                                                     String languageCode){
        Locale locale = LanguageCodeConverters.languageCodeToLocale(languageCode);
        GWTJahiaVersion version = new GWTJahiaVersion();
        version.setVersionableUUID(rev.getObjectKey().getKey());
        version.setDate(rev.getVersionID()*1000L);
        version.setLang(languageCode);
        version.setName(String.valueOf(rev.getVersionID()));
        version.setWorkflowState(rev.getWorkflowState());
        String readableName = RevisionEntrySet.getVersionNumber(rev, jParams, locale);
        version.setReadableName(readableName);
        List<String> versionRowData = new ArrayList<String>();
        version.set(GWTJahiaVersion.VERSION_LABEL,version.getReadableName());
        versionRowData.add((String)version.get(GWTJahiaVersion.VERSION_LABEL));
        version.set(GWTJahiaVersion.WORKFLOW_STATE,RevisionEntrySet.getWorkflowState(rev, jParams, locale));
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
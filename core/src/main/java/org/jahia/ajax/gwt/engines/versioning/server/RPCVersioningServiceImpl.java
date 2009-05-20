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
package org.jahia.ajax.gwt.engines.versioning.server;

import org.jahia.ajax.gwt.client.service.versioning.RPCVersioningService;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionsData;
import org.jahia.ajax.gwt.client.data.GWTJahiaVersion;
import org.jahia.ajax.gwt.client.data.versioning.GWTJahiaVersionComparisonData;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 *
 */
public class RPCVersioningServiceImpl extends AbstractJahiaGWTServiceImpl implements RPCVersioningService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(RPCVersioningServiceImpl.class);

    public GWTJahiaVersionComparisonData getData(GWTJahiaPageContext page,String versionableUUID, String version1, String version2,
                                         String lang) {
        ContentContainerVersionComparisonDelegate delegate = new ContentContainerVersionComparisonDelegate();
        VersionComparisonContext context = new VersionComparisonContext(versionableUUID,version1,version2,lang,
                this.retrieveParamBean(page.getPid(),page.getMode()),this.getRemoteJahiaUser());

        GWTJahiaVersionComparisonData data = null;
        try {
           data = delegate.getVersionComparisonData(context);
        } catch ( Exception t ){
            logger.debug("t");
        }
        return data;
    }

    /**
     * Returns revisions entries with or without staging entry
     *
     * @param page
     * @param versionableUUID
     * @param lang
     * @param withStagingRevision if true, return staging revision too
     * @param withDeletedRevision if true, return deleted revision too
     * @param applyLanguageFiltering
     * @param skipNotAvailablePageRevisions
     * @param offset
     * @param sortParameter
     * @param isAscending
     * @param versionPerPage
     * @return
     */
    public PagingLoadResult<GWTJahiaVersion> getRevisions(GWTJahiaPageContext page,String versionableUUID,
                                        String lang, boolean withStagingRevision, boolean withDeletedRevision,
                                        boolean applyLanguageFiltering,
                                        boolean skipNotAvailablePageRevisions,
                                        int offset, String sortParameter,
                                        boolean isAscending, int versionPerPage) {
        ContentPageRevisionsDelegate delegate = new ContentPageRevisionsDelegate();
        GWTJahiaVersionsData data = null;
        try {
            data = delegate.getRevisions(versionableUUID,lang, withStagingRevision, withDeletedRevision,
                    applyLanguageFiltering, skipNotAvailablePageRevisions, this.retrieveParamBean(page.getPid(),
                            page.getMode()));
        } catch ( Throwable t ){
            logger.debug("t");
        }
        if (data != null){
            List<GWTJahiaVersion> subList = new ArrayList<GWTJahiaVersion>();
            try {
                List<GWTJahiaVersion> versions = data.getVersions();
                // sort job list depending on the sortParameter value
                logger.debug("Parameter: " + sortParameter);
                Locale locale = Locale.getDefault();
                if (lang != null){
                    locale = new Locale(lang);
                }
                Collections.sort(versions, new VersionComparator(sortParameter,isAscending,locale));
                // get sub list
                int lastIndex = offset + versionPerPage;
                for (int i = offset; i < lastIndex; i++) {
                    if (i < versions.size()) {
                        subList.add(versions.get(i));
                    } else {
                        break;
                    }
                }
                return new BasePagingLoadResult(subList, offset, versions.size());
            } catch (Exception e) {
                logger.error(e, e);
                return new BasePagingLoadResult(subList);
            }
        }
        return new BasePagingLoadResult(new ArrayList());
    }

}
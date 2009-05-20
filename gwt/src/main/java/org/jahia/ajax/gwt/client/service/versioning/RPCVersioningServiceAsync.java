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
package org.jahia.ajax.gwt.client.service.versioning;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;

/**
 *
 */
public interface RPCVersioningServiceAsync {

    void getData(GWTJahiaPageContext page,String versionableUUID, String version1, String version2, String lang,
                 AsyncCallback async);

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
    void getRevisions(GWTJahiaPageContext page,String versionableUUID,
                                        String lang, boolean withStagingRevision, boolean withDeletedRevision,
                                        boolean applyLanguageFiltering, boolean skipNotAvailablePageRevisions,
                                        int offset, String sortParameter, boolean isAscending, int versionPerPage,
                                        AsyncCallback async);

}
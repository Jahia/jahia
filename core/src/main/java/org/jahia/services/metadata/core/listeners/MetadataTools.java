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
package org.jahia.services.metadata.core.listeners;

import org.jahia.params.ProcessingContext;
import org.jahia.services.version.EntryLoadRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 31 mai 2007
 * Time: 11:59:11
 * To change this template use File | Settings | File Templates.
 */
public class MetadataTools {

    public static boolean switchToStagingEntryLoadRequest(ProcessingContext jParams){
        if ( jParams == null ){
            return false;
        }
        EntryLoadRequest loadRequest = jParams.getEntryLoadRequest();
        if (loadRequest==null){
            return false;
        }
        if ( loadRequest.isStaging() ){
            return false;
        }
        List locales = new ArrayList();
        locales.addAll(loadRequest.getLocales());
        EntryLoadRequest stagingEntryLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,locales);
        jParams.setSubstituteEntryLoadRequest(stagingEntryLoadRequest);
        return true;
    }

}

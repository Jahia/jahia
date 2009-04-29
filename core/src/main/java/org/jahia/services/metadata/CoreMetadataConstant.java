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
 package org.jahia.services.metadata;

import java.util.Set;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 26 oct. 2005
 * Time: 12:43:45
 * To change this template use File | Settings | File Templates.
 */
public class CoreMetadataConstant {

    public static final String CREATOR = "createdBy";
    public static final String CREATION_DATE = "created";
    public static final String LAST_CONTRIBUTOR = "lastModifiedBy";
    public static final String LAST_MODIFICATION_DATE = "lastModified";
    public static final String LAST_PUBLISHER = "lastPublisher";
    public static final String LAST_PUBLISHING_DATE = "lastPublishingDate";
    public static final String DESCRIPTION = "description";
    public static final String KEYWORDS = "keywords";
    public static final String DEFAULT_CATEGORY = "defaultCategory";
    public static final String PAGE_PATH = "pagePath";

    public static Set<String> notRestorableMetadatas  = new HashSet<String>();

    static {
        notRestorableMetadatas.add(CoreMetadataConstant.CREATION_DATE);
        notRestorableMetadatas.add(CoreMetadataConstant.CREATOR);
        notRestorableMetadatas.add(CoreMetadataConstant.LAST_CONTRIBUTOR);
        notRestorableMetadatas.add(CoreMetadataConstant.LAST_MODIFICATION_DATE);
        notRestorableMetadatas.add(CoreMetadataConstant.LAST_PUBLISHER);
        notRestorableMetadatas.add(CoreMetadataConstant.LAST_PUBLISHING_DATE);
    }

}

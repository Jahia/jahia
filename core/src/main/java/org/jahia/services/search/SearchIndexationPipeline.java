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
 package org.jahia.services.search;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 5 sept. 2005
 * Time: 10:51:59
 * To change this template use File | Settings | File Templates.
 */
public interface SearchIndexationPipeline {

    public static final String SOURCE_OBJECT = "SearchIndexationPipeline.sourceObject";

    public static final String LOAD_REQUEST = "SearchIndexationPipeline.loadRequest";

    public static final String INDEXABLE_DOCUMENTS = "SearchIndexationPipeline.indexableDocument";

    public static final String PROCESSING_CONTEXT = "SearchIndexationPipeline.processingContext";

    public static final String APPLY_FILE_FIELD_INDEXATION_RULE 
            = "SearchIndexationPipeline.applyFileFieldIndexationRule";

}

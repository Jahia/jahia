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

//
//
//

package org.jahia.services.search;

import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.services.metadata.CoreMetadataConstant;

/**
 * Search Engine constants
 *
 * @author NK
 */

public final class JahiaSearchConstant {


    public static final String LUCENE_WRITE_LOCK_TIMEOUT = "org.apache.lucene.writeLockTimeout";
    public static final String LUCENE_COMMIT_LOCK_TIMEOUT = "org.apache.lucene.commitLockTimeout";
    public static final String LUCENE_MIN_MERGE_DOCS = "org.apache.lucene.minMergeDocs";
    public static final String LUCENE_MAX_MERGE_DOCS = "org.apache.lucene.maxMergeDocs";
    public static final String LUCENE_MERGE_FACTOR = "org.apache.lucene.mergeFactor";
    public static final String LUCENE_MAX_FIELD_LENGTH = "org.apache.lucene.maxFieldLength";
    public static final String LUCENE_MAX_BUFFERED_DOCS = "org.apache.lucene.maxBufferedDocs";
    public static final String LUCENE_MAX_CLAUSE_COUNT = "org.apache.lucene.maxClauseCount";
    public static final String LUCENE_USE_COMPOUND_FILE = "org.apache.lucene.useCompoundFile";
    public static final String LUCENE_INDEXER_AUTO_COMMIT = "org.apache.lucene.indexerAutoCommit";    
    public static final String LUCENE_INDEX_DELETION_POLICY = "org.apache.lucene.indexDeletionPolicy";
    public static final String LUCENE_INDEX_DELETION_EXPIRATION_TIME = "org.apache.lucene.expirationTimeForDeletion";    

    // short description lenght for a search hit
    public static final int TEASER_LENGTH = 80;

    public static final String JAHIA_PREFIX                 = "jahia.";

    public static final String OBJECT_PROPERTY_PREFIX       = JAHIA_PREFIX + "object_property_";
    public static final String DEFINITION_PROPERTY_PREFIX   = JAHIA_PREFIX + "definition_property_";
    public static final String CONTAINER_FIELD_PREFIX       = JAHIA_PREFIX + "containerfield_";
    public static final String CONTAINER_FIELD_SORT_PREFIX  = JAHIA_PREFIX + "containerfieldsort_";
    public static final String CONTAINER_FIELD_FACET_PREFIX = JAHIA_PREFIX + "containerfieldfacet_";
    public static final String CONTAINER_EMPTY_FIELD_FACET_PREFIX = JAHIA_PREFIX + "no_containerfieldfacet_";    
    public static final String CONTAINER_FIELD_ALIAS_PREFIX = JAHIA_PREFIX + "containerfieldalias_";
    
    public static final String CONTAINER_DEFINITION_PRIMARYTYPE  = JAHIA_PREFIX + "definition_primarytype";    

    /**
     * Metadata prefix
     */
    public static final String METADATA_PREFIX                  = JAHIA_PREFIX + "metadata_";

    /**
     * Default full content and metadata text search
     */
    public static final String ALL_FULLTEXT_SEARCH_FIELD        = JAHIA_PREFIX + "all";

    /**
     * Default full search without metadata & without field, used for query rewriting
     */
    public static final String ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE = JAHIA_PREFIX + "all_for_query_rewrite";

    /**
     * Default full content text search
     */
    public static final String CONTENT_FULLTEXT_SEARCH_FIELD    = JAHIA_PREFIX + "content";

    /**
     * Default full content text search  without container field, used for query rewriting
     */
    public static final String CONTENT_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE  = JAHIA_PREFIX + "content_for_query_rewrite";

    /**
     * Default full metadata text search
     */
    public static final String METADATA_FULLTEXT_SEARCH_FIELD   = JAHIA_PREFIX + "metadata";

    /**
     * Default full metadata text search
     */
    public static final String METADATA_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE   = JAHIA_PREFIX + "metadata_for_query_rewrite";

    /**
     * Default file content text search
     */
    public static final String FILE_CONTENT_FULLTEXT_SEARCH_FIELD   = JAHIA_PREFIX + "filecontent";

    /**
     * Default file content text search
     */
    public static final String FILE_METADATA_FULLTEXT_SEARCH_FIELD  = JAHIA_PREFIX + "filemetadata";

    /**
     * Jahia's Page, Container resources
     */
    public static final String ID                           = JAHIA_PREFIX + "id";
    public static final String COMP_ID                      = JAHIA_PREFIX + "comp_id";
    public static final String OBJECT_KEY                   = JAHIA_PREFIX + "object_key";

    public static final String ACL_ID                       = JAHIA_PREFIX + "acl_id";
    public static final String DEFINITION_ID                = JAHIA_PREFIX + "definition_id";
    public static final String DEFINITION_NAME              = JAHIA_PREFIX + "definition_name";
    public static final String JAHIA_ID                     = JAHIA_PREFIX + "jahia_id";
    public static final String LANGUAGE_CODE                = JAHIA_PREFIX + "language_code";
    public static final String PAGE_ID                      = JAHIA_PREFIX + "page_id";
    public static final String PARENT_ID                    = JAHIA_PREFIX + "parent_id";
    public static final String VERSION                      = JAHIA_PREFIX + "version";
    public static final String WORKFLOW_STATE               = JAHIA_PREFIX + "workflow_state";

    public static final String CATEGORY_ID                  = JAHIA_PREFIX + "category_id";

    /**
     * may be a field, a container or page...
     */
    public static final String CONTENT_TYPE                 = JAHIA_PREFIX + "content_type";
    public static final String FIELD_TYPE                   = JAHIA_PREFIX + "content_type.field";
    public static final String CONTAINER_TYPE               = JAHIA_PREFIX + "content_type.container";
    public static final String CONTAINERLIST_TYPE           = JAHIA_PREFIX + "content_type.containerlist";
    public static final String PAGE_TYPE                    = JAHIA_PREFIX + "content_type.page";


    // Container specific attributes
    public static final String CONTAINER_ALIAS              = JAHIA_PREFIX + "container_alias";
    public static final String CONTAINER_DEFINITION_NAME    = JAHIA_PREFIX + "container_definition_name";


    /**
     * Prefix for extracted file's properties
     */
    public static final String FILE_PROPERTY_PREFIX         = JAHIA_PREFIX + "file_property_";

    // File attributes
    public static final String FILE_REALNAME                = JAHIA_PREFIX + "file_realname";
    public static final String FILE_NAME                    = JAHIA_PREFIX + "file_filename";
    public static final String FILE_CONTENT_TYPE            = JAHIA_PREFIX + "file_content_type";
    public static final String FILE_SIZE                    = JAHIA_PREFIX + "file_size";


    public static final String FIELD_FIELDID                = JAHIA_PREFIX + "field_id";
    public static final String FIELD_DEFINITION_ID          = JAHIA_PREFIX + "field_definition_id";
    public static final String FIELD_DEFINITION_NAME        = JAHIA_PREFIX + "field_definition_name";


    public static final String TITLE                        = JAHIA_PREFIX + "title";

    public static final String PAGE_URL_KEY                 = JAHIA_PREFIX + "page_url_key";

    public static final String NO_PADDED_FIELD_POSTFIX  = "_no_padded_field";
    /**
     * The content is a copy ( content picking )
     */
    public static final String CONTENT_PICKING                  = JAHIA_PREFIX + "content_picking";
    
    public static final String METADATA_CREATION_DATE = METADATA_PREFIX + CoreMetadataConstant.CREATION_DATE.toLowerCase();
    public static final String METADATA_CREATOR = METADATA_PREFIX + CoreMetadataConstant.CREATOR.toLowerCase();
    public static final String METADATA_LAST_CONTRIBUTOR = METADATA_PREFIX + CoreMetadataConstant.LAST_CONTRIBUTOR.toLowerCase();
    public static final String METADATA_LAST_MODIFICATION_DATE = METADATA_PREFIX + CoreMetadataConstant.LAST_MODIFICATION_DATE.toLowerCase();
    public static final String METADATA_KEYWORDS = METADATA_PREFIX + CoreMetadataConstant.KEYWORDS.toLowerCase();    
    public static final String METADATA_PAGE_PATH = METADATA_PREFIX + CoreMetadataConstant.PAGE_PATH.toLowerCase();    

    //--------------------------------------------------------------------------
    /**
     * Supported Form Input
     *
     */

    /**
     * Container list search query form input name prefix.
     * The complete name is the combination of :
     * "clistsquery_" + ctnlist.getDefinition().getName() ( ctnlist name );
     */
    public static final String CLIST_SEARCHQUERY_INPUT_PREFIX = "clistsquery_";


    /**
     *  JahiaSearchConstant.PAGE_TYPE is mapped to JahiaSearchHitInterface.PAGE_TYPE
     *  JahiaSearchConstant.FIELD_TYPE is mapped to JahiaSearchHitInterface.FIELD_TYPE
     *  JahiaSearchConstant.CONTAINER_TYPE is mapped to JahiaSearchHitInterface.CONTAINER_TYPE
     *  JahiaSearchConstant.CONTAINERLIST_TYPE is mapped to JahiaSearchHitInterface.CONTAINERLIST_TYPE
     *
     * @return
     */
    public static int getJahiaSearchHitTypeFromSearchConstantType(String type){
        if ( JahiaSearchConstant.PAGE_TYPE.equals(type) ){
            return JahiaSearchHitInterface.PAGE_TYPE;
        } else if ( JahiaSearchConstant.CONTAINER_TYPE.equals(type) ){
            return JahiaSearchHitInterface.CONTAINER_TYPE;
        } else if ( JahiaSearchConstant.CONTAINERLIST_TYPE.equals(type) ){
            return JahiaSearchHitInterface.CONTAINERLIST_TYPE;
        } else if ( JahiaSearchConstant.FIELD_TYPE.equals(type) ){
            return JahiaSearchHitInterface.FIELD_TYPE;
        } else {
            return JahiaSearchHitInterface.UNDEFINED_TYPE;
        }
    }

}

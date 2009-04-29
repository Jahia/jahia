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
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.services.search.lucene.fs;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.RefCounted;

public class LuceneQueryRequest {
    LuceneCoreSearcher coreSearcher;
    public LuceneQueryRequest (LuceneCoreSearcher coreSearcher) {
        this.coreSearcher = coreSearcher; 
    }
    
    // The index searcher associated with this request
    protected RefCounted searcherHolder;
    public JahiaIndexSearcher getSearcher() throws JahiaException {
      // should this reach out and get a searcher from the core singleton, or
      // should the core populate one in a factory method to create requests?
      // or there could be a setSearcher() method that Solr calls

      if (searcherHolder==null) {
        searcherHolder = coreSearcher.getSearcher();
      }

      return (JahiaIndexSearcher) searcherHolder.get();
    }
    
   /* Frees resources associated with this request, this method <b>must</b>
    * be called when the object is no longer in use.
    */
   public void close() {
     if (searcherHolder!=null) {
       searcherHolder.decref();
       searcherHolder = null;
     }
   }    
}

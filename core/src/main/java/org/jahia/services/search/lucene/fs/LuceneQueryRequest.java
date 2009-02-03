/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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

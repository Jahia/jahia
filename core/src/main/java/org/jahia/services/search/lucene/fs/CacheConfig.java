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

import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.Properties;

/**
 * Contains the knowledge of how cache config is
 * stored in the solrconfig.xml file, and implements a
 * factory to create caches.
 *
 * @version $Id: CacheConfig.java 597847 2007-11-24 13:51:46Z ryan $
 */
public class CacheConfig {
    private static Logger log = Logger.getLogger(CacheConfig.class.getName());
  private Properties args;

  private String cacheImpl;
  private Class<? extends SearcherCache> clazz;

  private Object[] persistence = new Object[1];

  private String regenImpl;
  private CacheRegenerator regenerator;

  public CacheRegenerator getRegenerator() {
    return regenerator;
  }

  public void setRegenerator(CacheRegenerator regenerator) {
    this.regenerator = regenerator;
  }

  public static CacheConfig getConfig(Properties cacheConfig) {
      CacheConfig config = new CacheConfig();
    config.args = cacheConfig;
    config.cacheImpl = cacheConfig.getProperty("class");
    config.regenImpl = cacheConfig.getProperty("regenerator");
    try {
        config.clazz = Class.forName(config.cacheImpl).asSubclass(SearcherCache.class);
        if (config.regenImpl != null && config.regenImpl.length() > 0) {
          config.regenerator = (CacheRegenerator) Class.forName(config.regenImpl).newInstance();
        } else {
            config.regenerator = new CacheRegenerator() {
                public boolean regenerateItem(JahiaIndexSearcher newSearcher, SearcherCache newCache, SearcherCache oldCache, Object oldKey, Object oldVal) throws IOException {
                  newSearcher.cacheFilter((Query)oldKey, null, false);
                  return true;
                }
              };
        }
    } catch (Exception e) {
        log.error("Cannot find or instantiate filter cache or regenerator class", e);
    } 
    
    return config;
  }

  public SearcherCache newInstance() {
    try {
        SearcherCache cache = (SearcherCache)clazz.newInstance();
      persistence[0] = cache.init(args, persistence[0], regenerator);
      return cache;
    } catch (Exception e) {
      log.error("Error instantiating cache",e);
      // we can carry on without a cache... but should we?
      // in some cases (like an OOM) we probably should try to continue.
      return null;
    }
  }

}

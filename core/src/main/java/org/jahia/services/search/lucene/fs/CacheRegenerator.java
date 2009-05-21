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

import java.io.IOException;

/**
 * Implementations of <code>CacheRegenerator</code> are used in autowarming to populate a new cache
 * based on an old cache.  <code>regenerateItem</code> is called for each item that should be inserted into the new cache.
 * <p>
 * Implementations should have a noarg constructor and be thread safe (a single instance will be
 * used for all cache autowarmings).
 *
 * @version $Id: CacheRegenerator.java 555343 2007-07-11 17:46:25Z hossman $
 */
public interface CacheRegenerator {
  /**
   * Regenerate an old cache item and insert it into <code>newCache</code>
   *
   * @param newSearcher the new searcher who's caches are being autowarmed
   * @param newCache    where regenerated cache items should be stored. the target of the autowarming
   * @param oldCache    the old cache being used as a source for autowarming
   * @param oldKey      the key of the old cache item to regenerate in the new cache
   * @param oldVal      the old value of the cache item
   * @return true to continue with autowarming, false to stop
   */
  public boolean regenerateItem(JahiaIndexSearcher newSearcher, SearcherCache newCache, SearcherCache oldCache, Object oldKey, Object oldVal) throws IOException;
}

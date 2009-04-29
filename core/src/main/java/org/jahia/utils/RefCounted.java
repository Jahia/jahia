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

package org.jahia.utils;

import java.util.concurrent.atomic.AtomicInteger;

/** Keep track of a reference count on a resource and close it when
 * the count hits zero.
 *
 * By itself, this class could have some race conditions
 * since there is no synchronization between the refcount
 * check and the close.  Solr's use in reference counting searchers
 * is safe since the count can only hit zero if it's unregistered (and
 * hence incref() will not be called again on it).
 *
 * @version $Id: RefCounted.java 555343 2007-07-11 17:46:25Z hossman $
 */

public abstract class RefCounted {
  protected final Object resource;
  protected final AtomicInteger refcount = new AtomicInteger();

  public RefCounted(Object resource) {
    this.resource = resource;
  }

  public final RefCounted incref() {
    refcount.incrementAndGet();
    return this;
  }

  public final Object get() {
    return resource;
  }

  public void decref() {
    if (refcount.decrementAndGet() == 0) {
      close();
    }
  }

  protected abstract void close();
}

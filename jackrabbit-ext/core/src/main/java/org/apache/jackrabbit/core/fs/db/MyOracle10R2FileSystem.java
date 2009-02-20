/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.fs.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@inheritDoc}
 * <p/>
 * <code>Oracle10R2FileSystem</code> works for Oracle database version 10 R2+.
 */
public class MyOracle10R2FileSystem extends MyOracleBaseFileSystem {
    /**
     * Logger instance
     */
    private static Logger log = LoggerFactory.getLogger(MyOracle10R2FileSystem.class);

    /**
     * Creates a new <code>Oracle10FileSystem</code> instance.
     */
    public MyOracle10R2FileSystem() {
        super();
    }
}

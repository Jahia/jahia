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

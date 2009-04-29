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
package org.jahia.hibernate.dialect;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 19 mars 2009
 * Time: 14:22:36
 * To change this template use File | Settings | File Templates.
 */
public class PostgreSQLDialect extends org.hibernate.dialect.PostgreSQLDialect {
    /**
     * Does this dialect support "pooled" sequences.  Not aware of a better
     * name for this.  Essentially can we specify the initial and increment values?
     *
     * @return True if such "pooled" sequences are supported; false otherwise.
     * @see #getCreateSequenceStrings(String, int, int)
     * @see #getCreateSequenceString(String, int, int)
     */
    @Override
    public boolean supportsPooledSequences() {
        return true;
    }
}

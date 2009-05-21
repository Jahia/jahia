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
package org.apache.jackrabbit.core.persistence.bundle;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import org.apache.jackrabbit.core.persistence.PMContext;
import org.apache.jackrabbit.core.persistence.bundle.util.DbNameIndex;
import org.apache.jackrabbit.core.persistence.bundle.util.NGKDbNameIndex;
import org.apache.jackrabbit.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the {@link BundleDbPersistenceManager} by Oracle specific code.
 * <p/>
 * Configuration:<br>
 * <ul>
 * <li>&lt;param name="{@link #setExternalBLOBs(String)} externalBLOBs}" value="false"/>
 * <li>&lt;param name="{@link #setBundleCacheSize(String) bundleCacheSize}" value="8"/>
 * <li>&lt;param name="{@link #setConsistencyCheck(String) consistencyCheck}" value="false"/>
 * <li>&lt;param name="{@link #setMinBlobSize(String) minBlobSize}" value="16384"/>
 * <li>&lt;param name="{@link #setDriver(String) driver}" value="oracle.jdbc.OracleDriverr"/>
 * <li>&lt;param name="{@link #setUrl(String) url}" value="jdbc:oracle:thin:@127.0.0.1:1521:xe"/>
 * <li>&lt;param name="{@link #setUser(String) user}" value="crx"/>
 * <li>&lt;param name="{@link #setPassword(String) password}" value="crx"/>
 * <li>&lt;param name="{@link #setSchema(String) schema}" value="oracle"/>
 * <li>&lt;param name="{@link #setSchemaObjectPrefix(String) schemaObjectPrefix}" value="${wsp.name}_"/>
 * <li>&lt;param name="{@link #setTableSpace(String) tableSpace}" value=""/>
 * <li>&lt;param name="{@link #setErrorHandling(String) errorHandling}" value=""/>
 * </ul>
 */
public class MyOraclePersistenceManager extends BundleDbPersistenceManager {

    /**
     * the default logger
     */
    private static Logger log = LoggerFactory.getLogger(OraclePersistenceManager.class);

    /** the variable for the Oracle table space */
    public static final String TABLE_SPACE_VARIABLE =
        "${tableSpace}";

    /** the Oracle table space to use */
    protected String tableSpace;

    /**
     * Creates a new oracle persistence manager
     */
    public MyOraclePersistenceManager() {
        // enable db blob support
        setExternalBLOBs(false);
    }

    /**
     * Returns the configured Oracle table space.
     * @return the configured Oracle table space.
     */
    public String getTableSpace() {
        return tableSpace;
    }

    /**
     * Sets the Oracle table space.
     * @param tableSpace the Oracle table space.
     */
    public void setTableSpace(String tableSpace) {
        if (tableSpace != null) {
            this.tableSpace = tableSpace.trim();
        } else {
            this.tableSpace = null;
        }
    }

    public void init(PMContext context) throws Exception {
        // init default values
        if (getDriver() == null) {
            setDriver("oracle.jdbc.OracleDriver");
        }
        if (getSchema() == null) {
            setSchema("oracle");
        }
        if (getSchemaObjectPrefix() == null) {
            setSchemaObjectPrefix(context.getHomeDir().getName() + "_");
        }
        super.init(context);

        // check driver version
        try {
            DatabaseMetaData metaData = connectionManager.getConnection().getMetaData();
            if (metaData.getDriverMajorVersion() < 10) {
                // Oracle drivers prior to version 10 only support
                // writing BLOBs up to 32k in size...
                log.warn("Unsupported driver version detected: "
                        + metaData.getDriverName()
                        + " v" + metaData.getDriverVersion());
            }
        } catch (SQLException e) {
            log.warn("Can not retrieve driver version", e);
        }
    }

    /**
     * Returns a new instance of a NGKDbNameIndex.
     * @return a new instance of a NGKDbNameIndex.
     * @throws SQLException if an SQL error occurs.
     */
    protected DbNameIndex createDbNameIndex() throws SQLException {
        return new NGKDbNameIndex(connectionManager, schemaObjectPrefix);
    }

    /**
     * {@inheritDoc}
     *
     * @return <code>true</code>
     */
    protected boolean checkTablesWithUser() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected String createSchemaSQL(String sql) {
        sql = Text.replace(sql, SCHEMA_OBJECT_PREFIX_VARIABLE, schemaObjectPrefix).trim();
        // set the tablespace if it is defined
        String tspace;
        if (tableSpace == null || "".equals(tableSpace)) {
            tspace = "";
        } else {
            tspace = "tablespace " + tableSpace;
        }
        return Text.replace(sql, TABLE_SPACE_VARIABLE, tspace).trim();
    }

    /**
     * Since Oracle only supports table names up to 30 characters in
     * length illegal characters are simply replaced with "_" rather than
     * escaping them with "_x0000_".
     *
     * @inheritDoc
     */
    protected void prepareSchemaObjectPrefix() throws Exception {
        DatabaseMetaData metaData = connectionManager.getConnection().getMetaData();
        String legalChars = metaData.getExtraNameCharacters();
        legalChars += "ABCDEFGHIJKLMNOPQRSTUVWXZY0123456789_";

        String prefix = schemaObjectPrefix.toUpperCase();
        StringBuffer escaped = new StringBuffer();
        for (int i = 0; i < prefix.length(); i++) {
            char c = prefix.charAt(i);
            if (legalChars.indexOf(c) == -1) {
                escaped.append('_');
            } else {
                escaped.append(c);
            }
        }
        schemaObjectPrefix = escaped.toString();
    }
}

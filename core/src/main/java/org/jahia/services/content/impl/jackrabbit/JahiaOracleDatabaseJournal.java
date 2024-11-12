/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.util.db.CheckSchemaOperation;
import org.apache.jackrabbit.core.util.db.ConnectionHelper;
import org.apache.jackrabbit.core.util.db.OracleConnectionHelper;

import javax.sql.DataSource;

/**
 * Database journal implementation for Oracle.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaOracleDatabaseJournal extends JahiaDatabaseJournal {
    /**
     * The default tablespace clause used when {@link #tablespace} or {@link #indexTablespace}
     * are not specified.
     */
    protected static final String DEFAULT_TABLESPACE_CLAUSE = "";

    /**
     * Name of the replacement variable in the DDL for {@link #tablespace}.
     */
    protected static final String TABLESPACE_VARIABLE = "${tablespace}";

    /**
     * Name of the replacement variable in the DDL for {@link #indexTablespace}.
     */
    protected static final String INDEX_TABLESPACE_VARIABLE = "${indexTablespace}";

    /** The Oracle tablespace to use for tables */
    protected String tablespace;

    /** The Oracle tablespace to use for indexes */
    protected String indexTablespace;

    /**
     * Initialize this instance with the default schema and driver values.
     */
    public JahiaOracleDatabaseJournal() {
        super();
        super.setDatabaseType("oracle");
        super.setDriver("oracle.jdbc.OracleDriver");
        super.setSchemaObjectPrefix("");
        tablespace = DEFAULT_TABLESPACE_CLAUSE;
        indexTablespace = DEFAULT_TABLESPACE_CLAUSE;
    }

    /**
     * Returns the configured Oracle tablespace for tables.
     * @return the configured Oracle tablespace for tables.
     */
    public String getTablespace() {
        return tablespace;
    }

    /**
     * Sets the Oracle tablespace for tables.
     * @param tablespaceName the Oracle tablespace for tables.
     */
    public void setTablespace(String tablespaceName) {
        this.tablespace = this.buildTablespaceClause(tablespaceName);
    }

    /**
     * Returns the configured Oracle tablespace for indexes.
     * @return the configured Oracle tablespace for indexes.
     */
    public String getIndexTablespace() {
        return indexTablespace;
    }

    /**
     * Sets the Oracle tablespace for indexes.
     * @param tablespaceName the Oracle tablespace for indexes.
     */
    public void setIndexTablespace(String tablespaceName) {
        this.indexTablespace = this.buildTablespaceClause(tablespaceName);
    }

    /**
     * Constructs the <code>tablespace &lt;tbs name&gt;</code> clause from
     * the supplied tablespace name. If the name is empty, {@link #DEFAULT_TABLESPACE_CLAUSE}
     * is returned instead.
     *
     * @param tablespaceName A tablespace name
     * @return A tablespace clause using the supplied name or
     * <code>{@value #DEFAULT_TABLESPACE_CLAUSE}</code> if the name is empty
     */
    private String buildTablespaceClause(String tablespaceName) {
        if (tablespaceName == null || tablespaceName.trim().length() == 0) {
            return DEFAULT_TABLESPACE_CLAUSE;
        } else {
            return "tablespace " + tablespaceName.trim();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConnectionHelper createConnectionHelper(DataSource dataSrc) throws Exception {
        OracleConnectionHelper helper = new OracleConnectionHelper(dataSrc, false);
        helper.init();
        return helper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CheckSchemaOperation createCheckSchemaOperation() {
        if (DEFAULT_TABLESPACE_CLAUSE.equals(indexTablespace) && !DEFAULT_TABLESPACE_CLAUSE.equals(tablespace)) {
            // tablespace was set but not indexTablespace : use the same for both
            indexTablespace = tablespace;
        }
        return super.createCheckSchemaOperation()
            .addVariableReplacement(TABLESPACE_VARIABLE, tablespace)
            .addVariableReplacement(INDEX_TABLESPACE_VARIABLE, indexTablespace);
    }
}

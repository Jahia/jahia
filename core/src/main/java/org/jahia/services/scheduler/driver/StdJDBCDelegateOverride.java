/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.scheduler.driver;

import org.quartz.impl.jdbcjobstore.Util;
import org.quartz.utils.Key;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import static org.quartz.impl.jdbcjobstore.StdJDBCConstants.*;

/**
 * Quartz StdJDBCDelegate override to order triggers to acquire by START_TIME instead of NEXT_FIRE_TIME
 * To be able to execute trigger's jobs in the order of creation
 */
class StdJDBCDelegateOverride {

    /**
     * Query override of {@link org.quartz.impl.jdbcjobstore.StdJDBCConstants#SELECT_NEXT_TRIGGER_TO_ACQUIRE}
     * ORDER is done on: {@link org.quartz.impl.jdbcjobstore.StdJDBCConstants#COL_START_TIME}
     * instead of: {@link org.quartz.impl.jdbcjobstore.StdJDBCConstants#COL_NEXT_FIRE_TIME}
     */
    private static final String DX_SELECT_NEXT_TRIGGER_TO_ACQUIRE = "SELECT "
            + COL_TRIGGER_NAME + ", " + COL_TRIGGER_GROUP + ", "
            + COL_NEXT_FIRE_TIME + ", " + COL_PRIORITY + " FROM "
            + TABLE_PREFIX_SUBST + TABLE_TRIGGERS + " WHERE "
            + COL_TRIGGER_STATE + " = ? AND " + COL_NEXT_FIRE_TIME + " < ? "
            + "AND (" + COL_NEXT_FIRE_TIME + " >= ?) "
            + "ORDER BY "+ COL_START_TIME + " ASC, " + COL_PRIORITY + " DESC";

    /**
     * Static copy of {@link org.quartz.impl.jdbcjobstore.StdJDBCDelegate#selectTriggerToAcquire(Connection, long, long)}
     * Take a look at source code
     */
    static List selectTriggerToAcquire(Connection conn, long noLaterThan, long noEarlierThan, String tablePrefix)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List nextTriggers = new LinkedList();
        try {
            ps = conn.prepareStatement(rtp(DX_SELECT_NEXT_TRIGGER_TO_ACQUIRE, tablePrefix));

            // Try to give jdbc driver a hint to hopefully not pull over
            // more than the few rows we actually need.
            ps.setFetchSize(5);
            ps.setMaxRows(5);

            ps.setString(1, STATE_WAITING);
            ps.setBigDecimal(2, new BigDecimal(String.valueOf(noLaterThan)));
            ps.setBigDecimal(3, new BigDecimal(String.valueOf(noEarlierThan)));
            rs = ps.executeQuery();

            while (rs.next() && nextTriggers.size() < 5) {
                nextTriggers.add(new Key(
                        rs.getString(COL_TRIGGER_NAME),
                        rs.getString(COL_TRIGGER_GROUP)));
            }

            return nextTriggers;
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }

    /**
     * Static copy of {@link org.quartz.impl.jdbcjobstore.StdJDBCDelegate#rtp(String)}
     * Take a look at source code
     */
    private static String rtp(String query, String tablePrefix) {
        return Util.rtp(query, tablePrefix);
    }

    /**
     * Static copy of {@link org.quartz.impl.jdbcjobstore.StdJDBCDelegate#closeResultSet(ResultSet)}
     * Take a look at source code
     */
    private static void closeResultSet(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException ignore) {
            }
        }
    }

    /**
     * Static copy of {@link org.quartz.impl.jdbcjobstore.StdJDBCDelegate#closeStatement(Statement)}
     * Take a look at source code
     */
    private static void closeStatement(Statement statement) {
        if (null != statement) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }
    }
}

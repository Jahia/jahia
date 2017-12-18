/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes;

/**
 * The possible values for on-conflict action
 */
public class OnConflictAction {
    public static final int IGNORE = 0;
    public static final int UNRESOLVED = 1;
    public static final int USE_SOURCE = 2;
    public static final int USE_TARGET = 3;
    public static final int USE_OLDEST = 4;
    public static final int USE_LATEST = 5;
    public static final int NUMERIC_USE_MIN = 6;
    public static final int NUMERIC_USE_MAX = 7;
    public static final int NUMERIC_SUM = 8;
    public static final int TEXT_MERGE = 9;

    /**
     * The names of the defined on-conflict actions, as used in serialization.
     */
    public static final String ACTIONNAME_USE_OLDEST = "oldest";
    public static final String ACTIONNAME_USE_LATEST = "latest";
    public static final String ACTIONNAME_NUMERIC_USE_MIN = "min";
    public static final String ACTIONNAME_NUMERIC_USE_MAX = "max";
    public static final String ACTIONNAME_NUMERIC_SUM = "sum";

    /**
     * Returns the name of the specified <code>action</code>, as used in
     * serialization.
     *
     * @param action the on-conflict action
     * @return the name of the specified <code>action</code>
     * @throws IllegalArgumentException if <code>action</code> is not a valid
     *                                  on-conflict action.
     */
    public static String nameFromValue(int action) {
        switch (action) {
            case USE_OLDEST:
                return ACTIONNAME_USE_OLDEST;
            case USE_LATEST:
                return ACTIONNAME_USE_LATEST;
            case NUMERIC_USE_MIN:
                return ACTIONNAME_NUMERIC_USE_MIN;
            case NUMERIC_USE_MAX:
                return ACTIONNAME_NUMERIC_USE_MAX;
            case NUMERIC_SUM:
                return ACTIONNAME_NUMERIC_SUM;
            default:
                throw new IllegalArgumentException("unknown on-conflict action: " + action);
        }
    }

    /**
     * Returns the numeric constant value of the on-conflict action with the
     * specified name.
     *
     * @param name the name of the on-conflict action
     * @return the numeric constant value
     * @throws IllegalArgumentException if <code>name</code> is not a valid
     *                                  on-conflict action name.
     */
    public static int valueFromName(String name) {
        if (name.equals(ACTIONNAME_USE_OLDEST)) {
            return USE_OLDEST;
        } else if (name.equals(ACTIONNAME_USE_LATEST)) {
            return USE_LATEST;
        } else if (name.equals(ACTIONNAME_NUMERIC_USE_MIN)) {
            return NUMERIC_USE_MIN;
        } else if (name.equals(ACTIONNAME_NUMERIC_USE_MAX)) {
            return NUMERIC_USE_MAX;
        } else if (name.equals(ACTIONNAME_NUMERIC_SUM)) {
            return NUMERIC_SUM;
        } else {
            throw new IllegalArgumentException("unknown on-conflict action: " + name);
        }
    }
}

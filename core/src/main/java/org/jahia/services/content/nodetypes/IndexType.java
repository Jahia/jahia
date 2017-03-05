/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
 * The index parameters supported by Jahia.
 */
public class IndexType {
    public static final int NO = 0;
    public static final int TOKENIZED = 1;
    public static final int UNTOKENIZED = 2;

    public static final String INDEXNAME_NO = "no";
    public static final String INDEXNAME_TOKENIZED = "tokenized";
    public static final String INDEXNAME_UNTOKENIZED = "untokenized";

    public static String nameFromValue(int type) {
        switch (type) {
            case NO:
                return INDEXNAME_NO;
            case TOKENIZED:
                return INDEXNAME_TOKENIZED;
            case UNTOKENIZED:
                return INDEXNAME_UNTOKENIZED;
            default:
                throw new IllegalArgumentException("unknown index type: " + type);
        }
    }

    public static int valueFromName(String name) {
        if (name.equals(INDEXNAME_NO)) {
            return NO;
        } else if (name.equals(INDEXNAME_TOKENIZED)) {
            return TOKENIZED;
        } else if (name.equals(INDEXNAME_UNTOKENIZED)) {
            return UNTOKENIZED;
        } else {
            throw new IllegalArgumentException("unknown index type: " + name);
        }
    }

}

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

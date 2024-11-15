/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.comparator;

import java.util.Comparator;
import java.util.List;

/**
 * @author Xavier Lawrence
 */
public class LanguageCodesComparator implements Comparator<String> {

    private List<String> pattern;

    public LanguageCodesComparator() {
    }

    public LanguageCodesComparator(final List<String> pattern) {
        this.pattern = pattern;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public int compare(final String lang1, final String lang2) {
        final int rank1 = pattern.indexOf(lang1);
        final int rank2 = pattern.indexOf(lang2);

        if (rank1 > rank2) {
            if (rank2 == -1) {
                return -1;
            }
            return 1;

        } else if (rank1 == rank2) {
            return lang1.compareTo(lang2);

        } else {
            if (rank1 == -1) {
                return 1;
            }
            return -1;
        }
    }
}

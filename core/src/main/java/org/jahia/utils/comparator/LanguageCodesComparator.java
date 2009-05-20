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

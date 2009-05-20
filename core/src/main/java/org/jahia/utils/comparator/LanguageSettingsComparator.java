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

import org.jahia.services.sites.SiteLanguageSettings;

import java.util.Comparator;

/**
 * @author Xavier Lawrence
 */
public class LanguageSettingsComparator implements Comparator<SiteLanguageSettings> {

    public int compare(final SiteLanguageSettings settings1, final SiteLanguageSettings settings2) {
        final int rank1 = settings1.getRank();
        final int rank2 = settings2.getRank();

        if (rank1 > rank2) {
            return 1;
        } else if (rank1 == rank2) {
            return 0;
        } else {
            return -1;
        }
    }
}

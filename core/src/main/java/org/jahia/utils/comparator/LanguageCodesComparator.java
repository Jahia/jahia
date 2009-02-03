/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.comparator;

import java.util.Comparator;
import java.util.List;

/**
 * @author Xavier Lawrence
 */
public class LanguageCodesComparator implements Comparator {

    private List<String> pattern;

    public LanguageCodesComparator() {
    }

    public LanguageCodesComparator(final List<String> pattern) {
        this.pattern = pattern;
    }

    public void setPattern(List<String> pattern) {
        this.pattern = pattern;
    }

    public int compare(final Object o1, final Object o2) {
        final String lang1 = o1.toString();
        final String lang2 = o2.toString();

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

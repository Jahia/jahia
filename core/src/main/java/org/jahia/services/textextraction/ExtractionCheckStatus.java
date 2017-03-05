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
package org.jahia.services.textextraction;

import org.slf4j.helpers.MessageFormatter;

/**
 * Result of the extraction check operation.
 * 
 * @author Benjamin Papez
 */
public class ExtractionCheckStatus {

    long checked;

    long extractable;

    long fixed;

    /**
     * Returns the number of checked items.
     * 
     * @return the number of checked items
     */
    public long getChecked() {
        return checked;
    }

    /**
     * Returns the number of items, where extraction can be done.
     * 
     * @return the number of items, where extraction can be done
     */
    public long getExtractable() {
        return extractable;
    }

    /**
     * Returns the number of fixed text extractions.
     * 
     * @return the number of fixed text extractions
     */
    public long getFixed() {
        return fixed;
    }

    @Override
    public String toString() {
        return fixed == 0 ? MessageFormatter.arrayFormat(
                "{} potential files checked, {} possible text extractions found.",
                new Long[] { checked, extractable }).getMessage()
                : MessageFormatter
                        .arrayFormat(
                                "{} files checked, {} possible text extractions found. {} text extractions finished.",
                                new Long[] { checked, extractable, fixed })
                        .getMessage();
    }
}
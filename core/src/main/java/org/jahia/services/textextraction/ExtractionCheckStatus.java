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

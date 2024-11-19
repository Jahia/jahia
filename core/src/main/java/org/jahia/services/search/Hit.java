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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search;

import java.util.Date;
import java.util.List;

/**
 * Describes basic properties of a search hit item.
 *
 * @author Sergiy Shyrkov
 */
public interface Hit<T> {

    /**
     * Returns the MIME type of the hit content, if applicable.
     *
     * @return the MIME type of the hit content, if applicable
     */
    String getContentType();

    /**
     * Returns the content creation date.
     *
     * @return the content creation date
     */
    Date getCreated();

    /**
     * Returns the resource author (creator).
     *
     * @return the resource author (creator)
     */
    String getCreatedBy();

    /**
     * Returns the short description, abstract or excerpt of the hit's content.
     *
     * @return the short description, abstract or excerpt of the hit's content
     */
    String getExcerpt();

    /**
     * Returns the last modification date.
     *
     * @return the last modification date
     */
    Date getLastModified();

    /**
     * Returns the last contributor.
     *
     * @return the last contributor
     */
    String getLastModifiedBy();

    /**
     * Returns the URL to the hit page.
     *
     * @return the URL to the hit page
     */
    String getLink();

    /**
     * Returns the raw hit object.
     *
     * @return the raw hit object
     */
    T getRawHit();

    /**
     * Returns the hit score.
     *
     * @return the hit score
     */
    float getScore();

    /**
     * Returns the title text.
     *
     * @return the title text
     */
    String getTitle();

    /**
     * Returns the hit type.
     *
     * @return the hit type
     */
    String getType();

    /**
     * Returns the list of hits that use the current hit. If the underlying implementation doesn't support usages
     * computation, this method should return an empty list.
     *
     * @return the list of hits that use the current hit or an empty list if no hits use this hit (or the
     * implementation doesn't support usages computation)
     */
    List<Hit> getUsages();
}

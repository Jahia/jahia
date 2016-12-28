/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * http://www.jahia.com
 *
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ==================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.search;

import java.util.Objects;

/**
 * A class providing details identifying a match from a {@link SearchProvider}.
 *
 * @author Christophe Laprun
 */
public class MatchInfo {
    private final String id;
    private final String workspace;
    private final String lang;

    public MatchInfo(String id, String workspace, String lang) {
        this.id = id;
        this.workspace = workspace;
        this.lang = lang;
    }

    /**
     * Retrieves the identifier associated with the identified search result, usually the target node JCR identifier.
     *
     * @return the identifier associated with the identified search result
     */
    public String getId() {
        return id;
    }

    /**
     * Retrieves the JCR workspace associated with this match.
     *
     * @return the JCR workspace associated with this match
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Retrieves the language code associated with this match.
     *
     * @return the language code associated with this match
     */
    public String getLang() {
        return lang;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getWorkspace(), getLang());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof MatchInfo) {
            MatchInfo other = (MatchInfo) obj;
            return Objects.equals(getId(), other.getId()) && Objects.equals(getWorkspace(), other.getWorkspace())
                    && Objects.equals(getLang(), other.getLang());
        } else {
            return false;
        }
    }
}

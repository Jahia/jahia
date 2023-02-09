/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.pwdpolicy;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.collections.ComparatorUtils;

/**
 * Represents a history entry for the user password (encrypted).
 * 
 * @author Sergiy Shyrkov
 */
public class PasswordHistoryEntry implements Comparable<PasswordHistoryEntry>, Serializable {

    /**
     * Initializes an instance of this class.
     * 
     * @param password
     * @param modificationDate
     */
    public PasswordHistoryEntry(String password, Date modificationDate) {
        super();
        this.password = password;
        this.modificationDate = modificationDate;
    }

    private static final long serialVersionUID = -3097158027608649414L;

    private String password;

    private Date modificationDate;

    @SuppressWarnings("unchecked")
    public int compareTo(PasswordHistoryEntry o) {
        return ComparatorUtils.naturalComparator().compare(
                o.getModificationDate(), getModificationDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PasswordHistoryEntry that = (PasswordHistoryEntry) o;

        return getModificationDate().equals(that.getModificationDate());
    }

    @Override
    public int hashCode() {
        return getModificationDate() != null ? getModificationDate().hashCode()
                : 0;
    }

    public String getPassword() {
        return password;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

}

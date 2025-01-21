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
package org.jahia.params.valves;

import java.io.Serializable;
import java.util.UUID;

/**
 * Cookie authentication valve configuration.
 *
 * @author Sergiy Shyrkov
 */
public class CookieAuthConfig implements Serializable {

    private static final long serialVersionUID = 1575999842721969622L;

    private boolean activated;

    private String cookieName = "jid";

    private boolean httpOnly = true;

    @Deprecated(since = "7.2.2.0")
    private int idLength = 40;

    private int maxAgeInSeconds = 2592000;

    private boolean renewalActivated;

    private boolean secure;

    private String userPropertyName = "org.jahia.user.cookieauth.id";

    public String getCookieName() {
        return cookieName;
    }

    /**
     * @deprecated the mechanism to generate the cookie value is using now the {@link UUID} class, so this length parameter is no longer
     *             effective.
     */
    @Deprecated(since = "7.2.2.0")
    public int getIdLength() {
        return idLength;
    }

    public int getMaxAgeInSeconds() {
        return maxAgeInSeconds;
    }

    public String getUserPropertyName() {
        return userPropertyName;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public boolean isRenewalActivated() {
        return renewalActivated;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setActivated(boolean activted) {
        this.activated = activted;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * @deprecated the mechanism to generate the cookie value is using now the {@link UUID} class, so this length parameter is no longer
     *             effective.
     */
    @Deprecated(since = "7.2.2.0")
    public void setIdLength(int idLength) {
        this.idLength = idLength;
    }

    public void setMaxAgeInSeconds(int maxAgeInSeconds) {
        this.maxAgeInSeconds = maxAgeInSeconds;
    }

    public void setRenewalActivated(boolean renewalActivated) {
        this.renewalActivated = renewalActivated;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setUserPropertyName(String userPropertyName) {
        this.userPropertyName = userPropertyName;
    }

}

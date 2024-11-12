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
package org.jahia.services.pwd;

import org.jasypt.digest.StringDigester;

/**
 * Default password digester implementation which uses supplied {@link StringDigester}.
 *
 * @author Sergiy Shyrkov
 */
public class JasyptPasswordDigester implements PasswordDigester {

    private boolean defaultDigester;

    private String id;

    private StringDigester jasyptDigester;

    /**
     * Initializes an instance of this class.
     *
     * @param id
     *            the password digester ID
     * @param jasyptDigester
     *            the {@link StringDigester} instance to be used for password hashing and checks
     */
    public JasyptPasswordDigester(String id, StringDigester jasyptDigester) {
        super();
        this.id = id.toLowerCase();
        this.jasyptDigester = jasyptDigester;
    }

    @Override
    public String digest(String password) {
        return jasyptDigester.digest(password);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isDefault() {
        return defaultDigester;
    }

    @Override
    public boolean matches(String password, String digest) {
        return jasyptDigester.matches(password, digest);
    }

    /**
     * Enforces this digester to override the default platform's one.
     *
     * @param isDefault
     *            should this digester become the default one?
     */
    public void setDefault(boolean isDefault) {
        this.defaultDigester = isDefault;
    }

    /**
     * Sets the unique identifier of this digester.
     *
     * @param id
     *            the unique identifier of this digester
     */
    public void setId(String id) {
        this.id = id.toLowerCase();
    }

    public void setJasyptDigester(StringDigester jasyptDigester) {
        this.jasyptDigester = jasyptDigester;
    }

}

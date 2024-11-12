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
package org.apache.jackrabbit.core.security;

import org.apache.jackrabbit.core.security.authentication.CredentialsCallback;

import java.io.IOException;

import javax.jcr.SimpleCredentials;
import javax.security.auth.callback.*;
import javax.security.sasl.RealmCallback;

/**
 * Digital Experience Manager specific login callback handler.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaCallbackHandler implements CallbackHandler {

    private SimpleCredentials credentials;

    /**
     * Initializes an instance of this class.
     *
     * @param credentials
     */
    public JahiaCallbackHandler(SimpleCredentials credentials) {
        super();
        this.credentials = credentials;
    }

    public SimpleCredentials getCredentials() {
        return credentials;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof CredentialsCallback) {
                ((CredentialsCallback) callback).setCredentials(credentials);
            } else if (callback instanceof NameCallback) {
                ((NameCallback) callback).setName(credentials.getUserID());
            } else if (callback instanceof PasswordCallback) {
                ((PasswordCallback) callback).setPassword(credentials.getPassword());
            } else if (callback instanceof RealmCallback) {
                ((RealmCallback)callback).setText((String) credentials.getAttribute(JahiaLoginModule.REALM_ATTRIBUTE));
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

}

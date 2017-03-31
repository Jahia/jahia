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

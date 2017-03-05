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
package org.jahia.utils;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Miscellaneous encryption utilities.
 * 
 * @author Sergiy Shyrkov
 */
public final class EncryptionUtils {
    public static class EncryptedPasswordFactoryBean extends AbstractFactoryBean<String> {

        private String password;

        @Override
        protected String createInstance() throws Exception {
            if (this.password == null) {
                return null;
            }

            return passwordBaseDecrypt(password);
        }

        @Override
        public Class<?> getObjectType() {
            return String.class;
        }

        public void setPassword(String password) {
            this.password = password;
        }

    }

    /**
     * Bi-directional password base decryption of the provided text.
     * 
     * @param encrypted
     *            the text to be decrypted
     * @return password base decrypted text
     */
    public static String passwordBaseDecrypt(String encrypted) {
        return org.jahia.commons.encryption.EncryptionUtils.passwordBaseDecrypt(encrypted);
    }

    /**
     * Bi-directional password base encryption of the provided text.
     * 
     * @param source
     *            the text to be encrypted
     * @return password base encrypted text
     */
    public static String passwordBaseEncrypt(String source) {
        return org.jahia.commons.encryption.EncryptionUtils.passwordBaseEncrypt(source);
    }

    /**
     * Created the Base64 encoded SHA-1 digest of the provided text. The method is introduced for compatibility with the password encryption
     * in Jahia prior to 6.5.
     * 
     * @param source
     *            the source text to be digested
     * @return the Base64 encoded SHA-1 digest of the provided text
     */
    public static String sha1DigestLegacy(String source) {
        return org.jahia.commons.encryption.EncryptionUtils.sha1DigestLegacy(source);
    }

    /**
     * Initializes an instance of this class.
     */
    private EncryptionUtils() {
        super();
    }
}

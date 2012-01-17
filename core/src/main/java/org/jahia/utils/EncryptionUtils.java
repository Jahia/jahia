/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.codec.binary.Base64;
import org.jasypt.digest.ByteDigester;
import org.jasypt.digest.PooledByteDigester;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
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

    private static ByteDigester sha1DigesterLegacy;

    private static StringEncryptor stringEncryptor;

    private static ByteDigester getSHA1DigesterLegacy() {
        if (sha1DigesterLegacy == null) {
            synchronized (EncryptionUtils.class) {
                if (sha1DigesterLegacy == null) {
                    // StandardByteDigester digester = new StandardByteDigester();
                    PooledByteDigester digester = new PooledByteDigester();
                    digester.setAlgorithm("SHA-1");
                    digester.setSaltSizeBytes(0);
                    digester.setIterations(1);
                    digester.setPoolSize(4);
                    sha1DigesterLegacy = digester;
                }
            }
        }

        return sha1DigesterLegacy;
    }

    private static StringEncryptor getStringEncryptor() {
        if (stringEncryptor == null) {
            synchronized (EncryptionUtils.class) {
                if (stringEncryptor == null) {
                    StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
                    encryptor.setPassword(new String(new byte[] { 74, 97, 104, 105, 97, 32, 120,
                            67, 77, 32, 54, 46, 53 }));
                    // encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
                    stringEncryptor = encryptor;
                }
            }
        }

        return stringEncryptor;
    }

    /**
     * Bi-directional password base decryption of the provided text.
     * 
     * @param encrypted
     *            the text to be decrypted
     * @return password base decrypted text
     */
    public static String passwordBaseDecrypt(String encrypted) {
        return getStringEncryptor().decrypt(encrypted);
    }

    /**
     * Bi-directional password base encryption of the provided text.
     * 
     * @param source
     *            the text to be encrypted
     * @return password base encrypted text
     */
    public static String passwordBaseEncrypt(String source) {
        return getStringEncryptor().encrypt(source);
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
        return new String(Base64.encodeBase64(getSHA1DigesterLegacy().digest(source.getBytes())));
    }

    /**
     * Initializes an instance of this class.
     */
    private EncryptionUtils() {
        super();
    }

    public static void main(String[] args) {
        System.out.println("\nJahia 6.6 Password Encryption Tool");
        System.out
                .println("Copyright 2002-2012 - Jahia Solutions Group SA http://www.jahia.com - All Rights Reserved\n");
        String password = null;
        if (args.length > 0) {
            password = args[0];
        } else {
            while (password == null || password.length() == 0) {
                System.out
                        .print("Provide a password you would like to encrypt (SHA-1 + Base64): ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                try {
                    password = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (password != null && password.length() > 0) {
            System.out.println("Encrypted password for \"" + password + "\" is: "
                    + sha1DigestLegacy(password));
        }
    }
}

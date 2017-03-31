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
package org.jahia.services.pwd;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jahia.utils.EncryptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized service for password hashing and checking operations.
 * 
 * @author Sergiy Shyrkov
 */
public class PasswordService {

    private static final PasswordService instance = new PasswordService();

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    /**
     * Returns an instance of this service.
     * 
     * @return an instance of this service
     */
    public static PasswordService getInstance() {
        return instance;
    }

    private PasswordDigester defaultDigester;

    private Map<String, PasswordDigester> digesters = new ConcurrentHashMap<>();

    private PasswordDigester enforcedDigester;

    private PasswordDigester strongDigester;

    /**
     * Create a digest of the provided password, adding the used digester ID as a prefix.
     * 
     * @param password
     *            the clear text password to be hashed
     * @return the digest of the provided password, prefixed with an ID of the used digester
     */
    public String digest(String password) {
        return digest(password, false);
    }

    /**
     * Create a digest of the provided password, adding the used digester ID as a prefix.
     * 
     * @param password
     *            the clear text password to be hashed
     * @param strong
     *            if the configured string digester should be used (mainly for root user the stronger digester is used than the default one)
     * @return the digest of the provided password, prefixed with an ID of the used digester
     */
    public String digest(String password, boolean strong) {
        if (StringUtils.isEmpty(password)) {
            return null;
        }

        PasswordDigester digester = strong && strongDigester != null ? strongDigester : getDefaultDigester();
        if (digester == null) {
            return EncryptionUtils.sha1DigestLegacy(password);
        }

        return digester.getId().length() > 0 ? (digester.getId() + ":" + digester.digest(password))
                : digester.digest(password);
    }

    protected PasswordDigester getDefaultDigester() {
        return enforcedDigester != null ? enforcedDigester : defaultDigester;
    }

    /**
     * Checks, if the provided clear text password matches the specified digest, considering all aspects like salt, hashing iterations, etc.
     * It is expected that the digest is prefixed with a digester ID to be used to the check. If there is no prefix, the legacy SHA-1 digest
     * (no salt, 1 iteration) will be used.
     * 
     * @param password
     *            the clear text password to be checked
     * @param prefixedDigest
     *            the digest against which the password will be matched; it is expected that the digest is prefixed with a digester ID to be
     *            used to the check
     * @return <code>true</code>, if the provided password matches its hashed equivalent
     */
    public boolean matches(String password, String prefixedDigest) {
        if (password == null || prefixedDigest == null) {
            return false;
        }
        int pos = prefixedDigest.indexOf(':');
        if (pos == -1) {
            // use legacy digest
            return prefixedDigest.equals(EncryptionUtils.sha1DigestLegacy(password));
        }

        String digesterId = prefixedDigest.substring(0, pos);
        PasswordDigester digester = digesters.get(digesterId);
        if (null == digester) {
            logger.warn("Unable to find registered password digester for ID {}." + " Skipping password match check.",
                    digesterId);
            return false;
        }

        return digester.matches(password, prefixedDigest.substring(pos + 1));
    }

    /**
     * Adds the password digester into the registry.
     * 
     * @param digester
     *            the password digester to be registered
     */
    public void registerDigester(PasswordDigester digester) {
        digesters.put(digester.getId(), digester);
        logger.info("Registered password digester {}", digester.getId());
        if (digester.isDefault()) {
            if (enforcedDigester != null) {
                logger.warn(
                        "Multiple default password digesters registered." + " The digester {} is overridden by the {}.",
                        enforcedDigester.getId(), digester.getId());
                enforcedDigester = digester;
            }
        }
    }

    public void setDefaultDigester(PasswordDigester defaultDigester) {
        this.defaultDigester = defaultDigester;
        if (defaultDigester != null) {
            registerDigester(defaultDigester);
        }
    }

    public void setDigesters(List<PasswordDigester> digesters) {
        this.digesters = new ConcurrentHashMap<>();
        if (digesters != null) {
            for (PasswordDigester pwd : digesters) {
                registerDigester(pwd);
            }
        }
    }

    public void setStrongDigester(PasswordDigester strongDigester) {
        this.strongDigester = strongDigester;
    }

    /**
     * Adds the password digester to unregister.
     * 
     * @param digesterId
     *            the ID of the password digester to be removed from the registry
     */
    public void unregisterDigester(String digesterId) {
        logger.info("Unregistered password digester {}", digesterId);
        digesters.remove(digesterId);
        if (enforcedDigester != null && enforcedDigester.getId().equals(digesterId)) {
            enforcedDigester = null;
        }
    }
}

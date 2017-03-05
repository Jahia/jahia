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
package org.jahia.services.pwdpolicy;

import java.io.Writer;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.time.FastDateFormat;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRUserNode;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Business object controller class for the Jahia Password Policy Service.
 * 
 * @author Sergiy Shyrkov
 */
class JahiaPasswordPolicyManager {

    private static final String HISTORY_NODENAME = "passwordHistory";

    private static final FastDateFormat NODENAME_FORMAT = FastDateFormat
            .getInstance("yyyy-MM-dd-HH-mm-ss");

    private static final String POLICY_NODENAME = "passwordPolicy";

    private static final String POLICY_NODETYPE = "jnt:passwordPolicy";

    private static final String POLICY_PROPERTY = "j:policy";

    private static volatile XStream serializer;

    private static XStream createSerializer() {
        XStream xstream = new XStream(new XppDriver() {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new CompactWriter(out, getNameCoder());
            }
        });
        xstream.alias("password-policy", JahiaPasswordPolicy.class);
        xstream.alias("rule", JahiaPasswordPolicyRule.class);
        xstream.alias("param", JahiaPasswordPolicyRuleParam.class);

        return xstream;
    }

    private static XStream getSerializer() {
        if (serializer == null) {
            synchronized (JahiaPasswordPolicyManager.class) {
                if (serializer == null) {
                    serializer = createSerializer();
                }
            }
        }

        return serializer;
    }

    /**
     * Returns the default password policy.
     * 
     * @return the default password policy
     * @throws RepositoryException
     *             in case of a JCR error
     */
    public JahiaPasswordPolicy getDefaultPolicy() throws RepositoryException {
        return JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<JahiaPasswordPolicy>() {
                    public JahiaPasswordPolicy doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        JahiaPasswordPolicy policy = null;
                        try {
                            JCRNodeWrapper policyNode = session.getNode("/"
                                    + POLICY_NODENAME);
                            String serializedPolicy = policyNode.getProperty(
                                    POLICY_PROPERTY).getString();
                            if (serializedPolicy != null) {
                                policy = (JahiaPasswordPolicy) getSerializer().fromXML(serializedPolicy);
                            }
                        } catch (PathNotFoundException e) {
                            // no policy was persisted yet
                        }

                        return policy;
                    }
                });
    }

    /**
     * Returns the (encrypted) password history map, sorted by change date
     * descending, i.e. the newer passwords are at the top of the list.
     * 
     * @return the (encrypted) password history list, sorted by change date
     *         descending, i.e. the newer passwords are at the top of the list
     * @throws RepositoryException
     *             in case of a JCR error
     */
    public List<PasswordHistoryEntry> getPasswordHistory(final JCRUserNode user) throws RepositoryException {
        List<PasswordHistoryEntry> pwds;
        try {
            pwds = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<List<PasswordHistoryEntry>>() {
                public List<PasswordHistoryEntry> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    List<PasswordHistoryEntry> entries = new LinkedList<>();

                    for (@SuppressWarnings("unchecked")
                    Iterator<JCRNodeWrapper> iterator = session.getNode(user.getPath()).getNode(HISTORY_NODENAME)
                            .getNodes(); iterator.hasNext();) {
                        JCRNodeWrapper historyEntryNode = iterator.next();
                        entries.add(
                                new PasswordHistoryEntry(historyEntryNode.getPropertyAsString(JCRUserNode.J_PASSWORD),
                                        historyEntryNode.getProperty(Constants.JCR_CREATED).getDate().getTime()));
                    }
                    return entries;
                }
            });
            Collections.sort(pwds);
        } catch (PathNotFoundException e) {
            // ignore
            pwds = Collections.emptyList();
        }

        return pwds;
    }

    /**
     * Stores the current user's password into password history.
     * 
     * @param user
     *            the user to store password history for
     * @throws RepositoryException
     *             in case of a JCR error
     */
    public void storePasswordHistory(final JCRUserNode user) throws RepositoryException {
        JCRNodeWrapper pwdHistory = user.getNode(HISTORY_NODENAME);
        JCRNodeWrapper entry = pwdHistory.addNode(
                JCRContentUtils.findAvailableNodeName(pwdHistory, "pwd-" + NODENAME_FORMAT.format(System.currentTimeMillis())),
                "jnt:passwordHistoryEntry");
        entry.setProperty(JCRUserNode.J_PASSWORD, user.getProperty(JCRUserNode.J_PASSWORD).getString());
    }

    /**
     * Updates the specified policy.
     * 
     * @param policy
     *            the policy to update
     * @throws RepositoryException
     *             in case of a JCR error
     */
    public synchronized void update(final JahiaPasswordPolicy policy) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper policyNode = null;
                try {
                    policyNode = session.getNode("/" + POLICY_NODENAME);
                } catch (PathNotFoundException e) {
                    // no policy was persisted yet -> create it
                    JCRNodeWrapper root = session.getRootNode();
                    session.checkout(root);
                    policyNode = root.addNode(POLICY_NODENAME, POLICY_NODETYPE);
                }
                policyNode.setProperty(POLICY_PROPERTY, getSerializer().toXML(policy));

                session.save();

                return Boolean.TRUE;
            }
        });
    }

}

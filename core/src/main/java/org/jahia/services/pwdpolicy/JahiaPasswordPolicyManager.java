/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
 *
 *      INCLUDED LICENSES:
 *
 *      Indiana University Extreme! Lab Software License Version 1.1.1
 *
 *      Copyright (c) 2002 Extreme! Lab, Indiana University. All rights reserved.
 *
 *      Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 *      the following conditions are met:
 *
 *      1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *         following disclaimer.
 *
 *      2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *         following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *      3. The end-user documentation included with the redistribution, if any, must include the following
 *         acknowledgment:
 *
 *      "This product includes software developed by the Indiana University Extreme! Lab
 *       (http://www.extreme.indiana.edu/)."
 *
 *      Alternately, this acknowledgment may appear in the software itself, if and wherever such third-party
 *      acknowledgments normally appear.
 *
 *      4. The names "Indiana University" and "Indiana University Extreme! Lab" must not be used to endorse or promote
 *         products derived from this software without prior written permission. For written permission, please contact
 *          (http://www.extreme.indiana.edu/).
 *
 *      5. Products derived from this software may not use "Indiana University" name nor may "Indiana University"
 *         appear in their name, without prior written permission of the Indiana University.
 *
 *     THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *     IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *     AUTHORS, COPYRIGHT HOLDERS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *     EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *     LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 *     IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

    private static class Holder {
        static XStream serializer = createSerializer();

        private static XStream createSerializer() {
            XStream xstream = new XStream(new XppDriver() {
                @Override
                public HierarchicalStreamWriter createWriter(Writer out) {
                    return new CompactWriter(out, getNameCoder());
                }
            });
            XStream.setupDefaultSecurity(xstream);
            xstream.allowTypes(
                    new Class[] { JahiaPasswordPolicy.class, JahiaPasswordPolicyRule.class, JahiaPasswordPolicyRuleParam.class });
            xstream.alias("password-policy", JahiaPasswordPolicy.class);
            xstream.alias("rule", JahiaPasswordPolicyRule.class);
            xstream.alias("param", JahiaPasswordPolicyRuleParam.class);

            return xstream;
        }
    }

    private static XStream getSerializer() {
        return Holder.serializer;
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
                    policyNode.setAclInheritanceBreak(true);
                }
                policyNode.setProperty(POLICY_PROPERTY, getSerializer().toXML(policy));

                session.save();

                return Boolean.TRUE;
            }
        });
    }

}

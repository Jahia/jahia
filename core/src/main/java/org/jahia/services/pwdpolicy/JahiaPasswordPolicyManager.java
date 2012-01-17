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
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUser;

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

	private static final XStream SERIALIZER;

	static {
		SERIALIZER = new XStream(new XppDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(Writer out) {
				return new CompactWriter(out, xmlFriendlyReplacer());
			}
		});
		SERIALIZER.alias("password-policy", JahiaPasswordPolicy.class);
		SERIALIZER.alias("rule", JahiaPasswordPolicyRule.class);
		SERIALIZER.alias("param", JahiaPasswordPolicyRuleParam.class);
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
						        policy = (JahiaPasswordPolicy) SERIALIZER.fromXML(serializedPolicy);
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
	public List<PasswordHistoryEntry> getPasswordHistory(final JahiaUser user)
	        throws RepositoryException {
		return JCRTemplate.getInstance().doExecuteWithSystemSession(
		        new JCRCallback<List<PasswordHistoryEntry>>() {
			        public List<PasswordHistoryEntry> doInJCR(JCRSessionWrapper session)
			                throws RepositoryException {
				        List<PasswordHistoryEntry> pwds = Collections.emptyList();
				        try {
					        pwds = new LinkedList<PasswordHistoryEntry>();
					        for (@SuppressWarnings("unchecked")
					        Iterator<JCRNodeWrapper> iterator = ((JCRUser) user).getNode(session)
					                .getNode(HISTORY_NODENAME).getNodes(); iterator
					                .hasNext();) {
						        JCRNodeWrapper historyEntryNode = (JCRNodeWrapper) iterator.next();
						        pwds.add(new PasswordHistoryEntry(historyEntryNode
						                .getPropertyAsString("j:password"), historyEntryNode
						                .getProperty(Constants.JCR_CREATED).getDate().getTime()));
					        }
					        Collections.sort(pwds);
				        } catch (PathNotFoundException e) {
					        // ignore
					        pwds = Collections.emptyList();
				        }

				        return pwds;
			        }
		        });
	}

	/**
	 * Stores the current user's password into password history.
	 * 
	 * @param user
	 *            the user to store password history for
	 * @throws RepositoryException
	 *             in case of a JCR error
	 */
	public void storePasswordHistory(final JahiaUser user) throws RepositoryException {
		JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
			public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
				JCRNodeWrapper pwdHistory = ((JCRUser) user).getNode(session).getNode(
				        HISTORY_NODENAME);
				session.checkout(pwdHistory);
				JCRNodeWrapper entry = pwdHistory.addNode(
				        JCRContentUtils.findAvailableNodeName(pwdHistory,
				                "pwd-" + NODENAME_FORMAT.format(System.currentTimeMillis())),
				        "jnt:passwordHistoryEntry");
				entry.setProperty("j:password", user.getProperty("j:password"));
				session.save();
				return true;
			}
		});
	}

	/**
	 * Updates the specified policy.
	 * 
	 * @param policy
	 *            the policy to update
	 * @throws RepositoryException
	 *             in case of a JCR error
	 */
	public void update(final JahiaPasswordPolicy policy) throws RepositoryException {
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
				policyNode.setProperty(POLICY_PROPERTY, SERIALIZER.toXML(policy));

				session.save();

				return Boolean.TRUE;
			}
		});
	}

}

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.usermanager;

import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

/**
 * <p>Title: Stores server configuration as described in xml router files.</p>
 * <p>Description: completely specify a server associated to a provider.</p>
 *
 * @author EP
 * @version 3.0
 */
public class ServerBean {

	// Map to store parameters for public context
	private Map publicParameters;
	// Map to store parameters for private context
	private Map privateParameters;
	
	// the priority of the server
	private int priority;
	// the max tries for reconnection
	private int maxReconn;
	// the provider associated to this server
	private String provider;

	private static final int DEFAULT_PRIORITY = 99;
	private static final int DEFAULT_RECONN = 1;
	
	/**
	 * Constructor.
	 * @param newPriority String, the priority read from xml file.
	 * @param newProvider String, the provider read from xml file.
	 * @param newMaxReconn String, the maximum reconnection tries read from xml file.
	 */
	public ServerBean (String newPriority, String newProvider, String newMaxReconn) {
		try {
			priority = Integer.parseInt (newPriority);
        	} catch (Exception e) {
            		priority = DEFAULT_PRIORITY;
        	}

		try {
			maxReconn = Integer.parseInt (newMaxReconn);
        	} catch (Exception e) {
            		maxReconn = DEFAULT_RECONN;
        	}
        	
		provider = newProvider;
		
		publicParameters = new HashMap(11);
		privateParameters = new HashMap(11);
	}
	
	/**
	 * Return this server priority.
	 * @return String.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Return this server provider.
	 * @return String.
	 */	
	public String getProvider() {
		return provider;
	}

	/**
	 * Return this server maximum reconnection tries.
	 * @return int.
	 */
	public int getMaxReconnection() {
		return maxReconn;
	}
		
	/**
	 * Set this server factory name.
	 * @param factoryName String, get from xml configuration file.
	 */
	public void setFactoryName(String factoryName) {
		if (factoryName != null && factoryName.length() > 0) {
			publicParameters.put (Context.INITIAL_CONTEXT_FACTORY, factoryName);
			privateParameters.put (Context.INITIAL_CONTEXT_FACTORY, factoryName);
		}
	}

	/**
	 * Set this server url.
	 * @param url String, get from xml configuration file.
	 */	
	public void setServerUrl(String url) {
		if (url != null && url.length() > 0) {
			publicParameters.put (Context.PROVIDER_URL, url);
			privateParameters.put (Context.PROVIDER_URL, url);
		}
	}
	
	/**
	 * Set this server factory userName.
	 * @param userName String, get from xml configuration file.
	 */
	public void setUserName(String userName) {
		if (userName != null && userName.length() > 0) 
			publicParameters.put (Context.SECURITY_PRINCIPAL, userName);
	}
	
	/**
	 * Set this server authentication mode.
	 * @param authenticationMode String, get from xml configuration file.
	 */
	public void setAuthenticationMode(String authenticationMode) {
		if (authenticationMode != null && authenticationMode.length() > 0) {
			publicParameters.put (Context.SECURITY_AUTHENTICATION, authenticationMode);
			privateParameters.put (Context.SECURITY_AUTHENTICATION, authenticationMode);
		}
	}
	
	/**
	 * Set this server password.
	 * @param password String, get from xml configuration file.
	 */
	public void setUserPassword(String password) {
		if (password != null && password.length() > 0) 
			publicParameters.put (Context.SECURITY_CREDENTIALS, password);
	}
	
	/**
	 * Set this server behavior.
	 * @param behavior String, get from xml configuration file.
	 */
	public void setReferralBehavior (String behavior) {
		if (behavior != null && behavior.length() > 0) {
			publicParameters.put (Context.REFERRAL, behavior);
			privateParameters.put (Context.REFERRAL, behavior);
		}
	}
	
	/**
	 * Return the parameters for public connection.
	 * @return Map.
	 */
	public Map getPublicConnectionParameters() {
		return publicParameters;
	}

	/**
	 * Return the parameters for public connection.
	 * @param userName String, the userName for a private connection.
	 * @param password String, the password for a private connection.
	 * @return Map.
	 */
	public Map getPrivateConnectionParameters(String userName, String password) {
		privateParameters.put (Context.SECURITY_CREDENTIALS, password);
		privateParameters.put (Context.SECURITY_PRINCIPAL, userName);
		
		return privateParameters;
	}
		
	/**
	 * Return the frinedly description of this object.
	 * @return String.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer("ServerBean[");
		sb.append(maxReconn);
		sb.append("::");
		sb.append(provider);
		sb.append("::");
		sb.append(priority);
		sb.append("::");
		sb.append(publicParameters);
		sb.append("]");
		
		return sb.toString();
	}
}